package com.letv.mobile.core.utils;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

public class ViewUtil {

    /**
     * 移除OnGlobalLayoutListener
     * @param view
     * @param listener
     */
    @SuppressLint("NewApi")
    public static void removeOnGlobalLayoutListener(View view,
            OnGlobalLayoutListener listener) {
        if (view != null && listener != null) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= 16) {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(
                            listener);
                } else {
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(
                            listener);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
