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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import com.letv.mobile.core.utils.HandlerUtils;
import com.letv.mobile.core.utils.IOUtils;
import com.letv.mobile.core.utils.StringUtils;

public class LetvTimeFetcher extends BaseTimeFetcher {
    private static final int TIME_FETCH_TIMEOUT = 3000;
    private static final int MAX_NETWORK_INTERVAL = 5000;
    private static final String TIME_HOST = "http://d.itv.letv.com/mobile/message/walltime.json";

    private static final String JSON_NAME_STATUS = "status";
    private static final String JSON_NAME_DATA = "data";
    private static final String JSON_NAME_TIME = "time";

    private static final int JSON_STATUS_OK = 1;

    @Override
    public void getCurrentTime(final FetchTimeListener listener) {
        HandlerUtils.getWorkingThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                LetvTimeFetcher.this.startHttpGet();
                LetvTimeFetcher.this.callListener(listener);
            }
        });
    }

    private long parseData(String data) {
        if (StringUtils.isStringEmpty(data)) {
            return ReferenceTime.INVALID_TIME;
        }

        long time = ReferenceTime.INVALID_TIME;
        try {
            JSONObject ob = new JSONObject(data);
            int status = ob.getInt(LetvTimeFetcher.JSON_NAME_STATUS);
            if (status == LetvTimeFetcher.JSON_STATUS_OK) {
                JSONObject data1 = ob
                        .optJSONObject(LetvTimeFetcher.JSON_NAME_DATA);
                if (data1 != null) {
                    time = data1.optLong(LetvTimeFetcher.JSON_NAME_TIME,
                            ReferenceTime.INVALID_TIME);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ReferenceTime.INVALID_TIME;
        }

        return time;
    }

    private void startHttpGet() {
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(LetvTimeFetcher.TIME_HOST);

            // get current time and write it to the request packet
            long requestTicks = ReferenceTime.getSystemElapsedTime();

            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(LetvTimeFetcher.TIME_FETCH_TIMEOUT);
            conn.setReadTimeout(LetvTimeFetcher.TIME_FETCH_TIMEOUT);
            conn.setDoInput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Connection", "Close");
            conn.setRequestProperty("Content-Type", "text/html");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("contentType", "utf-8");
            inputStream = conn.getInputStream();
            long responseTicks = ReferenceTime.getSystemElapsedTime();
            byte[] buffer;
            if (conn.getResponseCode() == 200) {
                buffer = new byte[1024];
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                buffer = out.toByteArray();

                String data = new String(buffer);

                // Get the network interval.
                long interval = responseTicks - requestTicks;

                // If getting data duration is bigger than MAX_NETWORK_INTERVAL,
                // the result is invalidate.
                if (interval > LetvTimeFetcher.MAX_NETWORK_INTERVAL) {
                    return;
                }

                long currentTime = this.parseData(data);
                if (currentTime != ReferenceTime.INVALID_TIME) {
                    this.getReferenceTime().setCurrentTime(
                            currentTime + interval / 2);
                    this.getReferenceTime().setRefrenceTime(responseTicks);
                }
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

}
