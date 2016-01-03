
package com.letv.shared.widget.picker.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import com.letv.shared.R;
/**
 * @author mengfengxiao@letv.com
 */

public class DateAdapter extends AbstractWheelTextAdapter {
	/**
	 * Constructor
	 */
	public ArrayList<String> list;

	public DateAdapter(Context context, ArrayList<String> list) {
		super(context, R.layout.le_vertical_wheel_text_item, NO_RESOURCE);
		this.list = list;
	}

    public DateAdapter(Context context, String[] list) {
        super(context, R.layout.le_vertical_wheel_text_item, NO_RESOURCE);
        stringToArrayList(list);
    }

	@Override
	public View getItem(int index, View cachedView, ViewGroup parent) {
		View view = super.getItem(index, cachedView, parent);

		TextView textCity = (TextView) view.findViewById(R.id.text);
		textCity.setText(list.get(index));
		return view;
	}

	public int getItemsCount() {
		return list.size();
	}

	@Override
	public CharSequence getItemText(int index) {
		return list.get(index);
	}

    private void stringToArrayList(String[] str) {
        if(this.list==null)
            this.list = new ArrayList<String>();
        if(!this.list.isEmpty())
            this.list.clear();
        for(int i=0; i<str.length; i++)
            this.list.add(str[i]);
    }
}