package com.xero.ca;
import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.util.*;

public class KeeperService extends Service {
	public static KeeperService instance;
	
	@Override
	public IBinder onBind(Intent intent) {
		if (instance != null) return null;
		instance = this;
		showNotification();
		return new Binder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		instance = null;
		hideNotification();
		return super.onUnbind(intent);
	}
	
	public void showNotification() {
		startForeground(1, new Notification.Builder(this)
				  .setContentTitle("命令助手")
				  .setContentText("正在运行中...")
				  .setSmallIcon(R.drawable.icon)
				  .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon))
				  .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_ONE_SHOT))
				  .build());
	}

	public void hideNotification() {
		stopForeground(true);
	}
}
