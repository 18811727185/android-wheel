package com.letv.mobile.core.utils;

import android.content.Context;
import android.media.AudioManager;

public class VolumeUtils {
    public static final double C1_C1S_DIV = 15.0;
    public static final double OTHRER_DEVICE_DIV = 10.0;

    /**
     * 获取AudioManager
     * @param context
     * @return
     */
    public static AudioManager getAudioManager(Context context) {
        return (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * 获取当前音量
     * @param context
     * @return
     */
    public static int getCurrentStreamVolume(Context context,
            AudioManager manager) {
        if (context == null || manager == null) {
            return 0;
        }
        return manager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    /**
     * 获取当前音量
     * @param context
     * @return
     */
    public static int getCurrentStreamVolume(Context context) {
        if (context == null) {
            return 0;
        }
        AudioManager manager = getAudioManager(context);
        return getCurrentStreamVolume(context, manager);
    }

    /**
     * 获取最大音量
     * @param context
     * @return
     */
    public static int getMaxStreamVolume(Context context, AudioManager manager) {
        if (context == null || manager == null) {
            return 0;
        }
        return manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    /**
     * 获取最大音量
     * @param context
     * @return
     */
    public static int getMaxStreamVolume(Context context) {
        if (context == null) {
            return 0;
        }
        AudioManager manager = getAudioManager(context);
        return getMaxStreamVolume(context, manager);
    }

    /**
     * 设置音量
     * @param manager
     * @param direction
     *            1：增加音量 -1：减小音量 0：不变
     * @param div
     *            c1、c1s是15.0，第三方设备为10.0
     * @return 设置后
     */
    public static void setStreamVolume(AudioManager manager, int direction,
            double div) {
        if (manager == null) {
            return;
        }
        int currentVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        double interval = maxVolume / div;
        int currentIndex = (int) Math.rint(currentVolume / interval);
        double middleVolume;
        if (direction > 0) {
            middleVolume = (currentIndex + 1) * interval;
        } else if (direction < 0) {
            middleVolume = (currentIndex - 1) * interval;
        } else {
            middleVolume = currentIndex * interval;
        }
        int volume = (int) Math.rint(middleVolume);
        if (volume > maxVolume) {
            volume = maxVolume;
        } else if (volume < 0) {
            volume = 0;
        }
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

}
