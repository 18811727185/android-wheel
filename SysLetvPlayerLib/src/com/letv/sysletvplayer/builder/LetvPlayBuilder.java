package com.letv.sysletvplayer.builder;

import com.letv.component.player.LetvMediaPlayerControl;
import com.letv.component.player.LetvVideoViewBuilder;
import com.letv.component.player.LetvVideoViewBuilder.Type;
import com.letv.component.player.core.LetvMediaPlayerManager;
import com.letv.mobile.core.log.Logger;
import com.letv.mobile.core.utils.DeviceUtils;
import com.letv.mobile.core.utils.DeviceUtils.DeviceType;
import com.letv.sysletvplayer.R;
import com.letv.sysletvplayer.control.Interface.ListDataControlInterface;
import com.letv.sysletvplayer.control.Interface.ListPlayControlInterface;
import com.letv.sysletvplayer.control.Interface.PlayControlInterface;
import com.letv.sysletvplayer.control.Interface.ViewControlInterface;
import com.letv.sysletvplayer.control.ListDataControllerImpl;
import com.letv.sysletvplayer.control.ListPlayControllerImpl;
import com.letv.sysletvplayer.control.PlayControllerImplForC1;
import com.letv.sysletvplayer.control.PlayControllerImplForCommon;
import com.letv.sysletvplayer.control.PlayControllerImplForS250;
import com.letv.sysletvplayer.control.PlayControllerImplForS50;
import com.letv.sysletvplayer.control.PlayControllerImplForX60;
import com.letv.sysletvplayer.control.ViewControllerImpl;
import com.letv.sysletvplayer.control.base.BasePlayControllerImpl;
import com.letv.sysletvplayer.control.base.BaseViewLayer;
import com.letv.sysletvplayer.listener.ControlListener;
import com.letv.sysletvplayer.listener.SystemInfoListener;
import com.letv.sysletvplayer.setting.BufferSetting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.ViewGroup;

/**
 * 视频播放实例化入口
 * 供应用层调用的单例类
 * 提供播放界面总布局，
 * 提供PlayControlInterface实例（播放控制）、
 * ViewControlInterface实例(界面布局控制)、
 * ListDataControlInterface实例（列表数据操作）、
 * ListPlayControlInterface实例（列表播放控制），
 * 以及提供接口用以设置与播放相关的listener监听
 * @author caiwei
 */
public class LetvPlayBuilder {
    /****** 设备playcontrol对象的类型 start **********/
    private static final String PLAY_TYPE_X60 = "X60";
    private static final String PLAY_TYPE_S50 = "S50";
    private static final String PLAY_TYPE_S250 = "S250";
    private static final String PLAY_TYPE_C1 = "C1";
    /****** 设备playcontrol对象的类型 end **********/

    private ViewGroup mViewGroup;
    private BaseViewLayer mViewLayer;
    private ViewControllerImpl mViewControl;
    private BasePlayControllerImpl mPlayControl;
    private ListDataControllerImpl mListDataControl;
    private ListPlayControllerImpl mListPlayControl;
    private SystemInfoListener mSystemInfoListener;
    private Context mContext;
    private DeviceType mDeviceType;
    private LetvMediaPlayerControl mControl;

    public LetvPlayBuilder() {
    }

    /**
     * 单个视频播放的初始化
     * 供应用层调用：初始化播放控制类、View布局（默认的布局样式）
     * 返回ViewGroup对象，
     * 包含播放器界面、进度条界面等，
     * 应用层需要添加ViewGroup对象至自己的界面
     */
    public ViewGroup build(Context mContext, Type type) {
        LetvMediaPlayerManager.getInstance().init(mContext, "appKey", "appid", "pCode", "appVer");
        this.mContext = mContext;
        this.mDeviceType = DeviceUtils.getDeviceType();
        this.mControl = LetvVideoViewBuilder.getInstants().build(mContext, type);
        this.initView(mContext);
        this.initControl();
        this.registerSysReceiver();
        return this.mViewGroup;
    }

    /**
     * 单个视频播放的初始化
     * 传入自定义继承自BaseViewLayer的类,
     * 用以修改默认View的布局样式
     */
    public ViewGroup build(Context mContext, BaseViewLayer viewLayer, Type type) {
        this.mViewLayer = viewLayer;
        return this.build(mContext, type);
    }

    /**
     * 单个视频播放的初始化
     * 传入自定义继承自ViewControllerImpl的类,
     * 用以修改按键或播放响应逻辑
     */
    public ViewGroup build(Context mContext, ViewControllerImpl viewControl, Type type) {
        this.mViewControl = viewControl;
        return this.build(mContext, type);
    }

    /**
     * 单个视频播放的初始化
     * 传入自定义继承自BaseViewLayer、ViewControllerImpl的类,
     * 用以修改默认View的布局样式、按键或播放响应逻辑
     */
    public ViewGroup build(Context mContext, BaseViewLayer viewLayer,
            ViewControllerImpl viewControl, Type type) {
        this.mViewLayer = viewLayer;
        this.mViewControl = viewControl;
        return this.build(mContext, type);
    }

    /**
     * 返回列表数据的接口
     * (实例化列表数据类)
     */
    public ListDataControlInterface getListDataController() {
        if (this.mListDataControl == null) {
            this.mListDataControl = new ListDataControllerImpl();
        }
        return this.mListDataControl;
    }

    /**
     * 返回列表播放控制的接口
     * (实例化列表数据类、实例化列表播放控制类)
     */
    public ListPlayControlInterface getListPlayController() {
        if (this.mListPlayControl == null) {
            this.getListDataController();
            this.mListPlayControl = new ListPlayControllerImpl(this.mContext, this.mPlayControl,
                    this.mViewControl, this.mListDataControl);
        }
        return this.mListPlayControl;
    }

