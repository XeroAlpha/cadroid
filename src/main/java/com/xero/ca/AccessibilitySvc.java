package com.xero.ca;

import android.accessibilityservice.*;
import android.content.*;
import android.provider.*;
import android.view.*;
import android.view.accessibility.*;
import java.lang.ref.*;

public class AccessibilitySvc extends AccessibilityService {
	private static WeakReference<AccessibilitySvc> instance = new WeakReference<AccessibilitySvc>(null);
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
		AccessibilityNodeInfo node = getRootInActiveWindow();
		if (node == null) return 2;
		node = node.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
		if (node == null) return 3;
		if (!node.isEditable()) return 1;
		if (!node.performAction(AccessibilityNodeInfo.ACTION_PASTE)) return 4;
		return 0;
	}
	
	public CharSequence getFocusEditableText() {
		AccessibilityNodeInfo node = getRootInActiveWindow();
		if (node == null) return null;
		node = node.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
		if (node == null) return null;
		if (!node.isEditable()) return null;
		return node.getText();
	}
	
	public static void setLifeCycleListener(ServiceLifeCycleListener mListener) {
		mLifeCycleListener = mListener;
	}
	
	public interface ServiceLifeCycleListener {
		public void onCreate()
		public void onDestroy()
	}
}
