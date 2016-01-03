package com.letv.shared.personal;

import java.util.Calendar;

import android.content.ContentResolver;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

public class PersonalSettingsManager{
	private static final String TAG = "PersionalService";
	
	/**
     * Boolean property that specifies the typeface for disturb mode enabled:
     * boolean                    0 = disabled! default
     *                            1 = enabled
     * wanghailu@letv.com 2014.08.25
     * {@link #sDisturbModeDefault}
     * @hide
     */
    public static final String LEUI_DISTURBMODE_ENABLED = "leui_disturbmode_enabled";
    
    /**
     * Boolean property that specifies the typeface for disturb REPEAT MODE enabled:
     * boolean                    0 = disabled! default
     *                            1 = enabled
     * wanghailu@letv.com 2014.08.25
     * {@link #sDisturbRepeatModeDefault}
     * @hide
     */
    public static final String LEUI_DISTURBMODE_REPEAT_ENABLED = "leui_disturbmode_repeat_enabled";
    
    /**
     * Boolean property that specifies the typeface for disturb time set enabled:
     * wanghailu@letv.com 2014.08.25
     * {@link #sDisturbModeStartTimeDefault}
     * @hide
     */
    public static final String LEUI_DISTURBMODE_TIMESET_ENABLED = "leui_disturbmode_timeset_enabled";
    
    /**
     * Long property that specifies the typeface for disturb start time:
     * wanghailu@letv.com 2014.08.25
     * {@link #sDisturbModeStartTimeDefault}
     * @hide
     */
    public static final String LEUI_DISTURBMODE_STARTTIME = "leui_disturbmode_starttime";
    
    /**
     * Long property that specifies the typeface for disturb end time:
     * wanghailu@letv.com 2014.08.25
     * {@link #sDisturbModeEndTimeDefault}
     * @hide
     */
    public static final String LEUI_DISTURBMODE_ENDTIME = "leui_disturbmode_endtime";
    
    public static final int sDisturbModeContactType_EveryOne = 0;
    public static final int sDisturbModeContactType_WhiteList = 1;
    
    /**
     * Integer property that specifies the typeface for disturb contacts mode, one of:
     * <ul>
     * <li>0 = EVERYONE {@link #sContactTypeDefault}
     * <li>1 = CONTACTS WHITE LISTS 
     * </ul>
     * wanghailu@letv.com 2014.08.25
     * @hide
     */
    public static final String LEUI_DISTURBMODE_CONTACTMODE = "leui_disturbmode_contactmode";
    
    public static final boolean sDisturbModeDefault = false;
    public static final boolean sDisturbRepeatModeDefault = false;
    
    private static final boolean sDisturbModeTimeSetEnabled = false;
    private static final long sIllegalStartEndTime = 0;
    public static final long sDisturbModeStartTimeDefault = sIllegalStartEndTime;
    public static final long sDisturbModeEndTimeDefault = sIllegalStartEndTime;
    
    public static final int sContactTypeDefault = 0;
	
	/*private final Context mContext;
    private final ContentResolver mContentResolver;*/

    /*private static PersonalSettingsManager mInstance = null;
    
    public static PersonalSettingsManager getInstances(){
    	if(mInstance == null){
    		mInstance = new PersonalSettingsManager();
    	}
    	return mInstance;
    }
    
    private static PersonalSettingsManager getInstances(Context context){
    	if(mInstance == null){
    		mInstance = new PersonalSettingsManager(context);
    	}
    	return mInstance;
    }
    
    private PersonalSettingsManager() {
    	mContext = null;
    	mContentResolver = null;
    }
    
    private PersonalSettingsManager(Context context) {
        mContext = context;
        mContentResolver = context.getContentResolver();
//        mOpenHelper = new DatabaseHelper(mContext);
    }*/
    
	public static boolean isDisturbModeEnabled(ContentResolver resolver) {
		return getBoolean(resolver,LEUI_DISTURBMODE_ENABLED, sDisturbModeDefault);
	}
	
	public static boolean isDisturbModeTimeSetEnabled(ContentResolver resolver) {
		return getBoolean(resolver,LEUI_DISTURBMODE_TIMESET_ENABLED, sDisturbModeTimeSetEnabled);
	}

	public static boolean isCurrentDisturbEnvironment(ContentResolver resolver) {
		return isDisturbModeEnabled(resolver)||(isDisturbModeTimeSetEnabled(resolver)&&isCurrentTimeInDisturbMode(resolver));
	}
	
