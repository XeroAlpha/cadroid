package com.xero.ca;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import java.lang.ref.*;
import android.net.*;

public class KeeperService extends Service {
	public static WeakReference<KeeperService> instance = new WeakReference<KeeperService>(null);
	
	@Override
	public IBinder onBind(Intent intent) {
		if (instance.get() != null) return null;
		instance = new WeakReference<KeeperService>(this);
		showNotification();
		return new Binder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		instance.clear();
		hideNotification();
		return super.onUnbind(intent);
	}
	
	public void showNotification() {
		Notification.Builder nof = new Notification.Builder(this)
			.setContentTitle("命令助手")
			.setContentText("正在运行中...")
			.setSmallIcon(R.drawable.icon_small)
			.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon_small))
			.setContentIntent(PendingIntent.getService(this, 1, new Intent(this, ScriptActionService.class), PendingIntent.FLAG_UPDATE_CURRENT));
		startForeground(1, nof.build());
	}

	public void hideNotification() {
		stopForeground(true);
	}
}
