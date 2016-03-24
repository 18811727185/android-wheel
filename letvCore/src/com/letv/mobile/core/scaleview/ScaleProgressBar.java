package com.letv.mobile.core.scaleview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

/**
 * Created by hupei on 16/3/24.
 */
public class ScaleProgressBar extends ProgressBar implements ScaleStyleInterface {
    private ScaleParameter mScaleParameter;

    public ScaleProgressBar(Context context) {
        super(context);
        init(context, null);
    }

    public ScaleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ScaleProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mScaleParameter = new ScaleParameter(context, attrs);
    }

    @Override
    public ScaleStyle getScaleStyle() {
        return this.mScaleParameter.getScaleStyle();
    }

}
