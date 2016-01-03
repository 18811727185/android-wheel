package com.letv.shared.widget.picker;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.*;
import android.widget.LinearLayout;
import com.letv.shared.R;
import com.letv.shared.widget.picker.adapters.DateAdapter;
import com.letv.shared.widget.picker.adapters.DayArrayAdapter;
import com.letv.shared.widget.picker.adapters.NumericWheelAdapter;
import com.letv.shared.widget.picker.adapters.WheelViewAdapter;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * widget datetimewheel
 * @author mengfengxiao@letv.com
 *
 */
public class DateTimeWheel extends LinearLayout {
	public interface OnDateChangedListener {

        /**
         * @param view The view associated with this listener.
         * @param hourOfDay The current hour.
         * @param minute The current minute.
         */
        void onDateChanged(DateTimeWheel view, int year, int month, int day, int hourOfDay, int minute);
    }
	
	private WheelView hours;
	private WheelView mins;
	private WheelView am_pm ;
	private WheelView day;
    private WheelView year;

	private Calendar calendar=null;//inital calendar
	private Calendar mCurrentDate=null;
	private Boolean mIs24HourView;
	private static final int HOURS_IN_HALF_DAY = 12;
	private boolean mIsAm;
	private String[] mAmPmStrings;
    private boolean isLunar = false;
    private String dateFormat;
    private boolean noYearWheel = false;

	 // callbacks
	private OnDateChangedListener dateChangedListener;
    private ViewTreeObserver vto;
    private View dayWrapper,hoursWrapper;


	public DateTimeWheel(Context context) {
		this(context, null);
	}

