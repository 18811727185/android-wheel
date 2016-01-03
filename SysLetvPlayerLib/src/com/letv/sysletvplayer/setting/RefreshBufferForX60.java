package com.letv.sysletvplayer.setting;

import android.os.Handler;
import android.os.Message;

import com.letv.mobile.core.utils.ThreadUtils;
import com.letv.sysletvplayer.control.base.BasePlayControllerImpl.BufferUpdateCallBack;
import com.letv.sysletvplayer.util.PlayUtils;

/**
 * 兼容 X60系列2.3的rom
 * 需要主动刷新buffer进度
 * @author caiwei
 */
public abstract class RefreshBufferForX60 {

    private static final int DEAILY_SECOND_BUFFER = 1000;// 延迟一秒发送消息请求Buffer进度
    protected static final int MSG_AUTO_REFRESH_BUFFER = 300;// 自动刷新buffer进度
    private final static int MSG_UPDATE_BUFFER = 1001;// 更新buffer界面消息
    private final static int MSG_NEED_BUFFER = 1002;// 显示buffer界面消息
    private final static int MSG_BUFFER_OVER = 1003;// 结束buffer界面消息

    private final int minbuffertime = PlayUtils.BUFFER_MIN_TIME;// 播放最低缓冲时间
    private final int streamRate = PlayUtils.STREAM_RATE;
    private boolean isBuffering = false;

    public abstract int getBuffer();// 获取底层真实的缓冲值(不加当前播放位置)

    public abstract int getBufferProgressPercent();// 获取底层真实的缓冲总进度比

    public abstract int getBufferPercentage();// 获取标准android接口返回的缓冲总进度比

    public abstract BufferUpdateCallBack getBufferUpdateCallBack();// 获取buffer回调对象

    private final Handler myHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case MSG_AUTO_REFRESH_BUFFER:
                RefreshBufferForX60.this.onUpdateing();
                break;
            case MSG_UPDATE_BUFFER:
                BufferSetting.getInstance().updateBuffer(
                        RefreshBufferForX60.this.getBufferUpdateCallBack());
                break;
            case MSG_NEED_BUFFER:
                BufferSetting.getInstance().needBuffer(
                        RefreshBufferForX60.this.getBufferUpdateCallBack());
                break;
            case MSG_BUFFER_OVER:
                BufferSetting.getInstance().bufferOver(
                        RefreshBufferForX60.this.getBufferUpdateCallBack());
                break;
            }

        };
    };

    /**
     * 开始刷新buffer
     */
    public void startRefresh() {
        this.myHandler.removeMessages(MSG_AUTO_REFRESH_BUFFER);
        this.myHandler.sendEmptyMessage(MSG_AUTO_REFRESH_BUFFER);
    }

    /**
     * 移除刷新
     */
    public void handlerStopRefresh() {
        this.handlerBufferOver();
        this.myHandler.removeMessages(MSG_AUTO_REFRESH_BUFFER);
    }

    // 延时1s刷新
    private void autoStartRefreshDelayed() {
        this.myHandler.removeMessages(MSG_AUTO_REFRESH_BUFFER);
        this.myHandler.sendEmptyMessageDelayed(MSG_AUTO_REFRESH_BUFFER,
                DEAILY_SECOND_BUFFER);
    }

    // 发送开始buffer的消息
    private void handlerNeedBuffer() {
        this.isBuffering = true;
        this.myHandler.removeMessages(MSG_NEED_BUFFER);
        this.myHandler.sendEmptyMessage(MSG_NEED_BUFFER);
    }

    // 发送刷新buffer的消息
    private void handlerUpdateBuffer(int percent) {
        this.myHandler.removeMessages(MSG_UPDATE_BUFFER);
        Message msg = Message.obtain();
        msg.what = MSG_UPDATE_BUFFER;
        msg.arg1 = percent;
        this.myHandler.sendMessage(msg);
        msg = null;
    }

    // 发送结束buffer的消息
    private void handlerBufferOver() {
        if (this.isBuffering) {
            this.isBuffering = false;
            this.myHandler.removeMessages(MSG_BUFFER_OVER);
            this.myHandler.sendEmptyMessage(MSG_BUFFER_OVER);
        }
    }

    private int getStreamrate() {
        return this.streamRate;
    }

    /**
     * 判断如果总缓冲百分比达到100%，就不再显示buffer界面
     */
    private boolean judgePercentage100() {
        if (this.getBufferPercentage() == 100) {
            this.handlerBufferOver();
            return true;
        }
        return false;
    }

    /**
     * 启动子线程刷新
     */
    private void onUpdateing() {
        if (this.judgePercentage100()) {
            return;
        }
        this.runThreadForClearQueue(this.minbuffertime);
    }

    private void runThreadForClearQueue(final int minbuffertime) {
        ThreadUtils.startRunInThreadForClearQueue(new Runnable() {
            @Override
            public void run() {
                if (RefreshBufferForX60.this.judgePercentage100()) {
                    return;
                }
                float buffer = RefreshBufferForX60.this.getBuffer();
                int videoStreamRate = RefreshBufferForX60.this.getStreamrate();
                int mustBytes = videoStreamRate * minbuffertime;
                int progress = PlayUtils.getBufferProgress(buffer, mustBytes);
                RefreshBufferForX60.this.handlerBuffer(progress);
                RefreshBufferForX60.this.autoStartRefreshDelayed();
            }
        });
    }

    /**
     * 根据缓冲进度设置buffer
     * @param progress
     */
    private void handlerBuffer(int progress) {
        if (progress == 0) {
            RefreshBufferForX60.this.handlerNeedBuffer();
        } else if (progress >= 99) {
            this.handlerBufferOver();
        } else {
            if (!this.isBuffering) {
                RefreshBufferForX60.this.handlerNeedBuffer();
            } else {
                this.handlerUpdateBuffer(progress);
            }
        }
    }
}