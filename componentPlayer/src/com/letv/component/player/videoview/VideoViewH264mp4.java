/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//package com.letv.android.screen.client.views;
package com.letv.component.player.videoview;

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

import java.io.IOException;
import java.util.Map;

/**
 * Displays a video file. The VideoView class can load images from various
 * sources (such as resources or content providers), takes care of computing its
 * measurement from the video so that it can be used in any layout manager, and
 * provides various display options such as scaling and tinting.
 */
public class VideoViewH264mp4 extends SurfaceView implements LetvMediaPlayerControl {
	private String TAG = "VideoViewH264mp4";
	
	public static final int STATE_ERROR = -1;
	public static final int STATE_IDLE = 0;
	public static final int STATE_PREPARING = 1;
	public static final int STATE_PREPARED = 2;
	public static final int STATE_PLAYING = 3;
	public static final int STATE_PAUSED = 4;
	public static final int STATE_PLAYBACK_COMPLETED = 5;
	public static final int STATE_STOPBACK = 6;
	public static final int STATE_ENFORCEMENT = 7;

	// mCurrentState is a VideoView object's current state.
	// mTargetState is the state that a method caller intends to reach.
	// For instance, regardless the VideoView object's current state,
	// calling pause() intends to bring the object to a target state
	// of STATE_PAUSED.
	private int mCurrentState = STATE_IDLE;
	private int mTargetState = STATE_IDLE;
	
	private final int FORWARD_TIME = 15000 ;
	private final int REWIND_TIME = 15000 ;

	private SurfaceHolder mSurfaceHolder = null;
	private MediaPlayer mMediaPlayer = null;
	private Context mContext;
	private int mVideoWidth;
	private int mVideoHeight;
	private int mSurfaceWidth;
	private int mSurfaceHeight;
	private MediaController mMediaController;
	private OnCompletionListener mOnCompletionListener;
	private OnErrorListener mOnErrorListener;
	private OnPreparedListener mOnPreparedListener;
	private OnSeekCompleteListener mOnSeekCompleteListener;
	private OnVideoSizeChangedListener mOnVideoSizeChangedListener;
	private OnBufferingUpdateListener mOnBufferingUpdateListener;
	private OnInfoListener mOnInfoListener;
	private OnVideoViewStateChangeListener mOnVideoViewStateChangeListener;
	private OnMediaStateTimeListener mOnMediaStateTimeListener;
	private int mCurrentBufferPercentage;
	private int mSeekWhenPrepared; // recording the seek position while preparing
	
	private boolean mCanPause;
	private boolean mCanSeekBack;
	private boolean mCanSeekForward;
	
	private Uri mUri;
	private int mDuration;
	private int mRatioType = -1;
	
	private PlayUrl mPlayerUrl;
	
