package com.letv.tv.core.scaleview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by lizhennian on 2014/5/30.
 */
public class ScaleRelativeLayout extends RelativeLayout {
    public ScaleRelativeLayout(Context context) {
        super(context);
    }

    public ScaleRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ScaleRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
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
}
