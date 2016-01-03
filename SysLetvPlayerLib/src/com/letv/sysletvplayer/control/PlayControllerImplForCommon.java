package com.letv.sysletvplayer.control;

import android.content.Context;

import com.letv.component.player.LetvMediaPlayerControl;
import com.letv.sysletvplayer.control.base.BasePlayControllerImpl;
import com.letv.sysletvplayer.listener.ControlListener;
import com.letv.sysletvplayer.setting.BufferSetting;
import com.letv.sysletvplayer.setting.RefreshBufferForCommon;

/**
 * TODO(caiwei):后续实现
 * @author caiwei
 */
public class PlayControllerImplForCommon extends BasePlayControllerImpl {
    // 初始化刷新buffer的类
    private final RefreshBufferForCommon bufferSet = new RefreshBufferForCommon() {
        @Override
        public boolean isPlaying() {
            return PlayControllerImplForCommon.this.isPlaying();
        };

        @Override
        public int getCurrentPosition() {
            return PlayControllerImplForCommon.this.getCurrentPosition();
        }

        @Override
        public int getDuration() {
            return PlayControllerImplForCommon.this.getVideoDuration();
        }

        @Override
        public BufferUpdateCallBack getBufferUpdateCallBack() {
            return PlayControllerImplForCommon.this.callBack;
        }
    };

    public PlayControllerImplForCommon(Context context) {
        super(context);
    }

    @Override
    public void resetData() {
        this.bufferSet.handlerRemoveBuffer();
        // 销毁数据
        super.resetData();
    }

    @Override
    public void init(ControlListener mControlListener,
            LetvMediaPlayerControl mControl, Context mContext) {
        super.init(mControlListener, mControl, mContext);
        // 第三方需要设置SurfaceView在onMeasure()时改变播放窗口尺寸
        //this.mControl.setScreenChangeFlag(true);
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
        if (this.mPlayScreenSetting != null) {
            this.mPlayScreenSetting.adjust(type);
        }
    }

    @Override
    public void handler3D(TYPE3D type) {
    }

    @Override
    public int getTotalBufferProgress() {
        int totalDuration = this.getVideoDuration();
        int bufferPercentage = this.getBufferPercentage();
        return BufferSetting.getInstance().getTotalBufferProgressCommon(
                totalDuration, bufferPercentage);
    }
}
