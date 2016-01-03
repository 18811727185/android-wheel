package com.letv.sysletvplayer.control;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.SystemProperties;

import com.letv.sysletvplayer.control.base.BasePlayControllerImpl;
import com.letv.sysletvplayer.setting.BufferSetting;
import com.letv.sysletvplayer.setting.RefreshBufferForX60;
import com.letv.sysletvplayer.util.PlayUtils;

/**
 * TODO(caiwei):后续实现
 * @author caiwei
 */
public class PlayControllerImplForX60 extends BasePlayControllerImpl {
    private static final String SYS_PROPERTIES_3DMODE = "persist.sys.3dmode";
    private static final String FRC_3DMODE_2D_TYPE = "0";
    private static final String FRC_3DMODE_2D_TO_3D_TYPE = "1";
    private static final String FRC_3DMODE_3D_SIDE_BY_SIDE_TYPE = "2";
    private static final String FRC_3DMODE_3D_TOP_N_BOTTOM_TYPE = "3";

    // 初始化刷新buffer的类
    private final RefreshBufferForX60 bufferSet = new RefreshBufferForX60() {
        @Override
        public int getBuffer() {// 缓冲值
            return PlayControllerImplForX60.this.getBufferX60();
        }

        @Override
        public int getBufferProgressPercent() {// 总缓冲百分比（底层返回的）
            int totalBuffer = PlayControllerImplForX60.this
                    .getTotalBufferProgress();
            int duration = PlayControllerImplForX60.this.getVideoDuration();
            if (duration == 0) {
                return 0;
            }
            return totalBuffer * 100 / duration;
        }

        @Override
        public int getBufferPercentage() {// 总缓冲百分比（android标准的）
            return PlayControllerImplForX60.this.getBufferPercentage();
        }

        @Override
        public BufferUpdateCallBack getBufferUpdateCallBack() {// 获取缓冲回调类
            return PlayControllerImplForX60.this.callBack;
        }
    };

    public PlayControllerImplForX60(Context context) {
        super(context);
    }

    @Override
    public void resetData() {
        // 销毁数据
        this.bufferSet.handlerStopRefresh();
        super.resetData();

    }

    @Override
    public void setOnPrePared() {
        this.refreshBuffer();
        super.setOnPrePared();
    }

    /**
     * 针对，Max 70,x60 2.3的rom 或 C1,C1S,NewC1S 使用老的缓冲策略
     */
    @Override
    public void refreshBuffer() {
        boolean bufferSelect = PlayUtils.isOldBufferSelect();
        if (bufferSelect) {
            this.bufferSet.startRefresh();
        } else {
        }
    }

    @Override
    public void seekTo(int mDuration) {
        this.refreshBuffer();
        super.seekTo(mDuration);
    }

    // 画面比例调整
    @Override
    public void adjustScreen(int type) {
        if (this.mPlayScreenSetting != null) {
            this.mPlayScreenSetting.adjust(type);
        }
    }

    @Override
    public void handler3D(TYPE3D type) {
        switch (type) {
        case FRC_3DMODE_FLAG:
            if (super.getIs3Dflag() == true) {
                SystemProperties.set(SYS_PROPERTIES_3DMODE,
                        FRC_3DMODE_3D_SIDE_BY_SIDE_TYPE);
            } else {
                SystemProperties.set(SYS_PROPERTIES_3DMODE, FRC_3DMODE_2D_TYPE);
            }
            break;
        case FRC_3DMODE_2D:
            SystemProperties.set(SYS_PROPERTIES_3DMODE, FRC_3DMODE_2D_TYPE);
            break;
        case FRC_3DMODE_2D_TO_3D:
            SystemProperties.set(SYS_PROPERTIES_3DMODE,
                    FRC_3DMODE_2D_TO_3D_TYPE);
            break;
        case FRC_3DMODE_3D_SIDE_BY_SIDE:
            SystemProperties.set(SYS_PROPERTIES_3DMODE,
                    FRC_3DMODE_3D_SIDE_BY_SIDE_TYPE);
            break;
        case FRC_3DMODE_3D_TOP_N_BOTTOM:
            SystemProperties.set(SYS_PROPERTIES_3DMODE,
                    FRC_3DMODE_3D_TOP_N_BOTTOM_TYPE);
            break;
        }

    }

    @Override
    public int getTotalBufferProgress() {
        int buffer = this.getBufferX60();
        int curPosition = this.getCurrentPosition();
        return buffer + curPosition;
    }

    /**
     * 获取X60的缓冲值，从底层返回，不加当前的播放位置
     * 针对老的缓冲策略
     * @return
     */
    public int getBufferX60() {
        MediaPlayer mp = this.getMediaPlayer();
        int buffer = BufferSetting.getInstance().getBufferProgressX60(mp);
        return buffer;
    }

    @Override
    public void setOnInfo(int what, int extra) {
        boolean bufferSelect = PlayUtils.isOldBufferSelect();
        if (!bufferSelect) { // x60,Max 70 且 3.0rom ,使用标准的缓冲策略
            BufferSetting.getInstance().setInfoBufferUpdateSelf(what, extra,
                    this.callBack);
        }
    }
}
