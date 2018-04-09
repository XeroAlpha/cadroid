package com.xero.ca;

import android.accessibilityservice.*;
import android.content.*;
import android.provider.*;
import android.view.*;
import android.view.accessibility.*;

public class AccessibilitySvc extends AccessibilityService {
	private static AccessibilitySvc instance = null;
	private static ServiceLifeCycleListener mLifeCycleListener = null;

	public static void goToAccessibilitySetting(Context context) {
		context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}

	public static AccessibilitySvc getInstance() {
		return instance;
	}
	
	public static void notifyKeyEvent(final KeyEvent e) {
		if (MainActivity.instance == null) return;
		MainActivity.instance.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					MainActivity.instance.notifyKeyEvent(e);
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
		instance = this;
		super.onCreate();
		if (mLifeCycleListener != null) mLifeCycleListener.onCreate();
	}

	@Override
	public void onDestroy() {
		if (instance == this) {
			instance = null;
		}
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
