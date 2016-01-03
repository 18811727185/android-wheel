package com.letv.sysletvplayer.listener;

import java.util.Map;

/**
 * BasePlayController的回调，提供给BaseviewController
 * @author caiwei
 */
public interface ControlListener {

    public void onPrePared();// 视频准备完成

    /**
     * 视频暂停
     */
    public void onPause();

    public void onSetVideoPath(String path, Map<String, String> headers);

    /**
     * 跳转到某处
     * @param mDuration
     */
    public void onSeekTo(int mDuration);

    /**
     * 视频设置试看时长
     * @param mTraPlayTime
     */
    public void onSetTryPlayTime(int mTraPlayTime);

    /**
     * 视频被停止
     */
    public void onStopPlayBack();

    /**
     * 视频开始缓冲，需要显示buffer界面
     * @param percent
     */
    public void onNeedBuffer(int progress);

    /**
     * 视频缓冲中，更新buffer界面
     * @param percent
     */
    public void onBufferUpdating(int progress);

    /**
     * 视频缓冲结束，隐藏buffer界面
     * @param percent
     */
    public void onBufferOver();
}