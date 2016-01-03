package com.letv.mobile.pay.http.model;

import com.letv.mobile.http.model.LetvHttpBaseModel;

public class PurchaseOrderModel extends LetvHttpBaseModel {

    private String corderid; // 订单号
    private String ordernumber;

    /** 支付宝sdk所需参数 **/
    private String info;

    /** 微信支付sdk所需参数 **/
    private String parentId;
    private String price;
    private String timestamp;
    private String noncestr;
    private String prepayid;
    private String weixinPackage;
    private String appid;
    private String sign;

    public String getCorderid() {
        return this.corderid;
    }

    public void setCorderid(String corderid) {
        this.corderid = corderid;
    }

    public String getParentId() {
        return this.parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getInfo() {
        return this.info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getPrice() {
        return this.price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getOrdernumber() {
        return this.ordernumber;
    }

    public void setOrdernumber(String ordernumber) {
        this.ordernumber = ordernumber;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getNoncestr() {
        return this.noncestr;
    }

    public void setNoncestr(String noncestr) {
        this.noncestr = noncestr;
    }

    public String getPrepayid() {
        return this.prepayid;
    }

    public void setPrepayid(String prepayid) {
        this.prepayid = prepayid;
    }

    public String getWeixinPackage() {
        return this.weixinPackage;
    }

    public void setWeixinPackage(String weixinPackage) {
        this.weixinPackage = weixinPackage;
    }

    public String getAppid() {
        return this.appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getSign() {
        return this.sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "PurchaseOrderModel [corderid=" + this.corderid + ", parentId="
                + this.parentId + ", info=" + this.info + ", price="
                + this.price + ", ordernumber=" + this.ordernumber
                + ", timestamp=" + this.timestamp + ", noncestr="
                + this.noncestr + ", prepayid=" + this.prepayid
                + ", weixinPackage=" + this.weixinPackage + ", appid="
                + this.appid + ", sign=" + this.sign + "]";
    }

}
