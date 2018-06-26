package com.xero.ca;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

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
