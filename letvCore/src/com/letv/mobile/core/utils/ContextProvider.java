/**
 * Copyright 2012 LeTV Technology Co. Ltd., Inc. All rights reserved.
 *
 * @Author : qingxia
 * @Description :
 */

package com.letv.mobile.core.utils;

import android.content.Context;

/**
 * This class provide a global application context.
 * @author qingxia
 */
public final class ContextProvider {
    private static Context sContext = null;
    private static Context sHostAppContext = null;

    public static void initIfNotInited(Context context) {
        if (sContext == null) {
            init(context);
        }
    }

    /**
     * NOTE(qingxia): This function should be invoked in Application while the
     * application is been
     * created.
     * @param context
     */
    public static void init(Context context) {
        if (context == null) {
            throw new NullPointerException(
                    "Can not use null initialized application context");
        }
        sContext = context;
    }

    /**
     * 初始化宿主应用的context
     * @param context 宿主context
     */
    public static void initHostAppContext (Context context) {
        if (context == null) {
            throw new NullPointerException(
                    "Can not use null initialized application context");
        }
        sHostAppContext = context;
    }

    /**
     * Get application context.
     * @return null 如果没有调用init()来初始化Context
     */
    public static Context getApplicationContext() {
        return sContext;
    }

    /**
     * 获取宿主应用的context
     * @return null 如果没有调用initHostAppContext()来初始化Context
     */
    public static Context getHostAppContext() {
        return sHostAppContext;
    }

    private ContextProvider() {
    }
}
