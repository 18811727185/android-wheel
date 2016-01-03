package com.letv.shared.util;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Time;
import com.letv.shared.R;

/**
 *    Date&Time Tool
 */
public class LeDateTimeUtils {

	/**
	 *     group list :
	 *     
	 *     today: "today"
	 *     yesterday: "yesterday"
	 *     with this week: "this week"
	 *     with this month: "this month"
	 *     months ago: "Month xx"
	 *     years ago: "Month xx, Year xx"
	 *     century ago: "xxxx/xx/xx"
	 *     future: "xxxx/xx/xx"
	 */
	public static final int FORMAT_TYPE_GROUP = 0;
	
	/**
	 *    time on list element:
	 *     
	 *     today: "xx:xx(xx:xx am/pm)"
	 *     yesterday: "yesterday"
	 *     with this week: "Sun/Mon/Tue/.../Sat"
	 *     with this year: "xx/xx"
	 *     years ago: "xx/xx/xx"
	 *     centuries ago: "xxxx/xx/xx"
	 *     future: "xxxx/xx/xx"
	 */
	public static final int FORMAT_TYPE_LIST  = 1;
	
	/**
	 *    time on list element detail:
	 *     
	 *     today: "xx:xx(xx:xx am/pm)"
	 *     yesterday: "xx:xx(xx:xx am/pm)"
	 *     with this week: "Sun/Mon/Tue/.../Sat xx:xx(xx:xx am/pm)"
	 *     with this year: "xx/xx xx:xx(xx:xx am/pm)"
	 *     years ago: "xx/xx/xx xx:xx(xx:xx am/pm)"
	 *     centuries ago: "xxxx/xx/xx xx:xx(xx:xx am/pm)"
	 *     future: "xxxx/xx/xx(xx:xx am/pm)"
	 */
	public static final int FORMAT_TYPE_DETAIL = 2;
	

    //optimize format function
    private static Time NowTimeLast;
    private static Time ThenTimeLast;
    private static long NowMillisLast = 0;
    private static int FormatTypeLast = -1;
    private static String FormatResultLast = null;
    
