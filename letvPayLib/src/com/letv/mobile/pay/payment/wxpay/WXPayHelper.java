package com.letv.mobile.pay.payment.wxpay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.letv.mobile.core.utils.ContextProvider;
import com.letv.mobile.pay.http.model.PurchaseOrderModel;
import com.letv.mobile.pay.payment.BasePayHelper;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXPayHelper extends BasePayHelper {

    public static final String APPID = "wxec1aaf0d01e71b0b";
    private final String WX_PACKAGE = "com.tencent.mm";
    private static WXPayHelper sInstance;
    private IWXAPI mApi;
    private PurchaseOrderModel mPurchaseOrderModel;

    private WXPayHelper() {

    }

    public static WXPayHelper getInstance() {
        if (sInstance == null) {
            synchronized (WXPayHelper.class) {
                if (sInstance == null) {
                    sInstance = new WXPayHelper();
                }
            }
        }
        return sInstance;
    }

    @Override
    public boolean checkAvailable() {
        return this.checkWXInstalled();
    }

    @Override
    public void executePay(PurchaseOrderModel purchaseOrderModel,
            Activity activity) {
        this.mPurchaseOrderModel = purchaseOrderModel;
        this.registerToWX(ContextProvider.getApplicationContext());
        this.callWXClient();
    }

    private void registerToWX(Context context) {
        this.mApi = WXAPIFactory.createWXAPI(context,
                this.mPurchaseOrderModel.getAppid(), true);
        this.mApi.registerApp(this.mPurchaseOrderModel.getAppid());
    }

    private void callWXClient() {
        PayReq req = new PayReq();
        req.appId = this.mPurchaseOrderModel.getAppid();
        req.partnerId = this.mPurchaseOrderModel.getParentId();
        req.prepayId = this.mPurchaseOrderModel.getPrepayid();
        req.nonceStr = this.mPurchaseOrderModel.getNoncestr();
        req.timeStamp = this.mPurchaseOrderModel.getTimestamp();
        req.packageValue = this.mPurchaseOrderModel.getWeixinPackage();
        req.sign = this.mPurchaseOrderModel.getSign();
        this.mApi.sendReq(req);
    }

    @SuppressLint("InlinedApi")
    private boolean checkWXInstalled() {
        try {
            ContextProvider
                    .getApplicationContext()
                    .getPackageManager()
                    .getApplicationInfo(this.WX_PACKAGE,
                            PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }
}
