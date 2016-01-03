package com.letv.mobile.core.scaleview;

import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.letv.mobile.core.utils.ContextProvider;
import com.letv.mobile.core.utils.ReflectionUtils;
import com.letv.mobile.core.utils.SystemUtil;

/**
 * Created by lizhennian on 2014/5/29.
 */
public class ScaleCalculator {
    // 基础像素密度density为1.5， 宽1080， 高1920
    private static final float BASE_WIDTH = 1080.0f;
    private static final float BASE_HEIGHT = 1920.0f;

    private static float mCurrentWidth = 1080.0f;
    private static float mCurrentHeight = 1920.0f;

    private static boolean mIsBaseWidth = true;
    private static boolean mIsBaseHeight = true;

    private static float mWidthScale = 1.0f;
    private static float mHeightScale = 1.0f;

    private static ScaleCalculator mInstance;

    private ScaleCalculator(Context context) {
        this.getScreenSize(context);
    }

    private static ScaleCalculator init(Context context) {
        if (mInstance == null) {
            mInstance = new ScaleCalculator(context);
        }
        return mInstance;
    }

    public static ScaleCalculator getInstance() {
        if (mInstance == null) {
            return init(ContextProvider.getApplicationContext());
        }
        return mInstance;
    }

    /**
     * Get the current device screen size
     * @param context
     */
    private void getScreenSize(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(displayMetrics);
        if (displayMetrics.widthPixels <= displayMetrics.heightPixels) {
            mCurrentWidth = displayMetrics.widthPixels;
            mCurrentHeight = this.correctHeight(displayMetrics.heightPixels);
        } else {
            mCurrentWidth = displayMetrics.heightPixels;
            mCurrentHeight = this.correctHeight(displayMetrics.widthPixels);
        }

        mIsBaseWidth = mCurrentWidth == BASE_WIDTH;
        mIsBaseHeight = mCurrentHeight == BASE_HEIGHT;

        mWidthScale = mCurrentWidth / BASE_WIDTH;
        mHeightScale = mCurrentHeight / BASE_HEIGHT;
    }

    /**
     * Scale text size
     * @param pixel
     * @return
     */
    public final int scaleTextSize(float pixel) {
        return this.scaleTextSize(pixel, ScaleConstants.DEFAULT_SCALE_STYLE);
    }

    public final int scaleTextSize(float pixel, ScaleStyle scaleStyle) {
        if (pixel < 0.0F) {
            return 0;
        }
        switch (scaleStyle) {
        case BASED_ON_WIDTH:
            if (this.isBaseWidth()) {
                return (int) pixel;
            }
            return this.scaleWidth((int) pixel);
        case BASED_ON_WIDTH_AND_HEIGHT:
            if (this.isBaseSize()) {
                return (int) pixel;
            }
            if (mWidthScale >= mHeightScale) {
                return this.scaleHeight((int) pixel);
            }
            return this.scaleWidth((int) pixel);
        case BASED_ON_HEIGHT:
            if (this.isBaseHeight()) {
                return (int) pixel;
            }
            return this.scaleHeight((int) pixel);
        default:
            return (int) pixel;
        }
    }

    public final void scaleViewGroup(ViewGroup viewGroup) {
        this.scaleViewGroup(viewGroup, this.getScaleStyleOfView(viewGroup,
                ScaleConstants.DEFAULT_SCALE_STYLE));
    }

    public final void scaleViewGroup(ViewGroup viewGroup,
            ScaleStyle defaultScaleStyle) {
        if (viewGroup == null || this.isBaseSize()) {
            return;
        }
        int i = viewGroup.getChildCount();
        for (int j = 0; j < i; j++) {
            this.scaleView(viewGroup.getChildAt(j), defaultScaleStyle);
        }
    }

    public final void scaleView(View view) {
        this.scaleView(view, ScaleConstants.DEFAULT_SCALE_STYLE);
    }

