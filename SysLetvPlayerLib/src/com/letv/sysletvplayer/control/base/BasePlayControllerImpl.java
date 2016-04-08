package com.letv.sysletvplayer.control.base;

import com.letv.component.player.Interface.OnMediaStateTimeListener;
import com.letv.component.player.Interface.OnNeedSetPlayParamsListener;
import com.letv.component.player.LetvMediaPlayerControl;
import com.letv.mobile.core.log.Logger;
import com.letv.mobile.core.time.TimeProvider;
import com.letv.mobile.core.utils.DeviceUtils;
import com.letv.sysletvplayer.control.Interface.PlayControlInterface;
import com.letv.sysletvplayer.listener.ControlListener;
import com.letv.sysletvplayer.listener.PlayerListener;
import com.letv.sysletvplayer.setting.CommonScreenSetting;
import com.letv.sysletvplayer.util.PlayUtils;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 播放器播放相关实现
 * @author caiwei
 */
public abstract class BasePlayControllerImpl implements PlayControlInterface {
    private static final String TAG = "BasePlayControllerImpl";
    private static final float sLetvDeviceRatio = 9.0f / 16.0f;

    /**
     * 3d设置类型
     */
    public static enum TYPE3D {
        FRC_3DMODE_FLAG, // 通过3d状态设置
        FRC_3DMODE_2D, // 2d
        FRC_3DMODE_2D_TO_3D, // 模拟2d转3d
        FRC_3DMODE_3D_SIDE_BY_SIDE, // 左右合屏
        FRC_3DMODE_3D_TOP_N_BOTTOM;// 上下合屏
    }

    private final int PREPARE_TIME = 9500;
    private final int BUFFER_TIME = 9501;
    private final int CANPLAY_TIME = 9502;
    private final int PREPARETIME = 1000;// change 3000 to 1000
    private final int PREPARETIMEOLD = 1;
    private final int BUFFERTIME = 500;// 起播时缓冲的低水位
    private final int CANPLAYTIME = 2000;// 起播时缓冲的高水位
    private int curDuration = 0;
    protected LetvMediaPlayerControl mControl;
    private ControlListener mControlListener;
    private PlayerListener mPlayerListener;
    private String videoPath;
    private int mTotalDuration = 0;// 播放总时长
    private int mSpareDuration = 0;// 应用层传递过来的备用时长
    private boolean is3Dflag = false; // 是否是3d影片标识
    private AdjustType mAdjustType = null;// 保存比例类型
    private int mProgress = 0;// 记录随机的buffer百分比
    private long debug_playtimes = 0;
    protected Context mContext;
    protected BufferUpdateCallBack callBack;// buffer的回调

    /**
     * 通用调节画面比例对象的初始化
     */
    protected final CommonScreenSetting mPlayScreenSetting = new CommonScreenSetting() {

        @Override
        public void setLayoutParams(LayoutParams layoutParams) {
            SurfaceView view = (SurfaceView) BasePlayControllerImpl.this.mControl
                    .getView();
            view.setLayoutParams(layoutParams);
        }

        @Override
        public MediaPlayer getMediaPlayer() {
            return BasePlayControllerImpl.this.getMediaPlayer();
        }

        @Override
        public LayoutParams getLayoutParams() {
            SurfaceView view = (SurfaceView) BasePlayControllerImpl.this.mControl
                    .getView();
            return view.getLayoutParams();
        }

        @Override
        public SurfaceHolder getHolder() {
            SurfaceView view = (SurfaceView) BasePlayControllerImpl.this.mControl
                    .getView();
            return view.getHolder();
        }
    };

    public BasePlayControllerImpl(Context context) {
        this.mContext = context;
    }

    // 子类根据type值，设置画面比例
    protected abstract void adjustScreen(int type);

    // 子类对系统bufferUpdate监听的处理
    public void setListenerBufferUpdate(int percent) {
    }

    // 子类对onInfo监听的处理
    public void setOnInfo(int what, int extra) {
    }

    // 子类在prepared后的处理
    public void setOnPrePared() {
    }

    // 子类实现刷新buffer
    public void refreshBuffer() {
    }

    @Override
    public void adjust(AdjustType type) {
        this.mAdjustType = type;
        int adjustType = 0;
        switch (type) {
        case ADJUST_TYPE_AUTO:
            adjustType = 0;
            Logger.d(TAG, "ADJUST_TYPE_AUTO adjustType=" + adjustType);
            break;
        case ADJUST_TYPE_4X3:
            adjustType = 1;
            Logger.d(TAG, "ADJUST_TYPE_4X3 adjustType=" + adjustType);
            break;
        case ADJUST_TYPE_16X9:
            adjustType = 2;
            Logger.d(TAG, "ADJUST_TYPE_16X9 adjustType=" + adjustType);
            break;
        case ADJUST_TYPE_SMART:
            if (this.getMediaPlayer() != null) {
                float videoRatio = (float) this.getMediaPlayer()
                        .getVideoHeight()
                        / this.getMediaPlayer().getVideoWidth();
                adjustType = videoRatio > sLetvDeviceRatio ? 2 : 0;
                Logger.d(TAG, "ADJUST_TYPE_SMART adjustType=" + adjustType
                        + ", vRatio=" + videoRatio + ", dRatio="
                        + sLetvDeviceRatio);
            }
            break;
        }
        this.adjustScreen(adjustType);
    }

