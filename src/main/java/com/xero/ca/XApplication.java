package com.xero.ca;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.util.*;
import java.io.*;
import java.util.*;

public class XApplication extends Application {
	public static final String TAG = "CA";
	@Override
	public void onCreate() {
		super.onCreate();
		Thread.setDefaultUncaughtExceptionHandler(new ErrorCaughter());
	}
	
	public static String getPackageVersion(Context ctx) {
		try {
			return ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {}
		return null;
	}
	
	public static void reportError(Context ctx, Thread t, Throwable e) {
		Log.e(TAG, t.toString(), e);
		Intent i = new Intent(ctx, BugReportActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		StringWriter s = new StringWriter();
		s.append("Version ").append(getPackageVersion(ctx)).append("\n");
		e.printStackTrace(new PrintWriter(s));
		i.putExtra("exception", s.toString());
		try	{
			PrintWriter fs = new PrintWriter(new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/com.xero.ca.error.log", true));
			fs.println("* Error: " + new Date().toLocaleString());
			fs.println("FATAL:" + s);
			fs.close();
		} catch (Exception err) {
			Log.e(TAG, "I/O Error", err);
		}
		ctx.startActivity(i);
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(1);
	}
	
	public class ErrorCaughter implements Thread.UncaughtExceptionHandler {
		@Override
		public void uncaughtException(Thread t, Throwable e) {
			reportError(XApplication.this, t, e);
		}
	}
}
