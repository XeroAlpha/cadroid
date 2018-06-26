package com.xero.ca;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;

import java.lang.ref.WeakReference;

public class KeeperService extends Service {
    public static WeakReference<KeeperService> instance = new WeakReference<>(null);

    @Override
    public void onCreate() {
        instance = new WeakReference<>(this);
        showNotification();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        instance.clear();
        hideNotification();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void showNotification() {
        Notification.Builder nof = new Notification.Builder(this)
                .setContentTitle("命令助手")
                .setContentText("正在运行中...")
                .setSmallIcon(R.drawable.icon_small)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon_small))
                .setContentIntent(PendingIntent.getService(this, 1, new Intent(this, ScriptActionService.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), PendingIntent.FLAG_UPDATE_CURRENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            startForeground(1, nof.build());
        } else {
            startForeground(1, nof.getNotification());
        }
    }

    public void hideNotification() {
        stopForeground(true);
    }
}
