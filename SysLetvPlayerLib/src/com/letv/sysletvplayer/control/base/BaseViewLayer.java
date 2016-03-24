package com.letv.sysletvplayer.control.base;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.letv.mobile.core.scaleview.ScaleCalculator;
import com.letv.mobile.core.utils.StringUtils;
import com.letv.mobile.core.utils.VolumeUtils;

import com.letv.sysletvplayer.R;
import com.letv.sysletvplayer.util.DipToPx;
import com.letv.sysletvplayer.util.ViewUtils;

/**
 * 播放器view布局相关实现的基类
 * 应用层可继承该类，实现布局的重写
 * @author caiwei
 */
public class BaseViewLayer {
    protected BasePlayControllerImpl mPlayControl;
    protected Context mContext;
    private LayoutInflater mInflater;
    private AudioManager mAudioManager;
    private Resources mResources;
    /*************** 播放布局 *********************/
    private RelativeLayout mViewGroup;// 总体布局
    protected FrameLayout mPlayLayout;// playview的父布局
    protected View mPlayView;// 播放子布局
    /*************** Loading布局 *********************/
    protected View mPlayLoadingLayout;// loading页面布局
    protected View mPlayloadingTitleContent;// loading页的文字显示(片名+碼流)
    /*************** 缓冲布局 *********************/
    protected View mPlayBufferLayout;// 缓冲布局
    protected View mPlayBufferProgress;// 缓冲进度
    /*************** 播放暂停布局 *********************/
    protected View mPlayPauseLayout;// 暂停布局
    /**************** 音量控制布局 **********/
    protected RelativeLayout mVolumeControllerLayout;
    protected TextView mTvCurrentVolume;// 当前音量
    protected SeekBar mVolumeProBar;// 音量进度
    protected Drawable mVolumeDrawable;// 有声音图片
    protected Drawable mMuteDrawable;// 静音图片
    protected ImageView mVolumeStateImg;// 声音状态（有声音或静音）
    protected ImageView mPlayOrPauseImg;// 播放/暂停
    protected Drawable mVolumePlayDrawable;// 播放 drawable
    protected Drawable mVolumePauseDrawable;// 暂停 drawable
    protected TextView mTvVolumeRaise;// 音量+
    protected TextView mTvVolumeLower;// 音量-
    protected int mVolumeFoucsColor;// 音量调节字体有焦点颜色
    protected int mVolumeNormalColor;// 音量调节字体正常颜色
    /*************** 播放控制布局 *********************/
    protected RelativeLayout mPlayControlLayout;// 播放控制界面
    // private View mTopTitleLayout;// 顶部显示影片信息的布局（包括片名、提示）
    protected TextView mPlayControlTitleView;// 当前正在播放的片名
    // private TextView mPlayControlTostView;// 右上角的提示内容
    protected SeekBar mProgressSeekBar;// seekBar
    protected TextView mPlayBtnMarkView;// 快进快退图标
    protected TextView mPlayTotalTimeView;// 视频时长
    protected TextView mPlayCurrentTimeView;// 当前播放点
    private TextView mPlayCurrentSpeedView;// 当前的下载速度
    protected Drawable mBackDrawable;// back drawable
    protected Drawable mForwardDrawable;// forward drawable
    private Drawable mPauseDrawable;// pause drawable
    protected Drawable mStartDrawable;// start drawable
    // /*************** 整点报时布局 *********************/
    // private TextView mStrikeHourView;
    /******************* 总布局的分布局 ********************/
    private ViewGroup mInsideOfplayViewLayout;// 在playView里面层的布局（350界面的父布局）
    private ViewGroup mOutsideOfplayViewLayout;// 在playView外面层的布局（350界面缓冲，loading的父布局）
    protected ViewGroup mCanManualClearFunctionLayout;// 可手动取消功能布局
    protected ViewGroup mCannotManualClearFunctionLayout;// 不可手动取消的功能布局
    /************************** 进度条上时间控件的位置 ***********************/
    private float iTimeUpX = 0;
    private final static int sPlayOpt = 120;
    private final static int ePlayOpt = 160;
    private final static int playTimeLen = 110;
    private static int sPlayOpt1 = 0, ePlayOpt1 = 0, playTimeLen1 = 0;
    private String mPlayTitle;// 播放的标题
    private OnTouchListener mMouseTouchListener;
    private OnSeekBarChangeListener mSeekBarChangeListener;

