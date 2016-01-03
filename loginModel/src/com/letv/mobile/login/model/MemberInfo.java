/**
 *
 * Copyright 2015 LeTV Technology Co. Ltd., Inc. All rights reserved.
 *
 * @Author : qingxia
 *
 * @Description :
 *
 */

package com.letv.mobile.login.model;

import java.io.Serializable;

import com.letv.mobile.http.model.LetvHttpBaseModel;

/**
 * http://10.154.156.205/mobile/vip/getAccount.json?uid={uid}&token={token}
 * 参数：token 类型 String 是否必传：是
 * 参数：uid 类型 String 是否必传：是
 * 结果：
 * {
 * "status": {status},
 * "data":
 * {
 * "cancelTime": {cancelTime},
 * "seniorCancelTime": {seniorCancelTime},
 * "vipType": {vipType},
 * "vipTypeName": {vipTypeName}
 * }
 * }
 * 或
 * {
 * "status":{status},
 * "errorCode":{errorCode},
 * "errorMessage":{errorMessage}
 * }
 * 主要字段说明：
 * status:Integer，1业务正常，0业务异常
 * errorCode:String 业务异常码，status为0时返回此字段
 * errorMessage:String 业务异常方案，status为0时返回此字段
 * cancelTime:普通vip到期时间，毫秒数
 * seniorCancelTime：高级vip到期时间，毫秒数
 * vipType:0非会员，1普通会员，2高级会员
 * vipTypeName:会员类型名称
 * 例如：
 * http://10.154.156.205/mobile/vip/getAccount.json?uid=82097576&token=10382
 * b8d19P03h0leUyrAwileFMA3x5lz5ZXfbjzKl3N4VKsSzOTBOn5JuZQvYYpORdcO3
 * {
 * "status": 1,
 * "data":
 * {
 * "cancelTime": "1485168397000",
 * "seniorCancelTime": "1485168397000",
 * "vipType": "2",
 * "vipTypeName": "高级会员"
 * }
 * }
 * 或
 * http://10.154.156.205/mobile/vip/getAccount.json?uid=82097&token=10382
 * b8d19P03h0leUyrAwileFMA3x5lz5ZXfbjzKl3N4VKsSzOTBOn5JuZQvYYpORdcO3
 * {
 * "status": 1,
 * "data":
 * {
 * "vipType": "0",
 * "vipTypeName": "你还不是会员 "
 * }
 * }
 * @author xiaqing
 */
public class MemberInfo extends LetvHttpBaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String cancelTime = "";

    private String seniorCancelTime = "";

    private String vipType = "";

    private String vipTypeName = "";

    public String getCancelTime() {
        return this.cancelTime;
    }

    public void setCancelTime(String cancelTime) {
        this.cancelTime = cancelTime;
    }

    public String getSeniorCancelTime() {
        return this.seniorCancelTime;
    }

    public void setSeniorCancelTime(String seniorCancelTime) {
        this.seniorCancelTime = seniorCancelTime;
    }

    public String getVipType() {
        return this.vipType;
    }

    public void setVipType(String vipType) {
        this.vipType = vipType;
    }

    public String getVipTypeName() {
        return this.vipTypeName;
    }

    public void setVipTypeName(String vipTypeName) {
        this.vipTypeName = vipTypeName;
    }

    public boolean equals(MemberInfo memberInfo) {
        if (memberInfo == null) {
            return false;
        }

        if (!this.isStringEquals(memberInfo.cancelTime, this.cancelTime)) {
            return false;
        }

        if (!this.isStringEquals(memberInfo.vipType, this.vipType)) {
            return false;
        }

        if (!this.isStringEquals(memberInfo.seniorCancelTime,
                this.seniorCancelTime)) {
            return false;
        }

        return true;
    }

    private boolean isStringEquals(String s1, String s2) {
        if (s1 == null) {
            if (s2 == null) {
                return true;
            } else {
                return false;
            }
        }

        return s1.equals(s2);
    }

    @Override
    public String toString() {
        return "UserInfo [cancelTime=" + this.cancelTime
                + ", seniorCancelTime=" + this.seniorCancelTime + ", vipType="
                + this.vipType + ", vipTypeName=" + this.vipTypeName + "]";
    }
}
