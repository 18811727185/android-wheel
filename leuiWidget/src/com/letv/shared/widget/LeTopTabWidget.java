
package com.letv.shared.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.LinearLayout;
import com.letv.shared.R;
import java.util.ArrayList;

import android.view.Gravity;
import android.view.View;
import android.content.res.ColorStateList;

public class LeTopTabWidget extends LinearLayout {

    private Context mContext;
    private int mTabCounts = 2;
    private int mTabWidgetBgId;
    private int mTabLeftBgId;
    private int mTabRightBgId;
    private int mTabRecBgId;
    private int mTabTextColorId;
    private int mTabWidth = 504;
    private int mTabHeight = 84;
    private int mTabTextSize = 42;
    private boolean isAdd = false;
    private int mCurrentIndex = -1;
    private ArrayList<Button> mTabWidgetBtns = new ArrayList<Button>();
    private ColorStateList btnPressColor ;
    private ColorStateList btnColor;
    private int mDivideWidth =2;
    public LeTopTabWidget(Context context) {
        super(context);
        mContext = context;

    }

    public void addTab(int tabCounts, int TabWidgetwidth, int TabWidgetheight) {
        mTabCounts = tabCounts;
        mTabWidth = TabWidgetwidth;
        mTabHeight = TabWidgetheight;
        initView();
        requestLayout();
    }

