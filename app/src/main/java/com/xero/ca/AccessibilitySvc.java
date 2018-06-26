package com.xero.ca;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.lang.ref.WeakReference;

public class AccessibilitySvc extends AccessibilityService {
    private static WeakReference<AccessibilitySvc> instance = new WeakReference<>(null);
    private static ServiceLifeCycleListener mLifeCycleListener = null;

    public static void goToAccessibilitySetting(Context context) {
        context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public static AccessibilitySvc getInstance() {
        return instance.get();
    }

    public static void notifyKeyEvent(final KeyEvent e) {
        final MainActivity ins = MainActivity.instance.get();
        if (ins == null) return;
        ins.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ins.notifyKeyEvent(e);
            }
        });
    }

    public static void setLifeCycleListener(ServiceLifeCycleListener mListener) {
        mLifeCycleListener = mListener;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (event.isPrintingKey()) return false;
        notifyKeyEvent(event);
        return false;
    }

    @Override
    public void onCreate() {
        instance = new WeakReference<AccessibilitySvc>(this);
        super.onCreate();
        if (mLifeCycleListener != null) mLifeCycleListener.onCreate();
    }

    @Override
    public void onDestroy() {
        instance.clear();
        if (mLifeCycleListener != null) mLifeCycleListener.onDestroy();
        super.onDestroy();
    }

    public int paste() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            AccessibilityNodeInfo node = getRootInActiveWindow();
            if (node == null) return 2;
            node = node.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
            if (node == null) return 3;
            if (!node.isEditable()) return 1;
            if (!node.performAction(AccessibilityNodeInfo.ACTION_PASTE)) return 4;
            return 0;
        }
        return -1;
    }

    public CharSequence getFocusEditableText() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            AccessibilityNodeInfo node = getRootInActiveWindow();
            if (node == null) return null;
            node = node.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
            if (node == null) return null;
            if (!node.isEditable()) return null;
            return node.getText();
        }
        return null;
    }

    public interface ServiceLifeCycleListener {
        void onCreate();

        void onDestroy();
    }
}