    public final void scaleView(View view, ScaleStyle defaultScaleStyle) {
        if (view == null || this.isBaseSize()) {
            return;
        }

        ScaleStyle scaleStyle = this.getScaleStyleOfView(view,
                defaultScaleStyle);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams != null) {
            int height = layoutParams.height;
            int width = layoutParams.width;
            switch (scaleStyle) {
            case BASED_ON_WIDTH:
                if (height > 0 && !this.isBaseWidth()) {
                    height = this.scaleWidth(height);
                    layoutParams.height = height;
                }
                if (width > 0 && !this.isBaseWidth()) {
                    width = this.scaleWidth(width);
                    layoutParams.width = width;
                }
                break;
            case BASED_ON_WIDTH_AND_HEIGHT:
                if (height > 0 && !this.isBaseHeight()) {
                    height = this.scaleHeight(height);
                    layoutParams.height = height;
                }
                if (width > 0 && !this.isBaseWidth()) {
                    width = this.scaleWidth(width);
                    layoutParams.width = width;
                }
                break;
            case BASED_ON_HEIGHT:
                if (height > 0 && !this.isBaseHeight()) {
                    height = this.scaleHeight(height);
                    layoutParams.height = height;
                }
                if (width > 0 && !this.isBaseHeight()) {
                    width = this.scaleHeight(width);
                    layoutParams.width = width;
                }
                break;
            }
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                this.scaleMargin((ViewGroup.MarginLayoutParams) layoutParams,
                        scaleStyle);
            }
        }
        this.scalePadding(view, scaleStyle);
    }

    private ScaleStyle getScaleStyleOfView(View view,
            ScaleStyle defaultScaleStyle) {
        if (view instanceof ScaleStyleInterface) {
            return ((ScaleStyleInterface) view).getScaleStyle() == null ? defaultScaleStyle
                    : ((ScaleStyleInterface) view).getScaleStyle();
        }
        return defaultScaleStyle;
    }

    private void scaleMargin(
            ViewGroup.MarginLayoutParams marginLayoutParams,
            ScaleStyle scaleStyle) {

        if (marginLayoutParams == null || this.isBaseSize()) {
            return;
        }
        switch (scaleStyle) {
        case BASED_ON_WIDTH:
            if (!this.isBaseWidth()) {
                if (marginLayoutParams.leftMargin != 0) {
                    marginLayoutParams.leftMargin = this
                            .scaleWidth(marginLayoutParams.leftMargin);
                }
                if (marginLayoutParams.rightMargin != 0) {
                    marginLayoutParams.rightMargin = this
                            .scaleWidth(marginLayoutParams.rightMargin);
                }

                if (marginLayoutParams.topMargin != 0) {
                    marginLayoutParams.topMargin = this
                            .scaleWidth(marginLayoutParams.topMargin);
                }
                if (marginLayoutParams.bottomMargin != 0) {
                    marginLayoutParams.bottomMargin = this
                            .scaleWidth(marginLayoutParams.bottomMargin);
                }
            }
            break;
        case BASED_ON_WIDTH_AND_HEIGHT:
            if (!this.isBaseWidth()) {
                if (marginLayoutParams.leftMargin != 0) {
                    marginLayoutParams.leftMargin = this
                            .scaleWidth(marginLayoutParams.leftMargin);
                }
                if (marginLayoutParams.rightMargin != 0) {
                    marginLayoutParams.rightMargin = this
                            .scaleWidth(marginLayoutParams.rightMargin);
                }
            }

            if (!this.isBaseHeight()) {
                if (marginLayoutParams.topMargin != 0) {
                    marginLayoutParams.topMargin = this
                            .scaleHeight(marginLayoutParams.topMargin);
                }
                if (marginLayoutParams.bottomMargin != 0) {
                    marginLayoutParams.bottomMargin = this
                            .scaleHeight(marginLayoutParams.bottomMargin);
                }
            }
            break;
        case BASED_ON_HEIGHT:
            if (!this.isBaseHeight()) {
                if (marginLayoutParams.leftMargin != 0) {
                    marginLayoutParams.leftMargin = this
                            .scaleHeight(marginLayoutParams.leftMargin);
                }
                if (marginLayoutParams.rightMargin != 0) {
                    marginLayoutParams.rightMargin = this
                            .scaleHeight(marginLayoutParams.rightMargin);
                }
                if (marginLayoutParams.topMargin != 0) {
                    marginLayoutParams.topMargin = this
                            .scaleHeight(marginLayoutParams.topMargin);
                }
                if (marginLayoutParams.bottomMargin != 0) {
                    marginLayoutParams.bottomMargin = this
                            .scaleHeight(marginLayoutParams.bottomMargin);
                }
            }
            break;
        }

    }

    private void scalePadding(View view, ScaleStyle scaleStyle) {
        if (view == null || this.isBaseSize()) {
            return;
        }
        int paddingLeft = view.getPaddingLeft();
        int paddingTop = view.getPaddingTop();
        int paddingRight = view.getPaddingRight();
        int paddingBottom = view.getPaddingBottom();

        switch (scaleStyle) {
        case BASED_ON_WIDTH:
            if (!this.isBaseWidth()) {
                if (paddingLeft > 0) {
                    paddingLeft = this.scaleWidth(paddingLeft);
                }
                if (paddingRight > 0) {
                    paddingRight = this.scaleWidth(paddingRight);
                }
                int minimumWidth = this.getMinimumWidth(view);
                if (minimumWidth > 0) {
                    minimumWidth = this.scaleWidth(minimumWidth);
                    view.setMinimumWidth(minimumWidth);
                }
                if (view instanceof TextView) {
                    int miniWidth = this.getMinWidth((TextView) view);
                    if (miniWidth > 0) {
                        miniWidth = this.scaleWidth(miniWidth);
                        ((TextView) view).setMinWidth(miniWidth);
                    }
                }
                if (paddingBottom > 0) {
                    paddingBottom = this.scaleWidth(paddingBottom);
                }
                if (paddingTop > 0) {
                    paddingTop = this.scaleWidth(paddingTop);
                }
                int minimumHeight = this.getMinimumHeight(view);
                if (minimumHeight > 0) {
                    minimumHeight = this.scaleWidth(minimumHeight);
                    view.setMinimumHeight(minimumHeight);
                }
                if (view instanceof TextView) {
                    int miniHeight = this.getMinHeight((TextView) view);
                    if (miniHeight > 0) {
                        miniHeight = this.scaleWidth(miniHeight);
                        ((TextView) view).setMinHeight(miniHeight);
                    }
                }
            }
            break;
        case BASED_ON_WIDTH_AND_HEIGHT:
            if (!this.isBaseWidth()) {
                if (paddingLeft > 0) {
                    paddingLeft = this.scaleWidth(paddingLeft);
                }
                if (paddingRight > 0) {
                    paddingRight = this.scaleWidth(paddingRight);
                }
                int minimumWidth = this.getMinimumWidth(view);
                if (minimumWidth > 0) {
                    minimumWidth = this.scaleWidth(minimumWidth);
                    view.setMinimumWidth(minimumWidth);
                }
                if (view instanceof TextView) {
                    int miniWidth = this.getMinWidth((TextView) view);
                    if (miniWidth > 0) {
                        miniWidth = this.scaleWidth(miniWidth);
                        ((TextView) view).setMinWidth(miniWidth);
                    }
                }
            }
            if (!this.isBaseHeight()) {
                if (paddingBottom > 0) {
                    paddingBottom = this.scaleHeight(paddingBottom);
                }
                if (paddingTop > 0) {
                    paddingTop = this.scaleHeight(paddingTop);
                }
                int minimumHeight = this.getMinimumHeight(view);
                if (minimumHeight > 0) {
                    minimumHeight = this.scaleHeight(minimumHeight);
                    view.setMinimumHeight(minimumHeight);
                }
                if (view instanceof TextView) {
                    int miniHeight = this.getMinHeight((TextView) view);
                    if (miniHeight > 0) {
                        miniHeight = this.scaleHeight(miniHeight);
                        ((TextView) view).setMinHeight(miniHeight);
                    }
                }

            }
            break;
        case BASED_ON_HEIGHT:
            if (!this.isBaseHeight()) {
                if (paddingLeft > 0) {
                    paddingLeft = this.scaleHeight(paddingLeft);
                }
                if (paddingRight > 0) {
                    paddingRight = this.scaleHeight(paddingRight);
                }
                int minimumWidth = this.getMinimumWidth(view);
                if (minimumWidth > 0) {
                    minimumWidth = this.scaleHeight(minimumWidth);
                    view.setMinimumWidth(minimumWidth);
                }
                if (view instanceof TextView) {
                    int miniWidth = this.getMinWidth((TextView) view);
                    if (miniWidth > 0) {
                        miniWidth = this.scaleHeight(miniWidth);
                        ((TextView) view).setMinWidth(miniWidth);
                    }
                }
                if (paddingBottom > 0) {
                    paddingBottom = this.scaleHeight(paddingBottom);
                }
                if (paddingTop > 0) {
                    paddingTop = this.scaleHeight(paddingTop);
                }
                int minimumHeight = this.getMinimumHeight(view);
                if (minimumHeight > 0) {
                    minimumHeight = this.scaleHeight(minimumHeight);
                    view.setMinimumHeight(minimumHeight);
                }
                if (view instanceof TextView) {
                    int miniHeight = this.getMinHeight((TextView) view);
                    if (miniHeight > 0) {
                        miniHeight = this.scaleHeight(miniHeight);
                        ((TextView) view).setMinHeight(miniHeight);
                    }
                }

            }
            break;
        }

        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    @SuppressLint("NewApi")
    private Integer getMinimumWidth(View view) {
        if (this.isIceCreamSandwichMR1OrOld()) {// api <= 15
            try {
                Field field = View.class.getDeclaredField("mMinWidth");
                Object ob = ReflectionUtils.getFieldValueSafely(field, view);
                return (Integer) ob;
            } catch (Exception e) {
                return view.getWidth();
            }
        } else {
            return view.getMinimumWidth();
        }
    }

    @SuppressLint("NewApi")
    private Integer getMinimumHeight(View view) {
        if (this.isIceCreamSandwichMR1OrOld()) {// api <= 15
            try {
                Field field = View.class.getDeclaredField("mMinHeight");
                Object ob = ReflectionUtils.getFieldValueSafely(field, view);
                return (Integer) ob;
            } catch (Exception e) {
                return view.getHeight();
            }
        } else {
            return view.getMinimumHeight();
        }
    }

    @SuppressLint("NewApi")
    private Integer getMinWidth(TextView view) {
        if (this.isIceCreamSandwichMR1OrOld()) {// api <= 15
            try {
                Field field = TextView.class.getDeclaredField("mMinWidth");
                Object ob = ReflectionUtils.getFieldValueSafely(field, view);
                return (Integer) ob;
            } catch (Exception e) {
                return view.getWidth();
            }
        } else {
            return view.getMinWidth();
        }
    }

    @SuppressLint("NewApi")
    private Integer getMinHeight(TextView view) {
        if (this.isIceCreamSandwichMR1OrOld()) {// api <= 15
            try {
                Field field = TextView.class.getDeclaredField("mMinimum");
                Object ob = ReflectionUtils.getFieldValueSafely(field, view);
                return (Integer) ob;
            } catch (Exception e) {
                return view.getHeight();
            }
        } else {
            return view.getMinHeight();
        }
    }

    /**
     * 判断api等级
     * @return true：api等级 <= 15
     *         false：api等级 > 15
     */
    private boolean isIceCreamSandwichMR1OrOld() {
        int version = SystemUtil.getAndroidSDKVersion();
        // api <= 15
        return version <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
    }

    private int correctHeight(int hight) {
        if ((hight >= 672) && (hight <= 720)) {
            hight = 720;
        }
        return hight;
    }

    public final int scaleWidth(int pixel) {
        return Math.round(pixel * mWidthScale);
    }

    public final int scaleHeight(int pixel) {
        return Math.round(pixel * mHeightScale);
    }

    private boolean isBaseSize() {
        return (this.isBaseHeight()) && (this.isBaseWidth());
    }

    private boolean isBaseHeight() {
        return mIsBaseHeight;
    }

    private boolean isBaseWidth() {
        return mIsBaseWidth;
    }

}
