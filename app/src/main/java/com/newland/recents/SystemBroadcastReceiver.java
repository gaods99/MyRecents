package com.newland.recents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SystemBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        RecentsController controller = RecentsController.getInstance(context);

        if (controller == null) return;

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