    public void setPlayTitle(String title) {
        this.mPlayTitle = title;
    }

    public void setProgressBarTouchListener(OnTouchListener mouseTouchListener) {
        this.mMouseTouchListener = mouseTouchListener;
    }

    public void setProgressBarChangeListener(
            OnSeekBarChangeListener seekBarChangeListener) {
        this.mSeekBarChangeListener = seekBarChangeListener;
    }

    public void setPlayTouchListener(OnTouchListener playTouchListener) {
        this.mPlayView.setOnTouchListener(playTouchListener);
    }

    public void setPlayKeyListener(OnKeyListener onKeyListener) {
        this.mPlayView.setOnKeyListener(onKeyListener);
    }

    public void setMouseTouchListener(OnTouchListener mouseTouchListener) {
        this.mMouseTouchListener = mouseTouchListener;
    }

    /**
     * 初始化
     * @param mViewGroup
     * @param mPlayControl
     * @param mContext
     */
    public void init(ViewGroup mViewGroup, BasePlayControllerImpl mPlayControl,
            Context mContext) {
        this.mPlayControl = mPlayControl;
        this.mViewGroup = (RelativeLayout) mViewGroup;
        this.mContext = mContext;
        this.initData();
        this.initView();
    }

    // 初始化数据
    private void initData() {
        this.mInflater = LayoutInflater.from(this.mContext);
        this.mAudioManager = (AudioManager) this.mContext
                .getSystemService(Context.AUDIO_SERVICE);
        this.mResources = this.mContext.getResources();
        this.initPx();
    }

    // 初始化界面
    private void initView() {
        this.initLayout();
        this.initPlayView();
    }

    /**
     * 初始化布局
     */
    protected void initLayout() {
        this.mPlayLayout = (FrameLayout) this.mViewGroup
                .findViewById(R.id.play_rootView);
        this.mCanManualClearFunctionLayout = (ViewGroup) this.mViewGroup
                .findViewById(R.id.play_function_layout2);
        this.mCannotManualClearFunctionLayout = (ViewGroup) this.mViewGroup
                .findViewById(R.id.play_function_layout1);
        this.mInsideOfplayViewLayout = (ViewGroup) this.mViewGroup
                .findViewById(R.id.stream_350_layout);
        this.mOutsideOfplayViewLayout = (ViewGroup) this.mViewGroup
                .findViewById(R.id.stream_350_layout_item);
    }

