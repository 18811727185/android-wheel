package com.letv.statistic.http;

import android.content.Context;

/**
 * @author lilong
 *         env上报请求
 */
public class EnvRequest extends BaseRequest {

    public EnvRequest(Context context, int connectTimeOut, int readTimeOut, String domain,
            String path) {
        super(context, connectTimeOut, readTimeOut, domain, path);
    }
}
