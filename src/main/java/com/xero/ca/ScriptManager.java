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
	private static Activity bindActivity = null;
	private static String debugFile = null;

	public static void startScript(final Activity ctx) {
		if (handler != null) return;
		bindActivity = ctx;
		Thread th = new Thread(Thread.currentThread().getThreadGroup(), new StartCommand(), "CA_Loader", 262144);
		th.setDefaultUncaughtExceptionHandler(new CrashListener());
		th.start();
	}

	public static void endScript(final boolean callUnload) {
		if (handler == null) return;
		handler.post(new ExitCommand(callUnload));
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
							public void onClick(DialogInterface p1, int p2) {
								ctx.finish();
							}
						})
						.show();
				}
			});
	}
	
	public static Context initContext() {
		Context context = Context.enter();
		context.setOptimizationLevel(-1);
		context.seal(sealKey);
		return context;
	}
	
	public static Scriptable initScope(Context cx) {
		Scriptable s = cx.initStandardObjects();
		s.put("ScriptActivity", s, bindActivity);
		return s;
	}
	
	public static void setDebugFile(String path) {
		debugFile = path;
	}
	
	public static Reader getScriptReader() throws IOException {
		if (debugFile != null) return new FileReader(debugFile);
		return new InputStreamReader(new ScriptFileStream(bindActivity, "script.js"));
	}
	
	static class CrashListener implements Thread.UncaughtExceptionHandler {
		@Override
		public void uncaughtException(Thread t, Throwable e) {
			showError(bindActivity, e);
		}
	}
	
	static class StartCommand implements Runnable {
		@Override
		public void run() {
			Looper.prepare();
			cx = initContext();
			scope = initScope(cx);
			try {
				cx.evaluateReader(scope, getScriptReader(), "命令助手", 0, null);
			} catch (Exception e) {
				showError(bindActivity, new SecurityException("Fail to decode and execute the script.", e));
				return;
			}
			handler = new Handler();
			Looper.loop();
			cx.exit();
			cx = null;
			scope = null;
			handler = null;
		}
	}
	
	static class ExitCommand implements Runnable {
		boolean callUnload;
		ExitCommand(boolean callUnload) {
			this.callUnload = callUnload;
		}
		@Override
		public void run() {
			if (callUnload) callScriptHook("unload", new Object[] {});
			handler.getLooper().quitSafely();
		}
	}
}
