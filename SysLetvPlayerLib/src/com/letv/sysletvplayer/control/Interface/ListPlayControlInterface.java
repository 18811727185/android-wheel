package com.letv.sysletvplayer.control.Interface;

import com.letv.sysletvplayer.list.PlayInfoCallBack;
import com.letv.sysletvplayer.listener.PlayListListener;

/**
 * 列表播放控制接口
 * @author caiwei
 */
public interface ListPlayControlInterface {

    public void setCallBack(PlayInfoCallBack callBack);// 回调请求实际的播放路径

    public void playList();// 按照默认位置，启动播放

    public void playList(int pos);// 按照指定位置，启动播放

    public void playList(Object item);// 播放指定元素

    public void playPrevious();// 播放上一个

    public void playNext();// 播放下一个

    /**
     * 设置集合列表的元素类型是否是播放路径
     */
    public void setIsPath(boolean isPlayPath);

    public void start();// 开始播放

    public void play();// 播放

    public void pause();// 暂停

    public void stopPlayBack();// 停止播放

    public void setPlayListListener(PlayListListener listListener);// 设置列表播放相关监听

    public void resetPlayList();// 销毁播放设置

}
