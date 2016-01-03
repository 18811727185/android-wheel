package com.letv.mobile.core.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;

public class UIUtils {

    /**
     * 将一倍尺寸缩放到当前屏幕大小的尺寸（宽）
     */
    public static int zoomWidth(int w, Context context) {
        int sw;
        sw = Math.min(getScreenWidth(context), getScreenHeight(context));

        return Math.round(w * sw / 320f + 0.5f);
    }

    /**
     * 将一倍尺寸缩放到当前屏幕大小的尺寸（高）
     */
    public static int zoomHeight(int h, Context context) {
        int sh;
        sh = getScreenHeight(context);

        return (int) (h * sh / 480f + 0.5f);
    }

    /**
     * 缩放控件
     */
    public static void zoomViewWidth(int w, View view, Context context) {
        if (view == null) {
            return;
        }

        LayoutParams params = view.getLayoutParams();

        if (params == null) {
            return;
        }
        int width = zoomWidth(w, context);
        params.width = width;
    }

    /**
     * 缩放控件
     */
    public static void zoomView(int w, int h, View view, Context context) {
        if (view == null) {
            return;
        }

        LayoutParams params = view.getLayoutParams();

        if (params == null) {
            return;
        }

        params.width = zoomWidth(w, context);
        params.height = zoomWidth(h, context);
    }

    /**
     * 缩放控件
     */
    public static void zoomViewFull(View view, Context context) {
        if (view == null) {
            return;
        }

        LayoutParams params = view.getLayoutParams();

        if (params == null) {
            return;
        }

        params.width = getScreenWidth(context);
        params.height = getScreenHeight(context);
    }

    /**
     * 获取顶部状态栏高度
     * @param act
     * @return
     */
    public static int getStatusBarHeight(Activity act) {
        Rect frame = new Rect();
        act.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        return frame.top;
    }

    /**
     * 得到屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        return ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getWidth();
    }

    /**
     * 得到屏幕高度
     */
    public static int getScreenHeight(Context context) {
        return ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getHeight();
    }

    /**
     * 测量该控件的 尺寸
     * @param v
     * @return int[]{width,height}
     */
    public static int[] measure(View v) {
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        v.measure(w, h);
        int width = v.getMeasuredWidth();
        int height = v.getMeasuredHeight();
        return new int[] { width, height };
    }

    /**
     * 收藏渐变规则
     */
    private static final float[] collectionAniScaleRec = new float[] { 1.0f, 0.6f,
            1.5f, 0.8f, 1.0f };

    /*
     * private static float[] unConllectionAniScalRec = new float[] { 1.0f,
     * 0.6f,
     * 1.5f, 0.6f, 1.0f };
     */
    /**
     * 渐变率(必须要能被scaleSize数组的相邻两个数值之差整除)
     */
    private static final float scaleRate = 0.1f;
    /**
     * 动画总执行时间ms(由于实际几个Animation之间执行回调需要时间，所以实际总动画时间会比这个时间长)
     */
    private static final int animTotallTime = 220;

    /**
     * 收藏动画
     * @param view
     */
    public static void animCollection(Context context, final View view) {
        if (view != null) {
            if (null != view.getAnimation() && !view.getAnimation().hasEnded()) {
                return;
            }
            view.post(new Runnable() {
                @Override
                public void run() {
                    animCollection(view, collectionAniScaleRec, 0);
                }
            });
        }
    }

    /**
     * 收藏/取消收藏 动画实际逻辑
     */
    private static void animCollection(final View view,
            final float[] aniScaleRec, final int index) {
        animFrames(view, index, aniScaleRec, new Runnable() {

            @Override
            public void run() {
                if (index + 1 < aniScaleRec.length - 1) {
                    animCollection(view, aniScaleRec, index + 1);
                }
            }
        });

    }

    /**
     * 收藏view的每次变换动画逻辑
     * @param view
     * @param index
     * @param callback
     *            动画执行完成回调
     */
    private static void animFrames(final View view, final int index,
            float[] aniScaleRec, final Runnable callback) {
        /**
         * 总帧数
         */
        float totalFrame = 0;
        for (int i = 0; i < aniScaleRec.length - 1; i++) {
            totalFrame += Math.abs(aniScaleRec[i + 1] - aniScaleRec[i]);
        }
        /**
         * 执行每一帧切换的时间
         */
        final int animFrameTime = animTotallTime
                / ((int) (totalFrame / scaleRate));
        float animEnd = aniScaleRec[index + 1];
        float animStart = aniScaleRec[index];
        final int scaleCount = Math
                .abs(((int) (animEnd * 100) - (int) (animStart * 100))
                        / ((int) (scaleRate * 100)));
        int duration = scaleCount * animFrameTime;
        ScaleAnimation sa = new ScaleAnimation(animStart, animEnd, animStart,
                animEnd, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        sa.setFillAfter(true);
        sa.setDuration(duration);
        sa.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                callback.run();
            }
        });
        view.startAnimation(sa);
    }
}
