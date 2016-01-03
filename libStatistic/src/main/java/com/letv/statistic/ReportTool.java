package com.letv.statistic;

import com.android.letvmanager.LetvManager;
import com.letv.mobile.core.time.TimeProvider;
import com.letv.mobile.core.utils.ContextProvider;
import com.letv.mobile.core.utils.DeviceUtils;
import com.letv.mobile.core.utils.MD5Util;
import com.letv.mobile.core.utils.SystemUtil;
import com.letv.mobile.loginModel.LoginModel;
import com.letv.statistic.http.ActionParameter;
import com.letv.statistic.http.ActionRequest;
import com.letv.statistic.http.EnvParameter;
import com.letv.statistic.http.EnvRequest;
import com.letv.statistic.http.ErrorParameter;
import com.letv.statistic.http.ErrorRequest;
import com.letv.statistic.http.LoginParameter;
import com.letv.statistic.http.LoginRequest;
import com.letv.statistic.http.PlayParameter;
import com.letv.statistic.http.PlayRequest;
import com.letv.statistic.model.ActionDataModel;
import com.letv.statistic.model.EnvDataModel;
import com.letv.statistic.model.ErrorDataModel;
import com.letv.statistic.model.LoginDataModel;
import com.letv.statistic.model.PlayDataModel;

import android.app.Service;
import android.net.wifi.WifiManager;

/**
 * @author lilong
 *         上报工具类，单例
 *         注意：静态方法返回的字段值，如果没有，则有可能是空字符串或者null
 */
public class ReportTool {

    private static volatile ReportTool sReportTool;

    private static Boolean IsFirstPlay = false;

    public static ReportTool getInstance() {
        if (sReportTool == null) {
            synchronized (ReportTool.class) {
                if (sReportTool == null) {
                    sReportTool = new ReportTool();
                }
            }
        }
        return sReportTool;
    }

    private ReportTool() {
    }

    /**
     * action上报
     * @param model
     */
    public void reportAction(ActionDataModel model) {

        if (model == null) {
            return;
        }

        ActionParameter parameter = new ActionParameter();
        parameter.setAcode(model.getAcode());
        parameter.setCur_url(model.getCur_url());
        parameter.setAr(model.getAr());
        parameter.setAp(model.getAp());
        parameter.setCid(model.getCid());
        parameter.setVid(model.getVid());
        parameter.setUid(getUid());
        parameter.setLid(model.getLid());
        parameter.setUuid(getUuid());
        parameter.setAuid(getAuid());
        parameter.setIlu(getIlu());
        parameter.setCtime(getCtime());
        parameter.setSt(model.getSt());
        new ActionRequest(ContextProvider.getApplicationContext(), Constants.HTTP_CONNECT_TIMEOUT,
                Constants.HTTP_READ_TIMEOUT, Constants.REPORT_DOMAIN, Constants.REPORT_ACTION_PATH)
                .execute(parameter.combineParams());
    }

    /**
     * 上报env
     * @param model
     */
    public void reportEnv(EnvDataModel model) {

        if (model == null) {
            return;
        }

        EnvParameter parameter = new EnvParameter();
        parameter.setUuid(getUuid());
        parameter.setIp(getIP());
        parameter.setNt(getNt());
        parameter.setOs(getOs());
        parameter.setOsv(getOsv());
        parameter.setApp(getApp());
        parameter.setBd(getBd());
        parameter.setXh(getXh());
        parameter.setSrc(getSrc());
        parameter.setSsid(getSsid());
        parameter.setApprunid(getApprunid());
        parameter.setCtime(getCtime());
        new EnvRequest(ContextProvider.getApplicationContext(), Constants.HTTP_CONNECT_TIMEOUT,
                Constants.HTTP_READ_TIMEOUT, Constants.REPORT_DOMAIN, Constants.REPORT_ENV_PATH)
                .execute(parameter.combineParams());
    }

