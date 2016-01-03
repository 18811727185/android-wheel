package com.letv.sysletvplayer.setting;

import com.tvos.common.ThreeDimensionManager;
import com.tvos.common.TvManager.EnumScreenMuteType;
import com.tvos.common.exception.TvCommonException;
import com.tvos.common.vo.TvOsType.Enum3dType;
import com.tvos.common.vo.TvOsType.EnumInputSource;
import com.tvos.common.vo.TvOsType.EnumScalerWindow;

/**
 * 3d接口工具类
 * 针对3d片源自动调用3d合屏、分屏处理
 * @author jdan
 */
public class S3DSkinUtils {
    public final static boolean autoflag = false;
    private final static ThreeDimensionManager manager = new ThreeDimensionManager();
    private final static ThreeDimensionManager m3dM = com.tvos.common.TvManager
            .getThreeDimensionManager();

    private static EnumInputSource GetCurrentInputSource() {
        EnumInputSource curSourceType = EnumInputSource.E_INPUT_SOURCE_NONE;
        try {
            curSourceType = com.tvos.common.TvManager.getCurrentInputSource();
        } catch (TvCommonException e) {
            e.printStackTrace();
        }
        return curSourceType;
    }

    public static void set3dFormat(Enum3dType type) {
        try {
            com.tvos.common.TvManager.setVideoMute(true,
                    EnumScreenMuteType.E_BLACK, 0, GetCurrentInputSource());
            m3dM.enable3d(type);
            m3dM.set3dFormatDetectFlag(false);
            com.tvos.common.TvManager.setVideoMute(false,
                    EnumScreenMuteType.E_BLACK, 0, GetCurrentInputSource());
        } catch (TvCommonException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取当前播放3d类型
     */
    public static Enum3dType getType() {
        try {
            Enum3dType type = m3dM.getCurrent3dFormat();
            return type;
        } catch (TvCommonException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 模拟2d转3d(关闭、左右屏合并、上下屏合并)
     * @throws TvCommonException
     */
    public static void setDisplayFormat2D_TO_3D() throws TvCommonException {
        set3dFormat(Enum3dType.EN_3D_2DTO3D);
    }

    /**
     * 3d左右屏合屏设置
     * @throws TvCommonException
     */
    public static void setDisplayFormatSIDE_BY_SIDE_HALF()
            throws TvCommonException {
        set3dFormat(Enum3dType.EN_3D_SIDE_BY_SIDE_HALF);
    }

    /**
     * 3d上下屏合屏设置
     * @throws TvCommonException
     */
    public static void setDisplayFormatTOP_BOTTOM() throws TvCommonException {
        set3dFormat(Enum3dType.EN_3D_TOP_BOTTOM);
    }

    /**
     * 关闭3d
     * @throws TvCommonException
     */
    public static void setDisplayFormatNONE() throws TvCommonException {
        set3dFormat(Enum3dType.EN_3D_NONE);
    }

    private static boolean isSignalStable() {
        boolean signalStatus = false;
        try {
            if (com.tvos.common.TvManager.getPlayerManager().isSignalStable()) {
                signalStatus = true;
            } else {
                signalStatus = false;
            }
        } catch (TvCommonException e) {
            e.printStackTrace();
        }
        return signalStatus;
    }

    /**
     * 自动侦测并设置3d模式 待验证
     * @throws TvCommonException
     */
    public static void set3dAuto() {
        if (isSignalStable()) {
            try {
                com.tvos.common.TvManager.setVideoMute(true,
                        EnumScreenMuteType.E_BLACK, 0, GetCurrentInputSource());
                m3dM.enable3d(Enum3dType.EN_3D_NONE);
            } catch (TvCommonException e) {
                e.printStackTrace();
            }
            try {
                Enum3dType _3dtype;
                _3dtype = m3dM.detect3dFormat(EnumScalerWindow.E_MAIN_WINDOW);
                m3dM.enable3d(_3dtype);
                m3dM.set3dFormatDetectFlag(true);
                com.tvos.common.TvManager.setVideoMute(false,
                        EnumScreenMuteType.E_BLACK, 0, GetCurrentInputSource());
            } catch (TvCommonException e) {
                e.printStackTrace();
            }
        } else {

        }
    }
}