    @Override
    public boolean getIs3Dflag() {
        return this.is3Dflag;
    }

    @Override
    public void setIs3Dflag(boolean is3Dflag) {
        this.is3Dflag = is3Dflag;
    }

    @Override
    public void setVideoPath(String path) {
        this.setVideo(path, 0, null);
    }

    @Override
    public void setVideoPath(String path, Map<String, String> headers) {
        this.setVideo(path, 0, headers);
    }

    @Override
    public void setVideoPath(String path, int startPosition) {
        this.setVideo(path, startPosition, null);
    }

    @Override
    public void setVideoPath(String path, int startPosition,
            Map<String, String> headers) {
        this.setVideo(path, startPosition, headers);
    }

    @Override
    public void seekTo(int mDuration) {
        this.curDuration = mDuration;
        this.mControl.seekTo(this.curDuration);
        if (this.mControlListener != null) {
            this.mControlListener.onSeekTo(this.curDuration);
        }
    }

    @Override
    public void start() {
        this.startPlay();
    }

    @Override
    public void play() {
        this.mControl.start();
    }

    @Override
    public void pause() {
        this.pausePlay();
    }

    @Override
    public void stopPlayBack() {
        this.mControl.stopPlayback();
        if (this.mControlListener != null) {
            this.mControlListener.onStopPlayBack();
        }
    }

    @Override
    public void forward(int rate) {
        this.mControl.forward();
    }

    @Override
    public void rewind(int rate) {
        this.mControl.rewind();
    }

    @Override
    public MediaPlayer getMediaPlayer() {
        return this.mControl.getMediaPlayer();
    }

    @Override
    public boolean isPlaying() {
        return this.mControl.isPlaying();
    }

    @Override
    public boolean isPause() {
        return this.mControl.isPaused();
    }

    @Override
    public int getVideoDuration() {
        return this.getTotalDuration();
    }

    @Override
    public int getCurrentPosition() {
        if (this.mControl == null) {
            return 0;
        }
        return this.mControl.getCurrentPosition();
    }

    public int getBufferPercentage() {
        if (this.mControl == null) {
            return 0;
        }
        return this.mControl.getBufferPercentage();
    }

    @Override
    public void setTryPlayTime(int mTraPlayTime) {
        if (this.mControlListener != null) {
            this.mControlListener.onSetTryPlayTime(mTraPlayTime);
        }
    }

    @Override
    public void setSpareDuration(int duration) {
        this.mSpareDuration = duration;
    }

    @Override
    public String getPath() {
        return this.videoPath;
    }

    @Override
    public void resetPlay() {
        this.resetData();
        if (this.mControl != null) {
            this.mControl.stopPlayback();
        }
        this.setPlayerListener(null);
    }

    @Override
    public void setPlayerListener(PlayerListener mPlayerListener) {
        this.mPlayerListener = mPlayerListener;
    }

    public View getPlayView() {
        return this.mControl.getView();
    }

    public void init(ControlListener mControlListener,
            LetvMediaPlayerControl mControl, Context mContext) {
        this.mControl = mControl;
        this.mControlListener = mControlListener;
        this.initBufferCallBack();
        this.initPlayListener();
    }

    public void setVolume(int volume) {
        if (mControl != null) {
            mControl.setVolume(volume);
        }
    }

    /**
     * 重置数据
     */
    public void resetData() {
        this.mTotalDuration = 0;
        this.mSpareDuration = 0;
        this.curDuration = 0;
        this.mProgress = 0;
        this.videoPath = null;
        this.mAdjustType = null;
    }

    /**
     * 获取随机数，用于缓冲，缓冲时随机生成一个累加的<100的缓冲数字
     * @return
     */
    private int getRandomForBuffer(int percent) {
        if (percent > this.mProgress) {
            this.mProgress = percent;
        } else {
            Random random = new Random();
            int randomProgress = random.nextInt(10);
            this.mProgress += randomProgress;
        }
        if (this.mProgress > PlayUtils.RANDOM_PROGRESS) {
            this.mProgress = PlayUtils.RANDOM_PROGRESS;
        } else if (this.mProgress < PlayUtils.MIN_RARANDOM_PROGRESS) {
            this.mProgress = PlayUtils.MIN_RARANDOM_PROGRESS;
        }
        return this.mProgress;
    }

