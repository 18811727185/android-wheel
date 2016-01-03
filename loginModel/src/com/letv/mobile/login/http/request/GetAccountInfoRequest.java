/**
 *
 * Copyright 2015 LeTV Technology Co. Ltd., Inc. All rights reserved.
 *
 * @Author : qingxia
 *
 * @Description :
 *
 */

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
import com.letv.mobile.login.model.MemberInfo;

/**
 * A common request example is
 * :http://10.154.156.205/mobile/vip/getAccount.json?uid={uid}&token={token}
 * @author xiaqing
 */
public class GetAccountInfoRequest extends LetvHttpDynamicRequest<MemberInfo> {

    public GetAccountInfoRequest(Context context, TaskCallBack callback) {
        super(context, callback);
    }

    @Override
    public LetvHttpBaseUrlBuilder getRequestUrl(LetvBaseParameter params) {
        DynamicUrlBuilder urlBuilder = new DynamicUrlBuilder(
                LoginHttpContants.GET_USER_ACCOUNT_INFO, params);
        // CriticalPathLogUtil.log(CriticalPathEnum4Login.LoginProcess,
        // "getUserAccount url:" + urlBuilder.buildUrl());
        return urlBuilder;
    }

    @Override
    public LetvBaseBean<MemberInfo> parse(String sourceData) throws Exception {
        CommonResponse<MemberInfo> data = JSON.parseObject(sourceData,
                new TypeReference<CommonResponse<MemberInfo>>() {
                });
        return data;
    }

}
