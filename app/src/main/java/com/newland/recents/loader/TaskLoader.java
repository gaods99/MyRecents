package com.newland.recents.loader;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.LruCache;

import com.newland.recents.model.Task;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 任务加载器，其架构参考SystemUI的实现，采用缓存优先、异步加载、失败后优雅降级的策略。
 */
public class TaskLoader {
    
    private static final String TAG = "TaskLoader";
    private static final int MAX_RECENT_TASKS = 20;
    
    private final Context mContext;
    private final ActivityManager mActivityManager;
    private final PackageManager mPackageManager;
    
    private final LruCache<String, Drawable> mIconCache;
    private final LruCache<Integer, Bitmap> mThumbnailCache;
    // 用于标记已知加载失败的任务，避免重复加载
    private final Set<Integer> mFailedTaskIds;
    
    public interface TaskLoadListener {
        void onTasksLoaded(List<Task> tasks);
        // 注意：此处的thumbnail参数现在可能为null
        void onTaskThumbnailLoaded(Task task, Bitmap thumbnail);
    }
    
    public TaskLoader(Context context) {
        mContext = context.getApplicationContext();
        mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        mPackageManager = mContext.getPackageManager();
        
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        
        mIconCache = new LruCache<>(cacheSize);
        mThumbnailCache = new LruCache<Integer, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Integer key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
        mFailedTaskIds = new HashSet<>();
    }
    
    public void loadTasks(TaskLoadListener listener) {
        new LoadTasksTask(listener).execute();
    }
    
    public void loadTaskThumbnail(Task task, TaskLoadListener listener) {
        if (mFailedTaskIds.contains(task.key.id)) {
            listener.onTaskThumbnailLoaded(task, null);
            return;
        }
        new LoadThumbnailTask(task, listener).execute();
    }
    
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
    
    private boolean shouldIncludeTask(ActivityManager.RecentTaskInfo taskInfo) {
        if (taskInfo.baseIntent == null || taskInfo.baseIntent.getComponent() == null) {
            return false;
        }
        String packageName = taskInfo.baseIntent.getComponent().getPackageName();
        if ("com.newland.recents".equals(packageName) || "com.android.systemui".equals(packageName)) {
            return false;
        }
        return !isLauncherApp(packageName);
    }
    
    private boolean isLauncherApp(String packageName) {
        try {
            android.content.Intent homeIntent = new android.content.Intent(android.content.Intent.ACTION_MAIN);
            homeIntent.addCategory(android.content.Intent.CATEGORY_HOME);
            android.content.pm.ResolveInfo resolveInfo = mPackageManager.resolveActivity(
                homeIntent, PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfo != null && resolveInfo.activityInfo != null) {
                return packageName.equals(resolveInfo.activityInfo.packageName);
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to check default launcher for " + packageName, e);
        }
        return false;
    }
    
    private void loadTaskInfo(Task task) {
        if (task.key.sourceComponent == null) return;
        Drawable icon = mIconCache.get(task.packageName);
        if (icon != null) {
            task.icon = icon;
        }
        try {
            ApplicationInfo appInfo = mPackageManager.getApplicationInfo(
                task.packageName, PackageManager.GET_META_DATA);
            if (task.icon == null) {
                task.icon = mPackageManager.getApplicationIcon(appInfo);
                mIconCache.put(task.packageName, task.icon);
            }
            CharSequence label = mPackageManager.getApplicationLabel(appInfo);
            task.title = label != null ? label.toString() : task.packageName;
            task.titleDescription = task.title;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Failed to load task info for " + task.packageName, e);
            task.title = task.packageName;
            task.titleDescription = task.packageName;
        }
    }
    
    private Bitmap getThumbnailFromSystem(int taskId) {
        try {
            Method getTaskThumbnailMethod = ActivityManager.class.getMethod("getTaskThumbnail", int.class);
            Object taskThumbnailObject = getTaskThumbnailMethod.invoke(mActivityManager, taskId);
            if (taskThumbnailObject != null) {
                java.lang.reflect.Field mainThumbnailField =
                    taskThumbnailObject.getClass().getDeclaredField("mainThumbnail");
                mainThumbnailField.setAccessible(true);
                Bitmap thumbnail = (Bitmap) mainThumbnailField.get(taskThumbnailObject);

                java.lang.reflect.Field descriptorField = taskThumbnailObject.getClass().getDeclaredField("thumbnailFileDescriptor");
                descriptorField.setAccessible(true);
                ParcelFileDescriptor descriptor = (ParcelFileDescriptor) descriptorField.get(taskThumbnailObject);

                if (thumbnail == null && descriptor != null) {
                    BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
                    sBitmapOptions.inMutable = true;
                    thumbnail = BitmapFactory.decodeFileDescriptor(descriptor.getFileDescriptor(),
                            null, sBitmapOptions);
                }
                if (descriptor != null) {
                    try {
                        descriptor.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }

                if (thumbnail != null && !thumbnail.isRecycled()) {
                    return thumbnail;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to get task thumbnail from system for task " + taskId, e);
        }
        return null;
    }
    
    private class LoadTasksTask extends AsyncTask<Void, Void, List<Task>> {
        private final TaskLoadListener mListener;
        LoadTasksTask(TaskLoadListener listener) { mListener = listener; }
        @Override
        protected List<Task> doInBackground(Void... voids) { return getRecentTasks(); }
        @Override
        protected void onPostExecute(List<Task> tasks) {
            if (mListener != null) { mListener.onTasksLoaded(tasks); }
        }
    }
    
    private class LoadThumbnailTask extends AsyncTask<Void, Void, Bitmap> {
        private final Task mTask;
        private final TaskLoadListener mListener;
        
        LoadThumbnailTask(Task task, TaskLoadListener listener) {
            mTask = task;
            mListener = listener;
        }
        
        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap thumbnail = mThumbnailCache.get(mTask.key.id);
            if (thumbnail != null) {
                return thumbnail;
            }
            thumbnail = getThumbnailFromSystem(mTask.key.id);
            if (thumbnail != null) {
                mThumbnailCache.put(mTask.key.id, thumbnail);
                return thumbnail;
            }
            // 如果加载失败，将任务ID加入失败集合
            mFailedTaskIds.add(mTask.key.id);
            return null;
        }
        
        @Override
        protected void onPostExecute(Bitmap thumbnail) {
            if (mListener != null) {
                // 直接将后台线程的结果（可能是null）传递出去
                mListener.onTaskThumbnailLoaded(mTask, thumbnail);
            }
        }
    }
}