	/**
	 * 记录消耗前的时间点
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

	public VideoViewH264mp4(Context context) {
		super(context);
		this.mContext = context;
		initVideoView();
	}

	public VideoViewH264mp4(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		this.mContext = context;
		initVideoView();
	}

	public VideoViewH264mp4(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
		initVideoView();
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
	@Override
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
	
	private void initVideoView() {
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
	
	@Override
	public void setVideoPath(String path) {
		mPlayerUrl = new PlayUrl();
		mPlayerUrl.setVid(-1);
		mPlayerUrl.setUrl(path);
		mPlayerUrl.setStreamType(PlayUrl.StreamType.STREAM_TYPE_UNKNOWN);
		setVideoURI(Uri.parse(path));
	}
	
	@Override
	public void setVideoPath(String path, Map<String, String> headers) {
		
	}

	public void setVideoURI(Uri uri) {
		String currentDate = Tools.getCurrentDate();
		LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDate+" VideoViewH264mp4(普通手机videoview)  setVideoURI(), url="
				+ ((uri != null) ? uri.toString() : "null"),true);
		if(mOnMediaStateTimeListener!=null){
			mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.INITPATH, currentDate);
		}
		mUri = uri;
		mSeekWhenPrepared = 0;
		openVideo();
		requestLayout();
		invalidate();
		LogTag.i("setVideoURI(), url="
				+ ((uri != null) ? uri.toString() : "null"));
	}

	private void openVideo() {
		if (mUri == null || mSurfaceHolder == null)	{
			setVisibility(VISIBLE);
			return;
		}
		// Tell the music playback service to pause
		// framework.
		Intent i = new Intent("com.android.music.musicservicecommand");
		i.putExtra("command", "pause");
		mContext.sendBroadcast(i);

		// we shouldn't clear the target state, because somebody might have
		// called start() previously
		release(false);
		try {
			String currentDate = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDate+" VideoViewH264mp4(普通手机videoview)  创建MediaPlayer对象");
			if(mOnMediaStateTimeListener!=null){
				mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.CREATE, currentDate);
			}
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnPreparedListener(mPreparedListener);
			mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
			mDuration = -1;
			mMediaPlayer.setOnCompletionListener(mCompletionListener);
			mMediaPlayer.setOnErrorListener(mErrorListener);
			mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
			mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
			mMediaPlayer.setOnInfoListener(mInfoListener);
			mCurrentBufferPercentage = 0;
			//设置播放高低水位
			//changed for tvlive by zanxiaofei 2015-10-30
			if(mOnNeedSetPlayParamsListener != null){
				mOnNeedSetPlayParamsListener.onNeedSet();
			}
			//end

			mMediaPlayer.setDataSource(mContext, mUri);
			mMediaPlayer.setDisplay(mSurfaceHolder);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setScreenOnWhilePlaying(true);
			mMediaPlayer.prepareAsync();
			// we don't set the target state here either, but preserve the
			// target state that was there before.
			mCurrentState = STATE_PREPARING;
			StateChange(mCurrentState);
			attachMediaController();
		} catch (IOException ex) {
			LogTag.i("Unable to open content: " + mUri + " ,IOException=" + ex);
			mCurrentState = STATE_ERROR;
			StateChange(mCurrentState);
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		} catch (IllegalArgumentException ex) {
			LogTag.i("Unable to open content: " + mUri + " ,IllegalArgumentException=" + ex);
			mCurrentState = STATE_ERROR;
			StateChange(mCurrentState);
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		}
	}

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

	MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
		public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
			LogTag.i("onVideoSizeChanged(), width=" + width + ", heigth=" + height);
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();
			if (mVideoWidth != 0 && mVideoHeight != 0) {
				getHolder().setFixedSize(mVideoWidth, mVideoHeight);
			}
			
			if(mOnVideoSizeChangedListener != null) {
				mOnVideoSizeChangedListener.onVideoSizeChanged(mp, width, height);
			}
		}
	};

	MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
		public void onPrepared(MediaPlayer mp) {
			LogTag.i("onPrepared()");
			String currentDate = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDate+" VideoViewH264mp4(普通手机videoview)  onPrepared()");
			if(mOnMediaStateTimeListener!=null){
				mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.PREPARED, currentDate);
			}
			mCurrentState = STATE_PREPARED;
			StateChange(mCurrentState);

			mCanPause = mCanSeekBack = mCanSeekForward = true;
			if (mMediaController != null) {
				mMediaController.setEnabled(true);
			}
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();

			int seekToPosition = mSeekWhenPrepared; // mSeekWhenPrepared may be
													// changed after seekTo()
													// call
			if (seekToPosition != 0) {
				seekTo(seekToPosition);
			}
			if (mVideoWidth != 0 && mVideoHeight != 0) {
				// Log.i("@@@@", "video size: " + mVideoWidth +"/"+
				// mVideoHeight);
				getHolder().setFixedSize(mVideoWidth, mVideoHeight);
				if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
					// We didn't actually change the size (it was already at the
					// size
					// we need), so we won't get a "surface changed" callback,
					// so
					// start the video here instead of in the callback.
					if (mTargetState == STATE_PLAYING) {
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
			} else {
				// We don't know the video size yet, but should start anyway.
				// The video size might be reported to us later.
				if (mTargetState == STATE_PLAYING) {
					start();
				}
			}
			
			if(mOnPreparedListener != null) {
				mOnPreparedListener.onPrepared(mp);
			}
		}
	};

	private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
		public void onCompletion(MediaPlayer mp) {
			LogTag.i("onCompletion()");
			mCurrentState = STATE_PLAYBACK_COMPLETED;
			StateChange(mCurrentState);
			mTargetState = STATE_PLAYBACK_COMPLETED;
			mCurrentState = STATE_STOPBACK;
			StateChange(mCurrentState);//未统计加入
			if (mMediaController != null) {
				mMediaController.hide();
			}
			
			if(mOnCompletionListener != null) {
				mOnCompletionListener.onCompletion(mp);
			}
		}
	};

	private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
		public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
			LogTag.i("onError(): framework_err=" + framework_err + ", impl_err=" + impl_err);
			mCurrentState = STATE_ERROR;
			StateChange(mCurrentState);
			mTargetState = STATE_ERROR;
			if (mMediaController != null) {
				mMediaController.hide();
			}
			
			if(mOnErrorListener != null) {
				mOnErrorListener.onError(mp, framework_err, impl_err);
			}
			
//			PreferenceUtil.setErrorCode(mContext, "VideoViewH264mp4 error, framework_err=" + framework_err + ", impl_err=" + impl_err);
			String currentDate = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDate+"VideoViewH264mp4(普通手机videoview) 播放出错error, framework_err=" + framework_err + ", impl_err=" + impl_err);
			if(mOnMediaStateTimeListener!=null){
				mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.ERROR, currentDate);
			}
			return true;
		}
	};

	private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			mCurrentBufferPercentage = percent;
			
			if(mOnBufferingUpdateListener != null) {
				mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
			}
		}
	};
	
	private MediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener()
	{
		public void onSeekComplete(MediaPlayer mp)
		{
			LogTag.i("onSeekComplete()");
			if (mOnSeekCompleteListener != null) {
				mOnSeekCompleteListener.onSeekComplete(mMediaPlayer);
			}
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

	SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			LogTag.i("mSHCallback:surfaceChanged(), w=" + w + ", h="+ h);
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
			LogTag.i("mSHCallback:surfaceCreated()");
			mSurfaceHolder = holder;
			openVideo();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			LogTag.i("mSHCallback:surfaceDestroyed()");
			// after we return from this we can't use the surface any more
			mSurfaceHolder = null;
			if (mMediaController != null) {
				mMediaController.hide();
			}
			lastSeekWhenDestoryed = getCurrentPosition() ;
			release(true);
		}
	};

	/*
	 * release the media player in any state
	 */
	private void release(boolean cleartargetstate) {
		if (mMediaPlayer != null) {
			mMediaPlayer.reset();
			String currentDateRelease = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDateRelease+"VideoViewH264mp4 release()");
			if(mOnMediaStateTimeListener!=null){
				mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.RELEASE, currentDateRelease);
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK
				&& keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN
				&& keyCode != KeyEvent.KEYCODE_MENU && keyCode != KeyEvent.KEYCODE_CALL
				&& keyCode != KeyEvent.KEYCODE_ENDCALL;
		if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
			if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
				if (mMediaPlayer.isPlaying()) {
					pause();
					mMediaController.show();
				} else {
					start();
					mMediaController.hide();
				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP && mMediaPlayer.isPlaying()) {
				pause();
				mMediaController.show();
			} else {
				toggleMediaControlsVisiblity();
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	public int getLastSeekWhenDestoryed() {
		return lastSeekWhenDestoryed;
	}

	private void toggleMediaControlsVisiblity() {
		if (mMediaController.isShowing()) {
			mMediaController.hide();
		} else {
			mMediaController.show();
		}
	}

	public void start() {
		HttpRequestManager.getInstance(mContext).requestCapability();
		if(!enforcementWait && !enforcementPause){
			if (isInPlaybackState()) {
//				setVisibility(View.VISIBLE);
				LogTag.i("start()  Mp4系统播放器开始播放");
				mMediaPlayer.start();
				String currentDate = Tools.getCurrentDate();
				LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDate+" VideoViewH264mp4(普通手机videoview)  start()");
				mCurrentState = STATE_PLAYING;
				StateChange(mCurrentState);
			}
		} else {
			StateChange(STATE_ENFORCEMENT);
		}
		mTargetState = STATE_PLAYING;
	}

	public void pause() {
		if (isInPlaybackState()) {
			if (mMediaPlayer.isPlaying()) {
				LogTag.i("pause()");
				mMediaPlayer.pause();
				String currentDate = Tools.getCurrentDate();
				LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDate+" VideoViewH264mp4(普通手机videoview)  pause()");
				mCurrentState = STATE_PAUSED;
				StateChange(mCurrentState);
			}
		}
		mTargetState = STATE_PAUSED;
	}

