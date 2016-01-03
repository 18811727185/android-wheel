/**
 *
 * Copyright 2013 LeTV Technology Co. Ltd., Inc. All rights reserved.
 *
 * @Author : qingxia
 *
 * @Description :
 *
 */

package com.letv.mobile.core;

import android.content.Context;

import com.letv.mobile.core.config.LeTVConfig;
import com.letv.mobile.core.imagecache.LetvCacheMannager;
import com.letv.mobile.core.reportlog.ReportManager;
import com.letv.mobile.core.reportlog.collector.ReportData;
import com.letv.mobile.core.reportlog.sender.ReportSender;
import com.letv.mobile.core.time.TimeProvider;
import com.letv.mobile.core.utils.ContextProvider;
import com.letv.mobile.core.utils.NetworkUtil;
import com.letv.mobile.core.utils.SharedPreferencesManager;

/**
 * @author qingxia
 * @description: This class provide a unique interface to initialize base
 *               library.
 */
final public class BaseLibrary {
    public static void init(Context context, String appName) {
        // Initialize global configurations
        LeTVConfig.init(appName);
        // Initialize crash handler.
        ReportManager.getInstance().init(
                ContextProvider.getApplicationContext(), new ReportSender() {
                    @Override
                    public void send(ReportData errorContent) {
                        // TODO(qingxia): We do not send log now.
                    }
                });
        // Create global share preferences manager
        SharedPreferencesManager.createInstance(context, appName);
        // Initialize network utils
        NetworkUtil.init();
        // Initialize image cache.
        LetvCacheMannager.getInstance().init(
                ContextProvider.getApplicationContext());

        // Init time provider.
        TimeProvider.getCurrentMillisecondTime();
    }

    // We do not want this class can be new.
    private BaseLibrary() {
    }
}
