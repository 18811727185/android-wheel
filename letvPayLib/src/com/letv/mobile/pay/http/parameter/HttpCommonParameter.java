/**
 *
 * Copyright 2015 LeTV Technology Co. Ltd., Inc. All rights reserved.
 *
 * @Author : qingxia
 *
 * @Description :
 *
 */

package com.letv.mobile.pay.http.parameter;

import com.letv.mobile.core.utils.ContextProvider;
import com.letv.mobile.core.utils.DeviceUtils;
import com.letv.mobile.core.utils.SystemUtil;
import com.letv.mobile.http.parameter.LetvBaseParameter;
import com.letv.mobile.loginModel.LoginModel;
import com.letv.mobile.pay.http.HttpConstants;

public class HttpCommonParameter extends LetvBaseParameter {

    private static final long serialVersionUID = -6696431583924895751L;
    private static final String COMMON_KEY_PCODE = "pcode";// 产品编码
    private static final String COMMON_KEY_DEV_ID = "devId";// 客户端设备唯一id
    private static final String COMMON_KEY_TERMINAL_SERIES = "terminalSeries";// 设备型号
    private static final String COMMON_KEY_APP_VERSION = "appVersion";// 版本号
    private static final String COMMON_KEY_UID = "uid";// 用户ID
    private static final String COMMON_KEY_TOKEN = "token";// 用户ID
    private static final String COMMON_KEY_BS_CHANNEL = "bsChannel";// 渠道号
    private static final String COMMON_KEY_TERMINAL_APPLICATION = "terminalApplication";// 应用
    private static final String COMMON_KEY_TERMINAL_BRAND = "terminalBrand";
    private static final String COMMON_KEY_LANGCODE = "langcode";
    private static final String COMMON_KEY_WCODE = "wcode";
    private static final String COMMON_KEY_MAC = "mac";

    // 延用之前的pcode
    private static final String PCODE = "160110000";

    @Override
    public LetvBaseParameter combineParams() {
        if (LoginModel.isLogin()) {
            this.put(COMMON_KEY_UID, LoginModel.getUID());
            this.put(COMMON_KEY_TOKEN, LoginModel.getToken());
        }
        this.put(COMMON_KEY_PCODE, PCODE);
        this.put(COMMON_KEY_DEV_ID, DeviceUtils.getDeviceId());
        this.put(COMMON_KEY_TERMINAL_SERIES, DeviceUtils.getTerminalSeries());
        this.put(COMMON_KEY_BS_CHANNEL, HttpConstants.BS_CHANNEL);
        this.put(COMMON_KEY_APP_VERSION, SystemUtil
                .getVersionName(ContextProvider.getApplicationContext()));
        this.put(COMMON_KEY_TERMINAL_APPLICATION,
                HttpConstants.TERMINAL_APPLICATION);
        this.put(COMMON_KEY_TERMINAL_BRAND, HttpConstants.TERMINAL_BRAND);
        this.put(COMMON_KEY_LANGCODE, HttpConstants.LANGCODE);
        this.put(COMMON_KEY_WCODE, HttpConstants.WCODE);
        this.put(COMMON_KEY_MAC, SystemUtil.getMacAddress());
        return this;
    }

}
