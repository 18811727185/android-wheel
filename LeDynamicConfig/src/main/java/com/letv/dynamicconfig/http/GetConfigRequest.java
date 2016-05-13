package com.letv.dynamicconfig.http;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.letv.mobile.async.TaskCallBack;
import com.letv.mobile.http.bean.CommonResponse;
import com.letv.mobile.http.bean.LetvBaseBean;
import com.letv.mobile.http.builder.LetvHttpBaseUrlBuilder;
import com.letv.mobile.http.builder.StaticUrlBuilder;
import com.letv.mobile.http.parameter.LetvBaseParameter;
import com.letv.mobile.http.request.LetvHttpStaticRequest;

/**
 * Created by shibin on 16/4/22.
 */
public class GetConfigRequest extends LetvHttpStaticRequest<GetConfigResponse> {

    // TODO need change to dynamic request and define url other place
    private final String URL = "iptv/api/new/terminal/config.json";

    public GetConfigRequest(Context context, TaskCallBack callback) {
        super(context, callback);
    }

    @Override
    public LetvHttpBaseUrlBuilder getRequestUrl(LetvBaseParameter params) {
        return new StaticUrlBuilder(URL, params);
    }

    @Override
    protected LetvBaseBean<GetConfigResponse> parse(String sourceData)
            throws Exception {
        CommonResponse<GetConfigResponse> data;
        data = JSON.parseObject(sourceData,
                new TypeReference<CommonResponse<GetConfigResponse>>() {
                });
        return data;
    }

}
