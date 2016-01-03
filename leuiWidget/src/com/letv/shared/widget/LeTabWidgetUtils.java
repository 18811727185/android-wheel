package com.letv.shared.widget;

/*
 * Usage:
 * 1. provide an static createIndicatorView(...) to create an indicator view according to Leui's UI design
 * 2. provide an static setTabWidgetLayout(...) to adjust indicator views layout according to Leui's UI design
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
 *          TabWidget tw = mTabHost.getTabWidget();
 *          tw.setDividerDrawable(null);
 *
 *          View indicatorView;
 *          LayoutInflater inflater =
 *                  (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 *
 *          // create an tab indicator view with icon and title
 *          indicatorView = LeTabWidgetUtils.createIndicatorView(inflater, 
 *                  tw, 
 *                  R.drawable.calendar, 
 *                  getString(R.string.calendar));
 *          mTabHost.addTab(mTabHost.newTabSpec("calendar").setIndicator(indicatorView), 
 *                  BrowserFragment.class, null);
 *
 *          // create an tab indicator view with icon only
 *          indicatorView = LeTabWidgetUtils.createIndicatorView(inflater, 
 *                  tw, 
 *                  R.drawable.writer, 
 *                  null);
 *          mTabHost.addTab(mTabHost.newTabSpec("writer").setIndicator(indicatorView), 
 *                  Fragment2.class, null);
 *
 *          // adjust tab indicator views layout to fit Leui's UI design
 *          LeTabWidgetUtils.setTabWidgetLayout(this, tw);
 *          ...
 *      }    
 *  }
 */

import com.letv.shared.R;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabWidget;
import android.widget.TextView;

public class LeTabWidgetUtils {
        
    /**
     * Create an indicator view according to Leui's UI design 
     */
    public static View createIndicatorView(LayoutInflater inflater, TabWidget tw, int iconId, String title) {
        if (inflater == null || tw == null)
            return null;
        
        View v = null;
        if (title != null && !title.isEmpty()) {
            //v = inflater.inflate(R.layout.le_tab_indicator_with_icon_title_linearlayout, tw, false);
            v = inflater.inflate(R.layout.le_bottom_tab_with_icon_title, tw, false);
            TextView tv = (TextView) v.findViewById(R.id.title);
            tv.setText(title);
        } else {
            //v = inflater.inflate(R.layout.le_tab_indicator_with_icon_only_linearlayout, tw, false);
            v = inflater.inflate(R.layout.le_bottom_tab_with_icon_only, tw, false);
        }

        ImageView imgView = (ImageView) v.findViewById(R.id.icon);
        imgView.setImageResource(iconId);

        return v;
    }
    
