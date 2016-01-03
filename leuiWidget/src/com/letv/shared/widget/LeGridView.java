package com.letv.shared.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

/**
 * Created by liangchao on 14-12-11.
 */
public class LeGridView extends GridView {


    public LeGridView(Context context) {
        super(context);
    }

    public LeGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LeGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (ev.getAction()==MotionEvent.ACTION_UP||ev.getAction()==MotionEvent.ACTION_CANCEL){
            for (int i = 0;i<getChildCount();i++){
                getChildAt(i).setAlpha(1f);
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
