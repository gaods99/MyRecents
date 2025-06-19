package com.newland.recents.utils;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * 通过反射访问系统属性
 */
public class SystemPropertiesProxy {
    private static final String TAG = "SystemPropertiesProxy";

    private static final String SYSTEM_PROPERTIES_CLASS = "android.os.SystemProperties";
    private static volatile SystemPropertiesProxy sInstance;
    private Class<?> mSystemPropertiesClass;
    private Method mGetBooleanMethod;
    private Method mGetIntMethod;
    private Method mGetLongMethod;
    private Method mGetStringMethod;

    private SystemPropertiesProxy() {
        try {
            mSystemPropertiesClass = Class.forName(SYSTEM_PROPERTIES_CLASS);

            // 获取方法
            mGetBooleanMethod = mSystemPropertiesClass.getMethod(
                    "getBoolean", String.class, boolean.class);
            mGetIntMethod = mSystemPropertiesClass.getMethod(
                    "getInt", String.class, int.class);
            mGetLongMethod = mSystemPropertiesClass.getMethod(
                    "getLong", String.class, long.class);
            mGetStringMethod = mSystemPropertiesClass.getMethod(
                    "get", String.class);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize SystemPropertiesProxy", e);
        }
    }

    public static SystemPropertiesProxy getInstance() {
        if (sInstance == null) {
            synchronized (SystemPropertiesProxy.class) {
                if (sInstance == null) {
                    sInstance = new SystemPropertiesProxy();
                }
            }
        }
        return sInstance;
    }

    /**
     * 获取布尔类型的系统属性
     *
     * @param key 属性键
     * @param defValue 默认值
     * @return 属性值或默认值
     */
    public boolean getBoolean(Context context, String key, boolean defValue) {
        if (mGetBooleanMethod == null) {
            return defValue;
        }

        try {
            return (Boolean) mGetBooleanMethod.invoke(null, key, defValue);
        } catch (Exception e) {
            Log.e(TAG, "getBoolean failed for key: " + key, e);
            return defValue;
        }
    }

    /**
     * 获取整型系统属性
     *
     * @param key 属性键
     * @param defValue 默认值
     * @return 属性值或默认值
     */
    public int getInt(Context context, String key, int defValue) {
        if (mGetIntMethod == null) {
            return defValue;
        }

        try {
            return (Integer) mGetIntMethod.invoke(null, key, defValue);
        } catch (Exception e) {
            Log.e(TAG, "getInt failed for key: " + key, e);
            return defValue;
        }
    }

    /**
     * 获取长整型系统属性
     *
     * @param key 属性键
     * @param defValue 默认值
     * @return 属性值或默认值
     */
    public long getLong(Context context, String key, long defValue) {
        if (mGetLongMethod == null) {
            return defValue;
        }

        try {
            return (Long) mGetLongMethod.invoke(null, key, defValue);
        } catch (Exception e) {
            Log.e(TAG, "getLong failed for key: " + key, e);
            return defValue;
        }
    }

    /**
     * 获取字符串类型系统属性
     *
     * @param key 属性键
     * @return 属性值或空字符串
     */
    public String getString(Context context, String key) {
        if (mGetStringMethod == null) {
            return "";
        }

        try {
            return (String) mGetStringMethod.invoke(null, key);
        } catch (Exception e) {
            Log.e(TAG, "getString failed for key: " + key, e);
            return "";
        }
    }

    /**
     * 获取字符串类型系统属性（带默认值）
     *
     * @param key 属性键
     * @param defValue 默认值
     * @return 属性值或默认值
     */
    public String getString(Context context, String key, String defValue) {
        String value = getString(context, key);
        return value.isEmpty() ? defValue : value;
    }
}