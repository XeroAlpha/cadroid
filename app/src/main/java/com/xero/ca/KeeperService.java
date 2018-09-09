package com.xero.ca;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;

import java.lang.ref.WeakReference;

public class KeeperService extends Service {
    public static WeakReference<KeeperService> instance = new WeakReference<>(null);
    private static final String DEFAULT_CHANNEL = "default";

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
        Notification.Builder nof = createNotificationBuilder()
                .setContentTitle("命令助手")
                .setContentText("正在运行中...")
                .setSmallIcon(R.mipmap.icon_small)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.icon_small))
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

    private Notification.Builder createNotificationBuilder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(DEFAULT_CHANNEL, "保持活跃", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("快速管理命令助手");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
            return new Notification.Builder(this, DEFAULT_CHANNEL);
        } else {
            return new Notification.Builder(this);
        }
    }
}
