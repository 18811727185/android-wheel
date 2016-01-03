package com.letv.mobile.core.utils;

import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;
import android.telephony.TelephonyManager;

import com.android.letvmanager.LetvManager;
import com.letv.mobile.core.config.DeviceConfig;
import com.letv.mobile.core.log.Logger;
import com.letv.tv.core.device.DeviceInfo;

import java.util.Locale;

//import com.letv.leui.os.phonebind.IPhoneBind;

/**
 * 设备工具类
 * @author LETV
 */
public class DeviceUtils {

    private static Logger sLogger = new Logger("DeviceUtils");

    /**
     * 获取设备名称型号
     */
    public static String getTerminalSeries() {
        return android.os.Build.MODEL;
    }

    private static final String LETV_BRAND_NAME = "Letv";

    /** 是否第三方设备 */
    //public static boolean isOtherDevice() {
        //return !isLetvDevice();
    //}

    /** 是否自有设备 */
    public static boolean isLetvDevice() {
        return LETV_BRAND_NAME.equalsIgnoreCase(android.os.Build.BRAND);
    }

    /**
     * 获取设备ID，先取IMEI，获取不到取mac地址的MD5值作为设备id
     */
    public static String getDeviceId() {
        if (ContextProvider.getApplicationContext() == null) {
            return "";
        }
        TelephonyManager telephonyManager = (TelephonyManager) ContextProvider
                .getApplicationContext().getSystemService(
                        Context.TELEPHONY_SERVICE);
        String deviceId = telephonyManager.getDeviceId();

        if (deviceId != null && deviceId.length() > 0) {
            return deviceId;
        }

        String mac = SystemUtil.getMacAddress();
        if (StringUtils.equalsNull(mac)) {
            return "";
        }
        return MD5Util.MD5(mac);
    }

    // 获取device key
    public static String getDeviceKey() {
        IBinder binder = ServiceManager.getService("leuiphonebind");
        if (binder == null) {
            sLogger.d("leuiphonebind binder is null");
            return null;
        }
        String bindKey = null;
        try {
            //IPhoneBind phoneBind = IPhoneBind.Stub.asInterface(binder);
           // bindKey = phoneBind.getLeTVSNValue("leui_phone_bind_key");
            sLogger.d("bindKey is[" + bindKey + "]");
        } catch (Exception e) {
            sLogger.e("getPhoneBind error: " + e.getMessage());
            e.printStackTrace();
        }
        return bindKey;
    }

    public static final String TYPE_X60 = "x60";
    public static final String TYPE_S50 = "s50";
    public static final String TYPE_S40 = "s40";
    public static final String TYPE_C1 = "c1";
    public static final String TYPE_C1S = "c1s";
    public static final String TYPE_C1A = "c1a";
    public static final String TYPE_C1B = "c1b";
    public static final String TYPE_T1S = "t1s";
    public static final String TYPE_NEWC1S = "newc1s";
    public static final String TYPE_C2 = "c2";
    public static final String TYPE_OTHER = "other";
    public static final String TYPE_MAX70 = "max70";
    public static final String TYPE_S250F = "s250f";
    public static final String TYPE_S250U = "s250u";
    public static final String TYPE_S255U = "s255u";// X55 Air
    public static final String TYPE_X365 = "max365";// X65 Air
    public static final String TYPE_U2 = "u2";// U2
    public static final String TYPE_U3 = "u3";// U3
    public static final String TYPE_G1 = "g1";// G1
    public static final String TYPE_RECOVERY = "0radixROM";// 刷机版本
    public static final String TYPE_S240F = "s240f";// X40Air
    public static final String TYPE_S243F = "s243f";// X43Air
    public static final String TYPE_GS39 = "gs39";// 国美渠道
    public static final String TYPE_MXYTV = "mxytv";// 茂鑫源
    public static final String TYPE_SHM6 = "shm6";// 第三方SHM6
    public static enum DeviceType {
        DEVICE_X60(TYPE_X60),
        DEVICE_S50(TYPE_S50),
        DEVICE_S40(TYPE_S40),
        DEVICE_C1(TYPE_C1),
        DEVICE_C1S(TYPE_C1S),
        DEVICE_OTHER(TYPE_OTHER),
        DEVICE_MAX70(TYPE_MAX70),
        DEVICE_S250F(TYPE_S250F),
        DEVICE_S250U(TYPE_S250U),
        DEVICE_NEWC1S(TYPE_NEWC1S),
        DEVICE_C2(TYPE_C2),
        DEVICE_T1S(TYPE_T1S),
        DEVICE_S240F(TYPE_S240F),
        DEVICE_MXYTV(TYPE_MXYTV),
        DEVICE_GS39(TYPE_GS39),
        DEVICE_RECOVERY(TYPE_RECOVERY),
        DEVICE_S255U(TYPE_S255U),
        DEVICE_X365(TYPE_X365),
        DEVICE_U2(TYPE_U2),
        DEVICE_G1(TYPE_G1);
        final String mDeviceType;