    /**
     * @param context get relative string resource under particular locale
     * @param when time for format    unit: ms
     * @param type format type<br/>
     * {@link #FORMAT_TYPE_GROUP} <br/>
     * {@link #FORMAT_TYPE_LIST} <br/>
     * {@link #FORMAT_TYPE_DETAIL} <br/>
     * @return formatted string
     */
    public static String formatTimeStampString(Context context, long when, int type) {
        Time then = new Time();
        then.set(when);
        Time now = null;
        Long nowmillis = System.currentTimeMillis();
        boolean is24 =  DateFormat.is24HourFormat(context);
        boolean sameType = (type == FormatTypeLast);
        FormatTypeLast = type;

        boolean sameNowDay = false;
        if (NowTimeLast == null) {
            sameNowDay = false;
        } else {
            // reuse object "now" if the same day
            if (nowmillis >= NowMillisLast && nowmillis < (NowMillisLast + 24 * 60 * 60 * 1000)) {
                sameNowDay = true;
            } else {
                sameNowDay = false;
            }
        }
        if (!sameNowDay) {
            now = new Time();
            now.set(nowmillis);
            // remember object "now" and the beginning time of that day
            NowTimeLast = now;
            NowMillisLast = nowmillis - (now.hour * 60 * 60 * 1000 + now.minute * 60 * 1000 + now.second * 1000);
        } else {
            now = NowTimeLast;
        }

        boolean sameWhenDay = false;
        if (ThenTimeLast != null) {
            sameWhenDay = (ThenTimeLast.year == then.year && ThenTimeLast.yearDay == then.yearDay);
        }
        
        ThenTimeLast = then;
        int weekStart = now.yearDay - now.weekDay;
        boolean isThisMonth= (now.year== then.year)&& (now.month== then.month) && (then.yearDay <= now.yearDay);
        boolean isThisYear = (now.year == then.year) && (then.yearDay <= now.yearDay);
        boolean isToday = (isThisYear && (then.yearDay == now.yearDay));
        boolean isYesterday = (isThisYear && (then.yearDay == now.yearDay -1));
        boolean isThisWeek = (isThisYear && (then.yearDay >= weekStart && then.yearDay < now.yearDay));
        boolean isThisCentury=(then.year/100 == now.year/100 && then.year < now.year);
        boolean isCenturyAgo = (then.year/100 <  now.year/100);
        Resources resources = context.getResources();
        switch (type) {
            case FORMAT_TYPE_GROUP:
                if (isToday) {// today
                    return then.format(resources.getString(R.string.pattern_group_today));
                }  else if(isYesterday){
                	return then.format(resources.getString(R.string.pattern_group_yesterday));
                }  else if (isThisWeek) {// this week
                    if (sameWhenDay && sameType && !TextUtils.isEmpty(FormatResultLast)) {
                        return FormatResultLast;
                    }
                    FormatResultLast = then.format(resources.getString(R.string.pattern_group_within_week));
                    return FormatResultLast;
                } else if (isThisMonth) {// this month
                    // return result of last time in cache
                    if (sameWhenDay && sameType && !TextUtils.isEmpty(FormatResultLast)) {
                        return FormatResultLast;
                    }
                    FormatResultLast = then.format(resources.getString(R.string.pattern_group_within_month));
                    return FormatResultLast;
                } else if (isThisYear) {// this year
                    // return result of last time in cache
                    if (sameWhenDay && sameType && !TextUtils.isEmpty(FormatResultLast)) {
                        return FormatResultLast;
                    }
                    FormatResultLast = then.format(resources.getString(R.string.pattern_group_over_month));
                    return FormatResultLast;
                }  else if (isThisCentury) {// this century
                    // return result of last time in cache
                    if (sameWhenDay && sameType && !TextUtils.isEmpty(FormatResultLast)) {
                        return FormatResultLast;
                    }
                    FormatResultLast = then.format(resources.getString(R.string.pattern_group_over_year));
                    return FormatResultLast;
                }   else if (isCenturyAgo) {// several centuries ago
                    // return result of last time in cache
                    if (sameWhenDay && sameType && !TextUtils.isEmpty(FormatResultLast)) {
                        return FormatResultLast;
                    }
                    FormatResultLast = then.format(resources.getString(R.string.pattern_group_over_century));
                    return FormatResultLast;
                }  else {// future
                    FormatResultLast = then.format(resources.getString(R.string.pattern_group_future));
                    return FormatResultLast;
                }
                
            case FORMAT_TYPE_LIST:
            	if (isToday) {// today
                    if (is24) {
                        return then.format(resources.getString(R.string.pattern_list_today));
                    } else {
                        return then.format(resources.getString(R.string.pattern_list_today_12));
                    }
                } else if (isYesterday){//yesterday
                    // return result of last time in cache
                    if (sameWhenDay && sameType && !TextUtils.isEmpty(FormatResultLast)) {
                        return FormatResultLast;
                    }
                    FormatResultLast = then.format(resources.getString(R.string.pattern_list_yesterday));
                	return FormatResultLast;
                } else if (isThisWeek){//this week
                    // return result of last time in cache
                    if (sameWhenDay && sameType && !TextUtils.isEmpty(FormatResultLast)) {
                        return FormatResultLast;
                    }
                    FormatResultLast = then.format(resources.getString(R.string.pattern_list_within_week));
                    return FormatResultLast;
                } else if(isThisYear){//this year
                    // return result of last time in cache
                    if (sameWhenDay && sameType && !TextUtils.isEmpty(FormatResultLast)) {
                        return FormatResultLast;
                    }
                    FormatResultLast = then.format(resources.getString(R.string.pattern_list_over_week));
                    return FormatResultLast;
                } else if(isThisCentury){//this century
                    // return result of last time in cache
                    if (sameWhenDay && sameType && !TextUtils.isEmpty(FormatResultLast)) {
                        return FormatResultLast;
                    }
                    FormatResultLast = then.format(resources.getString(R.string.pattern_list_over_year));
                    return FormatResultLast;
                } else if(isCenturyAgo){//several centuries ago
                    // return result of last time in cache
                    if (sameWhenDay && sameType && !TextUtils.isEmpty(FormatResultLast)) {
                        return FormatResultLast;
                    }
                    FormatResultLast = then.format(resources.getString(R.string.pattern_list_over_century));
                    return FormatResultLast;
                } else { // future
                    FormatResultLast = then.format(resources.getString(R.string.pattern_list_future));
                    return FormatResultLast;
                }
            	
            case FORMAT_TYPE_DETAIL:
            	if (isToday) {// today
                    if (is24) {
                        return then.format(resources.getString(R.string.pattern_detail_today));
                    } else {
                        return then.format(resources.getString(R.string.pattern_detail_today_12));
                    }
                } else if (isYesterday){//yesterday
                	if(is24){
                	   return then.format(resources.getString(R.string.pattern_detail_yesterday));
                	} else {
                		return then.format(resources.getString(R.string.pattern_detail_yesterday_12));
                	}
                } else if (isThisWeek){//this week
                	if(is24){
                        FormatResultLast = then.format(resources.getString(R.string.pattern_detail_within_week));
                	} else {
                		FormatResultLast = then.format(resources.getString(R.string.pattern_detail_within_week_12));
                	}
                    return FormatResultLast;
                } else if(isThisYear){//this year
                	if(is24){
                        FormatResultLast = then.format(resources.getString(R.string.pattern_detail_over_week));
                	} else {
                		FormatResultLast = then.format(resources.getString(R.string.pattern_detail_over_week_12));
                	}
                    return FormatResultLast;
                } else if(isThisCentury){//this century
                	if(is24){
                		FormatResultLast = then.format(resources.getString(R.string.pattern_detail_over_year));
                	} else {
                		FormatResultLast = then.format(resources.getString(R.string.pattern_detail_over_year_12));
                	}
                    return FormatResultLast;
                } else if(isCenturyAgo){//  several centuries ago
                	if(is24){
                		FormatResultLast = then.format(resources.getString(R.string.pattern_detail_over_century));
                	} else {
                		FormatResultLast = then.format(resources.getString(R.string.pattern_detail_over_century_12));
                	}
                    return FormatResultLast;
                } else { // future
                	if(is24){
                		FormatResultLast = then.format(resources.getString(R.string.pattern_detail_future));
                	} else {
                		FormatResultLast = then.format(resources.getString(R.string.pattern_detail_future_12));
                	}
                    return FormatResultLast;
                }
           
        }
        return null;
    }
    
