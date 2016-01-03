
package com.letv.shared.pim.lunar;

import android.content.res.Resources;
import com.letv.shared.R;

public class LunarFestival {

    public static String getLunarFestival(String chinaDate, Lunar lunar) {
        Resources res = Resources.getSystem();
        String[] sLunarFestivalArray = res
                .getStringArray(R.array.le_lunar_festival);
        chinaDate = chinaDate.substring(chinaDate.length() - 4, chinaDate.length());
        for (int i = 0; i < sLunarFestivalArray.length; i++) {
            String[] lunar_str = sLunarFestivalArray[i].split(" ");
            if (lunar_str[0].equals(chinaDate)) {
                if (i == 0) {
                    return lunar.isBigMonth(lunar_str[0]) ? "" : lunar_str[1];
                } else {
                    return lunar_str[1];
                }
            }
        }
        return "";
    }
}
