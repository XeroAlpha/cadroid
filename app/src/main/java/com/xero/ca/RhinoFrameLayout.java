package com.xero.ca;

import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class RhinoFrameLayout extends FrameLayout {
    public static final int RETURN_TRUE = 1;
    public static final int RETURN_FALSE = -1;
    public static final int RETURN_DEFAULT = 0;

    private Callback mCallback;

    public RhinoFrameLayout(Context context, Callback callback) {
        super(context);
        if (callback == null) throw new IllegalArgumentException();
        mCallback = callback;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int r = mCallback.dispatchKeyEvent(event);
        if (r == RETURN_TRUE) {
            return true;
        } else if (r == RETURN_FALSE) {
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int r = mCallback.dispatchTouchEvent(ev);
        if (r == RETURN_TRUE) {
            return true;
        } else if (r == RETURN_FALSE) {
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }

    public interface Callback {
        int dispatchKeyEvent(KeyEvent event);
        int dispatchTouchEvent(MotionEvent ev);
    }
}
