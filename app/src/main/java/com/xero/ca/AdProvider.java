package com.xero.ca;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;

import com.xero.ca.script.AnalyticsPlatform;

import java.lang.ref.WeakReference;

public class AdProvider {
    private static final AdProvider sInstance = new AdProvider();

    public static AdProvider getInstance() {
        return sInstance;
    }

    private ADListener mADListener;

    private boolean mPaid;
    private boolean mCompleted;
    private boolean mPaused;
    private boolean mDispatchWhenResume;
    private long mPresentTime;
    private int mPresentDuration;
    private boolean mAutoSkip;
    private String mMessage;
    private WeakReference<Activity> mActivity;
    private Listener mListener;
    private OnCompleteListener mOnCompleteListener;

    public AdProvider() {
        mADListener = new ADListener();
    }

    public void prepare(Activity activity, ViewGroup container) {
        prepare(activity, container, null);
    }

    public void prepare(Activity activity, ViewGroup container, View skipView) {
        prepare(activity, container, skipView, null);
    }

    public void prepare(Activity activity, ViewGroup container, View skipView, Listener listener) {
        mActivity = new WeakReference<>(activity);
        mListener = listener;
        mPaused = false;
        mDispatchWhenResume = false;
        mPresentTime = 0;
        mPresentDuration = -1;
        mAutoSkip = false;
        synchronized (this) {
            mOnCompleteListener = null;
            mCompleted = false;
            mPaid = false;
        }
//        Preference pref = Preference.getInstance(activity);
        mADListener.onADDismissed();
    }

    public void runAfterComplete(OnCompleteListener listener) {
        synchronized (this) {
            if (mCompleted) {
                listener.onComplete(this, mPaid, mMessage);
            } else {
                mOnCompleteListener = listener;
            }
        }
    }

    private void dispatchComplete(boolean success, String message) {
        synchronized (this) {
            if (!mCompleted) {
                mCompleted = true;
                mPaid = success;
                mMessage = message;
                if (mOnCompleteListener != null) mOnCompleteListener.onComplete(this, success, message);
            }
        }
    }

    private void dispatchCompleteCheckSkip(boolean success, String message) {
        if (success && mAutoSkip && mPresentDuration < 3000) {
            success = false;
            message = "?????????????????????????????????????????????????????????????????????????????????";
            reportError(5212, message);
        }
        dispatchComplete(success, message);
    }

    public boolean isCompleted() {
        synchronized (this) {
            return mCompleted;
        }
    }

    public int getPresentDuration() {
        return mPresentDuration;
    }

    public void pause() {
        mPaused = true;
    }

    public void resume() {
        mPaused = false;
        if (mDispatchWhenResume) {
            mDispatchWhenResume = false;
            dispatchCompleteCheckSkip(true, null);
        }
    }

    public void release() {
        mActivity = null;
        dispatchComplete(false, null);
        mListener = null;
        mOnCompleteListener = null;
    }
    
    private void reportError(int errorCode, String errorMsg) {
    	if (mActivity != null) { // GDT??????????????????Activity???destroy??????????????????
            Activity activity = mActivity.get();
            if (activity != null) {
                AnalyticsPlatform.reportAdError(activity, errorCode, errorMsg);
            }
        }
    }

    class ADListener {
        public void onADDismissed() {
            mPresentDuration = (int) (SystemClock.uptimeMillis() - mPresentTime);
            if (mPaused) {
                mDispatchWhenResume = true;
            } else {
                dispatchCompleteCheckSkip(true, null);
            }
            if (mListener != null) {
                mListener.onComplete();
            }
        }

//        public void onNoAD(AdError adError) {
//            int errorCode = adError.getErrorCode();
//            boolean adEnabled = errorCode >= 5000;
//            reportError(errorCode, adError.getErrorMsg());
//            dispatchComplete(adEnabled, adError.getErrorMsg());
//        }
//
//        public void onADLoaded(long l) {}
//
//        public void onADPresent() {
//            mPresentTime = SystemClock.uptimeMillis();
//            if (mListener != null) {
//                mListener.onPresent();
//            }
//        }
//
//        public void onADClicked() {}
//
//        public void onADTick(long l) {
//            if (mListener != null) {
//                mListener.onTick((int) (l / 1000));
//            }
//        }
//
//        public void onADExposure() {}
    }

    class SkipChecker extends View.AccessibilityDelegate {
        @Override
        public boolean performAccessibilityAction(View host, int action, Bundle args) {
            if (action == AccessibilityNodeInfo.ACTION_CLICK) { // ???????????????????????????
                mAutoSkip = true;
            }
            return super.performAccessibilityAction(host, action, args);
        }
    }

    interface Listener {
        void onPresent();
        void onTick(int secondsLeft);
        void onComplete();
    }

    interface OnCompleteListener {
        void onComplete(AdProvider provider, boolean success, String message);
    }
}
