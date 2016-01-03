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
import com.letv.mobile.pay.http.model.PurchaseOrderModel;

/**
 * @author shibin
 */
public class PurchaseOrderRequest extends
        LetvHttpDynamicRequest<PurchaseOrderModel> {

    public PurchaseOrderRequest(Context context, TaskCallBack callback) {
        super(context, callback);
    }

    @Override
    public LetvHttpBaseUrlBuilder getRequestUrl(LetvBaseParameter params) {
        return new DynamicUrlBuilder(HttpConstants.PURCHASE_ORDER, params);
    }

    @Override
    protected LetvBaseBean<PurchaseOrderModel> parse(String sourceData)
            throws Exception {
        CommonResponse<PurchaseOrderModel> response = JSON.parseObject(
                sourceData,
                new TypeReference<CommonResponse<PurchaseOrderModel>>() {
                });
        return response;
    }

}
