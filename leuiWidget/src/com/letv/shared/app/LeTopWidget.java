
package com.letv.shared.app;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.letv.shared.R;

public class LeTopWidget extends RelativeLayout {
    private View mActionBar;
    private ImageView mBackIV;
    private TextView mBackTV;
    private TextView mCenterTV;
    private TextView mRightTV;
    private ImageView mRightIV;
    private LinearLayout mLeftLayout;
    private LinearLayout mRightLayout;
    public static final int LEFT_ONLY_TITLE = 0x0;
    public static final int LEFT_ONLY_LOGO = 0x1;
    public static final int LEFT_TITLE_LOGO = 0x2;
    public static final int LEFT_TITLE_LOGO_GONE = 0x4;
    public static final int RIGHT_ONLY_LOGO = 0x16;
    public static final int RIGHT_ONLY_TITLE = 0x8;
    public static final int RIGHT_TITLE_LOGO_GONE = 0x32;
    private static final int LAYOUT_MARGIN_VALUE = 14;
    private static final int TOP_WIDGET_VIEW_HEIGHT = 48;
    private Context mContext;
    private CharSequence mLeftText;
    private CharSequence mCenterText;
    private CharSequence mRightText;
    private int mBackIconId;
    private int mRightIconId;
    private int mLeTopWidgetBgId;
    private int mLeftTvColor;
    private int mCenterTvColor;
    private int mRightTvColor;
    private int mLeftTvSize;
    private int mRighTvSize;
    private int mCenterTvSize;
    private static final int LEFT_DEFAULT_MODE = 0x1;
    private static final int RIGHT_DEFAULT_MODE = 0x32;
    private int mLeftMode;
    private int mRightMode;
    private static final int LAND_CENTER_TV_WIDTH = 350;
    private static final int LAND_CENTER_TV_WIDTH_OTNER = 300;
    private static final int PORT_CENTER_TV_WIDTH = 200;
    private static final int LAND_LEFT_TV_WIDTH_OTNER = 100;
    private int mTextSize;
    private int mTextColor;

    public LeTopWidget(Context context) {
        this(context, null);
    }

