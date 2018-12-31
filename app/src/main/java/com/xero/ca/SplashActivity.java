package com.xero.ca;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.main);
        super.onCreate(savedInstanceState);
        ScriptInterface.onSplashActivityCreate(this);
    }

    @Override
    protected void onDestroy() {
        ScriptInterface.onSplashActivityDestroy(this);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        //do nothing
    }

    public void setLoadingTitle(String title) {
        runOnUiThread(new SetLoadingTitleTask(title));
    }

    class SetLoadingTitleTask implements Runnable {
        private String title;

        public SetLoadingTitleTask(String title) {
            this.title = title;
        }

        @Override
        public void run() {
            try {
                ((TextView) findViewById(R.id.loadingTitle)).setText(title);
            } catch (Exception e) {
                //do nothing
            }
        }
    }
}
