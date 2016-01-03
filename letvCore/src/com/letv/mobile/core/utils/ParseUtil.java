package com.letv.mobile.core.utils;

import com.letv.mobile.core.log.Logger;
import com.letv.mobile.core.utils.StringUtils;

/**
 * 强制转换
 * @author baiwenlong
 */
public class ParseUtil {

    private static final String TAG = "ParseUtil";

    public static int parseInt(String strValue, int defValue) {
        if (!StringUtils.equalsNull(strValue)) {
            try {
                return Integer.parseInt(strValue);
            } catch (Exception e) {
                Logger.w(TAG, "parseInt failed: strValue is " + strValue
                        + ", defValue = " + defValue);
                e.printStackTrace();
            }
        } else {
            Logger.w(TAG, "parseInt failed: strValue is " + strValue
                    + ", defValue = " + defValue);
        }
        return defValue;
    }

    public static long parseLong(String strValue, long defValue) {
        if (!StringUtils.equalsNull(strValue)) {
            try {
                return Long.parseLong(strValue);
            } catch (Exception e) {
                Logger.w(TAG, "parseLong failed: strValue is " + strValue
                        + ", defValue = " + defValue);
                e.printStackTrace();
            }
        } else {
            Logger.w(TAG, "parseLong failed: strValue is " + strValue
                    + ", defValue = " + defValue);
        }
        return defValue;
    }

    public static float parseFloat(String strValue, float defValue) {
        if (!StringUtils.equalsNull(strValue)) {
            try {
                return Float.parseFloat(strValue);
            } catch (Exception e) {
                Logger.w(TAG, "parseFloat failed: strValue is " + strValue
                        + ", defValue = " + defValue);
                e.printStackTrace();
            }
        } else {
            Logger.w(TAG, "parseFloat failed: strValue is " + strValue
                    + ", defValue = " + defValue);
        }
        return defValue;
    }
}
