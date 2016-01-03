package com.letv.sysletvplayer.list;

public interface PlayInfoCallBack {
    public void requestPlayInfo(Object item,
            PlayInfoResponseCallBack responseCallBack);// 根据元素获取播放路径
}
