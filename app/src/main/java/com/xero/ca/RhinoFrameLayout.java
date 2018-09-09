package com.xero.ca;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Outline;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
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


    public RhinoFrameLayout(Context context) {
        super(context);
        mCallback = new DefaultCallback();
    }

    public RhinoFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCallback = new DefaultCallback();
    }

    public RhinoFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mCallback = new DefaultCallback();
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

    public void setRoundRectRadius(final int radius) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        if (radius <= 0) {
            setClipToOutline(false);
        } else {
            setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                }
            });
            setClipToOutline(true);
        }
    }

    public interface Callback {
        int dispatchKeyEvent(KeyEvent event);
        int dispatchTouchEvent(MotionEvent ev);
    }

    private class DefaultCallback implements Callback {
        @Override
        public int dispatchKeyEvent(KeyEvent event) {
            return RETURN_DEFAULT;
        }

        @Override
        public int dispatchTouchEvent(MotionEvent ev) {
            return RETURN_DEFAULT;
        }
    }
}
