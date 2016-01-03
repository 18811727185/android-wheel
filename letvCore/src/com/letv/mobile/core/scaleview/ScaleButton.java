package com.letv.mobile.core.scaleview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by lizhennian on 2014/5/30.
 */
public class ScaleButton extends Button implements ScaleStyleInterface {

    private ScaleParameter mScaleParameter;

    public ScaleButton(Context context) {
        super(context);
        this.init(context, null);
        this.setTextSize(this.getTextSize());
    }

    public ScaleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
        this.setTextSize(this.getTextSize());
    }

    public ScaleButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init(context, attrs);
        this.setTextSize(this.getTextSize());
    }

    private void init(Context context, AttributeSet attrs) {
        this.mScaleParameter = new ScaleParameter(context, attrs);
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

    @Override
    public ScaleStyle getScaleStyle() {
        return this.mScaleParameter.getScaleStyle();
    }
}
