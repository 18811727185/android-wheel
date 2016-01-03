package com.letv.mobile.login.login;

import java.util.Observable;
import java.util.Observer;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.letv.mobile.async.TaskCallBack;
import com.letv.mobile.common.AsynRequestListener;
import com.letv.mobile.core.activity.BaseActivity;
import com.letv.mobile.core.log.Logger;
import com.letv.mobile.core.utils.ContextProvider;
import com.letv.mobile.core.utils.DeviceUtils;
import com.letv.mobile.core.utils.StringUtils;
import com.letv.mobile.http.bean.CommonResponse;
import com.letv.mobile.login.http.parameter.GetDeviceBindParameter;
import com.letv.mobile.login.http.request.GetDeviceBindRequest;
import com.letv.mobile.login.model.DeviceBindInfo;
import com.letv.mobile.login.model.DeviceBindTextInfo;
import com.letv.mobile.login.model.LoginConstants;
import com.letv.mobile.loginModel.LoginModel;

/**
 * This class provide letv device bind logic and state.
 * @author shibin
 */
public class DeviceBindModel {

    private final static String TAG = "DeviceBindModel";

    // System device bind action
    private static final String ACTION_DEVICE_BIND_RESULT = "com.letv.android.accountinfo.getvipresult";
    private static final String BIND_RESULT_FLAG = "isSuccess";
    private static final int RESULT_SUCCESS = 1;
    private static final String ACCOUNT_PACKAGE = "com.letv.android.accountinfo";
    private static final String GET_FULL_SCREEN_VIP_PROTOCAL_ACIVITY = "com.letv.android.accountinfo.activity.GetFullScreenVipProtocalActivity";
    private static final String GET_FULL_SCREEN_VIP_VIA_TV_ACTIVITY = "com.letv.android.accountinfo.activity.GetFullScreenVipViaTVActivity";

    private static final int REQUEST_STATE_NOT_END = 0; // 请求未结束
    private static final int REQUEST_STATE_END = 1; // 请求已结束
    private int mRequestState = REQUEST_STATE_NOT_END;
    private DeviceBindInfo mBindInfo;
    private DeviceBindTextInfo mBindTextInfo;
    private String mErrorCode;
    private String mErrorMsg;
    private final DeviceBindObservable mBindObservable = new DeviceBindObservable();
    private AsynRequestListener mListener;
    private final Context mContext = ContextProvider.getApplicationContext();

