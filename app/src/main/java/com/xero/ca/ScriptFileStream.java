package com.xero.ca;
import android.content.*;
import android.content.pm.*;
import java.io.*;
import java.util.zip.*;
import android.util.*;
import java.util.*;

public class ScriptFileStream extends GZIPInputStream {
	public ScriptFileStream(Context cx, String fileName) throws IOException {
		super(new ES(cx, fileName));
	}

	private static class ES extends FilterInputStream {
		private int mReadBytes, mMark;
		private byte[] mKeys = new byte[]{0};
		
		public ES(Context cx, String fileName) throws IOException {
			super(cx.getAssets().open(fileName));
			mReadBytes = 0;
			try {
				List<byte[]> lb = new ArrayList<>();
				int size = 0;
				byte[] t;
				for (Signature e : cx.getPackageManager().getPackageInfo(cx.getPackageName(), 64).signatures) {
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
			} catch (Exception e) {}
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
