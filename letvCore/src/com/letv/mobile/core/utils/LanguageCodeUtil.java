package com.letv.mobile.core.utils;

import java.util.Locale;

import com.letv.mobile.core.log.Logger;

/**
 * 提供HTTPParameter的基本参数中的langcode的值
 */
public class LanguageCodeUtil {

    private static final Logger mLogger = new Logger("LanguageCodeUtil");
    /**
     * langcode值共有三种，繁体中文为"zh_hk"，简体中文为"zh_cn"，其他传"en_us"，
     */
    private static final String LANGUAGE_CODE_HK = "zh_hk";
    private static final String LANGUAGE_CODE_CN = "zh_cn";
    private static final String LANGUAGE_CODE_EN = "en_us";

    private static final String LANGUAGE_CODE_HK_COUNTRY_VALUE_TW = "TW";
    private static final String LANGUAGE_CODE_HK_COUNTRY_VALUE_HK = "HK";
    private static final String LANGUAGE_CODE_CN_COUNTRY_VALUE = "CN";

    private static String langCode = null;
    private static final Object mLock = new Object();

    private LanguageCodeUtil() {
    }

    public static String getLanguageCode() {

        if (langCode == null) {
            synchronized (mLock) {
                generateLanguageCode();
            }
        }
        mLogger.e("get language code = " + langCode);
        return langCode;
    }

    public static void setLanguageCodeNull() {
        synchronized (mLock) {
            mLogger.e("language code = null");
            langCode = null;
        }
    }

    /**
     * 在LETVX60上测试改变系统语言，无论是繁体中文还是简体中文Locale.getDefault().getLanguage()
     * 返回的都是“zh”，而Locale.getDefault().getCountry()会返回“TW”或者“CN”，因此要生成langCode
     * 只能依靠Locale.getDefault().getCountry()了。
     * 非“TW”和“CN”的情况，统一传LANGUAGE_CODE_EN
     * TODO:需要验证一下第三方机器上这个接口的返回情况是否通用
     */
    private static void generateLanguageCode() {
        String country = Locale.getDefault().getCountry();
        if (LANGUAGE_CODE_HK_COUNTRY_VALUE_TW.equalsIgnoreCase(country)
                || LANGUAGE_CODE_HK_COUNTRY_VALUE_HK.equalsIgnoreCase(country)) {
            langCode = LANGUAGE_CODE_HK;
        } else if (LANGUAGE_CODE_CN_COUNTRY_VALUE.equalsIgnoreCase(country)) {
            langCode = LANGUAGE_CODE_CN;
        } else {
            langCode = LANGUAGE_CODE_EN;
        }
    }
}
