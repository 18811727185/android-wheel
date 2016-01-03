package com.letv.sysletvplayer.list;

public class PlayInfoModel {
    private String path = null;// 播放路径
    private int position = 0;// 起播位置
    private int tryPlayTime = 0;// 试看时长
    private String title;// 标题

    public PlayInfoModel(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getPosition() {
        return this.position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getTryPlayTime() {
        return this.tryPlayTime;
    }

    public void setTryPlayTime(int tryPlayTime) {
        this.tryPlayTime = tryPlayTime;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
