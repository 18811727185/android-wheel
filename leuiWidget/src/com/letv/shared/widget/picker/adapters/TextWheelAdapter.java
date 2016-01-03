package com.letv.shared.widget.picker.adapters;

import android.content.Context;

/**
 * Numeric Wheel adapter.
 * @author mengfengxiao@letv.com
 */
public class TextWheelAdapter extends AbstractWheelTextAdapter {
	
	/** The default min value */
	private String[] strContents;
	/**
	 * constructor
	 * @param strContents
	 */
	public TextWheelAdapter(Context context, String[] strArray) {
        this(context, strArray, true);
	}

    public TextWheelAdapter(Context context, String[] strArray, boolean isVertical) {
        super(context);
        this.strContents = strArray;
        setOritentation(isVertical);
    }
	
	public String[] getStrContents() {
		return strContents;
	}


	public void setStrContents(String[] strContents) {
		this.strContents = strContents;
	}

	@Override
	public CharSequence getItemText(int index) {
		if (index >= 0 && index < getItemsCount()) {
			return strContents[index];
		}
		return null;
	}
	
	public int getItemsCount() {
		return strContents.length;
	}
	
	/**
	 * set max width
	 */
	public int getMaximumLength() {
		if(null==strContents)
			return 0;
		else {
			int count = getItemsCount();
			int len_start = strContents[0].length();
			int len_max = len_start;
			for(int i=0; i<count; i++) {
				len_max = strContents[i].length()>len_max ? strContents[i].length() : len_max;
			}
			return len_max;
		}
	}
}
