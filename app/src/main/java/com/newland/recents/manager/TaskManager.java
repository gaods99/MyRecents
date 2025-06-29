package com.newland.recents.manager;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.newland.recents.model.Task;

import java.lang.reflect.Method;

/**
 * 任务管理器，参考SystemUI的实现
 */
public class TaskManager {
    
    private static final String TAG = "TaskManager";
    
    private final Context mContext;
    private final ActivityManager mActivityManager;
    
    public TaskManager(Context context) {
        mContext = context.getApplicationContext();
        mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
    }
    
    /**
     * 启动任务
     */
    public boolean startTask(Task task) {
        if (task == null || task.key == null || task.key.baseIntent == null) {
            Log.w(TAG, "Cannot start task: invalid task or intent");
            return false;
        }
        
        try {
            Intent intent = new Intent(task.key.baseIntent);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
                          Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            
            mContext.startActivity(intent);
            Log.d(TAG, "Started task: " + task.title);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to start task: " + task.title, e);
            return false;
        }
    }
    
    /**
     * 删除任务，参考SystemUI的实现
     */
    public boolean removeTask(Task task) {
        if (task == null || task.key == null) {
            Log.w(TAG, "Cannot remove task: invalid task");
            return false;
        }
        
        if (!task.isDismissable()) {
            Log.w(TAG, "Task is not dismissable: " + task.title);
            return false;
        }
        
        try {
            // 方法1：尝试使用ActivityManager.removeTask (系统权限)
            if (removeTaskWithActivityManager(task.key.id)) {
                Log.d(TAG, "Removed task using ActivityManager: " + task.title);
                return true;
            }
            
            // 方法2：尝试使用反射调用隐藏API (备用)
            if (removeTaskWithReflection(task.key.id)) {
                Log.d(TAG, "Removed task using reflection: " + task.title);
                return true;
            }
            
            // 方法3：尝试发送REMOVE_TASK广播 (最后备用)
            if (removeTaskWithBroadcast(task)) {
                Log.d(TAG, "Removed task using broadcast: " + task.title);
                return true;
            }
            
            Log.w(TAG, "All removal methods failed for task: " + task.title);
            return false;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to remove task: " + task.title, e);
            return false;
        }
    }
    
    /**
     * 使用ActivityManager.removeTask删除任务 (系统权限，通过反射)
     */
    private boolean removeTaskWithActivityManager(int taskId) {
        try {
            // 即使是系统应用，在Android 7中removeTask仍然是隐藏API
            Method removeTaskMethod = ActivityManager.class.getDeclaredMethod("removeTask", int.class);
            removeTaskMethod.setAccessible(true);
            Object result = removeTaskMethod.invoke(mActivityManager, taskId);
            return result instanceof Boolean ? (Boolean) result : true;
        } catch (SecurityException e) {
            Log.d(TAG, "removeTask requires system permission");
            return false;
        } catch (Exception e) {
            Log.w(TAG, "removeTask failed", e);
            return false;
        }
    }
    
    /**
     * 使用反射调用隐藏API删除任务
     */
    private boolean removeTaskWithReflection(int taskId) {
        try {
            Class<?> activityManagerClass = mActivityManager.getClass();
            Method removeTaskMethod = activityManagerClass.getDeclaredMethod("removeTask", int.class);
            removeTaskMethod.setAccessible(true);
            
            Object result = removeTaskMethod.invoke(mActivityManager, taskId);
            return result instanceof Boolean ? (Boolean) result : true;
            
        } catch (Exception e) {
            Log.d(TAG, "Reflection removeTask failed", e);
            return false;
        }
    }
    
    /**
     * 使用广播删除任务
     */
    private boolean removeTaskWithBroadcast(Task task) {
        try {
            Intent intent = new Intent("com.android.systemui.recents.REMOVE_TASK");
            intent.putExtra("task_id", task.key.id);
            intent.putExtra("package_name", task.packageName);
            
            mContext.sendBroadcast(intent);
            return true;
            
        } catch (Exception e) {
            Log.d(TAG, "Broadcast removeTask failed", e);
            return false;
        }
    }
    
    /**
     * 移动任务到前台
     */
    public boolean moveTaskToFront(Task task) {
        if (task == null || task.key == null) {
            return false;
        }
        
        try {
            mActivityManager.moveTaskToFront(task.key.id, ActivityManager.MOVE_TASK_WITH_HOME);
            Log.d(TAG, "Moved task to front: " + task.title);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to move task to front: " + task.title, e);
            return false;
        }
    }
    
    /**
     * 检查任务是否仍然存在
     */
    public boolean isTaskActive(Task task) {
        if (task == null || task.key == null) {
            return false;
        }
        
        try {
            for (ActivityManager.RunningTaskInfo runningTask : 
                 mActivityManager.getRunningTasks(100)) {
                if (runningTask.id == task.key.id) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to check if task is active", e);
        }
        
        return false;
    }
    
    /**
     * 获取当前前台任务
     */
    public Task getForegroundTask() {
        try {
            java.util.List<ActivityManager.RunningTaskInfo> runningTasks = 
                mActivityManager.getRunningTasks(1);
            
            if (!runningTasks.isEmpty()) {
                ActivityManager.RunningTaskInfo runningTask = runningTasks.get(0);
                Task task = new Task();
                task.key = new Task.TaskKey(runningTask.id, 0, null,
                    runningTask.baseActivity, 0, System.currentTimeMillis());
                return task;
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to get foreground task", e);
        }
        
        return null;
    }
}