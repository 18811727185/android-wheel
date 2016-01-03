package com.letv.statistic.http;

import android.content.Context;

/**
 * @author lilong
 *         登录上报请求
 */
public class LoginRequest extends BaseRequest {

    public LoginRequest(Context context, int connectTimeOut, int readTimeOut, String domain,
            String path) {
        super(context, connectTimeOut, readTimeOut, domain, path);
    }
}
