package com.letv.shared.widget.picker.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.letv.shared.pim.lunar.Lunar;
import com.letv.shared.widget.picker.WheelView;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import com.letv.shared.R;
/**
 * Day adapter
 * @author mengfengxiao@letv.com
 */
public class DayArrayAdapter  extends AbstractWheelTextAdapter {
	// Count of days to be shown
	private int daysCount;
	// Calendar
	Calendar calendar;
	Calendar curCalendar;
	WheelView whellView;
	private String dateFormat;
	private boolean isLunar = false;
    private Lunar lunar;

	/**
	 * Constructor
	 */
	public DayArrayAdapter(Context context,  Calendar calendar) {
        this(context, calendar, true);
	}

    /**
     * Constructor
     */
    public DayArrayAdapter(Context context,  Calendar calendar, boolean isVertical) {
        super(context, R.layout.le_vertical_wheel_text_item, NO_RESOURCE);
        this.calendar = (Calendar) calendar.clone();
        this.curCalendar = (Calendar) calendar.clone();
        super.setOritentation(isVertical);
        int year = this.calendar.get(Calendar.YEAR);
        setYear(year);
        if(dateFormat==null||dateFormat.isEmpty())
            dateFormat = context.getString(R.string.date_format);
            dateFormat = "MM.dd";
        if(isZh(context)){
            lunar = Lunar.getInstance();
        }

    }
    private boolean isZh(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        String country = locale.getCountry().toLowerCase();
        return language.endsWith("zh");
    }
	/**
	 * Constructor
	 */
	@Override
	public View getItem(int index, View cachedView, ViewGroup parent) {
		View view = super.getItem(index, cachedView, parent);
		return view;
	}

	@Override
	public int getItemsCount() {
		return daysCount;
	}

	@Override
	public CharSequence getItemText(int index) {
		Calendar newCalendar = (Calendar) calendar.clone();
		newCalendar.add(Calendar.DAY_OF_YEAR, index);
        String date = "";
        if(isLunar) {
            lunar.setDate(newCalendar);
            //date = lunar.toString();
            String year, month, day, week;
            year = month = day = week = "";
            if(dateFormat.indexOf("y")>=0) {
                year = lunar.getYear();
                date += year;
            }
            if(dateFormat.indexOf("M")>=0){
                month = lunar.getMonth();
                date += month;
            }
            if(dateFormat.indexOf("d")>=0){
                day = lunar.getDay();
                date += day;
            }
            if (dateFormat.indexOf("E")>=0){
                DateFormat format = new SimpleDateFormat("E");
                String w = format.format(newCalendar.getTime());
                week = w;
                date += week;
            }

        } else {
            DateFormat format = new SimpleDateFormat(dateFormat);
            date = format.format(newCalendar.getTime());
        }

        return date;
	}
	
	public void setDateFormat(String formate) {
		dateFormat = formate;
	}
	
	//vivian
	public void setYear(int year) {
		if(year%4==0&&year%100!=0)
			daysCount = 366;
		else
			daysCount = 365;
	}
	
	public WheelView getWhellView() {
		return whellView;
	}

	public void setWhellView(WheelView whellView) {
		this.whellView = whellView;
	}

    public void setIsLunar(boolean isluanr) {
        if(isLunar==isluanr)
            return;
        else
            isLunar = isluanr;
    }

}
