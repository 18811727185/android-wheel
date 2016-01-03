package com.letv.sysletvplayer.control.Interface;

import android.media.MediaPlayer;

import com.letv.sysletvplayer.control.base.BasePlayControllerImpl.TYPE3D;
import com.letv.sysletvplayer.listener.PlayerListener;

import java.util.Map;

/**
 * 播放相关,底层具体实现，供应用层调用
 * 提供各种play数据接口
 * @author caiwei
 */
public interface PlayControlInterface {
    /**
     * 调整比例类型
     */
    public enum AdjustType {
        ADJUST_TYPE_AUTO, // 自动比例,视频原始比例
        ADJUST_TYPE_4X3, // 4：3
        ADJUST_TYPE_16X9, // 16：9，即全屏
        ADJUST_TYPE_SMART// 智能比例，全屏和原始比例二选一,TvLive需求
    }

    public void setVideoPath(String path);// 设置视频路径

    public void setVideoPath(String path, Map<String, String> headers);// 设置视频路径，可增加自定义headers

    public void setVideoPath(String path, int startPosition);// 设置视频路径,带起播位置

    public void setVideoPath(String path, int startPosition,
            Map<String, String> headers);// 设置视频路径,带起播位置，可增加自定义headers

    public int getTotalBufferProgress();// 获取当前已缓冲的时长

    public void setTryPlayTime(int mTraPlayTime);// 设置试看时长

    /*
     * 点播时，用以设置服务器传来的该视频的总时长
     * 在由底层获取的时长值不正确时，会采用该时长作为视频总时长
     */
    public void setSpareDuration(int duration);

    public void start();// 开始播放

    public void play();// 播放

    public void pause();// 暂停

    public void stopPlayBack();// 停止播放

    public void forward(int rate);// 快进

    public void rewind(int rate);// 快退

    public MediaPlayer getMediaPlayer(); // 获取MediaPlayer对象

    public void resetPlay();// 销毁所有播放数据

    public void adjust(AdjustType type);// 比例调节

    public void seekTo(int mDuration);// 跳转到指定时长

    public boolean isPlaying();// 判断视频是否正在播放

    public boolean isPause();// 判断视频是否已暂停

    public int getVideoDuration();// 获取视频时长

    public String getPath();// 获取路径

    public int getCurrentPosition();// 获取当前播放位置

    public void handler3D(TYPE3D type);// 3d相关类型设置

    public boolean getIs3Dflag();// 获取当前是否是3D影片

    public void setIs3Dflag(boolean is3Dflag);// 设置是否是3D影片

    public void setPlayerListener(PlayerListener mPlayerListener);// 设置对播放控制的回调监听

    /**
     * 设置播放器音量,0表示静音,100最大音量;电i视系统非0代表系统音量
     */
    public void setVolume(int volume);

}