	static class CalendarUtil{
		public static long getBaseTimeByCurrentDay(long time){
			Calendar oldCalendar = Calendar.getInstance();
			oldCalendar.setTimeInMillis(time);
			long start = oldCalendar.getTimeInMillis();
			oldCalendar.set(Calendar.HOUR_OF_DAY, 0);
			oldCalendar.set(Calendar.MINUTE, 0);
			long end = oldCalendar.getTimeInMillis();
			return start - end;
		}
		
		public static long getCurrentTime(){
			return System.currentTimeMillis();
		}
	}

	public static long getDisturbModeStartTime(ContentResolver resolver) {
		return getLong(resolver,LEUI_DISTURBMODE_STARTTIME,sDisturbModeStartTimeDefault);
	}

	public static long getDisturbModeEndTime(ContentResolver resolver) {
		return getLong(resolver,LEUI_DISTURBMODE_ENDTIME,sDisturbModeEndTimeDefault);
	}

	public static boolean isDisturbModeRepeateEnabled(ContentResolver resolver) {
		return getBoolean(resolver,LEUI_DISTURBMODE_REPEAT_ENABLED,sDisturbRepeatModeDefault);
	}

	public static int getDisturbContactMode(ContentResolver resolver) {
		return getInt(resolver,LEUI_DISTURBMODE_CONTACTMODE,sContactTypeDefault);
	}
	
	public static boolean isDisturbTimeInValid(long time) {
		return time == sIllegalStartEndTime;
	}

	public static boolean isCurrentTimeInDisturbMode(ContentResolver resolver) {
		long startTime = CalendarUtil.getBaseTimeByCurrentDay(getDisturbModeStartTime(resolver));
		long endTime = CalendarUtil.getBaseTimeByCurrentDay(getDisturbModeEndTime(resolver));
		long currentTime = CalendarUtil.getBaseTimeByCurrentDay(CalendarUtil.getCurrentTime());
		Log.d(TAG, "PersonalSettingsManager startTime is:"+startTime+",endTime is:"+endTime
		        +",currentTime is:"+currentTime);
		return startTime <= currentTime && currentTime <= endTime;
	}
	
	
	public static long getCurrentTime(){
		return CalendarUtil.getCurrentTime();
	}
	
	public static void setDisturbModeEnabled(ContentResolver resolver,boolean value){
		setBoolean(resolver,LEUI_DISTURBMODE_ENABLED, value);
	}
	
	public static void setDisturbModeTimeSetEnabled(ContentResolver resolver,boolean value){
		setBoolean(resolver,LEUI_DISTURBMODE_TIMESET_ENABLED, value);
	}
	
	public static void setDisturbModeStartTime(ContentResolver resolver,long time) {
		setLong(resolver,LEUI_DISTURBMODE_STARTTIME,time);
	}
	
	public static void setDisturbModeEndTime(ContentResolver resolver,long time) {
		setLong(resolver,LEUI_DISTURBMODE_ENDTIME,time);
	}

	public static void setDisturbModeRepeateEnabled(ContentResolver resolver,boolean repeat) {
		setBoolean(resolver,LEUI_DISTURBMODE_REPEAT_ENABLED,repeat);
	}

	/**
	 * {@value #LEUI_DISTURBMODE_CONTACTMODE}
	 * @param resolver
	 * @param type
	 */
	public static void setDisturbContactMode(ContentResolver resolver,int type) {
		setInt(resolver,LEUI_DISTURBMODE_CONTACTMODE,type);
	}
	
	
    
    private static void setBoolean(ContentResolver resolver,String name, boolean enabled) {
        Settings.System.putInt(resolver, name, enabled?1:0);
    }
    
    private static void setLong(ContentResolver resolver,String name, long value) {
        Settings.System.putLong(resolver,name,value);
    }
    
    private static void setString(ContentResolver resolver,String name, String value) {
        Settings.System.putString(resolver, name, value);
    }
    
    private static void setInt(ContentResolver resolver,String key, int value){
    	Settings.System.putInt(resolver,key,value);
    }
    
    
    private static boolean getBoolean(ContentResolver resolver,String key, boolean defaultValue) {
    	String value = getString(resolver,key);
    	return TextUtils.isEmpty(value)?defaultValue:(value.equals("1")||value.equals("true"));
    }
    
    private static long getLong(ContentResolver resolver,String name, long defaultValue) {
        return Settings.System.getLong(resolver, name,defaultValue);
    }
    
    private static String getString(ContentResolver resolver,String name) {
        return Settings.System.getString(resolver, name);
    }
    
    public static int getInt(ContentResolver resolver,String name, int def) {
    	return Settings.System.getInt(resolver, name,def);
    }

}
