package com.letv.statistic.model;

/**
 * @author lilong
 *         登录上报的参数传递
 */
public class LoginDataModel {

    /** 上一个页面为站外页面时该字段报“-”，页面id明细见“页面id定义“选项卡 */
    private String ref;

    /** 登录tvlive时间 */
    private String ts;

    /** 0:登录成功 1:退出登录 */
    private String st;

    public LoginDataModel() {

    }

    public LoginDataModel(String ref, String ts, String st) {
        this.ref = ref;
        this.ts = ts;
        this.st = st;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public String getSt() {
        return st;
    }

    public void setSt(String st) {
        this.st = st;
    }
}
