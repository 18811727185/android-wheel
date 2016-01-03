package com.letv.mobile.core.scaleview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by lizhennian on 2014/5/30.
 */
public class ScaleRelativeLayout extends RelativeLayout implements
        ScaleStyleInterface {

    private ScaleParameter mScaleParameter;

    public ScaleRelativeLayout(Context context) {
        super(context);
        this.init(context, null);
    }

    public ScaleRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init(context, attrs);
    }

    public ScaleRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mScaleParameter = new ScaleParameter(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        try {
            ScaleCalculator.getInstance().scaleViewGroup(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public ScaleStyle getScaleStyle() {
        return this.mScaleParameter.getScaleStyle();
    }
}
