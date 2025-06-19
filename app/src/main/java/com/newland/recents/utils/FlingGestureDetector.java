package com.newland.recents.utils;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class FlingGestureDetector {
    public interface FlingListener {
        void onFlingUp();
        void onFlingDown();
    }

    private final GestureDetector mDetector;
    private final FlingListener mListener;

    public FlingGestureDetector(Context context, FlingListener listener) {
        mListener = listener;
        mDetector = new GestureDetector(context, new GestureListener());
    }

    public boolean onTouchEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) return false;

            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();

            if (Math.abs(diffX) > Math.abs(diffY)) {
                // 水平滑动，忽略
                return false;
            }

            if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffY > 0) {
                    mListener.onFlingDown();
                } else {
                    mListener.onFlingUp();
                }
                return true;
            }
            return false;
        }
    }
}