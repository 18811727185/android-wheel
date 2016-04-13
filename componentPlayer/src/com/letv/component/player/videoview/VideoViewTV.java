package com.letv.component.player.videoview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.media.Metadata;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.MediaController;
import android.widget.RelativeLayout.LayoutParams;

import com.letv.component.player.Interface.OnMediaStateTimeListener;
import com.letv.component.player.Interface.OnMediaStateTimeListener.MeidaStateType;
import com.letv.component.player.Interface.OnNeedSetPlayParamsListener;
import com.letv.component.player.Interface.OnVideoViewStateChangeListener;
import com.letv.component.player.LetvMediaPlayerControl;
import com.letv.component.player.core.LetvMediaPlayerManager;
import com.letv.component.player.core.PlayUrl;
import com.letv.component.player.http.HttpRequestManager;
import com.letv.component.player.utils.LogTag;
import com.letv.component.player.utils.Tools;
import com.media.ffmpeg.FFMpegPlayer.OnAdNumberListener;
import com.media.ffmpeg.FFMpegPlayer.OnBlockListener;
import com.media.ffmpeg.FFMpegPlayer.OnCacheListener;
import com.media.ffmpeg.FFMpegPlayer.OnFirstPlayLitener;
import com.media.ffmpeg.FFMpegPlayer.OnHardDecodeErrorListner;
import com.stv.launcher.sdk.player.LetvPlayer;
import com.stv.launcher.sdk.player.LetvPlayerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chenyueguo
 */
public class VideoViewTV extends SurfaceView implements LetvMediaPlayerControl {
    // changed for tvlive by zjl 2015-11-05 17:26:49
    private static final int MULTIPLICAND_VIDEOSIZE9 = 9;
    private static final int MULTIPLICAND_VIDEOSIZE16 = 16;

    private static final int STATE_ERROR = -1;// 错误状态
    private static final int STATE_IDLE = 0;// 空闲状态
    private static final int STATE_PREPARING = 1;// 准备中状态
    private static final int STATE_PREPARED = 2;// 准备好状态
    private static final int STATE_PLAYING = 3;// 播放状态
    private static final int STATE_PAUSED = 4;// 暂停状态
    private static final int STATE_PLAYBACK_COMPLETED = 5;// 完成回播状态

	private final int FORWARD_TIME = 20000;
	private final int REWIND_TIME = 20000;

    private Uri mUri;
    private Map<String, String> mHeaders;
    private int duration;
    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;
    private static AtomicInteger atomicInteger = new AtomicInteger(1); //代表VideoViewTV的实例个数
    private int mCount;

    private SurfaceHolder mSurfaceHolder = null;
    private LetvPlayer mMediaPlayer = null;
    protected Context mContext;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private MediaController mediaController;
    private OnCompletionListener mOnCompletionListener;
    private OnPreparedListener mOnPreparedListener;
    private OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    private OnInfoListener mOnInfoListener;
    private OnSeekCompleteListener mOnSeekCompleteListener;
    private OnBufferingUpdateListener mOnBufferingUpdateListener;
    private OnMediaStateTimeListener mOnMediaStateTimeListener;
    private OnErrorListener mOnErrorListener;
    private int mSeekWhenPrepared;

    private boolean mCanPause;
    private boolean mCanSeekBack;
    private boolean mCanSeekForward;

    private int mCurrentBufferPercentage;

    // changed for tvlive by zjl 2015-11-05 17:26:49
    /**
     * 是否改变播放窗口尺寸
     * 默认 false
     * 第三方 true
     */
    private boolean mScreenChangeFlag = false;

    /**
     * 记录消耗前的时间点
     */
    protected int lastSeekWhenDestoryed = 0;

    /**
     * 自适应屏幕 -1-初始化状态 0-自动 1-4:3 2-16:9
     */
    private int mRatioType = -1;

