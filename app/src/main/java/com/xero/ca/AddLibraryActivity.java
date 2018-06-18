package com.xero.ca;
import android.app.*;
import android.os.*;
import android.content.*;
import android.util.*;

public class AddLibraryActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		processIntent();
		finish();
	}

	private void processIntent() {
		Intent intent = getIntent();
		if (intent == null) return;
		Intent target = new Intent(this, MainActivity.class);
		target.setAction(MainActivity.ACTION_ADD_LIBRARY);
		target.setData(intent.getData());
		MainActivity.callIntent(this, target);
	}
}
