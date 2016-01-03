package com.letv.sysletvplayer.control;

import android.content.Context;

import com.letv.sysletvplayer.control.Interface.ListDataControlInterface;
import com.letv.sysletvplayer.control.Interface.ListPlayControlInterface;
import com.letv.sysletvplayer.control.Interface.PlayControlInterface;
import com.letv.sysletvplayer.control.Interface.ViewControlInterface;
import com.letv.sysletvplayer.list.PlayInfoCallBack;
import com.letv.sysletvplayer.list.PlayInfoModel;
import com.letv.sysletvplayer.list.PlayInfoResponseCallBack;
import com.letv.sysletvplayer.listener.PlayListListener;

/**
 * 列表播放控制接口实现类
 * @author caiwei
 */
public class ListPlayControllerImpl implements ListPlayControlInterface {
    private boolean isPath = false;// 当前集合里是否是播放路径,默认false
    private PlayInfoCallBack mPlayListCallBack = null;// 请求实际路径
    private final PlayControlInterface mPlayControl;
    private final ViewControlInterface mViewControl;
    private PlayListListener mListListener;
    private final ListDataControlInterface mListDataControl;
    /*
     * 对实际路径响应的回调
     */
    private final PlayInfoResponseCallBack responseCallBack = new PlayInfoResponseCallBack() {
        @Override
        public void onPlayInfoResponse(PlayInfoModel playInfo) {
            ListPlayControllerImpl.this.playVideo(playInfo);
        }

        @Override
        public void onPlayInfoResponse(String path) {
            ListPlayControllerImpl.this.playVideo(path);
        };
    };

    /**
     * 类的初始化
     */
    public ListPlayControllerImpl(Context context,
            PlayControlInterface playControl, ViewControlInterface viewControl,
            ListDataControlInterface listDataControl) {
        this.mPlayControl = playControl;
        this.mViewControl = viewControl;
        this.mListDataControl = listDataControl;
    }

    @Override
    public void setPlayListListener(PlayListListener listListener) {
        this.mListListener = listListener;
    }

    @Override
    public void setCallBack(PlayInfoCallBack callBack) {
        this.mPlayListCallBack = callBack;
    }

    @Override
    public void resetPlayList() {
        this.mListDataControl.clear();
        this.mListDataControl.resetData();
    }

    @Override
    public void playList() {// 播当前默认位置
        Object item = this.mListDataControl.getCurrentItem();
        this.goToPlay(item);
    }

    @Override
    public void playList(int position) {// 播指定位置
        Object item = this.mListDataControl.goTo(position);
        this.goToPlay(item);
    }

    @Override
    public void playList(Object item) {// 播指定元素
        int position = this.mListDataControl.getIndexOf(item);
        this.mListDataControl.setCurrrentIndex(position);
        this.goToPlay(item);
    }

    @Override
    public void playPrevious() {// 播前一个
        Object item = this.mListDataControl.goToPrevious();
        this.goToPlay(item);
    }

    @Override
    public void playNext() {// 播下一个
        Object item = this.mListDataControl.goToNext();
        this.goToPlay(item);
    }

    @Override
    public void setIsPath(boolean isPlayPath) {
        this.isPath = isPlayPath;
    }

    @Override
    public void start() {
        this.mPlayControl.start();
    }

    @Override
    public void play() {
        this.mPlayControl.play();
    }

    @Override
    public void pause() {
        this.mPlayControl.pause();
    }

    @Override
    public void stopPlayBack() {
        this.mPlayControl.stopPlayBack();
    }

    /**
     * 根据指定元素，设置播放路径
     */
    private void goToPlay(Object item) {
        if (item == null) {
            // 播放数据错误
            return;
        }
        if (this.isPath) {// 存储内容是播放路径,直接播放
            if (item instanceof String) {
                this.playVideo((String) item);
            } else if (item instanceof PlayInfoModel) {
                this.playVideo((PlayInfoModel) item);
            }

        } else if (this.mPlayListCallBack != null) {// 存储内容非播放路径，通过应用层请求数据
            this.mPlayListCallBack.requestPlayInfo(item, this.responseCallBack);
        }
    }

    /**
     * 根据播放信息，播放视频
     */
    private void playVideo(PlayInfoModel playInfo) {
        if (playInfo != null && playInfo.getPath() != null) {
            this.mViewControl.setTitle(playInfo.getTitle());
            this.mPlayControl.setTryPlayTime(playInfo.getTryPlayTime());
            this.mPlayControl.setVideoPath(playInfo.getPath(),
                    playInfo.getPosition());
        } else {
            // 播放路径为空
        }
    }

    /**
     * 根据播放路径，播放视频
     */
    private void playVideo(String path) {
        if (path != null) {
            this.mPlayControl.setVideoPath(path);
        } else {
            // 播放路径为空
        }
    }
}
