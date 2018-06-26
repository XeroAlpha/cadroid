package com.xero.ca;
import android.app.*;
import android.content.*;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction() == Intent.ACTION_BOOT_COMPLETED) {
			if (context.getSharedPreferences(MainActivity.PREFERENCE_NAME, Activity.MODE_PRIVATE).getBoolean(MainActivity.SETTING_START_ON_BOOT, false)) {
				Intent i = new Intent(context, MainActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.setAction(MainActivity.ACTION_START_ON_BOOT);
				context.startActivity(i);
			}
		}
	}
}