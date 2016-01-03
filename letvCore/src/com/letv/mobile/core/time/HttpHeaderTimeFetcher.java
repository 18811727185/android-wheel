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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.letv.mobile.core.utils.HandlerUtils;
import com.letv.mobile.core.utils.IOUtils;
import com.letv.mobile.core.utils.StringUtils;

public class HttpHeaderTimeFetcher extends BaseTimeFetcher {
    private static final int TIME_FETCH_TIMEOUT = 3000;
    private static final int MAX_NETWORK_INTERVAL = 5000;
    private static final String TIME_HOST = "http://m.baidu.com";

    private String mTimeHttpHost = HttpHeaderTimeFetcher.TIME_HOST;

    private static final String HTTP_RES_HEADER_DATE = "Date";

    public HttpHeaderTimeFetcher() {
        super();
    }

    public HttpHeaderTimeFetcher(String httpHost) {
        super();
        this.setTimeHttpHost(httpHost);
    }

    @Override
    public void getCurrentTime(final FetchTimeListener listener) {
        HandlerUtils.getWorkingThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                HttpHeaderTimeFetcher.this.startHttpGet();
                HttpHeaderTimeFetcher.this.callListener(listener);
            }
        });
    }

    private void startHttpGet() {
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(this.getTimeHttpHost());

            // get current time and write it to the request packet
            long requestTicks = ReferenceTime.getSystemElapsedTime();

            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(HttpHeaderTimeFetcher.TIME_FETCH_TIMEOUT);
            conn.setReadTimeout(HttpHeaderTimeFetcher.TIME_FETCH_TIMEOUT);
            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Connection", "Close");
            conn.setRequestProperty("Content-Type", "text/html");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("contentType", "utf-8");
            inputStream = conn.getInputStream();
            long responseTicks = ReferenceTime.getSystemElapsedTime();
            long currentTime = ReferenceTime.INVALID_TIME;
            if (conn.getResponseCode() == 200) {
                currentTime = conn.getHeaderFieldDate(
                        HttpHeaderTimeFetcher.HTTP_RES_HEADER_DATE,
                        ReferenceTime.INVALID_TIME);
            }

            // Get the network interval.
            long interval = responseTicks - requestTicks;

            // If getting data duration is bigger than MAX_NETWORK_INTERVAL,
            // the result is invalidate.
            if (interval > HttpHeaderTimeFetcher.MAX_NETWORK_INTERVAL) {
                return;
            }

            if (currentTime != ReferenceTime.INVALID_TIME) {
                this.getReferenceTime().setCurrentTime(
                        currentTime + interval / 2);
                this.getReferenceTime().setRefrenceTime(responseTicks);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeStream(inputStream);
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public String getTimeHttpHost() {
        return this.mTimeHttpHost;
    }

    public void setTimeHttpHost(String timeHttpHost) {
        if (StringUtils.isStringEmpty(timeHttpHost)) {
            return;
        }
        this.mTimeHttpHost = timeHttpHost;
    }
}
