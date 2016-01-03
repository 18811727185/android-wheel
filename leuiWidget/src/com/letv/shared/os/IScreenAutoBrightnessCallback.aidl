package com.letv.shared.os;

/**
 * screen auto brightness change callback
 * use oneway, so power no need wait client
 * @author fengzihua
 * {@hide}
 */
oneway interface IScreenAutoBrightnessCallback {
    /**
     * when receive timeout messages
     * notify client to fetch data
     * @param nowELAPSED the current systemclock time
     */
    void onBrightnessChange(in int brightness, in long nowELAPSED);
    
    /**
     * notify client begin or stop animation
     * @param start
     * @param currentBrightness
     * @param targetBrightness
     */
    void onAnimationStatusChange(in boolean start, in int currentBrightness, in int targetBrightness);
}