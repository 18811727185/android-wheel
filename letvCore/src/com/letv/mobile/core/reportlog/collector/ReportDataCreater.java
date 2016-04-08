package com.letv.mobile.core.reportlog.collector;

import android.content.Context;

import com.letv.mobile.core.reportlog.ReportField;
import com.letv.mobile.core.time.TimeProvider;
import com.letv.mobile.core.utils.DeviceUtils;
import com.letv.mobile.core.utils.SystemUtil;
import com.letv.mobile.core.utils.TimeUtils;

/**
 * 用于创建错误日志内容的公共数据
 * @author ZhouKeWen E-mail:zhoukewen@letv.com
 * @version 创建时间：2014-9-19 上午11:38:18
 */
public abstract class ReportDataCreater {
    /**
     * 生成公共的上报数据项的内容
     * @param context
     * @return
     */
    protected ReportData createCommonInfo(Context context) {
        final ReportData reportInfo = new ReportData();

        if (context != null) {
            reportInfo.put(ReportField.APP_PACKAGE_NAME.name(),
                    SystemUtil.getPackageName(context));
            reportInfo.put(ReportField.APP_VERSION_NAME.name(),
                    SystemUtil.getVersionName(context));
            reportInfo.put(ReportField.APP_VERSION_CODE.name(),
                    String.valueOf(SystemUtil.getVersionCode(context)));
        }

        // mac有时获取不到手机的
        reportInfo.put(ReportField.MAC.name(), SystemUtil.getMacAddress());
        // 这个ip获取的实现有问题，可能不全
        reportInfo.put(
                ReportField.IP.name(),
                (SystemUtil.getLocalIpAddress() != null ? SystemUtil
                        .getLocalIpAddress() : "ip_null"));
        reportInfo.put(ReportField.DEVICES_MODEL.name(),
                DeviceUtils.getTerminalSeries());
        reportInfo.put(ReportField.ANDROID_VERSION.name(),
                SystemUtil.getOSVersion());
        reportInfo.put(ReportField.REPORT_DATE.name(), TimeUtils
                .timeToStr(TimeUtils.longToTime(TimeProvider.getCurrentMillisecondTime())));

        return reportInfo;
    }

    /**
     * 不同的错误上报类，填充不同的数据项。
     * 可以先调用{@code createCommonInfo}填充公共的数据项。
     * 建议使用方法：
     * <pre>
     * Override
     * public ReportData createInfo() {
     * ReportData reportData = this.createCommonInfo(this.context);
     * reportData.put("自定义的key", "自定义的value");
     * reportData.setLogType("自定义的日志类型");
     * return reportData;
     * }
     * </pre>
     */
    public abstract ReportData createData();

}
