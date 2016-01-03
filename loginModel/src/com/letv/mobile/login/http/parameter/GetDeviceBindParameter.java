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

import com.letv.mobile.http.parameter.LetvBaseParameter;

/**
 * @author shibin
 */
public class GetDeviceBindParameter extends LoginHttpCommonParameter {

    private static final long serialVersionUID = 5034410253150375279L;

    public static final String TYPE_ALL = "0";
    public static final String TYPE_ACTIVE = "1";
    private final String TYPE = "type"; // 0--查询全部（自带机卡和赠送机卡），1--只查询自带机卡，2--只查询赠送机卡
    private final String UID = "uid"; // 查询赠送时长（type=2或0）时必传
    private final String DEVICEKEY = "deviceKey";
    private final String DEVID = "devId";
    private final String type;
    private final String uid;
    private final String deviceKey;
    private final String devId;

    public GetDeviceBindParameter(String type, String uid, String deviceKey,
            String devId) {
        super();
        this.type = type;
        this.uid = uid;
        this.deviceKey = deviceKey;
        this.devId = devId;
    }

    @Override
    public LetvBaseParameter combineParams() {
        LetvBaseParameter parameter = super.combineParams();
        parameter.put(this.TYPE, this.type);
        parameter.put(this.UID, this.uid);
        parameter.put(this.DEVICEKEY, this.deviceKey);
        parameter.put(this.DEVID, this.devId);
        return parameter;
    }
}
