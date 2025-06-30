package com.newland.recents;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.newland.recents.adapter.TaskAdapter;
import com.newland.recents.loader.TaskLoader;
import com.newland.recents.manager.TaskManager;
import com.newland.recents.model.Task;
import com.newland.recents.utils.TaskViewSizeCalculator;

import java.util.List;

/**
 * Recent tasks activity, based on Launcher3 Quickstep implementation
 */
public class RecentsActivity extends Activity implements 
        TaskAdapter.TaskAdapterListener, TaskLoader.TaskLoadListener {
    
    private static final String TAG = "RecentsActivity";
    private static RecentsActivity sInstance;
    
    // UI Components
    private RecyclerView mTaskRecyclerView;
    private View mEmptyView;
    private ProgressBar mLoadingIndicator;
    
    // Core Components
    private TaskAdapter mTaskAdapter;
    private TaskLoader mTaskLoader;
    private TaskManager mTaskManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recents_activity);
        
        sInstance = this;
        
        initializeComponents();
        setupRecyclerView();
        loadTasks();
    }
    
    private void initializeComponents() {
        // Initialize UI components
        mTaskRecyclerView = findViewById(R.id.task_recycler_view);
        mEmptyView = findViewById(R.id.empty_view);
        mLoadingIndicator = findViewById(R.id.loading_indicator);
        
        // Initialize core components
        mTaskAdapter = new TaskAdapter(this);
        mTaskLoader = new TaskLoader(this);
        mTaskManager = new TaskManager(this);
    }
    
    private void setupRecyclerView() {
        // Setup RecyclerView with horizontal layout like Launcher3
        // Use custom centering layout manager for single item centering
        CenteringLinearLayoutManager layoutManager = new CenteringLinearLayoutManager(this, 
                LinearLayoutManager.HORIZONTAL, false);
        mTaskRecyclerView.setLayoutManager(layoutManager);
        mTaskRecyclerView.setAdapter(mTaskAdapter);
        
        // Use dynamic sizing like Launcher3 Quickstep
        TaskViewSizeCalculator sizeCalculator = new TaskViewSizeCalculator(this);
        
        // Set dynamic padding
        int padding = sizeCalculator.getRecyclerViewPadding();
        mTaskRecyclerView.setPaddingRelative(padding, 0, padding, 0);
        
        // Add item decoration for spacing
        int spacing = sizeCalculator.getTaskMargin();
        mTaskRecyclerView.addItemDecoration(new TaskItemDecoration(spacing));
        
        // Log debug info
        Log.d(TAG, "Dynamic sizing: " + sizeCalculator.getDebugInfo());
    }
    
    private void loadTasks() {
        showLoading(true);
        mTaskLoader.loadTasks(this);
    }
    
    private void showLoading(boolean show) {
        mLoadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        mTaskRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        mEmptyView.setVisibility(View.GONE);
    }
    
    private void updateEmptyState() {
        boolean isEmpty = mTaskAdapter.isEmpty();
        mEmptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        mTaskRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        mLoadingIndicator.setVisibility(View.GONE);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh tasks when activity resumes
        loadTasks();
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
    
    public void hideRecents() {
        finish();
        overridePendingTransition(0, R.anim.recents_exit);
    }
    
    // TaskLoader.TaskLoadListener implementation
    
    @Override
    public void onTasksLoaded(List<Task> tasks) {
        Log.d(TAG, "Loaded " + tasks.size() + " tasks");
        
        mTaskAdapter.updateTasks(tasks);
        updateEmptyState();
        
        // Load thumbnails for visible tasks
        for (Task task : tasks) {
            mTaskLoader.loadTaskThumbnail(task, this);
        }
    }
    
    @Override
    public void onTaskThumbnailLoaded(Task task, Bitmap thumbnail) {
        Log.d(TAG, "Thumbnail loaded for task: " + task.title);
        mTaskAdapter.updateTaskThumbnail(task, thumbnail);
    }
    
    // TaskAdapter.TaskAdapterListener implementation
    
    @Override
    public void onTaskClick(Task task, int position) {
        Log.d(TAG, "Task clicked: " + task.title);
        
        if (mTaskManager.startTask(task)) {
            hideRecents();
        } else {
            showToast(R.string.recents_launch_error);
        }
    }
    
    @Override
    public void onTaskDismiss(Task task, int position) {
        Log.d(TAG, "Task dismissed: " + task.title);
        
        if (mTaskManager.removeTask(task)) {
            mTaskAdapter.removeTask(position);
            updateEmptyState();
        } else {
            showToast(R.string.recents_remove_error);
        }
    }
    
    @Override
    public void onTaskLongClick(Task task, int position) {
        Log.d(TAG, "Task long clicked: " + task.title);
        // TODO: Show task options menu
    }
    
    private void showToast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }
    
    // Static methods for external access
    
    public static RecentsActivity getInstance() {
        return sInstance;
    }
    
    public static boolean isVisible() {
        return sInstance != null;
    }
    
    /**
     * Custom LinearLayoutManager that centers single items horizontally
     */
    private static class CenteringLinearLayoutManager extends LinearLayoutManager {
        
        public CenteringLinearLayoutManager(android.content.Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }
        
        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            super.onLayoutChildren(recycler, state);
            
            // Only center when there's exactly one item
            if (getItemCount() == 1) {
                View child = getChildAt(0);
                if (child != null) {
                    int parentWidth = getWidth() - getPaddingLeft() - getPaddingRight();
                    int childWidth = child.getWidth();
                    
                    if (childWidth < parentWidth) {
                        // Calculate center position
                        int centerOffset = (parentWidth - childWidth) / 2;
                        
                        // Apply centering offset
                        child.offsetLeftAndRight(centerOffset - child.getLeft() + getPaddingLeft());
                    }
                }
            }
        }
    }
    
    /**
     * Task item decoration for spacing
     */
    private static class TaskItemDecoration extends RecyclerView.ItemDecoration {
        private final int spacing;
        
        TaskItemDecoration(int spacing) {
            this.spacing = spacing;
        }
        
        @Override
        public void getItemOffsets(android.graphics.Rect outRect, View view, 
                RecyclerView parent, RecyclerView.State state) {
            int itemCount = state.getItemCount();
            int position = parent.getChildAdapterPosition(view);
            
            if (itemCount == 1) {
                // For single item, no spacing to allow perfect centering
                outRect.left = 0;
                outRect.right = 0;
            } else {
                // For multiple items, use normal spacing
                outRect.left = spacing;
                outRect.right = spacing;
                
                // Add extra spacing for first and last items
                if (position == 0) {
                    outRect.left = spacing * 2;
                }
                if (position == itemCount - 1) {
                    outRect.right = spacing * 2;
                }
            }
        }
    }
}