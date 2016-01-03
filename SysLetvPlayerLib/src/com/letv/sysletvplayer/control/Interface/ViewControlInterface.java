package com.letv.sysletvplayer.control.Interface;

import android.view.View;
import android.view.ViewGroup;

/**
 * 界面相关,底层具体实现，供应用层调用
 * 提供各种调用显示、隐藏view的接口
 * @author caiwei
 */
public interface ViewControlInterface {
    /**
     * 添加自定义的View时，所添加的层次位置
     */
    public enum ViewLayersType {
        LAYERS_INSIDE_OF_PLAYVIEW, // 在playView的里层View(eg:350界面背景)
        LAYERS_OUTSIDE_OF_PLAYVIEW, // 在playView的外层View(预留接口)
        LAYERS_DEFALT, // 可手动取消的功能View(默认位置布局，eg:menu菜单界面)
        LAYERS_CANNOT_MANUAL_CLEAR// 不可手动取消的功能View(预留接口)
    }

    public void setTitle(String title);// 视频显示标题

    public void useDefaultLoading(boolean isUse);// 是否按默认的loading界面显示

    public void useDefaultPauseView(boolean isUse);// 是否按默认的播放暂停界面显示

    public void useDefaultPlayControlView(boolean isUse);// 是否按默认的播放控制界面显示

    public void useDefaultBufferView(boolean isUse);// 是否按默认buffer界面显示

    public void useDefaultVolumeView(boolean isUse);// 是否按默认音量界面显示

    public void setIsSupportFastForword(boolean isSupport);

    public void setIsSupportPlayControl(boolean isSupport);

    public View getPlayView();// 获取播放View

    public ViewGroup getContentView();// 获取播放器总布局

    public ViewGroup getCannotManualClearLayout();// 获取不可手动取消的布局

    public ViewGroup getDefaultLayout();// 获取默认位置（可手动取消）的布局

    public void resetView();// 重置所有界面内容

    public boolean removeAllCanManualClearFunctionView();// 移除所有可手动取消的功能布局

    public void showLoading(boolean isShowLoading);// 显示/隐藏loading界面

    public void showPauseView(boolean isShowPauseView);// 显示/隐藏播放暂停界面

    public void showPlayControlView(boolean isShowPlayControl);// 显示/隐藏播放控制界面

    public void showBufferView(boolean isShowBuffer);// 显示/隐藏buffer界面

    public void showVolumeView(boolean isShowVolume);// 显示/隐藏音量界面

    /**
     * 添加自定义View到布局
     * @param v
     *            要添加的view
     * @param lParams
     *            布局参数
     * @param layersType
     *            view的添加位置，说明
     *            LAYERS_INSIDE_OF_PLAYVIEW：在playView的里层View(eg:350界面背景)
     *            LAYERS_OUTSIDE_OF_PLAYVIEW：在playView的外层View(预留接口)
     *            LAYERS_DEFALT：默认位置布局，在OUTSIDE_OF_PLAYVIEW的外层,
     *            可手动取消(eg:menu菜单界面)
     *            LAYERS_CANNOT_MANUAL_CLEAR：在DEFALT的外层，不可手动取消(预留接口)
     */
    public void addView(View v, ViewGroup.LayoutParams lParams,
            ViewLayersType layersType);

    /**
     * 添加自定义View到默认位置布局
     * (ViewLayersType.LAYERS_DEFALT)
     * * @param v
     * 要添加的view
     * @param lParams
     *            布局参数
     */
    public void addView(View v, ViewGroup.LayoutParams lParams);

    /**
     * 添加自定义View到默认位置布局
     * (ViewLayersType.LAYERS_DEFALT)
     */
    public void addView(View v);

    /**
     * 从View所添加的位置，移除自定义的view
     * @param v
     * @param layersType添加位置
     */
    public void removeView(View v, ViewLayersType layersType);//

    /**
     * 从默认位置，移除自定义的view
     * @param v
     */
    public void removeView(View v);

}