    /**
     * this method can get array contains date for array[0] and time for array[1]
     * @param context
     * @param when : timestamp (ms)
     * @param isDateSimple : simple date format without 0 begin
     * @param isTimeSimple: simple time format without 0 begin
     * @return string array, array[0] for date(today, yesterday or **-**)
     *                  array[1] for time
     */
    public static String[] formatTimeStampStringToArray(Context context, long when, boolean isDateSimple, boolean isTimeSimple){
    	
    	 Time then = new Time();
         then.set(when);
         Time now = null;
         Long nowmillis = System.currentTimeMillis();
         String[] dateAndTimeArr = new String[2];
         boolean is24 =  DateFormat.is24HourFormat(context);

         boolean sameNowDay = false;
         if (NowTimeLast == null) {
             sameNowDay = false;
         } else {
             // reuse object "now" if the same day
             if (nowmillis >= NowMillisLast && nowmillis < (NowMillisLast + 24 * 60 * 60 * 1000)) {
                 sameNowDay = true;
             } else {
                 sameNowDay = false;
             }
         }
         if (!sameNowDay) {
             now = new Time();
             now.set(nowmillis);
             // remember object "now" and the beginning time of that day
             NowTimeLast = now;
             NowMillisLast = nowmillis - (now.hour * 60 * 60 * 1000 + now.minute * 60 * 1000 + now.second * 1000);
         } else {
             now = NowTimeLast;
         }
         
         ThenTimeLast = then;
         
         boolean isThisYear = (now.year == then.year) && (then.yearDay <= now.yearDay);
         boolean isToday = (isThisYear && (then.yearDay == now.yearDay));
         boolean isYesterday = (isThisYear && (then.yearDay == now.yearDay -1));

         Resources resources = context.getResources();
         
         if( isTimeSimple){
        	 if(is24){
        		 dateAndTimeArr[1] = then.format(resources.getString(R.string.pattern_time_simple));
        	 } else {
        		 dateAndTimeArr[1] = then.format(resources.getString(R.string.pattern_time_simple_12));
        	 }
         } else {
        	 if(is24){
        		 dateAndTimeArr[1] = then.format(resources.getString(R.string.pattern_time_complete));
        	 } else {
        		 dateAndTimeArr[1] = then.format(resources.getString(R.string.pattern_time_complete_12));
        	 }
         }
         if (isToday) {// today
        	 dateAndTimeArr[0] = resources.getString(R.string.pattern_today);
         }  else if(isYesterday){ //yesterday
        	 dateAndTimeArr[0] = resources.getString(R.string.pattern_yesterday);
         }  else {
        	 if(isDateSimple){
        		 dateAndTimeArr[0] = then.format(resources.getString(R.string.pattern_date_simple));
        	 } else {
        		 dateAndTimeArr[0] = then.format(resources.getString(R.string.pattern_date_complete));
        	 }
         }
         return dateAndTimeArr;
    }
    
