package com.letv.mobile.core.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.content.Context;

import com.letv.mobile.core.log.Logger;

/**
 * Copyright 2012 LETV, Inc. All rights reserved.
 * @Author : xiaqing
 * @Description : SharedPreferencesManager is used to manager write and read
 *              SharedPreferences value,
 */

public class SharedPreferencesManager {
    private static final Logger sLogger = new Logger("SharedPreferencesManager");
    private static Context sContext = null;
    private static SharedPreferencesManager sInstance = null;
    private static String sDefaultName = "com_letv_tv_preferences";

    public static void createInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SharedPreferencesManager(context);
            sLogger.d("createInstance()");
        }
    }

    public static void createInstance(Context context, String defaultName) {
        if (sInstance == null) {
            sInstance = new SharedPreferencesManager(context, defaultName);
            sLogger.d("createInstance()");
        }
    }

    private SharedPreferencesManager(Context context) {
        this(context, sDefaultName);
    }

    private SharedPreferencesManager(Context context, String defaultName) {
        sContext = context;
        sDefaultName = defaultName;
    }

    /**
     * 获取序列化保存的配置类
     * @param key
     * @param 默认值
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T getSerializable(String key,
            T defValue) {

        String valueString = SharedPreferencesManager.getString(key, key,
                defValue.toString());
        if (valueString == null || valueString.length() == 0) {
            saveSerializable(key, defValue);
            return defValue;
        }
        try {
            Object resultObject = fromString(valueString);
            return (T) resultObject;
        } catch (IOException e) {
            sLogger.d("getSerializable: IOException " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            sLogger.d("getSerializable: ClassNotFoundException "
                    + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            sLogger.d("getSerializable: Exception " + e.getMessage());
            e.printStackTrace();
        }
        return defValue;
    }

    /**
     * 序列化保存配置类
     * @param key
     * @param 当前值
     * @return
     */
    public static <T extends Serializable> boolean saveSerializable(String key,
            T value) {
        if (value == null) {
            return false;
        }
        try {
            String valueString = toString(value);
            SharedPreferencesManager.putString(key, key, valueString);
        } catch (IOException e) {
            sLogger.d("saveSerializable: IOException " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean getBoolean(String key, boolean defValue) {
        return getBoolean(getDefaultSharedPreferencesName(), key, defValue);
    }

    public static String getString(String key, String defValue) {
        return getString(getDefaultSharedPreferencesName(), key, defValue);
    }

    public static int getInt(String key, int defValue) {
        return getInt(getDefaultSharedPreferencesName(), key, defValue);
    }

    public static boolean putInt(String key, int value) {
        return putInt(getDefaultSharedPreferencesName(), key, value);
    }

    public static long getLong(String key, long defValue) {
        return getLong(getDefaultSharedPreferencesName(), key, defValue);
    }

    public static boolean putLong(String key, long value) {
        return putLong(getDefaultSharedPreferencesName(), key, value);
    }

    public static boolean putBoolean(String key, boolean value) {
        return putBoolean(getDefaultSharedPreferencesName(), key, value);
    }

    public static boolean putString(String key, String value) {
        return putString(getDefaultSharedPreferencesName(), key, value);
    }

    public static boolean getBoolean(String name, String key, boolean defValue) {
        sLogger.d("getBoolean() : name = " + name + " : key = " + key
                + " : defValue = " + defValue);
        if (sInstance != null && sContext != null) {
            return sContext.getSharedPreferences(name, Context.MODE_PRIVATE)
                    .getBoolean(key, defValue);
        }
        return defValue;
    }

    public static String getString(String name, String key, String defValue) {
        sLogger.d("getString() : name = " + name + " : key = " + key
                + " : defValue = " + defValue);
        if (sInstance != null && sContext != null) {
            return sContext.getSharedPreferences(name, Context.MODE_PRIVATE)
                    .getString(key, defValue);
        }
        return defValue;
    }

    public static int getInt(String name, String key, int defValue) {
        sLogger.d("getInt() : name = " + name + " : key = " + key
                + " : defValue = " + defValue);
        if (sInstance != null && sContext != null) {
            return sContext.getSharedPreferences(name, Context.MODE_PRIVATE)
                    .getInt(key, defValue);
        }
        return defValue;
    }

    public static boolean putInt(String name, String key, int value) {
        sLogger.d("putInt() : name = " + name + " : key = " + key
                + " : value = " + value);
        if (sInstance != null && sContext != null) {
            sContext.getSharedPreferences(name, Context.MODE_PRIVATE).edit()
                    .putInt(key, value).apply();
            return true;
        }
        return false;
    }

    public static long getLong(String name, String key, long defValue) {
        sLogger.d("getLong() : name = " + name + " : key = " + key
                + " : defValue = " + defValue);
        if (sInstance != null && sContext != null) {
            return sContext.getSharedPreferences(name, Context.MODE_PRIVATE)
                    .getLong(key, defValue);
        }
        return defValue;
    }

    public static boolean putLong(String name, String key, long value) {
        sLogger.d("putLong() : name = " + name + " : key = " + key
                + " : value = " + value);
        if (sInstance != null && sContext != null) {
            sContext.getSharedPreferences(name, Context.MODE_PRIVATE).edit()
                    .putLong(key, value).apply();
            return true;
        }
        return false;
    }

    public static boolean putBoolean(String name, String key, boolean value) {
        sLogger.d("putBoolean() : name = " + name + " : key = " + key
                + " : defValue = " + value);
        if (sInstance != null && sContext != null) {
            sContext.getSharedPreferences(name, Context.MODE_PRIVATE).edit()
                    .putBoolean(key, value).apply();
            return true;
        }
        return false;
    }

    public static boolean putString(String name, String key, String value) {
        sLogger.d("putString() : name = " + name + " : key = " + key
                + " : defValue = " + value);
        if (sInstance != null && sContext != null) {
            sContext.getSharedPreferences(name, Context.MODE_PRIVATE).edit()
                    .putString(key, value).apply();
            return true;
        }
        return false;
    }

    /**
     * Read the object from Base64 string.
     */
    private static Object fromString(String s) throws IOException,
            ClassNotFoundException {
        byte[] data = Base64.decode(s, Base64.DEFAULT);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
                data));
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    /**
     * Write the object to a Base64 string.
     */
    private static String toString(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
    }

    private static String getDefaultSharedPreferencesName() {
        return sDefaultName;
    }
}