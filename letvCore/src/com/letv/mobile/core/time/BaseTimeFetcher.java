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

import com.letv.mobile.core.utils.HandlerUtils;

/**
 * This class provide a base logic for RemoteTimerFetcher
 * @author xiaqing
 */
abstract public class BaseTimeFetcher implements RemoteTimeFetcher {
    // Store network refrence time.
    private ReferenceTime mReferenceTime = null;

    public BaseTimeFetcher() {
        this.mReferenceTime = new ReferenceTime();
    }

    /**
     * This callback is a common callback, it can be override by subclass
     * @param listener
     */
    protected void callListener(final FetchTimeListener listener) {
        if (listener == null) {
            return;
        }

        HandlerUtils.getUiThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                if (BaseTimeFetcher.this.mReferenceTime == null) {
                    BaseTimeFetcher.this.mReferenceTime = new ReferenceTime();
                }

                if (BaseTimeFetcher.this.mReferenceTime.isTimeValidate()) {
                    listener.onFetchTimeResult(TimeErrorCode.TIME_ERROR_OK,
                            BaseTimeFetcher.this.mReferenceTime);
                } else {
                    listener.onFetchTimeResult(
                            TimeErrorCode.TIME_ERROR_FETCH_ERROR,
                            BaseTimeFetcher.this.mReferenceTime);
                }
            }
        });
    }

    protected ReferenceTime getReferenceTime() {
        return this.mReferenceTime;
    }

    protected void setReferenceTime(ReferenceTime mReferenceTime) {
        this.mReferenceTime = mReferenceTime;
    }
}
