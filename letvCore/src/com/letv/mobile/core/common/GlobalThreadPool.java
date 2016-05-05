package com.letv.mobile.core.common;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * global thread
 */
public class GlobalThreadPool {

    private static final int SIZE = 3;
    private static final int MAX_SIZE = 5;
    private static ThreadPoolExecutor mPool;
    private static Executor mCachedPool;

    public static ThreadPoolExecutor getGlobalThreadPoolInstance() {
        if (mPool == null) {
            synchronized (GlobalThreadPool.class) {
                if (mPool == null) {
                    mPool = new ThreadPoolExecutor(SIZE, MAX_SIZE, 3, TimeUnit.SECONDS,
                            new LinkedBlockingQueue<Runnable>(), new NamedDefaultThreadFactory(
                                    GlobalThreadPool.class.getName()));
                }
            }
        }
        return mPool;
    }

    public static Executor getGlobalCachedPool() {
        if (mCachedPool == null) {
            synchronized (GlobalJsonThreadPool.class) {
                if (mCachedPool == null) {
                    mCachedPool = Executors.newCachedThreadPool(new NamedDefaultThreadFactory(
                            GlobalThreadPool.class.getName()));
                }
            }
        }
        return mCachedPool;
    }

    /**
     * run a thead ,== new thread
     */
    public static void startRunInThread(Runnable doSthRunnable) {
        getGlobalThreadPoolInstance().execute(doSthRunnable);
    }

    public static void startSingleThread(Runnable runnable) {
        new Thread(runnable, GlobalThreadPool.class.getName()).start();
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
