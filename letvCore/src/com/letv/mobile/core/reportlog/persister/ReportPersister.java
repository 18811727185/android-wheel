package com.letv.mobile.core.reportlog.persister;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import android.text.TextUtils;

import com.letv.mobile.core.reportlog.collector.ReportData;

/**
 * 日志内容保存到文件。
 * <p>
 * 非线程安全
 * </p>
 * @author zkw
 */
final class ReportPersister {

    private static final String LINE_SEPARATOR = "\n";

    private static final int DEFAULT_BUFFER_SIZE_IN_BYTES = 8192;

    ReportPersister() {
    }

    /**
     * 将String类型的上报数据保存到文件
     * @param logData
     * @param filePath
     */
    public void storeToFile(final String logData, final String filePath) {
        if (TextUtils.isEmpty(logData) || TextUtils.isEmpty(filePath)) {
            return;
        }
        File file = new File(filePath);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(logData.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 将上报数据保存到文件。
     * @param logData
     *            要上报的数据
     * @param filePath
     *            要写入的文件全路径
     */
    public void storeToFile(final ReportData logData, final String filePath) {
        if (logData == null || TextUtils.isEmpty(filePath)) {
            return;
        }

        final StringBuilder buffer = new StringBuilder(200);

        for (final Map.Entry<String, String> entry : logData.entrySet()) {
            buffer.append(entry.getKey());
            buffer.append(" => ");
            buffer.append(entry.getValue());
            buffer.append(LINE_SEPARATOR);
        }

        this.storeToFile(buffer.toString(), filePath);

        buffer.setLength(0);
    }

    /**
     * 将文件内容加载成String
     * @param file
     * @return
     */
    public String loadToString(final File file) {
        if (file == null) {
            return null;
        }
        return this.loadToString(file.getAbsolutePath());
    }

    /**
     * 将文件内容加载成String
     * @param file
     * @return
     */
    public String loadToString(final String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }

        StringBuilder result = new StringBuilder(200);

        File file = new File(filePath);
        BufferedInputStream bufferInput = null;
        try {
            bufferInput = new BufferedInputStream(new FileInputStream(file),
                    DEFAULT_BUFFER_SIZE_IN_BYTES);
            byte[] buffer = new byte[200];
            while (true) {
                int count = bufferInput.read(buffer);
                if (count == -1) {
                    break;
                }
                result.append(new String(buffer, 0, count));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferInput != null) {
                try {
                    bufferInput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (result.length() == 0) {
            return null;
        } else {
            return result.toString();
        }

    }

}
