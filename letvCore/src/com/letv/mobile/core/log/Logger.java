package com.letv.mobile.core.log;

import android.text.TextUtils;
import android.util.Log;

import com.letv.mobile.core.config.LeTVConfig;
import com.letv.mobile.core.utils.StringUtils;
import com.letv.mobile.core.utils.SystemUtil;

/**
 * Log
 * @author Fengwx
 */
public class Logger implements LogManager {

    /**
     * log level
     */
    private static int level;
    private static final String LOG_LEVELS = "VDIWE";
    private static final String PROPERTY_KEY_LOGLEVEL = "debug.tvclient.loglevel";

    /**
     * 包名
     */
    private static String sLogTag = "";

    /**
     * tag
     */
    private String tag = "LETV";

    /**
     * a switch control printing, default open
     */
    private boolean toggle = true;

    /**
     * a switch control write to sdcard, default close
     */
    // TODO(qingxia): Remove save to sd card feature.
    // private static boolean save = LetvCoreSetting.LETV_BASE_LOG_SAVE;

    /**
     * judge whether log is running, default false
     */
    private static boolean run = false;

    static {
        getSystemLogLevel();
    }

    private static void getSystemLogLevel() {
        String strLogLevel = SystemUtil
                .getSystemProperty(PROPERTY_KEY_LOGLEVEL);

        if (TextUtils.isEmpty(strLogLevel)) {
            level = LeTVConfig.LETV_BASE_LOG_LEVEL;
        } else {
            int logLevelIndex;
            if ((logLevelIndex = LOG_LEVELS.indexOf(Character
                    .toUpperCase(strLogLevel.charAt(0)))) != -1) {
                level = logLevelIndex + Level.VERBOSE;
            } else {
                level = LeTVConfig.LETV_BASE_LOG_LEVEL;
            }
        }
    }

    @Deprecated
    public Logger() {
    }

    public Logger(String tag) {
        this.tag = tag;
    }

    /**
     * 设置以packageName开始的Logtag（启动Application时调用）
     * @param packageName
     */
    public static void setLogTagByPackageName(String packageName) {
        if (StringUtils.equalsNull(sLogTag)
                && !StringUtils.equalsNull(packageName)) {
            sLogTag = packageName + " : ";
        }
    }

    /**
     * get current tag
     * @return
     */
    public String getTag() {
        return this.tag;
    }

    /**
     * set tag to show log's name
     * @param tag
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * get current toggle
     * @return
     */
    public boolean isToggle() {
        return this.toggle;
    }

    /**
     * set toggle to control whether log is open
     * @param toggle
     */
    public void setToggle(boolean toggle) {
        this.toggle = toggle;
    }

    /**
     * get current level
     * @return
     */
    public int getLevel() {
        return level;
    }

    /**
     * error logging
     * @param msg
     */
    public void e(String msg) {
        if (this.toggle && level <= Level.ERROR) {
            this.print(Level.ERROR, msg);
        }
    }

    /**
     * warn logging
     * @param msg
     */
    public void w(String msg) {
        if (this.toggle && level <= Level.WARN) {
            this.print(Level.WARN, msg);
        }
    }

    /**
     * info logging
     * @param msg
     */
    public void i(String msg) {
        if (this.toggle && level <= Level.INFO) {
            this.print(Level.INFO, msg);
        }
    }

    /**
     * debug logging
     * @param msg
     */
    public void d(String msg) {
        if (this.toggle && level <= Level.DEBUG) {
            this.print(Level.DEBUG, msg);
        }
    }

    /**
     * verbose logging
     * @param msg
     */
    public void v(String msg) {
        if (this.toggle && level <= Level.VERBOSE) {
            this.print(Level.VERBOSE, msg);
        }
    }

    /**
     * print log
     * @param level
     * @param msg
     * @param tr
     */
    private void print(int level, String msg) {
        if (!run) {
            Log.e(TAG, LOG_RUN_NAME);
            run = true;
        }
        callSysLog(level, this.markTag(), msg);
        // this.writeToSdcard(msg);
    }

    private static void callSysLog(int level, String tag, String msg) {
        if (tag == null) {
            tag = "";
        }
        if (msg == null) {
            msg = "";
        }
        switch (level) {
        case Level.ERROR:
            Log.e(tag, sLogTag + msg);
            break;
        case Level.WARN:
            Log.w(tag, sLogTag + msg);
            break;
        case Level.INFO:
            Log.i(tag, sLogTag + msg);
            break;
        case Level.DEBUG:
            Log.d(tag, sLogTag + msg);
            break;
        case Level.VERBOSE:
            Log.v(tag, sLogTag + msg);
            break;
        default:
            break;
        }
    }

    // TODO(qingxia): Enable later
    /**
     * write to sdcard
     * @throws IOException
     */
    // private int writeToSdcard(String msg) {
    // File file = new File(Environment.getExternalStorageDirectory()
    // + FILE_DIR);
    // if (file.exists()) {
    // ThreadUtils.startRunInSingleThread(new Runnable() {
    // @Override
    // public void run() {
    // FileUtils.DeleteFolder(Environment
    // .getExternalStorageDirectory() + FILE_DIR);
    // }
    //
    // });
    // }
    //
    // if (!save) {
    // return ERROR_CODE;
    // }
    // if (!LogUtils.externalMemoryAvailable()) {
    // return ERROR_CODE;
    // }
    // if (LogUtils.externalMemoryFull()) {
    // return ERROR_CODE;
    // }
    // BufferedWriter writer = null;
    // try {
    // if (!file.exists()) {
    // file.mkdirs();
    // }
    // file = new File(Environment.getExternalStorageDirectory()
    // + FILE_DIR + FILE_NAME + FILE_DATE + FILE_SUFFIX);
    // if (!file.exists()) {
    // file.createNewFile();
    // }
    // writer = new BufferedWriter(new FileWriter(file, true));
    // writer.write(this.markTag() + LOG_SEPARATOR + msg);
    // writer.newLine();
    // } catch (IOException e) {
    // e.printStackTrace();
    // } finally {
    // try {
    // if (null != writer) {
    // writer.flush();
    // writer.close();
    // }
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }
    // return SUCCESS_CODE;
    // }

    /**
     * mark tag
     */
    private String markTag() {
        if (null == this.tag) {
            return TAG;
        }
        return this.tag;
    }

    private static void write(int level, String tag, String msg) {
        if (level >= Logger.level) {
            Logger.callSysLog(level, tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        Logger.write(Level.VERBOSE, tag, msg);
    }

    public static void d(String tag, String msg) {
        Logger.write(Level.DEBUG, tag, msg);
    }

    public static void i(String tag, String msg) {
        Logger.write(Level.INFO, tag, msg);
    }

    public static void w(String tag, String msg) {
        Logger.write(Level.WARN, tag, msg);
    }

    public static void e(String tag, String msg) {
        Logger.write(Level.ERROR, tag, msg);
    }
}
