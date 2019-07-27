package com.xero.ca;

import android.app.Activity;
import android.view.ViewGroup;

import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.util.AdError;
import com.xero.ca.script.AnalyticsPlatform;

import java.lang.ref.WeakReference;

public class AdProvider implements SplashADListener {
    private static final AdProvider sInstance = new AdProvider();

    public static AdProvider getInstance() {
        return sInstance;
    }

    private boolean mPaid;
    private boolean mCompleted;
    private boolean mPaused;
    private boolean mDispatchWhenResume;
    private WeakReference<Activity> mActivity;
    private OnCompleteListener mListener;

    public void prepare(Activity activity, ViewGroup container) {
        mActivity = new WeakReference<>(activity);
        mPaused = false;
        mDispatchWhenResume = false;
        synchronized (this) {
            mListener = null;
            mCompleted = false;
            mPaid = false;
        }
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
            if (!mCompleted) {
                mCompleted = true;
                mPaid = success;
                if (mListener != null) mListener.onComplete(success);
            }
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

    public void release() {
        mActivity = null;
        dispatchComplete(false);
        mListener = null;
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
        if (mActivity != null) { // GDT似乎有时会在Activity被destroy后调用此方法
            Activity activity = mActivity.get();
            if (activity != null) {
                AnalyticsPlatform.reportAdError(activity, adError.getErrorCode(), adError.getErrorMsg());
            }
        }
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
