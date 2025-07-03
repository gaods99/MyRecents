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
import android.widget.FrameLayout;
import android.widget.OverScroller;

import com.newland.recents.model.Task;

import java.util.List;

public class RecentsView extends FrameLayout {
    private static final String TAG = "newland";

    public interface RecentsViewCallbacks {
        void onTaskLaunched(Task task);
        void onTaskDismissed(Task task);
    }

    private static final float MAX_VISUAL_DISTANCE = 720f;
    private static final float MAX_Z = 20f;  // 中心卡片高度
    private static final float MIN_Z = 0f;   // 两边卡片最低高度
    private static final float MIN_ALPHA = 0.7f;
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
            addView(taskView);
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
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        mTaskWidth = (int) (width * 0.75f);
        mTaskHeight = (int) (height * 0.8f);
        mTaskSpacing = mTaskWidth / 50;

        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).measure(
                    MeasureSpec.makeMeasureSpec(mTaskWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(mTaskHeight, MeasureSpec.EXACTLY));
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
        if (mCallbacks != null && mActiveTaskIndex != -1) {
            mCallbacks.onTaskLaunched(((TaskView) getChildAt(mActiveTaskIndex)).getTask());
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
                            if (mActiveTaskIndex >= getChildCount()) {
                                mActiveTaskIndex = getChildCount() - 1;
                            }
                            updateViewTransforms();
                            scrollToActiveTask();
                        }
                    }).start();
        }
    }

    private void scrollToActiveTask() {
        if (mActiveTaskIndex == -1) return;
        int targetScroll = getChildLeft(mActiveTaskIndex) - (getWidth() - mTaskWidth) / 2;
        mScroller.startScroll(getScrollX(), 0, targetScroll - getScrollX(), 0, 400);
        invalidate();
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
        return mIsBeingDragged;
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
                    if (x >= child.getLeft() && x <= child.getRight() && y >= child.getTop() && y <= child.getBottom()) {
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
                    mDownView.setTranslationY(mDownView.getTranslationY() - deltaY);
                }
                mLastMotionX = x;
                mLastMotionY = y;
                break;
            case MotionEvent.ACTION_UP:
                if (mDownView != null && Math.abs(mDownView.getTranslationY() - (getHeight() - mTaskHeight) / 2f) > mDownView.getHeight() / 3f) {
                    dismissTask(mDownViewIndex);
                } else {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) velocityTracker.getXVelocity();

                    if (Math.abs(initialVelocity) > mMinimumVelocity) {
                        int scrollRange = (getChildCount() - 1) * mTaskSpacing;
                        mScroller.fling(getScrollX(), 0, -initialVelocity, 0, 0, scrollRange, 0, 0);
                    }
                    // Snap to the nearest task
                    int targetIndex = Math.round((float) getScrollX() / mTaskSpacing);
                    mActiveTaskIndex = Math.max(0, Math.min(targetIndex, getChildCount() - 1));
                    scrollToActiveTask();
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

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            updateViewTransforms();
            invalidate();
        }
    }
}