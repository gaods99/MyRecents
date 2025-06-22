package com.newland.recents;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class RecentsActivity extends Activity {
    private static final String TAG = "newland";
    private static RecentsActivity sInstance;
    private ViewPager mViewPager;
    private RecentsPagerAdapter mAdapter;
    private View mEmptyView;
    private ActivityManager mActivityManager;
    private String mLauncherPackageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recents_activity);

        sInstance = this;
        mViewPager = findViewById(R.id.view_pager);
        mEmptyView = findViewById(R.id.empty_view);
        mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        mViewPager.setPageMargin(16);  // px

        mLauncherPackageName = getLauncherPackageName();
        loadRecentTasks();
    }

    private String getLauncherPackageName() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo != null && resolveInfo.activityInfo != null) {
            return resolveInfo.activityInfo.packageName;
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTasks();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sInstance = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            hideRecents();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void loadRecentTasks() {
        List<ActivityManager.RecentTaskInfo> tasks = getRecentTasks();
        mAdapter = new RecentsPagerAdapter(this, tasks);
        mViewPager.setAdapter(mAdapter);
        updateEmptyState(tasks);
    }

    private List<ActivityManager.RecentTaskInfo> getRecentTasks() {
        try {
            List<ActivityManager.RecentTaskInfo> tasks = mActivityManager.getRecentTasks(30, ActivityManager.RECENT_WITH_EXCLUDED);
            List<ActivityManager.RecentTaskInfo> filteredTasks = new ArrayList<>();
            for (ActivityManager.RecentTaskInfo task : tasks) {
                if (task.baseIntent != null && task.baseIntent.getComponent() != null) {
                    String packageName = task.baseIntent.getComponent().getPackageName();
                    if (!"com.newland.recents".equals(packageName) && (mLauncherPackageName == null || !mLauncherPackageName.equals(packageName))) {
                        filteredTasks.add(task);
                    }
                } else {
                    filteredTasks.add(task); // Keep tasks with null baseIntent or component
                }
            }
            return filteredTasks;
        } catch (SecurityException e) {
            showToast(R.string.recents_permission_error);
            return new ArrayList<>();
        }
    }

    public void refreshTasks() {
        List<ActivityManager.RecentTaskInfo> tasks = getRecentTasks();
        mAdapter.updateTasks(tasks);
        updateEmptyState(tasks);
    }

    private void updateEmptyState(List<ActivityManager.RecentTaskInfo> tasks) {
        boolean isEmpty = tasks == null || tasks.isEmpty();
        mEmptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        mViewPager.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    public void removeTask(int position) {
        ActivityManager.RecentTaskInfo task = mAdapter.getTask(position);
        if (task != null) {
            try {
                Class<?> activityManagerClass = mActivityManager.getClass();
                Method removeTaskMethod = activityManagerClass.getDeclaredMethod(
                        "removeTask", int.class);
                removeTaskMethod.setAccessible(true);
                removeTaskMethod.invoke(mActivityManager, task.persistentId);
                refreshTasks();
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error removing task", e);
            }
        }
    }

    public void launchTask(int position) {
        ActivityManager.RecentTaskInfo task = mAdapter.getTask(position);
        if (task != null && task.baseIntent != null) {
            try {
                startActivity(task.baseIntent);
                finish();
                overridePendingTransition(0, 0);
            } catch (Exception e) {
                showToast(R.string.recents_launch_error);
            }
        }
    }

    public void hideRecents() {
        finish();
        overridePendingTransition(0, R.anim.recents_exit);
    }

    private void showToast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    public static RecentsActivity getInstance() {
        return sInstance;
    }

    public static boolean isVisible() {
        return sInstance != null;
    }
}