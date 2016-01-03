package com.letv.shared.widget.picker;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import com.letv.shared.R;
import com.letv.shared.pim.lunar.Lunar;
import com.letv.shared.widget.picker.adapters.DateAdapter;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by mengfengxiao@letv.com on 14-10-23.
 */
public class DateWheel extends LinearLayout{

    public interface OnDateChangedListener {

        /**
         * @param view The view associated with this listener.
         */
        void onDateChanged(DateWheel view, int year, int month, int day);
    }

    private WheelView yearView;
    private WheelView monthView;
    private WheelView dayView;

    private DateAdapter yearAdapter;
    private DateAdapter monthAdapter;
    private DateAdapter dayAdapter;

    private Resources res;
    private Context context;
    private Calendar mCalendar;
    private Calendar mTimedDate;
    private boolean isLunar = false;
    private Lunar lunar;
    private boolean isTimed = false;
    private int initYear, initMonth, initDay;
    private int curYear, curMonth, curDay;

    private int yearIndex, monthIndex, dayIndex;
    public static int MIN_YEAR = 1950;
    public static int MAX_YEAR = 2037;
    private ArrayList<String> yearList, monthList, dayList;
    private int mLeapMonth;

    private String[] months, days;
    private String[] mLunarMonthName;
    private String year = "", month = "", day = "";
    private Date date;
    boolean current = false;
    String pattern = "/";
    private boolean isEnglish = false;
    private String[] months_en, months_zh, days_zh, days_en;

    public ArrayList<String> getYearList() {
        return yearList;
    }

    public ArrayList<String> getMonthList() {
        return monthList;
    }

    public ArrayList<String> getDayList() {
        return dayList;
    }

    public void setMinYear(int min) {
        this.MIN_YEAR = min;
    }

    public void setMaxYear(int max) {
        this.MAX_YEAR = max;
    }
    private OnDateChangedListener dateChangedListener;

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public WheelView getYear() {
        return yearView;
    }

    public WheelView getMonth() {
        return monthView;
    }

    public WheelView getDay() {
        return dayView;
    }

    public DateWheel(Context context) {
        super(context);
        initViews();
    }

    public DateWheel(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public DateWheel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews();
    }

    /**
     * Set the callback that indicates the time has been adjusted by the user.
     *
     * @param onDateChangedListener the callback, should not be null.
     */
    public void addOnDateChangedListener(OnDateChangedListener onDateChangedListener) {
        dateChangedListener = onDateChangedListener;
    }

    public void setCalendar(Calendar calendar) {
        setCalendar(calendar, false);
    }

    public void setCalendar(Calendar calendar, boolean islunar) {
        setCalendar(calendar,islunar, false);
    }

    public void setCalendar(Calendar calendar, boolean islunar, boolean istimed) {
        if(islunar&&isEnglish)
            return;
        if(istimed) {
            boolean isSameDate = isSameDate(calendar, mTimedDate);
            if(isSameDate&&islunar==this.isLunar&&istimed==this.isTimed||islunar)
                return;
            this.mTimedDate /*= this.mCalendar*/ = calendar;
        } /*else {
            this.mCalendar = calendar;
        }*/
        this.isLunar = islunar;
        this.isTimed = istimed;
        initData();
    }

    public void setIsTimed(boolean istimed) {
        if(isLunar)
            return;
        else
            this.isTimed = istimed;
        initData();
    }

    public boolean getIsTimed() {
        return isTimed;
    }

    private void initViews() {
        // TODO Auto-generated method stub
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.le_date_wheel, this, true);
        res = getContext().getResources();
        context = getContext();
        mCalendar = Calendar.getInstance();
        mTimedDate = Calendar.getInstance();
        String language = getContext().getResources().getConfiguration().locale.getLanguage();
        if(language.endsWith("en")){
            isEnglish = true;
        } else if(language.endsWith("zh")) {
            isEnglish = false;
        }

