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
		private int mReadBytes;
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
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int r = super.read(b, off, len);
			int end = off + r;
			for (int i = off; i < end; i++, mReadBytes++) {
				b[i] ^= mKeys[mReadBytes % mKeys.length];
			}
			return r;
		}
	}
}
