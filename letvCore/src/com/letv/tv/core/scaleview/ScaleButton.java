package com.letv.tv.core.scaleview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by lizhennian on 2014/5/30.
 */
public class ScaleButton extends Button {
    public ScaleButton(Context context) {
        super(context);
        this.setTextSize(this.getTextSize());
    }

    public ScaleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTextSize(this.getTextSize());
    }

    public ScaleButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setTextSize(this.getTextSize());
    }

    @Override
    public void setTextSize(float textSize) {
        this.setTextSize(0, textSize);
    }

    @Override
    public void setTextSize(int unit, float textSize) {
        try {
            textSize = ScaleCalculator.getInstance().scaleTextSize(textSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.setTextSize(unit, textSize);
    }
}
