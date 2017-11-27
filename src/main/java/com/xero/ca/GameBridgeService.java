package com.xero.ca;

import android.app.*;
import android.content.*;
import android.os.*;
import java.util.*;

public class GameBridgeService extends Service implements Handler.Callback {
	public static final int REQUEST_CODE = 1;
	
	public interface Callback {
		public void onRemoteEnabled();
		public void onRemoteMessage(Message msg);
		public void onRemoteDisabled();
	}
	
	private Handler mHandler = new Handler(this);
	
	private static Callback mCallback;
	private static List<Message> mQueue = Collections.synchronizedList(new ArrayList<Message>());
	public static GameBridgeService instance = null;
	
	@Override
	public IBinder onBind(Intent intent) {
		if (instance != null) return null;
		instance = this;
		if (MainActivity.instance == null) {
			runMain();
		}
		if (mCallback != null) mCallback.onRemoteEnabled();
		return new Messenger(mHandler).getBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		instance = null;
		mQueue.clear();
		if (mCallback != null) mCallback.onRemoteDisabled();
		return super.onUnbind(intent);
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		if (mCallback != null) {
			mCallback.onRemoteMessage(msg);
		} else {
			mQueue.add(msg);
		}
		return false;
	}
	
	public static void setCallback(Callback c) {
		mCallback = c;
		if (isConnected()) c.onRemoteEnabled();
		if (mQueue.size() > 0) {
			for (Message e : mQueue) {
				c.onRemoteMessage(e);
			}
			mQueue.clear();
		}
	}
	
	public static void removeCallback() {
		mCallback = null;
	}
	
	public static boolean isConnected() {
		return instance != null;
	}
	
	private void runMain() {
		/*
		Intent i = new Intent(this, MainActivity.class).setAction(MainActivity.ACTION_START_FROM_BACKGROUND).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_FROM_BACKGROUND);
		startActivity(i);
		*BUG：从适配器打开时，适配器所在Activity闪退
		 java.lang.RuntimeException: Performing stop of activity that is not resumed: {com.xero.ca/com.xero.ca.MainActivity}
		*/
	}
}
