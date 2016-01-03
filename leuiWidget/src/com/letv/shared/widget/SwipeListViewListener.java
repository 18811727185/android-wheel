package com.letv.shared.widget;

/**
 * Listener to get callback notifications for the SwipeListView
 */
public interface SwipeListViewListener extends LeListViewListener {

    /**
     * Called when open animation finishes
     * @param position list item
     * @param toRight Open to right
     */
    void onOpened(int position, boolean toRight);

    /**
     * Called when close animation finishes
     * @param position list item
     * @param fromRight Close from right
     */
    void onClosed(int position, boolean fromRight);

    /**
     * Called when the list changed
     */
    void onListChanged();

    /**
     * Called when user is moving an item
     * @param position list item
     * @param x Current position X
     */
    void onMove(int position, float x);

    /**
     * Start open item
     * @param position list item
     * @param action current action
     * @param right to right
     */
    void onStartOpen(int position, int action, boolean right);

    /**
     * Start close item
     * @param position list item
     * @param right
     */
    void onStartClose(int position, boolean right);

    /**
     * Used when user want to change swipe list mode on some rows. Return SWIPE_MODE_DEFAULT
     * if you don't want to change swipe list mode
     * @param position position that you want to change
     * @return type
     */
    int onChangeSwipeMode(int position);

    /**
     * User is in first item of list
     */
    void onFirstListItem();

    /**
     * User is in last item of list
     */
    void onLastListItem();

}