    /**
     * 初始化画面比例值
     */
    private void initScreen() {
        if (this.mAdjustType != null) {
            BasePlayControllerImpl.this.adjust(this.mAdjustType);
            return;
        }
        // TvLive 默认比例为智能比例
        BasePlayControllerImpl.this.adjust(AdjustType.ADJUST_TYPE_SMART);
    }

    // 判断是否全屏
    protected boolean getIsFullScreen() {
        int rootW = this.mContext.getResources().getDisplayMetrics().widthPixels;
        int rootH = this.mContext.getResources().getDisplayMetrics().heightPixels;
        int w = this.getPlayView().getWidth();
        int h = this.getPlayView().getHeight();
        boolean isFullScreen = rootW == w && rootH == h;
        return isFullScreen;
    }

    /**
     * 获取视频总时长
     */
    private int getTotalDuration() {
        if (this.mControl == null) {
            return 0;
        }
        if (this.mTotalDuration == 0) {
            this.mTotalDuration = this.mControl.getDuration();
        }
        if (this.mTotalDuration <= 0 && this.mSpareDuration > 0) {
            // 如果底层获取的时长不正确，且应用层提供了有效备用时长，用备用时长作为视频总时长
            this.mTotalDuration = this.mSpareDuration;
        }

        /*
         * NOTE(caiwei):在获取底层时长错误,并且应用层又未提供有效备用时长的时候，
         * 有可能视频是直播，或者是一个错误值
         * 此处一致作为是直播视频的依据（假设备用时长都是有效的）
         */
        return this.mTotalDuration;
    }

    /**
     * 设置播放路径
     * @param path
     * @param headers
     */
    private void setVideo(String path, int startPosition,
            Map<String, String> headers) {
        this.resetData();
        if (this.mControlListener != null) {
            this.mControlListener.onSetVideoPath(path, headers);
        }
        if (path == null) {
            return;
        }
        this.curDuration = startPosition;
        this.videoPath = path;
        // X60/MAX70的新版rom，起播前设置seek
        if (this.curDuration > 0 && PlayUtils.isNewSeekSelect()) {
            headers = this.setSeekForNewSeek(headers, this.curDuration);
        }
        if (headers == null) {
            this.mControl.setVideoPath(path);
        } else {
            this.mControl.setVideoPath(path, headers);
        }
    }

    private void startPlay() {
        // 不符合起播前设置seek的设备，在起播后设置seek点
        if (BasePlayControllerImpl.this.curDuration > 0
                && !PlayUtils.isNewSeekSelect()) {
            BasePlayControllerImpl.this.mControl
                    .seekTo(BasePlayControllerImpl.this.curDuration);
        }
        this.mControl.start();
    }

    private void pausePlay() {
        if (this.mControl != null && this.mControl.isPlaying()) {
            this.mControl.pause();
            if (this.mControlListener != null) {
                this.mControlListener.onPause();
            }
        }
    }

    /**
     * 初始化buffer的回调
     */
    private void initBufferCallBack() {
        this.callBack = new BufferUpdateCallBack() {
            // 开始缓冲
            @Override
            public void onNeedBuffer() {
                BasePlayControllerImpl.this.mProgress = 0;
                int mBufferPercent = BasePlayControllerImpl.this
                        .getRandomForBuffer(0);
                if (BasePlayControllerImpl.this.mControlListener != null) {
                    BasePlayControllerImpl.this.mControlListener
                            .onNeedBuffer(mBufferPercent);
                }
                if (BasePlayControllerImpl.this.mPlayerListener != null) {
                    BasePlayControllerImpl.this.mPlayerListener
                            .onNeedBuffer(mBufferPercent);
                }
            }

            // 正在缓冲
            @Override
            public void onBufferUpdating(int percent) {
                int mBufferPercent = BasePlayControllerImpl.this
                        .getRandomForBuffer(percent);
                if (BasePlayControllerImpl.this.mControlListener != null) {
                    BasePlayControllerImpl.this.mControlListener
                            .onBufferUpdating(mBufferPercent);
                }
                if (BasePlayControllerImpl.this.mPlayerListener != null) {
                    BasePlayControllerImpl.this.mPlayerListener
                            .onBufferUpdating(mBufferPercent);
                }
            }

            // 缓冲完成
            @Override
            public void onBufferOver() {
                if (BasePlayControllerImpl.this.mControlListener != null) {
                    BasePlayControllerImpl.this.mControlListener.onBufferOver();
                }
                if (BasePlayControllerImpl.this.mPlayerListener != null) {
                    BasePlayControllerImpl.this.mPlayerListener.onBufferOver();
                }
            }
        };
    }

