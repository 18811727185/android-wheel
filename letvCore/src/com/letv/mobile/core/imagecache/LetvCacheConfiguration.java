package com.letv.mobile.core.imagecache;

import java.io.File;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

public class LetvCacheConfiguration {

    // TODO(qingxia): Check those const meaning.
    @SuppressWarnings("unused")
    private static final int MAX_FILE_COUNT = 100;
    @SuppressWarnings("unused")
    private static final int MAX_FILE_TOTAL_SIZE = 20 * 1024 * 1024;

    public static DisplayImageOptions getDisplayOptions(Drawable defaultDrawable) {
        return new DisplayImageOptions.Builder()
                .showImageForEmptyUri(defaultDrawable)
                .showImageOnFail(defaultDrawable).resetViewBeforeLoading(true)
                .showImageOnLoading(defaultDrawable).cacheInMemory(true)
                .cacheOnDisk(true).build();
    }

    /**
     * @param defaultDrawable
     *            默认图
     * @param roundPixels
     *            圆角度
     * @return
     * @author yangn
     */
    public static DisplayImageOptions getDisplayOptions(
            Drawable defaultDrawable, int roundPixels) {
        return new DisplayImageOptions.Builder()
                .showImageForEmptyUri(defaultDrawable)
                .showImageOnFail(defaultDrawable).resetViewBeforeLoading(true)
                .showImageOnLoading(defaultDrawable).cacheInMemory(true)
                .cacheOnDisk(true)
                .displayer(new RoundedBitmapDisplayer(roundPixels)).build();
    }

    /**
     * 初始化缓存工具
     */
    public static void initCacheLibrary(Context context) {

        DisplayImageOptions opts = getDisplayOptions(null);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context)
                .memoryCache(getMemoryCache(context))
                .diskCache(getDiscCache())
                // 本地缓存配置
                .discCacheFileCount(MAX_FILE_COUNT).threadPoolSize(2)
                .threadPriority(Thread.MIN_PRIORITY)
                .denyCacheImageMultipleSizesInMemory()
                // 一个URL对应一个图片
                .imageDownloader(new BaseImageDownloader(context))
                .tasksProcessingOrder(QueueProcessingType.LIFO)// 任务队列执行顺序 后进先出
                .defaultDisplayImageOptions(opts).build();

        ImageLoader.getInstance().init(config);
    }

    /**
     * 缓存配置
     */
    public static LruMemoryCache getMemoryCache(Context context) {
        int cacheSize = 4 * 1024 * 1024;
        try {
            int memClass = ((ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE))
                    .getMemoryClass();
            int availableSize = memClass >> 3;
            cacheSize = 1024 * 1024 * (availableSize == 0 ? 4 : availableSize);
            Log.d("ljn", "getMemoryCache---memClass:" + memClass
                    + "----availableSize:" + availableSize);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        LruMemoryCache memoryCache = new LruMemoryCache(cacheSize);

        return memoryCache;
    }

    /**
     * 缓存目录生成，
     * 目的地：./letv/cache/pics/
     * 命名：MD5加密
     * 最大大小：50兆
     */
    public static DiskCache getDiscCache() {
        final File dir = new File(LetvCacheTools.ConstantTool.IMAGE_CACHE_PATH);

        /**
         * note:UnlimitedDiscCache is pretty faster than other limited disk
         * cache implementations
         */
        DiskCache diskCache = new UnlimitedDiscCache(dir, null,
                new Md5FileNameGenerator());

        return diskCache;
    }
}
