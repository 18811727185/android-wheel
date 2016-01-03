/**
 *
 * Copyright 2013 LeTV Technology Co. Ltd., Inc. All rights reserved.
 *
 * @Author : qingxia
 *
 * @Description :
 *
 */

package com.letv.mobile.core.time;

import android.os.SystemClock;

public class ReferenceTime {
    public static final long INVALID_TIME = -1;

    // This variable stored the current time given by fetcher
    private long mCurrentTime = ReferenceTime.INVALID_TIME;
    // This variable stored the current time given by
    private long mRefrenceTime = ReferenceTime.INVALID_TIME;

    public ReferenceTime() {
    }

    public ReferenceTime(long currentTime, long refrenceTime) {
        this.mCurrentTime = currentTime;
        this.mRefrenceTime = refrenceTime;
    }

    public synchronized long getCurrentTime() {
        return this.mCurrentTime;
    }

    public synchronized void setCurrentTime(long currentTime) {
        this.mCurrentTime = currentTime;
    }

    public synchronized long getRefrenceTime() {
        return this.mRefrenceTime;
    }

    public synchronized void setRefrenceTime(long refrenceTime) {
        this.mRefrenceTime = refrenceTime;
    }

    protected synchronized boolean isTimeValidate() {
        return this.mCurrentTime != ReferenceTime.INVALID_TIME
                && this.mRefrenceTime != ReferenceTime.INVALID_TIME;
    }

    /**
     * TODO(qingxia): Edit later.
     * @return
     */
    public long getAsynTime() {
        return this.isTimeValidate() ? this.getCurrentTime()
                + ReferenceTime.getSystemElapsedTime() - this.getRefrenceTime()
                : 0;
    }

    /**
     * Get system clock elapsed real time.
     * @return
     */
    public static long getSystemElapsedTime() {
        return SystemClock.elapsedRealtime();
    }

    /**
     * Get millisecond time.
     * @return
     */
    public long getCurrentMillisecondTime() {
        return this.isTimeValidate() ? this.getAsynTime() : System
                .currentTimeMillis();
    }
}
