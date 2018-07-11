package com.xero.ca;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

public class MainActivity extends Activity {
    public static final String ACTION_ADD_LIBRARY = "com.xero.ca.ADD_LIBRARY";
    public static final String ACTION_EDIT_COMMAND = "com.xero.ca.EDIT_COMMAND";
    public static final String ACTION_START_ON_BOOT = "com.xero.ca.START_ON_BOOT";
    public static final String ACTION_START_FROM_BACKGROUND = "com.xero.ca.ACTION_START_FROM_BACKGROUND";
    public static final String ACTION_START_FROM_SHORTCUT = "com.xero.ca.ACTION_START_FROM_SHORTCUT";
    public static final String ACTION_SCRIPT_ACTION = "com.xero.ca.ACTION_SCRIPT_ACTION";

    public static final String ACTION_SHOW_DEBUG = "com.xero.ca.SHOW_DEBUG";
    public static final String ACTION_DEBUG_EXEC = "com.xero.ca.DEBUG_EXEC";

    public static final String SCHEME = "commandassist";

    public static final String SETTING_HIDE_SPLASH = "hideSplash";
    public static final String SETTING_HIDE_NOTIFICATION = "hideNotification";
    public static final String SETTING_START_ON_BOOT = "bootStart";

    public static final String PREFERENCE_NAME = "user_settings";

    public static WeakReference<MainActivity> instance = new WeakReference<>(null);

    private ScriptManager mManager;
    private BridgeListener mBridgeListener;
    private SharedPreferences mPreferences;
    private boolean mShowNotification;
    private Intent mKeeperIntent;
    private boolean mIsForeground;

    private GameBridgeService.Callback gbsCallback = new GameBridgeService.Callback() {
        @Override
        public void onRemoteEnabled() {
            if (mBridgeListener != null) mBridgeListener.onRemoteEnabled();
        }

        @Override
        public void onRemoteMessage(Message msg) {
            if (mBridgeListener != null) mBridgeListener.onRemoteMessage(msg);
        }

        @Override
        public void onRemoteDisabled() {
            if (mBridgeListener != null) mBridgeListener.onRemoteDisabled();
        }
    };

    private AccessibilitySvc.ServiceLifeCycleListener acsCallback = new AccessibilitySvc.ServiceLifeCycleListener() {
        @Override
        public void onCreate() {
            if (mBridgeListener != null) mBridgeListener.onAccessibilitySvcCreate();
        }

        @Override
        public void onDestroy() {
            if (mBridgeListener != null) mBridgeListener.onAccessibilitySvcDestroy();
        }
    };

    public static void callIntent(Context ctx, Intent intent) {
        if (instance.get() != null) {
            instance.get().onNewIntent(intent);
        } else {
            ctx.startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle bundle) {
        mPreferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        instance = new WeakReference<>(this);
        mShowNotification = false;
        showNotification();
        if (getHideSplash() || isSubAction(getIntent().getAction())) {
            onBackPressed();
        } else {
            setTheme(R.style.AppTheme);
            setContentView(R.layout.main);
        }
        super.onCreate(bundle);
        Intent i = getIntent();
        if (ACTION_DEBUG_EXEC.equals(i.getAction()) && i.getData() != null && BuildConfig.DEBUG) {
            mManager = ScriptManager.createDebuggable(i.getData().getPath(), i.getStringExtra("log"));
        } else {
            mManager = ScriptManager.getInstance();
        }
        if (mManager.isRunning()) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
        } else {
            clearBridgeListener();
            mManager.startScript(this);
        }
    }

