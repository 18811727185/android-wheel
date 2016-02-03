package com.letv.mobile.core.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by dupengtao on 15-2-5.
 */
public class LeLoadingDialog extends Dialog {

    /**
     * 默认主题
     */
    public static final int DEFAULT_THEME = 1;

    /**
     * 默认Loading视图大小
     */
    public static final int DEFAULT_VIEW_SIZE_DP = 40;

    private final int mCurTheme;
    private final Context mContext;
    private LeLoadingView mLeLoadingView;
    private TextView mTitle, mContent;

    // NOTE:by letv leading app----start
    private final Handler mHandler = new Handler();
    private final Runnable mDismissAction = new Runnable() {

        @Override
        public void run() {
            LeLoadingDialog.this.doDismiss();
        }
    };

    // NOTE:by letv leading app----end

    public LeLoadingDialog(Context context) {
        this(context, DEFAULT_THEME, DEFAULT_VIEW_SIZE_DP);
    }

    public LeLoadingDialog(Context context, int contentViewSizeDp) {
        this(context, 0, contentViewSizeDp);
    }

    public LeLoadingDialog(Context context, int theme, int contentViewSizeDp) {
        this(context, theme, contentViewSizeDp, null, null);
    }

    public LeLoadingDialog(Context context, int theme, int contentViewSizeDp,
                           String title, String content) {
        this(context, theme, contentViewSizeDp, title, content, null);
    }

    public LeLoadingDialog(Context context, int theme, int contentViewSizeDp,
                           String title, String content, Runnable dismissCallBack) {
        // NOTE:by letv leading app
        super(context);
        this.mCurTheme = theme;
        this.mContext = context;
        this.initDialog(dismissCallBack);
        this.initView(contentViewSizeDp, title, content);
    }

