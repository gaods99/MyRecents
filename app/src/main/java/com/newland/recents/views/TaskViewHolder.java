package com.newland.recents.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.newland.recents.R;
import com.newland.recents.model.Task;

/**
 * 任务视图持有者，参考Launcher3的TaskView实现
 */
public class TaskViewHolder extends RecyclerView.ViewHolder {
    
    private static final long DISMISS_ANIMATION_DURATION = 300;
    private static final long RETURN_ANIMATION_DURATION = 200;
    
    public final CardView cardView;
    public final ImageView thumbnailView;
    public final ImageView iconView;
    public final TextView titleView;
    public final View dismissButton;
    
    private Task mTask;
    private TaskViewListener mListener;
    private boolean mIsAnimating = false;
    
    public interface TaskViewListener {
        void onTaskClick(Task task, int position);
        void onTaskDismiss(Task task, int position);
        void onTaskLongClick(Task task, int position);
    }
    
    public TaskViewHolder(View itemView, TaskViewListener listener) {
        super(itemView);
        
        mListener = listener;
        
        cardView = (CardView) itemView;
        thumbnailView = itemView.findViewById(R.id.task_thumbnail);
        iconView = itemView.findViewById(R.id.task_icon);
        titleView = itemView.findViewById(R.id.task_title);
        dismissButton = itemView.findViewById(R.id.task_dismiss);
        
        setupClickListeners();
    }
    
    private void setupClickListeners() {
        // 点击任务启动应用
        cardView.setOnClickListener(v -> {
            if (!mIsAnimating && mTask != null && mListener != null) {
                mListener.onTaskClick(mTask, getAdapterPosition());
            }
        });
        
        // 长按任务
        cardView.setOnLongClickListener(v -> {
            if (!mIsAnimating && mTask != null && mListener != null) {
                mListener.onTaskLongClick(mTask, getAdapterPosition());
                return true;
            }
            return false;
        });
        
        // 点击删除按钮
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
     */
    public void bind(Task task) {
        mTask = task;
        
        // 重置动画状态
        mIsAnimating = false;
        cardView.setAlpha(1f);
        cardView.setScaleX(1f);
        cardView.setScaleY(1f);
        cardView.setTranslationX(0f);
        cardView.setTranslationY(0f);
        
        // 设置任务标题
        if (titleView != null) {
            titleView.setText(task.title != null ? task.title : task.packageName);
        }
        
        // 设置应用图标
        if (iconView != null && task.icon != null) {
            iconView.setImageDrawable(task.icon);
            iconView.setVisibility(View.VISIBLE);
        } else if (iconView != null) {
            iconView.setVisibility(View.GONE);
        }
        
        // 设置缩略图
        if (thumbnailView != null) {
            if (task.thumbnail != null) {
                thumbnailView.setImageBitmap(task.thumbnail);
                thumbnailView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                // 使用默认缩略图或应用图标
                if (task.icon != null) {
                    thumbnailView.setImageDrawable(task.icon);
                    thumbnailView.setScaleType(ImageView.ScaleType.CENTER);
                } else {
                    thumbnailView.setImageResource(R.drawable.ic_default_task);
                    thumbnailView.setScaleType(ImageView.ScaleType.CENTER);
                }
            }
        }
        
        // 设置焦点状态
        cardView.setSelected(task.isFocused);
        
        // 设置激活状态
        if (task.isActive) {
            cardView.setCardElevation(cardView.getResources().getDimension(R.dimen.task_elevation_active));
        } else {
            cardView.setCardElevation(cardView.getResources().getDimension(R.dimen.task_elevation));
        }
    }
    
    /**
     * 更新缩略图
     */
    public void updateThumbnail(Bitmap thumbnail) {
        if (thumbnailView != null && thumbnail != null) {
            thumbnailView.setImageBitmap(thumbnail);
            thumbnailView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }
    
    /**
     * 执行删除动画
     */
    public void animateDismiss() {
        if (mIsAnimating) {
            return;
        }
        
        mIsAnimating = true;
        
        // 向右滑出动画
        ObjectAnimator translateX = ObjectAnimator.ofFloat(cardView, "translationX", 
                0f, cardView.getWidth());
        translateX.setDuration(DISMISS_ANIMATION_DURATION);
        
        // 透明度动画
        ObjectAnimator alpha = ObjectAnimator.ofFloat(cardView, "alpha", 1f, 0f);
        alpha.setDuration(DISMISS_ANIMATION_DURATION);
        
        // 缩放动画
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardView, "scaleX", 1f, 0.8f);
        scaleX.setDuration(DISMISS_ANIMATION_DURATION);
        
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardView, "scaleY", 1f, 0.8f);
        scaleY.setDuration(DISMISS_ANIMATION_DURATION);
        
        translateX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimating = false;
                if (mTask != null && mListener != null) {
                    mListener.onTaskDismiss(mTask, getAdapterPosition());
                }
            }
        });
        
        translateX.start();
        alpha.start();
        scaleX.start();
        scaleY.start();
    }
    
    /**
     * 执行返回动画
     */
    public void animateReturn() {
        if (mIsAnimating) {
            return;
        }
        
        mIsAnimating = true;
        
        ObjectAnimator translateX = ObjectAnimator.ofFloat(cardView, "translationX", 
                cardView.getTranslationX(), 0f);
        translateX.setDuration(RETURN_ANIMATION_DURATION);
        
        ObjectAnimator alpha = ObjectAnimator.ofFloat(cardView, "alpha", 
                cardView.getAlpha(), 1f);
        alpha.setDuration(RETURN_ANIMATION_DURATION);
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardView, "scaleX", 
                cardView.getScaleX(), 1f);
        scaleX.setDuration(RETURN_ANIMATION_DURATION);
        
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardView, "scaleY", 
                cardView.getScaleY(), 1f);
        scaleY.setDuration(RETURN_ANIMATION_DURATION);
        
        translateX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimating = false;
            }
        });
        
        translateX.start();
        alpha.start();
        scaleX.start();
        scaleY.start();
    }
    
    /**
     * 设置拖拽状态
     */
    public void setDragState(boolean isDragging) {
        if (isDragging) {
            cardView.setCardElevation(cardView.getResources().getDimension(R.dimen.task_elevation_dragging));
            cardView.setAlpha(0.8f);
        } else {
            cardView.setCardElevation(cardView.getResources().getDimension(R.dimen.task_elevation));
            cardView.setAlpha(1f);
        }
    }
    
    public Task getTask() {
        return mTask;
    }
    
    public boolean isAnimating() {
        return mIsAnimating;
    }
}