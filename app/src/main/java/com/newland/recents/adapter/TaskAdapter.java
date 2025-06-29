package com.newland.recents.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.newland.recents.R;
import com.newland.recents.model.Task;
import com.newland.recents.views.TaskViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务列表适配器，参考Launcher3的实现
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskViewHolder> 
        implements TaskViewHolder.TaskViewListener {
    
    private final List<Task> mTasks = new ArrayList<>();
    private TaskAdapterListener mListener;
    
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
        return new TaskViewHolder(view, this);
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
     * 更新任务列表
     */
    public void updateTasks(List<Task> tasks) {
        mTasks.clear();
        if (tasks != null) {
            mTasks.addAll(tasks);
        }
        notifyDataSetChanged();
    }
    
    /**
     * 添加任务
     */
    public void addTask(Task task) {
        if (task != null && !mTasks.contains(task)) {
            mTasks.add(0, task); // 添加到开头
            notifyItemInserted(0);
        }
    }
    
    /**
     * 移除任务
     */
    public void removeTask(int position) {
        if (position >= 0 && position < mTasks.size()) {
            mTasks.remove(position);
            notifyItemRemoved(position);
        }
    }
    
    /**
     * 移除任务
     */
    public void removeTask(Task task) {
        int position = mTasks.indexOf(task);
        if (position >= 0) {
            removeTask(position);
        }
    }
    
    /**
     * 获取任务
     */
    public Task getTask(int position) {
        if (position >= 0 && position < mTasks.size()) {
            return mTasks.get(position);
        }
        return null;
    }
    
    /**
     * 获取任务位置
     */
    public int getTaskPosition(Task task) {
        return mTasks.indexOf(task);
    }
    
    /**
     * 更新任务缩略图
     */
    public void updateTaskThumbnail(Task task, Bitmap thumbnail) {
        int position = getTaskPosition(task);
        if (position >= 0) {
            task.thumbnail = thumbnail;
            notifyItemChanged(position);
        }
    }
    
    /**
     * 清空任务列表
     */
    public void clear() {
        int count = mTasks.size();
        mTasks.clear();
        notifyItemRangeRemoved(0, count);
    }
    
    /**
     * 检查是否为空
     */
    public boolean isEmpty() {
        return mTasks.isEmpty();
    }
    
    /**
     * 获取所有任务
     */
    public List<Task> getTasks() {
        return new ArrayList<>(mTasks);
    }
    
    // TaskViewHolder.TaskViewListener 实现
    
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