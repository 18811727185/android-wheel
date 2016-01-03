package com.letv.shared.widget;

import android.view.MotionEvent;

/**
 * Created by liangchao on 14-11-4.
 */
public class MotionHolder{
    public int currentPage;
    public int duration;
    public MotionEvent motionEvent;
    public int mode;
    public int dst_for_overscroll = 0;
    public boolean isOverscroll = false;
    public boolean isCancel = false;
    public boolean needOffset;

    public MotionHolder(int currentPage, int duration,int mode) {
        this.currentPage = currentPage;
        this.duration = duration;
        this.mode = mode;
    }
    public MotionHolder(int currentPage, int duration,int mode,int dst_for_overscroll,boolean isOverscroll) {
        this.currentPage = currentPage;
        this.duration = duration;
        this.mode = mode;
        this.dst_for_overscroll = dst_for_overscroll;
        this.isOverscroll = isOverscroll;
    }
    public MotionHolder() {
    }
    public void setMotionHolder(MotionHolder motionHolder){
        this.currentPage = motionHolder.currentPage;
        this.duration = motionHolder.duration;
        this.mode = motionHolder.mode;
        this.dst_for_overscroll = motionHolder.dst_for_overscroll;
        this.isOverscroll = motionHolder.isOverscroll;
    }

    @Override
    public String toString() {
        return "MotionHolder{" +
                "currentPage=" + currentPage +
                ", duration=" + duration +
                ", mode=" + mode +
                ", dst_for_overscroll=" + dst_for_overscroll +
                ", isOverscroll=" + isOverscroll +
                '}';
    }
}
