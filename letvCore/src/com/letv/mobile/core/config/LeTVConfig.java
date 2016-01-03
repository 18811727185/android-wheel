/**
 *
 * Copyright 2013 LeTV Technology Co. Ltd., Inc. All rights reserved.
 *
 * @Author : xiaqing
 *
 * @Description :
 *
 */

package com.letv.mobile.core.config;

import android.annotation.SuppressLint;
import android.os.Environment;

import com.letv.mobile.core.log.Level;
import com.letv.mobile.core.utils.ContextProvider;
import com.letv.mobile.core.utils.FileUtils;
import com.letv.mobile.core.utils.Utils;

import java.io.File;

/**
 * TODO(xiaqing): We should consider the devices with pluggable SD card.
 * This class provide a common configure information for base project.
 * @author xiaqing
 */
public final class LeTVConfig {
    public static final int LETV_BASE_LOG_LEVEL = Level.DEBUG;
    // TODO(qingxia): Maybe we should use this definition later.
    // private static final String COMPANY_PATH = "/letv/";
    // Define the base directory by application name.
    private static String sAppName = "letv";
    // Global working base path
    private static String sWorkingPath = null;
    // Path information
    // save log path config
    public static final String LETV_FILE_LOGGER_PATH = "/log/";
    public static final String LETV_ERROR_PATH = "/errorLog/";
    // download path
    @Deprecated
    private static final String LETV_DOWNLOAD_PATH = "/download/";
    // Global download path
    private static final String GLOBAL_DOWNLOAD_PATH = "/ledown/";
    // player log path
    private static final String LETV_PLAYER_LOG_PATH = "/player_log/";
    // image cache path
    public static final String IMAGE_CACHE_DIR = "/.image/";
    // NOTE(qingxia): In letv devices, we should put a empty .nomedia file to
    // base application directory to prevent the media server scanning this
    // directory.
    private static final String LETV_NOT_CHECK_MEDIA_FILE = "/.nomedia";

    /**
     * @param appName
     */
    public static void init(String appName) {
        if (Utils.isStringEmpty(appName)) {
            return;
        }
        sAppName = appName;

        initAllDir();
    }

    /**
     * init all dir if not exits
     */
    private static void initAllDir() {
        // NOTE(qingxia): In letv devices, we should put a empty .nomedia file
        // to base application directory to prevent the media server scanning
        // this directory.
        FileUtils.createFile(LeTVConfig.getNoMediaFilePath());

        // Initialize all working directory.
        String[] allDir = new String[] { getImageCachePath(),
                getErrorLogPath(), getPlayerLogPath(), getDownloadPath() };
        for (String dirPath : allDir) {
            FileUtils.createDir(dirPath);
        }

        FileUtils.createFile(LeTVConfig.getDownloadNoMediaFilePath());
    }

    /**
     * get image cache path
     * @return if sdcard doesn't exist ,return null. otherwise return
     *         sdcard Path + /letv/.image/
     */
    public static String getImageCachePath() {
        return getGlobalWorkingPath() + sAppName + IMAGE_CACHE_DIR;
    }

    public static String getErrorLogPath() {
        return getGlobalWorkingPath() + sAppName + LETV_ERROR_PATH;
    }

    private static String getNoMediaFilePath() {
        return getGlobalWorkingPath() + sAppName + LETV_NOT_CHECK_MEDIA_FILE;
    }

    /**
     * 获取下载目录ledown隐藏文件路径
     * @return
     */
    public static String getDownloadNoMediaFilePath() {
        return getDownloadPath() + LETV_NOT_CHECK_MEDIA_FILE;
    }

    public static String getAppRoot() {
        return getGlobalWorkingPath() + sAppName;
    }

    public static String getDownloadPath() {
        // TODO 目前放在全局目录下 目的是让其他App(本地视频App)搜索到，若不需要被其他app搜索到可使用 sAppName +
        // LETV_DOWNLOAD_PATH
        // return getGlobalWorkingPath() + sAppName + LETV_DOWNLOAD_PATH;
        return getGlobalWorkingPath() + GLOBAL_DOWNLOAD_PATH;
    }

    public static String getPlayerLogPath() {
        return getGlobalWorkingPath() + sAppName + LETV_PLAYER_LOG_PATH;
    }

    /**
     * Get and initialize global application working path.
     * @return
     */
    public static String getGlobalWorkingPath() {
        if (sWorkingPath == null) {
            String str = getSDPath();
            if (str == null) {
                return getNoSdCardPath();
            }

            // Initialize base working directory.
            File sdDir = new File(str);
            if (sdDir.canWrite()) {
                sWorkingPath = str;
            } else {
                return getNoSdCardPath();
            }
        }

        // TODO(qingxia): May be we should add company name later.
        // return sWorkingPath + COMPANY_PATH;
        return sWorkingPath;
    }

    /**
     * Get global sd card path
     * @return
     */
    public static String getSDPath() {
        File sdDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            sdDir = Environment.getExternalStorageDirectory();
        }
        return sdDir == null ? null : sdDir.toString() + "/";
    }

    @SuppressLint("SdCardPath")
    public static String getNoSdCardPath() {
        return ContextProvider.getApplicationContext().getFilesDir().toString()
                + File.separator;
    }

    private static class ImgCacheFileHolder {
        private static final File imgCacheSDCardFile = new File(
                getImageCachePath());

        private static final File imgCacheMemoryFile = new File(
                getNoSdCardPath() + IMAGE_CACHE_DIR);
    }

    /**
     * @return a File for img cache path in sdcard. The File is single instance
     */
    public static File getImgCacheSDCardFileInstance() {
        return ImgCacheFileHolder.imgCacheSDCardFile;
    }

    /**
     * @return a File for img cache path in memory.The File is single instance
     */
    public static File getImgCacheMemoryFileInstance() {
        return ImgCacheFileHolder.imgCacheMemoryFile;
    }

    public static String getsAppName() {
        return sAppName;
    }

    // 分享临时使用路径
    static final String TEMP_PATH = "shareTemp.png";

    /**
     * 返回分享临时使用路径 因为目前分享组件只支持本地路径分享 所以所有的分享图片目前都要先存储到本地
     * @return
     */
    public static String getShareTempPath() {
        return getImageCachePath() + TEMP_PATH;
    }

    private LeTVConfig() {
    }
}
