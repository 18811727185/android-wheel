package com.letv.shared.animation;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.Resources.Theme;
import android.util.AttributeSet;
import android.view.animation.Interpolator;
import com.letv.shared.R;

public class LeDampingInterpolator implements Interpolator{
    private float mCycle; 
    private float mAttenFactor;
    private float mAmplitude;
    
    private float mInputWhenMaxOutput = 0f;
    private float mMaxOutput = 0f;
    
    public LeDampingInterpolator() {
        // cycle = 1.5, atten = 0.5
        mCycle = 1.5f;
        mAttenFactor = 2.0794f;  // 3*ln2
        mAmplitude = 1.38047f;  // 1.4142135 / 1.0233379
    }
    
    // cycle - how many cycles from wave crest to valley
    // atten - atten = height_of_the_last_wave_crest / height_of_the_next_wave_crest
    public LeDampingInterpolator(float cycle, float atten) {
        mCycle = cycle;
        mAttenFactor = (float)(-2f * mCycle * Math.log(atten) );
        mAmplitude = (float) (Math.pow(Math.E, mAttenFactor/(4.0*mCycle)) );
    }
    
    public LeDampingInterpolator(Resources resources, Theme theme, AttributeSet attrs) {
        TypedArray a;
        if (theme != null) {
            a = theme.obtainStyledAttributes(attrs, R.styleable.LeDampingInterpolator, 0, 0);
        } else {
            a = resources.obtainAttributes(attrs, R.styleable.LeDampingInterpolator);
        }
        
        mCycle = a.getFloat(R.styleable.LeDampingInterpolator_leCycle, 1.5f);
        float atten = a.getFloat(R.styleable.LeDampingInterpolator_leAtten, 0.5f);
        
        a.recycle();
        
        mAttenFactor = (float)(-2f * mCycle * Math.log(atten) );
        mAmplitude = (float) (Math.pow(Math.E, mAttenFactor/(4.0*mCycle)) );
    }
    
    public LeDampingInterpolator(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LeDampingInterpolator);
        
        mCycle = a.getFloat(R.styleable.LeDampingInterpolator_leCycle, 1.5f);
        float atten = a.getFloat(R.styleable.LeDampingInterpolator_leAtten, 0.5f);

        a.recycle();
        
        mAttenFactor = (float)(-2f * mCycle * Math.log(atten) );
        mAmplitude = (float) (Math.pow(Math.E, mAttenFactor/(4.0*mCycle)) );
    }
    
    @Override
    public float getInterpolation(float t) {
        double output = mAmplitude * Math.pow(Math.E, -mAttenFactor * t) * Math.sin(2*Math.PI*mCycle*t);
        
        if (mMaxOutput < (float)output && output > 1) {
            mMaxOutput = (float)output;
            mInputWhenMaxOutput = t;
        }

        return (float)output;
    }
    
    public float getMaxOutput() {
        return mMaxOutput;
    }
    
    public float getInputWhenMaxOutput() {
        return mInputWhenMaxOutput;
    }
    
}
