package com.letv.mobile.core.scaleview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by lizhennian on 2014/5/30.
 */
public class ScaleListView extends ListView {

    public ScaleListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
    }

    public ScaleListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init(context, attrs);
    }

    public ScaleListView(Context context) {
        super(context);
        this.init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
    }
}
