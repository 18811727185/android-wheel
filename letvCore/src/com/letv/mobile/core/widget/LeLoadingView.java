package com.letv.mobile.core.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;

/**
 * Created by dupengtao on 15-2-4.
 */
public class LeLoadingView extends View implements
        ValueAnimator.AnimatorUpdateListener {

    private static final int ROTATE_DURATION = 900;
    private static final int DURATION = 300;
    private static final int DURATION2 = 100;
    private float mBallRadius, mViewSize, mViewRadius;
    private int ballNum = 6;
    private final ArrayList<BallsLoadingShapeHolder> mBalls = new ArrayList<BallsLoadingShapeHolder>(
            6);
    private ObjectAnimator mRotateAnim;
    private ArrayList<Integer> colorList = new ArrayList<Integer>(6);
    private AnimatorSet disappearAnim, appearAnim;

    private LeLoadingAnimListener animListener;
    private boolean isAnimRunning, isAppearAnimRunning, isDisAppearAnimRunning;
    private boolean isCancelAnim;

    public LeLoadingView(Context context) {
        super(context);
    }

    public LeLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ArrayList<Integer> getDefaultColorList() {
        ArrayList<Integer> colorList = new ArrayList<Integer>(6);
        colorList.add(Color.parseColor("#ed1e20"));
        colorList.add(Color.parseColor("#6c24c6"));
        colorList.add(Color.parseColor("#1ab1eb"));
        colorList.add(Color.parseColor("#8ad127"));
        colorList.add(Color.parseColor("#ffd800"));
        colorList.add(Color.parseColor("#ff8a00"));
        return colorList;
    }

    private void initBall() {
        float angleUnit = 360f / this.ballNum;
        float drawRadius = this.mViewRadius - this.mBallRadius;
        for (int i = 0; i < this.ballNum; i++) {
            PointF pointF = new PointF();
            pointF.set(
                    (float) (this.mViewSize / 2 + drawRadius
                            * Math.sin(i * angleUnit * Math.PI / 180)),
                    (float) (this.mViewSize / 2 - drawRadius
                            * Math.cos(i * angleUnit * Math.PI / 180)));
            this.mBalls.add(this.addBall(pointF.x, pointF.y,
                    this.colorList.get(i)));
        }
    }

    private BallsLoadingShapeHolder addBall(float x, float y) {
        OvalShape circle = new OvalShape();
        circle.resize(this.mBallRadius, this.mBallRadius);
        ShapeDrawable drawable = new ShapeDrawable(circle);
        BallsLoadingShapeHolder shapeHolder = new BallsLoadingShapeHolder(
                drawable);
        shapeHolder.setX(x);
        shapeHolder.setY(y);
        Paint paint = drawable.getPaint();
        paint.setColor(Color.RED);
        shapeHolder.setPaint(paint);
        shapeHolder.setAlpha(0f);
        return shapeHolder;
    }

    private BallsLoadingShapeHolder addBall(float x, float y, int color) {
        OvalShape circle = new OvalShape();
        circle.resize(this.mBallRadius, this.mBallRadius);
        ShapeDrawable drawable = new ShapeDrawable(circle);
        BallsLoadingShapeHolder shapeHolder = new BallsLoadingShapeHolder(
                drawable);
        shapeHolder.setX(x);
        shapeHolder.setY(y);
        Paint paint = drawable.getPaint();
        paint.setColor(color);
        shapeHolder.setPaint(paint);
        shapeHolder.setAlpha(0f);
        return shapeHolder;
    }

    /**
     * has a bug please use {@link widget.LeLoadingView#appearAnim(Runnable)}
     */
    @Deprecated
    public boolean appearAnim() {
        // NOTE:by letv leading app(防止大小为0)
        int width = this.getWidth();
        int height = this.getHeight();
        ViewGroup.LayoutParams lp = this.getLayoutParams();
        if (width == 0 && lp != null) {
            width = lp.width;
        }
        if (width == 0 && lp != null) {
            width = lp.height;
        }
        return this.appearAnim(width, height);
    }

    public boolean appearAnim(int height, int width) {

        if (this.isAnimRunning || this.isDisAppearAnimRunning) {
            return false;
        }
        this.isAnimRunning = true;
        this.isCancelAnim = false;
        int h = height;
        int w = width;
        int size = h >= w ? h : w;
        this.mViewSize = size;
        this.mBallRadius = size / (192 / 24);
        this.mViewRadius = size / (192 / 96);
        if (this.colorList.size() == 0) {
            this.colorList.addAll(this.getDefaultColorList());
        }
        if (this.appearAnim == null) {
            this.mBalls.clear();
            this.initBall();
            ObjectAnimator[] mAnimators = new ObjectAnimator[this.mBalls.size()];
            for (int i = 0, j = this.mBalls.size(); i < j; i++) {
                // 0-normal
                mAnimators[i] = this.getZero2Normal(this.mBalls.get(i), i);
                mAnimators[i].setTarget(this.mBalls.get(i));
                mAnimators[i].addListener(new EmptyAnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        ObjectAnimator objectAnimator = (ObjectAnimator) animation;
                        BallsLoadingShapeHolder holder = (BallsLoadingShapeHolder) objectAnimator
                                .getTarget();
                        if (holder != null) {
                            holder.setAlpha(1f);
                        }
                    }
                });
            }
            this.mRotateAnim = this.getRotateAnim();
            this.appearAnim = new AnimatorSet();
            this.appearAnim.addListener(new EmptyAnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    LeLoadingView.this.mRotateAnim.start();
                    LeLoadingView.this.isAppearAnimRunning = true;
                    if (LeLoadingView.this.animListener != null) {
                        LeLoadingView.this.animListener.onLoadStart();
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    LeLoadingView.this.isAppearAnimRunning = false;
                    if (LeLoadingView.this.isCancelAnim) {
                        LeLoadingView.this.disappearAnim(null);
                    }
                }
            });
            this.appearAnim.playTogether(mAnimators);
        }
        this.appearAnim.start();
        return true;
    }

    public boolean appearAnim(final Runnable disappearedCallBack) {
        if (this.isAnimRunning || this.isDisAppearAnimRunning) {
            return false;
        }
        this.isAnimRunning = true;
        this.isCancelAnim = false;
        int h = this.getHeight();
        int w = this.getWidth();
        // NOTE:by letv leading
        // app（在getWidth和getHeight为0的情况下用layout的宽度和高度，防止view没有获取到宽度和高度时候显示不出来）
        ViewGroup.LayoutParams lp = this.getLayoutParams();
        if (w == 0 && lp != null) {
            w = lp.width;
        }
        if (h == 0 && lp != null) {
            h = lp.height;
        }
        int size = h >= w ? h : w;
        this.mViewSize = size;
        this.mBallRadius = size / (192 / 24);
        this.mViewRadius = size / (192 / 96);
        if (this.colorList.size() == 0) {
            this.colorList.addAll(this.getDefaultColorList());
        }
        if (this.appearAnim == null) {
            this.mBalls.clear();
            this.initBall();
            ObjectAnimator[] mAnimators = new ObjectAnimator[this.mBalls.size()];
            for (int i = 0, j = this.mBalls.size(); i < j; i++) {
                // 0-normal
                mAnimators[i] = this.getZero2Normal(this.mBalls.get(i), i);
                mAnimators[i].setTarget(this.mBalls.get(i));
                mAnimators[i].addListener(new EmptyAnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        ObjectAnimator objectAnimator = (ObjectAnimator) animation;
                        BallsLoadingShapeHolder holder = (BallsLoadingShapeHolder) objectAnimator
                                .getTarget();
                        if (holder != null) {
                            holder.setAlpha(1f);
                        }
                    }
                });
            }
            this.mRotateAnim = this.getRotateAnim();
            this.appearAnim = new AnimatorSet();
            this.appearAnim.addListener(new EmptyAnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    LeLoadingView.this.mRotateAnim.start();
                    LeLoadingView.this.isAppearAnimRunning = true;
                    if (LeLoadingView.this.animListener != null) {
                        LeLoadingView.this.animListener.onLoadStart();
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    LeLoadingView.this.isAppearAnimRunning = false;
                    if (LeLoadingView.this.isCancelAnim) {
                        LeLoadingView.this.disappearAnim(disappearedCallBack);
                    }
                }
            });
            this.appearAnim.playTogether(mAnimators);
        }
        this.appearAnim.start();
        return true;
    }

    public void disappearAnim(final Runnable disappearedCallBack) {
        this.isCancelAnim = true;
        if (!this.isAnimRunning || this.isAppearAnimRunning) {
            return;
        }
        if (this.disappearAnim == null) {
            final ObjectAnimator[] mAnimators = new ObjectAnimator[this.mBalls
                    .size()];
            for (int i = 0, j = this.mBalls.size(); i < j; i++) {
                // 0-normal
                mAnimators[i] = this.getNormal2Zero(this.mBalls.get(i), i);
                mAnimators[i].setTarget(this.mBalls.get(i));
                mAnimators[i].addListener(new EmptyAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ObjectAnimator objectAnimator = (ObjectAnimator) animation;
                        BallsLoadingShapeHolder holder = (BallsLoadingShapeHolder) objectAnimator
                                .getTarget();
                        if (holder != null) {
                            holder.setAlpha(0f);
                        }
                    }
                });
            }
            this.disappearAnim = new AnimatorSet();
            this.disappearAnim.addListener(new EmptyAnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    LeLoadingView.this.isDisAppearAnimRunning = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    LeLoadingView.this.mRotateAnim.cancel();
                    LeLoadingView.this.isAnimRunning = false;
                    LeLoadingView.this.isDisAppearAnimRunning = false;
                    if (LeLoadingView.this.animListener != null) {
                        LeLoadingView.this.animListener.onLoadFinished();
                    }
                    if (disappearedCallBack != null) {
                        disappearedCallBack.run();
                    }
                }

            });
            this.disappearAnim.playTogether(mAnimators);
        }
        this.disappearAnim.start();
    }

    // NOTE:by letv leading app
    public void disappearImmediately(final Runnable disappearedCallBack) {
        this.isCancelAnim = true;
        if (!this.isAnimRunning) {
            return;
        }

        if (this.appearAnim != null) {
            this.appearAnim.cancel();
        }
        if (this.mRotateAnim != null) {
            this.mRotateAnim.cancel();
        }
        this.isAnimRunning = false;
        this.isDisAppearAnimRunning = false;
        if (this.animListener != null) {
            this.animListener.onLoadFinished();
        }
        if (disappearedCallBack != null) {
            disappearedCallBack.run();
        }
    }

    /**
     * 0 - normal
     */
    private ObjectAnimator getZero2Normal(BallsLoadingShapeHolder ball,
            int orderId) {
        PropertyValuesHolder pvhW = PropertyValuesHolder.ofFloat("width", 0,
                ball.getWidth());
        PropertyValuesHolder pvhH = PropertyValuesHolder.ofFloat("height", 0,
                ball.getHeight());
        PropertyValuesHolder pvTX = PropertyValuesHolder.ofFloat("x",
                ball.getX() + this.mBallRadius / 2, ball.getX());
        PropertyValuesHolder pvTY = PropertyValuesHolder.ofFloat("y",
                ball.getY() + this.mBallRadius / 2, ball.getY());
        ObjectAnimator z2nAnim = ObjectAnimator.ofPropertyValuesHolder(ball,
                pvhW, pvhH, pvTX, pvTY).setDuration(DURATION / 2);
        z2nAnim.setStartDelay(75 * orderId);
        z2nAnim.setInterpolator(new AccelerateInterpolator());
        z2nAnim.addUpdateListener(this);
        return z2nAnim;
    }

    /**
     * normal - 0
     */
    private ObjectAnimator getNormal2Zero(BallsLoadingShapeHolder ball,
            int orderId) {
        PropertyValuesHolder pvhW = PropertyValuesHolder.ofFloat("width",
                ball.getWidth(), 0);
        PropertyValuesHolder pvhH = PropertyValuesHolder.ofFloat("height",
                ball.getHeight(), 0);
        PropertyValuesHolder pvTX = PropertyValuesHolder.ofFloat("x",
                ball.getX(), ball.getX() + this.mBallRadius / 2);
        PropertyValuesHolder pvTY = PropertyValuesHolder.ofFloat("y",
                ball.getY(), ball.getY() + this.mBallRadius / 2);
        ObjectAnimator z2nAnim = ObjectAnimator.ofPropertyValuesHolder(ball,
                pvhW, pvhH, pvTX, pvTY).setDuration(DURATION2 / 2);
        z2nAnim.setStartDelay(75 * orderId);
        z2nAnim.setInterpolator(new AccelerateInterpolator());
        z2nAnim.addUpdateListener(this);
        return z2nAnim;
    }

    private ObjectAnimator getRotateAnim() {
        PropertyValuesHolder rotation = PropertyValuesHolder.ofFloat(
                "rotation", 0, 360);
        ObjectAnimator rotateAnim = ObjectAnimator.ofPropertyValuesHolder(this,
                rotation).setDuration(ROTATE_DURATION);
        rotateAnim.setRepeatCount(-1); // -1:Infinite loop
        rotateAnim.setInterpolator(new LinearInterpolator());
        rotateAnim.addUpdateListener(this);
        return rotateAnim;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // if (drawBalls) {
        for (BallsLoadingShapeHolder ball : this.mBalls) {
            canvas.translate(ball.getX() - this.mBallRadius / 2, ball.getY()
                    - this.mBallRadius / 2);
            ball.getShape().draw(canvas);
            canvas.translate(-ball.getX() + this.mBallRadius / 2, -ball.getY()
                    + this.mBallRadius / 2);
        }
        // }
    }

    public int getBallNum() {
        return this.ballNum;
    }

    public void setBallNum(int ballNum, ArrayList<Integer> colorList) {

        if (colorList == null || colorList.size() < ballNum) {
            throw new IllegalArgumentException("colorList size < balls count");
        }
        this.colorList = colorList;
        this.ballNum = ballNum;
    }

    public ArrayList<Integer> getColorList() {
        return this.colorList;
    }

    public void setColorList(ArrayList<Integer> colorList) {
        this.colorList = colorList;
    }

    public LeLoadingAnimListener getAnimListener() {
        return this.animListener;
    }

    public void setAnimListener(LeLoadingAnimListener animListener) {
        this.animListener = animListener;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        this.invalidate();
    }

    public boolean isCancelAnim() {
        return this.isCancelAnim;
    }

    public void setCancelAnim(boolean isCancelAnim) {
        this.isCancelAnim = isCancelAnim;
    }

    class EmptyAnimatorListener implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }

    }

    /**
     * The AnimationProcessBar listener
     */
    public interface LeLoadingAnimListener {
        /**
         * when the animation start the method will be called
         */
        void onLoadStart();

        /**
         * when the animation finished the method will be called
         */
        void onLoadFinished();
    }

    // NOTE:by letv leading app(回复onDetached的时候取消的动画)
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.isAnimRunning) {
            if (this.mRotateAnim != null && !this.mRotateAnim.isStarted()) {
                this.mRotateAnim.start();
            }
        }
    }

    // NOTE:by letv leading app(防止内存溢出)
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mRotateAnim != null && this.mRotateAnim.isStarted()) {
            this.mRotateAnim.cancel();
        }
    }
}
