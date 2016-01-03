package com.letv.shared.widget;

/**
 * Listener to get callback notifications for the SwipeView
 */
public interface SwipeListViewSwitchListener {

    /**
     * Reach the switch line.
     * @param isChanged true is on, otherwise is off.
     */
    void onSwitching(int position, boolean isChanged);
    
    /**
     * switch is done. isSwithced is result.
     * @param isSwitched true is on, otherwise is off.
     */
    void onSwitched(int position, boolean isSwitched);

}
