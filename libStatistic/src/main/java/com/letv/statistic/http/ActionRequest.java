package com.letv.statistic.http;

import android.content.Context;

/**
 * @author lilong
 *         action上报请求
 */
public class ActionRequest extends BaseRequest {

    public ActionRequest(Context context, int connectTimeOut, int readTimeOut, String domain,
            String path) {
        super(context, connectTimeOut, readTimeOut, domain, path);
    }
}