    private void initDialog(final Runnable dismissCallBack) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // NOTE:by letv leading app
        ColorDrawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
        this.getWindow().setBackgroundDrawable(colorDrawable);
        this.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                LeLoadingDialog.this.mLeLoadingView
                        .appearAnim(new Runnable() {
                            @Override
                            public void run() {
                                // NOTE:by letv leading app
                                LeLoadingDialog.this.doSuperDismiss();
                                if (dismissCallBack != null) {
                                    dismissCallBack.run();
                                }
                            }
                        });
            }
        });
        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (dismissCallBack != null) {
                    dismissCallBack.run();
                }
            }
        });
        // setCancelable(false);
        this.setCanceledOnTouchOutside(false);
    }

    private void initView(int contentViewSizeDp, String title, String content) {
        RelativeLayout rl = new RelativeLayout(this.mContext);
        rl.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        rl.setGravity(Gravity.CENTER);
        LinearLayout innerLl = new LinearLayout(this.mContext);
        innerLl.setGravity(Gravity.CENTER);
        innerLl.setOrientation(LinearLayout.VERTICAL);
        RelativeLayout.LayoutParams innerRlLp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        innerLl.setLayoutParams(innerRlLp);
        rl.addView(innerLl);

        this.mLeLoadingView = new LeLoadingView(this.mContext);
        int v = (int) this.dipToPixels(this.mContext, contentViewSizeDp);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                v, v);
        layoutParams.gravity = Gravity.CENTER;
        this.mLeLoadingView.setLayoutParams(layoutParams);
        innerLl.addView(this.mLeLoadingView);

        LinearLayout l1 = this.addLinearLayout1(title);
        innerLl.addView(l1);
        if (TextUtils.isEmpty(title)) {
            l1.setVisibility(View.GONE);
        }
        LinearLayout l2 = this.addLinearLayout2(content);
        innerLl.addView(l2);
        if (TextUtils.isEmpty(content)) {
            l2.setVisibility(View.GONE);
        }

        this.setContentView(rl);

        if (this.mCurTheme == 0) {
            ArrayList<Integer> colorList = new ArrayList<Integer>();
            colorList.add(Color.WHITE);
            colorList.add(Color.WHITE);
            colorList.add(Color.WHITE);
            colorList.add(Color.WHITE);
            colorList.add(Color.WHITE);
            colorList.add(Color.WHITE);
            this.mLeLoadingView.setColorList(colorList);
        }
    }

    private LinearLayout addLinearLayout1(String title) {
        LinearLayout l1 = new LinearLayout(this.mContext);

        l1.setGravity(Gravity.CENTER);
        l1.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams l1Lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        l1Lp.setMargins(0, (int) this.dipToPixels(this.mContext, 9f), 0, 0);
        l1.setLayoutParams(l1Lp);

        TextView v1 = new TextView(this.mContext);
        LinearLayout.LayoutParams l1Item1Lp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        l1Item1Lp.weight = 1;
        v1.setLayoutParams(l1Item1Lp);
        l1.addView(v1);
        //
        this.mTitle = new TextView(this.mContext);
        LinearLayout.LayoutParams l1Item2Lp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        l1Item2Lp.weight = 8;
        this.mTitle.setGravity(Gravity.CENTER);
        this.mTitle.setLayoutParams(l1Item2Lp);
        this.mTitle.setSingleLine(true);
        this.mTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        if (this.mCurTheme == 0) {
            this.mTitle.setTextColor(Color.WHITE);
        } else {
            this.mTitle.setTextColor(Color.parseColor("#575757"));
        }
        this.mTitle.setText(title);
        l1.addView(this.mTitle);
        //
        TextView v3 = new TextView(this.mContext);
        LinearLayout.LayoutParams l1Item3Lp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        l1Item3Lp.weight = 1;
        v3.setLayoutParams(l1Item3Lp);
        l1.addView(v3);
        return l1;
    }

    private LinearLayout addLinearLayout2(String content) {
        LinearLayout l1 = new LinearLayout(this.mContext);

        l1.setGravity(Gravity.CENTER);
        l1.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams l1Lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        l1Lp.setMargins(0, (int) this.dipToPixels(this.mContext, 7f), 0, 0);
        l1.setLayoutParams(l1Lp);

        TextView v1 = new TextView(this.mContext);
        LinearLayout.LayoutParams l1Item1Lp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        l1Item1Lp.weight = 1;
        v1.setLayoutParams(l1Item1Lp);
        l1.addView(v1);
        //
        this.mContent = new TextView(this.mContext);
        LinearLayout.LayoutParams l1Item2Lp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        l1Item2Lp.weight = 8;
        this.mContent.setGravity(Gravity.CENTER);
        this.mContent.setLayoutParams(l1Item2Lp);
        this.mContent.setSingleLine(true);
        this.mContent.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        if (this.mCurTheme == 0) {
            this.mContent.setTextColor(Color.parseColor("#99ffffff"));
        } else {
            this.mContent.setTextColor(Color.parseColor("#717171"));
        }
        this.mContent.setText(content);
        l1.addView(this.mContent);
        //
        TextView v3 = new TextView(this.mContext);
        LinearLayout.LayoutParams l1Item3Lp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        l1Item3Lp.weight = 1;
        v3.setLayoutParams(l1Item3Lp);
        l1.addView(v3);
        return l1;
    }

    public float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue,
                metrics);
    }

    // NOTE:by letv leading app
    @Override
    public void dismiss() {
        if (Looper.myLooper() == this.mHandler.getLooper()) {
            this.doDismiss();
        } else {
            this.mHandler.post(this.mDismissAction);
        }
    }

    // NOTE:by letv leading app
    private void doDismiss() {
        // 不显示动画直接隐藏
        this.mLeLoadingView.disappearImmediately(new Runnable() {
            @Override
            public void run() {
                LeLoadingDialog.this.doSuperDismiss();
            }
        });
    }

    // NOTE:by letv leading app
    private void doSuperDismiss() {
        Window window = this.getWindow();
        if (window != null && window.getDecorView() != null
                && window.getDecorView().getParent() != null) {
            try {
                super.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void dismissNoAnim() {
       dismiss();
    }

    /**
     * eg.
     * @ Override
     * protected void onDestroy() {
     * onDismissDialog4DestroyContext();
     * super.onDestroy();
     * }
     */
    public void onDismissDialog4DestroyContext() {
        if (this.isShowing()) {
            this.dismissNoAnim();
        }
    }

    public LeLoadingView getLeLoadingView() {
        return this.mLeLoadingView;
    }

    /**
     * get title TextView
     */
    public TextView getTitle() {
        return this.mTitle;
    }

    /**
     * get content TextView
     */
    public TextView getContent() {
        return this.mContent;
    }

    public void setTitleStr(String title) {
        ViewGroup group = (ViewGroup) this.mTitle.getParent();
        if (group.getVisibility() == View.GONE) {
            group.setVisibility(View.VISIBLE);
        }
        this.mTitle.setText(title);
    }

    public void setContentStr(String content) {
        ViewGroup group = (ViewGroup) this.mContent.getParent();
        if (group.getVisibility() == View.GONE) {
            group.setVisibility(View.VISIBLE);
        }
        this.mContent.setText(content);
    }

}
