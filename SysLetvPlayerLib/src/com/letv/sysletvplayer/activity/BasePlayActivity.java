package com.letv.sysletvplayer.activity;

import android.app.Activity;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.ViewGroup;

import com.letv.component.player.Interface.OnMediaStateTimeListener;
import com.letv.component.player.LetvVideoViewBuilder;
import com.letv.sysletvplayer.builder.LetvPlayBuilder;
import com.letv.sysletvplayer.control.ViewControllerImpl;
import com.letv.sysletvplayer.control.Interface.ListDataControlInterface;
import com.letv.sysletvplayer.control.Interface.ListPlayControlInterface;
import com.letv.sysletvplayer.control.Interface.PlayControlInterface;
import com.letv.sysletvplayer.control.Interface.ViewControlInterface;
import com.letv.sysletvplayer.control.base.BaseViewLayer;
import com.letv.sysletvplayer.listener.PlayerListener;

/**
 * 播放基础类
 * 实现对播放控制、播放布局的基本设置
 * 实现对列表数据存储
 * 实现对列表播放控制类的基本设置
 * 应用层继承该类，并实现缺省方法：设置播放路径、布局、标题等等
 * 单个视频播放必设项：mPlayControl.setVideoPath(path)设置播放路径，启播
 * 视频列表播放必设项：mListDataControl. loadList(List list)设置路径列表后，
 * 调用mListPlayControl.playList()启播列表
 * 应用层如果需自己设定播放的初始化和嵌套布局，可参考此类实现。
 * @author caiwei
 */
    public class BasePlayActivity extends Activity implements PlayerListener {
    public ViewGroup mViewGroup;
    public ListPlayControlInterface mListPlayControl;
    public ListDataControlInterface mListDataControl;
    public PlayControlInterface mPlayControl;
    public ViewControlInterface mViewControl;
    public BaseViewLayer mViewLayer;

    LetvPlayBuilder mPlayManger = new LetvPlayBuilder();
    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int w, int h) {
    }

    @Override
    public void onMediaStateTime(OnMediaStateTimeListener.MeidaStateType mStateType, String time) {

    }

    @Override
    public void onSeekTo(int progress) {
    }

    @Override
    public void onSeekComplete() {
    }

    @Override
    public void onRewind() {
    }

    @Override
    public void onPrePared() {
        this.mPlayControl.start();
    }

    @Override
    public void onNeedBuffer(int progress) {
    }

    @Override
    public void onInfo(int arg1, int arg2) {
    }

    @Override
    public void onForward() {
    }

    @Override
    public void onError(int arg1, int arg2) {
    }

    @Override
    public void onCompletion() {
        if (this.mListPlayControl != null) {
            // 如果设置了播放列表，视频播放完后，默认播下一个视频
            // 应用层如不希望走此逻辑，可由子类重写
            this.mListPlayControl.playNext();
        }
    }

    @Override
    public void onBufferUpdating(int progress) {
    }

    @Override
    public void onBufferOver() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.init();
        this.initView();
        this.initControl();
        this.mPlayControl.setPlayerListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayManger.resetBuild();
        mPlayManger = null;
    }

    /**
     * 子类可重写，初始化自定义的类,用于改变默认项
     * eg: mViewControl = new SelfDefineViewControlImpl();
     * mBaseViewLayer = new SelfDefineBaseViewLayer();
     */
    public void init() {
    }

    /**
     * 通过 LetvPlayBuilder初始化总布局，并setContentView
     */
    private void initView() {
        this.mViewGroup = mPlayManger.build(this,
                this.mViewLayer, (ViewControllerImpl) this.mViewControl, LetvVideoViewBuilder.Type.TV);
        this.setContentView(this.mViewGroup);
    }

    /**
     * 初始化control对象
     */
    private void initControl() {
        this.mPlayControl = mPlayManger
                .getBasePlayController();
        if (this.mViewControl == null) {
            this.mViewControl = mPlayManger
                    .getBaseViewController();
        }
        this.mListDataControl = mPlayManger
                .getListDataController();
        this.mListPlayControl = mPlayManger
                .getListPlayController();
    }

}
