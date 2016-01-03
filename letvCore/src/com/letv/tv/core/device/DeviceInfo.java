package com.letv.tv.core.device;

/**
 * 设备信息Model
 * @author wenpeirong
 */
public class DeviceInfo {
    private String name; // type通过name归类，某些type（机型）不同但处理逻辑完全一样的设备可以有相同的name属性，比方说Letv
                         // U2和Letv U3的name值都为“DEVICE_U2”
    private String type; // 从LetvManager.getLetvModel()获取的type，每个设备唯一对应于一个type值，为了统一处理都为小写
    private String model; // 终端terminalSeries型号
    private int keyCode; // 对应列表键，如果没有默认为-1
    private String letvPlayView; // 对应PlayView，默认为“”
    private boolean isLetvBox; // 是否为乐视盒子
    private boolean isBufferPolicyForBox; // 是否使用和第三方相同的缓冲策略
    private boolean is3DPermitted; // 是否允许3D码流
    private boolean isAnimationPermitted; // 是否允许动画
    private boolean isAudioCtrlPermitted; // 是否弹出音量调节
    private boolean isSupportFirstSeek; // 是否支持FirstSeek
    private boolean isSupportPreBuffering; // 是否支持"零秒起播

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getModel() {
        return this.model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getKeyCode() {
        return this.keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public String getLetvPlayView() {
        return this.letvPlayView;
    }

    public void setLetvPlayView(String letvPlayView) {
        this.letvPlayView = letvPlayView;
    }

    public boolean isLetvBox() {
        return this.isLetvBox;
    }

    public void setLetvBox(boolean isLetvBox) {
        this.isLetvBox = isLetvBox;
    }

    public boolean isBufferPolicyForBox() {
        return this.isBufferPolicyForBox;
    }

    public void setBufferPolicyForBox(boolean isBufferPolicyForBox) {
        this.isBufferPolicyForBox = isBufferPolicyForBox;
    }

    public boolean isIs3DPermitted() {
        return this.is3DPermitted;
    }

    public void setIs3DPermitted(boolean is3dPermitted) {
        this.is3DPermitted = is3dPermitted;
    }

    public boolean isAnimationPermitted() {
        return this.isAnimationPermitted;
    }

    public void setAnimationPermitted(boolean isAnimationPermitted) {
        this.isAnimationPermitted = isAnimationPermitted;
    }

    public boolean isAudioCtrlPermitted() {
        return this.isAudioCtrlPermitted;
    }

    public void setAudioCtrlPermitted(boolean isAudioCtrlPermitted) {
        this.isAudioCtrlPermitted = isAudioCtrlPermitted;
    }

    public boolean isSupportFirstSeek() {
        return this.isSupportFirstSeek;
    }

    public void setSupportFirstSeek(boolean isSupportFirstSeek) {
        this.isSupportFirstSeek = isSupportFirstSeek;
    }

    public boolean isSupportPreBuffering() {
        return this.isSupportPreBuffering;
    }

    public void setSupportPreBuffering(boolean isSupportPreBuffering) {
        this.isSupportPreBuffering = isSupportPreBuffering;
    }

}