    /**
     * this method can get time duration between start and end time
     * @param context
     * @param start : start timestamp(ms)
     * @param end : end timestamp(ms)
     * @return duration time 
     */
    public static String getPassTimeDuration(Context context, long start, long end){
    	
        if( start > end){
        	return "Input parameters are invalid!";
        }
        
        StringBuilder timeDurationStr = new StringBuilder();
        Resources resources = context.getResources();
        
        long diff = end - start;
        long days = diff / (1000 * 60 * 60 * 24);
        long hours = (diff-days*(1000 * 60 * 60 * 24))/(1000* 60 * 60);
        long minutes = (diff-days*(1000 * 60 * 60 * 24)-hours*(1000* 60 * 60))/(1000* 60);
        long seconds = (diff-days*(1000 * 60 * 60 * 24)-hours*(1000* 60 * 60)-minutes*(1000*60))/1000;
        
        if( days > 0 ){
        	timeDurationStr.append(String.valueOf(days)).append(resources.getString(R.string.pattern_day));
        }
        if( hours > 0 ){
        	timeDurationStr.append(String.valueOf(hours)).append(resources.getString(R.string.pattern_hour));
        }
        if( minutes > 0 ){
        	timeDurationStr.append(String.valueOf(minutes)).append(resources.getString(R.string.pattern_minute));
        }
        if( seconds > 0 ){
        	timeDurationStr.append(String.valueOf(seconds)).append(resources.getString(R.string.pattern_second));
        }
        
        return timeDurationStr.toString().length() > 0 ? timeDurationStr.toString() : "";
    }
    
    public static String[] getFormaTimeForLetvSports(Context context, long when){
    	
   	 Time then = new Time();
     then.set(when);
     Time now = null;
     Long nowmillis = System.currentTimeMillis();
     String[] dateAndTimeArr = new String[2];

     boolean sameNowDay = false;
     if (NowTimeLast == null) {
         sameNowDay = false;
     } else {
         // reuse object "now" if the same day
         if (nowmillis >= NowMillisLast && nowmillis < (NowMillisLast + 24 * 60 * 60 * 1000)) {
             sameNowDay = true;
         } else {
             sameNowDay = false;
         }
     }
     if (!sameNowDay) {
         now = new Time();
         now.set(nowmillis);
         // remember object "now" and the beginning time of that day
         NowTimeLast = now;
         NowMillisLast = nowmillis - (now.hour * 60 * 60 * 1000 + now.minute * 60 * 1000 + now.second * 1000);
     } else {
         now = NowTimeLast;
     }
     
     ThenTimeLast = then;
     
     boolean isThisYear = (now.year == then.year);
     boolean isToday = (isThisYear && (then.yearDay == now.yearDay));
     boolean isYesterday = (isThisYear && (then.yearDay == now.yearDay - 1));
     boolean isTomorrow = (isThisYear && (then.yearDay == now.yearDay + 1));

     Resources resources = context.getResources();
     
     dateAndTimeArr[0] = then.format(resources.getString(R.string.pattern_month_day));
     if (isToday) {// today
    	 dateAndTimeArr[1] = resources.getString(R.string.pattern_today);
     }  else if(isYesterday){ //yesterday
    	 dateAndTimeArr[1] = resources.getString(R.string.pattern_yesterday);
     }  else if(isTomorrow){ //tomorrow
    	 dateAndTimeArr[1] = resources.getString(R.string.pattern_tomorrow);
     } else {
    	 dateAndTimeArr[1] = then.format(resources.getString(R.string.pattern_week_complete));
     }
     return dateAndTimeArr;
    }
    
}
