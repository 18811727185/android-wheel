package com.letv.mobile.login.model;

import java.io.Serializable;
import java.util.List;

import com.letv.mobile.http.model.LetvHttpBaseModel;

/**
 * @author shibin
 */
public class DeviceBindInfo extends LetvHttpBaseModel implements Serializable {

    private static final long serialVersionUID = -8321101584963206208L;

    public static final String PRIORITY_INVOKE = "1"; // 显示自带机卡绑定
    public static final String PRIORITY_PRESENT = "2"; // 显示赠送机卡绑定

    private int isDeviceActive; // 自带机卡绑定套餐是否已激活，0--数据不可用，1--未激活，可领取；2--已激活，不可领取
    private int bindMonths; // 自带机卡绑定时长，单位：月，仅在isDeviceActive为1时有效
    private String priority; // 机卡绑定显示优先级，1--优先显示自带机卡绑定，2--优先显示赠送机卡绑定
    private List<PresentDeviceBindInfo> presentDeviceBinds; // 可领取的赠送机卡时长列表
    private DeviceBindTextInfo deviceBindText;
    private DeviceBindTextInfo presentDeviceBindText;

    public int getIsDeviceActive() {
        return this.isDeviceActive;
    }

    public void setIsDeviceActive(int isDeviceActive) {
        this.isDeviceActive = isDeviceActive;
    }

    public int getBindMonths() {
        return this.bindMonths;
    }

    public void setBindMonths(int bindMonths) {
        this.bindMonths = bindMonths;
    }

    public String getPriority() {
        return this.priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public List<PresentDeviceBindInfo> getPresentDeviceBinds() {
        return this.presentDeviceBinds;
    }

    public void setPresentDeviceBinds(
            List<PresentDeviceBindInfo> presentDeviceBinds) {
        this.presentDeviceBinds = presentDeviceBinds;
    }

    public DeviceBindTextInfo getDeviceBindText() {
        return this.deviceBindText;
    }

    public void setDeviceBindText(DeviceBindTextInfo deviceBindText) {
        this.deviceBindText = deviceBindText;
    }

    public DeviceBindTextInfo getPresentDeviceBindText() {
        return this.presentDeviceBindText;
    }

    public void setPresentDeviceBindText(
            DeviceBindTextInfo presentDeviceBindText) {
        this.presentDeviceBindText = presentDeviceBindText;
    }

    @Override
    public String toString() {
        return "DeviceBindInfo [isDeviceActive=" + this.isDeviceActive
                + ", bindMonths=" + this.bindMonths + ", priority="
                + this.priority + ", presentDeviceBinds="
                + this.presentDeviceBinds + ", deviceBindText="
                + this.deviceBindText + ", presentDeviceBindText="
                + this.presentDeviceBindText + "]";
    }

}
