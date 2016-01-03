/**
 *
 * Copyright 2015 LeTV Technology Co. Ltd., Inc. All rights reserved.
 *
 * @Author : qingxia
 *
 * @Description :
 *
 */

package com.letv.mobile.login.http.parameter;

import com.letv.mobile.core.utils.DeviceUtils;
import com.letv.mobile.http.parameter.LetvBaseParameter;
import com.letv.mobile.loginModel.LoginModel;

public class GetAccountInfoParameter extends LoginHttpCommonParameter {

    private static final long serialVersionUID = -2209004387355937635L;

    private static final String USERNAME = "username";
    private static final String UID = "uid";
    private static final String TOKEN = "token";
    private static final String DEVICE_KEY = "deviceKey"; // 机卡绑定时长必须参数
    private String mUid = "";
    private String mToken = "";

    private LetvBaseParameter mParameter;

    public GetAccountInfoParameter(String uid, String token) {
        super();
        this.mUid = uid;
        this.mToken = token;
    }

    @Override
    public LetvBaseParameter combineParams() {
        this.mParameter = super.combineParams();
        this.mParameter.put(UID, this.mUid);
        this.mParameter.put(TOKEN, this.mToken);
        if (LoginModel.isLogin()) {
            this.mParameter.put(USERNAME, LoginModel.getUserInfo()
                    .getUsername());
        }
        this.mParameter.put(DEVICE_KEY, DeviceUtils.getDeviceKey());
        return this.mParameter;
    }

}
