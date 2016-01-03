package com.letv.shared.widget;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Custom {@link com.letv.shared.widget.LeQuickContactBadge} that improves layouting performance
 * 
 * This improves the performance by not passing requestLayout() to its parent, taking advantage 
 * of knowing that image size won't change once set.
 */
public class LeLayoutSuppressingQuickContactBadge extends LeQuickContactBadge {

    public LeLayoutSuppressingQuickContactBadge(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    public void requestLayout() {
        forceLayout();
    }

}
