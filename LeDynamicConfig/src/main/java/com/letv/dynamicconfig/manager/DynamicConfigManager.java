package com.letv.dynamicconfig.manager;

import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSON;
import com.letv.dynamicconfig.http.GetConfigRequest;
import com.letv.dynamicconfig.http.GetConfigResponse;
import com.letv.dynamicconfig.http.HttpCommonParameter;
import com.letv.mobile.async.TaskCallBack;
import com.letv.mobile.core.config.LeTVConfig;
import com.letv.mobile.core.log.Logger;
import com.letv.mobile.core.utils.ContextProvider;
import com.letv.mobile.core.utils.FileUtils;
import com.letv.mobile.core.utils.HandlerUtils;
import com.letv.mobile.core.utils.ThreadUtils;
import com.letv.mobile.http.bean.CommonResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dynamic config all logic handle class.
 * Created by shibin on 16/4/19.
 */
public class DynamicConfigManager {

    private final String TAG = "DynamicConfigManager";
    private final String CACHE_FILE_NAME = "le_dynamic_config.cache";
    private final int MSG_READ_CACHE_SUCCESS = 1;

    private String mConfigVersion = "";
    private JSONObject mConfigInfo; // all module config json data
    private Map<String, List<OnConfigChangeListener>> mKeyListeners;
    private OnGlobalDynamicConfigChangeListener mOnGlobalDynamicConfigChangeListener;

