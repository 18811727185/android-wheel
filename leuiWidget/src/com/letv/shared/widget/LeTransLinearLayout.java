package com.letv.shared.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * Created by liangchao on 15-1-13.
 */
public class LeTransLinearLayout extends LinearLayout{
    public void setIntercept(boolean isIntercept) {
        this.isIntercept = isIntercept;
    }

    private boolean isIntercept = false;

    public void setTransparentHelper(LeLayoutTransparentHelper transparentHelper) {
        this.transparentHelper = transparentHelper;
    }
    public void setHidePercent(float hidePercent) {
        this.hidePercent = hidePercent;
    }

    private float hidePercent;

    private LeLayoutTransparentHelper transparentHelper;

    public LeTransLinearLayout(Context context) {
        super(context);
    }

    public LeTransLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

//    public LeTransLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//    }
//
//    public LeTransLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(isIntercept){
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if(transparentHelper!=null){
            transparentHelper.setHidePercent(hidePercent);
            transparentHelper.draw(canvas);
        }

    }
}
