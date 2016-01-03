package com.letv.component.player;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.view.View;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

import com.letv.component.player.Interface.OnMediaStateTimeListener;
import com.letv.component.player.Interface.OnNeedSetPlayParamsListener;
import com.letv.component.player.Interface.OnVideoViewStateChangeListener;
import com.letv.component.player.core.PlayUrl;
import com.media.ffmpeg.FFMpegPlayer.OnAdNumberListener;
import com.media.ffmpeg.FFMpegPlayer.OnBlockListener;
import com.media.ffmpeg.FFMpegPlayer.OnCacheListener;
import com.media.ffmpeg.FFMpegPlayer.OnFirstPlayLitener;
import com.media.ffmpeg.FFMpegPlayer.OnHardDecodeErrorListner;

import java.util.Map;

/**
 * 播放器实现接口
 * @author chenyueguo
 */
public interface LetvMediaPlayerControl extends MediaPlayerControl {
	
	
	/**
	 * 方式一，传入PlayUrl
	 * @param url
	 */
	public void setVideoPlayUrl(PlayUrl url);

	/**
	 * 方式二，设置视频路径。
	 */
	public void setVideoPath(String videoPath);
	
	/**
	 * 方式三，设置视频路径,带头信息。TV端使用
	 */
	public void setVideoPath(String videoPath, Map<String, String> headers);
	
	/**
	 * 视频停止播放
	 */
	public void stopPlayback();
	
	/**
	 * 快进，modile和tv端快进单位不同,moblie每次快进15000ms，tv每次快进20000ms
	 */
	public void forward(); //快进
	
	/**
	 * 快退，modile和tv端快进单位不同,moblie每次快退15000ms，tv每次快退20000ms
	 */
	public void rewind(); //快退
	
	/**
     * 自适应屏幕 -1-初始化状态 0-自动 1-4:3 2-16:9
     */
    public void adjust(int type);
	
	/**
	 * 返回view对象，用于填充到布局。
	 */
	public View getView();
	
	/**
	 * 获取播放器销毁前时间点。
	 */
	public int getLastSeekWhenDestoryed();
	
	/**
	 * 判断播放器是否为暂停状态。
	 */
	public boolean isPaused();
	
	/**
	 * 判断是否在可控状态，
	 */
	public boolean isInPlaybackState();
	
	/**
	 * 设置媒体控制器。
	 */
	public void setMediaController(MediaController controller);
	
	/**
	 * 注册一个回调函数，在视频预处理完成后调用。此时视频的宽度、高度、宽高比信息已经获取到，此时可调用seekTo让视频从指定位置开始播放。
	 */
	public void setOnPreparedListener(OnPreparedListener l);
	
	/**
	 * 注册一个回调函数，视频播放完成后调用。
	 */
	public void setOnCompletionListener(OnCompletionListener l);
	
	/**
	 * 注册一个回调函数，在有警告或错误信息时调用。例如：开始缓冲、缓冲结束、下载速度变化。
	 */
	public void setOnInfoListener(OnInfoListener l);
	
	/**
	 * 注册一个回调函数，在异步操作调用过程中发生错误时调用。例如视频打开失败。
	 */
	public void setOnErrorListener(OnErrorListener l);
	
	/**
	 * 注册一个回调函数，在seek操作完成后调用。
	 */
	public void setOnSeekCompleteListener(OnSeekCompleteListener l);
	
