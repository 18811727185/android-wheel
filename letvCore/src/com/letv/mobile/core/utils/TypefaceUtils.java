package com.letv.mobile.core.utils;

import android.content.Context;
import android.graphics.Typeface;

public class TypefaceUtils {
    /**
     * 获取862-CAI978_2字体
     * @param context
     * @return
     */
    public static Typeface getTimeTypeface(Context context) {
        return Typeface.createFromAsset(context.getAssets(),
                "fonts/862-CAI978_2.ttf");
    }

}
