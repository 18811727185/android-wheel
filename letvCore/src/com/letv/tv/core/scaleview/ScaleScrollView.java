package com.letv.tv.core.scaleview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by lizhennian on 2014/5/30.
 */
public class ScaleScrollView extends ScrollView {

    public ScaleScrollView(Context context) {
        super(context);
    }

    public ScaleScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScaleScrollView(Context context, AttributeSet attrs, int defStyle) {
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
