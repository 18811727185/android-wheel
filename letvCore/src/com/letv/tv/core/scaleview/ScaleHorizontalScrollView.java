package com.letv.tv.core.scaleview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

public class ScaleHorizontalScrollView extends HorizontalScrollView {
    public ScaleHorizontalScrollView(Context context) {
        super(context);
    }

    public ScaleHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScaleHorizontalScrollView(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
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
