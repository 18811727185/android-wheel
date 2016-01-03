package com.letv.mobile.login.model;

import java.io.Serializable;

import com.letv.mobile.http.model.LetvHttpBaseModel;

/**
 * @author shibin
 */
public class ReceiveDeviceBindInfo extends LetvHttpBaseModel implements
        Serializable {

    private static final long serialVersionUID = 4895425809474614160L;

    private String endTime; // 领取的赠送时长对应会员有效期的截至时间

    public String getEndTime() {
        return this.endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "ReceiveDevicesBindInfo [endTime=" + this.endTime + "]";
    }

}
