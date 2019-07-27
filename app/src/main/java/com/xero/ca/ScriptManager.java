package com.xero.ca;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
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
    private android.content.Context bindContext = null;
    private ScriptInterface scriptInterface = null;
    private String debugFile = null;
    private Hotfix hotfix = null;
    private boolean running = false;
    private String sourceName;
    private Script compiledScript = null;

    private ScriptManager() {}

    public synchronized static boolean hasInstance() {
        return instance.get() != null;
    }

    public synchronized static ScriptManager getInstance() {
        ScriptManager m = instance.get();
        if (m == null) {
            m = new ScriptManager();
            instance = new WeakReference<>(m);
        }
        return m;
    }

    public static ScriptManager createDebuggable(String debugFile) {
        ScriptManager m = instance.get();
        if (m == null || !m.isRunning()) {
            m = new ScriptManager();
            m.debugFile = debugFile;
            instance = new WeakReference<>(m);
            return m;
        } else {
            return null;
        }
    }

    public synchronized void prepareScript(android.content.Context ctx, String srcName, boolean runAfterPrepared) {
        if (running) return;
        running = true;
        bindContext = ctx;
        sourceName = srcName;
        Thread th = new Thread(Thread.currentThread().getThreadGroup(), new PrepareCommand(runAfterPrepared), "Script_Loader", 262144);
        th.start();
    }

    public synchronized void startScript() {
        if (handler == null) return;
        handler.post(new StartCommand());
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
        if (obj instanceof Function) {
            try {
                ((Function) obj).call(cx, scope, scope, args);
            } catch (Exception ex) {
                //Ignore this exception
            }
        }
    }

    public void setHotfix(String coreFile, String signFile, byte[] verify, int versionCode) {
        hotfix = new Hotfix(coreFile, signFile, verify, versionCode);
    }

    protected synchronized Context initContext() {
        //Context context = Context.enter();
        Context context = new com.faendir.rhino_android.RhinoAndroidHelper().enterContext();
        context.setOptimizationLevel(-1);
        context.setLanguageVersion(Context.VERSION_ES6);
        return context;
    }

    protected Scriptable initScope(Context cx) {
        Scriptable s = cx.initStandardObjects();
        s.put("ScriptInterface", s, getScriptInterface());
        return s;
    }

    protected ScriptInterface initScriptInterface() {
        return new ScriptInterface(this);
    }

    protected Reader getScriptReader() throws IOException {
        if (debugFile != null) {
            try {
                return new FileReader(debugFile);
            } catch (IOException e) {
                Log.e("Script", "Loading debugFile failed", e);
            }
        }
        if (hotfix != null) {
            try {
                return new InputStreamReader(hotfix.getInputStream());
            } catch (IOException e) {
                Log.e("Script", "Loading hotfix failed", e);
            }
        }
        return new InputStreamReader(ScriptFileStream.fromAsset(bindContext, "script.js"));
    }

    public Context getContext() {
        return cx;
    }

    public Scriptable getScope() {
        return scope;
    }

    public android.content.Context getBindContext() {
        return bindContext;
    }

    public ScriptInterface getScriptInterface() {
        return scriptInterface;
    }

    public Handler getHandler() {
        return handler;
    }

    public InputStream open(String path) throws IOException {
        if (debugFile != null) {
            File src = new File(debugFile);
            return new FileInputStream(new File(src.getParent(), path));
        } else if (bindContext != null) {
            return bindContext.getAssets().open(path);
        }
        return null;
    }

    class PrepareCommand implements Runnable {
        private boolean runAfterPrepared;

        public PrepareCommand(boolean runAfterPrepared) {
            this.runAfterPrepared = runAfterPrepared;
        }

        @Override
        public void run() {
            Looper.prepare();
            scriptInterface = initScriptInterface();
            cx = initContext();
            scope = initScope(cx);
            handler = new Handler();
            try {
                compiledScript = cx.compileReader(getScriptReader(), sourceName, 0, null);
            } catch (Exception e) {
                XApplication.reportError(bindContext.getApplicationContext(), Thread.currentThread(), new SecurityException("Fail to decode and execute the script.", e));
                return;
            }
            if (runAfterPrepared) {
                startScript();
            } else {
                scriptInterface.onScriptReady();
            }
            Looper.loop();
            Context.exit();
            cx = null;
            scope = null;
            handler = null;
            scriptInterface = null;
        }
    }

    class StartCommand implements Runnable {
        @Override
        public void run() {
            if (compiledScript != null) {
                compiledScript.exec(cx, scope);
                compiledScript = null;
            }
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
            scriptInterface.clearBridge();
            handler.getLooper().quit();
            debugFile = null;
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
            return ScriptFileStream.fromFile(bindContext, coreFile, signFile, verify, versionCode);
        }
    }
}