	/**
	 * 注册一个回调函数，在获取视频大小或视频大小改变时调用。
	 */
	public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener l);
	
	/**
	 * 注册一个回调函数，在网络视频流缓冲变化时调用。
	 */
	public void setOnBufferingUpdateListener(OnBufferingUpdateListener l);
	
	/**
	 * 注册一个回调函数，在视频状态信息改变时回调。该回调是兼容移动端原来接口。
	 */
	public void setVideoViewStateChangeListener(OnVideoViewStateChangeListener videoViewStateChangeListener);
	
	/**
	 * 注册一个回调函数，通知上层当前播放到哪一个广告
	 */
	public void setOnAdNumberListener(OnAdNumberListener l);
	
	/**
	 * 注册一个回调函数，通知上层当前卡顿了
	 */
	public void setOnBlockListener(OnBlockListener l);
	
	/**
	 * 注册一个回调函数，通知上层缓存操作
	 * lhq 2015-06-15
	 */
	public void setOnCacheListener(OnCacheListener l);
	
	/**
	 * lhq 2015-06-17
	 */
	public void setOnFirstPlayListener(OnFirstPlayLitener l);
	
	/**
	 * 注册回调函数，通知上层当前播放器状态时间点
	 */
	public void setOnMediaStateTimeListener(OnMediaStateTimeListener l);
	
	/**
	 * 硬解播放播放失败
	 */
	public void setOnHardDecodeErrorListener(OnHardDecodeErrorListner l);
	
	/**以下四个方法，在播广告时，强制停止正片播放**/
	public boolean isEnforcementWait();

	public void setEnforcementWait(boolean enforcementWait);

	public boolean isEnforcementPause();

	public void setEnforcementPause(boolean enforcementPause);
	
	public void setCacheSize(int video_size,int audio_size,int picutureSize, int startpic_size);
	
	/*全景视频相关接口，panorama add by qinshenglin 20150811*/
	
	/*全景视频相关接口，add by qinshenglin 20150811*/
	
	//通知component player 当前要播放的片源的类型,0是普通片源，1是全景片源
	public int setSourceType(int sourceType);
	
	//屏幕分辨率,可能根据机型适配的需要,需要传入(0.0)点的位置.现在尚未发现异常机型.正常机型左下角的位置为坐标系(0,0)点
	public int setMachineInfomation(float ScreenResolution);
	
	//单指touch屏幕,1次触发,开始的坐标begin_x,begin_y,结束的坐标end_x,end_y.
	public int setOneFingertouchInfomation(float begin_x,float begin_y,float end_x,float end_y);
	
	//双指touch屏幕,1次触发,开始的两个手指的坐标begin_x0,begin_y0,begin_x1,begin_y1,结束时两个手指的坐标end_x0,end_y0,end_x1,end_y1.
	public int setTwoFingertouchInfomation(float begin_x0,float begin_y0,float begin_x1,float begin_y1,float end_x0,float end_y0,float end_x1,float end_y1);
	
	//传入陀螺仪获取的值gravity_yro_x,gravity_yro_y,gravity_yro_z
	public int setgravity_yroInfomation(float gravity_yro_x,float gravity_yro_y,float gravity_yro_z);
	
	//传入重力感应器获取的值gravity_x,gravity_y,gravity_z
	public int setGravityInfomation(float gravity_x,float gravity_y,float gravity_z);
	
	//传入陀螺仪是否有效的开关状态
	public int setgravity_yroValidInfomation(boolean gravityValid);
	
	//用户touch屏幕上的归零按钮,要求旋转角度归零
	public int setAngleInit();
	
	//用户touch缩放比例
	public int setTwoFingerZoom(float zoom);
	
	/*通知播放器，当前播放的视频是全景视频*/
	
	/**
     * 设置初始化播放器开始的时间点，单位毫秒，用于走CDE且设置跳过片头片尾时，直接从实际播放点初始化播放器
     */
    public void setInitPosition(int msec);
    
    /**
     * 设置播放器是否静音，0：静音，非 0：播放器采用系统音量
     */
	public void setVolume(int volume);

	/**
	 *硬解码需要返回mediaplayer进行一些设置
	 * changed for tvlive by zanxiaofei 2015-10-28
	 */
	public MediaPlayer getMediaPlayer();

	/**
	 * 硬解码需要设置播放高低水位
	 * changed for tvlive by zanxiaofei 2015-10-30
	 * @param listener
	 */
	public void setOnNeedSetPlayParamsListener(OnNeedSetPlayParamsListener listener);
}