    private final BroadcastReceiver mBindStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int isSuccess = intent.getIntExtra(BIND_RESULT_FLAG, 0);
            Logger.i(TAG,
                    "Receive system device bind broadcastReceiver, isSuccess="
                            + isSuccess);
            if (RESULT_SUCCESS == isSuccess) {
                DeviceBindModel.this.getDeviceBindInfoFromServer();
                LoginModel.getMemberInfoFromServer(LoginModel.getUID(),
                        LoginModel.getToken(), null);
            }
        }
    };

    private final Observer mLoginObser = new Observer() {
        @Override
        public void update(Observable observable, Object data) {
            Logger.i(TAG,
                    "login update, loginState=" + LoginModel.getLoginState()
                            + ", uid=" + LoginModel.getUID());
            if (LoginModel.getLoginState() == LoginState.LOGIN_STATE_LOGIN) {
                DeviceBindModel.this.getDeviceBindInfoFromServer();
            }
        }
    };

    public DeviceBindModel() {
        Logger.i(TAG, "DeviceBindModel deviceKey=" + DeviceUtils.getDeviceKey());
        if (this.isSupportDeviceBind()) {
            LoginModel.addLoginStateObersvers(this.mLoginObser);
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_DEVICE_BIND_RESULT);
            this.mContext.registerReceiver(this.mBindStateReceiver, filter);
            this.getDeviceBindInfoFromServer(); // 查询绑定信息
        } else {
            this.mRequestState = REQUEST_STATE_END;
        }
    }

    /**
     * Start system device bind view
     */
    @SuppressLint("NewApi")
    public void bind() {
        final String REPORT_KEY = "report_id";
        final String REPORT_VALUE = "letv";
        if (this.mBindInfo != null) {
            final Intent intent = new Intent(Intent.ACTION_MAIN);
            String priority = this.mBindInfo.getPriority();
            if (DeviceBindInfo.PRIORITY_INVOKE.equals(priority)) {
                intent.setClassName(ACCOUNT_PACKAGE,
                        GET_FULL_SCREEN_VIP_PROTOCAL_ACIVITY);
            } else {
                intent.setClassName(ACCOUNT_PACKAGE,
                        GET_FULL_SCREEN_VIP_VIA_TV_ACTIVITY);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(LoginConstants.FROM, LoginConstants.LETV_LEADING);
            intent.putExtra(REPORT_KEY, REPORT_VALUE);
            // NOTE(shibin) 因致新领取会员页面未做登录验证(未登录跳转乐视账号崩溃),
            // 故跳转之前做登录判断,如未登录则先跳登录,登录完成后再跳会员领取页面.
            // Activity传入不便,启动登录使用topActivity,当topActivity为空时,不能启动登录页面
            if (LoginModel.isLogin()) {
                this.mContext.startActivity(intent);
            } else {
                LoginModel.logout(BaseActivity.getTopActivity(),
                        new AccountManagerCallback<Bundle>() {
                            @Override
                            public void run(AccountManagerFuture<Bundle> future) {
                                if (LoginModel.isLogin()) {
                                    DeviceBindModel.this.mContext
                                            .startActivity(intent);
                                }
                            }
                        });
            }
        }
    }

    public boolean isSupportDeviceBind() {
        return !StringUtils.isStringEmpty(DeviceUtils.getDeviceKey());
    }

    public void getDeviceBindInfo(AsynRequestListener listener) {
        this.mListener = listener;
        if (this.mRequestState == REQUEST_STATE_END) {
            if (this.mBindTextInfo != null) {
                this.callAsynRequestSuccess();
            } else {
                this.callAsynRequestError(this.mErrorCode, this.mErrorMsg);
            }
        }
    }

    private void getDeviceBindInfoFromServer() {
        Logger.i(TAG, "getDeviceBindInfoFromServer");
        this.mRequestState = REQUEST_STATE_NOT_END;
        String type;
        // NOTE(shibin) 登录情况下查询所有绑定信息,非登录时只查机器自身的绑定信息
        if (LoginModel.getLoginState() == LoginState.LOGIN_STATE_LOGIN) {
            type = GetDeviceBindParameter.TYPE_ALL;
        } else {
            type = GetDeviceBindParameter.TYPE_ACTIVE;
        }
        new GetDeviceBindRequest(ContextProvider.getApplicationContext(),
                new TaskCallBack() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void callback(int code, String errorMsg,
                            String errorCode, Object object) {
                        if (code == TaskCallBack.CODE_OK
                                && object instanceof CommonResponse) {
                            DeviceBindModel.this.mBindInfo = ((CommonResponse<DeviceBindInfo>) object)
                                    .getData();
                            Logger.i(TAG,
                                    "get device bind callback, mBindInfo="
                                            + DeviceBindModel.this.mBindInfo);
                            if (DeviceBindModel.this.mBindInfo != null) {
                                if (DeviceBindInfo.PRIORITY_INVOKE
                                        .equals(DeviceBindModel.this.mBindInfo
                                                .getPriority())) {
                                    DeviceBindModel.this.mBindTextInfo = DeviceBindModel.this.mBindInfo
                                            .getDeviceBindText();
                                } else {
                                    DeviceBindModel.this.mBindTextInfo = DeviceBindModel.this.mBindInfo
                                            .getPresentDeviceBindText();
                                }
                            }
                            DeviceBindModel.this.callAsynRequestSuccess();
                        } else {
                            DeviceBindModel.this.mErrorCode = errorCode;
                            DeviceBindModel.this.mErrorMsg = errorMsg;
                            DeviceBindModel.this.callAsynRequestError(
                                    errorCode, errorMsg);
                        }
                        DeviceBindModel.this.notifyBindStateChanged();
                        DeviceBindModel.this.mRequestState = REQUEST_STATE_END;
                    }
                }).execute(new GetDeviceBindParameter(type,
                LoginModel.getUID(), DeviceUtils.getDeviceKey(), DeviceUtils
                        .getDeviceId()).combineParams());
    }

    private void callAsynRequestError(String errorCode, String errorMsg) {
        Logger.i(TAG, "callAsynRequestError,errorCode= " + errorCode
                + ", errorMsg=" + errorMsg);
        if (this.mListener != null) {
            this.mListener.onFailed(errorCode, errorMsg);
            this.mListener = null;
        }
    }

    private void callAsynRequestSuccess() {
        Logger.i(TAG, "callAsynRequestSuccess, BindTextInfo" + this.mBindInfo);
        if (this.mListener != null) {
            this.mListener.onSuccess(this.mBindTextInfo);
            this.mListener = null;
        }
    }

    public void addBindStateObserver(Observer observer) {
        this.mBindObservable.addObserver(observer);
    }

    public void deleteBindStateObserver(Observer observer) {
        this.mBindObservable.deleteObserver(observer);
    }

    private void notifyBindStateChanged() {
        Logger.i(TAG, "notifyBindStateChanged");
        this.mBindObservable.setChanged();
        this.mBindObservable.notifyObservers();
    }

}
