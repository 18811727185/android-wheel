package com.letv.mobile.pay.http.request;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.letv.mobile.async.TaskCallBack;
import com.letv.mobile.http.bean.CommonResponse;
import com.letv.mobile.http.bean.LetvBaseBean;
import com.letv.mobile.http.builder.DynamicUrlBuilder;
import com.letv.mobile.http.builder.LetvHttpBaseUrlBuilder;
import com.letv.mobile.http.parameter.LetvBaseParameter;
import com.letv.mobile.http.request.LetvHttpDynamicRequest;
import com.letv.mobile.pay.http.HttpConstants;
import com.letv.mobile.pay.http.model.CheckOrderModel;

/**
 * @author shibin
 */
public class CheckOrderRequest extends LetvHttpDynamicRequest<CheckOrderModel> {

    public CheckOrderRequest(Context context, TaskCallBack callback) {
        super(context, callback);
    }

    @Override
    public LetvHttpBaseUrlBuilder getRequestUrl(LetvBaseParameter params) {
        return new DynamicUrlBuilder(HttpConstants.CHECK_ORDER, params);
    }

    @Override
    protected LetvBaseBean<CheckOrderModel> parse(String sourceData)
            throws Exception {
        CommonResponse<CheckOrderModel> response = JSON.parseObject(sourceData,
                new TypeReference<CommonResponse<CheckOrderModel>>() {
                });
        return response;
    }

}
