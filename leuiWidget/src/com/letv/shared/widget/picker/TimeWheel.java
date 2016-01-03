package com.letv.shared.widget.picker;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.letv.shared.widget.picker.adapters.DateAdapter;
import com.letv.shared.widget.picker.adapters.NumericWheelAdapter;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;
import com.letv.shared.R;
//import com.letv.leui.R;

/**
 * widget timewhell
 * @author mengfengxiao@letv.com
 *
 */

public class TimeWheel extends LinearLayout{

	public interface OnTimeChangedListener {

        /**
         * @param view The view associated with this listener.
         * @param hourOfDay The current hour.
         * @param minute The current minute.
         */
        void onTimeChanged(TimeWheel view, int hourOfDay, int minute);
    }
	
	private WheelView hours;
	private WheelView mins;
	private WheelView am_pm ;

	private Calendar calendar=null;
	private Calendar mCurrentDate=null;

	private int index_min = 0;
	private int index_hour = 0;
	private int index_am_pm = 0;
	private Boolean mIs24HourView;
	private static final int HOURS_IN_HALF_DAY = 12;
	private boolean mIsAm;
	private String[] mAmPmStrings;
	 // callbacks
	private OnTimeChangedListener timeChangedListener;

	public TimeWheel(Context context) {
		this(context, null);
	}
	public TimeWheel(Context context, AttributeSet attrs) {
		super(context, attrs);
        Log.d("test","TimeWheel...");
		setOrientation(VERTICAL);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.le_time_wheel, this, true);
		calendar = Calendar.getInstance(Locale.CHINA);
		mCurrentDate = Calendar.getInstance(Locale.CHINA);


		hours = (WheelView) findViewById(R.id.le_hour);
		NumericWheelAdapter hourAdapter = new NumericWheelAdapter(context, 0, 23, "%02d");
		hours.setViewAdapter(hourAdapter);
		hours.setCyclic(true);

		mins = (WheelView) findViewById(R.id.le_mins);
		NumericWheelAdapter minAdapter = new NumericWheelAdapter(context, 0, 59, "%02d");
		mins.setViewAdapter(minAdapter);
		mins.setCyclic(true);

		/* Get the localized am/pm strings and use them in the spinner */
        String language = context.getResources().getConfiguration().locale.getLanguage();
        if(language.endsWith("zh")) {
            mAmPmStrings = new DateFormatSymbols().getAmPmStrings();
        } else {
            mAmPmStrings = new String[2];
            mAmPmStrings[0] = "AM";
            mAmPmStrings[1] = "PM";
        }
		am_pm = (WheelView) findViewById(R.id.le_am_pm);
        DateAdapter am_pm_WheelAdapter = new DateAdapter(context, mAmPmStrings);
		am_pm.setViewAdapter(am_pm_WheelAdapter);
        am_pm.setCyclic(true);

		// initialize to current date
		hours.setCurrentItem(calendar.get(Calendar.HOUR));
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
                    onUpdateTime();
			}
            public void onChangedDiff(WheelView wheel, int diff) {
            }
		});

		mins.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				mCurrentDate.set(Calendar.MINUTE, getCurrentMinute());
				if(newValue!=oldValue)
                    onUpdateTime();
			}
            public void onChangedDiff(WheelView wheel, int diff) {
            }
		});

		am_pm.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				mCurrentDate.set(Calendar.AM_PM, newValue);
				mIsAm = newValue==0?true:false;
				if(newValue!=oldValue)
					onUpdateTime();
			}
            public void onChangedDiff(WheelView wheel, int diff) {
            }
		});
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
     * @param onTimeChangedListener the callback, should not be null.
     */
    public void addOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
    	timeChangedListener = onTimeChangedListener;
    }
    
	public void setIs24Hours(Boolean is24HourView) {
	     if (mIs24HourView == is24HourView) {
	            return;
	        }
	        mIs24HourView = is24HourView;
	        updateHourControl();
	        updateAmPmControl();
	        onUpdateTime();
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
    }

    /**
     * @return The current minute.
     */
     public Integer getCurrentMinute() {
        return mins.getCurrentItem();
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
      	onUpdateTime();
      }
  }

  private void setCurrentMin(Integer currentMin) {
      mins.setCurrentItem(currentMin);
	  mCurrentDate.set(Calendar.MINUTE, currentMin);
	  onUpdateTime();
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
  
  public void onUpdateTime() {
	  int hourOfDay = mCurrentDate.get(Calendar.HOUR_OF_DAY);
	  int minute = mCurrentDate.get(Calendar.MINUTE);
	   if(timeChangedListener!=null)
		   timeChangedListener.onTimeChanged(this, hourOfDay, minute);
	   else
		   return;
  }

  public void setCalendar(Calendar cal) {
      calendar = (Calendar) cal.clone();
      mCurrentDate = (Calendar) cal.clone();
      setCurrentHour(mCurrentDate.get(Calendar.HOUR_OF_DAY));
      setCurrentMin(mCurrentDate.get(Calendar.MINUTE));
      updateAmPmControl();
  }

    public void setTextSize(int size) {
        hours.setTextSize(size);
        mins.setTextSize(size);
        if(am_pm!=null&&!is24HourView()) {
            am_pm.setTextSize(size);
        }
    }

    public void setCenterItemTextColor(int color) {
        if(am_pm!=null)
            am_pm.setCenterTextColor(color);
        if(hours!=null)
            hours.setCenterTextColor(color);
        if(mins!=null)
            mins.setCenterTextColor(color);
    }

   /* @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if(hours==null)
            return;
        else {
            int center = hours.getHeight() / 2;
            int offset = (int) (hours.getItemHeight() / 2 );
            Paint mPaint = new Paint();
            mPaint.setStrokeWidth(1);
            mPaint.setColor(0xF0777777);
            canvas.drawLine(0, center - offset, getWidth(), center - offset, mPaint);
            canvas.drawLine(0, center + offset, getWidth(), center + offset, mPaint);
        }
    }*/
}
