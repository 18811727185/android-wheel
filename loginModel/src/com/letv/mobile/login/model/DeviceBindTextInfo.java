package com.letv.mobile.login.model;

import java.io.Serializable;

import com.letv.mobile.http.model.LetvHttpBaseModel;

public class DeviceBindTextInfo extends LetvHttpBaseModel implements
        Serializable {

    private static final long serialVersionUID = -3702624628317508922L;

    private String title; // 标题
    private String content; // 内容
    private String tips; // 简单文案

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTips() {
        return this.tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    @Override
    public String toString() {
        return "DeviceBindTextInfo [title=" + this.title + ", content="
                + this.content + ", tips=" + this.tips + "]";
    }

}
