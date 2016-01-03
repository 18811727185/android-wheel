package com.letv.mobile.pay.controller;

import android.app.Activity;
import android.content.Intent;

import com.letv.mobile.async.TaskCallBack;
import com.letv.mobile.core.log.Logger;
import com.letv.mobile.core.utils.ContextProvider;
import com.letv.mobile.http.bean.CommonResponse;
import com.letv.mobile.pay.common.PayTaskCallBack;
import com.letv.mobile.pay.http.model.CheckOrderModel;
import com.letv.mobile.pay.http.model.PurchaseOrderModel;
import com.letv.mobile.pay.http.parameter.CheckOrderParameter;
import com.letv.mobile.pay.http.parameter.PurchaseOrderParameter;
import com.letv.mobile.pay.http.request.CheckOrderRequest;
import com.letv.mobile.pay.http.request.PurchaseOrderRequest;
import com.letv.mobile.pay.model.Order;
import com.letv.mobile.pay.model.PayChannel;
import com.letv.mobile.pay.model.PayConstants;
import com.letv.mobile.pay.payment.BasePayHelper;
import com.letv.mobile.pay.payment.alipay.AliPayHelper;

/**
 * @author shibin
 */
public class LetvPayModel {

    private static final String TAG = "LetvPayModel";
    private static final float MIN_MONEY = 0.001f;
    private final PayChannel[] mSupportPayChannel = { PayChannel.ALI_PAY,
            PayChannel.WEIXIN_PAY };// TODO 暂时写法,返回支付宝和微信支付
    private final PayChannel[] mSupportPayChannelForLive = {
            PayChannel.ALI_PAY, PayChannel.WEIXIN_PAY_FOR_LIVE };
    private OrderState mOrderState = OrderState.ORDER_STATE_NOT_INIT;
    private Order mOrder;
    private PayChannel mPayChannel;
    private Activity mActivity;
    private PayTaskCallBack mPayTaskCallBack;
    private BasePayHelper mPayHelper;
    private PurchaseOrderModel mPurcaseOrderModel;

    private static LetvPayModel sInstance;

    private LetvPayModel() {
    }

    public static LetvPayModel getInstance() {
        if (LetvPayModel.sInstance == null) {
            synchronized (AliPayHelper.class) {
                if (LetvPayModel.sInstance == null) {
                    LetvPayModel.sInstance = new LetvPayModel();
                }
            }
        }
        return LetvPayModel.sInstance;
    }

    public PayChannel[] getSupportPayChannel(int purchaseType) {
        if (PayConstants.PURCHASE_TYPE_SUPER_LIVE_PAY == purchaseType) {
            return this.mSupportPayChannelForLive;
        } else {
            return this.mSupportPayChannel;
        }
    }

    public boolean checkAvailable(PayChannel payChannel) {
        this.mPayHelper = BasePayHelper.getInstance(payChannel);
        return this.mPayHelper.checkAvailable();
    }

    public void pay(Order order, PayChannel payChannel, Activity activity,
            PayTaskCallBack callback) {
        this.mOrder = order;
        this.mPayChannel = payChannel;
        this.mActivity = activity;
        this.mPayTaskCallBack = callback;
        this.mPayHelper = BasePayHelper.getInstance(payChannel);
        this.setOrderState(OrderState.ORDER_STATE_PRE_ORDER);
    }

    synchronized public void setOrderState(OrderState state) {
        Logger.i(LetvPayModel.TAG, "setOrderState, state=" + state
                + ", oldeState=" + this.mOrderState);
        if (state == null || this.mOrderState == state) {
            return;
        }
        this.mOrderState = state;
        switch (this.mOrderState) {
        case ORDER_STATE_NOT_INIT:
            break;
        case ORDER_STATE_PRE_ORDER:
            this.purchaseOrderFromServer();
            break;
        case ORDER_STATE_PRE_PAY:
            if (this.mPayHelper != null && this.mPurcaseOrderModel != null) {
                this.mPayHelper.executePay(this.mPurcaseOrderModel,
                        this.mActivity);
            }
            break;
        case ORDER_STATE_PAY_SUCCESS:
        case ORDER_STATE_PAY_PRE_CONFIRM:
        case ORDER_STATE_PAY_FAILURE:
        case ORDER_STATE_PAY_CANCEL:
            this.mActivity = null;
            this.doEndPayTransaction(state);
            break;
        default:
            this.mActivity = null;
            break;
        }
    }