        this.date = mCalendar.getTime();
        if(isEnglish) {
            yearView = (WheelView) findViewById(R.id.le_day);
            monthView = (WheelView) findViewById(R.id.le_month);
            dayView = (WheelView) findViewById(R.id.le_year);
        } else {
            yearView = (WheelView) findViewById(R.id.le_year);
            monthView = (WheelView) findViewById(R.id.le_month);
            dayView = (WheelView) findViewById(R.id.le_day);
        }
        months_zh = res.getStringArray(R.array.le_months);
        months_en = res.getStringArray(R.array.le_months_en);
        days_zh =  res.getStringArray(R.array.le_days_31);
        days_en = new String[31];
        for(int i=1; i<=31; i++)
            days_en[i-1] = String.valueOf(i);
        initData();
        initAction();
        yearView.setVisibleItems(5);
        monthView.setVisibleItems(5);
        dayView.setVisibleItems(5);
    }

    private void initAction() {
        yearView.addChangingListener(new OnWheelChangedListener() {
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                yearIndex = wheel.getCurrentItem();
                curYear = yearIndex + MIN_YEAR;
                int year = yearIndex + MIN_YEAR;
                int month = monthIndex + 1;
                if(!isLunar) {
                    if(isTimed) {
                        if(year==initYear) {
                            monthList.clear();
                            if(isEnglish)
                                for (int i = initMonth; i < 12; i++)
                                    monthList.add(months_en[i]);
                            else
                                for (int i = initMonth; i < 12; i++)
                                    monthList.add(months_zh[i]);
                            monthIndex = curMonth<initMonth ? 0 : curMonth-initMonth;
                            curMonth = curMonth<initMonth ? initMonth : curMonth;
                            monthAdapter = new DateAdapter(context, monthList);
                            monthView.setViewAdapter(monthAdapter);
                            monthView.setCurrentItem(monthIndex);
                            //update day
                            if(curMonth==initMonth) {
                                modifyInitDayView(year, curMonth);
                                dayAdapter = new DateAdapter(context, dayList);
                                dayView.setViewAdapter(dayAdapter);
                                dayView.setCurrentItem(dayIndex);
                                dayView.setCyclic(false);
                            }
                            yearView.setCurrentItem(0);
                            monthView.setCyclic(false);
                        } else {
                            if(monthList.size()!=12) {
                                if(isEnglish)
                                    stringToArrayList(monthList, months_en);
                                else
                                    stringToArrayList(monthList, months_zh);
                            }
                            monthIndex = curMonth;
                            month = monthIndex + 1;
                            modifyDayView(year, month);
                            dayAdapter = new DateAdapter(context, dayList);
                            dayView.setViewAdapter(dayAdapter);
                            dayView.setCurrentItem(dayIndex);
                            monthView.setCyclic(true);
                            dayView.setCyclic(true);
                        }
                        monthAdapter = new DateAdapter(context, monthList);
                        monthView.setViewAdapter(monthAdapter);
                        monthView.setCurrentItem(monthIndex);
                    } else {
                        modifyDayView(year, month);
                        dayAdapter = new DateAdapter(context, dayList);
                        dayView.setViewAdapter(dayAdapter);
                        dayView.setCurrentItem(dayIndex);
                    }
                   
                } else {
                    //lunar
                    int leapMonth = lunar.leapMonth(year);
                    if(mLeapMonth!=leapMonth) {
                        mLeapMonth = leapMonth;
                        if (mLeapMonth <= 0 || mLeapMonth > 12) {
                            if (monthList.size() != 12)
                                stringToArrayList(monthList, months);
                        } else {
                            if (!monthList.isEmpty())
                                monthList.clear();
                            String run = res.getString(R.string.le_status_leap);
                            String str = run + months[mLeapMonth - 1];
                            for (int i = 0; i < mLeapMonth; i++)
                                monthList.add(months[i]);
                            monthList.add(str);
                            for (int i = mLeapMonth; i < 12; i++)
                                monthList.add(months[i]);
                        }
                    }
                    monthAdapter = new DateAdapter(context, monthList);
                    monthView.setViewAdapter(monthAdapter);

                    // day
                    int days_num = 0;
                    if(mLeapMonth<=0||mLeapMonth>12) {
                        days_num = lunar.monthDays(year, monthIndex+1);
                    } else {
                        if(monthIndex==mLeapMonth)
                            days_num = lunar.leapDays(year);
                    }
                    if(dayList.size()!=days_num) {
                        if(dayList.size()>days_num) {
                            if(dayIndex==dayList.size()-1)
                                dayIndex = 0;
                            dayList.remove(dayList.size()-1);
                        }
                        else if(dayList.size()<days_num)
                            dayList.add(res.getString(R.string.le_status_sanshi));
                    }
                    dayAdapter = new DateAdapter(context, dayList);
                    dayView.setViewAdapter(dayAdapter);
                }
                onUpdateDate();
            }
        });

        monthView.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                monthIndex = wheel.getCurrentItem();
                curMonth = monthIndex;

                int year = yearIndex + MIN_YEAR;
                int i = monthIndex + 1;
                if(!isLunar) {
                    if(isTimed && year==initYear) {
                        i = monthIndex + initMonth + 1;
                        curMonth = monthIndex + initMonth;
                    }

                    if(!isTimed || year!= initYear || i!=(initMonth+1)) {
                        modifyDayView(year, i);
                        dayView.setCyclic(true);
                    } else {
                        modifyInitDayView(year, i-1);
                        dayView.setCyclic(false);
                    }
                    dayAdapter = new DateAdapter(context, dayList);
                    dayView.setViewAdapter(dayAdapter);
                    dayView.setCurrentItem(dayIndex);
                } else {
                    //lunar
                    int days_num = 0;
                    if(mLeapMonth<=0||mLeapMonth>12) {
                        days_num = lunar.monthDays(year, monthIndex+1);
                    } else {
                        if(monthIndex==mLeapMonth)
                            days_num = lunar.leapDays(year);
                        else if(monthIndex<mLeapMonth)
                            days_num = lunar.monthDays(year, monthIndex+1);
                        else if(monthIndex>mLeapMonth)
                            days_num = lunar.monthDays(year, monthIndex);
                    }
                    if(dayList.size()!=days_num) {
                        if(dayList.size()>days_num) {
                            if(dayIndex==dayList.size()-1)
                                dayIndex = 0;
                            dayList.remove(dayList.size()-1);
                        }
                        else if(dayList.size()<days_num)
                            dayList.add(res.getString(R.string.le_status_sanshi));
                    }
                    dayAdapter = new DateAdapter(context, dayList);
                    dayView.setViewAdapter(dayAdapter);
                }
                onUpdateDate();
            }
        });

        dayView.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                dayIndex = wheel.getCurrentItem();
                if(isTimed) {
                    if(curYear==initYear&&curMonth == initMonth)
                        curDay = dayIndex + initDay;
                    else
                        curDay = dayIndex+1;
                }
                else
                    curDay= dayIndex + 1;
                onUpdateDate();
            }
        });
    }

    public static boolean isLeapYear(String str) {
        int year = Integer.parseInt(str);
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
    }

    public static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
    }
	
	public boolean isSameDate(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR)==b.get(Calendar.YEAR)
                &&a.get(Calendar.MONTH)==b.get(Calendar.MONTH)
                &&a.get(Calendar.DAY_OF_MONTH)==b.get(Calendar.DAY_OF_MONTH);
    }

    public void initData() {
        // TODO Auto-generated method stub
         if(mCalendar.get(Calendar.YEAR)>MAX_YEAR||mCalendar.get(Calendar.YEAR)<MIN_YEAR)
            return;

        yearList = new ArrayList<String>();
        monthList = new ArrayList<String>();
        dayList = new ArrayList<String>();
        boolean isSameDate = isSameDate(mCalendar, mTimedDate);

        initYear = mCalendar.get(Calendar.YEAR);
        initMonth = mCalendar.get(Calendar.MONTH);
        initDay = mCalendar.get(Calendar.DAY_OF_MONTH);

        if(isTimed) {
            MIN_YEAR = initYear;
        }

        if(isEnglish) {
            for (int i = MIN_YEAR; i <= MAX_YEAR; i++) {
                yearList.add(String.valueOf(i));
            }
        } else {
            for (int i = MIN_YEAR; i <= MAX_YEAR; i++) {
                yearList.add(String.valueOf(i)+res.getString(R.string.le_status_year));
            }
        }

        //calculate index
        if(!isLunar) {
            if(isSameDate) {
                curYear = initYear;
                curMonth = initMonth;
                curDay = initDay;
            } else {
                curYear = mTimedDate.get(Calendar.YEAR);
                curMonth =  mTimedDate.get(Calendar.MONTH);
                curDay = mTimedDate.get(Calendar.DAY_OF_MONTH);
            }
            yearIndex = curYear - MIN_YEAR;
            if(isTimed) {
                if (isSameDate) {
                    monthIndex = dayIndex = 0;
                } else {
                    if(yearIndex==0) {
                        monthIndex = curMonth - initMonth;
                        if(monthIndex==0)
                            dayIndex = curDay - initDay;
                        else
                            dayIndex = curDay - 1;
                    } else {
                        monthIndex = curMonth;
                        dayIndex = curDay - 1;
                    }
                }
            } else {
                monthIndex = initMonth;
                dayIndex = initDay - 1;
            }
            //month list
            if(isTimed && yearIndex==0) {
                if(isEnglish) {
                    for(int i=initMonth; i<12; i++)
                        monthList.add(months_en[i]);
                } else {
                    for(int i=initMonth; i<12; i++)
                    monthList.add(months_zh[i]);
                }
            }
            else {
                if(isEnglish)
                    months = months_en;
                else
                    months = months_zh;
                stringToArrayList(monthList, months);
            }
            //day list
            int year = yearIndex + MIN_YEAR;
            int month = initMonth + 1;
            int days_num = 0;
            if(month==2) {
                if(isLeapYear(year))
                    days_num = 29;
                else
                    days_num = 28;
            } else if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12)
                days_num = 31;
            else
                days_num = 30;

            if(isTimed&&curYear==initYear&&curMonth==initMonth) {
                if(isEnglish) {
                    for (int i=initDay-1; i<days_num; i++)
                        dayList.add(String.valueOf(i+1));
                } else {
                    for (int i=initDay-1; i<days_num; i++)
                        dayList.add(days_zh[i]);
                }
            } else {
                if(isEnglish) {
                    for(int i=1; i<=days_num; i++)
                        dayList.add(String.valueOf(i));
                } else {
                    for(int i=0; i<days_num; i++)
                        dayList.add(days_zh[i]);
                }
            }
        } else{
            //lunar
            //calculate index
            lunar = Lunar.getInstance();
            if(isEnglish==false) {
                lunar.setDate(mCalendar);
                yearIndex = lunar.getmYear();
                if(yearIndex<MIN_YEAR || yearIndex>MAX_YEAR)
                    yearIndex = monthIndex = dayIndex = 0;
                else {
                    yearIndex = yearIndex - MIN_YEAR;
                    monthIndex = lunar.getmMonth();
                    dayIndex = lunar.getmDay()-1;
                }
                //list
                months = res.getStringArray(R.array.le_lunar_months);
                mLeapMonth = lunar.getmLeapMonth();
                if(!monthList.isEmpty())
                    monthList.clear();
                if(mLeapMonth>0 && mLeapMonth<=12) {
                    String run = res.getString(R.string.le_status_leap);
                    String str = run + months[mLeapMonth - 1];
                    for(int i=0;i<mLeapMonth; i++)
                        monthList.add(months[i]);
                    monthList.add(str);
                    for (int i=mLeapMonth; i<12; i++)
                        monthList.add(months[i]);
                    // monthList.add(mLeapMonth-1, str);
                } else {
                    stringToArrayList(monthList, months);
                }
                int dayofMonth = 0;
                if(monthIndex!=mLeapMonth)
                    dayofMonth = lunar.monthDays(lunar.getmYear(),monthIndex);
                else
                    dayofMonth = lunar.leapDays(lunar.getmYear());

                if(30==dayofMonth)
                    days = res.getStringArray(R.array.le_lunar_days_30);
                else if(29==dayofMonth)
                    days = res.getStringArray(R.array.le_lunar_days_29);
                stringToArrayList(dayList, days);
                if((mLeapMonth<=0||mLeapMonth>12)||(monthIndex<=mLeapMonth&&!lunar.getmLeap()))
                    monthIndex = monthIndex - 1;
                //int dayofMonth = LeReflectionUtils.invokeMethod(lunar, "monthDays", {int.class, int.class}, )
            }
        }
        yearAdapter = new DateAdapter(context, yearList);
        monthAdapter = new DateAdapter(context, monthList);
        dayAdapter = new DateAdapter(context, dayList);
        yearView.setViewAdapter(yearAdapter);
        monthView.setViewAdapter(monthAdapter);
        dayView.setViewAdapter(dayAdapter);
        if(isTimed) {
            yearView.setCyclic(false);
            if(curYear==initYear)
                monthView.setCyclic(false);
            else
                monthView.setCyclic(true);
            if(curYear==initYear&&curMonth==initMonth)
                dayView.setCyclic(false);
            else
                dayView.setCyclic(true);
        } else {
            yearView.setCyclic(true);
            monthView.setCyclic(true);
            dayView.setCyclic(true);
        }
