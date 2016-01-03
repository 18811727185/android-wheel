package com.letv.mobile.core.log;

/**
 * LogManager
 * @author Fengwx
 */
public interface LogManager {

    /**
     * tag
     */
    String TAG = "LETV_MOBILE_LEADING";

    /**
     * default level
     */
    int DEFAULT_LEVEL = Level.INFO;

    /**
     * error code
     */
    int ERROR_CODE = -0x1;

    /**
     * success code
     */
    int SUCCESS_CODE = 0x0;

    /**
     * default dir
     */
    String FILE_DIR = "/Letv/";

    /**
     * default name
     */
    String FILE_NAME = "log_";

    /**
     * default suffix
     */
    String FILE_SUFFIX = ".txt";

    /**
     * default date, templete is "yyyyMMdd"
     */
    String FILE_DATE = LogUtils.dateFormat("yyyyMMdd");

    /**
     * default separator
     */
    String LOG_SEPARATOR = "  >>>>>>  ";

    /**
     * default log_run_name
     */
    String LOG_RUN_NAME = "**********" + TAG
            + LogUtils.dateFormat("yyyy-MM-dd HH:mm:ss") + "**********";

}