    private void purchaseOrderFromServer() {
        new PurchaseOrderRequest(ContextProvider.getApplicationContext(),
                new TaskCallBack() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void callback(int code, String msg,
                            String errorCode, Object object) {
                        if (code == TaskCallBack.CODE_OK
                                && object instanceof CommonResponse) {
                            LetvPayModel.this.mPurcaseOrderModel = ((CommonResponse<PurchaseOrderModel>) object)
                                    .getData();
                            if (LetvPayModel.this.mPurcaseOrderModel != null) {
                                Logger.i(
                                        LetvPayModel.TAG,
                                        "purchaseOrder callback. PurchaseOrderModel="
                                                + LetvPayModel.this.mPurcaseOrderModel);
                                LetvPayModel.this.mOrder
                                        .setId(LetvPayModel.this.mPurcaseOrderModel
                                                .getCorderid());

                                // NOTE(qingxia): If the price is equals to 0,
                                // we assume the payment is success.
                                if (LetvPayModel.this.mOrder != null
                                        && ((LetvPayModel.this.mOrder
                                                .getCurrentPrice() - LetvPayModel.MIN_MONEY) < 0f)) {
                                    Logger.i(
                                            LetvPayModel.TAG,
                                            "getCurrentPrice = "
                                                    + LetvPayModel.this.mOrder
                                                            .getCurrentPrice());
                                    LetvPayModel.this
                                            .setOrderState(OrderState.ORDER_STATE_PAY_SUCCESS);
                                } else {
                                    LetvPayModel.this
                                            .setOrderState(OrderState.ORDER_STATE_PRE_PAY);
                                }

                            } else {
                                LetvPayModel.this
                                        .setOrderState(OrderState.ORDER_STATE_PAY_FAILURE);
                            }
                        } else {
                            LetvPayModel.this
                                    .setOrderState(OrderState.ORDER_STATE_PAY_FAILURE);
                        }
                    }
                }).execute(new PurchaseOrderParameter(this.mOrder.getId(),
                this.mOrder.getPurchaseType(), this.mOrder.getProductid(),
                this.mPayChannel.getId(), String.valueOf(this.mOrder
                        .getCurrentPrice()), this.mOrder.getVipType(),
                this.mOrder.getActivityIds(), null).combineParams());
    }

    private OrderState checkOrderStatusFromServer() {
        new CheckOrderRequest(ContextProvider.getApplicationContext(),
                new TaskCallBack() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void callback(int code, String msg,
                            String errorCode, Object object) {
                        if (code == TaskCallBack.CODE_OK
                                && object instanceof CommonResponse) {
                            CheckOrderModel model = ((CommonResponse<CheckOrderModel>) object)
                                    .getData();
                            if (model != null) {
                                Logger.i(LetvPayModel.TAG,
                                        "checkOrder callback, CheckOrderModel="
                                                + model);
                                LetvPayModel.this.mOrder.setValiddate(model
                                        .getCancelTime());
                            }
                        }
                        if (LetvPayModel.this.getOrderState() == OrderState.ORDER_STATE_PAY_SUCCESS) {
                            if (LetvPayModel.this.mPayTaskCallBack != null) {
                                LetvPayModel.this.mPayTaskCallBack
                                        .callback(PayTaskCallBack.CODE_SUCCESS);
                            }
                        } else {
                            if (LetvPayModel.this.mPayTaskCallBack != null) {
                                LetvPayModel.this.mPayTaskCallBack
                                        .callback(PayTaskCallBack.CODE_PRE_CONFIRM);
                            }
                        }
                    }
                }).execute(new CheckOrderParameter(this.mOrder.getId())
                .combineParams());
        return null;
    }

    private void doEndPayTransaction(OrderState state) {
        int resultCode;
        switch (state) {
        case ORDER_STATE_PAY_SUCCESS:
            resultCode = PayTaskCallBack.CODE_SUCCESS;
            break;
        case ORDER_STATE_PAY_PRE_CONFIRM:
            resultCode = PayTaskCallBack.CODE_PRE_CONFIRM;
            break;
        case ORDER_STATE_PAY_FAILURE:
            resultCode = PayTaskCallBack.CODE_FAILURE;
            break;
        case ORDER_STATE_PAY_CANCEL:
            resultCode = PayTaskCallBack.CODE_CANCEL;
            break;
        default:
            resultCode = PayTaskCallBack.CODE_CANCEL;
            break;
        }
        if (LetvPayModel.this.mPayTaskCallBack != null) {
            LetvPayModel.this.mPayTaskCallBack.callback(resultCode);
        }
        this.sendBoradcastForPayResult(resultCode);
    }

    private void sendBoradcastForPayResult(int resultCode) {
        Intent intent = new Intent();
        intent.setAction(PayConstants.ACTION_PAY_RESULT);
        intent.putExtra(PayConstants.PAY_RESULT, resultCode);
        intent.putExtra(PayConstants.PURCHASE_TYPE,
                this.mOrder.getPurchaseType());
        intent.putExtra(PayConstants.PRODUCT_ID, this.mOrder.getProductid());
        ContextProvider.getApplicationContext().sendBroadcast(intent);
    }

    public Order getOrder() {
        return this.mOrder;
    }

    public OrderState getOrderState() {
        return this.mOrderState;
    }

}