    private static boolean hasTitle(TabWidget tw) {
        for (int i=0; i<tw.getTabCount(); i++) {
            View tab = tw.getChildAt(i);
            if (tab != null && tab instanceof ViewGroup) {
                ViewGroup vGroup = (ViewGroup)tab;
                if (vGroup.getChildCount() <= 1)
                    continue;
                
                for (int j=0; j<vGroup.getChildCount(); j++) {
                    if (vGroup.getChildAt(j) instanceof TextView)
                        return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * set tab indicator view's leftMargin and rightMargin to fit Leui's UI design 
     */
    public static void setTabWidgetLayout(Activity activity, TabWidget tw) {
        setTabWidgetLayout(activity, tw, false);
    }
    
    public static void setTabWidgetLayout(Activity activity, TabWidget tw, boolean isDialerApp) {
        if (activity == null || tw == null || tw.getChildCount() == 0)
            return;
        
        
        setTabWidgetLayout(activity, tw, hasTitle(tw), isDialerApp ); 
    }
    
    public static void setTabWidgetLayout(Activity activity, LinearLayout bottomView, boolean hasTitle) {
        if (activity == null || bottomView == null || bottomView.getChildCount() == 0)
            return;
        
        setTabWidgetLayout(activity.getApplicationContext(), bottomView, hasTitle, false);
    }
    
    private static void setTouchGlowWidth2HeightRatio(Resources res, ViewGroup vGroup) {
        int childCnt = vGroup.getChildCount();
        float width2HeightRatio = 2f;
        
        if (childCnt == 1) {
            width2HeightRatio = res.getInteger(R.integer.le_bottom_1_tabs_touch_flow_width_2_height_ratio) / 100f;
        } else if (childCnt == 2) {
            width2HeightRatio = res.getInteger(R.integer.le_bottom_2_tabs_touch_flow_width_2_height_ratio) / 100f;
        } else if (childCnt == 3) {
            width2HeightRatio = res.getInteger(R.integer.le_bottom_3_tabs_touch_flow_width_2_height_ratio) / 100f;
        } else if (childCnt == 4) {
            width2HeightRatio = res.getInteger(R.integer.le_bottom_4_tabs_touch_flow_width_2_height_ratio) / 100f;
        } else if (childCnt == 5) {
            width2HeightRatio = res.getInteger(R.integer.le_bottom_5_tabs_touch_flow_width_2_height_ratio) / 100f;
        }
        
        for (int i=0; i<childCnt; i++) {
            View view = vGroup.getChildAt(i);
            if (view != null && view instanceof LeGlowRelativeLayout) {
                ((LeGlowRelativeLayout)view).setPressScaleMultiple(width2HeightRatio);
            }
        }
    }
    
    public static void setTabWidgetLayout(Context context, LinearLayout bottomView, boolean hasTitle, boolean isDialerApp) {
        if (context == null || bottomView == null || bottomView.getChildCount() == 0)
            return;
        
        Resources res = context.getResources();
        
        // get screen width in px
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        
        // get tab width in px
        int tabWidth = 0;
        if (hasTitle ) {
            tabWidth = res.getDimensionPixelOffset(R.dimen.le_bottom_tab_with_icon_title_width);
        } else {
            tabWidth = res.getDimensionPixelOffset(R.dimen.le_bottom_tab_with_icon_only_width);
        }
        
        // get tab gaps
        int count = bottomView.getChildCount();
        int end = 0;
        int gap = 0;
        double ratio = 0.0;
        
        switch (count) {
            case 1:
                end = (screenWidth - tabWidth * count) / 2;
                break;
            case 2:
                ratio = res.getInteger(R.integer.le_bottom_2_tabs_screen_ratio) / 100000.0;
                end = (int)(screenWidth * (1.0 - ratio) / 2);
                gap = (int)((screenWidth - end * 2 - tabWidth * count) / (count - 1));
                break;
            case 3:
                if (isDialerApp) {
                    ratio = res.getInteger(R.integer.le_bottom_3_tabs_screen_dialer_ratio) / 100000.0;
                } else {
                    ratio = res.getInteger(R.integer.le_bottom_3_tabs_screen_ratio) / 100000.0;
                }
                end = (int)(screenWidth * (1.0 - ratio) / 2);
                gap = (int)((screenWidth - end * 2 - tabWidth * count) / (count - 1));
                break;
            case 4:
                ratio = res.getInteger(R.integer.le_bottom_4_tabs_screen_ratio) / 100000.0;
                end = (int)(screenWidth * (1.0 - ratio) / 2);
                gap = (int)((screenWidth - end * 2 - tabWidth * count) / (count - 1));
                break;
            case 5:
                ratio = res.getInteger(R.integer.le_bottom_5_tabs_screen_ratio) / 100000.0;
                end = (int)(screenWidth * (1.0 - ratio) / 2);
                gap = (int)((screenWidth - end * 2 - tabWidth * count) / (count - 1));
                break;

            default:
                break;
        }
        
        // layout tabs
        for (int i=0; i<count; i++) {
            final View tab = bottomView.getChildAt(i);
            
            // set width/height/leftMargin/rightMargin for tab indicator view
            //LinearLayout.LayoutParams lp =(LinearLayout.LayoutParams) tab.getLayoutParams();
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)tab.getLayoutParams();
            if (params == null) {
                params = new ViewGroup.MarginLayoutParams(tabWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, 0);
            }
            
            if (i == 0) {
                params.leftMargin = end;
                params.rightMargin = gap / 2;
            } else if (i == count - 1){
                params.leftMargin = gap / 2;
                params.rightMargin = end;
            } else {
                params.leftMargin = gap / 2;
                params.rightMargin = gap / 2;
            }
            
            tab.setLayoutParams(params);
        }
        
        // set touch glow width/height ratio
        setTouchGlowWidth2HeightRatio(res, bottomView);
    }
    
    public static View createDefaultFloatingView(LayoutInflater inflater, int floatIconId) {
    	ImageView view = (ImageView)inflater.inflate(R.layout.le_tab_indicator_default_floating_view, null, false);
    	view.setImageResource(floatIconId);
    	return view;
    }
    
    public static WindowManager.LayoutParams createDefaultFloatingViewLayoutParams(Context context) {
        if (context == null)
            return null;
        
    	WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();

    	wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
    	wmParams.format = PixelFormat.RGBA_8888;

    	wmParams.flags = 
    			WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
    			| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
    	        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
    	wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
    	wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
    	wmParams.x = 0;
    	wmParams.y = context.getResources().getDimensionPixelSize(R.dimen.le_tab_indicator_float_view_marginbottom);
    	wmParams.gravity = Gravity.BOTTOM;
    	return wmParams;
    }
    
    /**
     * Create an indicator view 
     * If selected, a floating view will be shown
     * If not selected, a normal view will be shown
     */
    public static View createFloatingIndicatorView(LayoutInflater inflater, TabWidget tw, int iconId, String title, View floatingView) {
        if (inflater == null || tw == null || floatingView == null)
            return null;
        
        View normalView = createIndicatorView(inflater, tw, iconId, title);
        
        LeOverflowTab view = new LeOverflowTab(tw.getContext());

        LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) normalView.getLayoutParams();
        if (params2 != null) {
            view.setLayoutParams(params2);
        }
        
        view.setNormalView(normalView);
        view.setFloatView(floatingView);
        
        return view;
    }
}
