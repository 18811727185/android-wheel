package com.letv.mobile.login.http.request;

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
import com.letv.mobile.login.http.LoginHttpContants;
import com.letv.mobile.login.model.ReceiveDeviceBindInfo;

/**
 * A common request example is :
 * http://api.m.letv.com/mobile/vip/devicebind/receiveDeviceBind.json?type={type
 * }&
 * deviceKey={deviceKey}&devId={devId}&uid={uid}
 * @author shibin
 */
public class ReceiveDeviceBindRequest extends
        LetvHttpDynamicRequest<ReceiveDeviceBindInfo> {

    public ReceiveDeviceBindRequest(Context context, TaskCallBack callback) {
        super(context, callback);
    }

    @Override
    public LetvHttpBaseUrlBuilder getRequestUrl(LetvBaseParameter params) {
        DynamicUrlBuilder urlBuilder = new DynamicUrlBuilder(
                LoginHttpContants.RECEIVE_DEVICE_BIND, params);
        return urlBuilder;
    }

    @Override
    public LetvBaseBean<ReceiveDeviceBindInfo> parse(String sourceData)
            throws Exception {
        CommonResponse<ReceiveDeviceBindInfo> data = JSON.parseObject(
                sourceData,
                new TypeReference<CommonResponse<ReceiveDeviceBindInfo>>() {
                });
        return data;
    }
}