        DeviceType(String deviceType) {
            this.mDeviceType = deviceType;
        }

        public String getDeviceType() {
            return this.mDeviceType;
        }

        /**
         * 是否是乐视盒子
         * @param deviceType
         * @return
         */
        public static boolean isLetvBox(DeviceType deviceType) {
            return deviceType == DEVICE_C1 || deviceType == DEVICE_C1S
                    || deviceType == DEVICE_NEWC1S || deviceType == DEVICE_C2
                    || deviceType == DEVICE_T1S || deviceType == DEVICE_U2
                    || deviceType == DEVICE_G1;
        }

        public static DeviceType getDeviceTypeByString(String type) {

            // for the older version, type is Upper case. so change it to lower
            // case.
            String lowerType = type.toLowerCase();

            DeviceType[] typeArray = DeviceType.values();

            for (DeviceType deviceType : typeArray) {
                if (deviceType.getDeviceType().equals(lowerType)) {
                    return deviceType;
                }
            }

            return DEVICE_OTHER;
        }

    }
    private static DeviceType mDeviceType = null;
    private static DeviceInfo mDevice = null;

    /** 是否第三方设备 */
    public static boolean isOtherDevice() {
        return DeviceUtils.getDevice().getName().equals(DeviceType.DEVICE_OTHER)
                && DeviceUtils.getUIVersion() == DeviceUtils.UIVersion.UIVERSION_OTHER;
    }


    /**
     * the new version get the Device from this function
     * and return an object of Device
     * @return
     */
    public static DeviceInfo getDevice() {
        if (mDevice == null) {
            mDevice = getDevice(getDeviceTypeTextInternal());
        }
        return mDevice;
    }

    /**
     * get an object of Device according to type
     * @param type
     * @return
     */
    private static DeviceInfo getDevice(String type) {

        return DeviceConfig.getDeviceByType(type);
    }


    /**
     * the old version get the DeviceType from this function
     * and return an object of Enum
     * @return
     */
    public static DeviceType getDeviceType() {
        if (mDeviceType == null) {
            mDeviceType = getDeviceType(getDeviceTypeTextInternal());
        }
        return mDeviceType;
    }
    private static final String ZERO = "0";
    private static String strDeviceType = ZERO;// save device type
    /*
    * get DeviceType according to type
    * @author caoxianjin
    * @param sp
    * SharedPreferences
    * @param type
    * get from system command(UI2.0&UI2.3) or LetvManager(UI3.0)
    * @return DeviceType
    */
    private static DeviceType getDeviceType(String type) {
        if (StringUtils.isBlank(type)) {
            strDeviceType = TYPE_OTHER;
            return DeviceType.DEVICE_OTHER;
        }
        if (type != null) {
            type = type.toLowerCase(Locale.getDefault());
            // add this judgement because we find one device called X60T is X60
            // with DTMB, in the future there is another device called X60S
            if (type.contains(TYPE_X60)) {
                type = TYPE_X60;
            } else if (type.contains(TYPE_S50)) {
                type = TYPE_S50;
            } else if (type.contains(TYPE_S40)) {
                type = TYPE_S40;
            } else if (type.contains(TYPE_S243F)) {
                type = TYPE_S240F;
            } else if (type.contains(TYPE_U3)) {
                type = TYPE_U2;
            } else if (type.contains(TYPE_MAX70)) {
                type = TYPE_MAX70;
            } else if (type.contains(TYPE_C1A) || type.contains(TYPE_C1B)) {
                type = TYPE_C1S;
            } else if (type.contains(TYPE_SHM6)) {
                type = TYPE_NEWC1S;
            }
        }
       // if (isLogInnerType) {
           // isLogInnerType = false;
            //logger.i("get from system command, type:" + type);
        //}
        return DeviceType.getDeviceTypeByString(type);
    }
    private static final String LETVPRODUCTNAME = "ro.letv.product.name";
    private static String getDeviceTypeTextInternal() {
        String type = null;
        if (isUI30orHigher()) {
            try {
                type = LetvManager.getLetvModel();
            } catch (Throwable e) {
                sLogger.e("LetvManager getLetvModel fucntion is not supported.");
            }
        }
        if (StringUtils.equalsNull(type)) {
            type = SystemUtil.getSystemProperty(LETVPRODUCTNAME);
        }
        sLogger.i("getDeviceTypeTextInternal: " + type);
        return type;
    }

