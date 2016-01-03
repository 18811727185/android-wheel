package com.letv.statistic.http;

import android.content.Context;

/**
 * @author lilong
 *         播放上报请求
 */
public class PlayRequest extends BaseRequest {

    public PlayRequest(Context context, int connectTimeOut, int readTimeOut, String domain,
            String path) {
        super(context, connectTimeOut, readTimeOut, domain, path);
    }
}
