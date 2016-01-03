package com.letv.mobile.core.imagecache;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.letv.mobile.core.imagecache.LetvCacheTools.ConstantTool;
import com.letv.mobile.core.imagecache.LetvCacheTools.SDCardTool;
import com.letv.mobile.core.imagecache.LetvCacheTools.SDCardTool.cleanCacheListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * 缓存管理者，实现了对外调用的方法
 */
public final class LetvCacheMannager {

    /**
     * 动画时间
     */
    public static final int ANIMATION_DURATION = 800;

    /**
     * 构造方法
     */
    private LetvCacheMannager() {
    }

    private static class InstanceHolder {
        static final LetvCacheMannager cacheManager = new LetvCacheMannager();
        static final ImageLoader imageLoader = ImageLoader.getInstance();
        static final SimpleImageLoadingListener animationListener = new SimpleImageLoadingListener() {

            @Override
            public void onLoadingComplete(String imageUri, View view,
                    Bitmap loadedImage) {
                FadeInBitmapDisplayer.animate(view, ANIMATION_DURATION);
            }

        };
    }

    /**
     * 得到实例
     */
    public static LetvCacheMannager getInstance() {
        return InstanceHolder.cacheManager;
    }

    /**
     * 初始化
     */
    public void init(Context context) {
        LetvCacheConfiguration.initCacheLibrary(context);
    }

    /**
     * 下载网络图片,不显示
     */
    public void loadPrestrainImage(String imgUrl, ImageLoadingListener listener) {
        InstanceHolder.imageLoader.loadImage(imgUrl, listener);
    }

    /**
     * 加载图片
     */
    public void loadImage(final String url, final ImageView imageView) {
        if (imageView == null) {
            return;
        }
        InstanceHolder.imageLoader.displayImage(url, imageView);
    }

    /**
     * 加载图片
     */
    public void loadImageWithAnimation(final String url,
            final ImageView imageView) {
        if (imageView == null) {
            return;
        }
        InstanceHolder.imageLoader.displayImage(url, imageView,
                InstanceHolder.animationListener);
    }

    /**
     * 加载图片
     */
    public void loadImage(final String url, final ImageView imageView,
            DisplayImageOptions options) {
        if (imageView == null) {
            return;
        }

        InstanceHolder.imageLoader.displayImage(url, imageView, options);
    }

    /**
     * 加载图片
     */
    public void loadImageWithAnimation(final String url,
            final ImageView imageView, DisplayImageOptions options) {
        if (imageView == null) {
            return;
        }

        InstanceHolder.imageLoader.displayImage(url, imageView, options,
                InstanceHolder.animationListener);
    }

    /**
     * 加载图片
     */
    public void loadImage(final String url, final ImageView imageView,
            ImageLoadingListener loadingListener) {
        if (imageView == null) {
            return;
        }

        InstanceHolder.imageLoader
                .displayImage(url, imageView, loadingListener);
    }

    /**
     * 加载图片
     */
    public void loadImage(final String url, final ImageView imageView,
            DisplayImageOptions options, ImageLoadingListener loadingListener) {
        if (imageView == null) {
            return;
        }

        InstanceHolder.imageLoader.displayImage(url, imageView, options,
                loadingListener);
    }

    /**
     * 取消下载图片
     * @param imageView
     */
    public void cancelLoadImage(ImageView imageView) {
        if (imageView == null) {
            return;
        }
        InstanceHolder.imageLoader.cancelDisplayTask(imageView);
    }

    /**
     * 加载本地视频截图
     */
    public void loadVideoImage(String path, final ImageView imageView) {
        path = "LetvThumbnailUtils" + path;
        InstanceHolder.imageLoader.displayImage(path, imageView);
    }

    /**
     * 下载网络图片
     */
    public void loadImageSync(String imgUrl) {
        if (TextUtils.isEmpty(imgUrl)) {
            return;
        }

        InstanceHolder.imageLoader.loadImageSync(imgUrl);
    }

    /**
     * 销毁缓存对象，外部工程无需直接调用
     */
    public void destroy() {
        if (InstanceHolder.imageLoader.isInited()) {
            InstanceHolder.imageLoader.stop();
            InstanceHolder.imageLoader.clearMemoryCache();
            InstanceHolder.imageLoader.destroy();
        }
    }

    /**
     * 销毁缓存对象，外部工程无需直接调用
     */
    public void clearCacheBitmap() {
        try {
            if (InstanceHolder.imageLoader.isInited()) {
                InstanceHolder.imageLoader.clearMemoryCache();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清除缓存
     */
    public static void cleanCache(cleanCacheListener listener) {
        if (SDCardTool.sdCardMounted()) {
            SDCardTool.deleteAllFile(ConstantTool.IMAGE_CACHE_PATH, listener);
        } else {
            listener.onErr();
        }
    }

    /**
     * 清除缓存目录大小
     */
    public static String getCacheSize() {
        if (SDCardTool.sdCardMounted()) {
            File file = new File(ConstantTool.IMAGE_CACHE_PATH);
            if (file.exists()) {
                long size = SDCardTool.getFileSize(file);

                return SDCardTool.FormetFileSize(size);
            } else {
                return " 0.00M ";
            }
        } else {
            return "";
        }
    }
}
