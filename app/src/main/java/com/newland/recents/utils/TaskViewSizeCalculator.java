package com.newland.recents.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * Task view size calculator, based on Android 7.1.2 SystemUI recents implementation
 * Uses fixed aspect ratio similar to original SystemUI design
 */
public class TaskViewSizeCalculator {
    
    private static final String TAG = "TaskViewSizeCalculator";
    
    // Android 7.1.2 SystemUI recents style - exact original dimensions and ratios
    
    // SystemUI original task dimensions (dp) - from Android 7.1.2 source
    private static final int SYSTEMUI_TASK_WIDTH_DP = 312;   // Original SystemUI width
    private static final int SYSTEMUI_TASK_HEIGHT_DP = 420;  // Original SystemUI height
    
    // SystemUI task aspect ratio - calculated from original dimensions
    private static final float SYSTEMUI_TASK_ASPECT_RATIO = 312f / 420f; // ~0.743 (width/height)
    
    // SystemUI screen usage ratios - based on original implementation
    private static final float SYSTEMUI_WIDTH_RATIO = 0.9f;   // 90% of screen width
    private static final float SYSTEMUI_HEIGHT_RATIO = 0.75f; // 75% of screen height
    
    // SystemUI size limits (dp) - based on original constraints
    private static final int MIN_TASK_WIDTH_DP = 280;
    private static final int MAX_TASK_WIDTH_DP = 360;
    private static final int MIN_TASK_HEIGHT_DP = 380;
    private static final int MAX_TASK_HEIGHT_DP = 480;
    
    // Task header height (dp) - SystemUI style
    private static final int TASK_HEADER_HEIGHT_DP = 48;
    
    private final Context mContext;
    private final DisplayMetrics mDisplayMetrics;
    private final Point mScreenSize;
    private final boolean mIsLandscape;
    
    public TaskViewSizeCalculator(Context context) {
        mContext = context;
        mDisplayMetrics = context.getResources().getDisplayMetrics();
        mScreenSize = getScreenSize(context);
        mIsLandscape = isLandscape(context);
    }
    
    /**
     * Calculate task card width - SystemUI style
     */
    public int getTaskWidth() {
        // Use SystemUI original width as base
        int baseWidth = dpToPx(SYSTEMUI_TASK_WIDTH_DP);
        
        // Scale based on screen density and size
        float screenWidthRatio = (float) mScreenSize.x / dpToPx(360); // Based on 360dp reference width
        float scaleFactor = Math.min(1.2f, Math.max(0.8f, screenWidthRatio));
        
        int scaledWidth = (int) (baseWidth * scaleFactor);
        
        // Apply SystemUI size limits
        int minWidth = dpToPx(MIN_TASK_WIDTH_DP);
        int maxWidth = dpToPx(MAX_TASK_WIDTH_DP);
        
        return Math.max(minWidth, Math.min(maxWidth, scaledWidth));
    }
    
    /**
     * Calculate task card height - SystemUI style with original aspect ratio
     */
    public int getTaskHeight() {
        // Use SystemUI original height as base
        int baseHeight = dpToPx(SYSTEMUI_TASK_HEIGHT_DP);
        
        // Scale proportionally with width
        int taskWidth = getTaskWidth();
        int baseWidth = dpToPx(SYSTEMUI_TASK_WIDTH_DP);
        float scaleFactor = (float) taskWidth / baseWidth;
        
        int scaledHeight = (int) (baseHeight * scaleFactor);
        
        // Ensure we don't exceed screen height
        int maxScreenHeight = (int) (mScreenSize.y * SYSTEMUI_HEIGHT_RATIO);
        scaledHeight = Math.min(scaledHeight, maxScreenHeight);
        
        // Apply SystemUI size limits
        int minHeight = dpToPx(MIN_TASK_HEIGHT_DP);
        int maxHeight = dpToPx(MAX_TASK_HEIGHT_DP);
        
        return Math.max(minHeight, Math.min(maxHeight, scaledHeight));
    }
    
    
    /**
     * Get task header height
     */
    public int getTaskHeaderHeight() {
        return dpToPx(TASK_HEADER_HEIGHT_DP);
    }
    
    /**
     * Get task thumbnail height (total height minus header height)
     */
    public int getTaskThumbnailHeight() {
        return getTaskHeight() - getTaskHeaderHeight();
    }
    
    
    
    /**
     * Calculate spacing between task cards - SystemUI style
     */
    public int getTaskMargin() {
        // SystemUI uses consistent smaller margins
        return dpToPx(6);
    }
    
    /**
     * Calculate RecyclerView horizontal padding - SystemUI style
     */
    public int getRecyclerViewPadding() {
        // SystemUI uses minimal padding
        return dpToPx(12);
    }
    
    /**
     * Get screen size
     */
    private Point getScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }
    
    /**
     * Check if in landscape orientation
     */
    private boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation 
                == Configuration.ORIENTATION_LANDSCAPE;
    }
    
    /**
     * Convert dp to px
     */
    private int dpToPx(int dp) {
        return Math.round(dp * mDisplayMetrics.density);
    }
    
    /**
     * Get debug information
     */
    public String getDebugInfo() {
        int currentWidth = getTaskWidth();
        int currentHeight = getTaskHeight();
        int originalWidthPx = dpToPx(SYSTEMUI_TASK_WIDTH_DP);
        int originalHeightPx = dpToPx(SYSTEMUI_TASK_HEIGHT_DP);
        float taskAspectRatio = (float) currentWidth / currentHeight;
        
        return String.format(
            "Screen: %dx%d, Density: %.1f, Landscape: %b\n" +
            "SystemUI AspectRatio: %.3f, TaskAspectRatio: %.3f\n" +
            "TaskSize: %dx%d\n" +
            "SystemUI Original: %dx%d (%.3f ratio)\n" +
            "Scale: %.2fx width, %.2fx height",
            mScreenSize.x, mScreenSize.y, mDisplayMetrics.density, mIsLandscape,
            SYSTEMUI_TASK_ASPECT_RATIO, taskAspectRatio,
            currentWidth, currentHeight,
            originalWidthPx, originalHeightPx, SYSTEMUI_TASK_ASPECT_RATIO,
            (float) currentWidth / originalWidthPx, (float) currentHeight / originalHeightPx
        );
    }
    
}