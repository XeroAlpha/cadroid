package com.xero.ca;
import android.content.*;
import android.app.*;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction() == Intent.ACTION_BOOT_COMPLETED) {
			if (context.getSharedPreferences(MainActivity.PREFERENCE_NAME, Activity.MODE_PRIVATE).getBoolean(MainActivity.SETTING_START_ON_BOOT, false)) {
				Intent i = new Intent(MainActivity.ACTION_START_ON_BOOT);
				i.setComponent(new ComponentName("com.xero.ca", "com.xero.ca.MainActivity"));
				context.startActivity(i);
			}
		}
	}
}