    public VideoViewTV(Context context) {
        super(context);
        this.mContext = context;
        this.initVideoView();
        mCount = atomicInteger.getAndIncrement();
        LogTag.i("VideoViewTV","["+mCount+"]"+"VideoViewTV create");
        //不需要设置背景色
        // changed for tvlive by zanxiaofei 2015-10-28
        //this.setBackgroundColor(Color.argb(101, 99, 22, 00));
        //changed end
    }

    public VideoViewTV(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.mContext = context;
        this.initVideoView();
    }

    public VideoViewTV(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        this.initVideoView();
    }

    private void initVideoView() {
        this.mVideoWidth = 0;
        this.mVideoHeight = 0;
        this.getHolder().addCallback(this.mSHCallback);
        this.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        //初始化时不能立即获取焦点,会影响launcher获取焦点
        // changed for tvlive by zanxiaofei 2015-10-28
        //this.requestFocus();
        //changed end
        this.mCurrentState = STATE_IDLE;
        this.mTargetState = STATE_IDLE;
    }

    public boolean isInPlaybackState() {
        return (this.mMediaPlayer != null && this.mCurrentState != STATE_ERROR
                && this.mCurrentState != STATE_IDLE && this.mCurrentState != STATE_PREPARING);
    }

    /**
     * 给外部提供一个判断当前播放状态的方法
     * @return
     */
    public boolean extIsInPlaybackState() {
        return this.isInPlaybackState();
    }

    @Override
    public boolean canPause() {
        return this.mCanPause;
    }

    @Override
    public boolean canSeekBackward() {
        return this.mCanSeekBack;
    }

    @Override
    public boolean canSeekForward() {
        return this.mCanSeekForward;
    }

