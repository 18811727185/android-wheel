/**
 *
 * Copyright 2015 LeTV Technology Co. Ltd., Inc. All rights reserved.
 *
 * @Author : qingxia
 *
 * @Description :
 *
 */

package com.letv.mobile.login.model;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public class LoginConstants {
    private static final String TAG = "LoginConstants";
    private static Handler sUiHandler = new Handler(Looper.getMainLooper());

    public static final String LOGIN_NET_ERROR_TOKEN_INVALIDATE = "0001";
    public static final String LOGIN_NET_ERROR_DATA_INVALIDATE = "10001";

    public static final String FROM = "from";
    public static final String LETV_LEADING = "LetvLeading";
    public static final String CONTENT_ACCOUNT_USER_INFO_URI = "content://com.letv.account.userinfo/com.letv";
    public static final String LOGIN_NAME = "login_name";
    public static final String NICK_NAME = "nick_name";
    public static final String BEAN = "bean";
    public static final String TOKEN = "token";
    // Working thread.
    private static HandlerThread mWorkingThread = new HandlerThread(TAG);
    private static Handler sHandler = null;

    static {
        // Initialize working thread handler
        mWorkingThread.start();
        sHandler = new Handler(mWorkingThread.getLooper());
    }

    /**
     * Get ui thread Handler.
     * @return
     */
    public static Handler getUiThreadHandler() {
        return sUiHandler;
    }

    /**
     * Get working thread Handler.
     * @return
     */
    public static Handler getWorkingThreadHandler() {
        return sHandler;
    }
}