    @Override
    protected void onResume() {
        mIsForeground = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        mIsForeground = false;
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        if (mBridgeListener != null) mBridgeListener.onNewIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mBridgeListener != null)
            mBridgeListener.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        instance.clear();
        hideNotification();
        clearBridgeListener();
        mManager.endScript(false);
        super.onDestroy();
    }

    private boolean isSubAction(String action) {
        if (action == null) return false;
        return action.equals(ACTION_ADD_LIBRARY) ||
                action.equals(ACTION_START_ON_BOOT) ||
                action.equals(ACTION_START_FROM_BACKGROUND) ||
                action.equals(ACTION_SCRIPT_ACTION);
    }

    public void setBridgeListener(BridgeListener bridgeListener) {
        mBridgeListener = bridgeListener;
        GameBridgeService.setCallback(gbsCallback);
        AccessibilitySvc.setLifeCycleListener(acsCallback);
    }

    public void clearBridgeListener() {
        mBridgeListener = null;
        GameBridgeService.removeCallback();
        AccessibilitySvc.setLifeCycleListener(null);
    }

    public void setLoadingTitle(String title) {
        try {
            ((TextView) findViewById(R.id.loadingTitle)).setText(title);
        } catch (Exception e) {
            //do nothing
        }
    }

    public boolean getHideSplash() {
        return mPreferences.getBoolean(SETTING_HIDE_SPLASH, false);
    }

    public void setHideSplash(boolean v) {
        mPreferences.edit().putBoolean(SETTING_HIDE_SPLASH, v).apply();
    }

    public boolean getHideNotification() {
        return mPreferences.getBoolean(SETTING_HIDE_NOTIFICATION, false);
    }

    public void setHideNotification(boolean v) {
        if (mShowNotification && v) {
            hideNotification();
        } else if (mShowNotification && !v) {
            showNotification();
        }
        mPreferences.edit().putBoolean(SETTING_HIDE_NOTIFICATION, v).apply();
    }

    public boolean getBootStart() {
        return mPreferences.getBoolean(SETTING_START_ON_BOOT, false);
    }

    public void setBootStart(boolean v) {
        mPreferences.edit().putBoolean(SETTING_START_ON_BOOT, v).apply();
    }

    public void showNotification() {
        if (mShowNotification) return;
        mShowNotification = true;
        if (getHideNotification()) return;
        //if (KeeperService.instance != null) return;
        mKeeperIntent = new Intent(this, KeeperService.class);
        startService(mKeeperIntent);
    }

    public void hideNotification() {
        if (!mShowNotification) return;
        mShowNotification = false;
        //if (KeeperService.instance == null) return;
        if (mKeeperIntent != null) {
            stopService(mKeeperIntent);
            mKeeperIntent = null;
        }
    }

    @Override
    public void startActivity(Intent intent) {
        if (mBridgeListener == null || this.mBridgeListener.applyIntent(intent)) {
            super.startActivity(intent);
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (mBridgeListener == null || this.mBridgeListener.applyIntent(intent)) {
            super.startActivityForResult(intent, requestCode);
        }
    }

    public void notifyKeyEvent(KeyEvent e) {
        if (mBridgeListener != null) {
            mBridgeListener.onKeyEvent(e);
        }
    }

    public AccessibilitySvc getAccessibilitySvc() {
        return AccessibilitySvc.getInstance();
    }

    public void goToAccessibilitySetting() {
        AccessibilitySvc.goToAccessibilitySetting(this);
    }

    public ScriptManager getScriptManager() {
        return mManager;
    }

    public void bringToFront() {
        if (mIsForeground) return;
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        if (am == null) return;
        List<ActivityManager.RunningTaskInfo> tl = am.getRunningTasks(100);
        for (ActivityManager.RunningTaskInfo e : tl) {
            if (e.topActivity.getPackageName().equals(getPackageName())) {
                am.moveTaskToFront(e.id, 0);
                return;
            }
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public RhinoWebSocketHelper createWebSocketHelper(int port, RhinoWebSocketHelper.DelegateInterface delegate) {
        return new RhinoWebSocketHelper(port, delegate);
    }

    public interface BridgeListener {
        boolean applyIntent(Intent intent);

        void onAccessibilitySvcCreate();

        void onAccessibilitySvcDestroy();

        void onActivityResult(int requestCode, int resultCode, Intent data);

        void onNewIntent(Intent intent);

        void onKeyEvent(KeyEvent event);

        void onRemoteEnabled();

        void onRemoteMessage(Message msg);

        void onRemoteDisabled();
    }
}
