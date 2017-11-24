package com.xero.ca;

import android.accessibilityservice.*;
import android.content.*;
import android.provider.*;
import android.view.*;
import android.view.accessibility.*;

public class AccessibilitySvc extends AccessibilityService {
	private static AccessibilitySvc instance = null;

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
	protected void onServiceConnected() {
		instance = this;
		super.onServiceConnected();
	}

	@Override
	public void onDestroy() {
		if (instance == this) {
			instance = null;
		}
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
}