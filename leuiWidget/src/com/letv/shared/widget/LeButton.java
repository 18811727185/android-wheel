package com.letv.shared.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;
import android.graphics.drawable.GradientDrawable;

/**
 * Created by liangchao on 14-10-29.
 */
public class LeButton extends Button{

    private boolean isPressState2 = false;
    private Drawable mDrawable;
    private static int strokeWidth;
    private int textColor;
    private int bgColor;
    private int stokeColor;
    private GradientDrawable gd;
    private Context context;
    private boolean textColorSet = false;
    private boolean bgColorSet = false;


    private static int dip2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public void setTextColor(int textColor) {
        super.setTextColor(textColor);
        this.textColor = textColor;
        textColorSet = true;

    }


    public void setBgColor(int bgColor,int stokeColor) {
        gd = new GradientDrawable();
        gd.setCornerRadius(dip2px(context,3));
        gd.setStroke(strokeWidth,stokeColor);
        gd.setColor(bgColor);
        setBackground(gd);
        this.bgColor = bgColor;
        this.stokeColor = stokeColor;
        bgColorSet = true;

    }
    public void setPressState2(boolean isPressState2) {
        this.isPressState2 = isPressState2;
    }

    public LeButton(Context context,boolean isPressState2){
        super(context);
        this.context = context;
        this.isPressState2 = isPressState2;
        strokeWidth = dip2px(context,1);

    }

    public LeButton(Context context) {

        super(context);
        this.context = context;
        strokeWidth = dip2px(context,1);
    }

    public LeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        strokeWidth = dip2px(context,1);
    }

    public LeButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        strokeWidth = dip2px(context,1);
        this.context = context;
    }


    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (!isPressState2){
            if (!isEnabled()){
                setAlpha(0.3f);
            }else{
                setAlpha(1f);
            }
        }

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isPressState2){
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    setBgMask();
                    break;
                case MotionEvent.ACTION_UP:
                    resetBgMask();
                    break;
            }
        }else if(textColorSet&&bgColorSet){
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    swapTextColorWithBgColor();
                    break;
                case MotionEvent.ACTION_UP:
                    swapTextColorWithBgColor();
                    break;
                default:
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    private void resetBgMask() {
        mDrawable = getBackground();
        mDrawable.clearColorFilter();

    }

    private void setBgMask() {

        mDrawable = getBackground();
        mDrawable.setColorFilter(0x26000000, PorterDuff.Mode.OVERLAY);

    }

    private void swapTextColorWithBgColor(){
        int temp = this.textColor;
        this.textColor = this.bgColor;
        this.bgColor = temp;
        super.setTextColor(textColor);
        super.setBackground(getNewGradienDrawable());
    }

    private GradientDrawable getNewGradienDrawable(){

        gd.setColor(this.bgColor);
        return gd;
    }

}