    /**
     * 设置对系统层的回调监听
     */
    public void setSystemInfoListener(SystemInfoListener mSystemInfoListener) {
        this.mSystemInfoListener = mSystemInfoListener;
    }

    /**
     * 获取对播放控制的控制对象
     */
    public PlayControlInterface getBasePlayController() {
        return this.mPlayControl;
    }

    /**
     * 获取对播放界面的控制对象
     */
    public ViewControlInterface getBaseViewController() {
        return this.mViewControl;
    }

    public void hideOtherHardSurface(SurfaceView selfView) {
        LetvVideoViewBuilder.getInstants().hideOtherHardSurface(selfView);
    }

    /**
     * 销毁视频播放的所有设置
     */
    public void resetBuild() {
        this.unregisterSysReceiver();
        if (this.mPlayControl != null) {
            this.mPlayControl.resetPlay();
            mPlayControl = null;
        }
        if (this.mPlayControl != null) {
            this.mViewControl.resetView();
            mViewControl = null;
        }
        if (this.mListDataControl != null) {
            // 如果有列表数据，销毁列表数据
            this.mListDataControl.resetData();
            this.mListDataControl.clear();
            mListDataControl = null;
        }

        if (this.mListPlayControl != null) {
            // 如果有列表播放，销毁列表播放设置
            this.mListPlayControl.resetPlayList();
            mListPlayControl = null;
        }
        this.mViewGroup = null;
        this.mViewLayer = null;
        mControl = null;
        mContext = null;

        this.setSystemInfoListener(null);

        BufferSetting.unitInstance();
    }

    /**
     * 初始化布局
     * @param mContext
     */
    private void initView(Context mContext) {
        this.mViewGroup = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.play_layout,
                null);
    }

    /**
     * 初始化各种control对象
     */
    private void initControl() {
        this.mPlayControl = this.getPlayControl();
        if (this.mViewControl == null) {
            this.mViewControl = new ViewControllerImpl();
        }
        ControlListener mControlListener = this.mViewControl.getControlListener();
        this.mPlayControl.init(mControlListener, this.mControl, this.mContext);
        this.mViewLayer = this.mViewControl.init(this.mViewGroup, this.mPlayControl,
                this.mViewLayer, this.mContext);
    }

    /**
     * 根据不同的设备类型，返回对应的子playcontrol对象
     * @return
     */
    private BasePlayControllerImpl getPlayControl() {
        String type = DeviceUtils.getDevice().getLetvPlayView();
        Logger.i("Buffer", "type:" + type);
        if (PLAY_TYPE_X60.equals(type)) {
            return new PlayControllerImplForX60(this.mContext);
        } else if (PLAY_TYPE_S50.equals(type)) {
            return new PlayControllerImplForS50(this.mContext);
        } else if (PLAY_TYPE_S250.equals(type)) {
            return new PlayControllerImplForS250(this.mContext);
        } else if (PLAY_TYPE_C1.equals(type)) {
            return new PlayControllerImplForC1(this.mContext);
        } else {
            return new PlayControllerImplForCommon(this.mContext);
        }
    }

    /**
     * 注册关于监听 电量，时间，网络状态的监听
     */
    private void registerSysReceiver() {
        try {
            if (this.mContext != null && this.mBroadcastReceiver != null) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                filter.addAction(Intent.ACTION_TIME_TICK);
                filter.addAction(Intent.ACTION_BATTERY_CHANGED);
                filter.addAction(Intent.ACTION_SCREEN_ON);
                filter.addAction(Intent.ACTION_SCREEN_OFF);
                filter.addAction(Intent.ACTION_USER_PRESENT);
                this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 取消关于监听 电量，时间，网络状态的监听
     */
    private void unregisterSysReceiver() {
        if (this.mContext != null && this.mBroadcastReceiver != null) {
            try {
                this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 监听网络，电量，时间
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {// 监听每分钟的时间变化
                if (LetvPlayBuilder.this.mSystemInfoListener != null) {
                    LetvPlayBuilder.this.mSystemInfoListener.onTimeChange();
                }
            } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {// 监听网络连接状态变化
                if (LetvPlayBuilder.this.mSystemInfoListener != null) {
                    LetvPlayBuilder.this.mSystemInfoListener.onNetChange();
                }
            } else if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {// 监听电量变化
                int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);// 获得电池状态
                int level = intent.getExtras().getInt("level", 0);// 获得当前电量
                int scale = intent.getExtras().getInt("scale", 100);// 获得总电量
                int curPower = level * 100 / scale;
                if (LetvPlayBuilder.this.mSystemInfoListener != null) {
                    LetvPlayBuilder.this.mSystemInfoListener.onBatteryChange(status, curPower);
                }
            } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {// 开屏
                if (LetvPlayBuilder.this.mSystemInfoListener != null) {
                    LetvPlayBuilder.this.mSystemInfoListener.onScreenOn();
                }
            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {// 锁屏
                if (LetvPlayBuilder.this.mSystemInfoListener != null) {
                    LetvPlayBuilder.this.mSystemInfoListener.onScreenOff();
                }
            } else if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {// 解锁
                if (LetvPlayBuilder.this.mSystemInfoListener != null) {
                    LetvPlayBuilder.this.mSystemInfoListener.onUserPersent();
                }
            }
        }
    };

    public BaseViewLayer getViewLayer() {
        return mViewLayer;
    }

    public void setViewLayer(BaseViewLayer mViewLayer) {
        this.mViewLayer = mViewLayer;
    }

}