    /**
     * play上报
     * @param model
     */
    public void reportPlay(PlayDataModel model) {

        if (model == null) {
            return;
        }

        PlayParameter parameter = new PlayParameter();
        parameter.setAc(model.getAc());
        parameter.setPt(model.getPt());
        parameter.setUid(getUid());
        parameter.setAuid(getAuid());
        parameter.setUuid(model.getUuid());
        parameter.setCid(model.getCid());
        parameter.setTy(model.getTy());
        parameter.setVt(model.getVt());
        parameter.setRef(model.getRef());
        parameter.setPv(getPv());
        parameter.setSt(model.getSt());
        parameter.setIlu(getIlu());
        parameter.setLid(model.getLid());
        parameter.setCtime(getCtime());
        parameter.setVid(model.getVid());
        parameter.setIpt(model.getIpt());

        // ac = init时上报
        if (Constants.AC_INIT.equals(model.getAc())) {
            parameter.setCdev(model.getCdev());
            parameter.setCaid(model.getCaid());
        }
        // ac = play时上报
        else if (Constants.AC_PLAY.equals(model.getAc())) {
            parameter.setPay(model.getPay());
        }

        new PlayRequest(ContextProvider.getApplicationContext(), Constants.HTTP_CONNECT_TIMEOUT,
                Constants.HTTP_READ_TIMEOUT, Constants.REPORT_DOMAIN, Constants.REPORT_PLAY_PATH)
                .execute(parameter.combineParams());
    }

    /**
     * 上报login
     */
    public void reportLogin(LoginDataModel model) {

        if (model == null) {
            return;
        }

        LoginParameter parameter = new LoginParameter();
        parameter.setUid(getUid());
        parameter.setAuid(getAuid());
        parameter.setUuid(getUuid());
        parameter.setLp(getLp());
        parameter.setRef(model.getRef());
        parameter.setTs(model.getTs());
        parameter.setSt(model.getSt());

        new LoginRequest(ContextProvider.getApplicationContext(), Constants.HTTP_CONNECT_TIMEOUT,
                Constants.HTTP_READ_TIMEOUT, Constants.REPORT_DOMAIN, Constants.REPORT_LOGIN_PATH)
                .execute(parameter.combineParams());
    }

    /**
     * error上报
     */
    public void reportError(ErrorDataModel model) {

        if (model == null) {
            return;
        }

        ErrorParameter parameter = new ErrorParameter();
        parameter.setEt(model.getEt());
        parameter.setErr(model.getErr());
        parameter.setAuid(getAuid());
        parameter.setNt(getNt());
        parameter.setEp(model.getEp());
        parameter.setCid(model.getCid());
        parameter.setVid(model.getVid());
        parameter.setUuid(model.getUuid());
        parameter.setApprunid(getApprunid());
        parameter.setLid(model.getLid());
        parameter.setSt(model.getSt());

        new ErrorRequest(ContextProvider.getApplicationContext(), Constants.HTTP_CONNECT_TIMEOUT,
                Constants.HTTP_READ_TIMEOUT, Constants.REPORT_DOMAIN, Constants.REPORT_ERROR_PATH)
                .execute(parameter.combineParams());
    }

    /** 日志版本号 */
    public static final String getVer() {
        return Constants.VER;
    }

    /**
     * 一级业务线代码
     */
    public static final String getP1() {
        return Constants.P1;
    }

    /**
     * 二级业务线代码
     */
    public static final String getP2() {
        return Constants.P2;
    }

    /**
     * 三级业务线代码
     */
    public static final String getP3() {
        return Constants.P3;
    }

    /**
     * 用户注册id
     */
    public static final String getUid() {
        return LoginModel.isLogin() ? LoginModel.getUID() : "";
    }

    /**
     * 设备ID_当前时间戳_０ , 播放器的话０可能是其它数字
     */
    public static final String getUuid() {
        return getUuid(0);
    }

    /**
     * 设备ID_当前时间戳_数字 , 播放器的话切换一次码率数字加一
     */
    public static final String getUuid(int suffix) {
        return getMacWithoutColon() + System.currentTimeMillis() + "_" + suffix;
    }

