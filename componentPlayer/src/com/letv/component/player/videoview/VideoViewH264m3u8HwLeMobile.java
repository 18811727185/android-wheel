package com.letv.component.player.videoview;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
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
import com.letv.component.player.hardwaredecode.CodecWrapper;
import com.letv.component.player.utils.LogTag;
import com.letv.component.player.utils.Tools;
import com.media.ffmpeg.FFMpegPlayer;
import com.media.ffmpeg.FFMpegPlayer.OnAdNumberListener;
import com.media.ffmpeg.FFMpegPlayer.OnBlockListener;
import com.media.ffmpeg.FFMpegPlayer.OnCacheListener;
import com.media.ffmpeg.FFMpegPlayer.OnFirstPlayLitener;
import com.media.ffmpeg.FFMpegPlayer.OnHardDecodeErrorListner;
import com.media.ffmpeg.FFMpegPlayer.OnSuccessListener;

import java.io.IOException;
import java.util.Map;

public class VideoViewH264m3u8HwLeMobile extends SurfaceView implements LetvMediaPlayerControl {
	private static final String TAG = "VideoViewH264LeMobileHw";
	
	public static final int STATE_ERROR = -1;
	public static final int STATE_IDLE = 0;
	public static final int STATE_PREPARING = 1;
	public static final int STATE_PREPARED = 2;
	public static final int STATE_PLAYING = 3;
	public static final int STATE_PAUSED = 4;
	public static final int STATE_PLAYBACK_COMPLETED = 5;
	public static final int STATE_STOPBACK = 6;
	public static final int STATE_ENFORCEMENT = 7;
	
	private static final int VIDEO_SIZE = 400;
	private static final int AUDIO_SIZE = 1600;
	private static final int PICTURE_SIZE = 90;
	private static final int STARTPIC_SIZE = 20; 
	
	private int mCurrentState = STATE_IDLE;
	private int mTargetState = STATE_IDLE;
	
	private final int FORWARD_TIME = 15000 ;
	private final int REWIND_TIME = 15000 ;

	private SurfaceHolder mSurfaceHolder = null;
	private FFMpegPlayer mMediaPlayer = null;
	private Context mContext;
	private int mVideoWidth;
	private int mVideoHeight;
	private int mSurfaceWidth;
	private int mSurfaceHeight;
	private MediaController mMediaController;
	private OnCompletionListener mOnCompletionListener;
	private OnPreparedListener mOnPreparedListener;
	private int mCurrentBufferPercentage;
	private OnSuccessListener mOnSuccessListener;
	private OnErrorListener mOnErrorListener;
	private OnBufferingUpdateListener mOnBufferingUpdateListener;
	private OnSeekCompleteListener mOnSeekCompleteListener;
	private OnVideoSizeChangedListener mOnVideoSizeChangedListener;
	private OnInfoListener mOnInfoListener;
	private OnVideoViewStateChangeListener mVideoViewStateChangeListener;
	private FFMpegPlayer.OnBlockListener mOnBlockListener;
	private FFMpegPlayer.OnCacheListener mOnCacheListener;
	private FFMpegPlayer.OnFirstPlayLitener mOnFirstPlayLitener;
	private OnMediaStateTimeListener mOnMediaStateTimeListener;
	private OnHardDecodeErrorListner mOnHardDecodeErrorListner;
	private int mSeekWhenPrepared; // recording the seek position while
	private boolean mCanPause;
	private boolean mCanSeekBack;
	private boolean mCanSeekForward;

	private String mLastUrl;
	private String mVersion;
	private Uri mUri;
	private int mDuration;
	private int mRatioType = -1;
	
	private PlayUrl mPlayerUrl;
	
	/**
	 * 记录消毁前的时间点
	 * */
	protected int lastSeekWhenDestoryed = 0 ;
	
	/**
	 * 强制等待，无法播放
	 * */
	private boolean enforcementWait = false ;
	
