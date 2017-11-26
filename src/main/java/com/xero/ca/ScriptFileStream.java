package com.xero.ca;
import android.content.*;
import android.content.pm.*;
import java.io.*;
import java.util.zip.*;
import android.util.*;

public class ScriptFileStream extends GZIPInputStream {
	public ScriptFileStream(Context cx, String fileName) throws IOException {
		super(new ES(cx, fileName));
	}

	private static class ES extends FilterInputStream {
		private int mReadBytes;
		private byte[] mKeys;
		
		public ES(Context cx, String fileName) throws IOException {
			super(cx.getAssets().open(fileName));
			mReadBytes = 0;
			try {
				mKeys = cx.getPackageManager().getPackageInfo(cx.getPackageName(), 64).signatures[0].toByteArray();
			} catch (Exception e) {}
			if (mKeys == null) mKeys = new byte[]{0};
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
