package com.letv.sysletvplayer.setting;

import android.media.MediaPlayer;
import android.os.Parcel;

import com.letv.sysletvplayer.control.base.BasePlayControllerImpl.BufferUpdateCallBack;

/**
 * 缓冲进度设置
 * @author caiwei
 */
public class BufferSetting {
    private final static String IMEDIA_PLAYER = "android.media.IMediaPlayer";
    public static final int MEDIA_INFO_BUFFERING_START = 701;// 缓冲开始
    public static final int MEDIA_INFO_BUFFERING_END = 702;// 缓冲结束
    public static final int MEDIA_INFO_BUFFERING_PERCENT = 704;// 缓冲进度
    private static BufferSetting sInstance;

    private BufferSetting() {
    }

    public static BufferSetting getInstance() {
        if (sInstance == null) {
            synchronized (BufferSetting.class) {
                if(sInstance == null) {
                    sInstance = new BufferSetting();
                }
            }
        }
        return sInstance;
    }

    public static void unitInstance() {
        sInstance = null;
    }

    public int getTotalBufferProgressCommon(int totalDuration,
            int bufferPercentage) {
        return this.getTotalBufferProgressS50(totalDuration, bufferPercentage);
    }

    /**
     * 获取当前缓冲值X60
     * （不包括当前播放位置）
     * @return
     */
    public int getBufferProgressX60(MediaPlayer mp) {
        if (mp == null) {
            return 0;
        }
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            request.writeInterfaceToken(IMEDIA_PLAYER);
            request.writeInt(100);
            mp.invoke(request, reply);
            int size = reply.readInt();
            return size >>> 7;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            request.recycle();
            reply.recycle();
        }
    }

    /**
     * 获取当前已缓冲的总时长值S50
     * @return
     */
    public int getTotalBufferProgressS50(int totalDuration, int bufferPercentage) {
        int buffer = bufferPercentage * totalDuration / 100;
        return buffer;
    }

    /**
     * 自有设备通过info的返回值来设置显示buffer
     * @param what
     * @param exta
     */
    public void setInfoBufferUpdateSelf(int what, int extra,
            BufferUpdateCallBack callBack) {
        switch (what) {
        case MEDIA_INFO_BUFFERING_START:
            this.needBuffer(callBack);
            break;
        case MEDIA_INFO_BUFFERING_END:
            this.bufferOver(callBack);
            break;
        case MEDIA_INFO_BUFFERING_PERCENT:
            this.updateBuffer(extra, callBack);
            break;
        }
    }

    /**
     * 开始buffer
     * @param percent
     * @param callBack
     */
    public void needBuffer(BufferUpdateCallBack callBack) {
        if (callBack != null) {
            callBack.onNeedBuffer();
        }
    }

    /**
     * 更新buffer,设备没有返回进度
     * @param percent
     * @param callBack
     */
    public void updateBuffer(BufferUpdateCallBack callBack) {
        if (callBack != null) {
            callBack.onBufferUpdating(0);
        }
    }

    /**
     * 更新buffer
     * @param percent
     * @param callBack
     */
    public void updateBuffer(int percent, BufferUpdateCallBack callBack) {
        if (callBack != null) {
            callBack.onBufferUpdating(percent);
        }
    }

    /**
     * 结束buffer
     * @param callBack
     */
    public void bufferOver(BufferUpdateCallBack callBack) {
        if (callBack != null) {
            callBack.onBufferOver();
        }
    }
}