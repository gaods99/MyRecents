package com.newland.recents.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.newland.recents.R;
import com.newland.recents.model.Task;

public class TaskView extends FrameLayout {

    private Task mTask;

    private ImageView mThumbnailView;
    private ImageView mIconView;
    private TextView mTitleView;

    public TaskView(Context context) {
        this(context, null);
    }

    public TaskView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TaskView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.task_item, this, true);
        mThumbnailView = findViewById(R.id.task_thumbnail);
        mIconView = findViewById(R.id.task_icon);
        mTitleView = findViewById(R.id.task_title);
    }

    public void bind(Task task) {
        mTask = task;
        mTitleView.setText(task.title);
        if (task.icon != null) {
            mIconView.setImageDrawable(task.icon);
        }
    }

    public Task getTask() {
        return mTask;
    }

    public void setThumbnail(Bitmap thumbnail) {
        if (thumbnail != null) {
            mThumbnailView.setImageBitmap(thumbnail);
        }
    }
}