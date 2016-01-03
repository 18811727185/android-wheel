package com.letv.mobile.core.reportlog.collector;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import android.content.Context;

/**
 * 崩溃日志内容的创建类。
 * 如果应用自己有需要向崩溃日志里添加的信息，继承该类，并实现 {@code putExtraData()} 方法
 * @author ZhouKeWen E-mail:zhoukewen@letv.com
 * @version 创建时间：2014-9-29 下午1:48:46
 */
public class CrashReportDataCreater extends ReportDataCreater {

    private static final String STACK_TRACE = "STACK_TRACE";

    private static final String CRASH_LOG_NAME = "CrashLog";

    private Throwable uncaughtException;

    private Context context;

    /**
     * 崩溃日志内容的创建类
     * @param uncaughtEx
     * @param context
     */
    public CrashReportDataCreater(Throwable uncaughtEx, Context context) {
        this.uncaughtException = uncaughtEx;
        this.context = context;
    }

    public CrashReportDataCreater() {

    }

    public void setUncaughtException(Throwable uncaughtEx) {
        this.uncaughtException = uncaughtEx;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public ReportData createData() {
        ReportData reportData = this.createCommonInfo(this.context);
        reportData.put(STACK_TRACE,
                this.getStackTrace(this.uncaughtException));
        reportData.setLogType(CRASH_LOG_NAME);
        return reportData;
    }

    private String getStackTrace(Throwable uncaughtEx) {
        if (uncaughtEx == null) {
            return "no stack trace";
        }

        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        Throwable cause = uncaughtEx;
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        final String stacktraceAsString = result.toString();
        printWriter.close();

        return stacktraceAsString;
    }
}