    public LeTopTabWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.leTopTabWidget);
        mTabWidgetBgId = ta.getResourceId(R.styleable.leTopTabWidget_leTabWidgetBgSelector,
                R.drawable.le_tab_widget_bg_selector);
        mTabLeftBgId = ta.getResourceId(R.styleable.leTopTabWidget_leTabWidgetLeftSelector,
                R.drawable.le_tab_widget_left_selector);
        mTabRightBgId = ta.getResourceId(R.styleable.leTopTabWidget_leTabWidgetRightSelector,
                R.drawable.le_tab_widget_right_selector);
        mTabRecBgId = ta.getResourceId(R.styleable.leTopTabWidget_leTabWidgetRecSelector,
                R.drawable.le_tab_widget_rec_selector);
        mTabTextColorId = ta.getResourceId(R.styleable.leTopTabWidget_leTabWidgetTextColor,
                R.drawable.le_tab_widget_text_color);
        mTabCounts = ta.getInt(R.styleable.leTopTabWidget_leTabCounts, mTabCounts);
        mTabTextSize = (int) ta.getDimension(R.styleable.leTopTabWidget_leTabWidgetTextSize,
                context.getResources().getDimension(R.dimen.le_tab_text_size));
        ta.recycle();
        initView();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (this.getWidth() != 0) {
            mTabWidth = this.getWidth();
        }
        if (this.getHeight() != 0) {
            mTabHeight = this.getHeight();
        }
        LayoutParams params = new LayoutParams(((int) mTabWidth - mDivideWidth*(mTabCounts-1))
                / mTabCounts, (int) mTabHeight);

        if (!isAdd) {
            removeAllViews();
            for (int i = 0; i < mTabCounts; i++) {
                if(i>0) {
                   params.setMarginStart(mDivideWidth);
                }
                addView(mTabWidgetBtns.get(i), params);
            }
            isAdd = true;
            this.setPadding(6, 6, 6, 6);
            this.setVerticalGravity(Gravity.CENTER_VERTICAL);
            this.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
        }
    }

    private void initView() {
        if (mTabCounts < 2) {
            throw new IllegalArgumentException("tabCounts must  more than two");
        }
        if (mTabWidgetBgId == 0) {
            mTabWidgetBgId = R.drawable.le_tab_widget_bg_selector;
        }
        this.setBackgroundResource(mTabWidgetBgId);
        for (int i = 0; i < mTabCounts; i++) {
            Button tabBtn = new Button(mContext);
            if (mTabTextColorId == 0) {
                mTabTextColorId = R.drawable.le_tab_widget_text_color;
            }
           ColorStateList btnColor=getResources().getColorStateList(mTabTextColorId);
           tabBtn.setTextColor(btnColor);
           tabBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabTextSize);
            mTabWidgetBtns.add(tabBtn);
            if (i == 0) {
                if (mTabLeftBgId == 0) {
                    mTabLeftBgId = R.drawable.le_tab_widget_left_selector;
                }
                tabBtn.setBackgroundResource(mTabLeftBgId);
            } else if (i == mTabCounts - 1) {
                if (mTabRightBgId == 0) {
                    mTabRightBgId = R.drawable.le_tab_widget_right_selector;
                }
                tabBtn.setBackgroundResource(mTabRightBgId);
            } else {
                if (mTabRecBgId == 0) {
                    mTabRecBgId = R.drawable.le_tab_widget_rec_selector;
                }
                tabBtn.setBackgroundResource(mTabRecBgId);
            }
        }
    }

    public View getTabView(int index) {
        if (index > mTabCounts - 1) {
            throw new IllegalArgumentException("index must  less than tabCount-1");
        }
        if (index < 0) {
            throw new IllegalArgumentException("index must more than 0 ");
        }
        if (mTabWidgetBtns != null && mTabWidgetBtns.size() > 0) {
            return mTabWidgetBtns.get(index);
        }
        throw new IllegalArgumentException("getTabView is null ");
    }

    public void setmDivideWidth(int width){
        mDivideWidth = width;
        requestLayout();
    }

    public void setTabText(int index, int textId) {
        String text = mContext.getString(textId);
        mTabWidgetBtns.get(index).setText(text);
    }

    public void setTabOnClickListener(int index, OnClickListener listener) {
        mTabWidgetBtns.get(index).setOnClickListener(listener);
    }

    public void setTabWidgetBg(int resId) {
        mTabWidgetBgId = resId;
        this.setBackgroundResource(mTabWidgetBgId);
    }

    public void setTabWidgetLeftBg(int resId) {
        mTabLeftBgId = resId;
        mTabWidgetBtns.get(0).setBackgroundResource(mTabLeftBgId);
    }

    public void setTabWidgetRightBg(int resId) {
        mTabRightBgId = resId;
        mTabWidgetBtns.get(mTabWidgetBtns.size() - 1).setBackgroundResource(mTabLeftBgId);
    }

    public void setTabWidgetTextColor(int resId) {
        mTabTextColorId = resId;
        ColorStateList btnColor=getResources().getColorStateList(mTabTextColorId);
        for(int i =0;i<mTabWidgetBtns.size();i++) {
            mTabWidgetBtns.get(i).setTextColor(btnColor);
        }

    }

    public void setTabWidgetRecBg(int resId) {
        mTabRecBgId = resId;
        for (int i = 1; i < mTabWidgetBtns.size() - 2; i++) {
            mTabWidgetBtns.get(i).setBackgroundResource(mTabRecBgId);
        }
    }

    public void setTabTextSize(int textSzie) {
        for (int i = 0; i < mTabWidgetBtns.size(); i++) {
            mTabWidgetBtns.get(i).setTextSize(TypedValue.COMPLEX_UNIT_PX, textSzie);
        }
    }

    public void setCurrentTab(int index) {
        if(btnPressColor == null) {
            btnPressColor=getResources().getColorStateList(R.color.le_color_tab_widget_text_press);
        }
        if(btnColor == null) {
            btnColor=getResources().getColorStateList(R.drawable.le_tab_widget_text_color);
        }
        mCurrentIndex = index;
        for (int i = 0; i < mTabWidgetBtns.size(); i++) {
            if (i == 0) {
                if (i == index) {
                    mTabWidgetBtns.get(i).setBackground(null);
                    mTabWidgetBtns.get(i).setTextColor(btnPressColor);
                } else {
                    mTabWidgetBtns.get(i).setBackgroundResource(mTabLeftBgId);
                     mTabWidgetBtns.get(i).setTextColor(btnColor);
                }

            } else if (i == mTabCounts - 1) {
                if (i == index) {
                    mTabWidgetBtns.get(i).setBackground(null);
                    mTabWidgetBtns.get(i).setTextColor(btnPressColor);
                } else {
                    mTabWidgetBtns.get(i).setBackgroundResource(mTabRightBgId);
                    mTabWidgetBtns.get(i).setTextColor(btnColor);
                }
            } else {
                if (i == index) {
                    mTabWidgetBtns.get(i).setBackground(null);
                    mTabWidgetBtns.get(i).setTextColor(btnPressColor);
                } else {
                    mTabWidgetBtns.get(i).setBackgroundResource(mTabRecBgId);
                    mTabWidgetBtns.get(i).setTextColor(btnColor);
                }
            }
        }
    }

    public int getCurrentTab() {
        if(mCurrentIndex >=0) {
            return mCurrentIndex;
        }
        throw new IllegalArgumentException("return currentIndex is error");
    }

    public void setSelectTextColor(int resId) {
         btnPressColor=getResources().getColorStateList(resId);
        for(int i =0;i<mTabWidgetBtns.size();i++) {
            if(i == mCurrentIndex){
                mTabWidgetBtns.get(i).setTextColor(btnPressColor);
            }
        }
    }

    public void setNormalTextColor(int resId) {
        btnColor=getResources().getColorStateList(resId);
        for(int i =0;i<mTabWidgetBtns.size();i++) {
            if(i != mCurrentIndex){
                mTabWidgetBtns.get(i).setTextColor(btnColor);
            }
        }
    }

    public void  setTabListener(int index,OnClickListener listener){
           getTabView(index).setOnClickListener(listener);
    }

}
