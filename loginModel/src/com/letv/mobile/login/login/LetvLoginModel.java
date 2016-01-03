/**
 *
 * Copyright 2015 LeTV Technology Co. Ltd., Inc. All rights reserved.
 *
 * @Author : qingxia
 *
 * @Description :
 *
 */

package com.letv.mobile.login.login;

import com.letv.mobile.async.TaskCallBack;
import com.letv.mobile.common.AsynRequestListener;
import com.letv.mobile.core.log.Logger;
import com.letv.mobile.core.utils.ContextProvider;
import com.letv.mobile.core.utils.NetworkUtil;
import com.letv.mobile.core.utils.NetworkUtil.OnNetworkChangeListener;
import com.letv.mobile.core.utils.SharedPreferencesManager;
import com.letv.mobile.core.utils.StringUtils;
import com.letv.mobile.core.utils.TimeUtils;
import com.letv.mobile.http.bean.CommonResponse;
import com.letv.mobile.login.http.parameter.GetAccountInfoParameter;
import com.letv.mobile.login.http.parameter.TokenLoginParameter;
import com.letv.mobile.login.http.request.GetAccountInfoRequest;
import com.letv.mobile.login.http.request.TokenLoginRequest;
import com.letv.mobile.login.model.LoginConstants;
import com.letv.mobile.login.model.MemberInfo;
import com.letv.mobile.login.model.UserInfo;
import com.letv.mobile.loginModel.LoginModel;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AuthenticatorDescription;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import java.util.Date;
import java.util.Observer;

/**
 * This class provide letv login logic and state.
 * Reference: http://wiki.letv.cn/pages/viewpage.action?pageId=24305935
 * @author xiaqing
 */
final public class LetvLoginModel {
    private final static String TAG = "LetvLoginModel";
    // System login action
    public static final String ACTION_ACCOUNTS_CHANGED = "android.accounts.LOGIN_ACCOUNTS_CHANGED"; // 登录帐号变化广播
    // Pay result action
    private final static String ACTION_PAY_RESULT = "com.letv.mobile.pay";

    // Get system login token
    public static final String ACCOUNT_TYPE = "com.letv";
    public static final String AUTH_TOKEN_TYPE_LETV = "tokenTypeLetv";

    // login state
    private LoginState mState = LoginState.LOGIN_STATE_NOT_INIT;
    private MemberState mMemberState = MemberState.MEMBER_STATE_NO_INIT;
    private String mLoginToken = "";

    // User info
    private UserInfo mUserInfo = null;
    private final LoginObservable mUserStateObservable = new LoginObservable();
    // Member info
    private MemberInfo mMemberInfo = null;
    private final LoginObservable mMemberStateObservable = new LoginObservable();

