package com.newland.recents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SystemBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "newland";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        RecentsController controller = RecentsController.getInstance(context);

        if (controller == null) return;

        Log.i(TAG, "received broadcast, action: " + action);

        switch (action) {
            case "com.android.systemui.recents.ACTION_SHOW":
                controller.showRecents();
                break;
            case "com.android.systemui.recents.ACTION_HIDE":
                controller.hideRecents();
                break;
            case "com.android.systemui.recents.ACTION_TOGGLE":
                controller.toggleRecents();
                break;
        }
    }
}