package com.letv.sysletvplayer.control;

import android.content.Context;

import com.letv.sysletvplayer.control.base.BasePlayControllerImpl;
import com.letv.sysletvplayer.setting.BufferSetting;
import com.letv.sysletvplayer.setting.SpecialScreenSetting;
import com.mstar.android.tvapi.common.ThreeDimensionManager;
import com.mstar.android.tvapi.common.TvManager;
import com.mstar.android.tvapi.common.exception.TvCommonException;
import com.mstar.android.tvapi.common.vo.Enum3dType;
import com.mstar.android.tvapi.common.vo.EnumScalerWindow;

/**
 * TODO(caiwei):后续实现
 * @author caiwei
 */
public class PlayControllerImplForS250 extends BasePlayControllerImpl {

    private static final int MEDIA_INFO_VIDEO_FIRST_FRAME_DECODED = 1000;
    private SpecialScreenSetting screenSetting;
    private final ThreeDimensionManager m3dM = TvManager.getInstance()
            .getThreeDimensionManager();

    public PlayControllerImplForS250(Context context) {
        super(context);
    }

    // 设置3D转换
    @Override
    public void handler3D(TYPE3D type) {
        switch (type) {
        case FRC_3DMODE_FLAG:
            if (super.getIs3Dflag() == true) {
                this.set3dAuto();
            }
            break;
        default:
            break;
        }
    }

    public void set3dAuto() {
        if (this.isSignalStable()) {
            try {
                Enum3dType _3dtype;
                _3dtype = this.m3dM
                        .detect3dFormat(EnumScalerWindow.E_MAIN_WINDOW);
                this.m3dM.enable3d(_3dtype);
                this.m3dM.set3dFormatDetectFlag(true);
            } catch (TvCommonException e) {
                e.printStackTrace();
            }
        } else {
        }
    }

    private boolean isSignalStable() {
        boolean signalStatus = false;
        try {
            if (TvManager.getInstance().getPlayerManager().isSignalStable()) {
                signalStatus = true;
            } else {
                signalStatus = false;
            }
        } catch (TvCommonException e) {
            e.printStackTrace();
        }
        return signalStatus;
    }

    // 画面比例调整
    @Override
    public void adjustScreen(int type) {
        if (this.mPlayScreenSetting != null) {
            this.mPlayScreenSetting.adjust(type);
        }
    }

    @Override
    public int getTotalBufferProgress() {
        int totalDuration = this.getVideoDuration();
        int bufferPercentage = this.getBufferPercentage();
        return BufferSetting.getInstance().getTotalBufferProgressS50(
                totalDuration, bufferPercentage);
    }

    @Override
    public void setOnInfo(int what, int extra) {
        if (what == MEDIA_INFO_VIDEO_FIRST_FRAME_DECODED) {// 只针对S40/S50
            // 当收到消息1000后，再设置画面比例
            if (this.screenSetting != null && this.getIsFullScreen()) {
                this.screenSetting.setFirstFrameDecoded();
            }
        } else {
            BufferSetting.getInstance().setInfoBufferUpdateSelf(what, extra,
                    this.callBack);
        }
    }

}
