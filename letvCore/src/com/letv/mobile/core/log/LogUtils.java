package com.letv.mobile.core.log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Environment;
import android.os.StatFs;

/**
 * log utils
 * TODO(qingxia): Add file logger later.
 * @author Fengwx
 */
public class LogUtils {

    /**
     * external avaliable momory size, default 100MB
     */
    private static final long AVALIABLE_EXTERNAL_MEMORY_SIZE = 100 * 1024 * 1024;

    /**
     * error code
     */
    private static final int ERROR = -1;

    /**
     * judge whether external memory is available
     * @return
     */
    public static boolean externalMemoryAvailable() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * judge whether external memory is full
     * @return
     */
    public static boolean externalMemoryFull() {
        return getAvailableExternalMemorySize()
                - AVALIABLE_EXTERNAL_MEMORY_SIZE < 0;
    }

    /**
     * get available external memory size
     * @return
     */
    @SuppressWarnings("deprecation")
    public static long getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            // NOTE(qingxia) : This method was deprecated in API level 18. Use
            // getAvailableBlocksLong() instead.
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        } else {
            return ERROR;
        }
    }

    /**
     * date format
     * @param template
     * @return
     */
    public static String dateFormat(String template) {
        SimpleDateFormat df = new SimpleDateFormat(template,
                Locale.getDefault());
        return df.format(new Date());
    }

    public static File getBasePath() {
        return Environment.getExternalStorageDirectory();
    }

}
