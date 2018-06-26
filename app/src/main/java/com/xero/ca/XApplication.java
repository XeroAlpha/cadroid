package com.xero.ca;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import com.tendcloud.tenddata.TCAgent;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public class XApplication extends Application {
    public static final String TAG = "CA";

    public static String getPackageVersion(Context ctx) {
        try {
            return ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            //do nothing
        }
        return null;
    }

    public static void reportError(Context ctx, Thread t, Throwable e) {
        TCAgent.onError(ctx, e);
        Log.e(TAG, t.toString(), e);
        Intent i = new Intent(ctx, BugReportActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        StringWriter s = new StringWriter();
        s.append("Version ").append(getPackageVersion(ctx)).append("\n");
        e.printStackTrace(new PrintWriter(s));
        i.putExtra("exception", s.toString());
        try {
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

    @Override
    public void onCreate() {
        super.onCreate();
        TCAgent.init(this.getApplicationContext(), "70066D88E1654D9F988993CFF0A2A216", "default");
        Thread.setDefaultUncaughtExceptionHandler(new ErrorCaughter());
    }

    public class ErrorCaughter implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            reportError(XApplication.this, t, e);
        }
    }
}
