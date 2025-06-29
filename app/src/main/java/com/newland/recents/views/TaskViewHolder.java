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

        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        if (task.thumbnail != null) {
            thumbnailView.setImageBitmap(task.thumbnail);
            
            // 动态计算并设置整个任务卡片的宽度，以匹配缩略图的宽高比
            int thumbnailHeight = task.thumbnail.getHeight();
            int thumbnailWidth = task.thumbnail.getWidth();
            int containerHeight = params.height;

            if (containerHeight > 0 && thumbnailHeight > 0) {
                float scale = (float) containerHeight / thumbnailHeight;
                params.width = (int) (thumbnailWidth * scale);
            }
            
        } else {
            thumbnailView.setImageDrawable(null);
            // 如果没有缩略图，恢复卡片的默认宽度
            params.width = mDefaultTaskWidth;
        }
        itemView.setLayoutParams(params);
        
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
                resetState();
            }
        });
        translateX.start();
    }
    
    public Task getTask() {
        return mTask;
    }
}