package com.letv.mobile.pay.common;

public interface PayTaskCallBack {

    public final int CODE_FAILURE = 0; // 支付失败
    public final int CODE_SUCCESS = 1; // 支付成功
    public final int CODE_PRE_CONFIRM = 2; // 支付结果待确认
    public final int CODE_CANCEL = 3; // 支付取消

    // TODO 定义更详细错误码 如 客户端不支持 下单失败 签名失败等

    public void callback(int code);

}
