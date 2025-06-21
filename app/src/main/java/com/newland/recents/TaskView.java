package com.newland.recents;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.newland.recents.utils.FlingGestureDetector;

public class TaskView extends FrameLayout {
    private ActivityManager.RecentTaskInfo mTaskInfo;
    private int mPosition;
    private FlingGestureDetector mFlingDetector;

    public TaskView(Context context) {
        this(context, null);
    }

    public TaskView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.task_view, this);
        mFlingDetector = new FlingGestureDetector(getContext(), new FlingListener());
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mFlingDetector.onTouchEvent(event);
            }
        });
    }

    public void bind(ActivityManager.RecentTaskInfo taskInfo, int position) {
        mTaskInfo = taskInfo;
        mPosition = position;

        ImageView iconView = findViewById(R.id.task_icon);
        TextView titleView = findViewById(R.id.task_title);

        try {
            Drawable icon = getContext().getPackageManager().getActivityIcon(taskInfo.baseIntent);
            iconView.setImageDrawable(icon);
        } catch (Exception e) {
            iconView.setImageResource(R.drawable.ic_default_task);
        }

        String title = taskInfo.baseIntent.getComponent().getShortClassName();
        titleView.setText(title);

        setOnClickListener(v -> RecentsActivity.getInstance().launchTask(mPosition));
    }

    private class FlingListener implements FlingGestureDetector.FlingListener {
        @Override
        public void onFlingUp() {
            RecentsActivity.getInstance().removeTask(mPosition);
        }

        @Override
        public void onFlingDown() {}
    }
}