	/**
	 * 强制暂停
	 * */
	private boolean enforcementPause = false ;
	
	private int bufferTime = 0;
	

	public VideoViewH264m3u8HwLeMobile(Context context)
	{
		super(context);
		this.mContext = context;
		initVideoView();
	}
	

	public VideoViewH264m3u8HwLeMobile(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		initVideoView();
	}
	
	private void initVideoView()
	{
		mVideoWidth = 0;
		mVideoHeight = 0;
		getHolder().addCallback(mSHCallback);
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();
		mCurrentState = STATE_IDLE;
		StateChange(mCurrentState);
		mTargetState = STATE_IDLE;
	}
	

	public void setVideoPath(String videoPath) {
		mPlayerUrl = new PlayUrl();
		mPlayerUrl.setVid(-1);
		mPlayerUrl.setUrl(videoPath);
		mPlayerUrl.setStreamType(PlayUrl.StreamType.STREAM_TYPE_UNKNOWN);
		setVideoURI(Uri.parse(videoPath));
	}
	
	@Override
	public void setVideoPath(String path, Map<String, String> headers) {
		
	}
	
	public void setVideoURI(Uri uri) {
		String currentDate = Tools.getCurrentDate();
		LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDate+" VideoViewH264LeMobileHw(硬解m3u8)  setVideoURI(), url="
				+ ((uri != null) ? uri.toString() : "null"),true);
		if(mOnMediaStateTimeListener!=null){
			mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.INITPATH, currentDate);
		}
		mUri = uri;
		mSeekWhenPrepared = 0;
		openVideo();
		requestLayout();
		invalidate();
		LogTag.i(TAG, "uri=" + mUri.toString());
	}

	@Override
	public boolean canPause()
	{
		return mCanPause;
	}

	@Override
	public boolean canSeekBackward()
	{
		return mCanSeekBack;
	}

	@Override
	public boolean canSeekForward()
	{
		return mCanSeekForward;
	}

	@Override
	public int getBufferPercentage()
	{
		if (mMediaPlayer != null)
		{
			return mCurrentBufferPercentage;
		}

		return 0;
	}

	@Override
	public int getCurrentPosition()
	{
		if (isInPlaybackState())
		{
			return mMediaPlayer.getCurrentPosition();
		}

		return 0;
	}

	@Override
	public int getDuration()
	{
		if (isInPlaybackState())
		{
			if (mDuration > 0)
			{
				return mDuration;
			}
			mDuration = mMediaPlayer.getDuration();
			return mDuration;
		}

		mDuration = -1;
		return mDuration;
	}

	public String getSkipLastURL()
	{
		return mLastUrl;
	}

	public String getVersion()
	{
		return mVersion;
	}

	public int getViewWidth()
	{
		return getLayoutParams().width;
	}

	public int getViewHeight()
	{
		return getLayoutParams().height;
	}

	@Override
	public void start()
	{
//		HttpRequestManager.getInstance(mContext).requestCapability();
		if(!enforcementWait && !enforcementPause){
			if (isInPlaybackState()) {
//				setVisibility(View.VISIBLE);
				LogTag.i("硬解开始播放");
				mMediaPlayer.start();
				String currentDate = Tools.getCurrentDate();
				LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDate+" VideoViewH264LeMobileHw(硬解m3u8)  start()");
			}
		} else {
			StateChange(STATE_ENFORCEMENT);
		}
		mTargetState = STATE_PLAYING;
	}

	@Override
	public void pause()
	{
		if (isInPlaybackState())
		{
			if (mMediaPlayer.isPlaying())
			{
				LogTag.i("pause()");
				mMediaPlayer.pause();
				String currentDate = Tools.getCurrentDate();
				LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDate+" VideoViewH264LeMobileHw(硬解m3u8)  pause()");
				mCurrentState = STATE_PAUSED;
				StateChange(mCurrentState);
			}
		}

		mTargetState = STATE_PAUSED;
	}

	@Override
	public void seekTo(int mesc)
	{
		if (isInPlaybackState())
		{
			mMediaPlayer.seekTo(mesc);
			mSeekWhenPrepared = 0;
			lastSeekWhenDestoryed = 0;
		}
		else
		{
			mSeekWhenPrepared = mesc;
			lastSeekWhenDestoryed = 0;
		}

	}
	
	/**
	 * 固定快进
	 * */
	public void forward() {
		seekTo(getCurrentPosition() + FORWARD_TIME) ;
	}

	/**
	 * 固定快退
	 * */
	public void rewind() {
		seekTo(getCurrentPosition() - REWIND_TIME) ;
	}
	

	@Override
	public void stopPlayback() {
		stopPlayback(false);
	}

	public void stopPlayback(boolean isRemoveCallBack)
	{
		StateChange(STATE_STOPBACK);//为统计加入
		if (mMediaPlayer != null)
		{
			String currentDateStop = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDateStop+"VideoViewH264LeMobileHw stop()");
			if(mOnMediaStateTimeListener!=null){
				mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.STOP, currentDateStop);
			}
			try {
				mMediaPlayer.stop();
			} catch (Exception e) {
				LogTag.i(TAG, "hard decode native player has already null");
			}
			if (isRemoveCallBack)
			{
				getHolder().removeCallback(mSHCallback);
			}
			String currentDateRelease = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDateRelease+"VideoViewH264LeMobileHw release()");
			if(mOnMediaStateTimeListener!=null){
				mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.RELEASE, currentDateRelease);
			}
			try {
				mMediaPlayer.release();
			} catch (Exception e) {
				LogTag.i(TAG, "hard decode native player has already null");
			}
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			StateChange(mCurrentState);
			mTargetState = STATE_IDLE;
			setVisibility(INVISIBLE);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
		int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
		
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            switch (mRatioType) {
			case -1: //自适配
				if (mVideoWidth * height > width * mVideoHeight) {
					height = width * mVideoHeight / mVideoWidth;
				} else if (mVideoWidth * height < width * mVideoHeight) {
					width = height * mVideoWidth / mVideoHeight;
				}
				break;
				
			case 0: //全屏
//				float widthPixels = mSurfaceWidth;
//				float heightPixels = mSurfaceHeight;
//				float wRatio = width / widthPixels;
//				float hRatio = height / heightPixels;
//				float ratios = Math.max(wRatio, hRatio);
//				width = (int) Math.ceil(width / ratios);
//				height = (int) Math.ceil(height / ratios);
				break;
				
			case 1: //4:3
				if (4 * height > width * 3) {
					height = width * 3 / 4;
				} else if (4 * height < width * 3) {
					width = height * 4 / 3;
				}
				break;
				
			case 2: //16:9
				if (16 * height > width * 9) {
					height = width * 9 / 16;
				} else if (16 * height < width * 9) {
					width = height * 16 / 9;
				}
				break;

			}
        }
		setMeasuredDimension(width, height);
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
			/*
			 * Parent says we can be as big as we want. Just don't be larger
			 * than max size imposed on ourselves.
			 */
			result = desiredSize;
			break;

		case MeasureSpec.AT_MOST:
			/*
			 * Parent says we can be as big as we want, up to specSize. Don't be
			 * larger than specSize, and don't be larger than the max size
			 * imposed on ourselves.
			 */
			result = Math.min(desiredSize, specSize);
			break;

		case MeasureSpec.EXACTLY:
			// No choice. Do what we are told.
			result = specSize;
			break;
		}
		return result;
	}

	SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback()
	{
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
		{
			mSurfaceWidth = w;
			mSurfaceHeight = h;
			boolean isValidState = (mTargetState == STATE_PLAYING);
			boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
			if (mMediaPlayer != null && isValidState && hasValidSize)
			{
				if (mSeekWhenPrepared != 0)
				{
					seekTo(mSeekWhenPrepared);
				}
				start();
				if (mMediaController != null) {
					mMediaController.show();
				}
			}
		}

		public void surfaceCreated(SurfaceHolder holder)
		{
			if (mSurfaceHolder == null)
			{
				mSurfaceHolder = holder;
				openVideo();
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder)
		{
			mSurfaceHolder = null;
			if (mMediaController != null) {
				mMediaController.hide();
			}
			lastSeekWhenDestoryed = getCurrentPosition() ;
			release(true);
		}
	};

	public boolean isInPlaybackState()
	{
		return (mMediaPlayer != null && mCurrentState != STATE_ERROR && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
	}

	private void openVideo()
	{
		if (mUri == null || mSurfaceHolder == null)
		{
			setVisibility(VISIBLE);
			return;
		}
		release(false);
		try
		{
			String currentDate = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDate+" VideoViewH264LeMobileHw(硬解m3u8)  创建FFMpegPlayer对象");
			if(mOnMediaStateTimeListener!=null){
				mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.CREATE, currentDate);
			}
			mMediaPlayer = new FFMpegPlayer(mContext);
			mMediaPlayer.setHardwareDecode(FFMpegPlayer.HARDWARE_DECODE);
			mMediaPlayer.setHwCapbility(CodecWrapper.getProfile(), CodecWrapper.getAVCLevel());
			mMediaPlayer.setOnPreparedListener(mPreparedListener);
			mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
			mDuration = -1;
			mMediaPlayer.setOnCompletionListener(mCompletionListener);
			mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
			mMediaPlayer.setOnSuccessListener(mSuccessListener);
			mMediaPlayer.setOnErrorListener(mErrorListener);
			mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
			mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
			mMediaPlayer.setOnInfoListener(mInfoListener);
			mMediaPlayer.setOnBlockListener(mBlockListener);
			mMediaPlayer.setOnCacheListener(mCacheListener); // lhq 2015-06-15
			mMediaPlayer.setOnFirstPlayListener(mFirstPlayLitener);
			mMediaPlayer.setOnDisplayListener(mDisplayListener);
			mMediaPlayer.setOnHardDecoddErrorListener(mHardDecodeErrorListner);
//			mMediaPlayer.setCacheSize(VIDEO_SIZE, AUDIO_SIZE, PICTURE_SIZE, STARTPIC_SIZE);
			mCurrentBufferPercentage = 0;
			//设置播放高低水位
			//changed for tvlive by zanxiaofei 2015-10-30
			if(mOnNeedSetPlayParamsListener != null){
				mOnNeedSetPlayParamsListener.onNeedSet();
			}
			//end
			mMediaPlayer.setDataSource(mContext, mUri);
			mMediaPlayer.setDisplay(mSurfaceHolder);
//			mMediaPlayer.setDecoderSurface(mSurfaceHolder.getSurface());
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setScreenOnWhilePlaying(true);
			mMediaPlayer.prepareAsync();
			mCurrentState = STATE_PREPARING;
			attachMediaController();
		}
		catch (IllegalStateException ex) //多个播放器创建会在setAudioStreamType()方法出现异常
		{
			LogTag.i("Unable to open content: " + mUri + " ,IllegalArgumentException=" + ex);
			mCurrentState = STATE_ERROR;
			StateChange(mCurrentState);
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		}
		catch (IllegalArgumentException ex)
		{
			mCurrentState = STATE_ERROR;
			StateChange(mCurrentState);
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		}
		catch (IOException e) {
			mCurrentState = STATE_ERROR;
			StateChange(mCurrentState);
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
		}
	}
	
	@Override
	public void setMediaController(MediaController controller) {
		if (mMediaController != null) {
			mMediaController.hide();
		}
		mMediaController = controller;
		attachMediaController();
	}

	private void attachMediaController() {
		if (mMediaPlayer != null && mMediaController != null) {
			mMediaController.setMediaPlayer(this);
			View anchorView = this.getParent() instanceof View ? (View) this.getParent() : this;
			mMediaController.setAnchorView(anchorView);
			mMediaController.setEnabled(isInPlaybackState());
		}
	}

	// 设置播放器控件的大小
	private void setVideoViewScale(int width, int height)
	{
		LayoutParams lp = (LayoutParams) this.getLayoutParams();
		lp.height = height;
		lp.width = width;
		setLayoutParams(lp);
	}

	private void release(boolean cleartargetstate)
	{
		if (mMediaPlayer != null)
		{
			String currentDateStop = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDateStop+"VideoViewH264LeMobileHw stop()");
			if(mOnMediaStateTimeListener!=null){
				mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.STOP, currentDateStop);
			}
			mMediaPlayer.stop();
			String currentDateRelease = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDateRelease+"VideoViewH264LeMobileHw release()");
			if(mOnMediaStateTimeListener!=null){
				mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.RELEASE, currentDateRelease);
			}
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			StateChange(mCurrentState);
			if (cleartargetstate)
			{
				mTargetState = STATE_IDLE;
			}
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (isInPlaybackState() && mMediaController != null) {
			toggleMediaControlsVisiblity();
		}
		return false;
	}

	@Override
	public boolean onTrackballEvent(MotionEvent ev) {
		if (isInPlaybackState() && mMediaController != null) {
			toggleMediaControlsVisiblity();
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK && keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN && keyCode != KeyEvent.KEYCODE_MENU
				&& keyCode != KeyEvent.KEYCODE_CALL && keyCode != KeyEvent.KEYCODE_ENDCALL;
		if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null)
		{
			if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
			{
				if (mMediaPlayer.isPlaying())
				{
					pause();
					mMediaController.show();
				}
				else
				{
					start();
					mMediaController.hide();

				}
				return true;
			}
			else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP && mMediaPlayer.isPlaying())
			{
				pause();
				mMediaController.show();
			}
			else
			{
				toggleMediaControlsVisiblity();
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	private void toggleMediaControlsVisiblity()
	{
		if (mMediaController.isShowing()) {
			mMediaController.hide();
		} else {
			mMediaController.show();
		}
	}

	@Override
	public boolean isPlaying()
	{
		return isInPlaybackState() && mMediaPlayer.isPlaying();
	}
	
	public boolean isPaused() {
		return  mCurrentState == STATE_PAUSED;
	}

	MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener()
	{
		public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();
			if (mVideoWidth != 0 && mVideoHeight != 0) {
				getHolder().setFixedSize(mVideoWidth, mVideoHeight);
			}
			if(mOnVideoSizeChangedListener != null) {
				mOnVideoSizeChangedListener.onVideoSizeChanged(mp, mVideoWidth, mVideoHeight);
			}
		}
	};

	MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener()
	{
		public void onPrepared(MediaPlayer mp)
		{
			LogTag.i("onPrepared()");
			String currentDate = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDate+" VideoViewH264LeMobileHw(硬解m3u8)  onPrepared()");
			if(mOnMediaStateTimeListener!=null){
				mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.PREPARED, currentDate);
			}
			mCurrentState = STATE_PREPARED;
			StateChange(mCurrentState);

			mCanPause = mCanSeekBack = mCanSeekForward = true;

			if (mOnPreparedListener != null)
			{
				mOnPreparedListener.onPrepared(mMediaPlayer);
			}
			mLastUrl = ((FFMpegPlayer) mp).getLastUrl();
			mVersion = ((FFMpegPlayer) mp).getVersion();
			
			if (mMediaController != null) {
				mMediaController.setEnabled(true);
			}

			int seekToPosition = mSeekWhenPrepared; // mSeekWhenPrepared may be
			if (seekToPosition != 0)
			{
				seekTo(seekToPosition);
			}
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();
			if (mVideoWidth != 0 && mVideoHeight != 0)
			{
				if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight)
				{
					if (mTargetState == STATE_PLAYING)
					{
						start();
						if (mMediaController != null) {
							mMediaController.show();
						}
					} else if (!isPlaying() && (seekToPosition != 0 || getCurrentPosition() > 0)) {
						if (mMediaController != null) {
							// Show the media controls when we're paused into a
							// video and make 'em stick.
							mMediaController.show(0);
						}
					}
				}
				else
				{
					getHolder().setFixedSize(mVideoWidth, mVideoHeight);
				}

			}
			else
			{

				if (mTargetState == STATE_PLAYING)
				{
					start();
				}
			}

		}
	};

	private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener()
	{
		public void onCompletion(MediaPlayer mp)
		{
			mCurrentState = STATE_PLAYBACK_COMPLETED;
			StateChange(mCurrentState);
			mTargetState = STATE_PLAYBACK_COMPLETED;
			mCurrentState = STATE_STOPBACK;
			StateChange(mCurrentState);//未统计加入
			if (mMediaController != null) {
				mMediaController.hide();
			}
			if (mOnCompletionListener != null)
			{
				mOnCompletionListener.onCompletion(mMediaPlayer);
			}
			/**
			 * 播放完成，停止并释放资源
			 */
			VideoViewH264m3u8HwLeMobile.this.pause();
			VideoViewH264m3u8HwLeMobile.this.release(true);
		}
	};
	
	private FFMpegPlayer.OnSuccessListener mSuccessListener = new OnSuccessListener() {
		
		@Override
		public void onSuccess() {
			if (mOnSuccessListener != null) {
				mOnSuccessListener.onSuccess();
			}
			LogTag.i("硬解成功");
//			HttpRequestManager.getInstance(mContext).hardDecodeReport(mPlayerUrl.mVid, mUri.toString(), WhiteBlackConst.REPORT_SUCCESS, WhiteBlackConst.REPORT_SUCCESS_ERRORCODE, mPlayerUrl.mStreamType, WhiteBlackConst.REPORT_IS_SMOOTH);//默认流畅
		}
	};
	
	private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener()
	{
		public boolean onError(MediaPlayer mp, int framework_err, int impl_err)
		{
			mCurrentState = STATE_ERROR;
			StateChange(mCurrentState);
			mTargetState = STATE_ERROR;
			if (mMediaController != null) {
				mMediaController.hide();
			}
			/* If an error handler has been supplied, use it and finish. */
			if (mOnErrorListener != null)
			{
				mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err);
				
			}
			
//			PreferenceUtil.setErrorCode(mContext, "VideoViewH264LeMobileHw error, framework_err=" + framework_err + ", impl_err=" + impl_err);
			
			String currentDate = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDate+"VideoViewH264LeMobileHw(硬解m3u8) 播放出错error, framework_err=" + framework_err + ", impl_err=" + impl_err);
			if(mOnMediaStateTimeListener!=null){
				mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.ERROR, currentDate);
			}
			LogTag.i("硬解失败");
			LogTag.i("framework_err:"+framework_err);
			return true;
		}
	};

	private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener()
	{

		public void onBufferingUpdate(MediaPlayer mp, int percent)
		{
			mCurrentBufferPercentage = percent;
			if (mOnBufferingUpdateListener != null) {
				mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
			}
		}
	};
	private MediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener()
	{
		public void onSeekComplete(MediaPlayer mp)
		{
			if (mOnSeekCompleteListener != null)
			{
				mOnSeekCompleteListener.onSeekComplete(mMediaPlayer);
			}
			//	resume();
		}
	};
	
	private MediaPlayer.OnInfoListener mInfoListener = new OnInfoListener() {
		
		@Override
		public boolean onInfo(MediaPlayer mp, int what, int extra) {
			
			LogTag.i("onInfo+++"+what+"extra+++"+extra);
			if (mOnInfoListener != null) {
				mOnInfoListener.onInfo(mp, what, extra);
			}
			return false;
		}
	};
	
	private FFMpegPlayer.OnBlockListener mBlockListener = new FFMpegPlayer.OnBlockListener() {
		
		@Override
		public void onBlock(FFMpegPlayer mediaPlayer, int blockinfo) {
			if (mOnBlockListener != null) {
				mOnBlockListener.onBlock(mediaPlayer, blockinfo);
			}
			if(blockinfo == FFMpegPlayer.MEDIA_BLOCK_START) {
				String currentDate = Tools.getCurrentDate();
				LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDate+" VideoViewH264LeMobileHw(硬解m3u8)  出现卡顿");
			} else if(blockinfo == FFMpegPlayer.MEDIA_BLOCK_END) {
				String currentDate = Tools.getCurrentDate();
				LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDate+" VideoViewH264LeMobileHw(硬解m3u8)  结束卡顿");
			}
		}
	};

	/***
	 * lhq 2015-06-14
	 */
	private FFMpegPlayer.OnCacheListener mCacheListener = new OnCacheListener() {
		
		@Override
		public void onCache(FFMpegPlayer mediaPlayer, int arg, int percent,
				long cacherate) {
			// TODO Auto-generated method stub
			if(mOnCacheListener != null)
			{
				mOnCacheListener.onCache(mediaPlayer, arg, percent, cacherate);
			}
			
			if(arg == FFMpegPlayer.MEDIA_CACHE_START)
			{
				String currentDate = Tools.getCurrentDate();
				LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDate+" VideoViewH264m3u8Hw(软解m3u8)  开始缓存 ");
			}
			
			if(arg == FFMpegPlayer.MEDIA_CACHE_END)
			{
				String currentDate = Tools.getCurrentDate();
				LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDate+" VideoViewH264m3u8Hw(软解m3u8)  缓存结束 ");
			}
			
			
		}
	};
	
	private FFMpegPlayer.OnFirstPlayLitener mFirstPlayLitener = new OnFirstPlayLitener() {
		
		@Override
		public void onFirstPlay(FFMpegPlayer mediaPlayer) {
			// TODO Auto-generated method stub
			String currentDate = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDate+"第一次播放 ");
			if(mOnFirstPlayLitener != null)
			{
				mOnFirstPlayLitener.onFirstPlay(mediaPlayer);
			}

		}
	};
	
	 private FFMpegPlayer.OnDisplayListener mDisplayListener = new FFMpegPlayer.OnDisplayListener() {

			@Override
			public void onDisplay(FFMpegPlayer mediaPlayer) {
				LogTag.i("硬解onDisplay()");
				String currentDate = Tools.getCurrentDate();
				LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDate+" VideoViewH264LeMobileHw(硬解m3u8)  第一帧画面时间  onDisplay()");
				if(mOnMediaStateTimeListener!=null){
					mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.DIAPLAY, currentDate);
				}
				mCurrentState = STATE_PLAYING;
				StateChange(mCurrentState);
			}
		};
		
	private FFMpegPlayer.OnHardDecodeErrorListner mHardDecodeErrorListner = new FFMpegPlayer.OnHardDecodeErrorListner() {

		@Override
		public void onError(FFMpegPlayer mediaPlayer, int arg1, int arg2) {
			LogTag.i("OnHardDecodeErrorListner.onError(): framework_err=" + arg1 + ", impl_err=" + arg2);
			String currentDate = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDate+"VideoViewH264LeMobileHw(硬解m3u8)  OnHardDecodeErrorListner.onError(): framework_err=" + arg1 + ", impl_err=" + arg2);
			if(mOnMediaStateTimeListener!=null){
				mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.HARD_ERROR, currentDate);
			}
			if(mOnHardDecodeErrorListner != null) {
				mOnHardDecodeErrorListner.onError(mediaPlayer, arg1, arg2);
			}
			if (mOnErrorListener != null){
				mOnErrorListener.onError(mediaPlayer, arg1, arg2);
			}
