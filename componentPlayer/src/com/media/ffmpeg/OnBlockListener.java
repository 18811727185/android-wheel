package com.media.ffmpeg;

import android.media.MediaPlayer;

/**
 * Created by zhangrui5 on 2015/9/17.
 */
public interface OnBlockListener {
	/**
     * 卡顿
     */
    void onBlock(MediaPlayer mediaPlayer, int blockInfo);
}
