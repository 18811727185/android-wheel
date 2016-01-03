
package com.letv.shared.pim.lunar;

import java.util.Calendar;
import java.util.Locale;

import android.content.res.Resources;
import com.letv.shared.R;

public class SolarHoliDay {
    public static int SUNDAY = 1;
    public static int FIRST_DAY_OF_MONTH = 1;
    public static int MAY = 4;
    public static int JUNE = 5;
    public static int DAYS_OF_TWO_WEEKS = 14;
    public static int DAYS_OF_ONE_WEEKS = 7;
    public static int DAYS_OF_THREE_WEEKS = 21;
    public static String getSolarHoliDay(int year,int currentMonth, int currentDayForMonth) {
        Resources res = Resources.getSystem();
        String num_date = String.format("%02d", currentMonth + 1) + ""
                + String.format("%02d", currentDayForMonth);
        String[] solarHolidayArray = res
                .getStringArray(R.array.le_solar_holiday);
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(year, currentMonth, FIRST_DAY_OF_MONTH);
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        if(currentMonth == MAY) {
            int secondSunday = -1;
            if (weekDay == SUNDAY) {
                secondSunday = FIRST_DAY_OF_MONTH + DAYS_OF_ONE_WEEKS;
            } else {
                secondSunday = DAYS_OF_TWO_WEEKS - weekDay + 2;
            }
            if (secondSunday == currentDayForMonth) {
                return res.getString(R.string.le_special_holiday_mother);
            }
        }
        if(currentMonth == JUNE) {
            int thirdSunday = -1;
            if (weekDay == SUNDAY) {
                thirdSunday = FIRST_DAY_OF_MONTH + DAYS_OF_TWO_WEEKS;
            } else {
                thirdSunday = DAYS_OF_THREE_WEEKS - weekDay + 2;
            }
            if (thirdSunday == currentDayForMonth) {
                return res.getString(R.string.le_special_holiday_father);
            }
        }
        for (int i = 0; i < solarHolidayArray.length; i++) {
            String[] solarHolidayDateStr = solarHolidayArray[i].split(" ");
            if (solarHolidayDateStr[0].equals(num_date)) {
                return solarHolidayDateStr[1];
            }
        }
        return "";
    }
}
