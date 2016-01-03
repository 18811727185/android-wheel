package com.letv.mobile.core.reportlog.persister;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.text.TextUtils;

import com.letv.mobile.core.config.LeTVConfig;
import com.letv.mobile.core.log.Logger;
import com.letv.mobile.core.reportlog.collector.ReportData;
import com.letv.mobile.core.utils.FileUtils;

/**
 * 为上层提供访问和储存ReportData的接口
 * @author zkw
 */
public final class ReportFileManager {

    private static final int FILE_COUNT = 10;
    private static final Logger logger = new Logger("ReportFileManager");

    /**
     * @param maxLoadCounts
     * @return List<CrashReportInfo> CrashReportInfo的列表
     */
    public synchronized static List<ReportData> loadLogFromFile(
            final int maxLoadCounts) {

        if (maxLoadCounts <= 0) {
            throw new IllegalArgumentException("maxLoadCounts must be > 0");
        }
        List<ReportData> logList = new ArrayList<ReportData>(maxLoadCounts);

        // 如果目录不存在，则创建目录，并将空列表返回
        String fileDir = LeTVConfig.getErrorLogPath();
        File dir = new File(fileDir);
        if (!dir.exists()) {
            dir.mkdirs();
            return logList;
        }

        File[] files = dir.listFiles();
        if (files == null || files.length <= 0) {
            return logList;
        }

        // 从新到旧排序文件列表
        Arrays.sort(files, new ReportFileManager.CompratorForNewToOld());

        // 读取一定数量的ReportLog
        ReportPersister persister = new ReportPersister();
        for (int i = 0; (i < files.length && i < maxLoadCounts); i++) {
            File file = files[i];
            String content = persister.loadToString(file);
            if (!TextUtils.isEmpty(content)) {
                String typeName = FileUtils.getFileExtension(file.getName());
                ReportData data = new ReportData();
                data.setLogType(typeName);
                data.setDataForSend(content);
                logList.add(data);
            }
            // 删除被load后的文件。此时load出的数据还没send出去，所以可能有些小问题
            file.delete();
        }

        return logList;

    }

    /**
     * 将信息保存到本地。
     * 还做了一些check和clear操作
     * @param reportInfo
     */
    public synchronized static void saveLogToFile(final ReportData reportInfo) {
        if (reportInfo == null) {
            return;
        }

        String filePath = ReportFileManager.makeFilePath(reportInfo
                .getLogType());
        if (TextUtils.isEmpty(filePath)) {
            return;
        }

        // 储存到文件
        ReportPersister persister = new ReportPersister();
        persister.storeToFile(reportInfo, filePath);

        ReportFileManager.checkAndClearDir();
    }

    /**
     * 重新保存到文件，只能保存已变成String的数据
     * @param reportInfo
     *            之包含LogType和String类型数据的reportInfo
     */
    public synchronized static void reSaveToFile(final ReportData reportInfo) {
        if (reportInfo == null) {
            return;
        }

        String filePath = ReportFileManager.makeFilePath(reportInfo
                .getLogType());

        if (TextUtils.isEmpty(filePath)) {
            return;
        }

        // 储存到文件
        ReportPersister persister = new ReportPersister();
        persister.storeToFile(reportInfo.getDataForSend(), filePath);

        ReportFileManager.checkAndClearDir();
    }

    /**
     * 创建崩溃日志的文件名
     * @return
     */
    private static String makeFilePath(String logType) {
        long timestamp = System.currentTimeMillis();
        String fileName = timestamp + "." + logType;
        fileName.replace(' ', '_');
        String fileDir = LeTVConfig.getErrorLogPath();
        String filePath = fileDir + fileName;

        File dir = new File(fileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Double check the path
        if (!dir.exists() || !dir.isDirectory()) {
            ReportFileManager.logger.i("dir path error = " + filePath);
            return null;
        }

        return filePath;
    }

    /**
     * 检测ErrorLog目录下的文件，如果超出指定数量，则优先删除老的文件
     */
    private static void checkAndClearDir() {
        String fileDir = LeTVConfig.getErrorLogPath();
        File dir = new File(fileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // Double check the path
        if (!dir.exists() || !dir.isDirectory()) {
            ReportFileManager.logger.i("dir path error = " + fileDir);
            return;
        }
        // 删除一些老的文件
        ReportFileManager.deleteSomeOldFile(dir.listFiles());
    }

    /**
     * 只留下最新的{@code FILE_COUNT}个文件，其他的都删除
     * @param fileList
     */
    private static void deleteSomeOldFile(final File[] fileList) {
        if (fileList != null && fileList.length > ReportFileManager.FILE_COUNT) {
            // 从旧到新排序文件列表
            Arrays.sort(fileList, new ReportFileManager.CompratorForOldToNew());
            int num = fileList.length - ReportFileManager.FILE_COUNT;
            for (int i = 0; i < num; i++) {
                try {
                    FileUtils.forceDelete(fileList[i]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 比较器：从旧到新排序
     * @author zkw
     */
    private static class CompratorForOldToNew implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {
            // 将最旧的文件排在前面
            return f1.lastModified() - f2.lastModified() > 0 ? 1 : -1;
        }
    }

    /**
     * 比较器：从新到旧排序
     * @author zkw
     */
    private static class CompratorForNewToOld implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {
            // 将最新的文件排在前面
            return f1.lastModified() - f2.lastModified() < 0 ? 1 : -1;
        }
    }
}
