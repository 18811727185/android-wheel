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
 * @deprecated
 * @author xiaqing
 */
@Deprecated
public class SntpTimeFetcher implements RemoteTimeFetcher {
    private static final int SNTP_FETCH_TIMEOUT = 3000;
    private static final String SNTP_HOST = "pool.ntp.org";
    private static final SntpClient sClient = new SntpClient();

    @Override
    public void getCurrentTime(final FetchTimeListener listener) {
        // TODO(qingxia): Maybe add later.
        // if (SntpTimeFetcher.sClient.isFetchedTime()) {
        // this.callListener(listener);
        // return;
        // }

        HandlerUtils.getWorkingThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                SntpTimeFetcher.sClient.requestTime(SntpTimeFetcher.SNTP_HOST,
                        SntpTimeFetcher.SNTP_FETCH_TIMEOUT);

                SntpTimeFetcher.this.callListener(listener);
            }
        });
    }

    private void callListener(final FetchTimeListener listener) {
        if (listener == null) {
            return;
        }

        HandlerUtils.getUiThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                if (SntpTimeFetcher.sClient.isFetchedTime()) {
                    listener.onFetchTimeResult(
                            TimeErrorCode.TIME_ERROR_OK,
                            new ReferenceTime(SntpTimeFetcher.sClient
                                    .getCurrentTime(), SntpTimeFetcher.sClient
                                    .getNtpTimeReference()));
                } else {
                    listener.onFetchTimeResult(
                            TimeErrorCode.TIME_ERROR_FETCH_ERROR,
                            new ReferenceTime(SntpTimeFetcher.sClient
                                    .getCurrentTime(), SntpTimeFetcher.sClient
                                    .getNtpTimeReference()));
                }
            }
        });
    }
}
