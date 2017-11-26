package com.xero.ca;
import android.app.*;
import android.content.*;
import java.io.*;
import java.util.zip.*;
import org.mozilla.javascript.*;
import android.os.*;

import org.mozilla.javascript.Context;

public class ScriptManager {
	private final static class SealKey {}
	public final static Object sealKey = new SealKey();
	
	private static Context cx = null;
	private static Scriptable scope = null;
	private static Handler handler = null;

	public static void startScript(final Activity ctx) {
		if (handler != null) return;
		Thread th = new Thread(Thread.currentThread().getThreadGroup(), new Runnable() {
				@Override
				public void run() {
					Looper.prepare();
					cx = Context.enter();
					cx.setOptimizationLevel(-1);
					cx.seal(sealKey);
					scope = cx.initStandardObjects();
					scope.put("ScriptActivity", scope, ctx);
					try {
						cx.evaluateReader(scope, new InputStreamReader(new ScriptFileStream(ctx, "script.js")), "命令助手", 0, null);
					} catch (Exception e) {
						showError(ctx, e);
						return;
					}
					handler = new Handler();
					Looper.loop();
					cx.exit();
					cx = null;
					scope = null;
					handler = null;
				}
			}, "CA_Loader", 262144);
		th.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					showError(ctx, e);
				}
			});
		th.start();
	}

	public static void endScript(final boolean callUnload) {
		if (handler == null) return;
		handler.post(new Runnable() {
				@Override
				public void run() {
					if (callUnload) callScriptHook("unload", new Object[] {});
					handler.getLooper().quitSafely();
				}
			});
	}
	
	public static void endScript() {
		endScript(true);
	}
	
	public static boolean isRunning() {
		return handler != null;
	}

	public static void callScriptHook(String name, Object[] args) {
		if (handler == null) return;
		Object obj = scope.get(name, scope);
		if (obj != null && obj instanceof Function) {
			((Function) obj).call(cx, scope, scope, args);
		}
	}

	public static void showError(final Activity ctx, final Throwable e) {
		ctx.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					StringWriter s = new StringWriter();
					e.printStackTrace(new PrintWriter(s));
					new AlertDialog.Builder(ctx)
						.setTitle("Error")
						.setMessage(s.toString())
						.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface p1, int p2) {}
						})
						.show();
				}
			});
	}
}
