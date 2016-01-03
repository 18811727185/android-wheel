package com.letv.shared.widget.picker.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * Numeric Wheel adapter.
 * @author mengfengxiao@letv.com
 */
public class NumericWheelAdapter extends AbstractWheelTextAdapter {

	/** The default min value */
	public static final int DEFAULT_MAX_VALUE = 9;

	/** The default max value */
	private static final int DEFAULT_MIN_VALUE = 0;

	// Values
	private int minValue;
	private int maxValue;

	// format
	private String format;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            the current context
	 */
	public NumericWheelAdapter(Context context) {
		this(context, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 *            the current context
	 * @param minValue
	 *            the wheel min value
	 * @param maxValue
	 *            the wheel max value
	 */
	public NumericWheelAdapter(Context context, int minValue, int maxValue) {
		this(context, minValue, maxValue, null);
		//setItemResource(R.layout.time2_day);
		
		//setItemTextResource(R.id.time2_monthday);
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 *            the current context
	 * @param minValue
	 *            the wheel min value
	 * @param maxValue
	 *            the wheel max value
	 * @param format
	 *            the format string
	 */
	public NumericWheelAdapter(Context context, int minValue, int maxValue, String format) {
        this(context, minValue, maxValue, format, true);
	}

    public NumericWheelAdapter(Context context, int minValue, int maxValue, String format, boolean isVertical) {
        super(context);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.format = format;
        super.setOritentation(isVertical);
    }

	@Override
    public CharSequence getItemText(int index) {
        if (index >= 0 && index < getItemsCount()) {
            int value = minValue + index;
            if(format!=null)
            	return String.format(format, value);
            else
            	return Integer.toString(value);
        }
        return null;
    }

//    public int getMinValue() {
//        return minValue;
//    }
//
//    public int getMaxValue() {
//        return maxValue;
//    }

    @Override
	public int getItemsCount() {
		return maxValue - minValue + 1;
	}
	@Override
	public View getItem(int index, View cachedView, ViewGroup parent) {
		View view = super.getItem(index, cachedView, parent);
		return view;
	}

}
