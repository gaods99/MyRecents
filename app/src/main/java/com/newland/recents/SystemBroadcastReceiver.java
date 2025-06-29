package com.newland.recents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * System broadcast receiver for handling recents actions
 */
public class SystemBroadcastReceiver extends BroadcastReceiver {
    
    private static final String TAG = "SystemBroadcastReceiver";
    
    // Action constants
    public static final String ACTION_SHOW = "com.android.systemui.recents.ACTION_SHOW";
    public static final String ACTION_HIDE = "com.android.systemui.recents.ACTION_HIDE";
    public static final String ACTION_TOGGLE = "com.android.systemui.recents.ACTION_TOGGLE";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        
        String action = intent.getAction();
        Log.d(TAG, "Received broadcast: " + action);
        
        RecentsController controller = RecentsController.getInstance(context);
        if (controller == null) {
            Log.w(TAG, "RecentsController is null");
            return;
        }
        
        switch (action) {
            case ACTION_SHOW:
                Log.d(TAG, "Showing recents");
                controller.showRecents();
                break;
                
            case ACTION_HIDE:
                Log.d(TAG, "Hiding recents");
                controller.hideRecents();
                break;
                
            case ACTION_TOGGLE:
                Log.d(TAG, "Toggling recents");
                controller.toggleRecents();
                break;
                
            default:
                Log.w(TAG, "Unknown action: " + action);
                break;
        }
    }
}