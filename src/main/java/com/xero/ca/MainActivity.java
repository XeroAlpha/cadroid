package com.xero.ca;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.widget.*;
import android.util.*;
import android.view.*;

public class MainActivity extends Activity {
	public static final String ACTION_ADD_LIBRARY = "com.xero.ca.ADD_LIBRARY";
	public static final String ACTION_EDIT_COMMAND = "com.xero.ca.EDIT_COMMAND";
	public static final String ACTION_START_ON_BOOT = "com.xero.ca.START_ON_BOOT";
	public static final String ACTION_START_FROM_BACKGROUND = "com.xero.ca.ACTION_START_FROM_BACKGROUND";
	public static final String ACTION_START_FROM_SHORTCUT = "com.xero.ca.ACTION_START_FROM_SHORTCUT";
	
	public static final String ACTION_SHOW_DEBUG = "com.xero.ca.SHOW_DEBUG";
	public static final String ACTION_DEBUG_EXEC = "com.xero.ca.DEBUG_EXEC";
	
	public static final String SETTING_HIDE_SPLASH = "hideSplash";
	public static final String SETTING_HIDE_NOTIFICATION = "hideNotification";
	public static final String SETTING_START_ON_BOOT = "bootStart";
	
	public static final String PREFERENCE_NAME = "user_settings";
	
	public static MainActivity instance;
	
	private BridgeListener mBridgeListener;
	private SharedPreferences mPreferences;
	private boolean mShowNotification;

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
	
	private ServiceConnection scv = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName p1, IBinder p2) {}
		@Override
		public void onServiceDisconnected(ComponentName p1) {}
	};

	public interface BridgeListener {
		public boolean applyIntent(Intent intent);
		public void onActivityResult(int requestCode, int resultCode, Intent data);
		public void onNewIntent(Intent intent);
		public void onKeyEvent(KeyEvent event);
		public void onRemoteEnabled();
		public void onRemoteMessage(Message msg);
		public void onRemoteDisabled();
	}

	@Override
	protected void onCreate(Bundle bundle) {
		mPreferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
		instance = this;
		if (getHideSplash() || isSubAction(getIntent().getAction())) {
			onBackPressed();
		} else {
			setTheme(R.style.AppTheme);
			setContentView(R.layout.main);
		}
		super.onCreate(bundle);
		if (ScriptManager.isRunning()) {
			new Handler().post(new Runnable() {
					@Override
					public void run() {
						finish();
					}
				});
		} else {
			Intent i = getIntent();
			if (i.getAction() == ACTION_DEBUG_EXEC && BuildConfig.DEBUG) {
				ScriptManager.setDebugFile(i.getData().getPath());
			} else {
				ScriptManager.setDebugFile(null);
			}
			ScriptManager.startScript(this);
		}
	}
	
	public static void callIntent(Context ctx, Intent intent) {
		if (instance != null) {
			instance.onNewIntent(intent);
		} else {
			ctx.startActivity(intent);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		if (mBridgeListener != null) mBridgeListener.onNewIntent(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mBridgeListener != null) mBridgeListener.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}

	@Override
	protected void onPause() {
		mShowNotification = true;
		showNotification();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mShowNotification = false;
		hideNotification();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		instance = null;
		hideNotification();
		mShowNotification = false;
		GameBridgeService.removeCallback();
		ScriptManager.endScript(false);
		super.onDestroy();
	}
	
	private boolean isSubAction(String action) {
		return action == ACTION_ADD_LIBRARY || action == ACTION_START_ON_BOOT || action == ACTION_START_FROM_BACKGROUND;
	}
	
	public int paste() {
		try {
			AccessibilitySvc svc = AccessibilitySvc.getInstance();
			if (svc == null) return -2;
			int paste = svc.paste();
			if (paste >= 0) {
				return 0;
			}
			AccessibilitySvc.goToAccessibilitySetting(this);
			return paste;
		} catch (Throwable e) {
			ScriptManager.showError(this, e);
			return -1;
		}
	}

	public void setBridgeListener(BridgeListener bridgeListener) {
		this.mBridgeListener = bridgeListener;
		GameBridgeService.setCallback(gbsCallback);
	}
	
	public void setLoadingTitle(String title) {
		try {
			((TextView) findViewById(R.id.loadingTitle)).setText(title);
		} catch (Exception e) {}
	}
	
	public boolean getHideSplash() {
		return mPreferences.getBoolean(SETTING_HIDE_SPLASH, false);
	}
	
	public void setHideSplash(boolean v) {
		mPreferences.edit().putBoolean(SETTING_HIDE_SPLASH, v).commit();
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
		mPreferences.edit().putBoolean(SETTING_HIDE_NOTIFICATION, v).commit();
	}
	
	public boolean getBootStart() {
		return mPreferences.getBoolean(SETTING_START_ON_BOOT, false);
	}

	public void setBootStart(boolean v) {
		mPreferences.edit().putBoolean(SETTING_START_ON_BOOT, v).commit();
	}
	
	public void showNotification() {
		if (getHideNotification()) return;
		if (KeeperService.instance != null) return;
		
		bindService(
			new Intent(this, KeeperService.class),
			scv,
			BIND_AUTO_CREATE
		);
	}
	
	public void hideNotification() {
		if (KeeperService.instance == null) return;
		unbindService(scv);
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
}
