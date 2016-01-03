package com.letv.mobile.pay.payment.alipay;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import com.alipay.sdk.app.PayTask;
import com.letv.mobile.core.log.Logger;
import com.letv.mobile.pay.controller.LetvPayModel;
import com.letv.mobile.pay.controller.OrderState;
import com.letv.mobile.pay.http.model.PurchaseOrderModel;
import com.letv.mobile.pay.payment.BasePayHelper;
import com.ta.utdid2.android.utils.StringUtils;

public class AliPayHelper extends BasePayHelper {

    /*
     * 支付结果错误码：
     * 9000:订单支付成功
     * 8000:正在处理中（"支付结果确认中"）
     * 代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
     * 4000:订单支付失败
     * 6001:用户中途取消
     * 6002:网络连接出错
     */

    private static final String TAG = "AliPayHelper";
    private static AliPayHelper sInstance;
    private final LetvPayModel mPayModel = LetvPayModel.getInstance();
    private static final String RESULT_STATUS_SUCCESS = "9000";
    private static final String RESULT_STATUS_CONFIRM = "8000";
    private static final String RESULT_STATUS_FAILURE = "4000";
    private static final String RESULT_STATUS_CANCEL = "6001";
    private static final int SDK_PAY_FLAG = 1;
    private static final int RESULT_STATUS_INFO_ERROR = -1;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SDK_PAY_FLAG:
                AliPayResult result = new AliPayResult((String) msg.obj);
                String resultStatus = result.getResultStatus();
                // 判断resultStatus 为“9000”则代表支付成功
                if (AliPayHelper.RESULT_STATUS_SUCCESS.equals(resultStatus)) {
                    AliPayHelper.this.mPayModel
                            .setOrderState(OrderState.ORDER_STATE_PAY_SUCCESS);
                } else if (AliPayHelper.RESULT_STATUS_CONFIRM
                        .equals(resultStatus)) {
                    AliPayHelper.this.mPayModel
                            .setOrderState(OrderState.ORDER_STATE_PAY_PRE_CONFIRM);
                } else if (AliPayHelper.RESULT_STATUS_CANCEL
                        .equals(resultStatus)) {
                    AliPayHelper.this.mPayModel
                            .setOrderState(OrderState.ORDER_STATE_PAY_CANCEL);
                } else {// 不是成功和用户主动取消的,其他错误码当支付失败处理
                    AliPayHelper.this.mPayModel
                            .setOrderState(OrderState.ORDER_STATE_PAY_FAILURE);
                }
                break;
            case RESULT_STATUS_INFO_ERROR:
            default:
                // Any other msg is failed.
                AliPayHelper.this.mPayModel
                        .setOrderState(OrderState.ORDER_STATE_PAY_FAILURE);
                break;
            }
        };
    };

    private AliPayHelper() {

    }

    public static AliPayHelper getInstance() {
        if (AliPayHelper.sInstance == null) {
            synchronized (AliPayHelper.class) {
                if (AliPayHelper.sInstance == null) {
                    AliPayHelper.sInstance = new AliPayHelper();
                }
            }
        }
        return AliPayHelper.sInstance;
    }

    @Override
    public boolean checkAvailable() {
        return true;
    }

    private void sendInfoErrorMsg() {
        this.mHandler.sendEmptyMessage(AliPayHelper.RESULT_STATUS_INFO_ERROR);
    }

    @Override
    public void executePay(PurchaseOrderModel purchaseOrderModel,
            final Activity activity) {
        if (purchaseOrderModel == null || activity == null) {
            this.sendInfoErrorMsg();
            return;
        }

        final String payInfo = purchaseOrderModel.getInfo();
        if (StringUtils.isEmpty(payInfo)) {
            this.sendInfoErrorMsg();
            return;
        }

        Logger.i(AliPayHelper.TAG, "alipay executepay payInfo=" + payInfo);
        new Thread(new Runnable() {
            @Override
            public void run() {
                PayTask aliPay = new PayTask(activity);
                String result = aliPay.pay(payInfo);
                Logger.i(AliPayHelper.TAG, result);
                Message msg = new Message();
                msg.what = AliPayHelper.SDK_PAY_FLAG;
                msg.obj = result;
                AliPayHelper.this.mHandler.sendMessage(msg);
            }
        }).start();
    }

}