    public LeTopWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.le_topwidget_view, this);
        this.setMinimumHeight(dip2px(mContext, TOP_WIDGET_VIEW_HEIGHT));
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(-2, -2);
        this.setMinimumWidth(param.FILL_PARENT);
        mBackIV = (ImageView) this.findViewById(R.id.back_iv);
        mBackTV = (TextView) this.findViewById(R.id.back_tv);
        mCenterTV = (TextView) this.findViewById(R.id.center_tv);
        mRightTV = (TextView) this.findViewById(R.id.right_tv);
        mRightIV = (ImageView) this.findViewById(R.id.right_iv);
        mLeftLayout = (LinearLayout) this.findViewById(R.id.left_layout);
        mLeftLayout.setOnClickListener(clickListener);
        mRightLayout = (LinearLayout) this.findViewById(R.id.right_layout);
        reMeasureWidth();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.leTopWidget);
        mLeftText = a.getText(R.styleable.leTopWidget_leftTextViewText);
        mRightText = a.getText(R.styleable.leTopWidget_rightTextViewText);
        mCenterText = a.getText(R.styleable.leTopWidget_centerTextViewText);
        mBackIconId = a.getResourceId(R.styleable.leTopWidget_leftLogo,
                R.drawable.le_actionbar_back);
        mRightIconId = a.getResourceId(R.styleable.leTopWidget_rightLogo,
                R.drawable.le_actionbar_complete);
        mLeftMode = a.getInt(R.styleable.leTopWidget_leftMode, LEFT_DEFAULT_MODE);
        mRightMode = a.getInt(R.styleable.leTopWidget_rightMode, RIGHT_DEFAULT_MODE);
        mLeTopWidgetBgId = a.getResourceId(R.styleable.leTopWidget_leTopWidgetBg, android.R.color.white);
        mTextColor = context.getResources().getColor(R.color.le_color_default_topwidget);
        mLeftTvColor = a.getColor(R.styleable.leTopWidget_leftTextViewTextColor, mTextColor);
        mCenterTvColor = a.getColor(R.styleable.leTopWidget_centerTextViewTextColor, mTextColor);
        mRightTvColor = a.getColor(R.styleable.leTopWidget_rightTextViewTextColor, mTextColor);
        mTextSize = (int) context.getResources().getInteger(R.integer.leTopWidget_textview_size);
        mLeftTvSize = (int) a.getInteger(R.styleable.leTopWidget_leftTextViewTextSize, mTextSize);
        mRighTvSize = (int) a.getInteger(R.styleable.leTopWidget_rightTextViewTextSize, mTextSize);
        mCenterTvSize = (int) a.getInteger(R.styleable.leTopWidget_centerTextViewTextSize,
                mTextSize);
        initView();
        a.recycle();
    }

    private void initView() {
        setLeftTitle(mLeftText);
        setCenterTitle(mCenterText);
        setRightTitle(mRightText);
        setLeftLogo(mBackIconId);
        setRightLogo(mRightIconId);
        setLeftMode(mLeftMode);
        setRightMode(mRightMode);
        setTopWidgetBg(mLeTopWidgetBgId);
        setLeftTvSize(mLeftTvSize);
        setRightTvSize(mCenterTvSize);
        setCenterTvSize(mTextSize);
        setLeftTvColor(mLeftTvColor);
        setCenterTvColor(mCenterTvColor);
        setRightTvColor(mRightTvColor);
    }

    private void reMeasureWidth() {
        int screenWidth = 1080;
        if (mContext instanceof Activity) {
            Display dis = ((Activity) mContext).getWindowManager().getDefaultDisplay();
            screenWidth = dis.getWidth();
        }
        Configuration cf = mContext.getResources().getConfiguration();
        int ori = cf.orientation;
        if (ori == cf.ORIENTATION_LANDSCAPE) {
            if (mLeftMode == LEFT_TITLE_LOGO_GONE && mRightMode == RIGHT_TITLE_LOGO_GONE
                    && mCenterTV.getVisibility() == View.VISIBLE) {
                mCenterTV.setMaxWidth(screenWidth);
            } else if ((mLeftMode == LEFT_ONLY_LOGO || mLeftMode == LEFT_ONLY_TITLE || mLeftMode == LEFT_TITLE_LOGO)
                    && mRightMode == RIGHT_TITLE_LOGO_GONE
                    && mCenterTV.getVisibility() != View.VISIBLE) {
                mBackTV.setMaxWidth(screenWidth);
            } else if (mRightMode == RIGHT_TITLE_LOGO_GONE) {
                mCenterTV.setMaxWidth(dip2px(mContext, LAND_CENTER_TV_WIDTH));
            } else {
                mCenterTV.setMaxWidth(dip2px(mContext, LAND_CENTER_TV_WIDTH_OTNER));
                mRightTV.setMaxWidth(dip2px(mContext, LAND_LEFT_TV_WIDTH_OTNER));
                mBackTV.setMaxWidth(dip2px(mContext, LAND_LEFT_TV_WIDTH_OTNER));
            }
        } else if (ori == cf.ORIENTATION_PORTRAIT) {
            if (mLeftMode == LEFT_TITLE_LOGO_GONE && mRightMode == RIGHT_TITLE_LOGO_GONE
                    && mCenterTV.getVisibility() == View.VISIBLE) {
                mCenterTV.setMaxWidth(screenWidth);
            } else if ((mLeftMode == LEFT_ONLY_LOGO || mLeftMode == LEFT_ONLY_TITLE || mLeftMode == LEFT_TITLE_LOGO)
                    && mRightMode == RIGHT_TITLE_LOGO_GONE
                    && mCenterTV.getVisibility() != View.VISIBLE) {
                mBackTV.setMaxWidth(screenWidth);
            } else if (mRightMode == RIGHT_TITLE_LOGO_GONE) {
                mCenterTV.setMaxWidth(dip2px(mContext, PORT_CENTER_TV_WIDTH));
            }else{
                mCenterTV.setMaxWidth(dip2px(mContext, 160));
                mRightTV.setMaxWidth(dip2px(mContext, 100));
                mBackTV.setMaxWidth(dip2px(mContext, 80));
            }
        }
    }

    public void setLeftTitle(CharSequence textTitle) {
        setText(mBackTV, textTitle);
    }

    public void setLeftTitle(int resId){
        mBackTV.setText(resId);
    }
    public void setCenterTitle(CharSequence textTitle) {
        if (textTitle !=null) {
            mCenterTV.setVisibility(View.VISIBLE);
        }
        setText(mCenterTV, textTitle);
    }

    public void setCenterTitle(int resId) {
        mCenterTV.setText(resId);
        mCenterTV.setVisibility(View.VISIBLE);
    }

    public void setRightTitle(CharSequence textTitle) {
        setText(mRightTV, textTitle);
    }

    public void setRightTitle(int resId){
        mRightTV.setText(resId);
    }
    private void setText(TextView view, CharSequence textTitle) {
        if (textTitle !=null) {
            view.setText(textTitle);
        }
    }

    public void setLeftTvColor(int color) {
        setTextColor(mBackTV, color);
    }

    public void setCenterTvColor(int color) {
        setTextColor(mCenterTV, color);
    }

    public void setRightTvColor(int color) {
        setTextColor(mRightTV, color);
    }

    private void setTextColor(TextView view, int color) {
        view.setTextColor(color);
    }

    public void setLeftLogo(int resId) {
        setBg(mBackIV, resId);
    }

    public void setRightLogo(int resId) {
        setBg(mRightIV, resId);
    }

    private void setBg(View view, int resId) {
        view.setBackgroundResource(resId);
    }

    public void setTopWidgetBg(int ResourceId) {
        this.setBackgroundResource(ResourceId);
    }

    public void setLeftMode(int mode) {
        if (mode == LEFT_ONLY_TITLE) {
            mBackIV.setVisibility(View.GONE);
            mBackTV.setVisibility(View.VISIBLE);
            mBackTV.setPadding(0,0,0,0);
            if(mLeftLayout.getVisibility() ==View.GONE){
                mLeftLayout.setVisibility(View.VISIBLE);
            }
        } else if (mode == LEFT_ONLY_LOGO) {
            mBackIV.setVisibility(View.VISIBLE);
            mBackTV.setVisibility(View.GONE);
            if(mLeftLayout.getVisibility() ==View.GONE){
                mLeftLayout.setVisibility(View.VISIBLE);
            }
        } else if (mode == LEFT_TITLE_LOGO) {
            mBackIV.setVisibility(View.VISIBLE);
            mBackTV.setVisibility(View.VISIBLE);
            mBackTV.setPadding(dip2px(mContext,4),0,0,0);
            if(mLeftLayout.getVisibility() ==View.GONE){
                mLeftLayout.setVisibility(View.VISIBLE);
            }
        } else if (mode == LEFT_TITLE_LOGO_GONE) {
            mLeftLayout.setVisibility(View.GONE);

        }
        mLeftMode = mode;
    }

    public void setLeftOnClick(OnClickListener listener) {
        mLeftLayout.setOnClickListener(listener);
    }

    public void setRightOnClick(OnClickListener listener) {
        mRightLayout.setOnClickListener(listener);
    }
    public void setRightMode(int mode) {
        if (mode == RIGHT_ONLY_LOGO) {
            mRightIV.setVisibility(View.VISIBLE);
            mRightTV.setVisibility(View.GONE);
        } else if (mode == RIGHT_ONLY_TITLE) {
            mRightIV.setVisibility(View.GONE);
            mRightTV.setVisibility(View.VISIBLE);
        } else if (mode == RIGHT_TITLE_LOGO_GONE) {
            mRightIV.setVisibility(View.GONE);
            mRightTV.setVisibility(View.GONE);
        }
        mRightMode = mode;
    }

    public void setLeftTvSize(int size) {
        setTextSize(mBackTV, size);
    }

    public void setCenterTvSize(int size) {
        setTextSize(mCenterTV, size);
    }

    public void setRightTvSize(int size) {
        setTextSize(mRightTV, size);
    }

    private void setTextSize(TextView view, int size) {
        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);

    }

    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            Activity activity = (Activity) mContext;
            if(activity !=null) {
                activity.onBackPressed();
            }
        }
    };

    public void setRightView(View view) {
        LayoutParams layoutParams = new LayoutParams(-2,
                LayoutParams.MATCH_PARENT);
        view.setPadding(dip2px(mContext,14),0,dip2px(mContext,14),0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        this.addView(view, layoutParams);

    }

    public void setLeftView(View view) {
        LayoutParams layoutParams = new LayoutParams(-2,
                LayoutParams.MATCH_PARENT);
        view.setPadding(dip2px(mContext,14),0,dip2px(mContext,14),0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        this.addView(view, layoutParams);

    }

    public void setCenterView(View view) {
        LayoutParams layoutParams = new LayoutParams(-2,
                LayoutParams.MATCH_PARENT);
        view.setPadding(dip2px(mContext,14),0,dip2px(mContext,14),0);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        this.addView(view, layoutParams);
    }

    public void setCenterView(View view,LayoutParams layoutParams) {
        if(layoutParams == null) {
            layoutParams = new LayoutParams(-2,
                    LayoutParams.MATCH_PARENT);
            view.setPadding(dip2px(mContext,14),0,dip2px(mContext,14),0);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        }
        this.addView(view, layoutParams);
    }

    public View getLeftLogo() {
        return mBackIV;
    }

    public View getRightView() {
        return mRightIV;
    }

    public View getCenterTextView() {
        return mCenterTV;
    }

    public View getRightTextView() {
        return mRightTV;
    }

    public View getLeftLayout() {
        return mLeftLayout;
    }

    public View getLeftTextView() {
        return mBackTV;
    }

    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public void setLeftTvEnable(View view,boolean enabled) {
        if(view == null){
            setEnable(mBackTV, enabled);
        }else{
            setEnable(view, enabled);
        }

    }

    public void setRightTvEnable(View view,boolean enabled) {
        if(view == null) {
            setEnable(mRightTV, enabled);
        }else{
            setEnable(view, enabled);
        }

    }


    private void setEnable(View view, boolean enabled) {
        float alpha = 0.0f;
        if (enabled) {
            alpha = mContext.getResources().getInteger(R.integer.le_view_enabled_alpha) * 0.01f;
        } else {
            alpha = mContext.getResources().getInteger(R.integer.le_view_disabled_alpha) * 0.01f;
        }
        if(view !=null) {
            view.setAlpha(alpha);
            view.setEnabled(enabled);
        }
    }

    public boolean getLeftEnable(){
        return mBackTV.isEnabled();
    }

    public boolean getRightEnable(){
        return mRightTV.isEnabled();
    }
}

