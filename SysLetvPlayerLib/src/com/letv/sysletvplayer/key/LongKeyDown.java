package com.letv.sysletvplayer.key;

import android.view.KeyEvent;

import com.letv.mobile.core.time.TimeProvider;

public class LongKeyDown {

    LongKeyListener longKeyListener;
    private int longTime = 100;// 长按事件时间
    private long lastTime = 0;// 系统上次调用时间
    private long allTime = 0;// 按键总共相应时间
    private long callTime = 0;// 累计调用时间
    private int shortTime = 50;// 短按事件时间

    public LongKeyDown(LongKeyListener listener) {

        this.longKeyListener = listener;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        long curTime = TimeProvider.getCurrentMillisecondTime();
        long gapTime = curTime - this.lastTime;
        this.lastTime = curTime;

        if (event.getRepeatCount() == 0) {// 短按
            this.allTime = 0;
            switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT: // left
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                event.startTracking();
                return this.longKeyListener.onShortClick(keyCode, event);
            case KeyEvent.KEYCODE_DPAD_RIGHT:// right
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                event.startTracking();
                return this.longKeyListener.onShortClick(keyCode, event);
            default:
                break;
            }
        } else {
            this.callTime += gapTime;
            this.allTime += gapTime;
            if ((this.callTime > this.shortTime)
                    && (this.allTime < this.longTime)) {
                this.callTime = 0;
                return this.longKeyListener.onShortClick(keyCode, event);
            }
        }
        if ((this.callTime > this.shortTime) && (this.allTime >= this.longTime)) {
            this.callTime = 0;
            return this.longKeyListener.onLongClick(keyCode, event);
        }

        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {

        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_LEFT: // left
        case KeyEvent.KEYCODE_MEDIA_REWIND:
            event.startTracking();
            return this.longKeyListener.onShortClickUp(keyCode, event);
        case KeyEvent.KEYCODE_DPAD_RIGHT:// right
        case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
            event.startTracking();
            return this.longKeyListener.onShortClickUp(keyCode, event);
        default:
            break;
        }

        return false;
    }

    public int getLongTime() {
        return this.longTime;
    }

    public void setLongTime(int longTime) {
        this.longTime = longTime;
    }

    public long getLastTime() {
        return this.lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public int getShortTime() {
        return this.shortTime;
    }

    public void setShortTime(int shortTime) {
        this.shortTime = shortTime;
    }

    public long getcallTime() {
        return this.callTime;
    }

    public void setcallTime(long callTime) {
        this.callTime = callTime;
    }

    public interface LongKeyListener {

        boolean onShortClick(int keyCode, KeyEvent event);

        boolean onLongClick(int keyCode, KeyEvent event);

        boolean onShortClickUp(int keyCode, KeyEvent event);
    }
}
