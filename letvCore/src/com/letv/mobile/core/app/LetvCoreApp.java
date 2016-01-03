package com.letv.mobile.core.app;

import android.app.Application;

import com.letv.mobile.core.BaseLibrary;
import com.letv.mobile.core.common.GlobalPoolManager;
import com.letv.mobile.core.utils.ContextProvider;

public class LetvCoreApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        ContextProvider.initIfNotInited(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        exitApp();
    }

    private static LetvCoreApp sApp = null;
    synchronized public static LetvCoreApp getApplication() {
        if (sApp == null) {
            throw new NullPointerException("sApp is null!");
        }
        return sApp;
    }
    /**
     * We should call this function first here.
     * @param appName
     */
    protected void init(String appName) {
        BaseLibrary.init(this, appName);
    }

    /**
     * If you want to exit app, call this method.
     **/
    public static void exitApp() {
        GlobalPoolManager.shutDownPool();
    }
}
