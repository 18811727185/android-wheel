package com.letv.mobile.core.scaleview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.letv.mobile.core.R;

public class ScaleParameter {

    private ScaleStyle scaleStyle;

    public ScaleParameter(Context context, AttributeSet attrs) {
        if (context == null || attrs == null) {
            return;
        }
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ScaleView);
        int styleId = a.getInt(R.styleable.ScaleView_scaleStyle, -1);
        this.scaleStyle = ScaleStyle.valueOf(styleId);
        a.recycle();
    }

    public ScaleStyle getScaleStyle() {
        return this.scaleStyle;
    }

}
