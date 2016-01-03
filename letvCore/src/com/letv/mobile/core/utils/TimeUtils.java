package com.letv.mobile.core.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;

import com.letv.mobile.core.R;
import com.letv.mobile.core.time.TimeProvider;

/**
 * 时间转换
 * @author Fengwx
 */
@SuppressLint("SimpleDateFormat")
public class TimeUtils {
    private final static String SERVERTIMEKEY = "servertimestamp";
    private final static String LOCTIMEKEY = "loctimestamp";
    private final static String servertime_sharepref = "com_letv_tv_servertime";

    public static final long ONE_SECOND = 1000L;
    public static final long ONE_MINUTE = 60000L;
    public static final long ONE_HOUR = 3600000L;
    public static final long ONE_DAY = 86400000L;
    public static final long ONE_WEEK = 7 * 86400000L;
    public static final long ONE_MONTH = 30 * 86400000L;
    public static final long ONE_YEAR = 365 * 86400000L;

    private static final String lONG_TIME_AGO = ContextProvider.getApplicationContext()
            .getResources().getString(R.string.long_time_ago);
    private static final String FEW_MONTH_AGO = ContextProvider.getApplicationContext()
            .getResources().getString(R.string.few_months_ago);
    private static final String MONTH_AGO = ContextProvider.getApplicationContext().getResources()
            .getString(R.string.month_ago);
    private static final String WEEK_AGO = ContextProvider.getApplicationContext().getResources()
            .getString(R.string.week_ago);
    private static final String FEW_DAY_AGO = ContextProvider.getApplicationContext()
            .getResources().getString(R.string.few_days_ago);
    private static final String DAY_AGO = ContextProvider.getApplicationContext().getResources()
            .getString(R.string.day_ago);
    private static final String HOUR_AGO = ContextProvider.getApplicationContext().getResources()
            .getString(R.string.hour_ago);
    private static final String MINUTE_AGO = ContextProvider.getApplicationContext().getResources()
            .getString(R.string.minute_ago);
    private static final String JUST_NOW = ContextProvider.getApplicationContext().getResources()
            .getString(R.string.just_now);
    private static final String YESTODAY = ContextProvider.getApplicationContext().getResources()
            .getString(R.string.yestoday);
    private static final String TIME_FORMAT = ContextProvider.getApplicationContext()
            .getResources().getString(R.string.update_time_format);

    private static final String YYYY_MM = ContextProvider.getApplicationContext().getResources()
            .getString(R.string.yyyy_mm);
    private static final String YYYY_MM_DD = ContextProvider.getApplicationContext().getResources()
            .getString(R.string.yyyy_mm_dd);
    private static final String MM_DD_HH = ContextProvider.getApplicationContext().getResources()
            .getString(R.string.mm_dd_hh);
    private static final String SUNDAY = ContextProvider.getApplicationContext().getResources()
            .getString(R.string.sunday);
    private static final String MONDAY = ContextProvider.getApplicationContext().getResources()
            .getString(R.string.monday);
    private static final String TUESDAY = ContextProvider.getApplicationContext().getResources()
            .getString(R.string.tuesday);
    private static final String WEDNESDAY = ContextProvider.getApplicationContext().getResources()
            .getString(R.string.wednesday);
    private static final String THURSDAY = ContextProvider.getApplicationContext().getResources()
            .getString(R.string.thursday);
    private static final String FRIDAY = ContextProvider.getApplicationContext().getResources()
            .getString(R.string.friday);
    private static final String SATURDAY = ContextProvider.getApplicationContext().getResources()
            .getString(R.string.saturday);

    /**
     * 根据日期获得星期 格式：周X
     * @param date
     * @return
     */
    public static String getWeekOfDate(Date date) {
        String[] weekDaysName = { SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY };
        // String[] weekDaysCode = { "0", "1", "2", "3", "4", "5", "6" };
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int intWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        return weekDaysName[intWeek];
    }

    /**
     * 时间转换成字符串，格式“yyyy-MM-dd HH:mm:ss”
     * @param date
     * @return
     */
    public static String timeToStr(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return df.format(date);
    }

    /**
     * 时间转换成字符串，格式“yyyy-MM-dd”
     * @param date
     * @return
     */
    public static String timeToString(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return df.format(date);
    }

