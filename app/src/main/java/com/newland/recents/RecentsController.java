package com.newland.recents;

import android.content.Context;
import android.content.Intent;

public class RecentsController {
    private static RecentsController sInstance;
    private final Context mContext;

    private RecentsController(Context context) {
        mContext = RecentsApp.getContext();
    }

    public static synchronized RecentsController getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RecentsController(context);
        }
        return sInstance;
    }

    public void showRecents() {
        if (RecentsActivity.isVisible()) return;

        try {
            Intent intent = new Intent();
            // Assuming RECENTS_PACKAGE and RECENTS_ACTIVITY are defined elsewhere,
            // for now, we use the known package and class name.
            String packageName = mContext.getPackageName();
            String activityName = RecentsActivity.class.getName();
            
            intent.setClassName(packageName, activityName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            
            // Using startActivity as we don't have system permissions for startActivityAsUser
            mContext.startActivity(intent);
        } catch (Exception e) {
            android.util.Log.e("RecentsController", "Failed to launch RecentsActivity", e);
        }
    }

    public void hideRecents() {
        if (RecentsActivity.isVisible()) {
            RecentsActivity.getInstance().hideRecents();
        }
    }

    public void toggleRecents() {
        if (RecentsActivity.isVisible()) {
            hideRecents();
        } else {
            showRecents();
        }
    }
}