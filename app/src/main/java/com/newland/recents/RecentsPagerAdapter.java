package com.newland.recents;

import android.app.ActivityManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.viewpager.widget.PagerAdapter;
import java.util.ArrayList;
import java.util.List;

public class RecentsPagerAdapter extends PagerAdapter {
    private final Context mContext;
    private final LayoutInflater mInflater;
    private List<ActivityManager.RecentTaskInfo> mTasks = new ArrayList<>();
    private final List<TaskView> mTaskViews = new ArrayList<>();

    public RecentsPagerAdapter(Context context, List<ActivityManager.RecentTaskInfo> tasks) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        updateTasks(tasks);
    }

    public void updateTasks(List<ActivityManager.RecentTaskInfo> tasks) {
        mTasks = tasks != null ? tasks : new ArrayList<>();
        mTaskViews.clear();
        notifyDataSetChanged();
    }

    public ActivityManager.RecentTaskInfo getTask(int position) {
        return (position >= 0 && position < mTasks.size()) ? mTasks.get(position) : null;
    }

    @Override
    public int getCount() {
        return mTasks.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        TaskView taskView = new TaskView(mContext);
        taskView.bind(mTasks.get(position), position);

        container.addView(taskView);
        mTaskViews.add(taskView);
        return taskView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        mTaskViews.remove(object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}