package com.letv.sysletvplayer.control;

import com.letv.mobile.core.utils.DeviceUtils;
import com.letv.mobile.core.utils.DeviceUtils.DeviceType;
import com.letv.mobile.core.utils.TerminalUtils;
import com.letv.mobile.core.utils.VolumeUtils;
import com.letv.sysletvplayer.control.Interface.ViewControlInterface;
import com.letv.sysletvplayer.control.base.BasePlayControllerImpl;
import com.letv.sysletvplayer.control.base.BaseViewLayer;
import com.letv.sysletvplayer.key.LongKeyDown;
import com.letv.sysletvplayer.key.LongKeyDown.LongKeyListener;
import com.letv.sysletvplayer.listener.ControlListener;
import com.letv.sysletvplayer.util.PlayUtils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import java.util.Map;

/**
 * View布局对按键及播放的逻辑处理类
 * 应用层可继承该类，实现按键及播放的逻辑的重写
 * @author caiwei
 */
public class ViewControllerImpl implements ViewControlInterface {
    protected BaseViewLayer mBaseViewController;// view布局控制，继承类可调用它处理布局
    private int mLongMoveOffset = 20 * 1000;// 长按步长,默认20s ,播放后，会根据时长算步长
    private int mShortMoveOffset = 20 * 1000;// 短按步长,默认20s
    private boolean mBuffering = false;// 视频是否在缓冲
    private int mBufferPercent = 0;// 当前缓冲百分比
    private int mTryPlayTime;// 试看时间
    private boolean mMustHidePauseView = false;// 是否必须隐藏暂停按钮
    private static final int SEEK_BACK_TIME_TRY_LOOK = 5000;// seek的时间超过了试看时间，从试看时间往回seek
    private boolean isFirstPressLeftOrRight = false;// 是否第一次按左或右键
    private double mVolumeDiv;
    private static final int AUTO_REFRESH_PROGRESS = 1005;// 刷新进度条
    private static final int AUTO_HIDE_VOLUME_CONTROLLER = 1006;// 自动隐藏音控界面
    private static final int AUTO_HIDE_VOLUME_CONTROLLER_TIME = 8000;// 8s后自动隐藏音控界面
    private static final int AUTO_HIDE_PLAY_CONTROLLER = 1007;
    private static final int HIDE_PLAY_CONTROLLER = 5000;// 5秒后自动隐藏播控条
    /*************************** 默认界面显示的相关参数 ******************************/
    private boolean isUseLoading = true;// 是否需要显示loading
    private boolean isUseBuffer = true;// 是否需要显示缓冲
    private boolean isUsePause = true;// 是否需要显示播放暂停
    private boolean isUsePlayControl = true; // 是否需要显示播控
    private boolean isUseVolume = true;// 是否需要显示音量
    private boolean isSupportFastForward = true; //是否支持快进快退
    private boolean isSupportPlayControl = true; //是否支持播放控制
    private BasePlayControllerImpl mPlayControl;
    private Context mContext;
    private DeviceType mOriginalType;// 设备类型
    private GestureDetector mGestureDetector;// 播放进度条手势检测器
    private LongKeyDown mLongKeyDown;
    private final Handler mAbsHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case AUTO_REFRESH_PROGRESS:
                this.removeMessages(AUTO_REFRESH_PROGRESS);
                if (ViewControllerImpl.this.mBaseViewController
                        .isPlayControlVisible()) {
                    ViewControllerImpl.this.mBaseViewController
                            .dealSeekToTimeChangedOver();
                    ViewControllerImpl.this.sendMsgRefreshCurrentProgress();
                }
                break;
            case AUTO_HIDE_VOLUME_CONTROLLER:
                ViewControllerImpl.this.dealShowVolumeView(false);
                break;
            case AUTO_HIDE_PLAY_CONTROLLER:
                ViewControllerImpl.this.dealShowPlayControlView(false);
                ViewControllerImpl.this.mAbsHandler
                        .removeMessages(AUTO_HIDE_PLAY_CONTROLLER);
                break;
            }
        }
    };

    /*********************************** 监听回调处理 *******************************/
    /**
     * 播放回调
     */
    private final ControlListener mControlListener = new ControlListener() {
        @Override
        public void onPrePared() {
            // 准备完成，移除loading界面
            ViewControllerImpl.this.dealShowLoadingView(false);
            int duration = ViewControllerImpl.this.mPlayControl
                    .getVideoDuration();
            ViewControllerImpl.this.setMoveOffset(duration);
        }

        @Override
        public void onStopPlayBack() {
            // 停止播放的时候,清空数据
            ViewControllerImpl.this.resetData();
        }

        @Override
        public void onSetVideoPath(String path, Map<String, String> headers) {
            if (path == null) {
                return;
            }
            // 初始化路径的时候,显示loading界面
            ViewControllerImpl.this.dealShowLoadingView(true);
        }

        @Override
        public void onPause() {
        };

        // 开始缓冲，显示buffer界面， 暂停播放
        @Override
        public void onNeedBuffer(int percent) {
            ViewControllerImpl.this.handlerPauseForBuffer();
            ViewControllerImpl.this.mBuffering = true;
            ViewControllerImpl.this.mBufferPercent = percent;
            ViewControllerImpl.this.dealShowBufferView(true);
        };

        // 正在缓冲，显示buffer界面
        @Override
        public void onBufferUpdating(int percent) {
            ViewControllerImpl.this.mBufferPercent = percent;
            ViewControllerImpl.this.dealShowBufferView(true);
        }

        // 缓冲完成，移除buffer界面， 恢复播放
        @Override
        public void onBufferOver() {
            ViewControllerImpl.this.mBuffering = false;
            ViewControllerImpl.this.dealShowBufferView(false);
            ViewControllerImpl.this.handlerPlayForBuffer();
        };

        @Override
        public void onSeekTo(int mDuration) {
        }

        @Override
        public void onSetTryPlayTime(int mTraPlayTime) {
            // NOTE(caiwei),在到了试看时间后，应该不再播放了(应用层做停止播放处理)
            ViewControllerImpl.this.mTryPlayTime = mTraPlayTime;
        }
    };

    /**
     * 长按事件
     */
    private final LongKeyListener mLongKeyListener = new LongKeyListener() {
        @Override
        public boolean onShortClickUp(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                    || keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
                return ViewControllerImpl.this.onLeftkeyUp();
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                    || keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
                return ViewControllerImpl.this.onRightkeyUp();
            }
            return false;
        }

        @Override
        public boolean onShortClick(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                    || keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
                return ViewControllerImpl.this
                        .onLeftkeyDown(ViewControllerImpl.this.mShortMoveOffset);
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                    || keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
                return ViewControllerImpl.this
                        .onRightkeyDown(ViewControllerImpl.this.mShortMoveOffset);
            }
            return false;
        }

        @Override
        public boolean onLongClick(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                    || keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
                return ViewControllerImpl.this
                        .onLeftkeyDown(ViewControllerImpl.this.mLongMoveOffset);
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                    || keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
                return ViewControllerImpl.this
                        .onRightkeyDown(ViewControllerImpl.this.mLongMoveOffset);
            }
            return false;
        }
    };

    /**
     * 遥控器事件
     */
    private final OnKeyListener mOnKeyListener = new OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            switch (event.getAction()) {
            case KeyEvent.ACTION_DOWN:
                return ViewControllerImpl.this.onKeyDown(keyCode, event);
            case KeyEvent.ACTION_UP:
                return ViewControllerImpl.this.onKeyUp(keyCode, event);
            }
            return false;
        }
    };

    private final OnTouchListener mPlayTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (ViewControllerImpl.this.mGestureDetector != null) {
                ViewControllerImpl.this.mGestureDetector.onTouchEvent(event);
                return true;
            }
            return false;
        }
    };

    /**
     * 处理空鼠模式时用户拖动进度条逻辑
     */
    private final OnTouchListener mMouseTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (ViewControllerImpl.this.mGestureDetector != null) {
                ViewControllerImpl.this.mGestureDetector.onTouchEvent(event);
            }
            return ViewControllerImpl.this.dealUserMouseTouchListener(v, event);
        }
    };

    private final SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            float x = e2.getX() - e1.getX();
            if (x > 0) {// 右滑
                ViewControllerImpl.this
                        .onRightkeyDown(ViewControllerImpl.this.mLongMoveOffset);
            } else if (x < 0) {// 左滑
                ViewControllerImpl.this
                        .onLeftkeyDown(ViewControllerImpl.this.mLongMoveOffset);
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            // 处理DOWN
            ViewControllerImpl.this.dealShowPlayControlView(true);
            return true;
        };

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            ViewControllerImpl.this.sendMsgDelayedHidePlayControl();
            return true;
        };

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            ViewControllerImpl.this.pressCenterKeyFunction();
            return true;
        };
    };

    private final OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                boolean fromUser) {
            ViewControllerImpl.this.mBaseViewController
                    .dealProgressChanged(progress);
        }
    };

    /*********************** ViewControlInterface接口实现 ***************************/

    @Override
    public View getPlayView() {
        if (this.mPlayControl == null) {
            return null;
        }
        return this.mPlayControl.getPlayView();
    }

    @Override
    public FrameLayout getContentView() {
        return this.mBaseViewController.getPlayLayout();
    }

    @Override
    public ViewGroup getCannotManualClearLayout() {
        return this.mBaseViewController.getCannotManualClearLayout();
    }

    @Override
    public ViewGroup getDefaultLayout() {
        return this.mBaseViewController.getCanManualClearLayout();
    }

    @Override
    public void setTitle(String title) {
        this.mBaseViewController.setPlayTitle(title);
    }

    @Override
    public void useDefaultLoading(boolean isUse) {
        this.isUseLoading = isUse;
    }

    @Override
    public void useDefaultPauseView(boolean isUse) {
        this.isUsePause = isUse;
    }

    @Override
    public void useDefaultPlayControlView(boolean isUse) {
        this.isUsePlayControl = isUse;
    }

    @Override
    public void useDefaultBufferView(boolean isUse) {
        this.isUseBuffer = isUse;
    }

    @Override
    public void useDefaultVolumeView(boolean isUse) {
        this.isUseVolume = isUse;
    }

    @Override
    public void setIsSupportFastForword(boolean isSupport){
        this.isSupportFastForward = isSupport;
    }

    @Override
    public void setIsSupportPlayControl(boolean isSupport){
        isSupportPlayControl = isSupport;
    }
    /**
     * loading界面
     */
    @Override
    public void showLoading(boolean isShowLoadingView) {
        this.dealShowLoadingView(isShowLoadingView);
    }

    /**
     * 暂停界面
     */
    @Override
    public void showPauseView(boolean isShowPauseView) {
        this.dealShowPlayPauseView(isShowPauseView);
    }

    /**
     * 播控界面
     */
    @Override
    public void showPlayControlView(boolean isShowPlayControlView) {
        this.dealShowPlayControlAutoHide(isShowPlayControlView);
    }

    /**
     * 缓冲界面
     */
    @Override
    public void showBufferView(boolean isShowBufferView) {
        this.dealShowBufferView(isShowBufferView);
    }

    /**
     * 音控界面
     */
    @Override
    public void showVolumeView(boolean isShowVolumeView) {
        this.dealShowVolumeAutoHide(isShowVolumeView);
    }

    // 根据设置的添加位置，添加应用层view至播放器并显示
    @Override
    public void addView(View v, ViewGroup.LayoutParams lParams,
            ViewLayersType layersType) {
        if (v == null) {
            return;
        }
        if (lParams != null) {
            v.setLayoutParams(lParams);
        }
        switch (layersType) {
        case LAYERS_INSIDE_OF_PLAYVIEW:
            this.mBaseViewController.showInsideOfPlayView(v);
            break;
        case LAYERS_OUTSIDE_OF_PLAYVIEW:
            this.mBaseViewController.showOutsideOfPlayView(v);
            break;
        case LAYERS_CANNOT_MANUAL_CLEAR:
            this.mBaseViewController.showCannotManualClearFunctionView(v);
            break;
        case LAYERS_DEFALT:
            this.mBaseViewController.showCanManualClearFunctionView(v);
            break;
        }
    }

    @Override
    public void addView(View v, LayoutParams lParams) {
        this.addView(v, lParams, ViewLayersType.LAYERS_DEFALT);
    }

    @Override
    public void addView(View v) {
        this.addView(v, null, ViewLayersType.LAYERS_DEFALT);
    }

    // 移除应用层添加的view使其不可见
    @Override
    public void removeView(View v, ViewLayersType layersType) {
        switch (layersType) {
        case LAYERS_INSIDE_OF_PLAYVIEW:
            this.mBaseViewController.hideInsideOfPlayView(v);
            break;
        case LAYERS_OUTSIDE_OF_PLAYVIEW:
            this.mBaseViewController.hideOutsideOfPlayView(v);
            break;
        case LAYERS_CANNOT_MANUAL_CLEAR:
            this.mBaseViewController.hideCannotManualClearFunctionView(v);
            break;
        case LAYERS_DEFALT:
            this.mBaseViewController.hideCanManualClearFunctionView(v);
            break;
        }
    }

    @Override
    public void removeView(View v) {
        this.removeView(v, ViewLayersType.LAYERS_DEFALT);
    }

    // 移除所有可手动取消的FunctionView
    @Override
    public boolean removeAllCanManualClearFunctionView() {
        return this.mBaseViewController.hideAllCanManualClearFuctionView();
    }

    /**
     * 销毁界面及数据设置
     */
    @Override
    public void resetView() {
        this.resetData();
        this.mBaseViewController.setPlayTitle(null);
        this.mBaseViewController.removeAllFunctionView();
    }

    /************************************** 初始化 ****************************************/
    public ControlListener getControlListener() {
        return this.mControlListener;
    }

    public BaseViewLayer init(ViewGroup viewGroup, BasePlayControllerImpl playControl,
            BaseViewLayer baseViewController, Context context) {
        this.initData();
        this.mPlayControl = playControl;
        this.mContext = context;
        if (baseViewController != null) {
            this.mBaseViewController = baseViewController;
        } else {
            this.mBaseViewController = new BaseViewLayer();
        }
        this.mBaseViewController.init(viewGroup, this.mPlayControl, context);
        this.initKeyListener();
        return  mBaseViewController;
    }

    private void initData() {
        this.mOriginalType = DeviceUtils.getDeviceType();
        if (this.mOriginalType == DeviceType.DEVICE_OTHER) {
            this.mVolumeDiv = VolumeUtils.OTHRER_DEVICE_DIV;
        } else {
            this.mVolumeDiv = VolumeUtils.C1_C1S_DIV;
        }
    }

    /**
     * 初始化短按事件、长按事件、遥控器事件的监听
     */
    private void initKeyListener() {
        this.mGestureDetector = new GestureDetector(this.mContext,
                this.gestureListener);
        this.mBaseViewController.setPlayTouchListener(this.mPlayTouchListener);
        this.mBaseViewController.setPlayKeyListener(this.mOnKeyListener);
        this.mBaseViewController
                .setProgressBarTouchListener(this.mMouseTouchListener);
        this.mBaseViewController
                .setProgressBarChangeListener(this.mSeekBarChangeListener);
        this.mLongKeyDown = new LongKeyDown(this.mLongKeyListener);
    }

    /**
     * 遥控器Down
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.mBaseViewController.isLoadingVisible()) {
            // 如果Loading页正在显示，需屏蔽其他按键操作
            return true;
        }
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_LEFT:
        case KeyEvent.KEYCODE_DPAD_RIGHT:
        case KeyEvent.KEYCODE_MEDIA_REWIND:
        case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
            if(isSupportPlayControl){
                return this.mLongKeyDown.onKeyDown(keyCode, event);
            }
            break;
        case KeyEvent.KEYCODE_DPAD_UP:
        case KeyEvent.KEYCODE_DPAD_DOWN:
            return this.dealUserKeyDownForVolume(keyCode, event);
        case KeyEvent.KEYCODE_MENU:
            // 为了解决第三方电视上，按menu键的问题
            return true;
        }
        return false;
    }

    /**
     * 遥控器Up
     */
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // 如果Loading页正在显示，需屏蔽其他按键操作(除了back键)
        if (this.mBaseViewController.isLoadingVisible()
                && keyCode != KeyEvent.KEYCODE_BACK
                && keyCode != KeyEvent.KEYCODE_ESCAPE) {
            return true;
        }
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_LEFT:
        case KeyEvent.KEYCODE_DPAD_RIGHT:
        case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
        case KeyEvent.KEYCODE_MEDIA_REWIND:
            if(isSupportPlayControl) {
                return this.mLongKeyDown.onKeyUp(keyCode, event);
            }
            break;
        case KeyEvent.KEYCODE_BACK:
        case KeyEvent.KEYCODE_ESCAPE:
            // 处理返回键，如果音量键正在暂停，不隐藏
            if (this.mBaseViewController.isVolumeControlVisible()
                    && !this.mPlayControl.isPlaying()) {
                return false;
            }
            return this.mBaseViewController.hideAllCanManualClearFuctionView();
        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
        case KeyEvent.KEYCODE_MEDIA_PLAY:
        case KeyEvent.KEYCODE_MEDIA_PAUSE:
        case KeyEvent.KEYCODE_DPAD_CENTER:
        case KeyEvent.KEYCODE_ENTER:
            // 如果正在缓冲，不显示pause
            if(isSupportPlayControl) {
                if (this.mBaseViewController.isBufferVisible()) {
                    return true;
                }
                this.pressCenterKeyFunction();
                return true;
            }
            break;
        case KeyEvent.KEYCODE_DPAD_UP:
        case KeyEvent.KEYCODE_DPAD_DOWN:
            if (this.dealUserKeyUpForVolume(keyCode, event)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 中间键
     */
    public void pressCenterKeyFunction() {
        if (this.mPlayControl.isPlaying()) {
            if (DeviceUtils.isAudioCtrlPermitted()) {
                if (this.mBaseViewController.isVolumeControlVisible()) {
                    this.mPlayControl.pause();
                }
                this.dealShowVolumeAutoHide(true);
            } else {
                this.mPlayControl.pause();
                this.dealShowPlayPauseView(true);
            }
            this.mAbsHandler.removeMessages(AUTO_HIDE_PLAY_CONTROLLER);
            this.dealShowPlayControlView(false);
            return;
        }
        this.mPlayControl.play();
        if (DeviceUtils.isAudioCtrlPermitted()) {
            this.dealShowVolumeAutoHide(true);
        } else {
            this.dealShowPlayPauseView(false);
        }
        this.sendMsgRefreshCurrentProgress();
        this.dealShowPlayControlAutoHide(true);
    }

    /**
     * 音控按键Down（音控界面只针对c1、c1s、第三方设备可见）
     */
    public boolean dealUserKeyDownForVolume(int keyCode, KeyEvent event) {
        if (!DeviceUtils.isAudioCtrlPermitted()
                || !this.mBaseViewController.isVolumeControlVisible()) {
            return false;
        }
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_DOWN:
            // 当音控界面显示时，按下键调低音量
            this.mBaseViewController.dealVolumeKeyDownRaiseOrLow(false,
                    this.mVolumeDiv);
            this.sendMsgDelayedHideVolume();
            return true;
        case KeyEvent.KEYCODE_DPAD_UP:
            // 当音控界面显示时，按上键调高音量
            this.mBaseViewController.dealVolumeKeyDownRaiseOrLow(true,
                    this.mVolumeDiv);
            this.sendMsgDelayedHideVolume();
            return true;
        }
        return false;
    }

    /**
     * 音控按键Up（音控界面只针对c1、c1s、第三方设备可见）
     */
    public boolean dealUserKeyUpForVolume(int keyCode, KeyEvent event) {
        if (!DeviceUtils.isAudioCtrlPermitted()
                || !this.mBaseViewController.isVolumeControlVisible()) {
            return false;
        }
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_DOWN:
            // 当音控界面显示时，按下键调低音量
            this.mBaseViewController.dealVolumeKeyUpRaiseOrLow(false);
            return true;
        case KeyEvent.KEYCODE_DPAD_UP:
            // 当音控界面显示时，按上键调高音量
            this.mBaseViewController.dealVolumeKeyUpRaiseOrLow(true);
            return true;
        }
        return false;
    }

    /**
     * 右键down或右滑
     */
    public boolean onRightkeyDown(int moveOffset) {
        return this.seekToLeftOrRight(moveOffset, false);
    }

    /**
     * 左键down或左滑
     */
    public boolean onLeftkeyDown(int moveOffset) {
        return this.seekToLeftOrRight(moveOffset, true);
    }

    /**
     * 右键up
     */
    public boolean onRightkeyUp() {
        return seekToPlay();
    }

    /**
     * 左键up
     */
    public boolean onLeftkeyUp() {
        return seekToPlay();
    }

    /**
     * Touch进度条
     */
    public boolean dealUserMouseTouchListener(View v, MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            // 处理DOWN
            this.dealShowPlayControlView(true);
            return true;
        case MotionEvent.ACTION_UP:
            // 处理UP
            this.sendMsgDelayedHidePlayControl();
            return true;
        }
        return false;
    }

    // 重置数据
    private void resetData() {
        this.mTryPlayTime = 0;
        this.mBuffering = false;
        this.mBufferPercent = 0;
        this.mMustHidePauseView = false;
    }

    // 设置快进快退位移
    private void setMoveOffset(int totalDuration) {
        this.setLongMoveOffset(totalDuration);
        this.setShortMoveOffset();
    }

    /*
     * 将视频按时间分为几类小于5分钟，移动步长为 视频时长/10 5分钟至20分钟，移动步长为，移动步长为 视频时长/20
     * 20分钟以上，移动步长为，固定时间 每隔100毫秒刷新一次进度条（LongKeyListener中的time）
     */
    private void setLongMoveOffset(int totalTime) {
        int offset = 20 * 1000;// 默认移动15秒,每个100毫秒刷新一次
        if (totalTime < 0) {
            this.mLongMoveOffset = offset;
        }
        int len1 = 10 * 60 * 1000;// 10分钟
        int len2 = 30 * 60 * 1000;// 30分钟
        int len3 = 60 * 60 * 1000;// 60分钟
        this.mLongKeyDown.setLongTime(100);
        if (totalTime < len1) {
            offset = totalTime / 40;
            this.mLongKeyDown.setLongTime(100 / 2 + 1);
        } else if (totalTime >= len1 && totalTime < len2) {
            offset = totalTime / 40;
        } else if (totalTime >= len2 && totalTime < len3) {
            offset = 5 * 60 * 1000 / 10;// 长按1秒走5分钟=每次刷新走3分钟/10
        } else if (totalTime >= len3) {
            offset = 9 * 60 * 1000 / 10;// 长按1秒走9分钟=每次刷新走6分钟/10
        }
        this.mLongMoveOffset = offset;
    }

    // 短按走20秒
    private void setShortMoveOffset() {
        this.mShortMoveOffset = 20 * 1000;
    }

    /**
     * 在播放页左右按键的逻辑
     * @param moveOffset
     *            步长
     * @param isLeft
     */
    private boolean seekToLeftOrRight(int moveOffset, boolean isLeft) {
        this.mAbsHandler.removeMessages(AUTO_REFRESH_PROGRESS);
        if (!this.isUsePlayControl) {
            return false;
        }
        if (this.mBaseViewController.isPlayControlVisible()) {
            if(!isSupportFastForward){
                return false;
            }
            this.isFirstPressLeftOrRight = false;
            this.handlerPauseForSeek();
            int seekToTime = this.getSeekToLeftOrRightTime(moveOffset, isLeft);
            this.seekForNotFirst(isLeft, seekToTime);
        } else {
            this.isFirstPressLeftOrRight = true;
            this.seekForFirst();
        }
        return true;
    }

    private void seekForNotFirst(boolean isLeft, int willSeekToTime) {
        this.mBaseViewController.dealSeekToTimeChanged(isLeft, willSeekToTime);
        this.mAbsHandler.removeMessages(AUTO_REFRESH_PROGRESS);
        this.sendMsgDelayedHidePlayControl();
    }

    private void seekForFirst() {
        this.sendMsgRefreshCurrentProgress();
        this.dealShowPlayControlAutoHide(true);
    }

    /**
     * 处理左右目标seek点 得到seekToTime
     */
    private int getSeekToLeftOrRightTime(int moveOffset, boolean isLeft) {
        int seekToTime = 0;
        if (isLeft) {
            seekToTime = this.mBaseViewController.getSeekProgress()
                    - moveOffset;
            if (seekToTime < 0) {
                seekToTime = 0;
            }
        } else {
            this.adjustTryPlayTime();
            int maxSeek = this.mTryPlayTime - SEEK_BACK_TIME_TRY_LOOK;
            int minSeek = this.mBaseViewController.getSeekProgress()
                    + moveOffset;
            // seek时间超过试看时间，相对试看点往回seek 5s
            seekToTime = minSeek < maxSeek ? minSeek : maxSeek;
            if (seekToTime <= 0) {
                // 处理边界seek点
                seekToTime = minSeek < this.mTryPlayTime ? minSeek
                        : this.mTryPlayTime;
            }
        }
        return seekToTime;
    }

    // 初始化试看时长
    private void adjustTryPlayTime() {
        if (this.mTryPlayTime <= 0) {
            this.mTryPlayTime = this.mPlayControl.getVideoDuration();
        }
    }

    /**
     * 快进、快退按键抬起的时候
     * 根据进度条更新播放进度
     * 更新后，恢复视频播放
     */
    private boolean seekToPlay() {
        boolean isSupportToSeek = false;
        if(this.mBaseViewController.isPlayControlVisible()){
            if(this.isSupportFastForward){
                isSupportToSeek = true;
            }
        } else {
            isSupportToSeek = true;
            return true;
        }

        boolean needSeek = !this.isFirstPressLeftOrRight
                && this.mBaseViewController.isPlayControlVisible()
                && isSupportToSeek;
        if (!needSeek) {
            return false;
        }
        int seekToPos = this.mBaseViewController.getSeekProgress();
        int curPos = this.mPlayControl.getCurrentPosition();
        if (curPos != seekToPos) {
            this.mPlayControl.seekTo(seekToPos);
        }
        this.sendMsgRefreshCurrentProgress();
        this.handlerPlayForSeek();
        return true;
    }

    // 自动隐藏音控界面
    private void sendMsgDelayedHideVolume() {
        this.mAbsHandler.removeMessages(AUTO_HIDE_VOLUME_CONTROLLER);
        // 视频暂停时不允许自动隐藏音控界面
        if (this.mPlayControl.isPlaying()) {
            this.mAbsHandler.sendEmptyMessageDelayed(
                    AUTO_HIDE_VOLUME_CONTROLLER,
                    AUTO_HIDE_VOLUME_CONTROLLER_TIME);
        }
    }

    // 自动隐藏播控界面
    private void sendMsgDelayedHidePlayControl() {
        this.mAbsHandler.removeMessages(AUTO_HIDE_PLAY_CONTROLLER);
        this.mAbsHandler.sendEmptyMessageDelayed(AUTO_HIDE_PLAY_CONTROLLER,
                HIDE_PLAY_CONTROLLER);
    }

    // 发送刷新当前进度的消息
    private void sendMsgRefreshCurrentProgress() {
        this.mAbsHandler.removeMessages(AUTO_REFRESH_PROGRESS);
        this.mAbsHandler.sendEmptyMessage(AUTO_REFRESH_PROGRESS);
    }

    // 判断显示音控，自动隐藏
    private void dealShowVolumeAutoHide(boolean isShow) {
        this.dealShowVolumeView(isShow);
        this.sendMsgDelayedHideVolume();
    }

    // 判断显示播控，自动隐藏
    private void dealShowPlayControlAutoHide(boolean isShow) {
        this.dealShowPlayControlView(isShow);
        this.sendMsgDelayedHidePlayControl();
    }

    // 判断显示/隐藏loading
    private void dealShowLoadingView(boolean isShow) {
        if (this.isUseLoading && isShow) {
            this.mBaseViewController.setLoadingView(true);
        } else {
            this.mBaseViewController.setLoadingView(false);
        }
    }

    // 判断显示/隐藏buffer
    private void dealShowBufferView(boolean isShow) {
        if(isUseLoading) {
            if (this.mBuffering && this.isUseBuffer && isShow) {
                this.mBaseViewController.setBufferView(true, this.mBufferPercent);
            } else {
                this.mBaseViewController.setBufferView(false, this.mBufferPercent);
            }
        }
    }

    // 判断显示/隐藏播放暂停
    private void dealShowPlayPauseView(boolean isShow) {
        if (!this.mMustHidePauseView && this.isUsePause && isShow
                && !this.mPlayControl.isPlaying()) {
            this.mBaseViewController.setPlayPauseView(true);
        } else {
            this.mBaseViewController.setPlayPauseView(false);
        }
    }

    // 判断显示/隐藏播控
    private void dealShowPlayControlView(boolean isShow) {
        if (this.isUsePlayControl && isShow) {
            this.mBaseViewController.setPlayControlView(true);
            this.mAbsHandler.removeMessages(AUTO_HIDE_PLAY_CONTROLLER);
        } else {
            this.mBaseViewController.setPlayControlView(false);
        }
    }

    // 判断显示/隐藏音控
    private void dealShowVolumeView(boolean isShow) {
        if (this.isUseVolume && isShow) {
            this.mBaseViewController.setVolumeView(true, this.mVolumeDiv);
        } else {
            this.mBaseViewController.setVolumeView(false, this.mVolumeDiv);
        }
    }

    // 非50设备、非第三方设备在buffer时暂停播放
    private void handlerPauseForBuffer() {
        this.mMustHidePauseView = true;
        if (this.mOriginalType != null
                && this.mOriginalType != DeviceType.DEVICE_OTHER
                && this.mOriginalType != DeviceType.DEVICE_S50) {
            if (!PlayUtils.isBufferSelect()) {
                this.handlerPause();
            }
        }
    }

    // 非50设备在快进时暂停播放
    private void handlerPauseForSeek() {
        this.mMustHidePauseView = true;
        if (this.mOriginalType != DeviceType.DEVICE_S50
                && !TerminalUtils.TERMINAL_BRAND_HISENSE
                        .equalsIgnoreCase(TerminalUtils.TERMINAL_BRAND)) {
            if (!PlayUtils.isBufferSelect()) {
                this.handlerPause();
            }
        }
    }

    // buffer结束，恢复播放
    // NOTE(caiwei),可能并不是所有的设备buffer完后都需要主动恢复播放
    private void handlerPlayForBuffer() {
        this.handlerPlay();
    }

    // 快进完成，恢复播放
    private void handlerPlayForSeek() {
        // NOTE(caiwei),可能并不是所有的设备快进完后都需要主动恢复播放
        this.handlerPlay();
    }

    // 暂停播放
    private void handlerPause() {
        if (this.mPlayControl != null && this.mPlayControl.isPlaying()) {
            this.mPlayControl.pause();
        }
    }

    // 恢复播放
    private void handlerPlay() {
        this.mMustHidePauseView = false;
        if (this.mPlayControl != null && !this.mBuffering
                && !this.mPlayControl.isPlaying()) {
            this.mPlayControl.play();
            // 如果暂停界面显示，隐藏暂停界面
            this.dealShowPlayPauseView(false);
        }
    }
}
