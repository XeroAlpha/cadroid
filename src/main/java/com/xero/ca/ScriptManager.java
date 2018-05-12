package com.xero.ca;
import android.app.*;
import android.os.*;
import java.io.*;
import org.mozilla.javascript.*;

public class ScriptManager {
	private final static class SealKey {}
	public final static Object sealKey = new SealKey();
	
	private static ScriptManager instance;
	
	private Context cx = null;
	private Scriptable scope = null;
	private Handler handler = null;
	private Activity bindActivity = null;
	private String debugFile = null;
	private boolean running = false;
	
	public synchronized static ScriptManager getInstance() {
		if (instance == null) {
			instance = new ScriptManager();
		}
		return instance;
	}
	
	public static ScriptManager createDebuggable(String debugFile) {
		ScriptManager r = new ScriptManager();
		r.debugFile = debugFile;
		return r;
	}

	public synchronized void startScript(Activity ctx) {
		if (running) return;
		running = true;
		bindActivity = ctx;
		Thread th = new Thread(Thread.currentThread().getThreadGroup(), new StartCommand(), "CA_Loader", 262144);
		th.start();
	}

	public void endScript(boolean callUnload) {
		if (!running) return;
		handler.post(new ExitCommand(callUnload));
	}
	
	public void endScript() {
		endScript(true);
	}
	
	public boolean isRunning() {
		return running;
	}

	public synchronized void callScriptHook(String name, Object[] args) {
		if (handler == null) return;
		Object obj = scope.get(name, scope);
		if (obj != null && obj instanceof Function) {
			((Function) obj).call(cx, scope, scope, args);
		}
	}
	
	public synchronized static Context initContext() {
		//Context context = Context.enter();
		Context context = new com.faendir.rhino_android.RhinoAndroidHelper().enterContext();
		context.setOptimizationLevel(-1);
		//context.seal(sealKey);
		return context;
	}
	
	public Scriptable initScope(Context cx) {
		Scriptable s = cx.initStandardObjects();
		s.put("ScriptActivity", s, bindActivity);
		return s;
	}
	
	public void setDebugFile(String path) {
		debugFile = path;
	}
	
	public Reader getScriptReader() throws IOException {
		if (debugFile != null) return new FileReader(debugFile);
		return new InputStreamReader(new ScriptFileStream(bindActivity, "script.js"));
	}
	
	public void startDebug(File outputFile) throws FileNotFoundException {
		Context x = Context.getCurrentContext();
		cx.setDebugger(new ScriptDebugger(outputFile), null);
	}
	
	class StartCommand implements Runnable {
		@Override
		public void run() {
			Looper.prepare();
			cx = initContext();
			scope = initScope(cx);
			try {
				cx.evaluateReader(scope, getScriptReader(), "命令助手", 0, null);
			} catch (Exception e) {
				XApplication.reportError(bindActivity.getApplicationContext(), Thread.currentThread(), new SecurityException("Fail to decode and execute the script.", e));
				return;
			}
			handler = new Handler();
			Looper.loop();
			cx.exit();
			cx = null;
			scope = null;
			handler = null;
			running = false;
		}
	}
	
	class ExitCommand implements Runnable {
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
