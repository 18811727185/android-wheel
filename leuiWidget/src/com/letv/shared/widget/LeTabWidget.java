package com.letv.shared.widget;

/*
 * Usage:
 * 1. add a top strip to TabWidget, and you can set top strip drawable and height in xml or by calling setTopStripDrawable()/setTopStripHeight
 *
 * Example:
 *
 *  import com.leui.widget.LeTabWidgetUtils;
 *  import android.support.v4.app.LeFragmentTabHost;
 *
 *  public class MainActivity extends FragmentActivity {
 *      private LeFragmentTabHost mTabHost;
 *
 *      @Override
 *      protected void onCreate(Bundle savedInstanceState) {
 *      super.onCreate(savedInstanceState);
 *          setContentView(R.layout.activity_main);
 *
 *          FrameLayout realTabContent = (FrameLayout)findViewById(R.id.realtabcontent);
 *
 *          mTabHost = (LeFragmentTabHost) findViewById(android.R.id.tabhost);
 *          mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent, realTabContent);
 *
 *          // removing divider
 *          LeTabWidget tw = mTabHost.getLeTabWidget();
 *          tw.setDividerDrawable(null);
 *          tw.setTopStripDrawable();
 *          tw.setTopStripHeight();
 *          ...
 *      }
 *  }
 */

import com.letv.shared.R;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabWidget;
import android.widget.TextView;

public class LeTabWidget extends TabWidget {
	
	private Drawable mTopStrip;
	private int mTopStripHeight;

	public LeTabWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	public LeTabWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public LeTabWidget(Context context) {
		super(context);
	}
	
	@Override
    public void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		
		int left = getLeft();
		int top = 0;
		int right = getRight();
		int bottom = mTopStripHeight;
		
		final Drawable topStrip = mTopStrip;
		if (topStrip != null && mTopStripHeight != 0) {
		    topStrip.setBounds(left, top, right, bottom);
		    topStrip.draw(canvas);
		}
	}	
	
	private void init(Context context, AttributeSet attrs) {
		if (attrs == null)
			return;
		
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LeTabWidget);
		
		int topStripRes = ta.getResourceId(R.styleable.LeTabWidget_leTopStrip, -1);
		if (topStripRes != -1) {
			mTopStrip = getContext().getResources().getDrawable(topStripRes);
		} else { // set default strip
			//mTopStrip = getContext().getResources().getDrawable(R.color.le_tab_indicator_top_strip_color);
		}
		
		int topStripHeight = (int) ta.getDimension(R.styleable.LeTabWidget_leTopStripHeight, 0);
		if (topStripHeight != 0 && mTopStrip != null) {
			mTopStripHeight = topStripHeight;
		} else {
			//mTopStripHeight = getResources().getDimensionPixelSize(R.dimen.le_tab_indicator_top_strip_height);
		}
		
		ta.recycle();
	}
	
	public void setTopStripDrawable(int resId) {
		setTopStripDrawable(getContext().getResources().getDrawable(resId));
	}
	
	public void setTopStripDrawable(Drawable drawable) {
		mTopStrip = drawable;
		//requestLayout();
		invalidate();
	}
	
	public void setTopStripHeight(int height) {
		mTopStripHeight = height;
		invalidate();
	}
    
    public ImageView getTabIcon(int pos) {
        View tab = getTabView(pos);
        if (tab == null)
            return null;
        
        ImageView img = (ImageView) tab.findViewById(R.id.icon);
        return img;
    }
    
    public TextView getTabTitle(int pos) {
        View tab = getTabView(pos);
        if (tab == null)
            return null;
        
        TextView tv = (TextView) tab.findViewById(R.id.title);
        return tv;
    }
    
    public void setTitleTextColor(int resId) {
        for (int i = 0; i < getChildCount(); i++) {
            TextView tv = getTabTitle(i);
            if (tv != null)
                tv.setTextColor(getResources().getColorStateList(resId));
        }
    }
    
    public void setTitleTextColor(int unselected_color, int selected_color) {
        final int[][] states = new int[][] {
            { android.R.attr.state_selected },
            {}
        };
        final int[] colors = new int[] {
            selected_color, unselected_color
        };
        ColorStateList list = new ColorStateList(states, colors);
        for (int i = 0; i < getChildCount(); i++) {
            TextView tv = getTabTitle(i);
            if (tv != null)
                tv.setTextColor(list);
        }
    }
    
    public View getTabView(int pos) {
        
        if (pos < 0 || pos >= getChildCount())
            return null;
        
        return this.getChildAt(pos);
    }
}
