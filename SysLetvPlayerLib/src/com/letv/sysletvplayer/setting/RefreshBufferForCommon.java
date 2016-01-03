package com.letv.sysletvplayer.setting;

import android.os.Handler;
import android.os.Message;

import com.letv.mobile.core.utils.ThreadUtils;
import com.letv.sysletvplayer.control.base.BasePlayControllerImpl.BufferUpdateCallBack;

public abstract class RefreshBufferForCommon {
    private static final int JUDGE_NEED_BUFFER_TIMES = 4;
    private static final int BUFFER_DELAY_HALF_ONE_SECONDS = 500;
    private static final int BUFFER_START_OTHER_DEVICES = 300;// 开启刷新消息
    private final static int MSG_UPDATE_BUFFER = 1001;// 更新buffer界面消息
    private final static int MSG_NEED_BUFFER = 1002;// 显示buffer界面消息
    private final static int MSG_BUFFER_OVER = 1003;// 结束buffer界面消息

    private int mCurrentPlayPos = 0;// 记录播放器上次播放位置
    private int mTimeCounts = 0;// 记录handler循环次数
    private boolean isBuffering = false;
    private boolean isRefreshing = false;

    private final Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case BUFFER_START_OTHER_DEVICES:
                RefreshBufferForCommon.this.runThreadForClearQueue();
                break;
            case MSG_UPDATE_BUFFER:
                BufferSetting.getInstance().updateBuffer(
                        RefreshBufferForCommon.this.getBufferUpdateCallBack());
                break;
            case MSG_NEED_BUFFER:
                BufferSetting.getInstance().needBuffer(
                        RefreshBufferForCommon.this.getBufferUpdateCallBack());
                break;
            case MSG_BUFFER_OVER:
                BufferSetting.getInstance().bufferOver(
                        RefreshBufferForCommon.this.getBufferUpdateCallBack());
                break;
            }
        }
    };

    public abstract boolean isPlaying();

    public abstract int getCurrentPosition();

    public abstract int getDuration();

    public abstract BufferUpdateCallBack getBufferUpdateCallBack();// 获取buffer回调对象

    /**
     * 发送缓冲轮循开始消息（第三方设备使用）
     */
    public void handlerStartRefreshBuffer() {
        if (!this.isRefreshing) {
            this.isRefreshing = true;
            this.myHandler.removeMessages(BUFFER_START_OTHER_DEVICES);
            this.myHandler.sendEmptyMessage(BUFFER_START_OTHER_DEVICES);
        }
    }

    /**
     * 清除缓冲轮循消息（第三方设备使用）
     */
    public void handlerRemoveBuffer() {
        if (this.isRefreshing) {
            this.isRefreshing = false;
            this.mCurrentPlayPos = 0;
            this.handlerBufferOver();
            this.myHandler.removeMessages(BUFFER_START_OTHER_DEVICES);
        }
    }

    private void runThreadForClearQueue() {
        ThreadUtils.startRunInThreadForClearQueue(new Runnable() {
            @Override
            public void run() {
                RefreshBufferForCommon.this.judgeBuffer4OtherDevices();
            }
        });
    }

    // 开始buffer
    private void handlerStartBuffer() {
        this.isBuffering = true;
        this.myHandler.removeMessages(MSG_NEED_BUFFER);
        this.myHandler.sendEmptyMessage(MSG_NEED_BUFFER);
    }

    // 更新buffer
    private void handlerUpdateBuffer() {
        this.myHandler.removeMessages(MSG_UPDATE_BUFFER);
        this.myHandler.sendEmptyMessage(MSG_UPDATE_BUFFER);
    }

    // 结束buffer
    private void handlerBufferOver() {
        if (this.isBuffering) {
            this.isBuffering = false;
            this.myHandler.removeMessages(MSG_BUFFER_OVER);
            this.myHandler.sendEmptyMessage(MSG_BUFFER_OVER);
        }
    }

    /**
     * 判断播放影片时是否需要显示缓冲进度（第三方设备使用）
     */
    private void judgeBuffer4OtherDevices() {
        this.myHandler.removeMessages(BUFFER_START_OTHER_DEVICES);
        int curPos = this.getCurrentPosition() / 1000;
        int duration = this.getDuration();
        if (duration > 1) {
            if (curPos == duration / 1000) {// 播放完成
                this.handlerRemoveBuffer();
                return;
            }
            // 正在播放
            if (curPos == this.mCurrentPlayPos && this.isPlaying()) {
                this.mTimeCounts++;
                if (this.mTimeCounts == JUDGE_NEED_BUFFER_TIMES) {
                    this.mTimeCounts = 0;
                    if (!this.isBuffering) {
                        this.handlerStartBuffer();
                    }
                }
                if (this.isBuffering) {
                    this.handlerUpdateBuffer();
                }
            } else {
                this.mTimeCounts = 0;
                this.handlerBufferOver();
            }
            this.mCurrentPlayPos = curPos;
        }
        this.myHandler.sendEmptyMessageDelayed(BUFFER_START_OTHER_DEVICES,
                BUFFER_DELAY_HALF_ONE_SECONDS);
    }
}