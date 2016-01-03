package com.letv.mobile.core.utils;

/**
 * 1080P速递消息弹出显示逻辑
 * @author lixin
 */
public class NotifyContentUtils {
    private static boolean isShowToast = false;// 标志应用回到前台时，是否需要弹出速递提示页面

    public static boolean isShowToast() {
        return isShowToast;
    }

    public static void setShowToast(boolean isShowToast) {
        NotifyContentUtils.isShowToast = isShowToast;
    }
}
