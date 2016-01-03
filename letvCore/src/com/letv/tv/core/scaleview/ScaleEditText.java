package com.letv.tv.core.scaleview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by lizhennian on 2014/6/3.
 */
public class ScaleEditText extends EditText {
    public ScaleEditText(Context context) {
        super(context);
        this.setTextSize(this.getTextSize());
    }

    public ScaleEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setTextSize(this.getTextSize());
    }

    public ScaleEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
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
