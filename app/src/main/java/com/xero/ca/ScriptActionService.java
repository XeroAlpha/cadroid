package com.xero.ca;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import java.lang.ref.*;
import android.net.*;

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