    /**
     * 字符串转换成时间，格式“yyyy-MM-dd HH:mm:ss”
     * @param str
     * @return
     */
    public static Date strToTime(String str) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = null;
        try {
            date = df.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 字符串转换成时间，格式“yyyy-MM-dd”
     * @param str
     * @return
     */
    public static Date stringToTime(String str) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = null;
        try {
            date = df.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 毫秒形式转换成字符串
     * @param time
     * @return
     */
    public static String longToStr(long time) {
        String str = "";
        try {
            str = timeToStr(longToTime(time));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 时间转换成毫秒形式
     * @param date
     * @return
     */
    public static long timeToLong(Date date) {
        if (date != null) {
            return date.getTime();
        } else {
            return 0;
        }
    }

    /**
     * 毫秒形式转换成时间
     * @param time
     * @return
     */
    public static Date longToTime(long time) {
        return new Date(time);
    }

    /**
     * 字符串转换成毫秒形式
     * @param str
     * @return
     */
    public static long strToLong(String str) {
        return timeToLong(strToTime(str));
    }

    /**
     * 比较两个字符串形式时间的前后
     * @param date1
     * @param date2
     * @return
     */
    public static int compareTime(Long date1, Long date2) {
        if (date1 > date2) {
            return 1;
        }
        return 0;
    }

    /**
     * 指定日期前N天
     * @Title preTime
     * @Description
     * @param
     * @return String
     * @throws
     */
    public static String preTime(String time, int days) {
        Calendar previous = Calendar.getInstance();
        Date date = stringToTime(time);
        if (date != null) {
            previous.setTime(date);
        }
        previous.add(Calendar.DAY_OF_MONTH, -days);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return formatter.format(previous.getTime());
    }

    /**
     * 指定日期后N天
     * @Title nextTime
     * @Description
     * @param
     * @return String
     * @throws
     */
    public static String nextTime(String time, int days) {
        Calendar previous = Calendar.getInstance();
        Date date = stringToTime(time);
        if (date != null) {
            previous.setTime(date);
        }
        previous.add(Calendar.DAY_OF_MONTH, +days);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return formatter.format(previous.getTime());
    }

    /**
     * 当前日期前N天
     * @Title preTime
     * @Description
     * @param
     * @return String
     * @throws
     */
    public static String preTime(int days) {
        Calendar previous = Calendar.getInstance();
        previous.add(Calendar.DAY_OF_MONTH, -days);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return formatter.format(previous.getTime());
    }

    /**
     * 当前日期后N天
     * @Title nextTime
     * @Description
     * @param
     * @return String
     * @throws
     */
    public static String nextTime(int days) {
        Calendar previous = Calendar.getInstance();
        previous.add(Calendar.DAY_OF_MONTH, +days);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return formatter.format(previous.getTime());
    }

    /**
     * 将时间转换成毫秒（如：10:00:00----》 10*60*60*1000ms）
     * @param str
     */
    public static long timeToLong(String str) {
        Date d = null;
        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String tt = formatter.format(calendar.getTime()) + " " + str;
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
            d = df.parse(tt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (d != null) {
            return d.getTime();
        }
        return 0;
    }

    /**
     * 播放开始时间格式 时间格式：hh:MM:ss 转化为 hh:MM
     * @param time
     *            hh:mm:ss
     * @return
     */
    public static String switchTime(String time) {
        if (!StringUtils.equalsNull(time)) {
            if (time.length() == 8) { // 时间格式：hh:MM:ss
                return time.substring(0, 5); // 转化时间格式：hh:MM
            } else {
                return time;
            }
        }
        return "";
    }

    /**
     * 将毫秒转换成 HH：mm形式
     * @param serverTime
     *            毫秒数
     * @return
     */
    public static String getCurrentTime(long serverTime) {
        Date date = new Date(serverTime);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(date);
    }

    /**
     * 格式化日期为yyyy年MM月dd日
     * @param date
     * @return
     */
    public static String formatDate(Date date) {
        String time = "";
        if (date != null) {
            SimpleDateFormat format2 = new SimpleDateFormat(YYYY_MM_DD);
            time = format2.format(date);
        }
        return time;
    }

    /**
     * 格式化日期MM月dd日 HH:mm
     * @param date
     * @return
     */
    public static String formatDateMMddHHmm(Date date) {
        String time = "";
        if (date != null) {
            SimpleDateFormat format = new SimpleDateFormat(MM_DD_HH);
            time = format.format(date);
        }
        return time;
    }

    /**
     * 格式化时间到 数值yyyyMMdd
     * @param date
     * @return 20150102
     */
    public static long formatDateYYYYMMddHHmmLong(Date date) {
        try {
            return Long.parseLong(formatDate(date, new SimpleDateFormat("yyyyMMdd")));
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    /**
     * 格式化时间 yyyy年MM月
     * @param date
     * @return
     */
    public static String formatDateYYYYMM(Date date) {

        return formatDate(date, new SimpleDateFormat(YYYY_MM));

    }

    public static long formatDateYYYYMMLong(Date date) {
        try {
            return Long.parseLong(formatDate(date, new SimpleDateFormat("yyyyMM")));
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    /**
     * 格式化时间 format
     * @param date
     * @param format
     * @return
     */
    public static String formatDate(Date date, SimpleDateFormat format) {
        String time = "";
        if (date != null) {
            time = format.format(date);
        }
        return time;
    }

    /**
     * 容错机制serverTime为0或者等于本地缓存上次的servertime,需要返回的本地缓存上次servertime+（当前本地时间-
     * 上次缓存本地时间）
     * @param context
     * @param serverTime
     * @return server time
     */
    public static long handlerServerTime(Context context, long serverTime) {
        SharedPreferencesManager.createInstance(context, servertime_sharepref);
        long currenttime = System.currentTimeMillis();
        long locservertime = SharedPreferencesManager.getLong(servertime_sharepref, SERVERTIMEKEY,
                currenttime);
        if (serverTime <= 0 || locservertime == serverTime) {
            long loctime = SharedPreferencesManager.getLong(servertime_sharepref, LOCTIMEKEY,
                    currenttime);
            if (loctime > 0) {
                long subtime = currenttime - loctime;
                if (subtime > 0) {
                    SharedPreferencesManager.putLong(servertime_sharepref, SERVERTIMEKEY,
                            locservertime + subtime);
                } else {
                    subtime = 0;
                }
                SharedPreferencesManager.putLong(servertime_sharepref, LOCTIMEKEY, currenttime);
                return subtime + locservertime;
            } else {
                SharedPreferencesManager.putLong(servertime_sharepref, LOCTIMEKEY, currenttime);
            }
        } else {
            SharedPreferencesManager.putLong(servertime_sharepref, SERVERTIMEKEY, serverTime);
            SharedPreferencesManager.putLong(servertime_sharepref, LOCTIMEKEY, currenttime);
            return serverTime;
        }

        return locservertime;
    }

    /**
     * 返回"分：秒"的时间（或"时：分：秒"）
     * @param seconds
     * @return
     */
    public static String getDuration(long seconds) {
        if (seconds < 0) {
            seconds = 0;
        }
        long hours = 0;
        long minutes = 0;
        if (seconds >= 60) {
            minutes = seconds / 60;
            seconds %= 60;
        }
        if (minutes >= 60) {
            hours = minutes / 60;
            minutes %= 60;
        }
        if (hours == 0) {
            return String.format("%1$02d:%2$02d", minutes, seconds);
        } else {
            return String.format("%1$02d:%2$02d:%3$02d", hours, minutes, seconds);
        }
    }

    /**
     * 格式化时间字符串
     * @param timeMs
     *            毫秒,时长
     * @return 返回格式00:00
     */
    public static String stringForTimeNoHour(long timeMs) {

        StringBuilder formatBuilder = new StringBuilder();
        Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());

        try {
            if (timeMs >= 0) {
                int totalSeconds = (int) (timeMs / 1000);

                int seconds = totalSeconds % 60;
                int minutes = totalSeconds / 60;

                formatBuilder.setLength(0);

                return formatter.format("%02d:%02d", minutes, seconds).toString();
            } else {
                return "00:00";
            }
        } finally {
            formatter.close();
        }
    }

    /**
     * 格式化时间字符串
     * @param timeMs
     *            秒,时长
     * @return 返回格式00:00
     */
    public static String stringForTimeNoHourS(long timeS) {

        StringBuilder formatBuilder = new StringBuilder();
        Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());

        try {
            if (timeS >= 0) {

                long seconds = timeS % 60;
                long minutes = timeS / 60;

                formatBuilder.setLength(0);
                if (minutes >= 100) {
                    return formatter.format("%03d:%02d", minutes, seconds).toString();
                } else {
                    return formatter.format("%02d:%02d", minutes, seconds).toString();
                }
            } else {
                return "00:00";
            }
        } finally {
            formatter.close();
        }
    }

    /**
     * 获取今天零点的毫秒值
     * @return
     */
    public static long getZeroTimeToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(TimeProvider.getCurrentMillisecondTime());
        int day = calendar.get(Calendar.DAY_OF_YEAR);
        calendar.set(Calendar.DAY_OF_YEAR, day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取本周第一天
     * @param whickDay
     *            本周是以周日还是周一作为第一天的
     * @return
     *         -1:传入参数不对。
     *         需要周日或者周一：Calendar.MONDAY 、Calendar.SUNDAY
     */
    public static long getZeroTimeCurWeek(int whickDay) {
        if (whickDay == Calendar.MONDAY || whickDay == Calendar.SUNDAY) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(TimeProvider.getCurrentMillisecondTime());
            int day;
            if (whickDay == Calendar.MONDAY) {// 周一是第一天
                day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            } else {
                day = calendar.get(Calendar.DAY_OF_WEEK);// 周日第一天
            }
            calendar.set(Calendar.DAY_OF_WEEK, day);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTimeInMillis();
        } else {
            return -1;
        }
    }

    private static final SimpleDateFormat format = new SimpleDateFormat(TIME_FORMAT);

    /**
     * 吧给定的yyyy-MM-dd 格式时间转换为只显示yyyy
     */
    public static String formatDateOnlyDisplayYear(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        try {
            sdf.applyPattern("yyyy-MM-dd");
            Date dd = sdf.parse(date);
            sdf.applyPattern("yyyy");
            return sdf.format(dd);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return date;
    }

    /**
     * 离现在时间距离多久
     */
    public static String getFormatUpDateTime(long date) {
        long todayDelta = getZeroTimeToday() - date;
        if (todayDelta > 0 && todayDelta < ONE_DAY) {
            return YESTODAY;
        } else {
            long delta = new Date().getTime() - date;
            if (delta < 10L * ONE_MINUTE) {
                return JUST_NOW;
            }
            if (delta < 60L * ONE_MINUTE) {
                return delta / ONE_MINUTE + MINUTE_AGO;
            }

            if (delta < 24L * ONE_HOUR) {
                return ((delta / ONE_HOUR) <= 0 ? 1 : delta / ONE_HOUR) + HOUR_AGO;
            }
            if (ONE_DAY <= delta && delta <= ONE_WEEK) {
                return (delta / ONE_DAY) + DAY_AGO;
            }
            if (ONE_WEEK < delta && delta < ONE_MONTH) {
                return FEW_DAY_AGO;
            }
            if (ONE_MONTH <= delta && delta < 3 * ONE_MONTH) {
                return (delta / ONE_MONTH) + MONTH_AGO;
            }
            if (3 * ONE_MONTH <= delta && delta < ONE_YEAR) {
                return FEW_MONTH_AGO;
            }
            if (ONE_YEAR <= delta && delta < 2 * ONE_YEAR) {
                return format.format(new Date(date));
            } else {
                return lONG_TIME_AGO;
            }
        }
    }

    /**
     * 两个日期是否在同一月
     * @param dataOne
     * @param dataTwo
     * @return
     */
    public static boolean isSameMonth(long dataOne, long dataTwo) {

        SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
        long dataOneLong = Long.parseLong(format.format(new Date(dataOne)));
        long dataTwoLong = Long.parseLong(format.format(new Date(dataTwo)));
        // System.out.println("dataOneLong " + dataOneLong + " dataTwoLong "
        // + dataTwoLong);
        return dataOneLong == dataTwoLong;
    }

    /**
     * 两个日期是否在同一天
     * @param dataOne
     * @param dataTwo
     * @return
     */
    public static boolean isSameDay(long dataOne, long dataTwo) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        long dataOneLong = Long.parseLong(format.format(new Date(dataOne)));
        long dataTwoLong = Long.parseLong(format.format(new Date(dataTwo)));
        // System.out.println("dataOneLong " + dataOneLong + " dataTwoLong "
        // + dataTwoLong);
        return dataOneLong == dataTwoLong;
    }
}
