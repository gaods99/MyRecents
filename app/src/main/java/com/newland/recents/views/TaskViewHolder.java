package com.newland.recents.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewGroup;
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
     * 优化3：减小卡片基础宽度，更适合手机屏幕显示
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
        taskView.setAlpha(1f);
        taskView.setTranslationX(0f);
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
    
    public Task getTask() {
        return mTask;
    }
}