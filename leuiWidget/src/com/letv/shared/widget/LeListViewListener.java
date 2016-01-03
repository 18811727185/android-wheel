package com.letv.shared.widget;

public interface LeListViewListener {

    /**
     * Called when user dismisses items
     * @param reverseSortedPositions Items dismissed
     */
    public void onDismiss(int[] reverseSortedPositions);

}
