/**
 *
 * Copyright 2012 LeTV Technology Co. Ltd., Inc. All rights reserved.
 *
 * @Author : qingxia
 *
 * @Description :
 *
 */

package com.letv.mobile.core.utils;

/**
 * This class support a new Runnable that allow user to call with T parameter.
 * @author qingxia
 */
public abstract class AbstractMessageRunnable<T> implements Runnable {
    protected final T mData;

    public AbstractMessageRunnable(T data) {
        this.mData = data;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public abstract void run();

}
