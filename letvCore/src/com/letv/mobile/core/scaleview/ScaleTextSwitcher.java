package com.letv.mobile.core.scaleview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextSwitcher;

/**
 * Created by hupei on 16/3/24.
 */
public class ScaleTextSwitcher extends TextSwitcher implements ScaleStyleInterface {


    private ScaleParameter mScaleParameter;

    public ScaleTextSwitcher(Context context) {
        super(context);
        this.init(context,null);
    }

    public ScaleTextSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mScaleParameter = new ScaleParameter(context, attrs);
    }
    @Override
    public ScaleStyle getScaleStyle() {
        return this.mScaleParameter.getScaleStyle();
    }
}