//        yearView.setVisibleItems(7);
//        monthView.setVisibleItems(7);
//        dayView.setVisibleItems(7);
        yearView.setCurrentItem(yearIndex);
        monthView.setCurrentItem(monthIndex);
        dayView.setCurrentItem(dayIndex);
    }

    public void onUpdateDate() {
        int year,month, day;
        year = month = day = 0;
        if(!isLunar) {
            year = curYear;
            month = curMonth;
            day = curDay;
        } else {
            boolean isLeapMonth = false;
            mLeapMonth = lunar.leapMonth(year);
            if((mLeapMonth>=1&&mLeapMonth<=12)&&(monthIndex==mLeapMonth))
                isLeapMonth = true;
            int[] solarInfo = new int[3];
            if(mLeapMonth>=1&&mLeapMonth<=12) {
                if(monthIndex<mLeapMonth)
                    solarInfo = LunarCalendar.lunarToSolar(yearIndex + MIN_YEAR, monthIndex + 1, dayIndex + 1, isLeapMonth);
                else
                    solarInfo = LunarCalendar.lunarToSolar(yearIndex + MIN_YEAR, monthIndex, dayIndex + 1, isLeapMonth);
            } else
                solarInfo = LunarCalendar.lunarToSolar(yearIndex + MIN_YEAR, monthIndex + 1, dayIndex + 1, isLeapMonth);

            year = solarInfo[0];
            month = solarInfo[1]-1;
            day = solarInfo[2];
        }
        if(dateChangedListener!=null){
            dateChangedListener.onDateChanged(this, year, month, day);
        }else
            return;
    }

   /* @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        int center = yearView.getHeight() / 2;
        int offset = (int) (yearView.getItemHeight() / 2 );
        Paint mPaint = new Paint();
        mPaint.setStrokeWidth(1);
        mPaint.setColor(0xF0777777);
        canvas.drawLine(0, center - offset, getWidth(), center - offset, mPaint);
        canvas.drawLine(0, center + offset, getWidth(), center + offset, mPaint);
    }*/

    private void stringToArrayList(ArrayList<String> arrayList, String[] str) {
        if(!arrayList.isEmpty())
            arrayList.clear();
        for(int i=0; i<str.length; i++)
            arrayList.add(str[i]);
    }

    public boolean getIsLunar() {
        return isLunar;
    }

    public void setCenterItemTextColor(int color) {
        if(yearView!=null)
            yearView.setCenterTextColor(color);
        if(monthView!=null)
            monthView.setCenterTextColor(color);
        if(dayView!=null)
            dayView.setCenterTextColor(color);
    }

    private void modifyDayView(int year, int month) {
        int days_num = 0;
        if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
            // 31
            if (dayList.size() != 31) {
               days_num = 31;
                dayIndex = curDay-1;
            } else
                return;
        } else if(month == 2) {
            if (isLeapYear(year)) {
                //29
                if (dayList.size() != 29) {
                   days_num = 29;
                    dayIndex = curDay>29 ? 0 : curDay-1;
                    curDay = dayIndex;
                } else
                    return;
            } else {
                if (dayList.size() != 28) {
                    days_num = 28;
                    dayIndex = curDay>28 ? 0 : curDay-1;
                    curDay = dayIndex+1;
                } else
                    return;
            }
        } else {
            if (dayList.size() != 30) {
                days_num = 30;
                dayIndex = curDay>30 ? 0 : curDay-1;
                curDay = dayIndex+1;
            } else
                return;
        }
        if(dayList!=null&&dayList.size()!=0)
            dayList.clear();
        if(isEnglish) {
            for(int i=0; i<days_num; i++)
                dayList.add(days_en[i]);
        } else {
            for(int i=0; i<days_num; i++)
                dayList.add(days_zh[i]);
        }
    }

    private void modifyInitDayView(int year, int month) {
        int days_num = 0;
        int i = month + 1;
        if (i == 1 || i == 3 || i == 5 || i == 7 || i == 8 || i == 10 || i == 12){
            days_num = 31;
        } else if(i==2) {
            if (isLeapYear(year)) {
                days_num = 29;
            } else {
                days_num = 28;
            }
        } else {
            days_num = 30;
        }
        dayList.clear();
        if(isEnglish) {
            for (int j=initDay-1; j<days_num; j++) {
                dayList.add(String.valueOf(j+1));
            }
        } else {
            for (int j=initDay-1; j<days_num; j++) {
                dayList.add(days_zh[j]);
            }
        }
        dayIndex = (curDay>days_num||curDay<initDay) ? 0 : curDay-initDay;
        curDay = dayIndex+initDay;
    }

}
