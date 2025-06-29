package com.newland.recents.loader;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import com.newland.recents.model.Task;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务加载器，参考SystemUI的实现
 */
public class TaskLoader {
    
    private static final String TAG = "TaskLoader";
    private static final int MAX_RECENT_TASKS = 20;
    
    private final Context mContext;
    private final ActivityManager mActivityManager;
    private final PackageManager mPackageManager;
    
    public interface TaskLoadListener {
        void onTasksLoaded(List<Task> tasks);
        void onTaskThumbnailLoaded(Task task, Bitmap thumbnail);
    }
    
    public TaskLoader(Context context) {
        mContext = context.getApplicationContext();
        mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        mPackageManager = mContext.getPackageManager();
    }
    
    /**
     * 加载最近任务列表
     */
    public void loadTasks(TaskLoadListener listener) {
        new LoadTasksTask(listener).execute();
    }
    
    /**
     * 加载任务缩略图
     */
    public void loadTaskThumbnail(Task task, TaskLoadListener listener) {
        new LoadThumbnailTask(task, listener).execute();
    }
    
    /**
     * 获取最近任务列表
     */
    private List<Task> getRecentTasks() {
        List<Task> tasks = new ArrayList<>();
        
        try {
            List<ActivityManager.RecentTaskInfo> recentTasks = 
                mActivityManager.getRecentTasks(MAX_RECENT_TASKS, 
                    ActivityManager.RECENT_WITH_EXCLUDED);
            
            for (ActivityManager.RecentTaskInfo taskInfo : recentTasks) {
                if (shouldIncludeTask(taskInfo)) {
                    Task task = new Task(taskInfo);
                    loadTaskInfo(task);
                    tasks.add(task);
                }
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to load recent tasks", e);
        }
        
        return tasks;
    }
    
    /**
     * 检查是否应该包含此任务
     */
    private boolean shouldIncludeTask(ActivityManager.RecentTaskInfo taskInfo) {
        if (taskInfo.baseIntent == null || taskInfo.baseIntent.getComponent() == null) {
            return false;
        }
        
        String packageName = taskInfo.baseIntent.getComponent().getPackageName();
        
        // 过滤掉自己的应用
        if ("com.newland.recents".equals(packageName)) {
            return false;
        }
        
        // 过滤掉系统UI
        if ("com.android.systemui".equals(packageName)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 加载任务信息（图标、标题等）
     */
    private void loadTaskInfo(Task task) {
        if (task.key.sourceComponent == null) {
            return;
        }
        
        try {
            ApplicationInfo appInfo = mPackageManager.getApplicationInfo(
                task.packageName, PackageManager.GET_META_DATA);
            
            // 加载应用图标
            task.icon = mPackageManager.getApplicationIcon(appInfo);
            
            // 加载应用标题
            CharSequence label = mPackageManager.getApplicationLabel(appInfo);
            task.title = label != null ? label.toString() : task.packageName;
            task.titleDescription = task.title;
            
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Failed to load task info for " + task.packageName, e);
            task.title = task.packageName;
            task.titleDescription = task.packageName;
        }
    }
    
    /**
     * 获取任务缩略图，参考SystemUI的实现
     */
    private Bitmap getTaskThumbnail(Task task) {
        try {
            // 使用反射调用ActivityManager.getTaskThumbnail
            Method getTaskThumbnailMethod = ActivityManager.class.getMethod("getTaskThumbnail", int.class);
            Object taskThumbnailObject = getTaskThumbnailMethod.invoke(mActivityManager, task.key.id);
            
            if (taskThumbnailObject != null) {
                // 获取mainThumbnail字段
                java.lang.reflect.Field mainThumbnailField = 
                    taskThumbnailObject.getClass().getDeclaredField("mainThumbnail");
                mainThumbnailField.setAccessible(true);
                Bitmap thumbnail = (Bitmap) mainThumbnailField.get(taskThumbnailObject);
                
                if (thumbnail != null && !thumbnail.isRecycled()) {
                    return thumbnail;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to get task thumbnail for task " + task.key.id, e);
        }
        
        // 如果无法获取缩略图，生成默认缩略图
        return generateDefaultThumbnail(task);
    }
    
    /**
     * 生成默认缩略图
     */
    private Bitmap generateDefaultThumbnail(Task task) {
        if (task.icon == null) {
            return null;
        }
        
        try {
            int size = mContext.getResources().getDimensionPixelSize(
                android.R.dimen.app_icon_size);
            
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            
            task.icon.setBounds(0, 0, size, size);
            task.icon.draw(canvas);
            
            return bitmap;
        } catch (Exception e) {
            Log.w(TAG, "Failed to generate default thumbnail", e);
            return null;
        }
    }
    
    /**
     * 异步加载任务列表
     */
    private class LoadTasksTask extends AsyncTask<Void, Void, List<Task>> {
        private final TaskLoadListener mListener;
        
        LoadTasksTask(TaskLoadListener listener) {
            mListener = listener;
        }
        
        @Override
        protected List<Task> doInBackground(Void... voids) {
            return getRecentTasks();
        }
        
        @Override
        protected void onPostExecute(List<Task> tasks) {
            if (mListener != null) {
                mListener.onTasksLoaded(tasks);
            }
        }
    }
    
    /**
     * 异步加载缩略图
     */
    private class LoadThumbnailTask extends AsyncTask<Void, Void, Bitmap> {
        private final Task mTask;
        private final TaskLoadListener mListener;
        
        LoadThumbnailTask(Task task, TaskLoadListener listener) {
            mTask = task;
            mListener = listener;
        }
        
        @Override
        protected Bitmap doInBackground(Void... voids) {
            return getTaskThumbnail(mTask);
        }
        
        @Override
        protected void onPostExecute(Bitmap thumbnail) {
            if (mListener != null && thumbnail != null) {
                mTask.thumbnail = thumbnail;
                mListener.onTaskThumbnailLoaded(mTask, thumbnail);
            }
        }
    }
}