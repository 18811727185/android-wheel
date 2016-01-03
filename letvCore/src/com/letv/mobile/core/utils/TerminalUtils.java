package com.letv.mobile.core.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.text.TextUtils;

public class TerminalUtils {

    public static final String TERMINALUUID = "terminaluuid";
    public static final String REG = "reg";
    public static final String RESET_PWD = "reset_pwd";

    public static final String IDENTIFY_CODE = "identify_code";
    /**
     * 产品渠道号，第三方使用，打包前产品确认渠道号。
     * 默认（乐视）："_"
     * 预置：
     * TCL：pre_tcl_update
     * 创维：pre_skyworth_update
     * 康佳：pre_konka_update
     * 海信：pre_hisense_1403
     * 海尔：pre_haier_1403
     * 商店：
     * 海信：pre_hisense_store01
     * 海尔：pre_haier_store01
     */
    public final static String BsChannel = "_";
    /**
     * 终端品牌：小写英文
     * 乐视：letv
     * 康佳：konka
     * 海信：hisense
     * 海尔：haier
     */
    public final static String TERMINAL_BRAND = "letv";
    // 海信厂商名称
    public final static String TERMINAL_BRAND_HISENSE = "hisense";

    public static final String CLIENT = "android";

    public static final String APPLICATIONID = "1001";// android+TV版(1,001)

    /* 播控平台 */
    public static final String CNTV = "0";// 中国网络电视台
    public static final String GUOGUANG = "2";// 中国人民广播电台
    public static final String WASHU = "3";// 华数传媒

    private static String strIndentifyCode = "";
    private static String strTerminalUUID = "";
    private static String reg = "";
    private static String resetPwd = "";

    /**
     * 获取终端identifyCode
     */
    public static String getTerminalIdentifyCode() {
        if (StringUtils.isBlank(strIndentifyCode)) {
            strIndentifyCode = SharedPreferencesManager.getString(
                    IDENTIFY_CODE, "");
        }
        return strIndentifyCode;
    }

    /**
     * 保存终端identifyCode
     */
    public static void setTerminalIdentifyCode(final String indetifyCode) {
        SharedPreferencesManager.putString(IDENTIFY_CODE, indetifyCode);
        strIndentifyCode = indetifyCode;
    }

    /**
     * 获取终端_terminalUUid
     */
    public static String getTerminalUUID() {
        if (StringUtils.isBlank(strTerminalUUID)) {
            strTerminalUUID = SharedPreferencesManager.getString(TERMINALUUID,
                    "");
        }
        return strTerminalUUID;
    }

    /**
     * 保存终端_terminalUUid
     */
    public static void setTerminalUUID(final String terminalUUID) {
        strTerminalUUID = terminalUUID;
        SharedPreferencesManager.putString(TERMINALUUID, terminalUUID);
    }

    /**
     * 保存注册电话号码
     */
    public static void setReg(String reg) {
        TerminalUtils.reg = reg;
        SharedPreferencesManager.putString(REG, reg);
    }

    /**
     * 获取注册电话号码
     */
    public static String getReg() {
        if (StringUtils.isBlank(reg)) {
            reg = SharedPreferencesManager.getString(REG, "");
        }
        return reg;
    }

    /**
     * 保存找回密码电话号码
     */
    public static void setResetPwd(String resetPwd) {
        TerminalUtils.resetPwd = resetPwd;
        SharedPreferencesManager.putString(RESET_PWD, resetPwd);
    }

    /**
     * 获取找回密码电话号码
     */
    public static String getResetPwd() {
        if (StringUtils.isBlank(resetPwd)) {
            resetPwd = SharedPreferencesManager.getString(RESET_PWD, "");
        }
        return resetPwd;
    }

    /**
     * 获取终端_terminalBrand品牌
     */
    public static String getTerminalBrand() {
        if (DeviceUtils.isOtherDevice()) {
            String brand;
            try {
                brand = android.os.Build.BRAND;
                if (TextUtils.isEmpty(brand)) {
                    Class<android.os.Build> build_class = android.os.Build.class;
                    java.lang.reflect.Field field;
                    field = build_class.getField("BRAND");
                    brand = (String) field.get(new android.os.Build());
                }
            } catch (Exception e) {
                e.printStackTrace();
                brand = "-";
            }
            return brand;
        } else {
            return TERMINAL_BRAND;
        }
    }

    /**
     * 从设备属性中获取品牌
     */
    public static String getTerminalBrandFromDevice() {
        String brand;
        try {
            brand = android.os.Build.BRAND;
            if (TextUtils.isEmpty(brand)) {
                Class<android.os.Build> build_class = android.os.Build.class;
                java.lang.reflect.Field field;
                field = build_class.getField("BRAND");
                brand = (String) field.get(new android.os.Build());
            }
        } catch (Exception e) {
            e.printStackTrace();
            brand = "-";
        }
        return brand;
    }

    /**
     * 需要和服务器端加密算法一样
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    public static String getClientUUID(String tBrand, String tSeries,
            String tUnique) {
        String uuid = null;
        try {
            uuid = StringUtils.getUUIDString(tBrand, tSeries, tUnique, 4, "0"); // 服务端同参数生成的唯一id号
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uuid;
    }

    /**
     * 获取商务渠道号
     * @return
     */
    public static String getBsChannel() {
        try {
            return URLEncoder.encode(BsChannel, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "_";
    }

    /*
     * 获取版本名称
     * @return version name
     */
    public static String getAppVersionName(Context ctx) {
        try {
            PackageInfo packInfo = ctx.getPackageManager().getPackageInfo(
                    ctx.getPackageName(), 0);
            return packInfo.versionName;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
