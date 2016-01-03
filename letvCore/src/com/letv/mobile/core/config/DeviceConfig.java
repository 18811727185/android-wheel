package com.letv.mobile.core.config;

import android.content.res.AssetManager;

import com.alibaba.fastjson.JSON;
import com.letv.mobile.core.app.LetvCoreApp;
import com.letv.mobile.core.log.Logger;
import com.letv.mobile.core.utils.IOUtils;
import com.letv.mobile.core.utils.StringUtils;
import com.letv.tv.core.device.DeviceInfo;
import com.letv.tv.core.device.DeviceModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 用于解析DeviceConfig.json
 * @author wenpeirong
 */
public class DeviceConfig {
    private static final String LETV_CONFIG_FILE_NAME = "DeviceConfig.json";
    private static DeviceConfig sConfig = null;

    private static final String TYPE_OTHER = "other";

    private static List<DeviceInfo> mDevices = new ArrayList<DeviceInfo>();
    private static Map<String, DeviceInfo> mDeviceMap = new HashMap<String, DeviceInfo>();
    private static DeviceInfo mDeviceInfo = null;

    private static DeviceConfig getInstance() {
        if (sConfig == null) {
            synchronized (DeviceConfig.class) {
                if (sConfig == null) {
                    sConfig = new DeviceConfig();
                }
            }
        }
        return sConfig;
    }

    private DeviceConfig() {
        this.init();
    }

    public void init() {
        AssetManager am = LetvCoreApp.getApplication().getAssets();
        ByteArrayOutputStream baos = null;
        InputStream is = null;
        String configString = null;
        try {
            is = am.open(LETV_CONFIG_FILE_NAME);
            baos = new ByteArrayOutputStream();
            int i;
            while ((i = is.read()) != -1) {
                baos.write(i);
            }
            configString = baos.toString();
            baos.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeStream(baos);
            IOUtils.closeStream(is);
            Logger.i("DeviceConfig", "configString = " + configString);

        }
        DeviceModel model = JSON.parseObject(configString, DeviceModel.class);
        this.mDevices = model.getDevices();
        for (DeviceInfo device : mDevices) {
            mDeviceMap.put(device.getType(), device);
        }
    }

    private DeviceInfo getDevice(String type) {
        if (mDeviceInfo != null) {
            return mDeviceInfo;
        }
        if (StringUtils.isBlank(type)) {
            mDeviceInfo = mDeviceMap.get(TYPE_OTHER);
        } else {
            type = type.toLowerCase(Locale.getDefault());
            mDeviceInfo = mDeviceMap.get(type);
            if (mDeviceInfo == null) {
                mDeviceInfo = mDeviceMap.get(TYPE_OTHER);
            }
        }

        return mDeviceInfo;

    }

    public static DeviceInfo getDeviceByType(String type) {

        return getInstance().getDevice(type);
    }
}
