package com.letv.shared.widget.picker;

/**
 * Wheel changed listener interface.
 * <p>The onChanged() method is called whenever current wheel positions is changed:
 * <li> New Wheel position is set
 * <li> Wheel view is scrolled
 * @author mengfengxiao@letv.com
 */
public abstract class OnWheelChangedListener {
	/**
	 * Callback method to be invoked when current item changed
	 * @param wheel the wheel view whose state has changed
	 * @param oldValue the old value of current item
	 * @param newValue the new value of current item
	 */
	public abstract void onChanged(WheelView wheel, int oldValue, int newValue);
    /**
     * Callback method to be invoked when current item changed
     * @param wheel the wheel view whose state has changed
     * @param diff  the difference between the old value and new value of current item
     */
    public void onChangedDiff(WheelView wheel, int diff){}
}