    private static DynamicConfigManager sInstance = new DynamicConfigManager();

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Logger.i(TAG, "handleMessage, what=" + msg.what);
            switch (msg.what) {
                case MSG_READ_CACHE_SUCCESS:
                    notifyConfigData((String) msg.obj, false);
                    break;
                default:
                    break;
            }
        }
    };

    private DynamicConfigManager() {
        mKeyListeners = new HashMap<>();
    }

    public static DynamicConfigManager getInstance() {
        return sInstance;
    }

    public void init() {
        readFromCache();
    }

    public void startSyncConfig() {
        Logger.i(TAG, "startSyncConfig");
        fetchConfigFromServer();
    }

    public void updateConfig() {
        fetchConfigFromServer();
    }

    public String getConfigVersion() {
        return mConfigVersion;
    }

    /**
     * Deal get config value logic.
     *
     * @param key
     * @param type
     * @param defaultValue
     * @param <T>
     * @return
     */
    public <T> T getConfigValue(String key, Class<T> type, T defaultValue) {
        T result = null;
        if (mConfigInfo != null && mConfigInfo.has(key)) {
            try {
                result = JSON.parseObject(mConfigInfo.getString(key), type);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else { // If not contains key, return default value.
            result = defaultValue;
        }
        if (result == null) {
            try {
                result = type.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Registers a callback to be invoked when a change happens to your specified key.
     *
     * @param keys
     * @param listener
     */
    public void registerOnConfigChangeListener(OnConfigChangeListener listener, String... keys) {
        if (keys == null || keys.length == 0) {
            Logger.i(TAG, "registerOnConfigChangeListener keys is null");
            return;
        }
        for (String key : keys) {
            if (mKeyListeners.containsKey(key)) {
                mKeyListeners.get(key).add(listener);
            } else {
                List<OnConfigChangeListener> list = new ArrayList<>();
                list.add(listener);
                mKeyListeners.put(key, list);
            }
        }
    }

    /**
     * Unregisters a previous callback.
     *
     * @param listener
     */
    public void unregisterOnConfigChangeListener(OnConfigChangeListener listener) {
        for (List<OnConfigChangeListener> list : mKeyListeners.values()) {
            list.remove(listener);
        }
    }

    private void fetchConfigFromServer() {
        new GetConfigRequest(ContextProvider.getApplicationContext(), new TaskCallBack() {
            @Override
            public void callback(int code, String msg, String errorCode, Object object) {
                if (code == TaskCallBack.CODE_OK && object instanceof CommonResponse) {
                    GetConfigResponse response = ((CommonResponse<GetConfigResponse>) object)
                            .getData();
                    Logger.i(TAG, "fetch config success, configInfo=" + response.getConfigInfo());
                    mConfigVersion = response.getVersion();
                    notifyConfigData(response.getConfigInfo(), true);
                } else {
                    Logger.i(TAG, "fetch config failure");
                }
            }
        }).execute(new HttpCommonParameter().combineParams());
    }

    private void notifyConfigData(final String content, final boolean needSaveCache) {
        Logger.i(TAG, "notifyConfigData content=" + content);
        // this place will be cost much time, use multi thread.
        ThreadUtils.startRunInSingleThread(new Runnable() {
            @Override
            public void run() {
                try {
                    long startTime = System.currentTimeMillis();
                    JSONObject jsonData = new JSONObject(content);
                    List<String> keys = new ArrayList<>(mKeyListeners.keySet());
                    for (int i = keys.size() - 1; i >= 0; i--) { // remove unchanged keys
                        String key = keys.get(i);
                        if (mConfigInfo != null && jsonData.has(key) && mConfigInfo.has(key)) {
                            if (jsonData.getString(key).equals(mConfigInfo.getString(key))) {
                                keys.remove(key);
                            }
                        } else if (!jsonData.has(key) && (mConfigInfo == null || !mConfigInfo.has
                                (key))) {
                            keys.remove(key);
                        }
                    }
                    mConfigInfo = jsonData;
                    onGlobalDynamicChange(jsonData);

                    for (String key : keys) { // notify changed keys
                        notifyListenersByKey(key);
                    }

                    long endTime = System.currentTimeMillis();
                    Logger.i(TAG, "compare changed use time = " + (endTime - startTime));

                    if (needSaveCache && keys.size() > 0) {
                        saveToCache();
                    }
                } catch (JSONException e) {
                    Logger.i(TAG, "notifyConfigData, content is not json string or not has target" +
                            " keys");
                    e.printStackTrace();
                }
            }
        });
    }

    public void onGlobalDynamicChange(JSONObject jsonData){
        if (jsonData != null) {
            if (mOnGlobalDynamicConfigChangeListener != null) {
                mOnGlobalDynamicConfigChangeListener.onDynamicConfigChanged(jsonData);
            }
        }
    }

    private void notifyListenersByKey(final String key) {
        Logger.i(TAG, "notifyListenersByKey, key=" + key);
        // We always notify observer in ui thread
        HandlerUtils.getUiThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mKeyListeners.containsKey(key)) {
                    List<OnConfigChangeListener> list = mKeyListeners.get(key);
                    for (OnConfigChangeListener listener : list) {
                        listener.onConfigChanged(key);
                    }
                }
            }
        });
    }

    private void saveToCache() {
        Logger.i(TAG, "saveToCache");
        if (mConfigInfo == null) {
            Logger.i(TAG, "saveToCache failure, mConfigInfo is null");
            return;
        }
        ThreadUtils.startRunInThreadForClearQueue(new Runnable() {
            @Override
            public void run() {
                FileUtils.write(mConfigInfo.toString(), getCacheFilePath());
            }
        });
    }

    private void readFromCache() {
        Logger.i(TAG, "readFromCache start");
        ThreadUtils.startRunInSingleThread(new Runnable() {
            @Override
            public void run() {
                File cacheFile = new File(getCacheFilePath());
                if (cacheFile.exists() && cacheFile.length() > 0) {
                    String content = FileUtils.read(getCacheFilePath());
                    Logger.i(TAG, "readFromCache success, content=" + content);
                    Message msg = mHandler.obtainMessage(MSG_READ_CACHE_SUCCESS, content);
                    mHandler.sendMessage(msg);
                } else {
                    Logger.i(TAG, "cache file is not exists");
                }
            }
        });

    }

    private String getCacheFilePath() {
        return LeTVConfig.getNoSdCardPath() + CACHE_FILE_NAME;
    }

    public void registerOnGlobalDynamicConfigListener(OnGlobalDynamicConfigChangeListener onGlobalDynamicConfigChangeListener){
        this.mOnGlobalDynamicConfigChangeListener=onGlobalDynamicConfigChangeListener;
    }
    /**
     * Interface definition for a callback to be invoked when only this key-config is changed.
     * Created by shibin on 16/4/28.
     */
    public interface OnConfigChangeListener {

        /**
         * Called when a shared preference is changed.
         * This callback will be run on your main thread.
         *
         * @param key  The key of the config that was changed.
         */
        void onConfigChanged(String key);

    }

    /**
     * Interface definition for a callback to be invoked when all dynamic configs are changed.
     * Created by shibin on 16/4/28.
     */
    public interface OnGlobalDynamicConfigChangeListener {

        void onDynamicConfigChanged(JSONObject object);

    }

}
