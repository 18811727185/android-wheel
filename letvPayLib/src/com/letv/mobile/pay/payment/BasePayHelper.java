package com.letv.mobile.pay.payment;

import android.app.Activity;

import com.letv.mobile.pay.http.model.PurchaseOrderModel;
import com.letv.mobile.pay.model.PayChannel;
import com.letv.mobile.pay.payment.alipay.AliPayHelper;
import com.letv.mobile.pay.payment.wxpay.WXPayHelper;

public abstract class BasePayHelper {

    public abstract boolean checkAvailable();

    public abstract void executePay(PurchaseOrderModel purchaseOrderModel,
            Activity activity);

    public static BasePayHelper getInstance(PayChannel payChannel) {
        BasePayHelper payHelper = null;
        switch (payChannel) {
        case ALI_PAY:
            payHelper = AliPayHelper.getInstance();
            break;
        case WEIXIN_PAY:
        case WEIXIN_PAY_FOR_LIVE:
            payHelper = WXPayHelper.getInstance();
            break;
        default:
            break;
        }
        return payHelper;
    }

}