//			HttpRequestManager.getInstance(mContext).hardDecodeReport(mPlayerUrl.mVid, mUri.toString(), WhiteBlackConst.REPORT_FAIL, arg1, mPlayerUrl.mStreamType, WhiteBlackConst.REPORT_IS_SMOOTH);//默认流畅
		}
	};
		
	public void setOnSeekCompleteListener(OnSeekCompleteListener l) {
		this.mOnSeekCompleteListener = l;
	}
	
	public int getLastSeekWhenDestoryed() {
		return lastSeekWhenDestoryed;
	}

	public int getAudioSessionId() {
		return 0;
	}

	public boolean isEnforcementWait() {
		return enforcementWait;
	}

	public void setEnforcementWait(boolean enforcementWait) {
		this.enforcementWait = enforcementWait;
	}

	public boolean isEnforcementPause() {
		return enforcementPause;
	}

	public void setEnforcementPause(boolean enforcementPause) {
		this.enforcementPause = enforcementPause;
	}

	@Override
	public View getView() {
		return this;
	}
	
	private void StateChange(int mCurrentState){
		if(mVideoViewStateChangeListener != null) {
			mVideoViewStateChangeListener.onChange(mCurrentState);
		}
	}
	
	@Override
	public void setVideoViewStateChangeListener(OnVideoViewStateChangeListener listener) {
		this.mVideoViewStateChangeListener = listener;
	}

	@Override
	public void setOnInfoListener(OnInfoListener l) {
		this.mOnInfoListener = l;
	}

	@Override
	public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener l) {
		this.mOnVideoSizeChangedListener = l;
	}

	public void setOnBufferingUpdateListener(OnBufferingUpdateListener l)
	{
		mOnBufferingUpdateListener = l;
	}

	public void setOnPreparedListener(OnPreparedListener l)
	{
		this.mOnPreparedListener = l;
	}

	public void setOnCompletionListener(OnCompletionListener l)
	{
		this.mOnCompletionListener = l;
	}

	public void setOnErrorListener(OnErrorListener l)
	{
		this.mOnErrorListener = l;
	}
	
	public void setOnSuccessListener(OnSuccessListener l)
	{
		this.mOnSuccessListener = l;
	}

	@Override
	public void setVideoPlayUrl(PlayUrl url) {
		// TODO Auto-generated method stub
		mPlayerUrl = url;
		setVideoURI(Uri.parse(mPlayerUrl.getUrl()));
	}


	@Override
	public void setOnAdNumberListener(OnAdNumberListener l) {
	}


	@Override
	public void setCacheSize(int video_size, int audio_size, int picture_size, int startpic_size) {
//		mMediaPlayer.setCacheSize(video_size, audio_size, picture_size, startpic_size);
	}
	@Override
	public void setOnBlockListener(OnBlockListener l) {
		this.mOnBlockListener = l;
	}
	
	@Override
	public void setOnCacheListener(OnCacheListener l) {
		// TODO Auto-generated method stub
		this.mOnCacheListener = l;
	}
	
	@Override
	public void setOnMediaStateTimeListener(OnMediaStateTimeListener l) {
		this.mOnMediaStateTimeListener = l;
	}
	
	@Override
	public void setOnHardDecodeErrorListener(OnHardDecodeErrorListner l) {
		mOnHardDecodeErrorListner = l;
	}


	@Override
	public void setOnFirstPlayListener(OnFirstPlayLitener l) {
		// TODO Auto-generated method stub
		this.mOnFirstPlayLitener = l;
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
	public int setgravity_yroValidInfomation(boolean gravityValid) {
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
		// TODO Auto-generated method stub
		
	}

	/**
	 * 硬解码需要返回mediaplayer进行一些设置
	 * changed for tvlive by zanxiaofei 2015-10-28
	 * @return
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
