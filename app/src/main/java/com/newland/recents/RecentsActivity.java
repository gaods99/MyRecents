package com.newland.recents;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.newland.recents.loader.TaskLoader;
import com.newland.recents.manager.TaskManager;
import com.newland.recents.model.Task;
import com.newland.recents.views.RecentsView;

import java.util.List;

public class RecentsActivity extends Activity implements TaskLoader.TaskLoadListener, RecentsView.RecentsViewCallbacks {
    private static RecentsActivity sInstance;

    private RecentsView mRecentsView;
    private View mEmptyView;
    private TaskLoader mTaskLoader;
    private TaskManager mTaskManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recents_activity);

        mRecentsView = findViewById(R.id.recents_view);
        mEmptyView = findViewById(R.id.empty_view);
        mRecentsView.setCallbacks(this);

        mTaskLoader = new TaskLoader(this);
        mTaskManager = new TaskManager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sInstance = this;
        mTaskLoader.loadTasks(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sInstance = null;
    }

    public static boolean isVisible() {
        return sInstance != null;
    }

    public static RecentsActivity getInstance() {
        return sInstance;
    }

    public void hideRecents() {
        finish();
    }

    @Override
    public void onTasksLoaded(List<Task> tasks) {
        if (tasks.isEmpty()) {
            mRecentsView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mRecentsView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
            mRecentsView.setTasks(tasks);
            for (Task task : tasks) {
                mTaskLoader.loadTaskThumbnail(task, this);
            }
        }
    }

    @Override
    public void onTaskThumbnailLoaded(Task task, android.graphics.Bitmap thumbnail) {
        if (mRecentsView != null) {
            com.newland.recents.views.TaskView taskView = mRecentsView.findTaskView(task.key.id);
            if (taskView != null) {
                taskView.setThumbnail(thumbnail);
            }
        }
    }

    @Override
    public void onTaskLaunched(Task task) {
        if (!mTaskManager.startTask(task)) {
            Toast.makeText(this, R.string.recents_launch_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTaskDismissed(Task task) {
        if (!mTaskManager.removeTask(task)) {
            Toast.makeText(this, R.string.recents_remove_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAllTasksRemoved() {
        mRecentsView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.VISIBLE);
    }
}