    /**
     * 初始化PlayView的宽、高、位置参数
     */
    protected void initPlayView() {
        int width = FrameLayout.LayoutParams.MATCH_PARENT;
        int height = FrameLayout.LayoutParams.MATCH_PARENT;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width,
                height);
        params.leftMargin = 0;
        params.topMargin = 0;
        params.gravity = Gravity.CENTER;
        this.mPlayView = this.mPlayControl.getPlayView();
        this.mPlayView.setFocusable(true);
        this.mPlayLayout.addView(this.mPlayView, params);
    }

    /************************************ loading界面处理 ************************/
    /**
     * 使loading界面可见/不可见
     * @param loading
     */
    public void setLoadingView(boolean loading) {
        if (loading) {
            this.removeAllFunctionView();
            if (this.mPlayLoadingLayout == null) {
                this.initLoadingView();
            }
            this.showLoadingView(null);
            return;
        }
        this.hideLoadingView();
    }

    /**
     * 初始化loading界面
     */
    protected void initLoadingView() {
        if (this.mInflater == null) {
            this.mInflater = LayoutInflater.from(this.mContext);
        }
        this.mPlayLoadingLayout = this.mInflater.inflate(
                R.layout.play_loading_layout, null);
        this.mPlayloadingTitleContent = this.mPlayLoadingLayout
                .findViewById(R.id.playloading_title_content);
    }

    /**
     * 显示loading界面
     * @param text
     */
    protected void showLoadingView(String text) {
        if (!ViewUtils.isVisibleView(this.mPlayLoadingLayout)) {
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            this.mCannotManualClearFunctionLayout.addView(
                    this.mPlayLoadingLayout, params);
            this.mPlayLoadingLayout.setVisibility(View.VISIBLE);
        }
        this.showLoadingText(this.mPlayloadingTitleContent, text);
    }

    /**
     * 显示loading文字
     * @param textView
     * @param text
     */
    protected void showLoadingText(View textView, String text) {
        if (TextUtils.isEmpty(text)) {
            String videoName = null;
            if (this.mPlayTitle == null) {
                return;
            } else {
                videoName = this.mPlayTitle;
            }
            StringBuffer stringBuilder = new StringBuffer(
                    this.mResources.getString(R.string.play_loaing_toast));
            stringBuilder.append(StringUtils.handlerStr(videoName, 15));
            ((TextView) textView).setText(stringBuilder.toString());
        } else {
            ((TextView) textView).setText(text);
        }
    }

    /**
     * 隐藏loading界面
     */
    protected void hideLoadingView() {
        if (this.mPlayLoadingLayout != null) {
            this.mPlayLoadingLayout.setVisibility(View.GONE);
            this.mCannotManualClearFunctionLayout
                    .removeView(this.mPlayLoadingLayout);
        }
    }

    /**************************** 播放暂停界面处理 ****************************************/

    /**
     * 使播放暂停界面可见/不可见
     * @param isShowPauseView
     */
    public void setPlayPauseView(boolean isShowPauseView) {
        if (isShowPauseView) {
            if (this.mPlayPauseLayout == null) {
                this.initPlayPauseView();
            }
            this.showCannotManualClearFunctionView(this.mPlayPauseLayout);
            return;
        }
        this.hidePlayPauseView();
    }

    /**
     * 初始化播放暂停View
     */
    protected void initPlayPauseView() {
        this.mPlayPauseLayout = this.mInflater.inflate(R.layout.play_pause,
                null);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        this.mPlayPauseLayout.setLayoutParams(params);
    }

    /**
     * 隐藏播放暂停界面
     */
    protected void hidePlayPauseView() {
        this.hideCannotManualClearFunctionView(this.mPlayPauseLayout);
    }

    /************************** 播控界面处理 ******************************************/

    /**
     * 快进中， 更新seek进度和时间点
     */
    public void dealSeekToTimeChanged(boolean isLeft, int willSeekToTime) {
        this.updateSeekBarBufferProgress();
        this.mProgressSeekBar.setProgress(willSeekToTime);
        this.mPlayCurrentTimeView.setText(this.formatTime(willSeekToTime));
        if (isLeft) {
            this.mPlayBtnMarkView.setBackgroundDrawable(this.mBackDrawable);
        } else {
            this.mPlayBtnMarkView.setBackgroundDrawable(this.mForwardDrawable);
        }
    }

    /**
     * 快进结束， 更新seek进度和时间点
     */
    public void dealSeekToTimeChangedOver() {
        this.updateSeekBarBufferProgress();
        int curProgress = this.mPlayControl.getCurrentPosition();
        if (curProgress > 0) {
            this.mProgressSeekBar.setProgress(curProgress);
            this.mPlayCurrentTimeView.setText(this.formatTime(curProgress));
        }
        this.mPlayBtnMarkView.setBackgroundDrawable(this.mStartDrawable);
    }

    /**
     * 处理进度条更新
     * @param
     * @param progress
     * @param
     */
    public void dealProgressChanged(int progress) {
        int duration = this.mPlayControl.getVideoDuration();
        if (this.mPlayView != null && duration > 0
                && this.mProgressSeekBar.getWidth() > 0) {
            this.iTimeUpX = this.mProgressSeekBar.getLeft()
                    + this.mProgressSeekBar.getWidth()
                    * ((float) progress / (float) duration);
            this.setTimeTextViewLocaton(this.iTimeUpX, progress);
        } else {
            this.iTimeUpX = -1;
            this.setTimeTextViewLocaton(this.iTimeUpX, progress);
        }
    }

    /**
     * 使播控界面可见/不可见
     * @param isShowPlayControlView
     */
    public void setPlayControlView(boolean isShowPlayControlView) {
        if (isShowPlayControlView) {
            if (this.mPlayControlLayout == null) {
                this.initPlayControlView();
            }
            if (this.mPlayControlLayout.getVisibility() != View.VISIBLE) {
                this.mCanManualClearFunctionLayout
                        .addView(this.mPlayControlLayout);
                this.mPlayControlLayout.setVisibility(View.VISIBLE);
            }
            this.initPlayControlContent();
            return;
        }
        this.hidePlayControlView();
    }

    /**
     * 初始化播控界面
     */
    protected void initPlayControlView() {
        this.mPlayControlLayout = (RelativeLayout)this.mInflater.inflate(
                R.layout.play_controller, null);
        this.mPlayControlTitleView = (TextView) this.mPlayControlLayout
                .findViewById(R.id.play_video_name);
        this.mProgressSeekBar = (SeekBar) this.mPlayControlLayout
                .findViewById(R.id.play_seek_bar);
        this.mPlayBtnMarkView = (TextView) this.mPlayControlLayout
                .findViewById(R.id.play_btn_mark);
        this.mPlayTotalTimeView = (TextView) this.mPlayControlLayout
                .findViewById(R.id.play_total_time);
        this.mPlayCurrentTimeView = (TextView) this.mPlayControlLayout
                .findViewById(R.id.play_current_time);
        this.mPlayCurrentSpeedView = (TextView) this.mPlayControlLayout
                .findViewById(R.id.play_video_speed);
        this.mForwardDrawable = this.mResources
                .getDrawable(R.drawable.play_btn_forward);
        this.mBackDrawable = this.mResources
                .getDrawable(R.drawable.play_btn_back);
        this.mPauseDrawable = this.mResources
                .getDrawable(R.drawable.play_btn_pause);
        this.mStartDrawable = this.mResources
                .getDrawable(R.drawable.play_btn_start);
        if (this.mSeekBarChangeListener != null) {
            this.mProgressSeekBar
                    .setOnSeekBarChangeListener(this.mSeekBarChangeListener);
        }
        if (this.mMouseTouchListener != null) {
            this.mProgressSeekBar.setOnTouchListener(this.mMouseTouchListener);
        }
    }

    /**
     * 初始化播放控制上的进度、总时长、文字
     */
    protected void initPlayControlContent() {
        int max = this.mPlayControl.getVideoDuration();
        this.mPlayTotalTimeView.setText(this.formatTime(max));
        if (this.mProgressSeekBar != null
                && this.mProgressSeekBar.getMax() != max) {
            this.mProgressSeekBar.setMax(max);
        }
        this.updateSeekBarBufferProgress();
        if (this.mPlayTitle != null) {
            this.mPlayControlTitleView.setText(this.mPlayTitle);
        }
    }

    /**
     * 隐藏播控界面
     */
    protected void hidePlayControlView() {
        if (this.mPlayControlLayout != null) {
            this.mPlayControlLayout.setVisibility(View.GONE);
            this.mCanManualClearFunctionLayout
                    .removeView(this.mPlayControlLayout);
        }
    }

    /**
     * 处理进度条上的缓冲进度显示
     */
    protected void updateSeekBarBufferProgress() {
        int bufferProgress = this.mPlayControl.getTotalBufferProgress();
        this.mProgressSeekBar.setSecondaryProgress(bufferProgress);
    }

    /**
     * 设置当前时间的位置
     */
    protected void setTimeTextViewLocaton(float iTimeUpX, int progress) {
        RelativeLayout.LayoutParams timeLayPms = (RelativeLayout.LayoutParams) this.mPlayCurrentTimeView
                .getLayoutParams();
        timeLayPms.leftMargin = (int) (iTimeUpX - playTimeLen1);
        if (progress <= 100 || iTimeUpX == -1) {
            this.mPlayCurrentTimeView.setVisibility(View.GONE);
        } else if (progress > 100
                && this.mPlayCurrentTimeView.getVisibility() == View.GONE) {
            this.refreshTimLayPms(timeLayPms);
            this.mPlayCurrentTimeView.setVisibility(View.VISIBLE);
        } else {
            this.refreshTimLayPms(timeLayPms);
        }
    }

    protected void refreshTimLayPms(RelativeLayout.LayoutParams timeLayPms) {
        int width = this.mProgressSeekBar.getWidth();
        if (timeLayPms.leftMargin < sPlayOpt1) {
            if (this.mPlayTotalTimeView.getVisibility() == View.GONE) {
                this.mPlayTotalTimeView.setVisibility(View.VISIBLE);
            }
            timeLayPms.leftMargin = sPlayOpt1;
        } else if (timeLayPms.leftMargin > width - ePlayOpt1) {
            this.mPlayTotalTimeView.setVisibility(View.GONE);
        } else {
            if (this.mPlayTotalTimeView.getVisibility() == View.GONE) {
                this.mPlayTotalTimeView.setVisibility(View.VISIBLE);
            }
        }
        this.mPlayCurrentTimeView.setLayoutParams(timeLayPms);
    }

    /************************** 缓冲界面处理 ******************************************/
    /**
     * 使缓冲界面可见/不可见
     *
     */
    public void setBufferView(boolean isShowBufferView, int percent) {
        // 如果loading界面正在显示，不能显示缓冲布局
        boolean isLoading = ViewUtils.isVisibleView(this.mPlayLoadingLayout);
        if (!isLoading && isShowBufferView) {
            // 缓冲布局显示时，如果音控界面正在显示，须隐藏音控界面
            if (ViewUtils.isVisibleView(this.mVolumeControllerLayout)) {
                this.hideCanManualClearFunctionView(this.mVolumeControllerLayout);
            }
            // 缓冲布局显示时，如果暂停图标显示，须隐藏暂停图标
            if (ViewUtils.isVisibleView(this.mPlayPauseLayout)) {
                this.hidePlayPauseView();
            }
            if (this.mPlayBufferLayout == null) {
                this.initPlayBufferView();
            }
            this.showCannotManualClearFunctionView(this.mPlayBufferLayout);
            this.updateBufferProgressView(percent);
            return;
        }
        this.hidePlayBufferView();
    }

    /**
     * 初始化缓冲View
     */
    protected void initPlayBufferView() {
        this.mPlayBufferLayout = this.mInflater.inflate(R.layout.play_buffer,
                null);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        this.mPlayBufferLayout.setLayoutParams(params);
        this.mPlayBufferProgress = this.mPlayBufferLayout
                .findViewById(R.id.play_progress_view);
    }

    // 更新缓冲进度显示
    protected void updateBufferProgressView(int progress) {
        if (this.mPlayBufferProgress != null) {
            ((TextView) this.mPlayBufferProgress).setText(progress + "%");
        }
    }

    /**
     * 隐藏缓冲View
     */
    protected void hidePlayBufferView() {
        this.hideCannotManualClearFunctionView(this.mPlayBufferLayout);
    }

    /******************************** 音量界面处理 ************************************/
    /**
     * 音量按键按下，更新音量大小及显示
     * @param isRaise
     * @param div
     */
    public void dealVolumeKeyDownRaiseOrLow(boolean isRaise, double div) {
        if (isRaise) {
            this.setVolumeTextColor(this.mTvVolumeRaise, true);
            this.setVolume(AudioManager.ADJUST_RAISE, div);
        } else {
            this.setVolumeTextColor(this.mTvVolumeLower, true);
            this.setVolume(AudioManager.ADJUST_LOWER, div);
        }
    }

    /**
     * 音量按键抬起，重新显示
     * @param isRaise
     */
    public void dealVolumeKeyUpRaiseOrLow(boolean isRaise) {
        if (isRaise) {
            this.setVolumeTextColor(this.mTvVolumeRaise, false);
        } else {
            this.setVolumeTextColor(this.mTvVolumeLower, false);
        }
    }

    /**
     * 使音量界面可见/不可见
     * @param
     */
    public void setVolumeView(boolean isShowVolumeView, double div) {
        if (isShowVolumeView) {
            if (this.mVolumeControllerLayout == null) {
                this.initVolumeController();
            }
            if (this.mPlayControl.isPlaying()) {
                this.mPlayOrPauseImg.setImageDrawable(this.mVolumePlayDrawable);
            } else {
                this.mPlayOrPauseImg
                        .setImageDrawable(this.mVolumePauseDrawable);
            }
            int currentVolume = VolumeUtils.getCurrentStreamVolume(
                    this.mContext, this.mAudioManager);
            if (currentVolume == 0) {
                this.mVolumeStateImg.setImageDrawable(this.mMuteDrawable);
            } else {
                this.mVolumeStateImg.setImageDrawable(this.mVolumeDrawable);
            }
            this.setVolume(AudioManager.ADJUST_SAME, div);
            this.showCanManualClearFunctionView(this.mVolumeControllerLayout);
            return;
        }
        this.hideVolumeView();

    }

    /**
     * 初始化音量界面
     */
    protected void initVolumeController() {
        this.mVolumeControllerLayout = (RelativeLayout) this.mInflater.inflate(
                R.layout.volume_controller, null);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        this.mVolumeControllerLayout.setLayoutParams(params);
        this.mTvCurrentVolume = (TextView) this.mVolumeControllerLayout
                .findViewById(R.id.tv_curr_volume);
        this.mVolumeProBar = (SeekBar) this.mVolumeControllerLayout
                .findViewById(R.id.volume_progress_bar);
        // 设置progressBar最大值
        int maxVolume = VolumeUtils.getMaxStreamVolume(this.mContext,
                this.mAudioManager);
        this.mVolumeProBar.setMax(maxVolume);
        this.mVolumeStateImg = (ImageView) this.mVolumeControllerLayout
                .findViewById(R.id.iv_volume_state);
        this.mPlayOrPauseImg = (ImageView) this.mVolumeControllerLayout
                .findViewById(R.id.play_or_pause);
        this.mTvVolumeRaise = (TextView) this.mVolumeControllerLayout
                .findViewById(R.id.volume_up);
        this.mTvVolumeLower = (TextView) this.mVolumeControllerLayout
                .findViewById(R.id.volume_down);
        this.mVolumeDrawable = this.mResources
                .getDrawable(R.drawable.volume_pic);
        this.mMuteDrawable = this.mResources
                .getDrawable(R.drawable.volume_mute);
        this.mVolumePauseDrawable = this.mResources
                .getDrawable(R.drawable.play_pause_pic);
        this.mVolumePlayDrawable = this.mResources
                .getDrawable(R.drawable.play_play_pic);
        this.mVolumeFoucsColor = this.mResources.getColor(R.color.volume_focus);
        this.mVolumeNormalColor = this.mResources
                .getColor(R.color.volume_normal);
        this.mAudioManager = VolumeUtils.getAudioManager(this.mContext);
    }

    /**
     * 设置音量显示
     * @param direction
     *            1：增加音量 -1：减小音量 0：不变
     * @param div
     *            c1、c1s是15.0，第三方设备为10.0
     */
    public void setVolume(int direction, double div) {
        VolumeUtils.setStreamVolume(this.mAudioManager, direction, div);
        int currentVolume = VolumeUtils.getCurrentStreamVolume(this.mContext,
                this.mAudioManager);
        if (currentVolume == 0) {
            this.mVolumeStateImg.setImageDrawable(this.mMuteDrawable);
        } else {
            this.mVolumeStateImg.setImageDrawable(this.mVolumeDrawable);
        }
        this.mTvCurrentVolume.setText(currentVolume + "");
        this.mVolumeProBar.setProgress(currentVolume);

    }

    /**
     * 设置音量文字颜色
     * @param textView
     * @param hasFoucs
     */
    public void setVolumeTextColor(TextView textView, boolean hasFoucs) {
        if (hasFoucs) {
            textView.setTextColor(this.mVolumeFoucsColor);
        } else {
            textView.setTextColor(this.mVolumeNormalColor);
        }
    }

    /**
     * 隐藏音量View
     */
    protected void hideVolumeView() {
        this.hideCanManualClearFunctionView(this.mVolumeControllerLayout);
    }

    /*************************** View布局处理 ************************************/
    /**
     * 移除所有功能界面
     */
    public void removeAllFunctionView() {
        this.hideAllCanManualClearFuctionView();
        this.hideCannotManualClearFunctionView(this.mPlayBufferLayout);
        this.hideCannotManualClearFunctionView(this.mPlayPauseLayout);
    }

    /**
     * 隐藏所有可手动取消的FunctionView
     */
    public boolean hideAllCanManualClearFuctionView() {
        boolean removeFlag = false;
        if (this.mCanManualClearFunctionLayout != null) {
            int count = this.mCanManualClearFunctionLayout.getChildCount();
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    View view = this.mCanManualClearFunctionLayout
                            .getChildAt(i);
                    if (view != null) {
                        if(ViewUtils.isVisibleView(view)){
                            view.setVisibility(View.GONE);
                            this.mCanManualClearFunctionLayout.removeView(view);
                            removeFlag = true;
                        }
                    }
                }
            }
        }
        return removeFlag;
    }

    /**
     * 显示可手动取消的功能View
     * @param view
     */
    public void showCanManualClearFunctionView(View view) {
        ViewUtils.showViewByViewGroup(this.mCanManualClearFunctionLayout, view);
    }

    /**
     * 显示不可手动取消的功能View
     * @param view
     */
    public void showCannotManualClearFunctionView(View view) {
        ViewUtils.showViewByViewGroup(this.mCannotManualClearFunctionLayout,
                view);
    }

    /**
     * 显示playView里面层的View
     * @param view
     */
    public void showInsideOfPlayView(View view) {
        if (!ViewUtils.isVisibleView(this.mInsideOfplayViewLayout)) {
            this.mInsideOfplayViewLayout.setVisibility(View.VISIBLE);
        }
        ViewUtils.showViewByViewGroup(this.mInsideOfplayViewLayout, view);
    }

    /**
     * 显示playView外面层的View
     * @param view
     */
    public void showOutsideOfPlayView(View view) {
        if (!ViewUtils.isVisibleView(this.mOutsideOfplayViewLayout)) {
            this.mOutsideOfplayViewLayout.setVisibility(View.VISIBLE);
        }
        ViewUtils.showViewByViewGroup(this.mOutsideOfplayViewLayout, view);
    }

    /**
     * 隐藏可手动取消的功能View
     * @param view
     */
    public void hideCanManualClearFunctionView(View view) {
        ViewUtils.hideViewByViewGroup(this.mCanManualClearFunctionLayout, view);
    }

    /**
     * 隐藏不可手动取消的功能View
     * @param view
     */
    public void hideCannotManualClearFunctionView(View view) {
        ViewUtils.hideViewByViewGroup(this.mCannotManualClearFunctionLayout,
                view);
    }

    /**
     * 隐藏playView里面层的布局包含的View
     * @param view
     */
    public void hideInsideOfPlayView(View view) {
        ViewUtils.hideViewByViewGroup(this.mInsideOfplayViewLayout, view);
    }

    /**
     * 隐藏playView外面层的布局包含的View
     * @param view
     */
    public void hideOutsideOfPlayView(View view) {
        ViewUtils.hideViewByViewGroup(this.mOutsideOfplayViewLayout, view);
    }

    public  void hidePlayerControlView() {
        this.hideCanManualClearFunctionView(this.mPlayControlLayout);
    }

    public View getPlayView() {
        return this.mPlayView;
    }

    public FrameLayout getPlayLayout() {
        return this.mPlayLayout;
    }
    public ViewGroup getPlayControlLayout() { return this.mPlayControlLayout;}

    public ViewGroup getCannotManualClearLayout() {
        return this.mCannotManualClearFunctionLayout;
    }

    public ViewGroup getCanManualClearLayout() {
        return this.mCanManualClearFunctionLayout;
    }

    public boolean isLoadingVisible() {
        return ViewUtils.isVisibleView(this.mPlayLoadingLayout);
    }

    public boolean isBufferVisible() {
        return ViewUtils.isVisibleView(this.mPlayBufferLayout);
    }

    public boolean isPlayControlVisible() {
        return ViewUtils.isVisibleView(this.mPlayControlLayout);
    }

    public boolean isVolumeControlVisible() {
        return ViewUtils.isVisibleView(this.mVolumeControllerLayout);
    }

    public int getSeekProgress() {
        return this.mProgressSeekBar.getProgress();
    }

    // NOTE(caiwei):seek位置设置，暂时按以前的，后续看是否需要调整
    private void initPx() {
        DipToPx dtp = new DipToPx(this.mContext);
        ScaleCalculator scaleCaculator = ScaleCalculator.getInstance();
        sPlayOpt1 = scaleCaculator.scaleWidth(dtp.dipToPx(sPlayOpt));
        ePlayOpt1 = scaleCaculator.scaleWidth(dtp.dipToPx(ePlayOpt));
        playTimeLen1 = scaleCaculator.scaleWidth(dtp.dipToPx(playTimeLen));
    }

    private String formatTime(int time) {
        if (time < 0) {
            return "0";
        }
        time /= 1000;
        int minute = time / 60;
        int hour = minute / 60;
        int second = time % 60;
        minute %= 60;
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

}