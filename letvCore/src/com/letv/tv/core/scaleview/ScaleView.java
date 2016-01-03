package com.letv.tv.core.scaleview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class ScaleView extends View {

    public ScaleView(Context context) {
        super(context);
    }

    public ScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScaleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        try {
            ScaleCalculator.getInstance().scaleView(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
