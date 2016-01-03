package com.letv.sysletvplayer.util;

import android.view.View;
import android.view.ViewGroup;

public class ViewUtils {
    private ViewUtils() {
    }

    /**
     * 将子View从布局容器中隐藏
     */
    public static void hideViewByViewGroup(ViewGroup container, View childView) {
        if (container == null || childView == null) {
            return;
        }
        if (isVisibleView(childView)) {
            container.removeView(childView);
        }
        childView.setVisibility(View.GONE);
    }

    /**
     * 显示子View,如果子View未添加，将子View加入布局容器
     */
    public static void showViewByViewGroup(ViewGroup container, View childView) {
        if (container == null || childView == null) {
            return;
        }
        if (!containChildView(container, childView)) {
            container.addView(childView);
        }
        childView.setVisibility(View.VISIBLE);
    }

    /**
     * 判断某布局容器中是否包含某个子View
     */
    public static boolean containChildView(ViewGroup container, View childView) {
        if (container == null || childView == null) {
            return false;
        }
        for (int i = 0; i < container.getChildCount(); i++) {
            if (childView == container.getChildAt(i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断view是否可见
     */
    public static boolean isVisibleView(View view) {
        if (view != null && view.getVisibility() == View.VISIBLE) {
            return true;
        }
        return false;
    }
}
