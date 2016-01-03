
package com.letv.shared.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.LinearLayout;
import com.letv.shared.R;
import java.util.ArrayList;

import android.view.Gravity;
import android.view.View;
public class LeTopWidgetArc extends LinearLayout {

    private Context mContext;
    private int mTabCounts = 2;
    private int mTabWidgetBgId;
    private int mTabLeftBgId;
    private int mTabRightBgId;
    private int mTabRecBgId;
    private int mTabTextColorId;
    private int mTabWidth = 504;
    private int mTabHeight = 84;
    private static final int mDefaultWidth = 504;
    private static final int mDefaultHight = 84;
    private int mTabTextSize = 42;
    private boolean isAdd = false;
    private int mCurrentIndex = -1;
    private ArrayList<Button> mTabWidgetBtns = new ArrayList<Button>();
    private int btnPressColor;
    private int btnColor;
    private int mDivideWidth = 3;
    private Drawable mDividerDrawable = null;
    private int mTabPressBgColor = -1;
    private int mTabNormalBgColor = -1;

    public LeTopWidgetArc(Context context) {
        this(context, mDefaultWidth, mDefaultHight);

    }

    public LeTopWidgetArc(Context context, int TabWidgetwidth, int TabWidgetheight) {
        this(context, null);
        mTabWidth = TabWidgetwidth;
        mTabHeight = TabWidgetheight;

    }

    public LeTopWidgetArc(Context context, AttributeSet attrs) {
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

    public void addTab(int id, String title) {
        Button tabBtn = new Button(mContext);
        tabBtn.setId(id);
        tabBtn.setText(title);
        tabBtn.setSingleLine();
        tabBtn.setEllipsize(TextUtils.TruncateAt.END);
        tabBtn.setBackground(null);
        tabBtn.setPadding(0, 0, 0, 0);
        mTabWidgetBtns.add(tabBtn);
        requestLayout();
    }

    public void setTabBgColor(int pressColor, int normalColor) {
        mTabPressBgColor = pressColor;
        mTabNormalBgColor = normalColor;
        btnPressColor = normalColor;
        btnColor = pressColor;
        initView();
        invalidate();
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mTabWidgetBtns == null) {
            return;
        }
        int left = this.getWidth() / mTabWidgetBtns.size();
        int top = 0 + mDivideWidth / 2;
        int right = left + mDivideWidth;
        int bottom = this.getHeight() - mDivideWidth / 2;
        final Drawable dividerStrip = mDividerDrawable;
        for (int i = 0; i < mTabWidgetBtns.size() - 1; i++) {
            if (dividerStrip != null) {
                dividerStrip.setBounds(left, top, right, bottom);
                dividerStrip.draw(canvas);
            }
            left = left + this.getWidth() / mTabWidgetBtns.size();
            right = left + mDivideWidth;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mTabWidgetBtns == null) {
            return;
        }
        if (this.getWidth() != 0) {
            mTabWidth = this.getWidth();
        }
        if (this.getHeight() != 0) {
            mTabHeight = this.getHeight();
        }
        LayoutParams params = new LayoutParams((int) mTabWidth / mTabWidgetBtns.size(), mTabHeight);
        if (!isAdd) {
            removeAllViews();
            for (int i = 0; i < mTabWidgetBtns.size(); i++) {
                addView(mTabWidgetBtns.get(i), params);
            }
            isAdd = true;
            this.setVerticalGravity(Gravity.CENTER_VERTICAL);
            this.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
        }
    }

    private void initView() {
        if (mTabWidgetBgId == 0) {
            mTabWidgetBgId = R.drawable.le_tab_widget_bg_selector;
        }
        Drawable drawableBg = getResources().getDrawable(R.drawable.le_tab_widget_bg_selector);

        this.setBackground(drawableBg);
        GradientDrawable bg = ((GradientDrawable) this.getBackground());
        if (mTabNormalBgColor != -1 && bg !=null) {
            bg.setColor(getResources().getColor(mTabNormalBgColor));

        }
        if (mTabPressBgColor != -1&& bg !=null) {
            bg.setStroke(mDivideWidth, getResources().getColor(mTabPressBgColor));
        }

        if (mTabLeftBgId == 0) {
            mTabLeftBgId = R.drawable.le_tab_widget_left_selector;
        }
        if (mTabRightBgId == 0) {
            mTabRightBgId = R.drawable.le_tab_widget_right_selector;
        }
        if (mTabRecBgId == 0) {
            mTabRecBgId = R.drawable.le_tab_widget_rec_selector;
        }
        if (mTabPressBgColor != -1) {
            mDividerDrawable = getResources().getDrawable(mTabPressBgColor);
        } else {
            mDividerDrawable = getResources().getDrawable(R.color.le_color_tab_widget_text_press);
        }
        if(mTabWidgetBtns !=null && mTabWidgetBtns.size()>0){
            for(int i = 0;i<mTabWidgetBtns.size();i++){
                GradientDrawable drawable = (GradientDrawable)mTabWidgetBtns.get(i).getBackground();
                if(mTabNormalBgColor != -1 && drawable != null){
                    drawable.setColor(getResources().getColor(mTabNormalBgColor));
                }
            }
            setCurrentTab(mCurrentIndex);
        }
    }

