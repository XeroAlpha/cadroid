package com.xero.ca;

import android.accessibilityservice.*;
import android.content.*;
import android.view.accessibility.*;

public class AccessibilitySvc extends AccessibilityService {
	public static AccessibilitySvc instance = null;

	public static void goToAccessibilitySetting(Context context) {
		context.startActivity(new Intent("android.settings.ACCESSIBILITY_SETTINGS").addFlags(268435456));
	}

	public static int paste() {
		if (instance == null) return -2;
		AccessibilityNodeInfo node = instance.getRootInActiveWindow();
		if (node == null) return 2;
		node = node.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
		if (node == null) return 3;
		if (!node.isEditable()) return 1;
		if (!node.performAction(AccessibilityNodeInfo.ACTION_PASTE)) return 4;
		return 0;
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
	}

	@Override
	public void onInterrupt() {
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
}
