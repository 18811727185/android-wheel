package com.letv.sysletvplayer.listener;

/**
 * 系统的回调，提供给应用层作出对应响应实现的接口
 * @author caiwei
 */
public interface SystemInfoListener {

    /**
     * 系统时间改变
     */
    public void onTimeChange();

    /**
     * 系统电量改变
     * @param status
     *            电量状态
     * @param curPower
     *            电量值
     */
    public void onBatteryChange(int status, int curPower);

    /**
     * 网络变化
     */
    public void onNetChange();

    /**
     * 开屏
     */
    public void onScreenOn();

    /**
     * 锁屏
     */
    public void onScreenOff();

    /**
     * 解锁
     */
    public void onUserPersent();
}
