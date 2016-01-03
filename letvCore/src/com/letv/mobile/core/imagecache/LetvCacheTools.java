package com.letv.mobile.core.imagecache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.text.TextUtils;

import com.letv.mobile.core.config.LeTVConfig;

/**
 * 工具类
 */
public class LetvCacheTools {

    public static class BasicParams {

        public static int locationThreadPoolNum = 3;

        public static int netThreadPoolNum = 5;

        public static int cachePoolNum = 30;

        /***
         * 初始化缓存基本参数
         * @param cachePoolNum
         *            软引用池的容量 默认30
         * @param locationThreadPoolNum
         *            本地图片线程池 默认 3
         * @param netThreadPoolNum
         *            网络图片线程池的线程数 默认 5
         */
        public static void init(int cachePoolNum, int locationThreadPoolNum,
                int netThreadPoolNum) {
            BasicParams.locationThreadPoolNum = locationThreadPoolNum;
            BasicParams.netThreadPoolNum = netThreadPoolNum;
            BasicParams.cachePoolNum = cachePoolNum;
        }
    }

    /**
     * 普通常量工具
     */
    public static class ConstantTool {

        // TODO(qingxia): Delete later.
        // public static final String DATA_PATH = Environment
        // .getExternalStorageDirectory().getPath() + "/Letv/";
        // public static final String IMAGE_CACHE_PATH = DATA_PATH +
        // "cache/pics/";
        public static final String IMAGE_CACHE_PATH = LeTVConfig
                .getImageCachePath();

    }

    /**
     * SD卡工具
     */
    public static class SDCardTool {

        /**
         * 检查是否装在sd卡
         */
        public static boolean sdCardMounted() {
            final String state = Environment.getExternalStorageState();
            return state.equals(Environment.MEDIA_MOUNTED)
                    && !state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
        }

        /**
         * 检查文件夹目录是否存在，不存在创建
         */
        public static boolean checkCacheDirectory(String path) {
            final File dir = new File(path);
            if (!dir.exists()) {
                final boolean isMkdirs = dir.mkdirs();
                return isMkdirs;
            }
            return true;
        }

        // TODO(qingxia): This class is never used.. Delete later.
        // /**
        // * 获取sd卡可用空间
        // */
        // public static long getAvailableSdCardSize() {
        // String path = Environment.getExternalStorageDirectory().getPath();
        // StatFs statFs = new StatFs(path);
        // long blockSize = statFs.getBlockSize();
        // int available = statFs.getAvailableBlocks();
        // return available * blockSize;
        // }

        /**
         * 获取图片大小
         */
        public static int getImageSize(Bitmap bmp) {
            try {
                if (bmp == null) {
                    return 0;
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmp.compress(CompressFormat.PNG, 100, baos);
                int size = baos.size();
                baos.flush();
                baos.close();
                return size;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }

        /**
         * 把文件大小转为M
         */
        public static String FormetFileSize(long fileS) {
            DecimalFormat df = new DecimalFormat("#0.00");
            String fileSizeString;

            fileSizeString = df.format((double) fileS / 1048576) + "M";

            return " " + fileSizeString + " ";
        }

        /**
         * 得到文件或文件夹大小
         */
        public static long getFileSize(File f) {
            long size = 0;
            File flist[] = f.listFiles();
            for (int i = 0; i < flist.length; i++) {
                if (flist[i].isDirectory()) {
                    size = size + getFileSize(flist[i]);
                } else {
                    size = size + flist[i].length();
                }
            }
            return size;
        }

        /**
         * 删除指定文件夹
         */
        public static void deleteAllFile(final String filePath,
                final cleanCacheListener listener) {
            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    File display = new File(filePath);
                    if (!display.exists()) {
                        listener.onNull();
                        return;
                    }
                    listener.onStar();
                    File[] items = display.listFiles();
                    int i = display.listFiles().length;
                    for (int j = 0; j < i; j++) {
                        if (items[j].isFile()) {
                            items[j].delete();
                        }
                        display.delete();
                    }
                    listener.onComplete();
                }
            });
            t.start();
        }

        /**
         * 清除缓存的监听器
         */
        public interface cleanCacheListener {
            public void onStar();

            public void onComplete();

            public void onErr();

            public void onNull();
        }
    }

    /**
     * 字符串工具
     */
    public static class StringTool {

        /**
         * 将url转换为缓存文件名
         */
        public static String createFileName(String url) {
            if (TextUtils.isEmpty(url)) {
                return null;
            }
            try {
                String name = url.replace(":", "");
                name = name.replace("/", "");
                String d = name.substring(name.lastIndexOf("."), name.length());
                name = name.replace(".", "");
                name = name + d + "letvimage";
                return name;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 将path转换为缓存文件名
         */
        public static String createFileNameByPath(String path) {
            if (TextUtils.isEmpty(path)) {
                return null;
            }

            String name = path.replace("/", "");
            String d = name.substring(name.lastIndexOf("."), name.length());
            name = name.replace(".", "");
            name = name + d + "letvimage";

            return name;
        }

        /**
         * 将url转换为路径
         */
        public static String createFilePath(String url) {
            if (TextUtils.isEmpty(url)) {
                return null;
            }

            String name = createFileName(url);

            return ConstantTool.IMAGE_CACHE_PATH + name;
        }

        /**
         * 将url转换为路径(非隐藏的)
         */
        public static String createFilePath2(String url) {
            if (TextUtils.isEmpty(url)) {
                return null;
            }

            String name = url.replace(":", "");
            name = name.replace("/", "");
            String d = name.substring(name.lastIndexOf("."), name.length());
            name = name.replace(".", "");
            name = name + d;

            return ConstantTool.IMAGE_CACHE_PATH + name;
        }
    }
}
