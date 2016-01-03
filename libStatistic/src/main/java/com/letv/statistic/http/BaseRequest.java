package com.letv.statistic.http;

import com.letv.mobile.async.LetvHttpAsyncRequest;
import com.letv.mobile.http.bean.LetvBaseBean;
import com.letv.mobile.http.builder.LetvHttpBaseUrlBuilder;
import com.letv.mobile.http.parameter.LetvBaseParameter;

import android.content.Context;

/**
 * @author lilong
 *         数据统计用的Request类
 */
public abstract class BaseRequest extends LetvHttpAsyncRequest {

    private int connectTimeOut;// 链接超时时间
    private int readTimeOut;// 读超时时间
    private String domain;
    private String path;

    public BaseRequest(Context context, int connectTimeOut, int readTimeOut, String domain,
            String path) {
        // NOTE(baiwenlong):上报不需要回调
        super(context, null);
        this.connectTimeOut = connectTimeOut;
        this.readTimeOut = readTimeOut;
        this.domain = domain;
        this.path = path;
    }

    @Override
    public LetvBaseBean parseData(String sourceData) throws Exception {
        // Do nothing.
        return null;
    }

    @Override
    protected void onChangeDomainRequestSuccess(LetvHttpBaseUrlBuilder params) {
        // Do nothing.
    }

    @Override
    protected int getTotalRetryCount() {
        // TODO(qingxia): We do not need retry to send pv log.
        return 0;
    }

    @Override
    protected int getReadTimeOut() {
        return this.readTimeOut;
    }

    @Override
    protected int getConnectTimeOut() {
        return this.connectTimeOut;
    }

    @Override
    public LetvHttpBaseUrlBuilder getRequestUrl(LetvBaseParameter params) {
        return new HttpUrlBuilder(this.domain, this.path, params);
    }
}
