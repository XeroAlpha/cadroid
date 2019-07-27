package com.xero.ca;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.TextView;

public class SplashActivity extends Activity {
    private TextView mLoadingTitleView;
    private FrameLayout mAdContainer;
    private AdProvider mProvider;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.main);
        mLoadingTitleView = findViewById(R.id.loadingTitle);
        mAdContainer = findViewById(R.id.adContainer);
        super.onCreate(savedInstanceState);
        if (!ScriptInterface.onSplashActivityCreate(this)) {
            finish();
        }
        mProvider = AdProvider.getInstance();
        mProvider.prepare(this, mAdContainer);
    }

    @Override
    protected void onDestroy() {
        ScriptInterface.onSplashActivityDestroy(this);
        mProvider.release();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        mProvider.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mProvider.resume();
        super.onResume();
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
            mLoadingTitleView.setText(title);
        }
    }
}
