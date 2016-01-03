package com.letv.sysletvplayer.control;

import android.content.Context;

import com.letv.sysletvplayer.control.base.BasePlayControllerImpl;
import com.letv.sysletvplayer.setting.BufferSetting;
import com.letv.sysletvplayer.setting.S3DSkinUtils;
import com.letv.sysletvplayer.setting.SpecialScreenSetting;
import com.tvos.common.exception.TvCommonException;
import com.tvos.common.vo.TvOsType.Enum3dType;

/**
 * TODO(caiwei):后续实现
 * @author caiwei
 */
public class PlayControllerImplForS50 extends BasePlayControllerImpl {
    private static final int MEDIA_INFO_VIDEO_FIRST_FREME_DECODED = 1000;
    private SpecialScreenSetting screenSetting;

    public PlayControllerImplForS50(Context context) {
        super(context);
    }

    @Override
    public void adjustScreen(int type) {
        // S50系列的画面比例调整，需要调用底层服务接口
        if (this.screenSetting == null) {
            this.screenSetting = new SpecialScreenSetting();
        }
        this.screenSetting.adjust(type);
    }

    // 设置3D转换
    @Override
    public void handler3D(TYPE3D type) {
        try {
            switch (type) {
            case FRC_3DMODE_FLAG:
                if (super.getIs3Dflag() == true) {
                    if (S3DSkinUtils.getType() != (Enum3dType.EN_3D_SIDE_BY_SIDE_HALF)) {
                        S3DSkinUtils.setDisplayFormatSIDE_BY_SIDE_HALF();
                    }
                } else {
                    if (S3DSkinUtils.getType() != Enum3dType.EN_3D_NONE) {
                        S3DSkinUtils.setDisplayFormatNONE();
                    }
                }
                break;
            case FRC_3DMODE_2D:
                if (S3DSkinUtils.getType() != Enum3dType.EN_3D_NONE) {
                    S3DSkinUtils.setDisplayFormatNONE();
                }
                break;
            case FRC_3DMODE_3D_SIDE_BY_SIDE:
                if (S3DSkinUtils.getType() != (Enum3dType.EN_3D_SIDE_BY_SIDE_HALF)) {
                    S3DSkinUtils.setDisplayFormatSIDE_BY_SIDE_HALF();
                }
                break;
            default:
                break;
            }
        } catch (TvCommonException e) {
            e.printStackTrace();
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
        if (what == MEDIA_INFO_VIDEO_FIRST_FREME_DECODED) {// 只针对S40/S50
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
