package com.letv.mobile.login.model;

import java.io.Serializable;

import com.letv.mobile.http.model.LetvHttpBaseModel;

/**
 * @author shibin
 */
public class PresentDeviceBindInfo extends LetvHttpBaseModel implements
        Serializable {

    private static final long serialVersionUID = 6961590804548797838L;

    private String id; // 赠送机卡时长信息的id
    private String title; // 标题
    private String activeTime; // 激活时间，格式为“yyyy-MM-dd”
    private String availableTime; // 从当前日期算起的剩余可领取时长，单位：天
    private String presentProductName; // 赠送时长的来源

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getActiveTime() {
        return this.activeTime;
    }

    public void setActiveTime(String activeTime) {
        this.activeTime = activeTime;
    }

    public String getAvailableTime() {
        return this.availableTime;
    }

    public void setAvailableTime(String availableTime) {
        this.availableTime = availableTime;
    }

    public String getPresentProductName() {
        return this.presentProductName;
    }

    public void setPresentProductName(String presentProductName) {
        this.presentProductName = presentProductName;
    }

    @Override
    public String toString() {
        return "PresentDeviceBindInfo [id=" + this.id + ", title=" + this.title
                + ", activeTime=" + this.activeTime + ", availableTime="
                + this.availableTime + ", presentProductName="
                + this.presentProductName + "]";
    }

}