    public void stopPlayback() {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.reset();
            String currentDateRelease = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog("["+mCount+"]"+"系统当前时间:  "+currentDateRelease+"VideoViewTV release()");
			if(mOnMediaStateTimeListener!=null){
				mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.RELEASE, currentDateRelease);
			}
            this.mMediaPlayer.release();
            this.mCurrentState = STATE_IDLE;
            this.mTargetState = STATE_IDLE;
        }
        this.mMediaPlayer = null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(this.mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(this.mVideoHeight, heightMeasureSpec);

        // changed for tvlive by zjl 2015-11-05 17:26:49
        // NOTE: 以下代码为第三方适配代码(mScreenChangeFlag为true)
        if (this.mScreenChangeFlag && this.mVideoWidth > 0 && this.mVideoHeight > 0) {
            if (this.mVideoWidth * MULTIPLICAND_VIDEOSIZE9 > MULTIPLICAND_VIDEOSIZE16
                    * this.mVideoHeight) {
                if (this.mVideoWidth * height > width * this.mVideoHeight) {
                    height = width * this.mVideoHeight / this.mVideoWidth;
                } else if (this.mVideoWidth * height < width * this.mVideoHeight) {
                    width = height * this.mVideoWidth / this.mVideoHeight;
                }
            }
        }
        setMeasuredDimension(width, height);
    }

    // changed for tvlive by zjl 2015-11-05 17:26:49
    /**
     * 设置是否是改变播放窗口尺寸
     */
    public void setScreenChangeFlag(boolean screenChangeFlag) {
        this.mScreenChangeFlag = screenChangeFlag;
    }

    // changed for tvlive by zjl 2015-11-05 17:26:49
    /**
     * 返回是否设置了改变播放窗口尺寸
     */
    public boolean getScreenChangeFlag() {
        return this.mScreenChangeFlag;
    }

    /**
     * 自适应屏幕 -1-初始化状态 0-自动 1-4:3 2-16:9
     */
    public void adjust(int type) {
        this.mRatioType = type;
        invalateScreenSize();
    }

    /**
     * 刷新屏幕尺寸
     */
	private void invalateScreenSize() {
		LayoutParams lp = (LayoutParams) this.getLayoutParams();
		this.setLayoutParams(lp);
	}

    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
        case MeasureSpec.UNSPECIFIED:
            result = desiredSize;
            break;

        case MeasureSpec.AT_MOST:
            result = Math.min(desiredSize, specSize);
            break;

        case MeasureSpec.EXACTLY:
            result = specSize;
            break;
        }
        return result;
    }

    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    public void setVideoPath(String path, Map<String, String> headers) {
        setVideoURI(Uri.parse(path), headers);
    }

    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    public void setVideoURI(Uri uri, Map<String, String> headers) {
    	String currentDate = Tools.getCurrentDate();
    	LetvMediaPlayerManager.getInstance().writePlayLog("["+mCount+"]"+"系统当前时间:  " + currentDate + " VideoViewTV(乐视电视videoview)  setVideoURI(), url="
                + ((uri != null) ? uri.toString() : "null"), true);
		if(mOnMediaStateTimeListener!=null){
			mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.INITPATH, currentDate);
		}
    	mUri = uri;
        mHeaders = headers;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    /**
     * 初始化监听接口
     */
    private void initListener() {
        this.mMediaPlayer.setOnSeekCompleteListener(this.mSeekCompleteListener);
        this.mMediaPlayer.setOnInfoListener(this.mInfoListener);
        this.mMediaPlayer.setOnPreparedListener(this.mPreparedListener);
        this.mMediaPlayer
                .setOnVideoSizeChangedListener(this.mSizeChangedListener);
        this.mMediaPlayer.setOnCompletionListener(this.mCompletionListener);
        this.mMediaPlayer.setOnErrorListener(this.mErrorListener);
        this.mMediaPlayer
                .setOnBufferingUpdateListener(this.mBufferingUpdateListener);
    }

    /**
     * 撤销监听
     * @param mMediaPlayer
     */
    private void deadListener(MediaPlayer mMediaPlayer) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setOnSeekCompleteListener(null);
            mMediaPlayer.setOnInfoListener(null);
            mMediaPlayer.setOnPreparedListener(null);
            mMediaPlayer.setOnVideoSizeChangedListener(null);
            mMediaPlayer.setOnCompletionListener(null);
            mMediaPlayer.setOnErrorListener(null);
            mMediaPlayer.setOnBufferingUpdateListener(null);
        }
    }

    @SuppressLint("NewApi")
    private void openVideo() {

        if (this.mUri == null || this.mSurfaceHolder == null) {
            // not ready for playback just yet, will try again later
            return;
        }
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        this.mContext.sendBroadcast(i);
        this.release(false);
        try {
        	String currentDate = Tools.getCurrentDate();
        	LetvMediaPlayerManager.getInstance().writePlayLog("["+mCount+"]"+"系统当前时间:  " + currentDate + " VideoViewH264mp4(乐视电视videoview)  创建MediaPlayer对象");
            if(mOnMediaStateTimeListener!=null){
				mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.CREATE, currentDate);
			}
            //this.mMediaPlayer = LetvPlayerFactory.instantiate(); //使用launcher的app的MediaPlayer进行MeidaPlayer的创建，解决launcher内的播放器资源竞争问题
             LetvPlayerFactory.instantiate(false, new LetvPlayer.OnInstantiateListener() {
                @Override
                public void onInstantiate(LetvPlayer letvPlayer, int i) {
                    VideoViewTV.this.mMediaPlayer = letvPlayer;
                    VideoViewTV.this.initListener();
                    VideoViewTV.this.duration = -1;

                    mCurrentBufferPercentage = 0;
                    //设置播放高低水位
                    //changed for tvlive by zanxiaofei 2015-10-30
                    if(mOnNeedSetPlayParamsListener != null){
                        mOnNeedSetPlayParamsListener.onNeedSet();
                    }
                    //end
                    try {
                        VideoViewTV.this.mMediaPlayer.setDataSource(VideoViewTV.this.mContext, VideoViewTV.this.mUri,
                                VideoViewTV.this.mHeaders);
                    } catch (IOException ex) {
                        VideoViewTV.this.mCurrentState = STATE_ERROR;
                        VideoViewTV.this.mTargetState = STATE_ERROR;
                        VideoViewTV.this.mErrorListener.onError(VideoViewTV.this.mMediaPlayer,
                                MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
                        return;
                    }
                    VideoViewTV.this.mMediaPlayer.setDisplay(VideoViewTV.this.mSurfaceHolder);
                    VideoViewTV.this.mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    VideoViewTV.this.mMediaPlayer.setScreenOnWhilePlaying(true);
                    //changed for tvlive by zanxiaofei 2015-12-22
                    //设置硬解码参数，用于播放停止时将画面停止到最后一帧
                    VideoViewTV.this.mMediaPlayer.setParameter(2001, 3);
                    VideoViewTV.this.mMediaPlayer.prepareAsync();
                    VideoViewTV.this.mCurrentState = STATE_PREPARING;
                    VideoViewTV.this.attachMediaController();
                }
            });
        } catch (IllegalArgumentException ex) {
            this.mCurrentState = STATE_ERROR;
            this.mTargetState = STATE_ERROR;
            this.mErrorListener.onError(this.mMediaPlayer,
                    MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (Exception ex) {
            this.mErrorListener.onError(this.mMediaPlayer,
                    MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        }
    }

    public void setMediaController(MediaController controller) {
        if (this.mediaController != null) {
            this.mediaController.hide();
        }
        this.mediaController = controller;
        this.attachMediaController();
    }

    private void attachMediaController() {
        if (this.mMediaPlayer != null && this.mediaController != null) {
            this.mediaController.setMediaPlayer(this);
            View anchorView = this.getParent() instanceof View ? (View) this
                    .getParent() : this;
            this.mediaController.setAnchorView(anchorView);
            this.mediaController.setEnabled(this.isInPlaybackState());
        }
    }

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int w,
                int h) {
            VideoViewTV.this.mSurfaceWidth = w;
            VideoViewTV.this.mSurfaceHeight = h;
            LogTag.i("VideoViewTV", "["+mCount+"]"+"surfaceChanged(), mSurfaceWidth=" + mSurfaceWidth + ", mSurfaceHeight=" + mSurfaceHeight);
            boolean isValidState = (VideoViewTV.this.mTargetState == STATE_PLAYING);
            boolean hasValidSize = (VideoViewTV.this.mVideoWidth == w && VideoViewTV.this.mVideoHeight == h);
            if (VideoViewTV.this.mMediaPlayer != null && isValidState
                    && hasValidSize) {
                if (VideoViewTV.this.mSeekWhenPrepared != 0) {
                    VideoViewTV.this
                            .seekTo(VideoViewTV.this.mSeekWhenPrepared);
                }
                VideoViewTV.this.start();
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
        	LogTag.i("VideoViewTV", "["+mCount+"]"+"surfaceCreated()");
            VideoViewTV.this.mSurfaceHolder = holder;
            VideoViewTV.this.openVideo();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        	LogTag.i("VideoViewTV", "["+mCount+"]"+"surfaceDestroyed()");
            VideoViewTV.this.mSurfaceHolder = null;
            if (VideoViewTV.this.mediaController != null) {
                VideoViewTV.this.mediaController.hide();
            }
            lastSeekWhenDestoryed = getCurrentPosition() ;
            VideoViewTV.this.release(true);
        }
    };

    /*
     * release the media player in any state
     */
    private void release(boolean cleartargetstate) {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.reset();
            String currentDateRelease = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog("["+mCount+"]"+"系统当前时间:  " + currentDateRelease + "VideoViewTV release()");
			if(mOnMediaStateTimeListener!=null){
				mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.RELEASE, currentDateRelease);
			}
            this.mMediaPlayer.release();
            this.deadListener(this.mMediaPlayer);
            this.mMediaPlayer = null;
            this.mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                this.mTargetState = STATE_IDLE;
            }
        }
    }

    public void reset() {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.reset();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (this.isInPlaybackState() && this.mediaController != null) {
            this.toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (this.isInPlaybackState() && this.mediaController != null) {
            this.toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK
                && keyCode != KeyEvent.KEYCODE_VOLUME_UP
                && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN
                && keyCode != KeyEvent.KEYCODE_VOLUME_MUTE
                && keyCode != KeyEvent.KEYCODE_MENU
                && keyCode != KeyEvent.KEYCODE_CALL
                && keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (this.isInPlaybackState() && isKeyCodeSupported
                && this.mediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (this.mMediaPlayer.isPlaying()) {
                    this.pause();
                    this.mediaController.show();
                } else {
                    this.start();
                    this.mediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!this.mMediaPlayer.isPlaying()) {
                    this.start();
                    this.mediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (this.mMediaPlayer.isPlaying()) {
                    this.pause();
                    this.mediaController.show();
                }
                return true;
            } else {
                this.toggleMediaControlsVisiblity();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

	public int getLastSeekWhenDestoryed() {
		return lastSeekWhenDestoryed;
	}

    private void toggleMediaControlsVisiblity() {
        if (this.mediaController.isShowing()) {
            this.mediaController.hide();
        } else {
            this.mediaController.show();
        }
    }

    @Override
    public void start() {
    	HttpRequestManager.getInstance(mContext).requestCapability();
        if (this.isInPlaybackState()) {
            this.mMediaPlayer.start();
            String currentDate = Tools.getCurrentDate();
            LetvMediaPlayerManager.getInstance().writePlayLog("["+mCount+"]"+"系统当前时间:  "+currentDate+" VideoViewTV(乐视电视videoview)  start()");
            this.mCurrentState = STATE_PLAYING;
        }
        this.mTargetState = STATE_PLAYING;
    }

    public void start(MediaPlayer mMediaPlayer) {
        if (this.isInPlaybackState()) {
            mMediaPlayer.start();
            this.mCurrentState = STATE_PLAYING;
        }
        this.mTargetState = STATE_PLAYING;
    }

    @Override
    public void pause() {
        if (this.isInPlaybackState()) {
            try {
                if (this.mMediaPlayer != null && this.mMediaPlayer.isPlaying()) {
                    this.mMediaPlayer.pause();
                    String currentDate = Tools.getCurrentDate();
                    LetvMediaPlayerManager.getInstance().writePlayLog("["+mCount+"]"+"系统当前时间:  "+currentDate+" VideoViewTV(乐视电视videoview)  pause()");
                    this.mCurrentState = STATE_PAUSED;
                }
            } catch (Exception e) {

            }
        }
        this.mTargetState = STATE_PAUSED;
    }

    public void suspend() {
        this.release(false);
    }

    public void resume() {
        this.openVideo();
    }

    @Override
    public int getCurrentPosition() {
        if (this.isInPlaybackState()) {
            return this.mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public int getDuration() {
        if (this.isInPlaybackState()) {
            if (this.duration > 0) {
                return this.duration;
            }
            this.duration = this.mMediaPlayer.getDuration();
            return this.duration;
        }
        this.duration = -1;
        return this.duration;
    }

    @Override
    public void seekTo(int msec) {
        if (this.isInPlaybackState()) {
            this.mMediaPlayer.seekTo(msec);
            this.mSeekWhenPrepared = 0;
            lastSeekWhenDestoryed = 0;
        } else {
            this.mSeekWhenPrepared = msec;
            lastSeekWhenDestoryed = 0;
        }
    }

	@Override
	public void forward() {
		seekTo(getCurrentPosition() + FORWARD_TIME);
	}

	@Override
	public void rewind() {
		seekTo(getCurrentPosition() - REWIND_TIME);
	}

    @Override
    public boolean isPlaying() {
        try {
            return this.isInPlaybackState() && this.mMediaPlayer.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

	public boolean isPaused() {
		return  mCurrentState == STATE_PAUSED;
	}

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    OnVideoSizeChangedListener mSizeChangedListener = new OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            LogTag.i("VideoViewTV", "["+mCount+"]"+"onVideoSizeChanged(), mVideoWidth=" + mVideoWidth + ", mVideoHeight=" + mVideoHeight);
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
            }

            // changed for tvlive by zjl 2015-11-05 17:26:49
            if (mOnVideoSizeChangedListener != null) {
                mOnVideoSizeChangedListener.onVideoSizeChanged(mp, width, height);
            }
        }
    };

    OnInfoListener mInfoListener = new OnInfoListener() {

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            if (VideoViewTV.this.mOnInfoListener != null
                    && VideoViewTV.this.mOnInfoListener.onInfo(mp, what,
                            extra)) {
                return true;
            }
            return false;
        }
    };

    OnSeekCompleteListener mSeekCompleteListener = new OnSeekCompleteListener() {

        @Override
        public void onSeekComplete(MediaPlayer mp) {
            if (VideoViewTV.this.mOnSeekCompleteListener != null) {
                VideoViewTV.this.mOnSeekCompleteListener
                        .onSeekComplete(mp);
            }
        }
    };

    OnPreparedListener mPreparedListener = new OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
        	String currentDate = Tools.getCurrentDate();
        	LetvMediaPlayerManager.getInstance().writePlayLog("["+mCount+"]"+"系统当前时间:  "+currentDate+" VideoViewTv(乐视电视videoview)  onPrepared()");
			if(mOnMediaStateTimeListener!=null){
				mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.PREPARED, currentDate);
			}
            VideoViewTV.this.mCurrentState = STATE_PREPARED;
            try {
                Class<?> cls = Class.forName(MediaPlayer.class.getName());
                Class<?> partypes[] = new Class[2];
                partypes[0] = Boolean.TYPE;
                partypes[1] = Boolean.TYPE;

                Method meth = cls.getMethod("getMetadata", partypes);
                Object arglist[] = new Object[2];
                arglist[0] = false;
                arglist[1] = false;
                Object retobj = meth.invoke(mp, arglist);
                Metadata data = (Metadata) retobj;
                if (data != null) {
                    VideoViewTV.this.mCanPause = !data
                            .has(Metadata.PAUSE_AVAILABLE)
                            || data.getBoolean(Metadata.PAUSE_AVAILABLE);
                    VideoViewTV.this.mCanSeekBack = !data
                            .has(Metadata.SEEK_BACKWARD_AVAILABLE)
                            || data.getBoolean(Metadata.SEEK_BACKWARD_AVAILABLE);
                    VideoViewTV.this.mCanSeekForward = !data
                            .has(Metadata.SEEK_FORWARD_AVAILABLE)
                            || data.getBoolean(Metadata.SEEK_FORWARD_AVAILABLE);
                } else {
                    VideoViewTV.this.mCanPause = VideoViewTV.this.mCanSeekBack = VideoViewTV.this.mCanSeekForward = true;
                }

                if (VideoViewTV.this.mOnPreparedListener != null
                        && VideoViewTV.this.isInPlaybackState()) {
                    VideoViewTV.this.mOnPreparedListener
                            .onPrepared(VideoViewTV.this.mMediaPlayer);
                }
                if (VideoViewTV.this.mediaController != null) {
                    VideoViewTV.this.mediaController.setEnabled(true);
                }
                VideoViewTV.this.mVideoWidth = mp.getVideoWidth();
                VideoViewTV.this.mVideoHeight = mp.getVideoHeight();

                int seekToPosition = VideoViewTV.this.mSeekWhenPrepared; // mSeekWhenPrepared
                                                                              // may
                                                                              // be
                // changed after seekTo()
                // call
                if (seekToPosition != 0) {
                    VideoViewTV.this.seekTo(seekToPosition);
                }
                if (VideoViewTV.this.mVideoWidth != 0
                        && VideoViewTV.this.mVideoHeight != 0) {
                    // Log.i("@@@@", "video size: " + mVideoWidth +"/"+
                    // mVideoHeight);
                    VideoViewTV.this.getHolder().setFixedSize(
                            VideoViewTV.this.mVideoWidth,
                            VideoViewTV.this.mVideoHeight);
                    if (VideoViewTV.this.mSurfaceWidth == VideoViewTV.this.mVideoWidth
                            && VideoViewTV.this.mSurfaceHeight == VideoViewTV.this.mVideoHeight) {
                        if (VideoViewTV.this.mTargetState == STATE_PLAYING) {
                            VideoViewTV.this.start();
                            if (VideoViewTV.this.mediaController != null) {
                                VideoViewTV.this.mediaController.show();
                            }
                        } else if (!VideoViewTV.this.isPlaying()
                                && (seekToPosition != 0 || VideoViewTV.this
                                        .getCurrentPosition() > 0)) {
                            if (VideoViewTV.this.mediaController != null) {
                                // Show the media controls when we're paused
                                // into a
                                // video and make 'em stick.
                                VideoViewTV.this.mediaController.show(0);
                            }
                        }
                    }
                } else {
                    // We don't know the video size yet, but should start
                    // anyway.
                    // The video size might be reported to us later.
                    if (VideoViewTV.this.mTargetState == STATE_PLAYING) {
                        VideoViewTV.this.start();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    private final OnCompletionListener mCompletionListener = new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            VideoViewTV.this.mCurrentState = STATE_PLAYBACK_COMPLETED;
            VideoViewTV.this.mTargetState = STATE_PLAYBACK_COMPLETED;
            if (VideoViewTV.this.mediaController != null) {
                VideoViewTV.this.mediaController.hide();
            }
            if (VideoViewTV.this.mOnCompletionListener != null) {
                VideoViewTV.this.mOnCompletionListener
                        .onCompletion(VideoViewTV.this.mMediaPlayer);
            }
        }
    };

    private final OnErrorListener mErrorListener = new OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
        	mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            if (mediaController != null) {
            	mediaController.hide();
            }

//            PreferenceUtil.setErrorCode(mContext, "VideoViewTV error, framework_err=" + framework_err + ", impl_err=" + impl_err);
            String currentDate = Tools.getCurrentDate();
            LetvMediaPlayerManager.getInstance().writePlayLog("["+mCount+"]"+"系统当前时间:  "+currentDate+"VideoViewTV(乐视电视videoview) error, framework_err=" + framework_err + ", impl_err=" + impl_err);
			if(mOnMediaStateTimeListener!=null){
				mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.ERROR, currentDate);
			}
            /* If an error handler has been supplied, use it and finish. */
            if (mOnErrorListener != null) {
                if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                    return true;
                }
            }
            return true;
        }
    };

    private final OnBufferingUpdateListener mBufferingUpdateListener = new OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
        	mCurrentBufferPercentage = percent;
            if (VideoViewTV.this.mOnBufferingUpdateListener != null) {
                VideoViewTV.this.mOnBufferingUpdateListener
                        .onBufferingUpdate(mp, percent);
            }
        }
    };

    protected int[] getVideoSize() {
        if (this.mMediaPlayer == null) {
            return null;
        }
        int width = this.mMediaPlayer.getVideoWidth();
        int height = this.mMediaPlayer.getVideoHeight();
        int[] screenSize = new int[2];
        float widthPixels = mSurfaceWidth;
        float heightPixels = mSurfaceHeight;
        float wRatio = width / widthPixels;
        float hRatio = height / heightPixels;
        float ratios = Math.max(wRatio, hRatio);
        screenSize[0] = (int) Math.ceil(width / ratios);
        screenSize[1] = (int) Math.ceil(height / ratios);
        return screenSize;
    }

	@Override
	public View getView() {
		return this;
	}

    /**
     * Register a callback to be invoked when the media file is loaded and ready
     * to go.
     */
    public void setOnPreparedListener(OnPreparedListener l) {
        this.mOnPreparedListener = l;
    }

    public void setOnInfoListener(OnInfoListener l) {
        this.mOnInfoListener = l;
    }

    /**
     * Register a callback to be invoked when the end of a media file has been
     * reached during playback.
     */
    public void setOnCompletionListener(OnCompletionListener l) {
        this.mOnCompletionListener = l;
    }

    public void setOnBufferingUpdateListener(OnBufferingUpdateListener l) {
        this.mOnBufferingUpdateListener = l;
    }

    /**
     * Register a callback to be invoked when an error occurs during playback or
     * setup. If no listener is specified, or if the listener returned false,
     * VideoView will inform the user of any errors.
     */
    public void setOnErrorListener(OnErrorListener l) {
        this.mOnErrorListener = l;
    }

    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener l) {
        this.mOnVideoSizeChangedListener = l;
    }

    public OnSeekCompleteListener getOnSeekCompleteListener() {
        return this.mOnSeekCompleteListener;
    }

    public void setOnSeekCompleteListener(
            OnSeekCompleteListener mOnSeekCompleteListener) {
        this.mOnSeekCompleteListener = mOnSeekCompleteListener;
    }

	@Override
	public void setVideoViewStateChangeListener(
			OnVideoViewStateChangeListener videoViewStateChangeListener) {

	}

	@Override
	public boolean isEnforcementWait() {
		return false;
	}

	@Override
	public void setEnforcementWait(boolean enforcementWait) {

	}

	@Override
	public boolean isEnforcementPause() {
		return false;
	}

	@Override
	public void setEnforcementPause(boolean enforcementPause) {

	}

	@Override
	public void setVideoPlayUrl(PlayUrl url) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOnAdNumberListener(OnAdNumberListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCacheSize(int video_size, int audio_size, int picutureSize, int startpic_size) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOnBlockListener(OnBlockListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOnMediaStateTimeListener(OnMediaStateTimeListener l) {
		this.mOnMediaStateTimeListener = l;
	}

	@Override
	public void setOnHardDecodeErrorListener(OnHardDecodeErrorListner l) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getAudioSessionId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setOnCacheListener(OnCacheListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOnFirstPlayListener(OnFirstPlayLitener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public int setSourceType(int sourceType) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int setMachineInfomation(float ScreenResolution) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int setOneFingertouchInfomation(float begin_x, float begin_y,
			float end_x, float end_y) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int setTwoFingertouchInfomation(float begin_x0, float begin_y0,
			float begin_x1, float begin_y1, float end_x0, float end_y0,
			float end_x1, float end_y1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int setgravity_yroInfomation(float gravity_yro_x,
			float gravity_yro_y, float gravity_yro_z) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int setGravityInfomation(float gravity_x, float gravity_y,
			float gravity_z) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int setgravity_yroValidInfomation(boolean  gravityValid) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int setAngleInit() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int setTwoFingerZoom(float zoom) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setInitPosition(int msec) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVolume(int volume) {
        // changed for tvlive by zanxiaofei 2015-11-30
        if(mMediaPlayer != null) {
            mMediaPlayer.setVolume(volume);
        }
	}

    /**
     * 硬解码需要返回mediaplayer进行一些设置 changed for tvlive by zanxiaofei 2015-10-28
     * @return 播放器
     */
    @Override
    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }


    private OnNeedSetPlayParamsListener mOnNeedSetPlayParamsListener;
    /**
     * 硬解码需要设置播放高低水位
     * changed for tvlive by zanxiaofei 2015-10-30
     * @param l
     */
    @Override
    public void setOnNeedSetPlayParamsListener(OnNeedSetPlayParamsListener l) {
        mOnNeedSetPlayParamsListener = l;
    }
}