    /**
     * 是否是ui3.0或者更高ui版本
     * UIVERSION_30 UIVERSION_50 都为true
     */
    public static boolean isUI30orHigher() {
        UIVersion uiVersion = getUIVersion();
        if (uiVersion == UIVersion.UIVERSION_30
                || uiVersion == UIVersion.UIVERSION_50) {
            return true;
        } else {
            return false;
        }
    }
    private static final String LETVUIVERSION = "ro.letv.ui";
    /*
    * @author caoxianjin
    * @return get the system UI version
    */
    public static UIVersion getUIVersion() {
        String uiVersion = "";
        try{
            //uiVersion = LetvManager.getLetvUiVersion();
        }catch (Throwable e){
            //logger.e("LetvManager getLetvUiVersion fucntion is not supported.");
        }
        //logger.i("get from LetvManager, uiVersion:" + uiVersion);
        if (StringUtils.equalsNull(uiVersion)) {
            uiVersion = SystemUtil.getSystemProperty(LETVUIVERSION);
        }
        return UIVersion.getUIVersionByString(uiVersion);
    }

    // UI version
    private static final String UITYPE_20 = "2.0";
    private static final String UITYPE_23 = "2.3";
    private static final String UITYPE_30 = "3.0";
    private static final String UITYPE_50 = "5.0";
    private static final String UITYPE_OTHER = "0.0";
    private static final int UIVERSION_CODE_30 = 3; // UI VERSION 3.0
    private static final int UIVERSION_CODE_50 = 5;// UI VERSION 5.0
    public static enum UIVersion {
        UIVERSION_20(UITYPE_20),
        UIVERSION_23(UITYPE_23),
        UIVERSION_30(UITYPE_30),
        UIVERSION_50(UITYPE_50),
        UIVERSION_OTHER(UITYPE_OTHER);
        final String mUIVersion;

        UIVersion(String uiVersion) {
            this.mUIVersion = uiVersion;
        }

        public String getUIVersion() {
            return this.mUIVersion;
        }

        public static UIVersion getUIVersionByString(String version) {
            String originVersion = version;
            double dVersion = 0.0;
            try {
                dVersion = Double.valueOf(originVersion);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int nVer = (int) dVersion;
            if (nVer >= UIVERSION_CODE_30 && nVer < UIVERSION_CODE_50) {
                originVersion = UITYPE_30;
            } else if (nVer >= UIVERSION_CODE_50) {
                originVersion = UITYPE_50;
            }

            for (UIVersion uiVersion : UIVersion.values()) {
                String sVersion = uiVersion.getUIVersion();
                if (sVersion.equals(originVersion)) {
                    return uiVersion;
                }
            }
            return UIVERSION_OTHER;
        }
    }

    /**
     * 是否允许弹出音量调节
     * @return
     */
    public static boolean isAudioCtrlPermitted() {
        return getDevice().isAudioCtrlPermitted();
    }

    /**
     * 用来区分视频内容是否在白名单中,在白名单内且在无版权地区可以进行播放
     * 按照：</br>
     * 1.如果能获取到imei，则拼接&devid=imeixxxxxxx（后面的xxx...是imei值，无冒号）</br>
     * 2.如果获取不到imei，则拼接&devid=macxxxxxxxx（后面的xxx...是mac地址，无冒号）</br>
     * @return 有Imei,&devid=imeixxxx;</br>
     *         没有imei有mac,&devid=macxxxx;</br>
     *         imei和mac都没有,&devid=;</br>
     */
    public static String getDevId() {
        String imei = DeviceUtils.getDeviceIMEI();
        StringBuilder sb = new StringBuilder();
        sb.append("&").append("devid=");// add "devid="
        if (!StringUtils.equalsNull(imei)) {
            sb.append("imei").append(imei);// add "imei983477347"
            return sb.toString();
        }

        String mac = SystemUtil.getMacAddress();
        if (StringUtils.equalsNull(mac)) {
            return sb.toString();
        }
        sb.append("mac");// add "mac"
        sb.append(mac);// add "mac989123829"
        return sb.toString();
    }

    /**
     * 获取设备IMEI值
     * @return
     */
    public static String getDeviceIMEI() {
        if (ContextProvider.getApplicationContext() == null) {
            return "";
        }
        TelephonyManager telephonyManager = (TelephonyManager) ContextProvider
                .getApplicationContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = telephonyManager.getDeviceId();

        if (deviceId != null && deviceId.length() > 0) {
            return deviceId;
        }
        return "";
    }

}
