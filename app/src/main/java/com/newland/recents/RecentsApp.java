package com.newland.recents;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.newland.recents.utils.SystemPropertiesProxy;

public class RecentsApp extends Application {
    private static final String TAG = "RecentsApp";
    private static RecentsApp sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        Log.d(TAG, "Recents application started");

        // 初始化系统属性代理
        SystemPropertiesProxy.getInstance();
    }

    public static RecentsApp getInstance() {
        return sInstance;
    }

    public static Context getContext() {
        return sInstance.getApplicationContext();
    }
}