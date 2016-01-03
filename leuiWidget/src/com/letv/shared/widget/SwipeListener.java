package com.letv.shared.widget;

/**
 * Listener to get callback notifications for the SwipeListView
 */
public interface SwipeListener {

    /**
     * Called when open animation finishes
     * 
     * @param toRight Open to right
     */
    void onOpened(boolean toRight);

    /**
     * Called when close animation finishes
     * 
     * @param fromRight Close from right
     */
    void onClosed(boolean fromRight);

    /**
     * Called when user is moving an item
     * 
     * @param x Current position X
     */
    void onMove(float x);

    /**
     * Start open item
     * 
     * @param action current action
     * @param right to right
     */
    void onStartOpen(int action, boolean right);

    /**
     * Start close item
     * 
     * @param right
     */
    void onStartClose(boolean right);
    
    /**
     * Used when user want to change swipe list mode on some rows. Return SWIPE_MODE_DEFAULT
     * if you don't want to change swipe list mode
     * @return type
     */
    int onChangeSwipeMode();

}
