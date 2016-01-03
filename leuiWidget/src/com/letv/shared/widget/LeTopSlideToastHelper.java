package com.letv.shared.widget;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.letv.shared.R;

/**
 * Created by liangchao on 15-2-2.
 */
public class LeTopSlideToastHelper {
    public static final int LENGTH_LONG = 10500;
    public static final int LENGTH_SHORT = 2000;
    public static final int TOAST_HEIGTH_DP_HIGH = 120;
    public static final int TOAST_HEIGTH_DP_LOW = 64;
    private static final float CONTENT_TEXT_WIDTH_RATIO = 0.55f;
    private static final float CONTENT_TEXT_WIDTH_RATIO_LARGE = 0.75f;
    private static int screenWidth;
    private final WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    private View toastView;
    private final Context mHostContext; // host应用的context
    private final Context mPluginContext; // 插件的context
    private Handler mHandler;
    private int duration = 0;
    private int animStyleId = android.R.style.Animation_Toast;
    private static float density;

    public void setCallback(LeTopSlideToastCallback callback) {
        this.callback = callback;
    }

    private LeTopSlideToastCallback callback;

    private final Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            LeTopSlideToastHelper.this.removeView();
        }
    };

    private LeTopSlideToastHelper(Context hostContext, Context pluginContext) {
        // Notice: we should get application context
        // otherwise we will get error
        // "Activity has leaked window that was originally added"
        Context ctx = hostContext.getApplicationContext();
        if (ctx == null) {
            ctx = hostContext;
        }
        this.mHostContext = ctx;
        mPluginContext = pluginContext;
        this.mWindowManager = (WindowManager) this.mHostContext
                .getSystemService(Context.WINDOW_SERVICE);
        density = pluginContext.getResources().getDisplayMetrics().density;
        screenWidth = pluginContext.getResources().getDisplayMetrics().widthPixels;
        this.init();
    }

    private void init() {

        this.mWindowParams = new WindowManager.LayoutParams();
        this.mWindowParams.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        this.mWindowParams.alpha = 1.0f;
        this.mWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        this.mWindowParams.height = dip2px(TOAST_HEIGTH_DP_HIGH);
        this.mWindowParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        this.mWindowParams.format = PixelFormat.TRANSLUCENT;
        this.mWindowParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        this.mWindowParams.setTitle("ToastHelper");
        this.mWindowParams.packageName = this.mHostContext.getPackageName();
        this.mWindowParams.windowAnimations = this.animStyleId;
    }

    public void show() {
        this.removeView();
        if (this.toastView == null) {
            return;
        }
        this.mWindowParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;

        this.toastView
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        this.mWindowManager.addView(this.toastView, this.mWindowParams);
        if (this.mHandler == null) {
            this.mHandler = new Handler();
        }
        this.mHandler.postDelayed(this.timerRunnable, this.duration);
        if (this.callback != null) {
            this.callback.onShow();
        }
    }

    public void removeView() {
        if (this.toastView != null && this.toastView.getParent() != null) {
            this.mWindowManager.removeView(this.toastView);
            this.mHandler.removeCallbacks(this.timerRunnable);
            if (this.callback != null) {
                this.callback.onDismiss();
            }
        }
    }

    public enum ContentTextAlign {
        LEFT,
        CENTER;
    }

    public static LeTopSlideToastHelper getToastHelper(Context context,
            int duration, String content, Drawable drawable, String btn_text,
            View.OnClickListener listener, LeTopSlideToastCallback callback,
            ContentTextAlign contentAlign) {
        return getToastHelper(context, context, duration, content, drawable,
                btn_text, listener, callback, contentAlign);
    }

    /**
     * @param hostContext
     *            宿主app的context, 用于获取system service
     * @param pluginContext
     *            插件的context, 用于获取插件内的资源
     * @param duration
     * @return
     */

    public static LeTopSlideToastHelper getToastHelper(Context hostContext,
            Context pluginContext, int duration, String content,
            Drawable drawable, String btn_text, View.OnClickListener listener,
            LeTopSlideToastCallback callback, ContentTextAlign contentAlign) {
        if (content == null) {
            return null;
        }
        LeTopSlideToastHelper helper = new LeTopSlideToastHelper(hostContext,
                pluginContext);
        View toast = LayoutInflater.from(pluginContext).inflate(
                R.layout.le_topslide_toast, null);
        ImageView toast_img = (ImageView) toast
                .findViewById(R.id.le_topslide_toast_img);
        TextView toast_text = (TextView) toast
                .findViewById(R.id.le_topslide_toast_text);
        TextView toast_btn = (TextView) toast
                .findViewById(R.id.le_topslide_toast_btn);
        ImageView toast_divider = (ImageView) toast
                .findViewById(R.id.le_topslide_toast_divider);
        if (drawable == null) {
            toast_img.setVisibility(View.GONE);
        } else {
            toast_img.setImageDrawable(drawable);
        }

        if (drawable == null && btn_text == null) {
            toast_text
                    .setMaxWidth((int) (screenWidth * CONTENT_TEXT_WIDTH_RATIO_LARGE));
        } else {
            toast_text
                    .setMaxWidth((int) (screenWidth * CONTENT_TEXT_WIDTH_RATIO));
        }
        toast_text.setText(content);
        if (contentAlign == ContentTextAlign.CENTER) {
            toast_text.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        if (btn_text == null) {
            toast_divider.setVisibility(View.GONE);
            toast_btn.setVisibility(View.GONE);
        } else {
            toast_btn.setText(btn_text);
            if (listener != null) {
                toast_btn.setOnClickListener(listener);
            }
        }
        helper.setCallback(callback);
        helper.setView(toast);
        helper.setDuration(duration);
        // helper.setAnimation(R.style.leTopSlideToast);
        helper.setAnimation(android.R.style.Animation_Toast);
        return helper;

    }

    public static LeTopSlideToastHelper getToastHelper(Context context,
            int duration, String content, Drawable drawable, String btn_text,
            View.OnClickListener listener, LeTopSlideToastCallback callback) {
        return getToastHelper(context, context, duration, content, drawable,
                btn_text, listener, callback, ContentTextAlign.LEFT);
    }

    public static LeTopSlideToastHelper getToastHelper(Context hostContext,
            Context pluginContext, int duration, String content,
            Drawable drawable, String btn_text, View.OnClickListener listener,
            LeTopSlideToastCallback callback) {
        return getToastHelper(hostContext, pluginContext, duration, content,
                drawable, btn_text, listener, callback, ContentTextAlign.LEFT);
    }

    public static LeTopSlideToastHelper getToastHelper(Context context,
            int duration, View view, LeTopSlideToastCallback callback) {
        return getToastHelper(context, context, duration, view, callback);
    }

    public static LeTopSlideToastHelper getToastHelper(Context hostContext,
            Context pluginContext, int duration, View view,
            LeTopSlideToastCallback callback) {
        if (view == null || hostContext == null) {
            return null;
        }
        LeTopSlideToastHelper helper = new LeTopSlideToastHelper(hostContext,
                pluginContext);
        helper.setCallback(callback);
        helper.setView(view);
        helper.setDuration(duration);
        // helper.setAnimation(R.style.leTopSlideToast);
        helper.setAnimation(android.R.style.Animation_Toast);
        return helper;
    }

    public LeTopSlideToastHelper setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public LeTopSlideToastHelper setAnimation(int animStyleId) {
        this.animStyleId = animStyleId;
        this.mWindowParams.windowAnimations = this.animStyleId;
        return this;
    }

    /**
     * set toast by dip
     * @param heightDip
     * @return LeTopSlideToastHelper
     */
    public LeTopSlideToastHelper setToastHeight(int heightDip) {
        this.mWindowParams.height = dip2px(heightDip);
        return this;
    }

    /**
     * custom view
     * @param view
     */
    public LeTopSlideToastHelper setView(View view) {
        this.toastView = view;
        return this;
    }

    private static int dip2px(float dp) {

        return (int) (dp * density + 0.5f);
    }
}
