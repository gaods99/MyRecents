package com.newland.recents.loader;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
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
import java.util.Iterator;
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
            List<ActivityManager.RecentTaskInfo> recentTasks = getRecentTasks(48);

            for (ActivityManager.RecentTaskInfo taskInfo : recentTasks) {
                Task task = new Task(taskInfo);
                loadTaskInfo(task);
//                Log.i(TAG, "Add: " + task.packageName
//                        + " taskId: " + task.key.id
//                        + " baseIntent: " + taskInfo.baseIntent);
                tasks.add(task);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to load recent tasks", e);
        }
        return tasks;
    }

    public static final int RECENT_WITH_EXCLUDED = 0x0001;
    public static final int RECENT_IGNORE_UNAVAILABLE = 0x0002;
    public static final int RECENT_INCLUDE_PROFILES = 0x0004;
    public static final int RECENT_IGNORE_HOME_STACK_TASKS = 0x0008;
    public static final int RECENT_INGORE_DOCKED_STACK_TOP_TASK = 0x0010;
    public static final int RECENT_INGORE_PINNED_STACK_TASKS = 0x0020;

    public List<ActivityManager.RecentTaskInfo> getRecentTasks(int numLatestTasks) {
        // Remove home/recents/excluded tasks
        int minNumTasksToQuery = 10;
        int numTasksToQuery = Math.max(minNumTasksToQuery, numLatestTasks);
        int flags = RECENT_IGNORE_HOME_STACK_TASKS |
                RECENT_INGORE_DOCKED_STACK_TOP_TASK |
                RECENT_INGORE_PINNED_STACK_TASKS |
                RECENT_IGNORE_UNAVAILABLE |
                RECENT_INCLUDE_PROFILES |
                RECENT_WITH_EXCLUDED;

        List<ActivityManager.RecentTaskInfo> tasks = null;
        try {
            tasks = mActivityManager.getRecentTasks(numTasksToQuery, flags);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get recent tasks", e);
        }

        // Break early if we can't get a valid set of tasks
        if (tasks == null) {
            return new ArrayList<>();
        }

        boolean isFirstValidTask = true;
        Iterator<ActivityManager.RecentTaskInfo> iter = tasks.iterator();
        while (iter.hasNext()) {
            ActivityManager.RecentTaskInfo t = iter.next();
            // NOTE: The order of these checks happens in the expected order of the traversal of the
            // tasks
            // Remove the task if it is marked as excluded, unless it is the first most task and we
            // are requested to include it
            boolean isExcluded = (t.baseIntent.getFlags() & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    == Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;
            if (isExcluded && (!isFirstValidTask)) {
                iter.remove();
            }

            if (!shouldIncludeTask(t)) {
                iter.remove();
            }

            isFirstValidTask = false;
        }

        return tasks.subList(0, Math.min(tasks.size(), numLatestTasks));
    }

    private boolean shouldIncludeTask(ActivityManager.RecentTaskInfo taskInfo) {
        if (taskInfo.baseIntent.getComponent() == null) {
            return false;
        }
        String packageName = taskInfo.baseIntent.getComponent().getPackageName();
        return !"com.newland.recents".equals(packageName) && !"com.android.systemui".equals(packageName);
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