    /**
     * 播放器版本，如无播放器版本，则报应用版本
     */
    public static final String getPv() {
        return SystemUtil.getVersionName(ContextProvider.getApplicationContext());
    }

    /**
     * MAC明文地址
     */
    public static final String getAuid() {
        return getMacWithoutColon();
    }

    /**
     * ０：登录用户，１：非登录用户
     */
    public static final String getIlu() {
        return LoginModel.isLogin() ? "0" : "1";
    }

    /**
     * MAC明文地址，带冒号
     */
    public static final String getMacWithColon() {
        String mac = SystemUtil.getMacAddress();
        if (mac != null && mac.length() == 12) {
            return mac.substring(0, 2) + ":" + mac.substring(2, 4) + ":" + mac.substring(4, 6)
                    + ":" + mac.substring(6, 8) + ":" + mac.substring(8, 10) + ":"
                    + mac.substring(10, 12);
        } else {
            return "";
        }
    }

    /**
     * MAC明文地址，不带冒号
     */
    public static final String getMacWithoutColon() {
        return SystemUtil.getMacAddress();
    }

    /**
     * IP地址
     */
    public static final String getIP() {
        return SystemUtil.getLocalIpAddress();
    }

    /**
     * wifi/wired,上网类型
     */
    public static final String getNt() {
        return SystemUtil.getNetType(ContextProvider.getApplicationContext());
    }

    /**
     * 操作系统(android)
     */
    public static final String getOs() {
        return "Android";
    }

    /**
     * 操作系统版本号
     */
    public static final String getOsv() {
        return SystemUtil.getOSVersion();
    }

    /**
     * 版本号字符串
     */
    public static final String getApp() {
        return SystemUtil.getVersionName(ContextProvider.getApplicationContext());
    }

    /**
     * bd=letv(AppConfig.getTerminalBrand())
     */
    public static final String getBd() {
        return "letv";
    }

    /**
     * 机器型号
     */
    public static final String getXh() {
        return DeviceUtils.getTerminalSeries();
    }

    /** 环境上报时用，表示某个环境上报所跟随的时机 , 这里是固定值pl */
    public static final String getSrc() {
        return "pl";
    }

    /**
     * wifi标识，上报无线名字，例如letvoffice，有线连接时不上报此字段
     */
    public static final String getSsid() {
        WifiManager wifiManager = (WifiManager) ContextProvider.getApplicationContext()
                .getSystemService(Service.WIFI_SERVICE);
        return wifiManager.getConnectionInfo().getSSID();
    }

    /**
     * 伴随环境日志上报，用来标tv版的启动，一次进入TV版过程中，上报只上报唯一值，退出tv版再进入重新生成
     */
    public static final String getApprunid() {
        return getMacWithoutColon() + "_" + System.currentTimeMillis();
    }

    /**
     * 表示主动登录，固定值
     */
    public static final String getLp() {
        return "initative_login";
    }

    /**
     * 与Lc一致
     */
    public static final String getCUID() {
        return MD5Util.MD5(LetvManager.getID());
    }

    /**
     * 与CUID一致
     */
    public static final String getLc() {
        return MD5Util.MD5(LetvManager.getID());
    }

    /**
     * 时间戳，毫秒
     */
    public static final Long getR() {
        return TimeProvider.getCurrentMillisecondTime();
    }

    /**
     * 时间戳，毫秒
     */
    public static final Long getCtime() {
        return TimeProvider.getCurrentMillisecondTime();
    }

    // Action上报
    public void reportAction(String acode, String cur_url, String ar, String ap, String cid,
            String vid, String lid, String st) {
        ActionDataModel model = new ActionDataModel();
        model.setAcode(acode);
        model.setCur_url(cur_url);
        model.setAr(ar);
        model.setAp(ap);
        model.setCid(cid);
        model.setVid(vid);
        model.setLid(lid);
        model.setSt(st);
        ReportTool.getInstance().reportAction(model);
    }

    public static Boolean getIsFirstPlay() {
        return IsFirstPlay;
    }

    public static void setIsFirstPlay(Boolean isFirstPlay) {
        IsFirstPlay = isFirstPlay;
    }

}
