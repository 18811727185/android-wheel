package com.letv.mobile.pay.http.model;

import com.letv.mobile.http.model.LetvHttpBaseModel;

public class CheckOrderModel extends LetvHttpBaseModel {

    public static final String PAY_STATUS_SUCCESS = "1";
    private String orderId;
    private String orderName;
    private String payStatus;
    private String money;
    private String cancelTime;

    public String getOrderId() {
        return this.orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderName() {
        return this.orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public String getPayStatus() {
        return this.payStatus;
    }

    public void setPayStatus(String payStatus) {
        this.payStatus = payStatus;
    }

    public String getMoney() {
        return this.money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getCancelTime() {
        return this.cancelTime;
    }

    public void setCancelTime(String cancelTime) {
        this.cancelTime = cancelTime;
    }

    @Override
    public String toString() {
        return "CheckOrderModel [orderId=" + this.orderId + ", orderName="
                + this.orderName + ", payStatus=" + this.payStatus + ", money="
                + this.money + ", cancelTime=" + this.cancelTime + "]";
    }

}
