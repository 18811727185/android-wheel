package com.letv.dynamicconfig;

import com.letv.dynamicconfig.manager.DynamicConfigManager;
import com.letv.dynamicconfig.manager.DynamicConfigManager.OnConfigChangeListener;
import com.letv.dynamicconfig.manager.DynamicConfigManager.OnGlobalDynamicConfigChangeListener;
import com.letv.mobile.core.log.Logger;

/**
 * I am not a SDK but a complete set of dynamic config ecosystem.
 * <p/>
 * This class provide common interface for all dynamic config function.
 * Created by shibin on 16/4/19.
 */
public class DynamicConfigUtils {

    private static final String TAG = "DynamicConfigUtils";

    private static String sTerminalApplication = "";
    private static String sBsChannel = "";
    private static String sTerminalBrand = "";
    public static boolean isInited = false;

    private static DynamicConfigManager sManager = DynamicConfigManager.getInstance();

    /**
     * Init dynamic module. Should be called when application onCreate.
     */
    public static void init(String terminalApplication, String bsChannel, String terminalBrand) {
        Logger.i(TAG, "DynamicConfig init, terminalApplication=" + terminalApplication + ", " +
                "bsChannel=" + bsChannel + ", terminalBrand=" + terminalBrand);
        sTerminalApplication = terminalApplication;
        sBsChannel = bsChannel;
        sTerminalBrand = terminalBrand;
        sManager.init();
        isInited=true;
    }

    /**
     * Call this function to sync config automatically by specified policy.
     */
    public static void startSyncConfig() {
        sManager.startSyncConfig();
    }

    /**
     * If version check out of date, call this function by hand.
     */
    public static void updateConfig() {
        sManager.updateConfig();
    }

    /**
     * Returns the config value by the specified key.
     * If not has contains key, return default value.
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static boolean getBooleanConfig(String key, boolean defaultValue) {
        return getConfigValue(key, Boolean.class, defaultValue);
    }

    /**
     * Returns the config value by the specified key.
     * If not has contains key, return default value.
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getStringConfig(String key, String defaultValue) {
        return getConfigValue(key, String.class, defaultValue);
    }

    /**
     * Returns the config value by the specified key.
     * If not has contains key, return default value.
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static int getIntConfig(String key, int defaultValue) {
        return getConfigValue(key, Integer.class, defaultValue);
    }

    /**
     * Returns the config value by the specified key.
     *
     * @param key
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T getConfigValue(String key, Class<T> type) {
        return getConfigValue(key, type, null);
    }

    /**
     * Returns the config value by the specified key.
     * If not has contains key, return default value.
     *
     * @param key
     * @param type
     * @param defaultValue
     * @param <T>
     * @return
     */
    public static <T> T getConfigValue(String key, Class<T> type, T defaultValue) {
        return sManager.getConfigValue(key, type, defaultValue);
    }

    /**
     * Returns ths config version.
     *
     * @return
     */
    public static String getConfigVersion() {
        return sManager.getConfigVersion();
    }

    /**
     * Registers a callback to be invoked when a change happens to your specified key.
     *
     * @param keys     The list of keys you want to listen these changed, It is not effected if
     *                 pass null value
     * @param listener
     */
    public static void registerOnConfigChangeListener(OnConfigChangeListener listener, String...
            keys) {
        sManager.registerOnConfigChangeListener(listener, keys);
    }

    /**
     * Registers a callback to be invoked when a change happens to dynamic configs.
     * @param listener
     */
    public static void registerOnGlobalDynamicConfigChangeListener(OnGlobalDynamicConfigChangeListener listener) {
        sManager.registerOnGlobalDynamicConfigListener(listener);
    }

    /**
     * Unregisters a previous callback.
     *
     * @param listener
     */
    public static void unregisterOnConfigChangeListener(OnConfigChangeListener listener) {
        sManager.unregisterOnConfigChangeListener(listener);
    }

    public static String getTerminalApplication() {
        return sTerminalApplication;
    }

    public static String getBsChannel() {
        return sBsChannel;
    }

    public static String getTerminalBrand() {
        return sTerminalBrand;
    }
}
