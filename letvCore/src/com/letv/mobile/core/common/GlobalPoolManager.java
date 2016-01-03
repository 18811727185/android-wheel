package com.letv.mobile.core.common;

public class GlobalPoolManager {

    public static void shutDownPool() {
        GlobalJsonThreadPool.shutdownPool();
    }

}
