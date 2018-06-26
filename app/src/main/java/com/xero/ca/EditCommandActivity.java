package com.xero.ca;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class EditCommandActivity extends Activity {
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
        target.setAction(MainActivity.ACTION_EDIT_COMMAND);
        target.putExtra("text", intent.getExtras().getString(Intent.EXTRA_TEXT, ""));
        MainActivity.callIntent(this, target);
    }
}
