package com.letv.shared.animation;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.Resources.Theme;
import android.util.AttributeSet;
import android.view.animation.Interpolator;
import com.letv.shared.R;

public class LeEaseInQuartEaseOutCubicInterpolator implements Interpolator {
    private float divide, height;
    
    public LeEaseInQuartEaseOutCubicInterpolator() {
        this(0.2f, 0.3f);
    }

    public LeEaseInQuartEaseOutCubicInterpolator(float divide, float height) {
        this.divide = divide;
        this.height = height;
    }
    
    public LeEaseInQuartEaseOutCubicInterpolator(Resources resources, Theme theme, AttributeSet attrs) {
        TypedArray a;
        if (theme != null) {
            a = theme.obtainStyledAttributes(attrs, R.styleable.LeEaseInOutInterpolator, 0, 0);
        } else {
            a = resources.obtainAttributes(attrs, R.styleable.LeEaseInOutInterpolator);
        }
        
        divide = a.getFloat(R.styleable.LeEaseInOutInterpolator_leInOutDivide, 0.2f);
        height = a.getFloat(R.styleable.LeEaseInOutInterpolator_leHeightAtDivide, 0.3f);
        
        a.recycle();
    }
    
    public LeEaseInQuartEaseOutCubicInterpolator(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LeEaseInOutInterpolator);
        
        divide = a.getFloat(R.styleable.LeEaseInOutInterpolator_leInOutDivide, 0.2f);
        height = a.getFloat(R.styleable.LeEaseInOutInterpolator_leHeightAtDivide, 0.3f);
        
        a.recycle();
    }
    
    @Override
    public float getInterpolation(float t) {

        if (t < divide)
            return EasingEquations.easeInQuart(t, 0, height, divide);
        else 
            return EasingEquations.easeOutCubic(t-divide, height, 1 - height, 1 - divide);
    }
}
