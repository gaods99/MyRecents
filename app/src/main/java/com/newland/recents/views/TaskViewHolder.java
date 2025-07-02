package com.newland.recents.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.newland.recents.R;
import com.newland.recents.model.Task;

/**
 * 任务视图持有者，SystemUI Android 7.1.2 风格实现
 */
public class TaskViewHolder extends RecyclerView.ViewHolder {
    
    private static final long DISMISS_ANIMATION_DURATION = 300;
    
    // Swipe gesture constants - based on Launcher3 Quickstep
    private static final float SWIPE_DISMISS_VELOCITY_THRESHOLD = 1000f; // dp/s
    private static final float SWIPE_DISMISS_DISTANCE_THRESHOLD = 0.3f; // 30% of view height
    private static final float SWIPE_PROGRESS_ALPHA_MIN = 0.5f;
    private static final float SWIPE_PROGRESS_SCALE_MIN = 0.8f;
    private static final long SWIPE_RETURN_ANIMATION_DURATION = 200;
    
    public final View taskView;
    public final View taskHeader;
    public final ImageView thumbnailView;
    public final ImageView iconView;
    public final TextView titleView;
    public final View dismissButton;
    
    private Task mTask;
    private TaskViewListener mListener;
    private boolean mIsAnimating = false;
    private int mDefaultTaskWidth;
    
    // Swipe gesture detection
    private GestureDetector mGestureDetector;
    private boolean mIsSwipeInProgress = false;
    private float mInitialTouchY;
    private float mCurrentTranslationY = 0f;
    
    public interface TaskViewListener {
        void onTaskClick(Task task, int position);
        void onTaskDismiss(Task task, int position);
        void onTaskLongClick(Task task, int position);
    }
    
    public TaskViewHolder(View itemView, TaskViewListener listener) {
        super(itemView);
        
        mListener = listener;
        
        taskView = itemView;
        taskHeader = itemView.findViewById(R.id.task_header);
        thumbnailView = itemView.findViewById(R.id.task_thumbnail);
        iconView = itemView.findViewById(R.id.task_icon);
        titleView = itemView.findViewById(R.id.task_title);
        dismissButton = itemView.findViewById(R.id.task_dismiss);
        
        setupClickListeners();
        setupSwipeGestureDetection();
    }
    
    public void setDefaultTaskWidth(int width) {
        mDefaultTaskWidth = width;
    }
    
    private void setupClickListeners() {
        taskView.setOnClickListener(v -> {
            if (!mIsAnimating && mTask != null && mListener != null) {
                mListener.onTaskClick(mTask, getAdapterPosition());
            }
        });
        
        taskView.setOnLongClickListener(v -> {
            if (!mIsAnimating && mTask != null && mListener != null) {
                mListener.onTaskLongClick(mTask, getAdapterPosition());
                return true;
            }
            return false;
        });
        
        if (dismissButton != null) {
            dismissButton.setOnClickListener(v -> {
                if (!mIsAnimating && mTask != null && mListener != null) {
                    animateDismiss();
                }
            });
        }
    }
    
    /**
     * 绑定任务数据
     * 修复1：统一使用优化后的卡片宽度，避免有无缩略图时卡片宽度不一致的问题
     * 修复2：使用智能缩放策略，优先铺满宽度同时尽量保持完整显示
     * 优化3：调整卡片尺寸，更适合手机屏幕显示
     * 优化4：增加卡片高度，使其显得更修长优雅 (240×380dp, 宽高比0.63)
     * 优化5：单卡片完美居中显示
     * 功能6：添加向上甩出删除功能，参考Launcher3 Quickstep实现
     */
    public void bind(Task task) {
        mTask = task;
        
        resetState();
        
        taskHeader.setVisibility(View.VISIBLE);
        
        titleView.setText(task.title != null ? task.title : task.packageName);
        if (task.icon != null) {
            iconView.setImageDrawable(task.icon);
        } else {
            iconView.setImageResource(R.drawable.ic_default_task);
        }

        // 统一使用SystemUI标准宽度，保持所有卡片宽度一致
        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        params.width = mDefaultTaskWidth; // 始终使用统一的标准宽度
        itemView.setLayoutParams(params);
        
        if (task.thumbnail != null) {
            thumbnailView.setImageBitmap(task.thumbnail);
            // 使用智能缩放策略：优先铺满宽度，同时尽量保持完整显示
            setOptimalThumbnailScale(task.thumbnail);
        } else {
            // 没有缩略图时显示默认占位图，保持视觉一致性
            thumbnailView.setImageResource(R.drawable.ic_default_task);
            thumbnailView.setScaleType(ImageView.ScaleType.CENTER);
        }
        
        taskView.setSelected(task.isFocused);
        taskView.setActivated(task.isActive);
    }
    
