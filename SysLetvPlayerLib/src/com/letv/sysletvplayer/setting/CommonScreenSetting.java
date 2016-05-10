package com.letv.sysletvplayer.setting;

import android.media.MediaPlayer;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

/**
 * * C1、X60、S250、第三方调整画面比例
 * @author caiwei
 */
public abstract class CommonScreenSetting {

    private static final int SCREEN_WIDTH = 1920;
    private static final int SCREEN_HEIGHT = 1080;
    private static final int screenwidth_1280_default = 1920;
    private static final int screenwidth_960_default = 1440;
    private static final int screenheight_720_default = 1080;

    public abstract MediaPlayer getMediaPlayer();

    public abstract LayoutParams getLayoutParams();

    public abstract void setLayoutParams(LayoutParams layoutParams);

    public abstract SurfaceHolder getHolder();

    /**
     * 根据调整类型设置要调整的宽高，调整画面比例
     * @param type
     *            0为自适应；1为4：3 ；2为16：9
     */
    public void adjust(int type) {
        int[] screenSize = new int[2];
        switch (type) {
        case 0:
            screenSize = this.getVideoSize();
            break;
        case 1:
            screenSize[0] = screenwidth_960_default;
            screenSize[1] = screenheight_720_default;
            break;
        case 2:
            screenSize[0] = screenwidth_1280_default;
            screenSize[1] = screenheight_720_default;
            break;
        }
        if (screenSize != null) {
            this.adjust(screenSize[0], screenSize[1]);
        }
    }

    private int[] getVideoSize() {
        if (this.getMediaPlayer() == null) {
            return null;
        }
        int width = this.getMediaPlayer().getVideoWidth();
        int height = this.getMediaPlayer().getVideoHeight();
        int[] screenSize = new int[2];
        float widthPixels = SCREEN_WIDTH;
        float heightPixels = SCREEN_HEIGHT;
        float wRatio = width / widthPixels;
        float hRatio = height / heightPixels;
        float ratios = Math.max(wRatio, hRatio);
        screenSize[0] = (int) Math.ceil(width / ratios);
        screenSize[1] = (int) Math.ceil(height / ratios);
        return screenSize;
    }

    private void adjust(int w, int h) {
        FrameLayout.LayoutParams lp = (android.widget.FrameLayout.LayoutParams) this
                .getLayoutParams();
        lp.width = w;
        lp.height = h;
        lp.gravity = Gravity.CENTER;
        this.setLayoutParams(lp);
        this.getHolder().setFixedSize(w, h);
    }
}
