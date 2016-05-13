package com.letv.dynamicconfig.http;


import com.letv.dynamicconfig.DynamicConfigUtils;
import com.letv.mobile.core.utils.ContextProvider;
import com.letv.mobile.core.utils.DeviceUtils;
import com.letv.mobile.core.utils.LanguageCodeUtil;
import com.letv.mobile.core.utils.SystemUtil;
import com.letv.mobile.http.parameter.LetvBaseParameter;

/**
 * Created by shibin on 16/4/21.
 */
public class HttpCommonParameter extends LetvBaseParameter {

    private static final String COMMON_KEY_PCODE = "pcode"; // product code
    private static final String COMMON_KEY_DEV_KEY = "devKey";
    private static final String COMMON_KEY_DEV_ID = "devId";
    private static final String COMMON_KEY_TERMINAL_SERIES = "terminalSeries";
    private static final String COMMON_KEY_APP_VERSION = "appVersion";
    private static final String COMMON_KEY_BS_CHANNEL = "bsChannel"; // channel number
    private static final String COMMON_KEY_TERMINAL_APPLICATION = "terminalApplication";
    private static final String COMMON_KEY_TERMINAL_BRAND = "terminalBrand";
    private static final String COMMON_KEY_LANGCODE = "langcode";
    private static final String COMMON_KEY_VER_CODE = "versionCode";
    private static final String COMMON_KEY_SALES_AREA = "salesArea";
    private static final String COMMON_KEY_COUNTRY_AREA = "countryArea";
    // NOTE(shibin) product line code, it is unclear effect, maybe should be removed or pass value
    // from main application.
    private static final String PCODE = "160110000";

    @Override
    public LetvBaseParameter combineParams() {
        this.put(COMMON_KEY_PCODE, PCODE);
        this.put(COMMON_KEY_DEV_KEY, DeviceUtils.getDeviceKey());
        this.put(COMMON_KEY_DEV_ID, DeviceUtils.getDeviceId());
        this.put(COMMON_KEY_TERMINAL_SERIES, DeviceUtils.getTerminalSeries());
        this.put(COMMON_KEY_BS_CHANNEL, DynamicConfigUtils.getBsChannel());
        this.put(COMMON_KEY_APP_VERSION, SystemUtil.getVersionName(ContextProvider
                .getApplicationContext()));
        this.put(COMMON_KEY_TERMINAL_APPLICATION, DynamicConfigUtils.getTerminalApplication());
        this.put(COMMON_KEY_TERMINAL_BRAND, DynamicConfigUtils.getTerminalBrand());
        this.put(COMMON_KEY_LANGCODE, LanguageCodeUtil.getLanguageCode());
        this.put(COMMON_KEY_VER_CODE, SystemUtil.getVersionCode(ContextProvider
                .getApplicationContext()));
        this.put(COMMON_KEY_SALES_AREA, DeviceUtils.getSalesArea(ContextProvider.getApplicationContext()));
        this.put(COMMON_KEY_COUNTRY_AREA, DeviceUtils.getCountryArea());
        return this;
    }
}
