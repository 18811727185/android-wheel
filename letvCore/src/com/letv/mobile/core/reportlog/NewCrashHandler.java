package com.letv.mobile.core.reportlog;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;

import com.letv.mobile.core.log.Logger;
import com.letv.mobile.core.reportlog.collector.CrashReportDataCreater;

public class NewCrashHandler implements UncaughtExceptionHandler {
    private final Logger logger = new Logger("NewCrashHandler");

    private static NewCrashHandler sInstance;

    private Context context;

    private CrashReportDataCreater creater;

    /**
     * 系统默认的异常处理
     */
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    public void init(Context xt) {
        this.logger.i("NewCrashHandler init");
        this.context = xt;
        this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    private NewCrashHandler() {
    }

    public static synchronized NewCrashHandler getInstance(Context mContext) {
        if (NewCrashHandler.sInstance == null) {
            synchronized (NewCrashHandler.class) {
                if (NewCrashHandler.sInstance == null) {
                    NewCrashHandler.sInstance = new NewCrashHandler();
                    NewCrashHandler.sInstance.init(mContext);
                }
            }
        }
        return NewCrashHandler.sInstance;
    }

    /**
     * 当应用需要向崩溃日志中添加额外数据时，设置自己实现的CrashReportDataCreater
     * @param creater
     */
    public void setExtraDataCreater(CrashReportDataCreater creater) {
        this.creater = creater;
    }

    @Override
    public void uncaughtException(Thread thread, final Throwable ex) {
        this.logger.e("NewCrashHandler uncaughtException");
        this.handleException(ex);

        if (this.mDefaultHandler != null) {
            this.mDefaultHandler.uncaughtException(thread, ex);
        }
    }

    /**
     * Handle exception by application.
     * @param ex
     * @return
     *         true have handled exception
     *         false have not handled exception
     */
    private boolean handleException(final Throwable ex) {
        this.logger.d("handleException");
        if (ex == null || (ex instanceof OutOfMemoryError)) {
            return false;
        }
        if (this.creater == null) {
            // 使用默认的data creater
            this.creater = new CrashReportDataCreater(ex, this.context);
        } else {
            // 使用应用自定义的creater
            this.creater.setContext(this.context);
            this.creater.setUncaughtException(ex);
        }
        ReportManager.getInstance().startReport(this.creater);
        return true;

    }

}
