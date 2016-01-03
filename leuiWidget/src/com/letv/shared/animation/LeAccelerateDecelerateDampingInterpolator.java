package com.letv.shared.animation;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.Resources.Theme;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import com.letv.shared.R;

public class LeAccelerateDecelerateDampingInterpolator implements Interpolator{
    private float mDivideTime;
    private float mExtend;
    
    private float mAdjustFactor;
    private float mFactor;
    
    private Interpolator mInterpolator1;
    private Interpolator mInterpolator2;
    
    private static float mAdjustTime = 0.3f;
    
    public static Interpolator getDefaultInterpolator() {
        return new LeAccelerateDecelerateDampingInterpolator(0.5f, 0.1f);
    }
    
    public LeAccelerateDecelerateDampingInterpolator(float divideTime, float extend) {
        mDivideTime = divideTime;
        mExtend = extend;
        
        mInterpolator1 = new AccelerateDecelerateInterpolator();
        mInterpolator2 = new LeDampingInterpolator();
        
        mAdjustFactor = 0.143407f; // if (t == 0.143407f) LeDampingInterpolator() reach its max value
        
        mFactor = (1 - mAdjustFactor) / (1 - mDivideTime);
    }
    
    public LeAccelerateDecelerateDampingInterpolator(Resources resources, Theme theme, AttributeSet attrs) {
        TypedArray a;
        if (theme != null) {
            a = theme.obtainStyledAttributes(attrs, R.styleable.LeAccelerateDecelerateDampingInterpolator, 0, 0);
        } else {
            a = resources.obtainAttributes(attrs, R.styleable.LeAccelerateDecelerateDampingInterpolator);
        }
        
        mDivideTime = a.getFloat(R.styleable.LeAccelerateDecelerateDampingInterpolator_leDivideTime, 0.5f);
        mExtend = a.getFloat(R.styleable.LeAccelerateDecelerateDampingInterpolator_leExtend, 0.1f);
        
        a.recycle();
        
        mInterpolator1 = new AccelerateDecelerateInterpolator();
        mInterpolator2 = new LeDampingInterpolator();
        
        mAdjustFactor = 0.143407f; // if (t == 0.143407f) LeDampingInterpolator() reach its max value
        
        mFactor = (1 - mAdjustFactor) / (1 - mDivideTime);
    }
    
    public LeAccelerateDecelerateDampingInterpolator(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LeAccelerateDecelerateDampingInterpolator);
        
        mDivideTime = a.getFloat(R.styleable.LeAccelerateDecelerateDampingInterpolator_leDivideTime, 0.5f);
        mExtend = a.getFloat(R.styleable.LeAccelerateDecelerateDampingInterpolator_leExtend, 0.1f);

        a.recycle();
        
        mInterpolator1 = new AccelerateDecelerateInterpolator();
        mInterpolator2 = new LeDampingInterpolator();
        
        mAdjustFactor = 0.143407f; // if (t == 0.143407f) LeDampingInterpolator() reach its max value
        
        mFactor = (1 - mAdjustFactor) / (1 - mDivideTime);
    }
    
    @Override
    public float getInterpolation(float t) {
        if (t < mDivideTime) {
            return (1f + mExtend) * mInterpolator1.getInterpolation(t/mDivideTime);
        } else {
            return mExtend * mInterpolator2.getInterpolation((t-mDivideTime)*mFactor + mAdjustFactor) + 1f;
        }
    }

}
