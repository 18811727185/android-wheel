package com.letv.sysletvplayer.setting;

import com.tvos.common.TvManager;
import com.tvos.common.exception.TvCommonException;
import com.tvos.common.vo.TvOsType.EnumVideoArcType;

/**
 * S50系列调整画面比例
 * @author caiwei
 */
public class SpecialScreenSetting {

    private int currentRatio = 0;

    /**
     * 调整比例
     * @param type
     *            0为自适应；1为4：3 ；2为16：9
     */
    public void adjust(int type) {
        this.currentRatio = type;
        this.adjustSizeByType(type);
    }

    private void adjustSizeByType(int type) {
        EnumVideoArcType videoArcType = this.getVideoArcType(type);
        try {
            TvManager.getPictureManager().setAspectRatio(videoArcType);
        } catch (TvCommonException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获得视频显示比例类型
     * @param adjustType
     * @return
     */
    private EnumVideoArcType getVideoArcType(int adjustType) {
        if (adjustType == 2) {
            return EnumVideoArcType.E_16x9;
        } else if (adjustType == 1) {
            return EnumVideoArcType.E_4x3;
        } else {
            return EnumVideoArcType.E_AUTO;
        }
    }

    /**
     * 设置第一帧加载后的处理
     */
    public void setFirstFrameDecoded() {
        this.adjustSizeByType(this.currentRatio);
    }
}