	public DateTimeWheel(Context context, AttributeSet attrs) {
		super(context, attrs);
        density = context.getResources().getDisplayMetrics().density;
		setOrientation(VERTICAL);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.le_date_time_wheel, this, true);
		calendar = Calendar.getInstance(Locale.CHINA);
		mCurrentDate = Calendar.getInstance(Locale.CHINA);
        if(attrs!=null){
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DateTimeWheel);
            this.noYearWheel = a.getBoolean(R.styleable.DateTimeWheel_leNoYearWheel, false);
            a.recycle();
        }

        if(this.noYearWheel){
            View yearWapper = findViewById(R.id.le_year_wrapper);
            dayWrapper = findViewById(R.id.le_day_wrapper);
            hoursWrapper = findViewById(R.id.le_hours_wrapper);
            yearWapper.setVisibility(GONE);
        }else {
            initYear();
            year = (WheelView)findViewById(R.id.le_year);
            DateAdapter yearAdapter = new DateAdapter(context, yearList);
            year.setCyclic(true);
            year.setViewAdapter(yearAdapter);
            year.setPadding(0,0,0,0);
            year.setCurrentItem(yearIndex);
            year.addChangingListener(new OnWheelChangedListener() {
                @Override
                public void onChanged(WheelView wheel, int oldValue, int newValue) {
                    yearIndex = wheel.getCurrentItem();
                    curYear = yearIndex + MIN_YEAR;
                    mCurrentDate.set(Calendar.YEAR,curYear);
                    if(oldValue!=newValue)
                        onUpdateDate();
                }


            });
        }


		day =  (WheelView)findViewById(R.id.le_day);
		day.setCyclic(true);
		DayArrayAdapter dayAdapter = new DayArrayAdapter(context, calendar);
		day.setViewAdapter(dayAdapter);
        day.setPadding(0,0,0,0);
        if(this.noYearWheel){
            vto = day.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    dayWrapper.setLeft(dayWrapper.getLeft()-dip2px(20));
                    hoursWrapper.setLeft(hoursWrapper.getLeft()-dip2px(20));
                    ViewGroup.LayoutParams lp = day.getLayoutParams();
                    lp.width = dip2px(150);
                    day.setLayoutParams(lp);

                }
            });
        }


		hours = (WheelView) findViewById(R.id.le_hour);
		NumericWheelAdapter hourAdapter = new NumericWheelAdapter(context, 0, 23, "%02d");
		hours.setViewAdapter(hourAdapter);
		hours.setCyclic(true);
        hours.setPadding(0,0,0,0);
		mins = (WheelView) findViewById(R.id.le_mins);
		NumericWheelAdapter minAdapter = new NumericWheelAdapter(context, 0, 59, "%02d");
		mins.setViewAdapter(minAdapter);
		mins.setCyclic(true);
        mins.setPadding(0,0,0,0);
		 /* Get the localized am/pm strings and use them in the spinner */
        String language = context.getResources().getConfiguration().locale.getLanguage();
        if(language.endsWith("en")) {
            mAmPmStrings = new String[2];
            mAmPmStrings[0] = "AM";
            mAmPmStrings[1] = "PM";
        } else {
            mAmPmStrings = new DateFormatSymbols().getAmPmStrings();
        }
		am_pm = (WheelView) findViewById(R.id.le_am_pm);
        DateAdapter am_pm_WheelAdapter = new DateAdapter(context, mAmPmStrings);
		am_pm.setViewAdapter(am_pm_WheelAdapter);
        am_pm.setPadding(0,0,0,0);


		// initialize to current date

		hours.setCurrentItem(calendar.get(Calendar.HOUR_OF_DAY));//24
		mins.setCurrentItem(calendar.get(Calendar.MINUTE));
		am_pm.setCurrentItem(calendar.get(Calendar.AM_PM));

		mIs24HourView = true;
		mIsAm = calendar.get(Calendar.AM)==0?true:false;

		updateAmPmControl();
		updateHourControl();
		//add listener
		hours.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				mCurrentDate.set(Calendar.HOUR_OF_DAY, getCurrentHour());
				if(newValue!=oldValue)
					onUpdateDate();
			}
		});

		mins.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				mCurrentDate.set(Calendar.MINUTE, getCurrentMinute());
				if(newValue!=oldValue)
					onUpdateDate();
			}
		});

		am_pm.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				mCurrentDate.set(Calendar.AM_PM, newValue);
				mIsAm = newValue==0?true:false;
				if(newValue!=oldValue)
					onUpdateDate();
			}
		});

		day.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
			}
            public void onChangedDiff(WheelView wheel, int diff) {
                mCurrentDate.add(Calendar.DAY_OF_YEAR, -diff);
                if(diff!=0)
                    onUpdateDate();
            }
		});




	}

    private static int dip2px(float dp) {

        return (int) (dp * density + 0.5f);
    }
    private static float density;
    private ArrayList yearList;
    private int yearIndex;
    private int curYear;
    private static final int MIN_YEAR = 1970;
    private void initYear(){
        curYear = mCurrentDate.get(Calendar.YEAR);

        yearIndex = curYear - MIN_YEAR;
        yearList = new ArrayList<String>();
        for (int i = MIN_YEAR; i <= DateWheel.MAX_YEAR; i++) {
            yearList.add(String.valueOf(i));
        }
    }
    public WheelView getYearWheelView(){
        return year;
    }

	public WheelView getDayWheelView() {
		return day;
	}

	public WheelView getHoursWheelView() {
		return hours;
	}

	public WheelView getMinsWheelView() {
		return mins;
	}

	public WheelView getAmPmWheelView() {
		return am_pm;
	}

	  /**
     * Set the callback that indicates the time has been adjusted by the user.
     *
     * @param onDateChangedListener the callback, should not be null.
     */
    public void addOnDateChangedListener(OnDateChangedListener onDateChangedListener) {
    	dateChangedListener = onDateChangedListener;
    }
	
	public void setIs24Hours(Boolean is24HourView) {
	     if (mIs24HourView == is24HourView) {
	            return;
	        }
	        mIs24HourView = is24HourView;
	        updateHourControl();
	        updateAmPmControl();
	    	onUpdateDate();
	}
	/**
    * @return The current hour in the range (0-23).
    */
   public Integer getCurrentHour() {
	   int currentHour = hours.getCurrentItem();
	   if (mIs24HourView) {
	          return currentHour;
	      } else if (mIsAm) {
	          return currentHour % HOURS_IN_HALF_DAY;
	      } else {
	         return (currentHour % HOURS_IN_HALF_DAY) + HOURS_IN_HALF_DAY;
	      }
	  //return  mCurrentDate.get(Calendar.HOUR_OF_DAY);
   }
   
   /**
    * @return The current minute.
    */
   public Integer getCurrentMinute() {
	   return mins.getCurrentItem();
	   //return  mCurrentDate.get(Calendar.MINUTE);
   }
   
   /**
    * @return true if this is in 24 hour view else false.
    */
   public boolean is24HourView() {
       return mIs24HourView;
   }
   
   /**
    * Set the current hour.
    */
   private void setCurrentHour(Integer currentHour) {
       setCurrentHour(currentHour, true);
   }

   /**
    * @currentHour currentHour in the range (0-23).
    */
   private void setCurrentHour(Integer currentHour, boolean notifyTimeChanged) {
       // why was Integer used in the first place?
       if (currentHour == null || currentHour == getCurrentHour()) {
           return;
       }
       mCurrentDate.set(Calendar.HOUR_OF_DAY, currentHour);
       if (!is24HourView()) {
           // convert [0,23] ordinal to wall clock display
           if (currentHour >= HOURS_IN_HALF_DAY) {
               mIsAm = false;
               am_pm.setCurrentItem(1);
               currentHour = currentHour%HOURS_IN_HALF_DAY;
           } else {
               mIsAm = true;
               am_pm.setCurrentItem(0);
           }
           updateAmPmControl();
       }
       hours.setCurrentItem(currentHour);
       if (notifyTimeChanged) {
       	onUpdateDate();
       }
   }
   
   private void setCurrentMin(Integer currentMin) {
	   mins.setCurrentItem(currentMin);
	   mCurrentDate.set(Calendar.MINUTE, currentMin);
	   onUpdateDate();
   }
   
   private void updateAmPmControl() {
       if (is24HourView()) {
           if (am_pm != null) {
           	am_pm.setVisibility(View.GONE);
           } else {
           	am_pm.setVisibility(View.GONE);
           }
       } else {
    	   int index = mCurrentDate.get(Calendar.AM_PM);
           if (am_pm != null) {
           	am_pm.setCurrentItem(index);
           	am_pm.setVisibility(View.VISIBLE);
           } 
       }
   }
   private void updateHourControl() {
       int currentHour = getCurrentHour();
	   if (is24HourView()) {
       	NumericWheelAdapter hourAdapter = new NumericWheelAdapter(getContext(), 0, 23, "%02d");
   		hours.setViewAdapter(hourAdapter);
       } else {
       	NumericWheelAdapter hourAdapter = new NumericWheelAdapter(getContext(), 0, 11, "%02d");
   		hours.setViewAdapter(hourAdapter);
       }
       setCurrentHour(currentHour);
   }
   /**
    * @return The selected day of month.
    */
   public int getDayOfMonth() {
       return mCurrentDate.get(Calendar.DAY_OF_MONTH);
   }
   /**
    * @return The selected Year.
    */
   public int getYear() {
       return mCurrentDate.get(Calendar.YEAR);
   }
   /**
    * @return The selected Year.
    */
   public int getMonth() {
       return mCurrentDate.get(Calendar.MONTH);
   }
   
   public void onUpdateDate() {
		int year = mCurrentDate.get(Calendar.YEAR);
	   	int month = mCurrentDate.get(Calendar.MONTH);
		int day = mCurrentDate.get(Calendar.DAY_OF_MONTH);
		int hourOfDay = mCurrentDate.get(Calendar.HOUR_OF_DAY);
		int minute = mCurrentDate.get(Calendar.MINUTE);
		if(dateChangedListener!=null)
			dateChangedListener.onDateChanged(this, year, month, day, hourOfDay, minute);
		else
			return;
   }
   
   public void setCalendar(Calendar cal) {
       setCalendar(cal, false);
   }

    public void setCalendar(Calendar cal, boolean islunar) {
        day.setCurrentItem(0);
        day.setFirstItemUnCyclic(0);
        day.setViewAdapter(new DayArrayAdapter(getContext(), cal));
        WheelViewAdapter adapter = day.getViewAdapter();
        /*if(adapter instanceof DayArrayAdapter)
            ((DayArrayAdapter) adapter).setDateFormat(dateFormat);*/
        calendar = (Calendar) cal.clone();
        mCurrentDate = (Calendar) cal.clone();
        setCurrentHour(mCurrentDate.get(Calendar.HOUR_OF_DAY));
        setCurrentMin(mCurrentDate.get(Calendar.MINUTE));
        updateAmPmControl();
        if(isLunar==islunar)
            return;
        else {
            isLunar = islunar;
            if(adapter instanceof DayArrayAdapter)
                ((DayArrayAdapter) adapter).setIsLunar(islunar);
        }
    }
   
   public void setTextSize(int size) {
	   day.setTextSize(size);
	   hours.setTextSize(size);
	   mins.setTextSize(size);
	   if(am_pm!=null&&!is24HourView()) {
		   am_pm.setTextSize(size);
	   }
   }

    public void setDateFormat(String format) {
        if(format==null||format.isEmpty())
            return;
        dateFormat = format;
        WheelViewAdapter adapter = day.getViewAdapter();
        if(adapter instanceof DayArrayAdapter)
            ((DayArrayAdapter) adapter).setDateFormat(format);
        else
            return;
    }

    /*@Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        int center = day.getHeight() / 2;
        int offset = (int) (day.getItemHeight() / 2 );
        Paint mPaint = new Paint();
        mPaint.setStrokeWidth(1);
        mPaint.setColor(0xF0777777);
        canvas.drawLine(0, center - offset, getWidth(), center - offset, mPaint);
        canvas.drawLine(0, center + offset, getWidth(), center + offset, mPaint);
    }*/

    public boolean getIsLunar() {
        return isLunar;
    }

    public void setCenterItemTextColor(int color) {
        if(day!=null)
            day.setCenterTextColor(color);
        if(am_pm!=null)
            am_pm.setCenterTextColor(color);
        if(hours!=null)
            hours.setCenterTextColor(color);
        if(mins!=null)
            mins.setCenterTextColor(color);
    }

}
