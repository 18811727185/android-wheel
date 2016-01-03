package com.letv.mobile.core.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.letv.mobile.core.utils.ContextProvider;
import com.letv.mobile.core.utils.StringUtils;
import com.letv.shared.widget.LeTopSlideToastCallback;
import com.letv.shared.widget.LeTopSlideToastHelper;

public class LetvToast implements LeTopSlideToastCallback {

    public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;

    public static final int LENGTH_LONG = Toast.LENGTH_LONG;

    private static LetvToast sLastInstance;

    private LeTopSlideToastHelper mHelper;
    private int mDuration;
    private final Context mContext;
    private CharSequence mContent;
    private String mBtnText;
    private Drawable mImage;

    public LetvToast(Context context) {
        this.mContext = context;
    }

    public static LetvToast makeText(Context context, CharSequence text,
            int duration) {
        if (sLastInstance != null) {
            sLastInstance.cancel();
        }
        LetvToast toast = new LetvToast(context);
        toast.mContent = text;
        toast.mDuration = duration;
        sLastInstance = toast;
        return toast;
    }

    public static LetvToast makeText(Context context, int resId, int duration)
            throws Resources.NotFoundException {
        return makeText(context, context.getResources().getText(resId),
                duration);
    }

    public static LetvToast makeText(Context context, CharSequence text, int duration, String btnText, Drawable image) {
        if (sLastInstance != null) {
            sLastInstance.cancel();
        }
        LetvToast toast = new LetvToast(context);
        toast.mContent = text;
        toast.mDuration = duration;
        toast.mBtnText = btnText;
        toast.mImage = image;
        sLastInstance = toast;
        return toast;
    }

    public void show() {
        // 如果内容为空，阻止显示
        if (StringUtils.equalsNull(this.mContent.toString())) {
            return;
        }

        if (this.mHelper == null) {
            int duration = LeTopSlideToastHelper.LENGTH_SHORT;
            if (this.mDuration == LENGTH_SHORT) {
                duration = LeTopSlideToastHelper.LENGTH_SHORT;
            } else if (this.mDuration == LENGTH_LONG) {
                duration = LeTopSlideToastHelper.LENGTH_LONG;
            }
            this.mHelper = LeTopSlideToastHelper.getToastHelper(this.mContext,
                    duration, this.mContent.toString(), this.mImage, this.mBtnText, null, this);
        }
        this.mHelper.show();
    }

    public void cancel() {
        if (this.mHelper != null) {
            this.mHelper.removeView();
        }
        if (sLastInstance == this) {
            sLastInstance = null;
        }
    }

    public void setDuration(int duration) {
        this.mDuration = duration;
    }

    public void setText(CharSequence s) {
        this.mContent = s;
    }

    @Override
    public void onShow() {
    }

    @Override
    public void onDismiss() {
        if (sLastInstance == this) {
            sLastInstance = null;
        }
    }

    /**
     * 显示短toast
     * @param text
     *            toast内容
     */
    public static void showShortToast(String text) {
        makeText(ContextProvider.getApplicationContext(), text, LENGTH_SHORT)
                .show();
    }

    /**
     * 显示短toast
     * @param resId
     *            toast内容资源id
     */
    public static void showShortToast(int resId) {
        showShortToast(ContextProvider.getApplicationContext().getString(resId));
    }

    /**
     * 显示长toast
     * @param text
     *            toast内容
     */
    public static void showLongToast(String text) {
        makeText(ContextProvider.getApplicationContext(), text, LENGTH_LONG)
                .show();
    }

    /**
     * 显示长toast
     * @param resId
     *            toast内容资源id
     */
    public static void showLongToast(int resId) {
        showLongToast(ContextProvider.getApplicationContext().getString(resId));
    }

}
