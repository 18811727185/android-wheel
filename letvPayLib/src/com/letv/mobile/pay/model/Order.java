package com.letv.mobile.pay.model;

import java.io.Serializable;

public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    private int state;
    private String id;
    private String name;
    private int purchaseType; // 1-影片单点 2-套餐 3-直播单点
    private String productid;
    private int vipType; // 会员类型，当购买套餐时需要赋值 1-普通会员 9-高级会员
    private float originPrice;
    private float currentPrice;
    private String validdate;
    private PayChannel payChannel;
    private String activityIds;

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int geturchaseType() {
        return this.purchaseType;
    }

    public void setPurchaseType(int purchaseType) {
        this.purchaseType = purchaseType;
    }

    public int getPurchaseType() {
        return this.purchaseType;
    }

    public String getProductid() {
        return this.productid;
    }

    public void setProductid(String productid) {
        this.productid = productid;
    }

    public int getVipType() {
        return this.vipType;
    }

    public void setVipType(int vipType) {
        this.vipType = vipType;
    }

    public float getOriginPrice() {
        return this.originPrice;
    }

    public void setOriginPrice(float originPrice) {
        this.originPrice = originPrice;
    }

    public float getCurrentPrice() {
        return this.currentPrice;
    }

    public void setCurrentPrice(float currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getValiddate() {
        return this.validdate;
    }

    public void setValiddate(String validdate) {
        this.validdate = validdate;
    }

    public PayChannel getPayChannel() {
        return this.payChannel;
    }

    public void setPayChannel(PayChannel payChannel) {
        this.payChannel = payChannel;
    }

    public String getActivityIds() {
        return this.activityIds;
    }

    public void setActivityIds(String activityIds) {
        this.activityIds = activityIds;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    public String toString() {
        return "Order [state=" + this.state + ", id=" + this.id + ", name="
                + this.name + ", purchaseType=" + this.purchaseType
                + ", productid=" + this.productid + ", vipType=" + this.vipType
                + ", originPrice=" + this.originPrice + ", currentPrice="
                + this.currentPrice + ", validdate=" + this.validdate
                + ", payChannel=" + this.payChannel + ", activityIds="
                + this.activityIds + "]";
    }

}
