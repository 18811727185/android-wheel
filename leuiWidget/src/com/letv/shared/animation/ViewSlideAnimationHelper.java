package com.letv.shared.animation;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class ViewSlideAnimationHelper {
    private View view;
    private int duration;
    private TimeInterpolator timeInterpolator;
    private static final int DURATION_DEFAULT = 350;
    private static final TimeInterpolator TIMEINTERPOLATOR_DEFAULT = new AccelerateDecelerateInterpolator();
    public ViewSlideAnimationHelper(View view, int duration,TimeInterpolator timeInterpolator) {
        this.view = view;
        this.duration = duration;
        this.timeInterpolator = timeInterpolator;
    }
    public ViewSlideAnimationHelper(View view, int duration) {
        this.view = view;
        this.duration = duration;
        this.timeInterpolator = TIMEINTERPOLATOR_DEFAULT;
    }
    public ViewSlideAnimationHelper(View view) {
        this.view = view;
        this.duration = DURATION_DEFAULT;
        this.timeInterpolator = TIMEINTERPOLATOR_DEFAULT;
    }
    public TimeInterpolator getTimeInterpolator() {
        return timeInterpolator;
    }
    public void setTimeInterpolator(TimeInterpolator timeInterpolator) {
        this.timeInterpolator = timeInterpolator;
    }
    public View getView() {
        return view;
    }
    public void setView(View view) {
        this.view = view;
    }
    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void slideInFromTop() {

        if (view.getVisibility()!=View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
        }
        
        ObjectAnimator topIn = ObjectAnimator.ofFloat(view, "y", view.getTop()-view.getHeight(),view.getTop());
        
        topIn.setInterpolator(timeInterpolator);

        
        topIn.setDuration(duration);
        topIn.start();
    }
    
    public void slideOutFromTop() {
        
        if (view.getVisibility()!=View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
        }
        
        ObjectAnimator topOut = ObjectAnimator.ofFloat(view, "y", view.getTop(),view.getTop()-view.getHeight());
        
        topOut.setInterpolator(timeInterpolator);
        topOut.setDuration(duration);
        topOut.start();
    }
    
    public void slideInFromBottom() {
        
        if (view.getVisibility()!=View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
        }
        
        ObjectAnimator bottomIn = ObjectAnimator.ofFloat(view, "y", view.getTop()+view.getHeight(),view.getTop());
        
        bottomIn.setInterpolator(timeInterpolator);
        
        
        bottomIn.setDuration(duration);
        bottomIn.start();
    }
    
    public void slideOutFromBottom() {

        if (view.getVisibility()!=View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
        }
        
        ObjectAnimator bottomOut = ObjectAnimator.ofFloat(view, "y", view.getTop(),view.getTop()+view.getHeight());
    
        bottomOut.setInterpolator(timeInterpolator);
        
        bottomOut.setDuration(duration);
        bottomOut.start();
        
    }
    public void slideInFromLeft() {
       
        if (view.getVisibility()!=View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
        }
        
        ObjectAnimator leftIn = ObjectAnimator.ofFloat(view, "x", view.getLeft()-view.getWidth(),view.getLeft());
      
        leftIn.setInterpolator(timeInterpolator);
        
        
        leftIn.setDuration(duration);
        leftIn.start();
    }
    
    public void slideOutFromLeft() {
        
        if (view.getVisibility()!=View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
        }
        
        ObjectAnimator leftOut = ObjectAnimator.ofFloat(view, "x", view.getLeft(),view.getLeft()-view.getWidth());
        
        leftOut.setInterpolator(timeInterpolator);
        
        
        leftOut.setDuration(duration);
        leftOut.start();
    }
    public void slideInFromRight() {

        if (view.getVisibility()!=View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
        }
        
        ObjectAnimator rightIn = ObjectAnimator.ofFloat(view, "x", view.getLeft()+view.getWidth(),view.getLeft());
       
        rightIn.setInterpolator(timeInterpolator);
        
        
        rightIn.setDuration(duration);
        rightIn.start();
    }
    
    public void slideOutFromRight() {

        if (view.getVisibility()!=View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
        }
        
        ObjectAnimator rightOut = ObjectAnimator.ofFloat(view, "x",view.getLeft(), view.getLeft()+view.getWidth());
        
        rightOut.setInterpolator(timeInterpolator);
        
        
        rightOut.setDuration(duration);
        rightOut.start(); 
    }
}
