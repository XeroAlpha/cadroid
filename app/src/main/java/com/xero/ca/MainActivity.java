package com.xero.ca;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.TextView;

import com.xero.ca.script.RhinoFrameLayout;
import com.xero.ca.script.RhinoWebSocketHelper;
import com.xero.ca.script.RhinoWebView;

import java.io.File;
import java.lang.ref.WeakReference;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        processIntent();
        finish();
    }

    private void processIntent() {
        Intent intent = getIntent();
        if (intent == null) return;
        ScriptInterface.callIntent(this, intent);
    }
}
