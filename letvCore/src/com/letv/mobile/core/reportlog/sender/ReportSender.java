package com.letv.mobile.core.reportlog.sender;

import com.letv.mobile.core.reportlog.collector.ReportData;

/**
 * 日志上传的接口，不同上传方式自己实现
 * @author ZhouKeWen E-mail:zhoukewen@letv.com
 * @version 创建时间：2014-9-19 下午3:04:03
 */
public interface ReportSender {
    /**
     * 发送日志
     * @param errorContent
     *            上传的日志内容
     */
    public void send(ReportData errorContent);
}
