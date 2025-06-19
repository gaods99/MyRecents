package com.newland.recents;

import android.content.Context;
import android.content.Intent;

public class RecentsController {
    private static RecentsController sInstance;
    private final Context mContext;

    private RecentsController(Context context) {
        mContext = context.getApplicationContext();
    }

    public static synchronized RecentsController getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RecentsController(context);
        }
        return sInstance;
    }

    public void showRecents() {
        if (RecentsActivity.isVisible()) return;

        Intent intent = new Intent(mContext, RecentsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        mContext.startActivity(intent);
        mContext.startActivity(new Intent(mContext, RecentsActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
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