    public View getTabView(int index) {
        if (index > mTabWidgetBtns.size() - 1) {
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

    public void setmDivideWidth(int width) {
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
        for (int i = 0; i < mTabWidgetBtns.size(); i++) {
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
        Drawable drawableLeftBg = getResources().getDrawable(R.drawable.le_tab_widget_left_selector);
        Drawable drawableRightBg = getResources().getDrawable(R.drawable.le_tab_widget_right_selector);
        Drawable drawableCenterBg = getResources().getDrawable(R.drawable.le_tab_widget_rec_selector);
        btnPressColor = getResources().getColor(R.color.le_color_tab_widget_text_press);
        btnColor = getResources().getColor(R.color.le_color_tab_widget_text_normal);
        mCurrentIndex = index;
        if(mTabWidgetBtns == null || mTabWidgetBtns.size()<1){
            throw new IllegalArgumentException("you haven't add tab");
        }
        for (int i = 0; i < mTabWidgetBtns.size(); i++) {
            if (i == 0) {
                if (i == index) {
                    mTabWidgetBtns.get(i).setBackground(drawableLeftBg);
                    GradientDrawable bg = ((GradientDrawable) mTabWidgetBtns.get(i).getBackground());
                    if (mTabPressBgColor != -1) {
                        bg.setColor(getResources().getColor(mTabPressBgColor));
                    }
                    if (mTabNormalBgColor != -1) {
                        mTabWidgetBtns.get(i).setTextColor(getResources().getColor(mTabNormalBgColor));
                    } else {
                        mTabWidgetBtns.get(i).setTextColor(btnColor);
                    }
                } else {
                    mTabWidgetBtns.get(i).setBackground(null);
                    if (mTabPressBgColor != -1) {
                        mTabWidgetBtns.get(i).setTextColor(getResources().getColor(mTabPressBgColor));
                    } else {
                        mTabWidgetBtns.get(i).setTextColor(btnPressColor);
                    }

                }

            } else if (i == mTabWidgetBtns.size() - 1) {
                if (i == index) {
                    mTabWidgetBtns.get(i).setBackground(drawableRightBg);
                    GradientDrawable bg = ((GradientDrawable) mTabWidgetBtns.get(i).getBackground());
                    if (mTabPressBgColor != -1) {

                        bg.setColor(getResources().getColor(mTabPressBgColor));

                    }
                    if (mTabNormalBgColor != -1) {
                        mTabWidgetBtns.get(i).setTextColor(mTabNormalBgColor);
                    } else {
                        mTabWidgetBtns.get(i).setTextColor(btnColor);
                    }
                } else {
                    mTabWidgetBtns.get(i).setBackground(null);
                    if (mTabPressBgColor != -1) {
                        mTabWidgetBtns.get(i).setTextColor(getResources().getColor(mTabPressBgColor));
                    } else {
                        mTabWidgetBtns.get(i).setTextColor(btnPressColor);
                    }
                }
            } else {
                if (i == index) {
                    mTabWidgetBtns.get(i).setBackground(drawableCenterBg);
                    GradientDrawable bg = ((GradientDrawable) mTabWidgetBtns.get(i).getBackground());
                    if (mTabPressBgColor != -1) {

                        bg.setColor(getResources().getColor(mTabPressBgColor));

                    }
                    if (mTabNormalBgColor != -1) {
                        mTabWidgetBtns.get(i).setTextColor(mTabNormalBgColor);
                    } else {
                        mTabWidgetBtns.get(i).setTextColor(btnColor);
                    }
                } else {
                    mTabWidgetBtns.get(i).setBackground(null);
                    if (mTabPressBgColor != -1) {
                        mTabWidgetBtns.get(i).setTextColor(getResources().getColor(mTabPressBgColor));
                    } else {
                        mTabWidgetBtns.get(i).setTextColor(btnPressColor);
                    }
                }
            }
        }
    }

    public int getCurrentTab() {
        if (mCurrentIndex >= 0) {
            return mCurrentIndex;
        }
        throw new IllegalArgumentException("return currentIndex is error");
    }

    public void setSelectTextColor(int resId) {
        for (int i = 0; i < mTabWidgetBtns.size(); i++) {
            if (i == mCurrentIndex) {
                mTabWidgetBtns.get(i).setTextColor(resId);
            }
        }
    }

    public void setNormalTextColor(int resId) {
        for (int i = 0; i < mTabWidgetBtns.size(); i++) {
            if (i != mCurrentIndex) {
                mTabWidgetBtns.get(i).setTextColor(resId);
            }
        }
    }

    public void setTabListener(int index, OnClickListener listener) {
        getTabView(index).setOnClickListener(listener);
    }

}