    private void resetState() {
        mIsAnimating = false;
        mIsSwipeInProgress = false;
        mCurrentTranslationY = 0f;
        taskView.setAlpha(1f);
        taskView.setTranslationX(0f);
        taskView.setTranslationY(0f);
        taskView.setScaleX(1f);
        taskView.setScaleY(1f);
    }
    
    public void animateDismiss() {
        if (mIsAnimating) {
            return;
        }
        mIsAnimating = true;
        
        ObjectAnimator translateX = ObjectAnimator.ofFloat(taskView, "translationX", taskView.getWidth());
        translateX.setDuration(DISMISS_ANIMATION_DURATION);
        translateX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mListener != null && mTask != null) {
                    mListener.onTaskDismiss(mTask, getAdapterPosition());
                }
                // Don't reset state here, let RecyclerView handle it
                mIsAnimating = false;
            }
        });
        translateX.start();
    }
    
    /**
     * 设置最优的缩略图缩放模式
     * 根据缩略图的宽高比智能选择缩放策略
     */
    private void setOptimalThumbnailScale(android.graphics.Bitmap thumbnail) {
        if (thumbnail == null) {
            thumbnailView.setScaleType(ImageView.ScaleType.CENTER);
            return;
        }
        
        // 获取缩略图和容器的宽高比
        float thumbnailRatio = (float) thumbnail.getWidth() / thumbnail.getHeight();
        
        // 获取容器的宽高比（减去header高度）
        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        int containerWidth = params.width;
        int containerHeight = params.height;
        
        if (containerWidth <= 0 || containerHeight <= 0) {
            // 如果无法获取容器尺寸，使用默认策略
            thumbnailView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            return;
        }
        
        // 减去header高度，获取实际缩略图区域的高度
        int headerHeight = taskHeader.getLayoutParams().height;
        if (headerHeight <= 0) {
            headerHeight = 48 * (int) itemView.getContext().getResources().getDisplayMetrics().density; // 默认48dp
        }
        int thumbnailAreaHeight = containerHeight - headerHeight;
        float containerRatio = (float) containerWidth / thumbnailAreaHeight;
        
        // 智能选择缩放策略
        if (Math.abs(thumbnailRatio - containerRatio) < 0.1f) {
            // 宽高比接近，使用FIT_XY填满整个区域
            thumbnailView.setScaleType(ImageView.ScaleType.FIT_XY);
        } else if (thumbnailRatio > containerRatio) {
            // 缩略图更宽，使用FIT_CENTER保持完整显示
            thumbnailView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            // 缩略图更高，使用CENTER_CROP优先填满宽度
            thumbnailView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }
    
    /**
     * Setup swipe gesture detection for upward swipe to dismiss
     * Based on Launcher3 Quickstep implementation
     */
    private void setupSwipeGestureDetection() {
        mGestureDetector = new GestureDetector(itemView.getContext(), new SwipeGestureListener());
        
        taskView.setOnTouchListener((v, event) -> {
            if (mIsAnimating) {
                return false;
            }
            
            boolean handled = mGestureDetector.onTouchEvent(event);
            
            // Handle touch up events for swipe completion
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (mIsSwipeInProgress) {
                    handleSwipeEnd();
                    return true; // Consume the event if it was a swipe
                }
            }
            
            // If not a swipe, let the click listener handle it
            if (handled) {
                return true;
            }

            // Fallback to ensure click works
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
            }

            return mIsSwipeInProgress;
        });
    }
    
    /**
     * Custom gesture listener for swipe detection
     */
    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        
        @Override
        public boolean onDown(MotionEvent e) {
            mInitialTouchY = e.getY();
            return true;
        }
        
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (e1 == null || e2 == null) return false;
            
            float deltaY = e2.getY() - e1.getY();
            float deltaX = e2.getX() - e1.getX();
            
            // Only handle upward swipes (negative deltaY)
            if (deltaY < 0 && Math.abs(deltaY) > Math.abs(deltaX)) {
                if (!mIsSwipeInProgress) {
                    mIsSwipeInProgress = true;
                }
                
                // Update translation and visual effects
                updateSwipeProgress(deltaY);
                return true;
            }
            
            return false;
        }
        
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) return false;
            
            // Check for upward fling with sufficient velocity
            if (velocityY < -SWIPE_DISMISS_VELOCITY_THRESHOLD && Math.abs(velocityY) > Math.abs(velocityX)) {
                if (mIsSwipeInProgress) {
                    animateSwipeDismiss();
                    return true;
                }
            }
            
            return false;
        }
    }
    
    /**
     * Update visual effects during swipe progress
     */
    private void updateSwipeProgress(float deltaY) {
        int viewHeight = taskView.getHeight();
        if (viewHeight == 0) return;
        
        // Calculate progress (0 to 1)
        float progress = Math.min(1f, Math.abs(deltaY) / viewHeight);
        
        // Apply translation
        mCurrentTranslationY = deltaY;
        taskView.setTranslationY(deltaY);
        
        // Apply alpha effect (fade out as swiping)
        float alpha = 1f - (progress * (1f - SWIPE_PROGRESS_ALPHA_MIN));
        taskView.setAlpha(alpha);
        
        // Apply scale effect (shrink as swiping)
        float scale = 1f - (progress * (1f - SWIPE_PROGRESS_SCALE_MIN));
        taskView.setScaleX(scale);
        taskView.setScaleY(scale);
    }
    
    /**
     * Handle end of swipe gesture
     */
    private void handleSwipeEnd() {
        if (!mIsSwipeInProgress) return;
        
        int viewHeight = taskView.getHeight();
        float swipeDistance = Math.abs(mCurrentTranslationY);
        float swipeThreshold = viewHeight * SWIPE_DISMISS_DISTANCE_THRESHOLD;
        
        if (swipeDistance >= swipeThreshold) {
            // Swipe distance is sufficient, dismiss the task
            animateSwipeDismiss();
        } else {
            // Swipe distance is insufficient, return to original position
            animateSwipeReturn();
        }
    }
    
    /**
     * Animate swipe dismiss (upward slide out)
     */
    private void animateSwipeDismiss() {
        if (mIsAnimating) return;
        
        mIsAnimating = true;
        mIsSwipeInProgress = false;
        
        // Calculate final position (slide out upward)
        float finalY = -taskView.getHeight() * 1.2f;
        
        // Create dismiss animation
        PropertyValuesHolder translationY = PropertyValuesHolder.ofFloat("translationY", mCurrentTranslationY, finalY);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", taskView.getAlpha(), 0f);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", taskView.getScaleX(), 0.8f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", taskView.getScaleY(), 0.8f);
        
        ValueAnimator dismissAnimator = ValueAnimator.ofPropertyValuesHolder(translationY, alpha, scaleX, scaleY);
        dismissAnimator.setDuration(DISMISS_ANIMATION_DURATION);
        dismissAnimator.setInterpolator(new AccelerateInterpolator(1.5f));
        
        dismissAnimator.addUpdateListener(animation -> {
            taskView.setTranslationY((Float) animation.getAnimatedValue("translationY"));
            taskView.setAlpha((Float) animation.getAnimatedValue("alpha"));
            taskView.setScaleX((Float) animation.getAnimatedValue("scaleX"));
            taskView.setScaleY((Float) animation.getAnimatedValue("scaleY"));
        });
        
        dismissAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mListener != null && mTask != null) {
                    mListener.onTaskDismiss(mTask, getAdapterPosition());
                }
                mIsAnimating = false;
            }
        });
        
        dismissAnimator.start();
    }
    
    /**
     * Animate return to original position
     */
    private void animateSwipeReturn() {
        if (mIsAnimating) return;
        
        mIsAnimating = true;
        mIsSwipeInProgress = false;
        
        // Create return animation
        PropertyValuesHolder translationY = PropertyValuesHolder.ofFloat("translationY", mCurrentTranslationY, 0f);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", taskView.getAlpha(), 1f);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", taskView.getScaleX(), 1f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", taskView.getScaleY(), 1f);
        
        ValueAnimator returnAnimator = ValueAnimator.ofPropertyValuesHolder(translationY, alpha, scaleX, scaleY);
        returnAnimator.setDuration(SWIPE_RETURN_ANIMATION_DURATION);
        returnAnimator.setInterpolator(new DecelerateInterpolator(2.0f));
        
        returnAnimator.addUpdateListener(animation -> {
            taskView.setTranslationY((Float) animation.getAnimatedValue("translationY"));
            taskView.setAlpha((Float) animation.getAnimatedValue("alpha"));
            taskView.setScaleX((Float) animation.getAnimatedValue("scaleX"));
            taskView.setScaleY((Float) animation.getAnimatedValue("scaleY"));
        });
        
        returnAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimating = false;
                mCurrentTranslationY = 0f;
            }
        });
        
        returnAnimator.start();
    }
    
    public Task getTask() {
        return mTask;
    }
}