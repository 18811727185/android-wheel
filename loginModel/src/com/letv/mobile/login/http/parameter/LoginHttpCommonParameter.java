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

import com.letv.mobile.core.utils.ContextProvider;
import com.letv.mobile.core.utils.DeviceUtils;
import com.letv.mobile.core.utils.SystemUtil;
import com.letv.mobile.core.utils.TerminalUtils;
import com.letv.mobile.http.parameter.LetvBaseParameter;
import com.letv.mobile.login.http.LoginHttpContants;
import com.letv.mobile.loginModel.LoginModel;

public class LoginHttpCommonParameter extends LetvBaseParameter {

    private static final long serialVersionUID = -3844696412420721190L;
    private final static String TERMINALBRAND = "terminalBrand";
    // TODO(qingxia): Maybe used later
    // private final static String TERMINALSERIES = "terminalSeries";
    // private final static String BROADCASTID = "broadcastId";
    private final static String TERMINALUUID = "terminalUuid"; //
    private final static String BSCHANNEL = "bsChannel";
    private final static String APPCODE = "appCode";
    private final static String APPID = "appId";
    private final static String CLIENT = "client";
    private final static String MAC = "mac";
    private static final String COMMON_KEY_DEV_ID = "devId";// 客户端设备唯一id
    protected static final String TERMINAL_APPLICATION = "terminalApplication";

    @Override
    public LetvBaseParameter combineParams() {
        LetvBaseParameter parameter = new LoginHttpCommonParameter();
        parameter.put(TERMINALBRAND, LoginHttpContants.TERMINAL_BRAND);
        // parameter.put(TERMINALSERIES, DevicesUtils.getTerminalSeries());
        // parameter.put(BROADCASTID, TerminalUtils.getBroadcastId());
        parameter.put(TERMINALUUID, TerminalUtils.getTerminalUUID());
        parameter.put(BSCHANNEL, LoginModel.getBsChannel());
        parameter.put(APPCODE, SystemUtil.getVersionCode(ContextProvider
                .getApplicationContext()));
        parameter.put(APPID, LoginHttpContants.APPLICATION_ID);
        parameter.put(CLIENT, LoginHttpContants.CLIENT);
        parameter.put(MAC, SystemUtil.getMacAddress());
        parameter.put(TERMINAL_APPLICATION,
                LoginModel.getTerminalApplicationName());
        parameter.put(COMMON_KEY_DEV_ID, DeviceUtils.getDeviceId());
        return parameter;
    }
}
