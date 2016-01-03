/**
 *
 * Copyright 2015 LeTV Technology Co. Ltd., Inc. All rights reserved.
 *
 * @Author : qingxia
 *
 * @Description :
 *
 */

package com.letv.mobile.loginModel;

import java.util.Observer;

import android.accounts.AccountManagerCallback;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.letv.mobile.common.AsynRequestListener;
import com.letv.mobile.core.log.Logger;
import com.letv.mobile.core.utils.ContextProvider;
import com.letv.mobile.core.utils.DeviceUtils;
import com.letv.mobile.login.login.DeviceBindModel;
import com.letv.mobile.login.login.LetvLoginModel;
import com.letv.mobile.login.login.LoginState;
import com.letv.mobile.login.login.MemberState;
import com.letv.mobile.login.model.MemberInfo;
import com.letv.mobile.login.model.UserInfo;

/**
 * This class provide a common interface for all login model functions.
 * This class can not be extends.
 * @author xiaqing
 */
final public class LoginModel {
    private final static String TAG = "LoginModel";

    private static LetvLoginModel sLoginModel;
    private static DeviceBindModel sDeviceBindModel;
    private static String sTerminalApplicationName = "";
    private static String sBsChannel = "_";
    private static boolean isNeedAutoLogin = false;

    // NOTE(qingxia): We do not need user to new this Class.
    private LoginModel() {
    }

    static {
        if (LetvLoginModel.hasLetvAuthenticator(ContextProvider
                .getApplicationContext())) {
            sLoginModel = new LetvLoginModel();
        } else {
            // TODO(qingxia): Delete later.
            sLoginModel = new LetvLoginModel();
            Logger.e(TAG, "We do not support no letv devices login model");
        }
        sDeviceBindModel = new DeviceBindModel();
    }

    /**
     * This class should be called while in application onCreate()
     */
    public static void init(String terminalApplication, String bsChannel,
            boolean needAutoLogin) {
        Logger.i(TAG, "LoginModel init");
        sTerminalApplicationName = terminalApplication;
        isNeedAutoLogin = needAutoLogin;
        sBsChannel = bsChannel;
    }

    public static String getTerminalApplicationName() {
        return sTerminalApplicationName;
    }

    public static String getBsChannel() {
        return sBsChannel;
    }

    public static boolean isNeedAutoLogin() {
        return isNeedAutoLogin;
    }

    /**
     * If user is login.
     * @return
     */
    public static boolean isLogin() {
        LoginState state = getLoginState();
        return state == LoginState.LOGIN_STATE_UNCHECKED_LOGIN
                || state == LoginState.LOGIN_STATE_LOGIN;
    }

    /**
     * If user is vip.
     * @return
     */
    public static boolean isVip() {
        MemberState state = getMemberState();
        return state == MemberState.MEMBER_STATE_ORDINARY_VIP
                || state == MemberState.MEMBER_STATE_SENIOR_VIP;
    }

    /**
     * Do login, start login view.
     * @param activity
     *            Can be null
     * @param callback
     *            Can be null
     */
    public static void login(Activity activity,
            AccountManagerCallback<Bundle> callback) {
        sLoginModel.login(activity, callback);
    }

    /**
     * Do logout, start logout view.
     * @param activity
     *            Can be null
     * @param callback
     *            Can be null
     */
    public static void logout(Activity activity,
            AccountManagerCallback<Bundle> callback) {
        sLoginModel.logout(activity, callback);
    }

    /**
     * Do login check.
     * @param listener
     */
    static void tokenLogin(AsynRequestListener listener) {
        sLoginModel.tokenLogin(listener);
    }

    /**
     * Get member info from server.
     * @param uid
     * @param token
     */
    static public void getMemberInfoFromServer(String uid, String token,
            AsynRequestListener listener) {
        sLoginModel.getMemberInfoFromServer(uid, token, listener);
    }

    /**
     * Get current user info.
     * @return
     */
    static public UserInfo getUserInfo() {
        return sLoginModel.getUserInfo();
    }

    /**
     * Get current member info.
     * @return
     *         null
     *         member information.
     */
    static public MemberInfo getMemberInfo() {
        return sLoginModel.getMemberInfo();
    }

    /**
     * This function will return "" while login state is no login or no init.
     * @return
     */
    public static String getToken() {
        return sLoginModel.getToken();
    }

    /**
     * Get login state.
     * @return
     */
    public static LoginState getLoginState() {
        return sLoginModel.getLoginState();
    }

    public static MemberState getMemberState() {
        return sLoginModel.getMemberState();
    }

    public static void addLoginStateObersvers(Observer observer) {
        sLoginModel.addLoginStateObersvers(observer);
    }

    public static void deleteLoginStateObservers(Observer observer) {
        sLoginModel.deleteLoginStateObservers(observer);
    }

    public static void addMemberStateObersvers(Observer observer) {
        sLoginModel.addMemberStateObersvers(observer);
    }

    public static void deleteMemberStateObservers(Observer observer) {
        sLoginModel.deleteMemberStateObservers(observer);
    }

    public static void addBindStateObservers(Observer observer) {
        sDeviceBindModel.addBindStateObserver(observer);
    }

    public static void deleteBindStateObservers(Observer observer) {
        sDeviceBindModel.deleteBindStateObserver(observer);
    }

    /**
     * Get User name.
     * @return
     */
    public static String getUserName() {
        UserInfo userInfo = sLoginModel.getUserInfo();
        if (userInfo != null) {
            return userInfo.getUsername();
        }

        return "";
    }

    /**
     * Get UID.
     * @return
     */
    public static String getUID() {
        UserInfo userInfo = sLoginModel.getUserInfo();
        if (userInfo != null) {
            return userInfo.getUid();
        }

        return "";
    }

    /**
     * Get Nick name.
     * @return
     */
    public static String getNickName() {
        UserInfo userInfo = sLoginModel.getUserInfo();
        if (userInfo != null) {
            return userInfo.getNickName();
        }
        return "";
    }

    /**
     * Get Head Picture.
     * @return pic url
     */
    public static String getHeadPicture() {
        UserInfo userInfo = sLoginModel.getUserInfo();
        if (userInfo != null) {
            return userInfo.getPicture();
        }
        return "";
    }

    /**
     * Get Vip type name
     * @return
     */
    public static String getVipTypeName() {
        MemberInfo memberInfo = sLoginModel.getMemberInfo();
        if (memberInfo != null) {
            return memberInfo.getVipTypeName();
        }
        return "";
    }

    /**
     * Get vip ordinary valid date
     */
    public static String getOrdinaryValidDate() {
        MemberInfo memberInfo = sLoginModel.getMemberInfo();
        if (memberInfo != null) {
            return memberInfo.getCancelTime();
        }
        return "";
    }

    /**
     * Get vip senior valid date
     */
    public static String getSeniorValidDate() {
        MemberInfo memberInfo = sLoginModel.getMemberInfo();
        if (memberInfo != null) {
            return memberInfo.getSeniorCancelTime();
        }
        return "";
    }

    public static void getDeviceBindInfo(AsynRequestListener listener) {
        sDeviceBindModel.getDeviceBindInfo(listener);
    }

    /**
     * Do login, start login view.
     */
    public static void bind() {
        Log.i(TAG, "deviceKey=" + DeviceUtils.getDeviceId());
        sDeviceBindModel.bind();
    }

}