    /**
     * 初始化播放监听
     */
    private void initPlayListener() {
        this.mControl
                .setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
                        // 当前缓冲总进度的回调
                        BasePlayControllerImpl.this
                                .setListenerBufferUpdate(arg1);
                    }
                });
        this.mControl.setOnInfoListener(new OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
                if (BasePlayControllerImpl.this.mPlayerListener != null) {
                    BasePlayControllerImpl.this.mPlayerListener.onInfo(arg1,
                            arg2);
                }
                // 自有设备以此消息判断是否更新buffer进度
                BasePlayControllerImpl.this.setOnInfo(arg1, arg2);
                return false;
            }
        });
        this.mControl.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer arg0) {
                if (BasePlayControllerImpl.this.mPlayerListener != null) {
                    BasePlayControllerImpl.this.mPlayerListener.onCompletion();
                }
            }
        });
        this.mControl.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
                if (BasePlayControllerImpl.this.mPlayerListener != null) {
                    BasePlayControllerImpl.this.mPlayerListener.onError(arg1,
                            arg2);
                }
                return false;
            }
        });
        this.mControl.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer arg0) {
                BasePlayControllerImpl.this.setOnPrePared();
                if (BasePlayControllerImpl.this.mControlListener != null) {
                    BasePlayControllerImpl.this.mControlListener.onPrePared();
                }
                if (BasePlayControllerImpl.this.mPlayerListener != null) {
                    BasePlayControllerImpl.this.mPlayerListener.onPrePared();
                }
            }
        });
        this.mControl.setOnSeekCompleteListener(new OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer arg0) {
                if (BasePlayControllerImpl.this.mPlayerListener != null) {
                    BasePlayControllerImpl.this.mPlayerListener
                            .onSeekComplete();
                }
            }
        });
        this.mControl
                .setOnVideoSizeChangedListener(new OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer arg0, int arg1,
                            int arg2) {
                        if (BasePlayControllerImpl.this.mPlayerListener != null) {
                            BasePlayControllerImpl.this.mPlayerListener
                                    .onVideoSizeChanged(arg0, arg1, arg2);
                } else {
                    // 判断如果playView全屏的时候，初始化调整画面比例
                    boolean isFullScreen = BasePlayControllerImpl.this.getIsFullScreen();
                    if (isFullScreen) {
                        BasePlayControllerImpl.this.initScreen();
                    }
                }
            }
        });
        this.mControl
                .setOnNeedSetPlayParamsListener(new OnNeedSetPlayParamsListener() {
                    @Override
                    public void onNeedSet() {
                        BasePlayControllerImpl.this.setPlayParameter();
                    }
                });

        this.mControl
                .setOnMediaStateTimeListener(new OnMediaStateTimeListener() {
                    @Override
                    public void onMediaStateTime(MeidaStateType stateType,
                            String time) {
                        if (BasePlayControllerImpl.this.mPlayerListener != null) {
                            BasePlayControllerImpl.this.mPlayerListener
                                    .onMediaStateTime(stateType, time);
                        }
                    }
                });
    }

    /**
     * 设置起播前的seek点
     * @param seek
     */
    private Map<String, String> setSeekForNewSeek(Map<String, String> headers,
            int seek) {
        if (headers == null) {
            headers = new HashMap<String, String>();
        }
        headers.put("first-seek", String.valueOf(seek));
        if (this.debug_playtimes > 0) {
            long playtime = TimeProvider.getCurrentMillisecondTime() - this.debug_playtimes;
            // NOTE(xuji):表示从点击播放按钮开始到此时的时间，用于播放器统计请求播放的时间，目前暂时没有用到，后续可能需要加上
            headers.put("request-time", String.valueOf(playtime));
        }
        this.setDebugPlaytimes(0);
        return headers;
    }

    /**
     * 设置debug_playtimes
     * 默认现在为0
     */
    public void setDebugPlaytimes(long debug_playtimes1) {
        this.debug_playtimes = debug_playtimes1;
    }

    /**
     * 设置起播时的高低水位
     */
    private void setPlayParameter() {
        MediaPlayer mMediaPlayer = this.getMediaPlayer();
        if (mMediaPlayer == null) {
            return;
        }
        boolean bufferSelect = PlayUtils.isBufferSelect();
        if (bufferSelect) {
            mMediaPlayer.setParameter(this.PREPARE_TIME, this.PREPARETIME);
            mMediaPlayer.setParameter(this.BUFFER_TIME, this.BUFFERTIME);
        } else if (!DeviceUtils.isOtherDevice()) {
            mMediaPlayer.setParameter(this.PREPARE_TIME, this.PREPARETIMEOLD);
        }
    }

    // 缓冲回调接口
    public interface BufferUpdateCallBack {
        public void onNeedBuffer();

        public void onBufferUpdating(int progress);

        public void onBufferOver();
    }
}