	// cache duration as mDuration for faster access
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

	public int getCurrentPosition() {
		if (isInPlaybackState()) {
			return mMediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	public void seekTo(int msec) {
		if (isInPlaybackState()) {
			mMediaPlayer.seekTo(msec);
			mSeekWhenPrepared = 0;
			lastSeekWhenDestoryed = 0;
		} else {
			mSeekWhenPrepared = msec;
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

	public boolean isPlaying() {
		return isInPlaybackState() && mMediaPlayer.isPlaying();
	}
	
	public boolean isPaused() {
		return  mCurrentState == STATE_PAUSED;
	}
	
	public boolean isInPlaybackState() {
		return (mMediaPlayer != null && mCurrentState != STATE_ERROR && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
	}

	public int getBufferPercentage() {
		if (mMediaPlayer != null) {
			return mCurrentBufferPercentage;
		}
		return 0;
	}

	public boolean canPause() {
		return mCanPause;
	}

	public boolean canSeekBackward() {
		return mCanSeekBack;
	}

	public boolean canSeekForward() {
		return mCanSeekForward;
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
	public void stopPlayback() {
		LogTag.i("stopPlayback()");
		StateChange(STATE_STOPBACK);//为统计加入
		if (mMediaPlayer != null) {
			String currentDateStop = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDateStop+"VideoViewH264mp4 stop()");
			if(mOnMediaStateTimeListener!=null){
				mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.STOP, currentDateStop);
			}
			mMediaPlayer.stop();
			String currentDateRelease = Tools.getCurrentDate();
			LetvMediaPlayerManager.getInstance().writePlayLog("系统当前时间:  "+currentDateRelease+"VideoViewH264mp4 release()");
			if(mOnMediaStateTimeListener!=null){
				mOnMediaStateTimeListener.onMediaStateTime(MeidaStateType.RELEASE, currentDateRelease);
			}
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			StateChange(mCurrentState);
			mTargetState = STATE_IDLE;
			setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public View getView() {
		return this;
	}
	
	private void StateChange(int mCurrentState){
		LogTag.i("StateChange(), mCurrentState=" + mCurrentState);
		if(mOnVideoViewStateChangeListener != null){
			mOnVideoViewStateChangeListener.onChange(mCurrentState) ;
		}
	}
	
	@Override
	public void setVideoViewStateChangeListener(OnVideoViewStateChangeListener videoViewListener) {
		mOnVideoViewStateChangeListener = videoViewListener;
	}

	@Override
	public void setOnBufferingUpdateListener(OnBufferingUpdateListener l) {
		this.mOnBufferingUpdateListener = l;
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener l) {
		this.mOnCompletionListener = l;
	}

	@Override
	public void setOnErrorListener(OnErrorListener l) {
		this.mOnErrorListener = l;
	}

	@Override
	public void setOnInfoListener(OnInfoListener l) {
		this.mOnInfoListener = l;
	}

	@Override
	public void setOnPreparedListener(OnPreparedListener l) {
		this.mOnPreparedListener = l;
	}

	@Override
	public void setOnSeekCompleteListener(OnSeekCompleteListener l) {
		this.mOnSeekCompleteListener = l;
	}

	@Override
	public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener l) {
		this.mOnVideoSizeChangedListener = l;
	}

	@Override
	public void setVideoPlayUrl(PlayUrl url) {
		mPlayerUrl = url;
		setVideoURI(Uri.parse(mPlayerUrl.getUrl()));
	}

	@Override
	public void setOnAdNumberListener(OnAdNumberListener l) {
	}

	@Override
	public void setCacheSize(int video_size, int audio_size, int picutureSize, int startpic_size) {
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
