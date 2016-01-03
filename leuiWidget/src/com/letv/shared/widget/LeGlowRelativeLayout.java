package com.letv.shared.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import com.letv.shared.R;

public class LeGlowRelativeLayout extends RelativeLayout {
    private LeGlowDelegate mDelegate;

    public LeGlowRelativeLayout(Context context) {
        this(context, null);
    }

    public LeGlowRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LeGlowRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mDelegate = new LeGlowDelegate(this);

        // read attr
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LeGlowDelegate);

        setEnabledAnimation(a.getBoolean(R.styleable.LeGlowDelegate_leGlowEnabledAnim, true));
        setPressColor(a.getInt(R.styleable.LeGlowDelegate_leGlowPressColor, Color.BLACK));
        setPressScaleMultiple(a.getFloat(R.styleable.LeGlowDelegate_leGlowPressScaleMultiple, mDelegate.backRoundRectPressScaleEnd));
        setPressScaleAlpha(a.getInt(R.styleable.LeGlowDelegate_leGlowPressScaleAlpha, mDelegate.backRoundRectPressAlphaEnd));

        a.recycle();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mDelegate.init();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        mDelegate.draw(canvas);
    }

    @Override
    public void setPressed(boolean pressed) {
        mDelegate.setPressed(pressed);
        super.setPressed(pressed);
    }

    public void setEnabledAnimation(boolean enable) {
        mDelegate.setAnimationEnabled(enable);
    }

    public void setPressColor(int color) {
        mDelegate.setBackRoundRectColor(color);
    }

    public void setPressScaleMultiple(float multiple) {
        mDelegate.setBackRoundRectScaleMultiple(multiple);
    }

    public void setPressScaleAlpha(int alpha) {
        mDelegate.setBackRoundRectScaleAlpha(alpha);
    }

}
