package com.xero.ca;

import android.app.*;
import android.content.*;
import android.os.*;

public class GameBridgeService extends Service implements Handler.Callback {
	public interface Callback {
		public void onRemoteEnabled();
		public void onRemoteMessage(Message msg);
		public void onRemoteDisabled();
	}
	
	private Handler mHandler = new Handler(this);
	
	private static Callback mCallback;
	public static GameBridgeService instance = null;
	
	@Override
	public IBinder onBind(Intent intent) {
		if (instance != null) return null;
		instance = this;
		if (MainActivity.instance == null) {
			startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		}
		if (mCallback != null) mCallback.onRemoteEnabled();
		return new Messenger(mHandler).getBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		instance = null;
		if (mCallback != null) mCallback.onRemoteDisabled();
		return super.onUnbind(intent);
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		if (mCallback != null) mCallback.onRemoteMessage(msg);	
		return false;
	}
	
	public static void setCallback(Callback c) {
		mCallback = c;
	}
	
	public static void removeCallback() {
		mCallback = null;
	}
	
	public static boolean isConnected() {
		return instance != null;
	}
}
