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
        Intent target = new Intent(ScriptInterface.ACTION_ADD_LIBRARY, intent.getData());
        ScriptInterface.callIntent(this, target);
    }
}
