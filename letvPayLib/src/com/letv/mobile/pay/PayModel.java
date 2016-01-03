package com.letv.mobile.pay;

import android.app.Activity;

import com.letv.mobile.pay.common.PayTaskCallBack;
import com.letv.mobile.pay.controller.LetvPayModel;
import com.letv.mobile.pay.controller.OrderState;
import com.letv.mobile.pay.model.Order;
import com.letv.mobile.pay.model.PayChannel;

/**
 * This class provide common interfaces for application pay requirement;
 * @author shibin
 */
public final class PayModel {

    private static LetvPayModel sModel = LetvPayModel.getInstance();

    private PayModel() {
    }

    /**
     * Get support pay modes
     */
    public static PayChannel[] getSupportPayChannel(int purchaseType) {
        return sModel.getSupportPayChannel(purchaseType);
    }

    public static boolean checkAvailable(PayChannel payChannel) {
        return sModel.checkAvailable(payChannel);
    }

    public static void pay(Order order, PayChannel payChannel,
            Activity activity, PayTaskCallBack callback) {
        sModel.pay(order, payChannel, activity, callback);
    }

    public static OrderState getOrderState() {
        return sModel.getOrderState();
    }

    public static Order getCurrentOrder() {
        return sModel.getOrder();
    }

}