    // This receiver listen to the letv account state changed information.
    private final BroadcastReceiver mLoginStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.i(TAG, "Receive system  BroadcastReceiver");
            String action = intent.getAction();
            if (AccountManager.LOGIN_ACCOUNTS_CHANGED_ACTION.equals(action)) {
                if (isLogin(context)) {
                    Logger.i(TAG, "Receive system ACTION_LOGIN BroadcastReceiver");
                    LetvLoginModel.this.setLoginState(LoginState.LOGIN_STATE_UNCHECKED_LOGIN);
                } else {
                    Logger.i(TAG,
                            "Receive system ACTION_LOGOUT ||  ACTION_LOGOUT_SAVE BroadcastReceiver");
                    LetvLoginModel.this.setLoginState(LoginState.LOGIN_STATE_LOGOUT);
                }
            }
        }

    };

    /**
     * NOTE(qingxia): We should observe the network. If the current state is not
     * initialize, we should fetch data while the network is OK.
     */
    private final OnNetworkChangeListener mListener = new OnNetworkChangeListener() {
        @Override
        public void onNetworkConnected() {
            if (LetvLoginModel.this.getLoginState() == LoginState.LOGIN_STATE_UNCHECKED_LOGIN) {
                LetvLoginModel.this.tokenLogin(null);
            } else if (LetvLoginModel.this.getMemberState() == MemberState.MEMBER_STATE_NO_INIT) {
                UserInfo userInfo = LetvLoginModel.this.getUserInfo();
                if (userInfo == null && StringUtils.isStringEmpty(LetvLoginModel.this.getToken())) {
                    return;
                }
                LetvLoginModel.this.getMemberInfoFromServer(userInfo.getUid(),
                        LetvLoginModel.this.getToken(), null);
            }
        }

        @Override
        public void onNetworkDisconnected() {

        }
    };

    private final BroadcastReceiver mMemberPayReceiver = new BroadcastReceiver() {
        final String PAY_RESULT = "pay_result";
        final String PURCHASE_TYPE = "purchase_type";
        final int PAY_SUCCESS = 1;
        final int TYPE_VIP_PACKAGE = 2;
        final int FETCH_MEMBER_INFO_DELAY = 2000;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_PAY_RESULT.equals(action)) {
                int result = intent.getIntExtra(this.PAY_RESULT, -1);
                int type = intent.getIntExtra(this.PURCHASE_TYPE, -1);
                if (result == this.PAY_SUCCESS && type == this.TYPE_VIP_PACKAGE) {
                    Logger.i(TAG, "mMemberPayReceiver, update member info");
                    // NOTE(shibin): We should update member information when
                    // user pay
                    // member success. It will take a long time for
                    // synchronizing
                    // payment data. So we get member information later.
                    LoginConstants.getWorkingThreadHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LetvLoginModel.this.getMemberInfoFromServer(LoginModel.getUID(),
                                    LoginModel.getToken(), null);
                        }
                    }, this.FETCH_MEMBER_INFO_DELAY);
                }
            }
        }
    };

    /**
     * TODO(qingxia): This function is not test
     * Get system account information.
     * NOTE(qingxia): This function should not be running in
     * Main Thread
     * @param context
     * @return
     *         null or login token
     */
    @SuppressLint("NewApi")
    private static String getTokenBlock(Context context) {
        String authToken = null;
        boolean notifyAuthFailure = true;
        AccountManager am = AccountManager.get(context);

        final Account[] accountList = am.getAccountsByType(ACCOUNT_TYPE);
        if (accountList.length <= 0) {
            return authToken;
        }

        try {
            authToken = am.blockingGetAuthToken(accountList[0], AUTH_TOKEN_TYPE_LETV,
                    notifyAuthFailure);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (StringUtils.isStringEmpty(authToken)) {
            Logger.e(TAG, "getTokenBlock failed");
            // TODO(qingxia): Maybe we should add some other logic here.
        }
        return authToken;
    }

    public String getTokenFromBox() {
        String token = null;
        Cursor cursor = null;

        try {
            Uri uri = Uri.parse(LoginConstants.CONTENT_ACCOUNT_USER_INFO_URI);
            cursor = ContextProvider.getApplicationContext().getContentResolver()
                    .query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                token = cursor.getString(cursor.getColumnIndex(LoginConstants.TOKEN));
            }
        } catch (Exception e) {
            Logger.e(TAG, "queryInfoFromBox failed" + e.getMessage());
            e.printStackTrace();
        } finally {
            Logger.e(TAG, "queryInfoFromBox token:" + token);
            if (cursor != null) {
                cursor.close();
            }
        }

        return token;
    }

    /**
     * TODO(qingxia): Unchecked.
     * @param context
     * @return
     */
    @SuppressLint("NewApi")
    private static boolean isLogin(Context context) {
        AccountManager am = AccountManager.get(context);
        boolean isLogin = false;
        final Account[] accounts = am.getAccountsByType(ACCOUNT_TYPE);
        if (accounts != null && accounts.length > 0) {
            isLogin = true;
        }
        return isLogin;
    }

    /**
     * This function provide to check if current account system has letv
     * account.
     * @param context
     * @return
     *         true
     *         flase
     */
    @SuppressLint("NewApi")
    public static boolean hasLetvAuthenticator(Context context) {
        AuthenticatorDescription[] allTypes = AccountManager.get(context).getAuthenticatorTypes();
        for (AuthenticatorDescription authenticatorType : allTypes) {
            if (ACCOUNT_TYPE.equals(authenticatorType.type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get login name from account manager.
     * @param context
     * @return
     */
    @SuppressLint("NewApi")
    private static UserInfo getUserInfo(Context context) {
        // NOTE(qingxia): This code should be called while the signature
        // final String UID = "UID";
        UserInfo userInfo = null;
        AccountManager am = AccountManager.get(context);
        final Account[] accountList = am.getAccountsByType(ACCOUNT_TYPE);

        if (accountList != null && accountList.length > 0) {
            userInfo = new UserInfo();
            userInfo.setUsername(accountList[0].name);
            // NOTE(qingxia): This code should be called while the signature
            // equals to system signature
            // userInfo.setUid(am.getUserData(accountList[0], UID));
        }
        return userInfo;
    }

    /**
     * Jump to
     * @param activity
     */
    @SuppressLint("NewApi")
    private static void systemLogin(final Activity activity, AccountManagerCallback<Bundle> callback) {
        AccountManager am = AccountManager.get(ContextProvider.getApplicationContext());
        Bundle options = new Bundle();

        // 如果调用后，不希望登录成功之后进入用户中心，而是直接finish调。
        // 可以在options中传入boolean值的参数loginFinish = true
        options.putBoolean("loginFinish", true);

        am.addAccount(ACCOUNT_TYPE, AUTH_TOKEN_TYPE_LETV, null, options, activity, callback,
                LoginConstants.getUiThreadHandler());
    }

    private void init() {
        Logger.i(TAG, "LetvLoginModel init()");

        // NOTE(qingxia): We should register receiver listen the system login
        // module.
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ACCOUNTS_CHANGED);
        ContextProvider.getApplicationContext().registerReceiver(this.mLoginStateReceiver, filter);

        // NOTE(shibin): We should register receiver listen member payment
        ContextProvider.getApplicationContext().registerReceiver(this.mMemberPayReceiver,
                new IntentFilter(ACTION_PAY_RESULT));

        // Initialize login state for first time.
        if (!isLogin(ContextProvider.getApplicationContext())) {
            LetvLoginModel.this.setLoginState(LoginState.LOGIN_STATE_LOGOUT);
        } else {
            LetvLoginModel.this.setLoginState(LoginState.LOGIN_STATE_UNCHECKED_LOGIN);
        }

        // NOTE(qingxia): Register observer to observe network state.
        NetworkUtil.registerNetworkChangeListener(this.mListener);
    }

    public LetvLoginModel() {
        this.init();
    }

    synchronized public LoginState getLoginState() {
        return this.mState;
    }

    private void notifyUserStateChanged() {
        LetvLoginModel.this.mUserStateObservable.setChanged();
        LetvLoginModel.this.mUserStateObservable.notifyObservers();
    }

    private void notifyMemberStateChanged() {
        LetvLoginModel.this.mMemberStateObservable.setChanged();
        LetvLoginModel.this.mMemberStateObservable.notifyObservers();
    }

    /**
     * Set login state.
     * @param loginState
     */
    synchronized private void setLoginState(LoginState loginState) {
        if (loginState == null || loginState.equals(this.mState)) {
            return;
        }
        this.mState = loginState;

        Logger.i(TAG, "setLoginState new state  = " + this.mState);

        // NOTE(qingxia): While the new state is
        // LoginState.LOGIN_STATE_UNCHECKED_LOGIN,
        // we should get the account information synchronized. So we have to
        // notify state changed after we get the users' token.
        if (loginState == LoginState.LOGIN_STATE_UNCHECKED_LOGIN) {
            if (StringUtils.isStringEmpty(this.mLoginToken)) {
                LoginConstants.getWorkingThreadHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        // Refresh token.
                        LetvLoginModel.this.mLoginToken = getTokenFromBox();

                        if (StringUtils.isStringEmpty(LetvLoginModel.this.mLoginToken)) {
                            Logger.e(TAG, "Receive login success bug no login token");
                        } else {
                            Logger.i(TAG, "Receive login success token = "
                                    + LetvLoginModel.this.mLoginToken);
                            // Do member info request.
                            LetvLoginModel.this.tokenLogin(null);
                        }

                        LetvLoginModel.this.mUserInfo = LetvLoginModel.this.getLocalUserInfo();
                        LetvLoginModel.this.notifyUserStateChanged();
                    }
                });
                return;
            }
        } else if (loginState == LoginState.LOGIN_STATE_LOGIN) {
            // update local userinfo
            this.saveOrUpdateLocalUserInfo();
        } else {
            // While logout, we should set the default member state to no init.
            this.setMemberState(MemberState.MEMBER_STATE_NO_INIT, null);
            this.mLoginToken = "";
            // While logout, we should set the userInfo to null
            this.setUserInfo(null);
        }

        this.notifyUserStateChanged();
    }

    synchronized public MemberState getMemberState() {
        return this.mMemberState;
    }

    synchronized public void setMemberState(MemberState memberState, MemberInfo memberInfo) {
        Logger.i(TAG, "setMemberState new memberState  = " + memberState + " memberInfo ="
                + memberInfo);
        // Check if member information have been changed.
        if (this.mMemberState == memberState) {
            if (memberInfo == null && this.mMemberInfo == null) {
                return;
            }

            if (this.mMemberInfo != null && this.mMemberInfo.equals(memberInfo)) {
                return;
            }
        }

        Logger.i(TAG, "setMemberState new memberState  changed");
        // If member information changed, modify state and notify obeservers.
        this.mMemberState = memberState;
        this.setMemberInfo(memberInfo);

        Logger.d(TAG, "setMemberState new memberState  notify observers.");
        this.notifyMemberStateChanged();
    }

    synchronized void updateMemberState(MemberInfo memberInfo) {
        MemberState state = MemberState.MEMBER_STATE_NO_INIT;

        if (memberInfo != null) {
            state = MemberState.getStateById(memberInfo.getVipType());
        }

        this.setMemberState(state, memberInfo);
    }

    synchronized public UserInfo getUserInfo() {
        return this.mUserInfo;
    }

    synchronized void setUserInfo(UserInfo userInfo) {
        this.mUserInfo = userInfo;
    }

    synchronized public MemberInfo getMemberInfo() {
        return this.mMemberInfo;
    }

    synchronized void setMemberInfo(MemberInfo memberInfo) {
        this.mMemberInfo = memberInfo;
    }

    private void callAsynRequestError(final AsynRequestListener listener, String errorCode,
            String errorMsg) {
        if (listener != null) {
            listener.onFailed(errorCode, errorMsg);
        }
    }

    private void callAsynRequestSuccess(final AsynRequestListener listener) {
        if (listener != null) {
            listener.onSuccess(null);
        }
    }

    /**
     * Do token login
     * @param listener
     */
    public void tokenLogin(final AsynRequestListener listener) {

        new TokenLoginRequest(ContextProvider.getApplicationContext(), new TaskCallBack() {
            @Override
            public void callback(int code, String msg, String errorCode, Object object) {
                Logger.i(TAG, "tokenLogin callback");

                if (code != TaskCallBack.CODE_OK) {
                    Logger.e(TAG, "tokenLogin callback != CODE_OK, errorCode=" + errorCode
                            + ", msg=" + msg);
                    if (errorCode != null
                            && errorCode.equals(LoginConstants.LOGIN_NET_ERROR_TOKEN_INVALIDATE)) {
                        // TODO(qingxia): Notify user login state is
                        // error
                    }

                    LetvLoginModel.this.setLoginState(LoginState.LOGIN_STATE_UNCHECKED_LOGIN);
                    LetvLoginModel.this.callAsynRequestError(listener, errorCode, msg);
                    return;
                }

                if (!(object instanceof CommonResponse)) {
                    LetvLoginModel.this.setLoginState(LoginState.LOGIN_STATE_UNCHECKED_LOGIN);
                    LetvLoginModel.this.callAsynRequestError(listener,
                            LoginConstants.LOGIN_NET_ERROR_DATA_INVALIDATE, "");
                    return;
                }

                @SuppressWarnings("unchecked")
                CommonResponse<UserInfo> data = (CommonResponse<UserInfo>) object;

                if (data.getData() instanceof UserInfo) {
                    Logger.i(TAG, "TokenLoginRequest UserInfo = " + data.getData().toString());
                    UserInfo userInfo = data.getData();
                    LetvLoginModel.this.setUserInfo(userInfo);
                    LetvLoginModel.this.getMemberInfoFromServer(userInfo.getUid(),
                            LetvLoginModel.this.getToken(), null);
                    LetvLoginModel.this.setLoginState(LoginState.LOGIN_STATE_LOGIN);
                    LetvLoginModel.this.callAsynRequestSuccess(listener);
                } else {
                    LetvLoginModel.this.setLoginState(LoginState.LOGIN_STATE_UNCHECKED_LOGIN);
                    LetvLoginModel.this.callAsynRequestError(listener,
                            LoginConstants.LOGIN_NET_ERROR_DATA_INVALIDATE, "");
                }
            }

        }).execute(new TokenLoginParameter(LoginModel.getToken()).combineParams());
    }

    /**
     * Get member information.
     * @param uid
     * @param token
     */
    public void getMemberInfoFromServer(String uid, String token, final AsynRequestListener listener) {
        if (StringUtils.isStringEmpty(token) || StringUtils.isStringEmpty(uid)) {
            Logger.e(TAG, "getMemberInfo paramter error uid = " + uid + " token = " + token);
            return;
        }

        new GetAccountInfoRequest(ContextProvider.getApplicationContext(), new TaskCallBack() {

            @Override
            public void callback(int code, String msg, String errorCode, Object object) {
                Logger.i(TAG, "GetAccountInfoRequest callback");

                if (code != TaskCallBack.CODE_OK) {
                    Logger.e(TAG, "GetAccountInfoRequest callback != CODE_OK");
                    LetvLoginModel.this.callAsynRequestError(listener, errorCode, msg);
                    return;
                }

                if (!(object instanceof CommonResponse)) {
                    LetvLoginModel.this.callAsynRequestError(listener,
                            LoginConstants.LOGIN_NET_ERROR_DATA_INVALIDATE, "");
                    return;
                }

                @SuppressWarnings("unchecked")
                CommonResponse<MemberInfo> data = (CommonResponse<MemberInfo>) object;

                if (data.getData() instanceof MemberInfo) {
                    Logger.i(TAG, "GetAccountInfoRequest MemberInfo = " + data.getData().toString());
                    LetvLoginModel.this.updateMemberState(data.getData());
                    LetvLoginModel.this.callAsynRequestSuccess(listener);
                } else {
                    LetvLoginModel.this.callAsynRequestError(listener,
                            LoginConstants.LOGIN_NET_ERROR_DATA_INVALIDATE, "");
                }
            }

        }).execute(new GetAccountInfoParameter(uid, token).combineParams());
    }

    /**
     * This function will return "" while login state is no login or no init.
     * @return
     */
    public String getToken() {
        return this.mLoginToken;
    }

    /**
     * Do system login.
     */
    public void login(Activity activity, AccountManagerCallback<Bundle> callback) {
        systemLogin(activity, callback);
    }

    /**
     * Do system logout.
     * @param activity
     */
    public void logout(Activity activity, AccountManagerCallback<Bundle> callback) {
        systemLogin(activity, callback);
    }

    public void saveOrUpdateLocalUserInfo() {
        // TODO(shibin) 存储用户信息的个数 是否本地存储会员信息
        SharedPreferencesManager.saveSerializable(this.getToken(), this.getUserInfo());
    }

    public UserInfo getLocalUserInfo() {
        UserInfo userInfo = null;
        if (!StringUtils.isStringEmpty(this.getToken())) {
            userInfo = SharedPreferencesManager.getSerializable(this.getToken(), new UserInfo());
        }

        return userInfo;
    }

    public String getVipValidDate(MemberState memberType) {
        String validDate = "";
        if (this.mMemberInfo == null) {
            return validDate;
        }
        if (memberType == MemberState.MEMBER_STATE_SENIOR_VIP
                && !StringUtils.isStringEmpty(this.mMemberInfo.getCancelTime())) {
            try {
                validDate = TimeUtils.timeToString(new Date(Long.valueOf(this.mMemberInfo
                        .getSeniorCancelTime())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (memberType == MemberState.MEMBER_STATE_ORDINARY_VIP
                && !StringUtils.isStringEmpty(this.mMemberInfo.getCancelTime())) {
            try {
                validDate = TimeUtils.timeToString(new Date(Long.valueOf(this.mMemberInfo
                        .getCancelTime())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return validDate;
    }

    public void addLoginStateObersvers(Observer observer) {
        this.mUserStateObservable.addObserver(observer);
    }

    public void deleteLoginStateObservers(Observer observer) {
        this.mUserStateObservable.deleteObserver(observer);
    }

    public void addMemberStateObersvers(Observer observer) {
        this.mMemberStateObservable.addObserver(observer);
    }

    public void deleteMemberStateObservers(Observer observer) {
        this.mMemberStateObservable.deleteObserver(observer);
    }
}
