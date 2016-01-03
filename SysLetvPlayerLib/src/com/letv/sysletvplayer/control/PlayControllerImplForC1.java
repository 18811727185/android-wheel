package com.letv.sysletvplayer.control;

import android.app.SystemWriteManager;
import android.content.Context;
import android.media.MediaPlayer;

import com.letv.component.player.LetvMediaPlayerControl;
import com.letv.sysletvplayer.control.base.BasePlayControllerImpl;
import com.letv.sysletvplayer.listener.ControlListener;
import com.letv.sysletvplayer.setting.BufferSetting;
import com.letv.sysletvplayer.setting.RefreshBufferForCommon;
import com.letv.sysletvplayer.setting.Video3DSetting;

/**
 * @author caiwei
 */
public class PlayControllerImplForC1 extends BasePlayControllerImpl {

    private final Video3DSetting setting3d;

    public PlayControllerImplForC1(Context context) {
        super(context);
        SystemWriteManager sw = (SystemWriteManager) context
                .getSystemService("system_write");
        Video3DSetting.setSystemWrite(sw);
        this.setting3d = new Video3DSetting();
    }

    // 设置3D转换
    @Override
    public void handler3D(TYPE3D type) {
        switch (type) {
        case FRC_3DMODE_FLAG:
            if (super.getIs3Dflag() == true) {
                this.setting3d.changeTo3DMode(0);
            } else {
                this.setting3d.changeTo2DMode();
            }
            break;
        case FRC_3DMODE_2D:
            this.setting3d.changeTo2DMode();
            break;
        case FRC_3DMODE_2D_TO_3D:
            this.setting3d.changeTo3DMode(0);
            break;
        case FRC_3DMODE_3D_SIDE_BY_SIDE:
            this.setting3d.changeTo3DMode(0);
            break;
        case FRC_3DMODE_3D_TOP_N_BOTTOM:
            this.setting3d.changeTo3DMode(1);
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
     *
     * 获取C1的缓冲值，从底层返回，不加当前的播放位置
     * 针对老的缓冲策略
     * 与X60的方式一样
     * @return
     */
    public int getBufferX60() {
        MediaPlayer mp = this.getMediaPlayer();
        int buffer = BufferSetting.getInstance().getBufferProgressX60(mp);
        return buffer;
    }

    // 初始化刷新buffer的类
    private final RefreshBufferForCommon bufferSet = new RefreshBufferForCommon() {
        @Override
        public boolean isPlaying() {
            return PlayControllerImplForC1.this.isPlaying();
        };

        @Override
        public int getCurrentPosition() {
            return PlayControllerImplForC1.this.getCurrentPosition();
        }

        @Override
        public int getDuration() {
            return PlayControllerImplForC1.this.getVideoDuration();
        }

        @Override
        public BufferUpdateCallBack getBufferUpdateCallBack() {
            return PlayControllerImplForC1.this.callBack;
        }
    };

    @Override
    public void resetData() {
        this.bufferSet.handlerRemoveBuffer();
        // 销毁数据
        super.resetData();
    }

    @Override
    public void setOnPrePared() {
        super.setOnPrePared();
        this.refreshBuffer();
    }

    @Override
    public void refreshBuffer() {
        this.bufferSet.handlerStartRefreshBuffer();
    }

    @Override
    public void seekTo(int mDuration) {
        this.refreshBuffer();
        super.seekTo(mDuration);
    }

    // 画面比例调整
    @Override
    public void adjustScreen(int type) {
        this.mPlayScreenSetting.adjust(type);
    }

}
