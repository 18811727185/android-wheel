package com.letv.component.player.videoview;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
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
import com.media.ffmpeg.FFMpegPlayer;
import com.media.ffmpeg.FFMpegPlayer.OnAdNumberListener;
import com.media.ffmpeg.FFMpegPlayer.OnBlockListener;
import com.media.ffmpeg.FFMpegPlayer.OnCacheListener;
import com.media.ffmpeg.FFMpegPlayer.OnFirstPlayLitener;
import com.media.ffmpeg.FFMpegPlayer.OnHardDecodeErrorListner;
import com.media.ffmpeg.FFMpegPlayer.OnInitGLListener;
import com.media.ffmpeg.FFMpegPlayer.OnSuccessListener;

import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
public class VideoViewH264m3u8 extends GLSurfaceView implements
		LetvMediaPlayerControl {
	private static final String TAG = "VideoViewH264m3u8";

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
	private static final int STARTPIC_SIZE = 3;

	public static final int SURFACE_CHANGED_INIT = 1;

	public static final int SURFACE_CHANGED_ING = 2;

	private static final int RELEASE_GL_STATE_INIT = 0;
	private static final int RELEASE_GL_STATE_ING = 1;
	private  int mReleaseGLState = RELEASE_GL_STATE_INIT;

	private int mCurrentState = STATE_IDLE;
	private int mTargetState = STATE_IDLE;

	private final int FORWARD_TIME = 15000;
	private final int REWIND_TIME = 15000;
	private final int RELEASE_BLOCK_TIME = 1000;

	public static final int MEDIA_INFO_BUFFERING_START = 701;// 缓冲开始
	public static final int MEDIA_INFO_BUFFERING_END = 702;// 缓冲结束
	public static final int MEDIA_INFO_BUFFERING_PERCENT = 704;// 缓冲进度

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
	private FFMpegPlayer.OnAdNumberListener mOnAdNumberListener;
	private FFMpegPlayer.OnBlockListener mOnBlockListener;
	private FFMpegPlayer.OnCacheListener mOnCacheListener;
	private FFMpegPlayer.OnFirstPlayLitener mOnFirstPlayLitener;
	private OnVideoSizeChangedListener mOnVideoSizeChangedListener;
	private OnMediaStateTimeListener mOnMediaStateTimeListener;
	private OnInfoListener mOnInfoListener;
	private OnVideoViewStateChangeListener mVideoViewStateChangeListener;

	private int mSeekWhenPrepared; // recording the seek position while
	private boolean mCanPause;
	private boolean mCanSeekBack;
	private boolean mCanSeekForward;

	private String mLastUrl;
	private String mVersion;
	private Uri mUri;
	private int mDuration;
	private int mRatioType = -1;
	private int mVolumevalue = 1;//xuehui add on 2015-10-14 for multi object to control player volume
	private int mInitPostion = 0;//xuehui add on 2015-09-28 for m3u8 init,tiao guo pian tou
	private int mSourceType = 0;//xuehui 通知component player 当前要播放的片源的类型,0是普通片源，1是全景片源

	private boolean isSupportScale = true;

	private Handler mReleaseMediaPlayerHandler = new Handler();;

	private PlayUrl mPlayerUrl;

	/**
	 * 记录消毁前的时间点
	 * */
	protected int lastSeekWhenDestoryed = 0;

	/**
	 * 强制等待，无法播放
	 * */
	private boolean enforcementWait = false;

	/**
	 * 强制暂停
	 * */
	private boolean enforcementPause = false;

	private int bufferTime = 0;

	/** lhq 2015-07-28 **/
	private MyRenderer mMyRenderer;

	//下面这部分代码应该定义在客户端的代码中，
	private float mPreviousY=1000000.0f;// 单指操作，存储上次move触发产生的时候的触控位置Y坐标。初始化为一个超大值来界定是一次连续的move动作的开始和结束
	private float mPreviousX=1000000.0f;// 
	private float mPreviousY0=1000000.0f;//
	private float mPreviousX0=1000000.0f;//
	private float mPreviousY1=1000000.0f;//
	private float mPreviousX1=1000000.0f;//
	Handler testhandler = new Handler();
	private int mode = 1;// 1: 单指头操作 2:双指操作
	//----------------------------------------
	private SensorManager mSensorManager;
	private Sensor orientationSensorG=null;
	private SensorEventListener mySensorListener=null;
	private SensorEventListener mySensorListenerG=null;
	/**
	 * @param context
	 */

	public VideoViewH264m3u8(Context context) {
		super(context);
		this.mContext = context;
		initVideoView();
	}

	public VideoViewH264m3u8(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		initVideoView();
	}
	private void initVideoView() {
		mVideoWidth = 0;
		mVideoHeight = 0;
		// setEGLConfigChooser(5, 6, 5, 0, 16, 0); // 解决摩托xt910切集显示黑色遮罩，黑屏有声音
		setEGLContextClientVersion(2);
		mMyRenderer = new MyRenderer();
		setRenderer(mMyRenderer);
		this.onPause();
		getHolder().addCallback(mSHCallback);
		// getHolder().setFormat(PixelFormat.TRANSLUCENT);
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
		mPlayerUrl.setStreamType(PlayUrl.StreamType.STREAM_TYPE_UNKNOWN);
		mPlayerUrl.setUrl(videoPath);
		setVideoURI(Uri.parse(videoPath));
	}

	@Override
	public void setVideoPath(String path, Map<String, String> headers) {

	}

	public void setVideoURI(Uri uri) {
		// uri = Uri.parse("http://123.126.32.30/ts/m3u8/mp_test/720p.m3u8");
		//uri = Uri.parse("/sdcard/manhattan.mp4");
		// uri =
		// Uri.parse("http://60.210.17.194/videos/v0/20150714/b4/58/0cb8f4ef9e3729cd2881e89958f9d598.f4v?key=00935dd8daba3f5be1e4d0498e6070d5b&src=iqiyi.com&qyid=860312029335443&qypid=33&uuid=7b7e21fd-55a4c92a-57");
		String currentDate = Tools.getCurrentDate();
		LetvMediaPlayerManager.getInstance().writePlayLog(
				"系统当前时间:  " + currentDate
						+ " VideoViewH264m3u8(软解m3u8)  setVideoURI(), url="
						+ ((uri != null) ? uri.toString() : "null"), true);
		if (mOnMediaStateTimeListener != null) {
			mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.INITPATH,
					currentDate);
		}
		mTargetState = STATE_IDLE;
		mUri = uri;
		mSeekWhenPrepared = 0;
		openVideo();
		requestLayout();
		invalidate();
		LogTag.i("setVideoURI(), url="
				+ ((uri != null) ? uri.toString() : "null"));
	}

	@Override
	public boolean canPause() {
		return mCanPause;
	}

	@Override
	public boolean canSeekBackward() {
		return mCanSeekBack;
	}

	@Override
	public boolean canSeekForward() {
		return mCanSeekForward;
	}

	@Override
	public int getBufferPercentage() {
		if (mMediaPlayer != null) {
			return mCurrentBufferPercentage;
		}

		return 0;
	}

	@Override
	public int getCurrentPosition() {
		if (isInPlaybackState()) {
			return mMediaPlayer.getCurrentPosition();
		}

		return 0;
	}

	@Override
	public int getDuration() {
		if (isInPlaybackState()) {
			if (mDuration > 0) {
				return mDuration;
			}
			mDuration = mMediaPlayer.getDuration();
			LogTag.i("getDuration()=" + mDuration);
			return mDuration;
		}

		mDuration = -1;
		LogTag.i("getDuration()=" + mDuration);
		return mDuration;
	}

	public String getSkipLastURL() {
		return mLastUrl;
	}

	public String getVersion() {
		return mVersion;
	}

	public int getViewWidth() {
		return getLayoutParams().width;
	}

	public int getViewHeight() {
		return getLayoutParams().height;
	}

	@Override
	public void start() {
		HttpRequestManager.getInstance(mContext).requestCapability();
		if (!enforcementWait && !enforcementPause) {
			if (isInPlaybackState()) {
				// setVisibility(View.VISIBLE);
				LogTag.i("软解开始播放");
				mMediaPlayer.start();
				String currentDate = Tools.getCurrentDate();
				LetvMediaPlayerManager.getInstance().writePlayLog(
						"系统当前时间:  " + currentDate
								+ " VideoViewH264m3u8(软解m3u8)  start()");
			}
		} else {
			StateChange(STATE_ENFORCEMENT);
		}
		mTargetState = STATE_PLAYING;
	}

	@Override
	public void pause() {
		if (isInPlaybackState()) {
			if (mMediaPlayer.isPlaying()) {
				LogTag.i("pause()");
				mMediaPlayer.pause();
				String currentDate = Tools.getCurrentDate();
				LetvMediaPlayerManager.getInstance().writePlayLog(
						"系统当前时间:  " + currentDate
								+ " VideoViewH264m3u8(软解m3u8)  pause()");
				// this.onPause(); //解决暂停播放横竖屏切换黑屏的问题
				mCurrentState = STATE_PAUSED;
				StateChange(mCurrentState);
			}
		}

		mTargetState = STATE_PAUSED;
	}

	@Override
	public void seekTo(int mesc) {
		if (isInPlaybackState()) {
			mMediaPlayer.seekTo(mesc);
			mSeekWhenPrepared = 0;
			lastSeekWhenDestoryed = 0;
		} else {
			mSeekWhenPrepared = mesc;
			lastSeekWhenDestoryed = 0;
		}

	}

	/**
	 * 固定快进
	 * */
	public void forward() {
		seekTo(getCurrentPosition() + FORWARD_TIME);
	}

	/**
	 * 固定快退
	 * */
	public void rewind() {
		seekTo(getCurrentPosition() - REWIND_TIME);
	}

	@Override
	public void stopPlayback() {
		LogTag.i(TAG,"stopPlayback()");
		if(mMediaPlayer!=null){
			mMediaPlayer.stop();
		}
		
//		stopPlayback(false);
//
		String currentDate = Tools.getCurrentDate();
		LetvMediaPlayerManager.getInstance().writePlayLog(
				"系统当前时间:  " + currentDate
						+ " OpenGL   stopPlayback releaseGL()");
		//type 1 和type 2都是干嘛的? false是指不清空当前的状态??
		releaseGL(false, 2);
	}

	public void stopPlayback(boolean isRemoveCallBack) {
		LogTag.i(TAG,"stopPlayback(boolean isRemoveCallBack)" + (isRemoveCallBack?"true":"false"));
		StateChange(STATE_STOPBACK);// 为统计加入
		if (mMediaPlayer != null) {
			LogTag.i(TAG,"stopPlayback(boolean isRemoveCallBack)" + (isRemoveCallBack?"true":"false") + "(mMediaPlayer != null)");
			String currentDateStop = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog(
					"系统当前时间:  " + currentDateStop + "VideoViewH264m3u8 stop()");
			if (mOnMediaStateTimeListener != null) {
				mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.STOP,
						currentDateStop);
			}
			try {
				mMediaPlayer.stop();
			} catch (Exception e) {
				LogTag.i(TAG, "native player has already null");
			}

			if (isRemoveCallBack) {
				getHolder().removeCallback(mSHCallback);
			}
			String currentDateRelease = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog(
					"系统当前时间:  " + currentDateRelease
							+ "VideoViewH264m3u8 release()");
			if (mOnMediaStateTimeListener != null) {
				mOnMediaStateTimeListener.onMediaStateTime(
						MeidaStateType.RELEASE, currentDateRelease);
			}
			try {
				mMediaPlayer.release();
			} catch (Exception e) {
				LogTag.i(TAG, "native player has already null");
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

		if (isSupportScale && mVideoWidth > 0 && mVideoHeight > 0) {
			switch (mRatioType) {

			case 0: // 全屏
				// float widthPixels = mSurfaceWidth;
				// float heightPixels = mSurfaceHeight;
				// float wRatio = width / widthPixels;
				// float hRatio = height / heightPixels;
				// float ratios = Math.max(wRatio, hRatio);
				// width = (int) Math.ceil(width / ratios);
				// height = (int) Math.ceil(height / ratios);
				break;

			case 1: // 4:3
				if (4 * height > width * 3) {
					height = width * 3 / 4;
				} else if (4 * height < width * 3) {
					width = height * 4 / 3;
				}
				break;

			case 2: // 16:9
				if (16 * height > width * 9) {
					height = width * 9 / 16;
				} else if (16 * height < width * 9) {
					width = height * 16 / 9;
				}
				break;

			case -1: // 自适配,执行默认逻辑
			default:
				if (mVideoWidth * height > width * mVideoHeight) {
					height = width * mVideoHeight / mVideoWidth;
				} else if (mVideoWidth * height < width * mVideoHeight) {
					width = height * mVideoWidth / mVideoHeight;
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

	SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			LogTag.i(TAG,"mSHCallback:surfaceChanged(), w=" + w + ", h=" + h);
			mSurfaceWidth = w;
			mSurfaceHeight = h;
			boolean isValidState = (mTargetState == STATE_PLAYING);
			boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
			if (mMediaPlayer != null && isValidState && hasValidSize) {
				if (mSeekWhenPrepared != 0) {
					seekTo(mSeekWhenPrepared);
				}
				start();
				if (mMediaController != null) {
					mMediaController.show();
				}
			}
		}

		public void surfaceCreated(SurfaceHolder holder) {
			LogTag.i(TAG,"mSHCallback:surfaceCreated()");
			if (mSurfaceHolder == null) {
				mSurfaceHolder = holder;
				openVideo();
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			LogTag.i(TAG,"mSHCallback:surfaceDestroyed()");
			mSurfaceHolder = null;
			if (mMediaController != null) {
				mMediaController.hide();
			}
			lastSeekWhenDestoryed = getCurrentPosition();
			// release(false);
			
			//在completed 的过程中,不重复释放openGL 资源
//			if((mCurrentState!=STATE_PLAYBACK_COMPLETED)&&(mMediaPlayer!=null)){
//				String currentDate = Tools.getCurrentDate();
//				LetvMediaPlayerManager.getInstance().writePlayLog(
//						"系统当前时间:  " + currentDate
//								+ " OpenGL   surfaceDestroyed releaseGL()");
//	
//				releaseGL(false, 1);
//			}else{
//				String currentDate = Tools.getCurrentDate();
//				LetvMediaPlayerManager.getInstance().writePlayLog(
//						"系统当前时间:  " + currentDate
//								+ " OpenGL   surfaceDestroyed NOT releaseGL()");
//			}
			String currentDate = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog(
					"系统当前时间:  " + currentDate
							+ " OpenGL   surfaceDestroyed releaseGL()");
			releaseGL(false, 1);
		}
	};

	public boolean isInPlaybackState() {
		return (mMediaPlayer != null && mCurrentState != STATE_ERROR
				&& mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
	}

	private void openVideo() {
		if (mUri == null || mSurfaceHolder == null) {
			setVisibility(VISIBLE);
			return;
		}
		
		String currentDate_openVideo = Tools.getCurrentDate();
		LetvMediaPlayerManager.getInstance().writePlayLog(
				"系统当前时间:  " + currentDate_openVideo
						+ " OpenGL   openVideo releaseGL()");
		releaseGL(false, 1);
		
		try {
			String currentDate = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog(
					"系统当前时间:  " + currentDate
							+ " VideoViewH264m3u8(软解m3u8)  创建FFMpegPlayer对象");
			if (mOnMediaStateTimeListener != null) {
				mOnMediaStateTimeListener.onMediaStateTime(
						MeidaStateType.CREATE, currentDate);
			}
			mMediaPlayer = new FFMpegPlayer();
			mMediaPlayer.setHardwareDecode(FFMpegPlayer.SOFTWARE_DECODE);
			onResume();
			mMediaPlayer.setOnPreparedListener(mPreparedListener);
			mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
			mDuration = -1;
			mMediaPlayer.setOnCompletionListener(mCompletionListener);
			mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
			mMediaPlayer.setOnSuccessListener(mSuccessListener);
			mMediaPlayer.setOnErrorListener(mErrorListener);
			mMediaPlayer.setOnCacheListener(mCacheListener); // lhq 2015-06-15
			mMediaPlayer.setOnFirstPlayListener(mFirstPlayLitener);
			mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
			mMediaPlayer.setOnAdNumberListener(mAdNumberListener);
			mMediaPlayer.setOnBlockListener(mBlockListener);
			mMediaPlayer.setOnDisplayListener(mDisplayListener);
			mMediaPlayer.setOnInitListener(mInitGLListener); // 2015-06-30
			mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
			mMediaPlayer.setOnInfoListener(mInfoListener);
			mMediaPlayer.setRenderControler(mGLRenderControler);
			//设置cachesize
			//changed for tvlive by zanxiaofei 2015-10-30
			mMediaPlayer.setCacheSize(VIDEO_SIZE, AUDIO_SIZE, PICTURE_SIZE,
			 STARTPIC_SIZE);
			//end
			mCurrentBufferPercentage = 0;

			mMediaPlayer.setDataSource(mContext, mUri);
			mMediaPlayer.setInitPosition(mInitPostion);
			mMediaPlayer.setVolume(mVolumevalue);
			//mMediaPlayer.setInitPosition(9000);
			mMediaPlayer.setSourceType(mSourceType);
			mMediaPlayer.setDisplay(mSurfaceHolder);
			// mMediaPlayer.setDecoderSurface(mSurfaceHolder.getSurface());
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setScreenOnWhilePlaying(true);
			mMediaPlayer.prepareAsync();
			mCurrentState = STATE_PREPARING;
			attachMediaController();
		} catch (IllegalStateException ex) // 多个播放器创建会在setAudioStreamType()方法出现异常
		{
			LogTag.i("Unable to open content: " + mUri
					+ " ,IllegalArgumentException=" + ex);
			mCurrentState = STATE_ERROR;
			StateChange(mCurrentState);
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer,
					MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		} catch (IllegalArgumentException ex) {
			LogTag.i("Unable to open content: " + mUri
					+ " ,IllegalArgumentException=" + ex);
			mCurrentState = STATE_ERROR;
			StateChange(mCurrentState);
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer,
					MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		} catch (IOException e) {
			mCurrentState = STATE_ERROR;
			StateChange(mCurrentState);
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer,
					MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
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
			View anchorView = this.getParent() instanceof View ? (View) this
					.getParent() : this;
			mMediaController.setAnchorView(anchorView);
			mMediaController.setEnabled(isInPlaybackState());
		}
	}

	// 设置播放器控件的大小
	private void setVideoViewScale(int width, int height) {
		LayoutParams lp = (LayoutParams) this.getLayoutParams();
		lp.height = height;
		lp.width = width;
		setLayoutParams(lp);
	}

	private void release(boolean cleartargetstate) {
		LogTag.i(TAG,"release(boolean)" + (cleartargetstate?"true":"false"));
		if (mMediaPlayer != null) {
			LogTag.i(TAG,"release(boolean)" + (cleartargetstate?"true":"false") + "(mMediaPlayer != null)");
			String currentDateStop = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog(
					"系统当前时间:  " + currentDateStop + "VideoViewH264m3u8 stop()");
			if (mOnMediaStateTimeListener != null) {
				mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.STOP,
						currentDateStop);
			}
			mMediaPlayer.stop();
			String currentDateRelease = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog(
					"系统当前时间:  " + currentDateRelease
							+ "VideoViewH264m3u8 release()");
			if (mOnMediaStateTimeListener != null) {
				mOnMediaStateTimeListener.onMediaStateTime(
						MeidaStateType.RELEASE, currentDateRelease);
			}
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			StateChange(mCurrentState);
			if (cleartargetstate) {
				mTargetState = STATE_IDLE;
			}
		}
	}
//	float _oldDest=-1 ;

//	@SuppressLint("FloatMath") @Override
//	public boolean onTouchEvent(MotionEvent ev) {
//		if (isInPlaybackState() && mMediaController != null) {
//			toggleMediaControlsVisiblity();
//		}
//
//
//
//		switch (ev.getAction() & MotionEvent.ACTION_MASK) {
//
//		case MotionEvent.ACTION_DOWN:
//			LogTag.i("ACTION_POINTER_DOWN");
//			mode = 1;
//			break;
//
//		case MotionEvent.ACTION_UP:
//			mode = 0;
//			_oldDest=-1;
//			mPreviousX=1000000.0f;
//			mPreviousY=1000000.0f;
//			break;
//
//		case MotionEvent.ACTION_POINTER_DOWN:
//			LogTag.i("ACTION_POINTER_DOWN");
//			mode += 1;
//			break;
//
//		case MotionEvent.ACTION_POINTER_UP:
//			mode -= 1;
//			mPreviousX=1000000.0f;
//			mPreviousY=1000000.0f;
//			break;
//
//		case MotionEvent.ACTION_MOVE:
//			if ( ev.getPointerCount()== 1) {
//				float y = ev.getY();
//				float x = ev.getX();
//				float dy;
//				float dx;
//				if((mPreviousX==1000000.0f)&(mPreviousY==1000000.0f)){
//					mPreviousY = y;// 记录触控笔位置
//					mPreviousX = x;// 记录触控笔位置
//					LogTag.i("mPreviousX=" +mPreviousX+"mPreviousY=" + mPreviousY );
//				}else{
//						this.setOneFingertouchInfomation(mPreviousX,mPreviousY,x,y);
//						mPreviousY = y;// 记录触控笔位置
//						mPreviousX = x;// 记录触控笔位置
//						//mMediaPlayer.opengl_panorama_Angle(-panorama.total_angle_x, -panorama.total_angle_y,panorama.total_angle_z);
//				}				
//			} 
//			else if(ev.getPointerCount() == 2) {//两个手指第一次按下
//
//					if(_oldDest==-1){//两个手指第一次同时按下
//						mPreviousX0 =ev.getX(0);
//						mPreviousY0 =ev.getY(0);
//						mPreviousX1 =ev.getX(1);
//						mPreviousY1 =ev.getY(1);
//						//LogTag.i("mPreviousX0="+mPreviousX0+"mPreviousY0="+mPreviousY0+"mPreviousX1"+mPreviousX1+"mPreviousY1"+mPreviousY1 );
//						_oldDest=1;
//					}else{
//						this.setTwoFingertouchInfomation(mPreviousX0,mPreviousY0,mPreviousX1,mPreviousY1,ev.getX(0),ev.getY(0),ev.getX(1),ev.getY(1));
////						Log.i("panorama.total_zoom","panorama.total_zoom="+panorama.total_zoom);
////							mMediaPlayer.opengl_panorama_Zoom(panorama.total_zoom);
//
//							mPreviousX0 =ev.getX(0);
//							mPreviousY0 =ev.getY(0);
//							mPreviousX1 =ev.getX(1);
//							mPreviousY1 =ev.getY(1);
//					}
//			}else{
//				
//			}
//		}
//
//		return true;
//	}

	@Override
	public boolean onTrackballEvent(MotionEvent ev) {
		if (isInPlaybackState() && mMediaController != null) {
			toggleMediaControlsVisiblity();
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK
				&& keyCode != KeyEvent.KEYCODE_VOLUME_UP
				&& keyCode != KeyEvent.KEYCODE_VOLUME_DOWN
				&& keyCode != KeyEvent.KEYCODE_MENU
				&& keyCode != KeyEvent.KEYCODE_CALL
				&& keyCode != KeyEvent.KEYCODE_ENDCALL;
		if (isInPlaybackState() && isKeyCodeSupported
				&& mMediaController != null) {
			if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
					|| keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
				if (mMediaPlayer.isPlaying()) {
					pause();
					mMediaController.show();
				} else {
					start();
					mMediaController.hide();

				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
					&& mMediaPlayer.isPlaying()) {
				pause();
				mMediaController.show();
			} else {
				toggleMediaControlsVisiblity();
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	private void toggleMediaControlsVisiblity() {
		if (mMediaController.isShowing()) {
			mMediaController.hide();
		} else {
			mMediaController.show();
		}
	}

	@Override
	public boolean isPlaying() {
		//---------------------------------------------------------------------------------------------
//		if (mContext instanceof Activity) {
//			mSensorManager=(SensorManager)((Activity)mContext).getSystemService(Context.SENSOR_SERVICE);
//			orientationSensorG=mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
//			SensorEventListener mySensorListener=new SensorEventListener() {
//
//				@Override
//				public void onSensorChanged(SensorEvent event) {
//					// TODO Auto-generated method stub
//		              float axisX = (float) event.values[0];//(Math.PI*2))*360;
//			             
//		              float axisY = (float) event.values[1];//(Math.PI*2))*360;
//		       
//		              float axisZ = (float) event.values[2];//(Math.PI*2))*360;
//		              //panorama.setgravity_yroInfomation(axisX,axisY,axisZ);
//		            	
//			       if(mMediaPlayer!=null){
//			    	   try{
//			    		   setgravity_yroInfomation(axisX,axisY,axisY);
//			    	   }catch(Exception e){
//			    		  LogTag.i(  "isplaying","isplaying="+e.getMessage());
//			    	   }
//			       }
//
//					
//				}
//
//				@Override
//				public void onAccuracyChanged(Sensor sensor, int accuracy) {
//					// TODO Auto-generated method stub
//					
//				}
//
//			
//			};
//			SensorEventListener mySensorListenerG=new SensorEventListener() {
//				
//				@Override
//				public void onSensorChanged(SensorEvent event) {
//					// TODO Auto-generated method stub
//					if(event.sensor.getType()==Sensor.TYPE_GRAVITY){
//						@SuppressWarnings("deprecation")
//						float gx = event.values[SensorManager.DATA_X];
//						@SuppressWarnings("deprecation")
//						float gy = event.values[SensorManager.DATA_Y];
//						@SuppressWarnings("deprecation")
//						float gz = event.values[SensorManager.DATA_Z];
////						panorama.setGravityInfomation(gx,gy,gz);
//						if(mMediaPlayer!=null){
//				    	   try{
//				    		   VideoViewH264m3u8.this.setGravityInfomation(gx,gy,gz);
//				    	   }catch(Exception e){
//				    		  LogTag.i(  "isplaying","isplaying="+e.getMessage());
//				    	   }
//						}
//					}
//					
//				}
//
//				
//				@Override
//				public void onAccuracyChanged(Sensor sensor, int accuracy) {
//					// TODO Auto-generated method stub
//					
//				}
//			};
//			mSensorManager.registerListener(mySensorListener,mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),SensorManager.SENSOR_DELAY_GAME);
//			mSensorManager.registerListener(mySensorListenerG,orientationSensorG,SensorManager.SENSOR_DELAY_GAME );
//		}
		
	//--------------------------------------------------------------------------------
		return isInPlaybackState() && mMediaPlayer.isPlaying();
	}

	public boolean isPaused() {
//		//------------------------------------------------------------------------------
//		mSensorManager.unregisterListener(mySensorListener);
//		mSensorManager.unregisterListener(mySensorListenerG);
//		//--------------------------------------------------------------------------------
		return mCurrentState == STATE_PAUSED;
	}

	MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
		public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
			LogTag.i("onVideoSizeChanged(), width=" + width + ", heigth="
					+ height);
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();
			if (mVideoWidth != 0 && mVideoHeight != 0) {
				getHolder().setFixedSize(mVideoWidth, mVideoHeight);
			}
			if (mOnVideoSizeChangedListener != null) {
				mOnVideoSizeChangedListener.onVideoSizeChanged(mp, mVideoWidth,
						mVideoHeight);
			}
		}
	};

	MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
		public void onPrepared(MediaPlayer mp) {
			LogTag.i("onPrepared()");
			String currentDate = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog(
					"系统当前时间:  " + currentDate
							+ " VideoViewH264m3u8(软解m3u8)  onPrepared()");
			FFMpegPlayer mpegPlayer = (FFMpegPlayer) mp;
			if ((mpegPlayer == mMediaPlayer) && (mMediaPlayer != null)) {
				if (mOnMediaStateTimeListener != null) {
					mOnMediaStateTimeListener.onMediaStateTime(
							MeidaStateType.PREPARED, currentDate);
				}
				mCurrentState = STATE_PREPARED;
				StateChange(mCurrentState);

				mCanPause = mCanSeekBack = mCanSeekForward = true;

				if (mOnPreparedListener != null) {
					mOnPreparedListener.onPrepared(mMediaPlayer);
				}

				if ((mpegPlayer == mMediaPlayer) && (mMediaPlayer != null)) {
					try {
						mLastUrl = mpegPlayer.getLastUrl();
						mVersion = mpegPlayer.getVersion();
					} catch (IllegalStateException ex) {
						LogTag.i(ex.toString());
					}
				}

				LogTag.i(".so verison=" + mVersion);

				if (mMediaController != null) {
					mMediaController.setEnabled(true);
				}

				int seekToPosition = mSeekWhenPrepared; // mSeekWhenPrepared may be
				if (seekToPosition != 0) {
					seekTo(seekToPosition);
				}

				if ((mpegPlayer == mMediaPlayer) && (mMediaPlayer != null)) {
					try {
						mVideoWidth = mpegPlayer.getVideoWidth();
						mVideoHeight = mpegPlayer.getVideoHeight();
					} catch (IllegalStateException ex) {
						LogTag.i(ex.toString());
					}
				}

				if (mVideoWidth != 0 && mVideoHeight != 0) {
					if (mSurfaceWidth == mVideoWidth
							&& mSurfaceHeight == mVideoHeight) {
						if (mTargetState == STATE_PLAYING) {
							start();
							if (mMediaController != null) {
								mMediaController.show();
							}
						} else if (!isPlaying()
								&& (seekToPosition != 0 || getCurrentPosition() > 0)) {
							if (mMediaController != null) {
								// Show the media controls when we're paused into a
								// video and make 'em stick.
								mMediaController.show(0);
							}
						}
					} else {
						getHolder().setFixedSize(mVideoWidth, mVideoHeight);
					}

				} else {

					if (mTargetState == STATE_PLAYING) {
						start();
					}
				}


			}
		}

	};

	private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
		public void onCompletion(MediaPlayer mp) {
			LogTag.i("onCompletion()");
			mCurrentState = STATE_PLAYBACK_COMPLETED;
			StateChange(mCurrentState);
			mTargetState = STATE_PLAYBACK_COMPLETED;

//			mCurrentState = STATE_STOPBACK;
//			StateChange(mCurrentState);// 未统计加入
			
			if (mMediaController != null) {
				mMediaController.hide();
			}
			if (mOnCompletionListener != null) {
				mOnCompletionListener.onCompletion(mMediaPlayer);
			}
			/**
			 * 播放完成，停止并释放资源
			 */
			VideoViewH264m3u8.this.pause();
			// VideoViewH264m3u8.this.release(true);
			String currentDate =Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog(
                    "系统当前时间:  " + currentDate
                            + " OpenGL   mCompletionListener releaseGL()");
			VideoViewH264m3u8.this.releaseGL(true, 1);
		}
	};

	private FFMpegPlayer.OnSuccessListener mSuccessListener = new OnSuccessListener() {

		@Override
		public void onSuccess() {
			LogTag.i("onSuccess()");
			if (mOnSuccessListener != null) {
				mOnSuccessListener.onSuccess();
			}
			LogTag.i("软解成功");
		}
	};

	private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
		public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
			LogTag.i("onError(): framework_err=" + framework_err
					+ ", impl_err=" + impl_err);
			mCurrentState = STATE_ERROR;
			StateChange(mCurrentState);
			mTargetState = STATE_ERROR;
			if (mMediaController != null) {
				mMediaController.hide();
			}
			/* If an error handler has been supplied, use it and finish. */
			if (mOnErrorListener != null) {
				mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err);

			}
			// PreferenceUtil.setErrorCode(mContext,
			// "VideoViewH264m3u8 error, framework_err=" + framework_err +
			// ", impl_err=" + impl_err);
			String currentDate = Tools.getCurrentDate();
			LetvMediaPlayerManager
					.getInstance()
					.writePlayLog(
							"系统当前时间:  "
									+ currentDate
									+ "VideoViewH264m3u8(软解m3u8) 播放出错error, framework_err="
									+ framework_err + ", impl_err=" + impl_err);
			if (mOnMediaStateTimeListener != null) {
				mOnMediaStateTimeListener.onMediaStateTime(
						MeidaStateType.ERROR, currentDate);
			}
			LogTag.i("软解失败");
			return true;
		}
	};

	private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {

		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			mCurrentBufferPercentage = percent;
			if (mOnBufferingUpdateListener != null) {
				mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
			}
			//通知使用层
			if (mOnInfoListener != null) {
				mOnInfoListener.onInfo(mMediaPlayer, MEDIA_INFO_BUFFERING_PERCENT, percent);
			}

		}
	};
	private MediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
		public void onSeekComplete(MediaPlayer mp) {
			LogTag.i("onSeekComplete()");
			if (mOnSeekCompleteListener != null) {
				mOnSeekCompleteListener.onSeekComplete(mMediaPlayer);
			}
			// resume();
		}
	};

	private FFMpegPlayer.OnAdNumberListener mAdNumberListener = new FFMpegPlayer.OnAdNumberListener() {

		@Override
		public void onAdNumber(FFMpegPlayer mediaPlayer, int number) {
			if (mOnAdNumberListener != null) {
				mOnAdNumberListener.onAdNumber(mediaPlayer, number);
			}
		}
	};

	private FFMpegPlayer.OnBlockListener mBlockListener = new FFMpegPlayer.OnBlockListener() {

		@Override
		public void onBlock(FFMpegPlayer mediaPlayer, int blockinfo) {
			if (mOnBlockListener != null) {
				mOnBlockListener.onBlock(mediaPlayer, blockinfo);
			}
			if (blockinfo == FFMpegPlayer.MEDIA_BLOCK_START) {
				String currentDate = Tools.getCurrentDate();
				LetvMediaPlayerManager.getInstance().writePlayLog(
						"系统当前时间:  " + currentDate
								+ " VideoViewH264m3u8Hw(软解m3u8)  出现卡顿 ");

				//通知使用层
				if (mOnInfoListener != null) {
					mOnInfoListener.onInfo(mMediaPlayer, MEDIA_INFO_BUFFERING_START, 0);
				}
			} else if (blockinfo == FFMpegPlayer.MEDIA_BLOCK_END) {
				String currentDate = Tools.getCurrentDate();
				LetvMediaPlayerManager.getInstance().writePlayLog(
						"系统当前时间:  " + currentDate
								+ " VideoViewH264m3u8Hw(软解m3u8)  结束卡顿 ");
				//通知使用层
				if (mOnInfoListener != null) {
					mOnInfoListener.onInfo(mMediaPlayer, MEDIA_INFO_BUFFERING_END, 0);
				}
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
			if (mOnCacheListener != null) {
				mOnCacheListener.onCache(mediaPlayer, arg, percent, cacherate);
			}

			if (arg == FFMpegPlayer.MEDIA_CACHE_START) {
				String currentDate = Tools.getCurrentDate();
				LetvMediaPlayerManager.getInstance().writePlayLog(
						"系统当前时间:  " + currentDate
								+ " VideoViewH264m3u8Hw(软解m3u8)  开始缓存 ");
			}

			if (arg == FFMpegPlayer.MEDIA_CACHE_END) {
				String currentDate = Tools.getCurrentDate();
				LetvMediaPlayerManager.getInstance().writePlayLog(
                        "系统当前时间:  " + currentDate
                                + " VideoViewH264m3u8Hw(软解m3u8)  缓存结束 ");
			}

		}
	};

	/**
	 * lhq 2015-06-30 注册初始化GL 的事件
	 */
	private FFMpegPlayer.OnInitGLListener mInitGLListener = new OnInitGLListener() {

		@Override
		public void initGL(int w, int h, int type, int flag, String cmdStr) {
			// TODO Auto-generated method stub
			VideoViewH264m3u8.this.initGL(w, h, type, flag, cmdStr);
		}
	};

	private FFMpegPlayer.OnFirstPlayLitener mFirstPlayLitener = new OnFirstPlayLitener() {

		@Override
		public void onFirstPlay(FFMpegPlayer mediaPlayer) {
			// TODO Auto-generated method stub
			String currentDate = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog(
					"系统当前时间:  " + currentDate + "第一次播放 ");
			if (mOnFirstPlayLitener != null) {
				mOnFirstPlayLitener.onFirstPlay(mediaPlayer);
			}

		}
	};

	private FFMpegPlayer.OnDisplayListener mDisplayListener = new FFMpegPlayer.OnDisplayListener() {

		@Override
		public void onDisplay(FFMpegPlayer mediaPlayer) {
			LogTag.i("软解onDisplay()");
			String currentDate = Tools.getCurrentDate();
			LetvMediaPlayerManager
					.getInstance()
					.writePlayLog(
							"系统当前时间:  "
									+ currentDate
									+ " VideoViewH264m3u8(软解m3u8)  第一帧画面时间  onDisplay()");
			if (mOnMediaStateTimeListener != null) {
				mOnMediaStateTimeListener.onMediaStateTime(
						MeidaStateType.DIAPLAY, currentDate);
			}
			mCurrentState = STATE_PLAYING;
			StateChange(mCurrentState);
			/*
			 * testhandler.postDelayed(new Runnable() {
			 * 
			 * @Override public void run() { // TODO Auto-generated method stub
			 * if(mPreviousY >= 360) { mPreviousY = 0; } mPreviousY+= 2;
			 * mMediaPlayer.opengl_panorama_Angle(mPreviousX, mPreviousY);
			 * testhandler.postDelayed(this, 200); } }, 100);
			 */
		}
	};

	private MediaPlayer.OnInfoListener mInfoListener = new OnInfoListener() {

		@Override
		public boolean onInfo(MediaPlayer mp, int what, int extra) {

			if (mOnInfoListener != null) {
				mOnInfoListener.onInfo(mp, what, extra);
			}
			return false;
		}
	};

	private FFMpegPlayer.GLRenderControler mGLRenderControler = new FFMpegPlayer.GLRenderControler() {

		public void setGLStartRenderMode() {
			setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

		}

		public void setGLStopRenderMode() {

			setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

		}

	};

	public void setOnSeekCompleteListener(OnSeekCompleteListener l) {
		this.mOnSeekCompleteListener = l;
	}

	public void setOnAdNumberListener(OnAdNumberListener l) {
		this.mOnAdNumberListener = l;
	}

	class MyRenderer implements GLSurfaceView.Renderer {

		public void onSurfaceCreated(GL10 gl, EGLConfig c) {
			LogTag.i("MyRenderer:onSurfaceCreated()");
			// lastW = 0;
			// lastH = 0;
		}

		public void onSurfaceChanged(GL10 gl, int w, int h) {
			try {
				mSurfaceHeight = h;
				mSurfaceWidth = w;

				LogTag.i("MyRenderer:onSurfaceChanged(), w=" + w + ", h=" + h
						+ ", lastW=" + lastW + ", lastH=" + lastH);
				if (mMediaPlayer != null) {

					if ((lastW != w) || (lastH != h)) {
						gl.glViewport(0, 0, w, h);
						mMediaPlayer.native_gl_resize(w, h);
						lastW = w;
						lastH = h;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void onDrawFrame(GL10 gl) {
			try {
				if (mMediaPlayer != null /*
										 * && (mFlag_SurfaceChangeMode ==
										 * SURFACE_CHANGED_INIT)
										 */) {
					mMediaPlayer.native_gl_render();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public long time = 0;
		public int lastW = 0;
		public int lastH = 0;
		public short framerate = 0;
		public long fpsTime = 0;
		public long frameTime = 0;
		public float avgFPS = 0;
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

	private void StateChange(int mCurrentState) {
		LogTag.i("StateChange(), mCurrentState=" + mCurrentState);
		if (mVideoViewStateChangeListener != null) {
			mVideoViewStateChangeListener.onChange(mCurrentState);
		}
	}

	@Override
	public void setVideoViewStateChangeListener(
			OnVideoViewStateChangeListener listener) {
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

	public void setOnBufferingUpdateListener(OnBufferingUpdateListener l) {
		mOnBufferingUpdateListener = l;
	}

	public void setOnPreparedListener(OnPreparedListener l) {
		this.mOnPreparedListener = l;
	}

	public void setOnCompletionListener(OnCompletionListener l) {
		this.mOnCompletionListener = l;
	}

	public void setOnErrorListener(OnErrorListener l) {
		this.mOnErrorListener = l;
	}

	public void setOnSuccessListener(OnSuccessListener l) {
		this.mOnSuccessListener = l;
	}

	@Override
	public void setVideoPlayUrl(PlayUrl url) {
		mPlayerUrl = url;
		setVideoURI(Uri.parse(mPlayerUrl.getUrl()));
	}

	/**
	 * 设置缓存
	 */
	@Override
	public void setCacheSize(int video_size, int audio_size, int picture_size,
			int startpic_size) {
		 mMediaPlayer.setCacheSize(video_size, audio_size, picture_size,
		 startpic_size);
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
		// TODO Auto-generated method stub

	}

	@Override
	public void setOnFirstPlayListener(OnFirstPlayLitener l) {
		// TODO Auto-generated method stub
		this.mOnFirstPlayLitener = l;
	}

	private boolean releaseOpenGLOK = false;
	
	private void startReleaseTimer(){
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				String currentDate= Tools.getCurrentDate();
				LogTag.i(TAG,"系统当前时间:  " + currentDate+"releaseBlock()  timer out releaseOpenGLOK:"+releaseOpenGLOK);
				releaseOpenGLOK = true;
			}
		};
		timer.schedule(task, RELEASE_BLOCK_TIME);
	}
	
	private void releaseBlock(){
		String currentDate= Tools.getCurrentDate();
		LogTag.i(TAG,"系统当前时间:  " + currentDate+"releaseBlock()  releaseOpenGLOK:"+releaseOpenGLOK);
		startReleaseTimer();
		while (!releaseOpenGLOK) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * realse opgl source
	 */
	private void releaseGL(boolean cleartargetstate, int type) {
		LogTag.i(TAG, "releaseGL()" + (cleartargetstate ? "true" : "false") + type);
		if (mMediaPlayer != null) {
			if(mReleaseGLState==RELEASE_GL_STATE_INIT){
				mReleaseGLState = RELEASE_GL_STATE_ING;
				LogTag.i(TAG, "releaseGL()" + (cleartargetstate ? "true" : "false") + type + "(mMediaPlayer != null) (mReleaseGLState==RELEASE_GL_STATE_INIT)");
				String currentDate = Tools.getCurrentDate();
				LetvMediaPlayerManager.getInstance().writePlayLog(
						"系统当前时间:  " + currentDate
								+ "OpenGL mReleaseGLState change to RELEASE_GL_STATE_ING");
				
				ReleaseGL releaseGL = new ReleaseGL(cleartargetstate, type);
				releaseOpenGLOK = false;
				queueEvent(releaseGL);
				releaseBlock();
				if (type == 1) // release
				{
							// TODO Auto-generated method stub
							release(cleartargetstate);
							mReleaseGLState=RELEASE_GL_STATE_INIT;							
							currentDate = Tools.getCurrentDate();
							LetvMediaPlayerManager.getInstance().writePlayLog(
									"系统当前时间:  " + currentDate
											+ "mReleaseGLState change to RELEASE_GL_STATE_INIT");
				} else {
					// stopplayback
							// TODO Auto-generated method stub
							stopPlayback(cleartargetstate);
							mReleaseGLState=RELEASE_GL_STATE_INIT;
							currentDate = Tools.getCurrentDate();
							LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  " + currentDate
											+ "mReleaseGLState change to RELEASE_GL_STATE_INIT");
				}
			}else if(mReleaseGLState==RELEASE_GL_STATE_ING){
				LogTag.i(TAG,"releaseGL()" + (cleartargetstate?"true":"false") + type + "(mMediaPlayer != null) (mReleaseGLState==RELEASE_GL_STATE_ING)");
				String currentDate = Tools.getCurrentDate();
				LetvMediaPlayerManager.getInstance().writePlayLog(
						"系统当前时间:  " + currentDate
								+ "mReleaseGLState RELEASE_GL_STATE_ING but receive releaseGL");
			}else{
				LogTag.i(TAG,"releaseGL()" + (cleartargetstate?"true":"false") + type + "(mMediaPlayer != null) (else)");
				String currentDate = Tools.getCurrentDate();
				LetvMediaPlayerManager.getInstance().writePlayLog(
						"系统当前时间:  " + currentDate
								+ "mReleaseGLState RELEASE_GL_STATE_ERROR");
			}
//			queueEvent(new Runnable() {
//				
//			});
		}
	}

	/**
	 * init GL resource
	 * 
	 * @param w
	 * @param h
	 * @param type
	 * @param flag
	 * @param cmdstr
	 */
	private void initGL(final int w, final int h, final int type,
			final int flag, final String cmdstr) {
		String currentDateStop = Tools.getCurrentDate();
		String threadinfo = Thread.currentThread().getName()
				+ Thread.currentThread().getId();
		LetvMediaPlayerManager.getInstance().writePlayLog(
                "OpenGL resource called 系统当前时间:  " + currentDateStop
                        + "VideoViewH264m3u8 initGL" + threadinfo);

		queueEvent(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                String currentDateStop = Tools.getCurrentDate();
                String threadinfo = Thread.currentThread().getName()
                        + Thread.currentThread().getId();
                LetvMediaPlayerManager.getInstance().writePlayLog(
                        "OpenGL resource called 系统当前时间:  " + currentDateStop
                                + "VideoViewH264m3u8 initGL(queueEvent)"
                                + threadinfo);
                // flag=0,普通视频,flag=1全景视频
                // mMediaPlayer.opengl_es_init(w, h, type, flag, cmdstr);
                if (mMediaPlayer.mSourceType == mMediaPlayer.MEDIA_SOURCE_TYPE_NORMAL) {
                    mMediaPlayer.opengl_es_init(w, h, type, 0, cmdstr);
                } else if (mMediaPlayer.mSourceType == mMediaPlayer.MEDIA_SOURCE_TYPE_PANORAMA) {
                    mMediaPlayer.opengl_es_init(w, h, type, 1, cmdstr);
                } else {
                }
            }
        });

	} 

	public class ReleaseGL implements Runnable {
		private boolean mCleartargetstate;

		private int mType;

		public ReleaseGL(boolean cleartargetstate, int type) {
			// TODO Auto-generated constructor stub
			this.mCleartargetstate = cleartargetstate;
			this.mType = type;
		}

		@Override
		public void run() {
			LogTag.i(TAG,"ReleaseGL()->run()");
			// TODO Auto-generated method stub
			String currentDateStop = Tools.getCurrentDate();
			String threadinfo = Thread.currentThread().getName()
					+ Thread.currentThread().getId();
			LetvMediaPlayerManager.getInstance().writePlayLog("OpenGL resource called 系统当前时间:  " + currentDateStop+ "VideoViewH264m3u8 realeseGL(queueEvent)"+ threadinfo);
			mMediaPlayer.opengl_es_destroy(0);
			
			String currentDate= Tools.getCurrentDate();
			LogTag.i(TAG,"系统当前时间:  " + currentDate+"releaseBlock()  normal end  releaseOpenGLOK:"+releaseOpenGLOK);
			releaseOpenGLOK=true;
//			if (mType == 1) // release
//			{
//				mReleaseMediaPlayerHandler.post(new Runnable() {
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						release(mCleartargetstate);
//						mReleaseGLState=RELEASE_GL_STATE_INIT;
//						
//						String currentDate = Tools.getCurrentDate();
//						LetvMediaPlayerManager.getInstance().writePlayLog(
//								"系统当前时间:  " + currentDate
//										+ "mReleaseGLState change to RELEASE_GL_STATE_INIT");
//					}
//				});
//			} else {
//				// stopplayback
//				mReleaseMediaPlayerHandler.post(new Runnable() {
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						stopPlayback(mCleartargetstate);
//						mReleaseGLState=RELEASE_GL_STATE_INIT;
//						String currentDate = Tools.getCurrentDate();
//						LetvMediaPlayerManager.getInstance().writePlayLog(
//								"系统当前时间:  " + currentDate
//										+ "mReleaseGLState change to RELEASE_GL_STATE_INIT");
//					}
//				});
//				
//			}
		}

	}

//	@OverrideReleaseGL
	public int setSourceType(int sourceType) {
		// TODO Auto-generated method stub
		mSourceType = sourceType;
		if (mMediaPlayer != null) {
			mMediaPlayer.setSourceType(sourceType);
		}
		return 0;
	}

	@Override
	public int setMachineInfomation(float ScreenResolution) {
		// TODO Auto-generated method stub
		if(mMediaPlayer!=null){
			mMediaPlayer.setMachineInfomation(ScreenResolution);
		}
		return 0;
	}

	@Override
	public int setOneFingertouchInfomation(float begin_x, float begin_y,
			float end_x, float end_y) {
		// TODO Auto-generated method stub
		if(mMediaPlayer!=null){
		mMediaPlayer.setOneFingertouchInfomation(begin_x, begin_y, end_x, end_y);
		}
		return 0;
	}

	@Override
	public int setTwoFingertouchInfomation(float begin_x0, float begin_y0,
										   float begin_x1, float begin_y1, float end_x0, float end_y0,
			float end_x1, float end_y1) {
		// TODO Auto-generated method stub
		if(mMediaPlayer!=null){
		mMediaPlayer.setTwoFingertouchInfomation(begin_x0, begin_y0, begin_x1, begin_y1, end_x0, end_y0, end_x1, end_y1);
		}
		return 0;
	}

	@Override
	public int setgravity_yroInfomation(float gravity_yro_x,
			float gravity_yro_y, float gravity_yro_z) {
		// TODO Auto-generated method stub
		if(mMediaPlayer!=null){
		mMediaPlayer.setgravity_yroInfomation(gravity_yro_x, gravity_yro_y, gravity_yro_z);
		}
		return 0;
	}

	@Override
	public int setGravityInfomation(float gravity_x, float gravity_y,
			float gravity_z) {
		// TODO Auto-generated method stub
		if(mMediaPlayer!=null){
		mMediaPlayer.setGravityInfomation(gravity_x, gravity_y, gravity_z);
		}
		return 0;
	}

	@Override
	public int setgravity_yroValidInfomation(boolean  gravityValid) {
		// TODO Auto-generated method stub
		if(mMediaPlayer!=null){
		mMediaPlayer.setgravity_yroValidInfomation(gravityValid);
		}
		return 0;
	}

	@Override
	public int setAngleInit() {
		// TODO Auto-generated method stub
		if(mMediaPlayer!=null){
		mMediaPlayer.setAngleInit();
		}
		return 0;
	}

	@Override
	public int setTwoFingerZoom(float zoom) {
		if(mMediaPlayer!=null){
		mMediaPlayer.setTwoFingerZoom(zoom);
		}
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setInitPosition(int msec) {
		// TODO Auto-generated method stub
		mInitPostion = msec;
		if(mMediaPlayer!=null){
			mMediaPlayer.setInitPosition(msec);
		}
	}	
	
	@Override
	public void setVolume(int volume) {
		// TODO Auto-generated method stub
		mVolumevalue = volume;
		if(mMediaPlayer!=null){
			mMediaPlayer.setVolume(volume);
		}
	}

    /**
     * 软解不需要返回播放器,返回null
     * changed for tvlive by zanxiaofei 2015-10-28
     * @return null
     */
    @Override
    public MediaPlayer getMediaPlayer() {
        return null;
    }

    /**
     * 硬解码需要设置播放高低水位,软解不需要
     * changed for tvlive by zanxiaofei 2015-10-30
     * @param l
     */
    @Override
    public void setOnNeedSetPlayParamsListener(OnNeedSetPlayParamsListener l) {
        //do nothing
    }

	/**
	 * 设置是否支持画面比例调整,如果不支持画面会填满父布局窗口;
	 * 如果支持,根据ratioType,会将画面调整为原始,4:3,全屏等比例.
	 * @param isSupportScale
	 */
	public void setIsSupportScale(boolean isSupportScale) {
		this.isSupportScale = isSupportScale;
	}
}
