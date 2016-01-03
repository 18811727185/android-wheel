package com.media.ffmpeg;

/**
 * Created by zhangrui5 on 2015/9/17.
 */
public interface OnAdNumberListener {
	/**
     * @param mediaPlayer
     * @param number 0 是第一个广告 1是第二个广告 2是第三个广告
     */
    void onAdNumber(FFMpegPlayer mediaPlayer, int number);
}
