package com.letv.sysletvplayer.util;

import com.letv.mobile.core.utils.DeviceUtils;
import com.letv.mobile.core.utils.DeviceUtils.DeviceType;
import com.letv.mobile.core.utils.DeviceUtils.UIVersion;

public class PlayUtils {
    public static final int MEDIA_INFO_RENDERING_START = 3;
    /**
     * 小于多长时间不可播放s
     */
    public final static int MUST_PLAY_MIN_TIME = 1;
    /**
     * 缓冲多长时间可以播放s，点播
     */
    public final static int BUFFER_MIN_TIME = 5;

    /**
     * 随机进度数
     */
    public final static int RANDOM_PROGRESS = 99;

    public final static int MIN_RARANDOM_PROGRESS = 4;
    /**
     * 码流
     */
    public final static int STREAM_RATE = 585;
    private static boolean bufferSelect;// 标识位，判断设备是不是符合设置起播时的高低水位
    private static boolean newSeekSelect;// 标识位，判断设备是不是符合在起播前设置seek点

    private static DeviceType mDeviceType = null;// 设备类型
    private static UIVersion mUiversion = null;// rom版本

    private PlayUtils() {
    }

    // 是不是符合设置高低水位的设备
    public static boolean isBufferSelect() {
        if (bufferSelect == false) {
            DeviceType type = getDeviceType();
            boolean isDeviceSelected = (type == DeviceType.DEVICE_X60
                    || type == DeviceType.DEVICE_MAX70 || type == DeviceType.DEVICE_S250F
                    || type == DeviceType.DEVICE_S250U || type == DeviceType.DEVICE_S240F);
            bufferSelect = (IsUIVersion30() && isDeviceSelected);
        }
        return bufferSelect;
    }

    /**
     * 返回设备类型
     * @return
     */
    public static DeviceType getDeviceType() {
        if (mDeviceType == null) {
            mDeviceType = DeviceUtils.getDeviceType();
        }
        return mDeviceType;
    }

    /**
     * 返回Rom版本
     * @return
     */
    public static UIVersion getUIVersion() {
        if (mUiversion == null) {
            mUiversion = DeviceUtils.getUIVersion();
        }
        return mUiversion;
    }

    /**
     * 是否是使用老的缓冲策略，该缓冲策略不使用android标准接口
     * 针对，Max 70,x60 2.3的rom 或 C1,C1S,NewC1S
     * @return true 使用老的缓冲策略 false 不是
     */
    public static boolean isOldBufferSelect() {
        // X60或Max70且不是3.0的rom
        DeviceType type = getDeviceType();
        if ((type == DeviceType.DEVICE_X60 || type == DeviceType.DEVICE_MAX70)
                && !DeviceUtils.isUI30orHigher()) {
            return true;
        }
        // C1或C1S或newC1s
        if (type == DeviceType.DEVICE_C1 || type == DeviceType.DEVICE_C1S
                || type == DeviceType.DEVICE_NEWC1S) {
            return true;
        }
        return false;
    }

    // UI version 是否是3.0
    private static boolean IsUIVersion30() {
        UIVersion uiversion = DeviceUtils.getUIVersion();
        return uiversion == UIVersion.UIVERSION_30;
    }

    // 是不是符合起播前设置seek点的设备
    public static boolean isNewSeekSelect() {
        if (newSeekSelect == false) {
            DeviceType type = getDeviceType();
            boolean isDeviceSelected = (type == DeviceType.DEVICE_X60 || type == DeviceType.DEVICE_MAX70);
            if (IsUIVersion30() && isDeviceSelected) {
                newSeekSelect = true;
            }
        }
        return newSeekSelect;
    }

    public static int getBufferProgress(float buffer_byte, int total) {
        if (buffer_byte >= total) {
            return 100;
        }
        float progress = buffer_byte / total;
        return (int) (progress * 100);
    }

}
