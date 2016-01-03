package com.letv.mobile.core.scaleview;

/**
 * 缩放模式
 * @author baiwenlong
 */
public enum ScaleStyle {
    BASED_ON_WIDTH(0), // 根据width来缩放
    BASED_ON_HEIGHT(1), // 根据height来缩放
    BASED_ON_WIDTH_AND_HEIGHT(2);// 根据宽和高缩放

    private final int id;

    private ScaleStyle(int StyleId) {
        this.id = StyleId;
    }

    public static ScaleStyle valueOf(int styleId) {
        for (ScaleStyle style : ScaleStyle.values()) {
            if (style.id == styleId) {
                return style;
            }
        }
        return null;
    }
}
