package com.letv.sysletvplayer.listener;

import android.media.MediaPlayer;

import com.letv.component.player.Interface.OnMediaStateTimeListener;

/**
 * 播放的回调，提供给应用层作出对应响应
 * @author caiwei
 */
public interface PlayerListener {
    /**
     * seek 完成
     */
    public void onSeekComplete();

    /**
     * 
     */
    public void onPrePared();

    /**
     * 视频快进
     */
    public void onForward();

    /**
     * 视频快退
     */
    public void onRewind();

    /**
     * seek进度
     * @param progress
     */
    public void onSeekTo(int progress);

    /**
     * 播放完成
     */
    public void onCompletion();

    /**
     * 开始缓冲
     */
    public void onNeedBuffer(int progress);

    /**
     * 更新缓冲
     * @param progress
     *            缓冲进度
     */
    public void onBufferUpdating(int progress);

    /**
     * 缓冲结束
     */
    public void onBufferOver();

    /**
     * 播放消息
     * @param arg1
     * @param arg2
     */
    public void onInfo(int arg1, int arg2);

    /**
     * 播放错误
     * @param type
     */
    public void onError(int arg1, int arg2);

    /**
     * 视频界面尺寸发生变化
     */
    public void onVideoSizeChanged(MediaPlayer mp, int w, int h);

    // TODO(caiwei):播放回调接口，后续可能新增这些接口
    // public void onBack();
    // public void onHalf();
    // public void onFull();
    // public void onShare(String content);
    // public void onZoom(int type);
    // public void onSoundChange(int cur, int max);

    public void onMediaStateTime(
            OnMediaStateTimeListener.MeidaStateType mStateType, String time);
}