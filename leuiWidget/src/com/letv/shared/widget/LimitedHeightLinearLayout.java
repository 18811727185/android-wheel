package com.letv.shared.widget;

import static android.view.View.MeasureSpec.EXACTLY;

import com.letv.shared.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * @hide
 */
public class LimitedHeightLinearLayout extends LinearLayout {

    private int mMaxHeight = Integer.MAX_VALUE;
    
    /**
     * @param context
     */
    public LimitedHeightLinearLayout(Context context) {
        super(context);
    }
    
    /**
     * @param context
     * @param attrs
     */
    public LimitedHeightLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LimitedHeightLinearLayout, 0, 0);
        mMaxHeight = a.getDimensionPixelSize(R.styleable.LimitedHeightLinearLayout_linearLayoutMaxHeight, Integer.MAX_VALUE);
        a.recycle();
    }


    /**
     * (non-Javadoc)
     * @see android.widget.LinearLayout#onMeasure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
       
        int height = getMeasuredHeight();
        boolean measure = false;
        
        // The height can't > mMaxHeight
        if (height > mMaxHeight) {
            height = mMaxHeight;
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height,EXACTLY);
            measure = true;
        }
        
        if (measure) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * Set max height
     * @param max  the max value
     */
    public void setMaxHeight(int max) {
        mMaxHeight = max;
    }

    /**
     * Get max height
     * @return max  the max value
     */
    public int getMaxHeight() {
        return mMaxHeight;
    }
}
