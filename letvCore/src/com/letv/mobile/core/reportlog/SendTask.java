package com.letv.mobile.core.reportlog;

import com.letv.mobile.core.reportlog.collector.ReportData;
import com.letv.mobile.core.reportlog.collector.ReportDataCreater;
import com.letv.mobile.core.reportlog.persister.ReportFileManager;
import com.letv.mobile.core.reportlog.sender.ReportSender;

import java.util.List;

/**
 * 在另一个线程启动发送任务
 * @author ZhouKeWen E-mail:zhoukewen@letv.com
 * @version 创建时间：2014-9-19 下午6:30:20
 */
public class SendTask extends Thread {
    @SuppressWarnings("unused")
    private static final int MAX_LOAD_LOG_COUNT = 5;

    /**
     * 循环发送时的间隔时间，300ms，经验值
     */
    @SuppressWarnings("unused")
    private static final int SEND_INTERVAL_TIME = 300;

    @SuppressWarnings("unused")
    private final ReportSender sender;
    private ReportDataCreater dataCreater;

    /**
     * 在另一个线程启动发送任务。
     * @param sender
     *            发送接口，实现具体的发送方法
     */
    public SendTask(ReportSender sender) {
        this.sender = sender;
    }

    public void setDataCreater(ReportDataCreater dataCreater) {
        this.dataCreater = dataCreater;
    }

    @Override
    public void run() {

        // 日志保存到本地文件
        if (this.dataCreater != null) {
            ReportData data = this.dataCreater.createData();
            this.dataCreater = null;
            if (data != null && !data.isEmpty()) {
                ReportFileManager.saveLogToFile(data);
            }
        }

        if (this.sender == null) {
            return;
        }

        // 从本地文件加载多组上报数据
        List<ReportData> infoList = ReportFileManager.loadLogFromFile(MAX_LOAD_LOG_COUNT);

        if (infoList == null) {
            return;
        }

        for (ReportData info : infoList) {
            this.sender.send(info);
            try {
                sleep(SEND_INTERVAL_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
