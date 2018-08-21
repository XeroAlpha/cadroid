package com.xero.ca;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.TextView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Arrays;
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
        String src = mPreferences.getString("debugSource", "");
        if (ACTION_DEBUG_EXEC.equals(i.getAction()) && !TextUtils.isEmpty(src)) {
            mManager = ScriptManager.createDebuggable(src);
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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

    public int checkSelfPermission(String permission) {
        return checkPermission(permission, android.os.Process.myPid(), android.os.Process.myUid());
    }

    public boolean shouldShowRequestPermissionRationable(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return shouldShowRequestPermissionRationale(permission);
        }
        return false;
    }

    public void requestPermissionsCompat(final int requestCode, final String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode);
        } else {
            Handler handler = new Handler(getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    final int[] grantResults = new int[permissions.length];
                    PackageManager packageManager = getPackageManager();
                    String packageName = getPackageName();
                    final int permissionCount = permissions.length;
                    for (int i = 0; i < permissionCount; i++) {
                        grantResults[i] = packageManager.checkPermission(
                                permissions[i], packageName);
                    }
                    onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (mBridgeListener != null) mBridgeListener.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

    public Uri fileToUri(File file) {
        return UnsafeFileProvider.getUriForFile(file);
    }

    public RhinoFrameLayout createFrameLayout(RhinoFrameLayout.Callback callback) {
        return new RhinoFrameLayout(this, callback);
    }

    public byte[] getVerifyKey() {
        int[] values = new int[] {
                0x30, 0x82, 0x01, 0x22, 0x30, 0x0D, 0x06, 0x09, 0x2A, 0x86, 0x48, 0x86, 0xF7, 0x0D, 0x01, 0x01,
                0x01, 0x05, 0x00, 0x03, 0x82, 0x01, 0x0F, 0x00, 0x30, 0x82, 0x01, 0x0A, 0x02, 0x82, 0x01, 0x01,
                0x00, 0xC0, 0xF8, 0x75, 0x9B, 0x1A, 0xBF, 0x74, 0x58, 0xD2, 0xB1, 0x2E, 0xAD, 0x45, 0x37, 0x5D,
                0xA9, 0x9A, 0xB4, 0xF5, 0x17, 0x4C, 0x10, 0x43, 0x4C, 0x2E, 0x29, 0x69, 0xB2, 0xB5, 0x01, 0xD3,
                0xA7, 0xA3, 0xD4, 0x85, 0xDC, 0x16, 0xB8, 0xA9, 0x8C, 0xFD, 0xF7, 0xE8, 0x22, 0x15, 0xAE, 0xAB,
                0x40, 0x2E, 0x43, 0x0A, 0xB3, 0xEB, 0x78, 0xAF, 0x4E, 0xD8, 0x13, 0xF5, 0x41, 0x82, 0x7E, 0x1A,
                0x3E, 0xF5, 0xC1, 0x5F, 0xFB, 0x5A, 0x7D, 0x2C, 0xEC, 0xC2, 0xAB, 0x22, 0x3D, 0xA9, 0x7A, 0xAD,
                0x23, 0x4B, 0x65, 0x2C, 0x84, 0x2F, 0x12, 0x2E, 0xA6, 0xB7, 0x1C, 0xBA, 0x6D, 0x17, 0xA8, 0x32,
                0x19, 0xE5, 0x35, 0x96, 0x50, 0x8B, 0xEF, 0x4A, 0x97, 0x50, 0x9C, 0xEB, 0xDB, 0x05, 0xD5, 0xA8,
                0x9F, 0xE1, 0xC2, 0xC8, 0xF2, 0x5E, 0x7C, 0xF7, 0x2B, 0x0F, 0x6A, 0x63, 0x44, 0x3B, 0x12, 0xB7,
                0x9E, 0x57, 0x7C, 0x12, 0x5B, 0x20, 0x6A, 0xFB, 0x29, 0xDA, 0x81, 0xC9, 0x7F, 0xC9, 0x2E, 0x47,
                0x6E, 0x5E, 0x94, 0xD0, 0x87, 0x7A, 0xEA, 0x28, 0x6F, 0xA6, 0xBE, 0x8C, 0xEB, 0xC1, 0xE8, 0xFB,
                0x85, 0xF9, 0xAC, 0xB6, 0xE9, 0xE6, 0x06, 0x24, 0xA7, 0x95, 0x39, 0xC1, 0x67, 0x7F, 0x0D, 0x0E,
                0x67, 0x9C, 0x6C, 0x2E, 0x26, 0xFB, 0xA9, 0xF1, 0x24, 0x47, 0xE8, 0xBE, 0xFF, 0xD9, 0x8E, 0xBF,
                0x92, 0x02, 0xBF, 0xAE, 0xCF, 0x0C, 0x5F, 0x6C, 0x20, 0xEA, 0x62, 0xB7, 0x13, 0xB4, 0x82, 0x85,
                0x79, 0x48, 0xD9, 0xE5, 0x32, 0xE4, 0x97, 0x10, 0x39, 0xE9, 0xCB, 0xDA, 0xC5, 0x6F, 0x45, 0x6F,
                0xEB, 0x3B, 0x7C, 0x14, 0x9E, 0x0C, 0x77, 0x29, 0xFB, 0x52, 0xC8, 0x52, 0x34, 0x09, 0x87, 0x4C,
                0x52, 0x51, 0x03, 0xEB, 0x9D, 0x20, 0x42, 0xA8, 0x0C, 0xE4, 0x1C, 0x6D, 0xB7, 0x6C, 0xE8, 0xBD,
                0xA5, 0x02, 0x03, 0x01, 0x00, 0x01
        };
        byte[] result = new byte[values.length];
        for (int i = 0; i < values.length; i++) result[i] = (byte) values[i];
        return result;
    }

    public int getShellVersion() {
        return BuildConfig.SHELL_VERSION;
    }

    public interface BridgeListener {
        boolean applyIntent(Intent intent);

        void onAccessibilitySvcCreate();

        void onAccessibilitySvcDestroy();

        void onActivityResult(int requestCode, int resultCode, Intent data);

        void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);

        void onNewIntent(Intent intent);

        void onKeyEvent(KeyEvent event);

        void onRemoteEnabled();

        void onRemoteMessage(Message msg);

        void onRemoteDisabled();
    }
}
