package com.letv.mobile.pay.controller;

/**
 * This state indicates the order state.
 * @author shibin
 */
public enum OrderState {
    ORDER_STATE_NOT_INIT, // 未初始化
    ORDER_STATE_PRE_ORDER, // 待下单
    ORDER_STATE_PRE_PAY, // 待支付
    ORDER_STATE_PAY_SUCCESS, // 支付成功
    ORDER_STATE_PAY_PRE_CONFIRM, // 支付结果待确认
    ORDER_STATE_PAY_CANCEL, // 取消支付
    ORDER_STATE_PAY_FAILURE; // 支付失败
}
