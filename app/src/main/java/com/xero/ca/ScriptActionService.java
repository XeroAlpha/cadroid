package com.xero.ca;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ScriptActionService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MainActivity.callIntent(this, new Intent(MainActivity.ACTION_SCRIPT_ACTION).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }
}
