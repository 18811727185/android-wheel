package com.media.ffmpeg;

/**
 * Created by zhangrui5 on 2015/9/17.
 */

import android.media.MediaPlayer;

/**
 * 硬解播放失败，需要切换到软解
 */
public interface OnHardDecodeErrorListner {
    void onError(MediaPlayer mediaPlayer, int arg1, int arg2);
}
