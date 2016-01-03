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

import com.letv.mobile.core.log.Logger;
import com.letv.mobile.core.utils.HandlerUtils;
import com.letv.mobile.core.utils.NetworkUtil;

import java.util.Date;

/**
 * TODO(qingxia):
 * 1, Should add network fetch time logic.
 * 2, Listen to the network state.
 * @author xiaqing
 */
final public class TimeProvider {
    private static final String TAG = "TimeProvider";
    private static ReferenceTime sReferenceTime = new ReferenceTime();
    private static RemoteTimeFetcher[] sTimeFetchers = null;
    private static final int[] sRetryTimes = new int[] { 1, 4, 4, 8, 16, 32, 64 };
    private static int sRetryCount = 0;
    private static OnFetchTimeResultListener mFetchTimeResultListener;
    private static final NetworkUtil.OnNetworkChangeListener sNetworkListener = new NetworkUtil.OnNetworkChangeListener() {

        @Override
        public void onNetworkConnected() {
            if (TimeProvider.sReferenceTime != null
                    && !TimeProvider.sReferenceTime.isTimeValidate()) {
                TimeProvider.retryTimeSyn();
            }
        }

        @Override
        public void onNetworkDisconnected() {

        }

    };

    /**
     * 根据CTA送检需求，在用户点击同意用户协议之前不允许有网络请求
     * 这里将开启同步的操作单独抽出一个接口
     */
    public static void startSyncTime() {
        TimeProvider.startTimeSyn();
    }

    static {
        TimeProvider.sTimeFetchers = new RemoteTimeFetcher[] { new HttpHeaderTimeFetcher(),
                new LetvTimeFetcher() };
        // TODO(qingxia): Enable while test.
        // TimeProvider.testFetchTime();
    }

    // We do not want someone new this class.
    private TimeProvider() {
    }

    // This class is used for test.
    static void testFetchTime() {
        HandlerUtils.getWorkingThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (TimeProvider.sReferenceTime != null) {
                    Logger.i(
                            TimeProvider.TAG,
                            "CurrentTime = "
                                    + TimeProvider.sReferenceTime.getAsynTime()
                                    + " Date = "
                                    + new Date(TimeProvider.sReferenceTime.getAsynTime())
                                            .toString());
                }

                TimeProvider.testFetchTime();
            }
        }, 2000);
    }

    /**
     * Get current time.
     * @return
     */
    public synchronized static Date getCurrentTime() {
        return new Date(TimeProvider.getCurrentMillisecondTime());
    }

    /**
     * Get millisecond time.
     * @return
     */
    public synchronized static long getCurrentMillisecondTime() {
        return TimeProvider.sReferenceTime.getCurrentMillisecondTime();
    }

    private static void retryTimeSyn() {
        Logger.i(TimeProvider.TAG, "retryTimeSyn");
        if (TimeProvider.sReferenceTime != null && !TimeProvider.sReferenceTime.isTimeValidate()) {
            Logger.i(TimeProvider.TAG, "retryTimeSyn sRetryCount = " + TimeProvider.sRetryCount);
            TimeProvider.sRetryCount = (TimeProvider.sRetryCount + 1)
                    % TimeProvider.sRetryTimes.length;
            HandlerUtils.getWorkingThreadHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    TimeProvider.startTimeSyn();
                }
            }, TimeProvider.sRetryTimes[TimeProvider.sRetryCount] * 1000);
        }
    }

    private synchronized static void startTimeSyn() {
        if (TimeProvider.sTimeFetchers == null || TimeProvider.sTimeFetchers.length <= 0
                || !NetworkUtil.isNetAvailable()) {
            TimeProvider.sRetryCount = 0;
            return;
        }

        RemoteTimeFetcher timeFetcher = TimeProvider.sTimeFetchers[TimeProvider.sRetryCount
                % TimeProvider.sTimeFetchers.length];

        Logger.i(TimeProvider.TAG, "startTimeSyn timeFetcher is "
                + timeFetcher.getClass().toString());

        timeFetcher.getCurrentTime(new FetchTimeListener() {
            @Override
            public void onFetchTimeResult(TimeErrorCode errorCode, ReferenceTime referenceTime) {
                if (errorCode != null && errorCode == TimeErrorCode.TIME_ERROR_OK) {
                    if (referenceTime != null && referenceTime.isTimeValidate()) {
                        TimeProvider.sRetryCount = 0;
                        TimeProvider.sReferenceTime = referenceTime;
                        Logger.i(
                                TimeProvider.TAG,
                                "sCurrentTime = "
                                        + TimeProvider.sReferenceTime.getCurrentMillisecondTime()
                                        + " Date = "
                                        + new Date(TimeProvider.sReferenceTime
                                                .getCurrentMillisecondTime()).toString());
                        if (mFetchTimeResultListener != null) {
                            mFetchTimeResultListener.onFetchTimeSuccess(TimeProvider.sReferenceTime
                                    .getCurrentMillisecondTime());
                        }
                        return;
                    }
                    if (mFetchTimeResultListener != null) {
                        mFetchTimeResultListener.onFetchTimeFaliure();
                    }
                    Logger.w(TimeProvider.TAG, "startTimeSyn get asyn time error");
                } else {
                    if (mFetchTimeResultListener != null) {
                        mFetchTimeResultListener.onFetchTimeFaliure();
                    }
                    Logger.w(TimeProvider.TAG, "startTimeSyn get asyn time network error");
                }

                TimeProvider.retryTimeSyn();
            }
        });
    }

    public static void setOnFetchTimeResultListener(OnFetchTimeResultListener listener) {
        mFetchTimeResultListener = listener;
    }

    public interface OnFetchTimeResultListener {
        public void onFetchTimeSuccess(long currentTime);

        public void onFetchTimeFaliure();
    }
}
