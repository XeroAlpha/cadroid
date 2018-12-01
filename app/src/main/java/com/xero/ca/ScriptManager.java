package com.xero.ca;

import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.tendcloud.tenddata.TCAgent;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.StackStyle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.WeakReference;

public class ScriptManager {
    private static WeakReference<ScriptManager> instance = new WeakReference<>(null);

    static {
        RhinoException.setStackStyle(StackStyle.V8);
    }

    private Context cx = null;
    private Scriptable scope = null;
    private Handler handler = null;
    private Activity bindActivity = null;
    private String debugFile = null;
    private Hotfix hotfix = null;
    private boolean running = false;

    private ScriptManager() {}

    public synchronized static ScriptManager getInstance() {
        ScriptManager m = instance.get();
        if (m == null) {
            m = new ScriptManager();
            instance = new WeakReference<>(m);
        }
        return m;
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

    public synchronized void endScript(boolean callUnload) {
        if (!running || handler == null) return;
        running = false;
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

    public void setHotfix(String coreFile, String signFile, byte[] verify, int versionCode) {
        hotfix = new Hotfix(coreFile, signFile, verify, versionCode);
    }

    public synchronized Context initContext() {
        //Context context = Context.enter();
        Context context = new com.faendir.rhino_android.RhinoAndroidHelper().enterContext();
        context.setOptimizationLevel(-1);
        return context;
    }

    public Scriptable initScope(Context cx) {
        Scriptable s = cx.initStandardObjects();
        s.put("ScriptActivity", s, bindActivity);
        s.put("TCAgent", s, new NativeJavaClass(s, TCAgent.class));
        return s;
    }

    public Reader getScriptReader() throws IOException {
        if (debugFile != null) return new FileReader(debugFile);
        if (hotfix != null) {
            try {
                return new InputStreamReader(hotfix.getInputStream());
            } catch (IOException e) {
                Log.e("CA", "Loading hotfix failed", e);
            }
        }
        return new InputStreamReader(ScriptFileStream.fromAsset(bindActivity, "script.js"));
    }

    public Context getContext() {
        return cx;
    }

    public Scriptable getScope() {
        return scope;
    }

    public Activity getBindActivity() {
        return bindActivity;
    }

    public Handler getHandler() {
        return handler;
    }

    public InputStream open(String path) throws IOException {
        if (debugFile != null) {
            File src = new File(debugFile);
            return new FileInputStream(new File(src.getParent(), path));
        } else if (bindActivity != null) {
            return bindActivity.getAssets().open(path);
        }
        return null;
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
            Context.exit();
            cx = null;
            scope = null;
            handler = null;
        }
    }

    class ExitCommand implements Runnable {
        boolean callUnload;

        ExitCommand(boolean callUnload) {
            this.callUnload = callUnload;
        }

        @Override
        public void run() {
            if (callUnload) callScriptHook("unload", new Object[]{});
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                handler.getLooper().quitSafely();
            } else {
                handler.getLooper().quit();
            }
        }
    }

    class Hotfix {
        public Hotfix(String coreFile, String signFile, byte[] verify, int versionCode) {
            this.coreFile = coreFile;
            this.signFile = signFile;
            this.verify = verify;
            this.versionCode = versionCode;
        }

        private String coreFile;
        private String signFile;
        private byte[] verify;
        private int versionCode;

        public InputStream getInputStream() throws IOException {
            return ScriptFileStream.fromFile(bindActivity, coreFile, signFile, verify, versionCode);
        }
    }
}
