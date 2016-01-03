package com.letv.shared.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import android.widget.LinearLayout;

/**
 * Created by liangchao on 14-12-11.
 */
public class LeGridView_LinearLayout extends LinearLayout{


    public LeGridView_LinearLayout(Context context) {
        super(context);


    }

    public LeGridView_LinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);


    }

    public LeGridView_LinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction()==MotionEvent.ACTION_DOWN){
            setAlpha(0.7f);
        }

        return super.dispatchTouchEvent(ev);
    }

}
