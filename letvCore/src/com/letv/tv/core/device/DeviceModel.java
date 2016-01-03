package com.letv.tv.core.device;

import java.util.List;

/**
 * 设备列表Model
 * @author wenpeirong
 */
public class DeviceModel {
    private List<DeviceInfo> devices;

    public List<DeviceInfo> getDevices() {
        return this.devices;
    }

    public void setDevices(List<DeviceInfo> devices) {
        this.devices = devices;
    }
}
