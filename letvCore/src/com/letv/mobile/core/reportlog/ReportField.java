package com.letv.mobile.core.reportlog;

/**
 * 报告中的公共项目
 * @author zkw
 */
public enum ReportField {

    /**
     * 在Manifest文件中定义，一般形如：android:versionCode="14"
     * @see android.content.pm.PackageInfo#versionCode
     */
    APP_VERSION_CODE,

    /**
     * 在Manifest文件中定义，一般形如：android:versionName="1.3.5"
     * @see android.content.pm.PackageInfo#versionName
     */
    APP_VERSION_NAME,

    /**
     * android系统版本，类似：4.4.1
     * @see android.os.Build.VERSION#RELEASE
     */
    ANDROID_VERSION,

    /**
     * 应用包名
     */
    APP_PACKAGE_NAME,

    /**
     * 设备mac地址
     */
    MAC,

    /**
     * 普通设备是android.os.Build.MODEL，
     * 但有些电视会添加用于识别的字串
     */
    DEVICES_MODEL,

    /**
     * 设备ip地址
     */
    IP,

    /**
     * 上报日志时，用户设备的时间
     */
    REPORT_DATE,
}
