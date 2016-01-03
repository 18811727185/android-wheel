package com.letv.shared.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

public class LeOverflowTab extends RelativeLayout{
	
	private View mNormalView;
	private View mFloatView;
	
	AnimatorSet mScaleAnimatorSet;
	
	public LeOverflowTab(Context context) {
		super(context);
	}
	
	public LeOverflowTab(Context context, AttributeSet attrs) {
	    super(context, attrs);
    }

	public LeOverflowTab(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		this.setClickable(true);
	}
	
	public void setNormalView(View v) {
		if (mNormalView != null)
			this.removeView(mNormalView);
		
		mNormalView = v;
		this.addView(mNormalView);
	}
	
	public void setFloatView(View v) {
	    mFloatView = v;
	}
	
	@Override
	public void setSelected(boolean selected) {
		super.setSelected(selected);
		setFloatMode(selected);
	}
	
    public void setFloatMode(boolean floatMode) {
        if (floatMode) {
            mNormalView.setVisibility(View.INVISIBLE);
            mFloatView.setVisibility(View.VISIBLE);
            
            startScaleAnimation();
        } else {
            mNormalView.setVisibility(View.VISIBLE);
            mFloatView.setVisibility(View.GONE);
        }
    }
    
    private void startScaleAnimation() {
        cancelScaleAnimation();
        createScaleAnimation();
        mScaleAnimatorSet.start();
    }
    
    public void createScaleAnimation() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mFloatView, "scaleX", 0f, 1f);
        scaleX.setDuration(150);
        
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mFloatView, "scaleY", 0f, 1f);
        scaleY.setDuration(150);
        
        mScaleAnimatorSet = new AnimatorSet();
        mScaleAnimatorSet.playTogether(scaleX, scaleY);
    }
    
    public void cancelScaleAnimation() {
        
        if (mScaleAnimatorSet != null) {
            mScaleAnimatorSet.cancel();
        }
    }
}


















