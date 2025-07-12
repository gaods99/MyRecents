package com.newland.recents.model;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

/**
 * 任务数据模型，参考Launcher3的Task实现
 */
public class Task {
    
    public static class TaskKey {
        public final int id;
        public final int windowingMode;
        public final Intent baseIntent;
        public final ComponentName sourceComponent;
        public final int userId;
        public final long lastActiveTime;
        
        public TaskKey(int id, int windowingMode, Intent baseIntent, 
                      ComponentName sourceComponent, int userId, long lastActiveTime) {
            this.id = id;
            this.windowingMode = windowingMode;
            this.baseIntent = baseIntent;
            this.sourceComponent = sourceComponent;
            this.userId = userId;
            this.lastActiveTime = lastActiveTime;
        }
        
        public TaskKey(ActivityManager.RecentTaskInfo taskInfo) {
            this.id = taskInfo.persistentId;
            this.windowingMode = 0; // Default windowing mode
            this.baseIntent = taskInfo.baseIntent;
            this.sourceComponent = taskInfo.baseIntent.getComponent();
            this.userId = 0; // Default user ID for Android 7 compatibility
            this.lastActiveTime = System.currentTimeMillis(); // Use current time for Android 7 compatibility
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof TaskKey)) return false;
            TaskKey other = (TaskKey) obj;
            return id == other.id && userId == other.userId;
        }
        
        @Override
        public int hashCode() {
            return id * 31 + userId;
        }
    }
    
    public TaskKey key;
    public Drawable icon;
    public Bitmap thumbnail;
    public String title;
    public String titleDescription;
    public String packageName;
    public boolean isDockable = true;
    public boolean isLocked = false;
    public boolean isStackTask = true;
    public boolean isTopTask = false;
    
    // 任务状态
    public boolean isActive = false;
    public boolean isFocused = false;
    
    public Task() {
        // Empty constructor
    }
    
    public Task(TaskKey key) {
        this.key = key;
    }
    
    public Task(ActivityManager.RecentTaskInfo taskInfo) {
        this.key = new TaskKey(taskInfo);
        if (taskInfo.baseIntent.getComponent() != null) {
            this.packageName = taskInfo.baseIntent.getComponent().getPackageName();
        }
        this.title = taskInfo.description != null ? taskInfo.description.toString() : "";
    }
    
    /**
     * 检查任务是否可以被删除
     */
    public boolean isDismissable() {
        return !isLocked;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Task)) return false;
        Task other = (Task) obj;
        return key != null && key.equals(other.key);
    }
    
    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }
    
    @NonNull
    @Override
    public String toString() {
        return "Task{" +
                "key=" + key.id +
                ", title='" + title + '\'' +
                ", packageName='" + packageName + '\'' +
                '}';
    }
}