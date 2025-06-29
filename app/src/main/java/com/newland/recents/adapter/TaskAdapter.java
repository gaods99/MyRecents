package com.newland.recents.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.newland.recents.R;
import com.newland.recents.model.Task;
import com.newland.recents.utils.TaskViewSizeCalculator;
import com.newland.recents.views.TaskViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Task list adapter, based on Launcher3 implementation
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskViewHolder> 
        implements TaskViewHolder.TaskViewListener {
    
    private final List<Task> mTasks = new ArrayList<>();
    private TaskAdapterListener mListener;
    private TaskViewSizeCalculator mSizeCalculator;
    
    public interface TaskAdapterListener {
        void onTaskClick(Task task, int position);
        void onTaskDismiss(Task task, int position);
        void onTaskLongClick(Task task, int position);
    }
    
    public TaskAdapter(TaskAdapterListener listener) {
        mListener = listener;
        setHasStableIds(true);
    }
    
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        
        // Initialize size calculator if not already done
        if (mSizeCalculator == null) {
            mSizeCalculator = new TaskViewSizeCalculator(parent.getContext());
        }
        
        // Dynamically set task card size
        int taskWidth = mSizeCalculator.getTaskWidth();
        int taskHeight = mSizeCalculator.getTaskHeight();
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = taskWidth;
        layoutParams.height = taskHeight;
        view.setLayoutParams(layoutParams);
        
        TaskViewHolder holder = new TaskViewHolder(view, this);
        holder.setDefaultTaskWidth(taskWidth); // 保存默认宽度
        return holder;
    }
    
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = mTasks.get(position);
        holder.bind(task);
    }
    
    @Override
    public int getItemCount() {
        return mTasks.size();
    }
    
    @Override
    public long getItemId(int position) {
        if (position >= 0 && position < mTasks.size()) {
            Task task = mTasks.get(position);
            return task.key != null ? task.key.id : RecyclerView.NO_ID;
        }
        return RecyclerView.NO_ID;
    }
    
    /**
     * Update task list
     */
    public void updateTasks(List<Task> tasks) {
        mTasks.clear();
        if (tasks != null) {
            mTasks.addAll(tasks);
        }
        notifyDataSetChanged();
    }
    
    /**
     * Add task
     */
    public void addTask(Task task) {
        if (task != null && !mTasks.contains(task)) {
            mTasks.add(0, task); // Add to beginning
            notifyItemInserted(0);
        }
    }
    
    /**
     * Remove task by position
     */
    public void removeTask(int position) {
        if (position >= 0 && position < mTasks.size()) {
            mTasks.remove(position);
            notifyItemRemoved(position);
        }
    }
    
    /**
     * Remove task by object
     */
    public void removeTask(Task task) {
        int position = mTasks.indexOf(task);
        if (position >= 0) {
            removeTask(position);
        }
    }
    
    /**
     * Get task by position
     */
    public Task getTask(int position) {
        if (position >= 0 && position < mTasks.size()) {
            return mTasks.get(position);
        }
        return null;
    }
    
    /**
     * Get task position
     */
    public int getTaskPosition(Task task) {
        return mTasks.indexOf(task);
    }
    
    /**
     * Update task thumbnail
     */
    public void updateTaskThumbnail(Task task, Bitmap thumbnail) {
        int position = getTaskPosition(task);
        if (position >= 0) {
            task.thumbnail = thumbnail;
            notifyItemChanged(position);
        }
    }
    
    /**
     * Clear task list
     */
    public void clear() {
        int count = mTasks.size();
        mTasks.clear();
        notifyItemRangeRemoved(0, count);
    }
    
    /**
     * Check if empty
     */
    public boolean isEmpty() {
        return mTasks.isEmpty();
    }
    
    /**
     * Get all tasks
     */
    public List<Task> getTasks() {
        return new ArrayList<>(mTasks);
    }
    
    // TaskViewHolder.TaskViewListener implementation
    
    @Override
    public void onTaskClick(Task task, int position) {
        if (mListener != null) {
            mListener.onTaskClick(task, position);
        }
    }
    
    @Override
    public void onTaskDismiss(Task task, int position) {
        if (mListener != null) {
            mListener.onTaskDismiss(task, position);
        }
    }
    
    @Override
    public void onTaskLongClick(Task task, int position) {
        if (mListener != null) {
            mListener.onTaskLongClick(task, position);
        }
    }
}