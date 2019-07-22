package com.xero.ca;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.Toast;

import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.util.AdError;

public class AdProvider implements SplashADListener {
    private static AdProvider sInstance = new AdProvider();

    public static AdProvider getInstance() {
        return sInstance;
    }

    private boolean mPaid;
    private boolean mCompleted;
    private boolean mPaused;
    private boolean mDispatchWhenResume;
    private Activity mActivity;
    private OnCompleteListener mListener;

    public void prepare(Activity activity, ViewGroup container) {
        mActivity = activity;
        mCompleted = false;
        mPaid = false;
        mPaused = false;
        mDispatchWhenResume = false;
        SplashAD ad = new SplashAD(activity, Secret.getGDTAppID(), Secret.getGDTPosID(), this);
        ad.fetchAndShowIn(container);
    }

    public void runAfterComplete(OnCompleteListener listener) {
        synchronized (this) {
            if (mCompleted) {
                listener.onComplete(mPaid);
            } else {
                mListener = listener;
            }
        }
    }

    private void dispatchComplete(boolean success) {
        synchronized (this) {
            mCompleted = true;
            mPaid = success;
            if (mListener != null) mListener.onComplete(success);
        }
    }

    public void pause() {
        mPaused = true;
    }

    public void resume() {
        mPaused = false;
        if (mDispatchWhenResume) {
            mDispatchWhenResume = false;
            dispatchComplete(true);
        }
    }

    public void dismiss() {
        mActivity = null;
    }

    @Override
    public void onADDismissed() {
        if (mPaused) {
            mDispatchWhenResume = true;
        } else {
            dispatchComplete(true);
        }
    }

    @Override
    public void onNoAD(AdError adError) {
        Toast.makeText(mActivity, adError.getErrorMsg(), Toast.LENGTH_LONG).show();
        dispatchComplete(adError.getErrorCode() >= 100000);
    }

    @Override
    public void onADPresent() {}

    @Override
    public void onADClicked() {}

    @Override
    public void onADTick(long l) {}

    @Override
    public void onADExposure() {}

    interface OnCompleteListener {
        void onComplete(boolean success);
    }
}
