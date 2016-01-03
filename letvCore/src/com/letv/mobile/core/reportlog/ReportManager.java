package com.letv.mobile.core.reportlog;

import android.content.Context;

import com.letv.mobile.core.reportlog.collector.ReportData;
import com.letv.mobile.core.reportlog.collector.ReportDataCreater;
import com.letv.mobile.core.reportlog.persister.ReportFileManager;
import com.letv.mobile.core.reportlog.sender.ReportSender;

/**
 * 日志上报管理。
 * 全局管理类
 * @author ZhouKeWen E-mail:zhoukewen@letv.com
 * @version 创建时间：2014-9-29 上午11:16:59
 */
public class ReportManager {

    /**
     * 由使用者自己实现的发送器
     */
    private ReportSender sender;

    private static ReportManager sInstance;

    private ReportManager() {
    }

    public static synchronized ReportManager getInstance() {
        if (sInstance == null) {
            synchronized (ReportManager.class) {
                if (sInstance == null) {
                    sInstance = new ReportManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 初始化管理器，包括初始化参数，初始化CrashHandler,初始化发送器
     * @param context
     */
    public void init(Context context, ReportSender sender) {
        NewCrashHandler.getInstance(context);
        if (sender == null) {
            throw new IllegalArgumentException("ReportSender can not null!!!");
        }
        this.sender = sender;

    }

    /**
     * 开始上报数据
     * @param data
     */
    public void startReport(ReportDataCreater dataCreater) {
        SendTask sendTask = new SendTask(this.sender);
        if (dataCreater != null) {
            sendTask.setDataCreater(dataCreater);
        }
        sendTask.start();
    }

    /**
     * 开始上报数据；上报未发送成功的历史数据
     * @param data
     */
    public void startReport() {
        this.startReport(null);
    }

    /**
     * 上报失败时，调用此接口，可以重新储存失败数据，以便下次重新上传
     * @param content
     */
    public void reSaveForReportFail(ReportData content) {
        if (content == null) {
            return;
        }
        ReportFileManager.reSaveToFile(content);
    }

}
