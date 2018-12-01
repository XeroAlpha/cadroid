package com.xero.ca;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class UriActionActivity extends Activity {
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
        target.setAction(MainActivity.ACTION_URI_ACTION);
        target.setDataAndType(intent.getData(), intent.getType());
        target.putExtras(intent);
        MainActivity.callIntent(this, target);
    }
}
