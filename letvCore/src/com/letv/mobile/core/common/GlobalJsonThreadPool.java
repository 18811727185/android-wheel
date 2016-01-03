package com.letv.mobile.core.common;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * global thread
 */
public class GlobalJsonThreadPool {

    private static final int SIZE = 3;
    private static final int MAX_SIZE = 5;
    private static ThreadPoolExecutor mPool;

    public static ThreadPoolExecutor getGlobalThreadPoolInstance() {
        if (mPool == null) {
            synchronized (GlobalJsonThreadPool.class) {
                if (mPool == null) {
                    mPool = new ThreadPoolExecutor(SIZE, MAX_SIZE, 3,
                            TimeUnit.SECONDS,
                            new LinkedBlockingQueue<Runnable>());
                }
            }
        }
        return mPool;
    }

    /**
     * run a thead ,== new thread
     */
    public static void startRunInThread(Runnable doSthRunnable) {
        getGlobalThreadPoolInstance().execute(doSthRunnable);
    }

    public static void startSingleThread(Runnable runnable) {
        new Thread(runnable).start();
    }

    /**
     * shut down
     */
    public static void shutdownPool() {
        if (mPool != null) {
            mPool.shutdown();
            mPool = null;
        }
    }
}
