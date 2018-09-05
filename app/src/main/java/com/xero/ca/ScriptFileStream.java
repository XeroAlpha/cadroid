package com.xero.ca;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class ScriptFileStream extends GZIPInputStream {
    public ScriptFileStream(Context cx, InputStream stream) throws IOException {
        super(new ES(cx, stream));
    }

    public static ScriptFileStream fromAsset(Context cx, String fileName) throws IOException {
        return new ScriptFileStream(cx, cx.getAssets().open(fileName));
    }

    public static ScriptFileStream fromFile(Context cx, String sourceFile, String signFile, byte[] verify, int versionCode) throws IOException {
        verifyFile(sourceFile, signFile, verify, versionCode);
        return new ScriptFileStream(cx, new FileInputStream(sourceFile));
    }

    private static void verifyFile(String sourceFile, String signFile, byte[] verify, int versionCode) throws IOException {
        try {
            java.security.Signature signature = java.security.Signature.getInstance("SHA256withRSA");
            KeyFactory kf = KeyFactory.getInstance("RSA");
            KeySpec spec = new X509EncodedKeySpec(verify);
            PublicKey pk = kf.generatePublic(spec);
            signature.initVerify(pk);
            byte[] buf = new byte[4096];
            InputStream is = new FileInputStream(sourceFile);
            int readBytes;
            while ((readBytes = is.read(buf)) >= 0) {
                signature.update(buf, 0, readBytes);
            }
            byte[] sign = getFileBytes(signFile);
            if (sign.length < 4) throw new SignatureException("Signature is not available");
			int signVer = readIntLE(sign, 0);
            if (signVer <= versionCode) throw new SignatureException("Version " + signVer + " is too low");
            if (!signature.verify(sign, 4, sign.length - 4)) {
                throw new SignatureException("Signature not correct");
            }
        } catch (Exception e) {
            throw new IOException("Verification failed", e);
        }
    }

    private static byte[] getFileBytes(String fileName) throws IOException {
        byte[] buf = new byte[4096];
        InputStream is = new FileInputStream(fileName);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int readBytes;
        while ((readBytes = is.read(buf)) >= 0) {
            os.write(buf, 0, readBytes);
        }
        return os.toByteArray();
    }

    private static int readIntLE(byte[] bytes, int offset) {
        return (bytes[offset] & 0xff) |
                ((bytes[offset + 1] & 0xff) << 8) |
                ((bytes[offset + 2] & 0xff) << 16) |
                ((bytes[offset + 3] & 0xff) << 24);
    }

    private static class ES extends FilterInputStream {
        private int mReadBytes, mMark;
        private byte[] mKeys = new byte[]{0};

        @SuppressLint("WrongConstant")
        @SuppressWarnings("NewApi")
        private ES(Context cx, InputStream stream) throws IOException {
            super(stream);
            mReadBytes = 0;
            try {
                List<byte[]> lb = new ArrayList<>();
                int size = 0;
                byte[] t;
                Signature[] signatures;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    signatures = cx.getPackageManager().getPackageInfo(cx.getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES).signingInfo.getApkContentsSigners();
                } else {
                    signatures = cx.getPackageManager().getPackageInfo(cx.getPackageName(), PackageManager.GET_SIGNATURES).signatures;
                }
                for (Signature e : signatures) {
                    t = e.toByteArray();
                    size += t.length;
                    lb.add(t);
                }
                mKeys = new byte[size];
                size = 0;
                for (byte[] be : lb) {
                    System.arraycopy(be, 0, mKeys, size, be.length);
                    size += be.length;
                }
            } catch (Exception e) {
                //do nothing
            }
            if (mKeys.length == 0) mKeys = new byte[]{0};
            mMark = -1;
        }

        /* Bug 修复
         * Android 7.x 无法解码代码
         * 原因：Android 7.x的GZIPInputStream在<init>时
           会调用readHeader，内部调用了int read()。（之前只
           是int read(byte, int, int)读取一个byte[10]）
         * 解决方案：Override int read()
         */
        @Override
        public int read() throws IOException {
            int r = in.read();
            if (r < 0) return r;
            byte b = (byte) r;
            b ^= mKeys[mReadBytes % mKeys.length];
            mReadBytes++;
            r = b < 0 ? b + 256 : b;
            return r;
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int r = in.read(b, off, len);
            int end = off + r;
            for (int i = off; i < end; i++, mReadBytes++) {
                b[i] ^= mKeys[mReadBytes % mKeys.length];
            }
            return r;
        }

        @Override
        public long skip(long n) throws IOException {
            long r = in.skip(n);
            mReadBytes += r;
            return r;
        }

        @Override
        public void mark(int readlimit) {
            in.mark(readlimit);
            mMark = mReadBytes;
        }

        @Override
        public void reset() throws IOException {
            in.reset();
            if (mMark >= 0) mReadBytes = mMark;
        }
    }
}
