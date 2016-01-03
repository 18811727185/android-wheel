package com.letv.android.letvlive.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.letv.mobile.core.log.Logger;
import com.letv.mobile.pay.controller.LetvPayModel;
import com.letv.mobile.pay.controller.OrderState;
import com.letv.mobile.pay.model.PayConstants;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final String TAG = "WXPayEntryActivity";
    private static final int CODE_SUCCESS = 0; // 支付成功
    private static final int CODE_FAILURE = -1; // 支付失败
    private static final int CODE_CANCEl = -2; // 取消支付
    private IWXAPI mApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mApi = WXAPIFactory.createWXAPI(this,
                PayConstants.APP_ID_SUPER_LIVE, true);
        this.mApi.handleIntent(this.getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
        this.mApi.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq arg0) {

    }

    @Override
    public void onResp(final BaseResp resp) {
        LetvPayModel payModel = LetvPayModel.getInstance();
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            Logger.i(TAG, "weixin pay end, errorCode=" + resp.errCode
                    + ", errorStr=" + resp.errStr);
            // 0 : 表示支付成功 -1 ：表示支付失败 -2 ：表示取消支付
            if (resp.errCode == CODE_SUCCESS) {
                payModel.setOrderState(OrderState.ORDER_STATE_PAY_SUCCESS);
            } else if (resp.errCode == CODE_CANCEl) {
                payModel.setOrderState(OrderState.ORDER_STATE_PAY_CANCEL);
            } else {
                payModel.setOrderState(OrderState.ORDER_STATE_PAY_FAILURE);
            }
        }

        this.finish();
    }

}
