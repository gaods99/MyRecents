package com.newland.recents.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.OverScroller;

import com.newland.recents.model.Task;

import java.util.List;

public class RecentsView extends FrameLayout {
    private static final String TAG = "newland";

    public interface RecentsViewCallbacks {
        void onTaskLaunched(Task task);
        void onTaskDismissed(Task task);
        void onAllTasksRemoved();
    }

    private static final float MAX_VISUAL_DISTANCE = 720f;
    private static final float MAX_Z = 20f;  // 中心卡片高度
    private static final float MIN_Z = 0f;   // 两边卡片最低高度
    private static final float MIN_ALPHA = 0.8f;
    private static final float MAX_ALPHA = 1.0f;

    private OverScroller mScroller;
    private VelocityTracker mVelocityTracker;
    private GestureDetector mGestureDetector;
    private RecentsViewCallbacks mCallbacks;

    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;

    private float mLastMotionX;
    private float mLastMotionY;
    private boolean mIsBeingDragged;
    private View mDownView;
    private int mDownViewIndex;

    private int mTaskWidth;
    private int mTaskHeight;
    private int mTaskSpacing;
    private int mActiveTaskIndex = -1;

    public RecentsView(Context context) { this(context, null); }
    public RecentsView(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public RecentsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mScroller = new OverScroller(context);
        ViewConfiguration config = ViewConfiguration.get(context);
        mTouchSlop = config.getScaledTouchSlop();
        mMinimumVelocity = config.getScaledMinimumFlingVelocity();
        mMaximumVelocity = config.getScaledMaximumFlingVelocity();

        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.i(TAG, "onSingleTapUp: mIsBeingDragged = " + mIsBeingDragged);
                if (!mIsBeingDragged) {
                    handleTaskTap();
                    return true;
                }
                return false;
            }
        });
    }

    public void setCallbacks(RecentsViewCallbacks callbacks) { mCallbacks = callbacks; }

    public void setTasks(List<Task> tasks) {
        removeAllViews();
        for (Task task : tasks) {
            TaskView taskView = new TaskView(getContext());
            taskView.bind(task);
            taskView.setTag(task.key.id);
            // 关键：添加时指定 wrap_content 的布局参数
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
            );
            addView(taskView, params);
        }
        mActiveTaskIndex = getChildCount() > 0 ? 0 : -1;
        updateViewTransforms();
        scrollToActiveTask();
    }

    public TaskView findTaskView(int taskId) {
        return findViewWithTag(taskId);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // First, let the FrameLayout do its standard measurement. This will measure children
        // with WRAP_CONTENT and determine the final size of this RecentsView.
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Now that the RecentsView's final dimensions are set, we can get its width.
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        // Calculate the desired task size based on the ratio of the final measured width.
        mTaskWidth = (int) (width * 0.75f);
        mTaskHeight = (int) (height * 0.8f);
        mTaskSpacing = mTaskWidth / 50;

        // Re-measure all children with the new, correct, ratio-based size.
        // This overrides the initial WRAP_CONTENT measurement and enforces consistency.
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.measure(
                    MeasureSpec.makeMeasureSpec(mTaskWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(mTaskHeight, MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        updateViewTransforms();
    }

    private void updateViewTransforms() {
        int scrollX = getScrollX();
        int parentCenter = getWidth() / 2;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int childCenter = getChildLeft(i) + mTaskWidth / 2;
            float distanceFromCenter = Math.abs(parentCenter - (childCenter - scrollX));
            float progress = Math.min(1f, distanceFromCenter/MAX_VISUAL_DISTANCE);

            float alpha = interpolate(MAX_ALPHA, MIN_ALPHA, progress);
            float z = interpolate(MAX_Z, MIN_Z, progress);

            child.setAlpha(alpha);
            child.setTranslationZ(z);

            child.setTranslationX(getChildLeft(i));
            if (!mIsBeingDragged || child != mDownView) {
                float yPosition = (getHeight() - mTaskHeight) / 2f;
                child.setTranslationY(yPosition);
            }
        }
    }

    private int getChildLeft(int i) { return (getWidth() - mTaskWidth) / 2 + i * (mTaskWidth + mTaskSpacing); }
    private float interpolate(float start, float end, float progress) { return start + (end - start) * progress; }

    private void handleTaskTap() {
        if (mCallbacks != null && mDownView != null) {
            mCallbacks.onTaskLaunched(((TaskView) mDownView).getTask());
        }
    }

    private void dismissTask(final int index) {
        if (mCallbacks != null && index >= 0 && index < getChildCount()) {
            final TaskView taskView = (TaskView) getChildAt(index);
            mCallbacks.onTaskDismissed(taskView.getTask());

            taskView.animate().translationY(-getHeight()).alpha(0).setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            removeView(taskView);
                            if (getChildCount() == 0) {
                                mCallbacks.onAllTasksRemoved();
                            } else {
                                if (mActiveTaskIndex >= getChildCount()) {
                                    mActiveTaskIndex = getChildCount() - 1;
                                }
                                updateViewTransforms();
                                scrollToActiveTask();
                            }
                        }
                    }).start();
        }
    }

    private void scrollToActiveTask() {
        if (mActiveTaskIndex == -1 || getChildCount() == 0) return;

        int currentScrollX = getScrollX();
        int targetScroll = mActiveTaskIndex * (mTaskWidth + mTaskSpacing);
        int dx = targetScroll - currentScrollX;

        if (dx != 0) {
            final int MIN_DURATION = 400; // ms
            final int MAX_DURATION = 1200; // ms
            final int BASE_DURATION = 200; // ms

            int distance = Math.abs(dx);
            // Calculate duration based on the distance to scroll, relative to the task width
            float distanceRatio = (float) distance / mTaskWidth;
            int duration = (int) (BASE_DURATION * (1 + distanceRatio));
            // Clamp the duration to the min/max values
            duration = Math.max(MIN_DURATION, Math.min(MAX_DURATION, duration));

            mScroller.startScroll(currentScrollX, 0, dx, 0, duration);
            invalidate();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) return true;

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = ev.getX();
                mLastMotionY = ev.getY();
                mIsBeingDragged = !mScroller.isFinished();
                if (!mIsBeingDragged) {
                    mScroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                final float x = ev.getX();
                final float y = ev.getY();
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                final int yDiff = (int) Math.abs(y - mLastMotionY);
                if (xDiff > mTouchSlop || yDiff > mTouchSlop) {
                    mIsBeingDragged = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                break;
        }
        return true;
    }

    private boolean inChildArea(int i, View child, float touchX, float touchY) {
        float childLeft = child.getX();
        float childRight = child.getX() + child.getWidth();
        float childTop = child.getY();
        float childBottom = child.getY() + child.getHeight();

        touchX += getScrollX();
        touchY += getScrollY();

        return touchX >= childLeft &&
                touchX <= childRight &&
                touchY >= childTop &&
                touchY <= childBottom;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mVelocityTracker == null) mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(ev);
        mGestureDetector.onTouchEvent(ev);

        final int action = ev.getAction();
        final float x = ev.getX();
        final float y = ev.getY();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) mScroller.abortAnimation();
                mLastMotionX = x;
                mLastMotionY = y;

                // Find the view under the touch
                for (int i = getChildCount() - 1; i >= 0; i--) {
                    View child = getChildAt(i);
                    if (inChildArea(i, child, x, y)) {
                        mDownView = child;
                        mDownViewIndex = i;
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = mLastMotionX - x;
                float deltaY = mLastMotionY - y;
                if (Math.abs(deltaX) > Math.abs(deltaY)) { // Horizontal scroll
                    scrollBy((int) deltaX, 0);
                } else if (mDownView != null) { // Vertical dismiss
                    float targetY = mDownView.getTranslationY() - deltaY;
                    float bottomY = (getHeight() - mTaskHeight) / 2f;
                    mDownView.setTranslationY(Math.min(targetY, bottomY));
                    // 添加透明度渐变效果
                    float swipeDistance = Math.abs(targetY - bottomY);
                    float maxSwipeDistance = mTaskHeight * 0.33f; // 最大滑动距离为卡片高度的50%
                    float progress = Math.min(1.0f, swipeDistance / maxSwipeDistance);
                    // 透明度从1.0渐变到0.7（向上滑动时逐渐变透明）
                    float alpha = 1.0f - (progress * 0.3f);
                    mDownView.setAlpha(alpha);
                }
                mLastMotionX = x;
                mLastMotionY = y;
                break;
            case MotionEvent.ACTION_UP:
                if (mDownView != null &&
                        Math.abs(mDownView.getTranslationY() - (getHeight() - mTaskHeight) / 2f)
                                > mTaskHeight / 3f) {
                    dismissTask(mDownViewIndex);
                } else {
                    // 恢复卡片的透明度和缩放
                    if (mDownView != null) {
                        float yPosition = (getHeight() - mTaskHeight) / 2f;
                        mDownView.setTranslationY(yPosition);
                        mDownView.animate().alpha(1.0f).setDuration(200).start();
                        mDownView = null;
                    }
                    flingAndSnap();
                }
                // Fallthrough
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mDownView = null;
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
        }
        return true;
    }

    private void flingAndSnap() {
        if (getChildCount() == 0) return;

        final VelocityTracker velocityTracker = mVelocityTracker;
        velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
        int initialVelocity = (int) velocityTracker.getXVelocity();

        int currentScrollX = getScrollX();
        int maxScrollX = getChildCount() > 0 ?
                getChildLeft(getChildCount() - 1) - getChildLeft(0) : 0;

        // Use scroller to predict final position
        mScroller.fling(currentScrollX, 0,
                -initialVelocity, 0, 0, maxScrollX, 0, 0);
        int predictedFinalX = mScroller.getFinalX();
        mScroller.abortAnimation(); // We don't want the fling itself, just the prediction

        // Find the task closest to the predicted final position
        int center = getWidth() / 2;
        int nearestIndex = -1;
        int minDistance = Integer.MAX_VALUE;

        for (int i = 0; i < getChildCount(); i++) {
            // Log.i(TAG, String.format("%d: childLeft: %d", i, getChildLeft(i)));
            int childCenter = getChildLeft(i) + mTaskWidth / 2;
            int distance = Math.abs(childCenter - (predictedFinalX + center));
            if (distance < minDistance) {
                minDistance = distance;
                nearestIndex = i;
            }
        }

        if (nearestIndex != -1) {
            mActiveTaskIndex = nearestIndex;
            scrollToActiveTask();
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            updateViewTransforms();
            postInvalidate();
        }
    }
}