package com.letv.mobile.core.scaleview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by lizhennian on 2014/5/29.
 */
public class ScaleTextView extends TextView implements ScaleStyleInterface {

    private ScaleParameter mScaleParameter;

    public ScaleTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init(context, attrs);
    }

    public ScaleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
    }

    public ScaleTextView(Context context) {
        super(context);
        this.init(context, null);
    }

    @SuppressLint("NewApi")
    private void init(Context context, AttributeSet attrs) {
        this.mScaleParameter = new ScaleParameter(context, attrs);

        this.setTextSize(this.getTextSize());

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            float lineSpacingExtra = this.getLineSpacingExtra();
            lineSpacingExtra = ScaleCalculator.getInstance().scaleHeight(
                    (int) lineSpacingExtra);
            this.setLineSpacing(lineSpacingExtra,
                    this.getLineSpacingMultiplier());
        }
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
