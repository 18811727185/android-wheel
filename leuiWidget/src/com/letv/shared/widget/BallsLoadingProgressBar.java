package com.letv.shared.widget;

import android.animation.*;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import com.letv.shared.R;

public class BallsLoadingProgressBar extends View implements
        ValueAnimator.AnimatorUpdateListener {

    private ArrayList<BallsLoadingShapeHolder> mBalls = new ArrayList<BallsLoadingShapeHolder>(5);
    private ObjectAnimator[] mAnimators;
    private Animator bounceAnim;
    private int mBallColor = 0xFF50af65, mDuration = 300, mCount = 5;
    private float mBallNormalRadius = 36, mBallExpandRadius = 72, mBallDistance = 80, mSumWidth;
    private boolean mIsFinish, mIsAnimLoading, mIsFirstRun = true;
    private AnimProcessBarListener mProcessBarListener;
    private int mGravity = 0;

    /**
     * Constructor.  This version is only needed if you will be instantiating
     * the object manually (not from a layout XML file).
     *
     * @param context
     */
    public BallsLoadingProgressBar(Context context) {
        super(context);
    }

    /**
     * Construct object, initializing with any attributes we understand from a
     * layout file.
     *
     * @see android.view.View#View(android.content.Context, android.util.AttributeSet)
     */
    public BallsLoadingProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BallsLoadingProgressBar);
        mBallColor = a.getColor(R.styleable.BallsLoadingProgressBar_ballsLoadingColor, 0xFF50af65);
        mBallNormalRadius = a.getDimensionPixelOffset(R.styleable.BallsLoadingProgressBar_ballsLoadingRadius, 0);
        mBallExpandRadius = mBallNormalRadius * 2;
        mBallDistance = a.getDimensionPixelOffset(R.styleable.BallsLoadingProgressBar_ballsLoadingDistance, (int) (mBallNormalRadius * 2.5));
        mCount = a.getInteger(R.styleable.BallsLoadingProgressBar_ballsLoadingCount, 5);
        mDuration = a.getInteger(R.styleable.BallsLoadingProgressBar_ballsLoadingDuration, 300);
        mGravity = a.getInt(R.styleable.BallsLoadingProgressBar_ballsLoadingGravity, 0);
        a.recycle();
        initBall();
        
    }

    private void initBall() {
        for (int i = 0; i < mCount; i++) {
            mBalls.add(addBall(i * mBallDistance, 0));
        }
        mSumWidth = mBalls.get(mBalls.size() - 1).getX() + mBallExpandRadius;
    }

    private BallsLoadingShapeHolder addBall(float x, float y) {
        OvalShape circle = new OvalShape();
        circle.resize(mBallNormalRadius, mBallNormalRadius);
        ShapeDrawable drawable = new ShapeDrawable(circle);
        BallsLoadingShapeHolder shapeHolder = new BallsLoadingShapeHolder(drawable);
        shapeHolder.setX(x);
        shapeHolder.setY(y);
        Paint paint = drawable.getPaint();
        paint.setColor(mBallColor);
        shapeHolder.setPaint(paint);
        shapeHolder.setAlpha(0f);
        return shapeHolder;
    }

    private void createBeginAnimation() {
        if (bounceAnim == null) {
            mAnimators = new ObjectAnimator[mBalls.size()];
            ObjectAnimator[] shrinkAnims = new ObjectAnimator[mBalls.size()];

            for (int i = 0, j = mBalls.size(); i < j; i++) {
                //0-double
                mAnimators[i] = getZero2DoubleAnim(mBalls.get(i), i);
                //double-normal
                shrinkAnims[i] = getDouble2NormalAnim(mBalls.get(i), i);
            }
            for (int i = 0, j = mAnimators.length; i < j; i++) {
                final int finalI = i;
                mAnimators[i].addListener(new EmptyAnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        BallsLoadingShapeHolder holder = mBalls.get(finalI);
                        if (holder != null) {
                            holder.setAlpha(1f);
                        }
                    }
                });
            }
            bounceAnim = new AnimatorSet();
            bounceAnim.addListener(new EmptyAnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mIsAnimLoading = true;
                    if (mProcessBarListener != null) {
                        mProcessBarListener.onLoadStart();
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    createAnimStep2();
                    bounceAnim.start();
                }
            });
            for (int i = 0, j = mAnimators.length; i < j; i++) {
                ((AnimatorSet) bounceAnim).play(shrinkAnims[i]).after(mAnimators[i]);
            }
        }
    }

    private void createAnimStep2() {
        if (mIsFirstRun) {
            mIsFirstRun = false;
            mAnimators = new ObjectAnimator[mBalls.size()];
            for (int i = 0, j = mBalls.size(); i < j; i++) {
                //expand
                ObjectAnimator expandAnimation = setExpandAnim(mBalls.get(i), i);
                mAnimators[i] = expandAnimation;

            }
            bounceAnim = new AnimatorSet();
            bounceAnim.addListener(new EmptyAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mIsFinish) {
                        createEndAnim();
                        bounceAnim.start();
                    } else {
                        createAnimStep2();
                        bounceAnim.start();
                    }
                }
            });
            ((AnimatorSet) bounceAnim).playTogether(mAnimators);
        }
    }

    private void createEndAnim() {
        if (mIsFinish) {
            mAnimators = new ObjectAnimator[mBalls.size()];
            ObjectAnimator[] normal2DoubleAnims = new ObjectAnimator[mBalls.size()];
            for (int i = 0, j = mBalls.size(); i < j; i++) {
                //normal - double
                normal2DoubleAnims[i] = getNormal2DoubleAnim(mBalls.get(i), i);
                //double - 0
                mAnimators[i] = getDouble2ZeroAnim(mBalls.get(i), i);
            }
            for (int i = 0, j = mAnimators.length; i < j; i++) {
                final int finalI = i;
                mAnimators[i].addListener(new EmptyAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        BallsLoadingShapeHolder holder = mBalls.get(finalI);
                        if (holder != null) {
                            holder.setAlpha(0f);
                        }
                    }
                });
            }
            bounceAnim = new AnimatorSet();
            bounceAnim.addListener(new EmptyAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsFinish = false;
                    mIsAnimLoading = false;
                    mIsFirstRun = true;
                    for (BallsLoadingShapeHolder balls : mBalls) {
                        balls.setWidth(mBallNormalRadius);
                        balls.setHeight(mBallNormalRadius);
                        balls.setX(balls.getX() - mBallNormalRadius / 2);
                        balls.setY(balls.getY() - mBallNormalRadius / 2);
                    }
                    if (mProcessBarListener != null) {
                        mProcessBarListener.onLoadFinished();
                    }
                }
            });

            for (int i = 0, j = mAnimators.length; i < j; i++) {
                ((AnimatorSet) bounceAnim).play(normal2DoubleAnims[i]).before(mAnimators[i]);
            }
        }
    }


    /**
     * 0 - normal
     */
    private ObjectAnimator getZero2Normal(BallsLoadingShapeHolder ball, int orderId) {
        PropertyValuesHolder pvhW = PropertyValuesHolder.ofFloat("width",
                0, ball.getWidth());
        PropertyValuesHolder pvhH = PropertyValuesHolder.ofFloat("height",
                0, ball.getHeight());
        PropertyValuesHolder pvTX = PropertyValuesHolder.ofFloat("x",
                ball.getX() + mBallNormalRadius / 2, ball.getX());
        PropertyValuesHolder pvTY = PropertyValuesHolder.ofFloat("y",
                ball.getY() + mBallNormalRadius / 2, ball.getY());
        ObjectAnimator z2nAnim = ObjectAnimator.ofPropertyValuesHolder(
                ball, pvhW, pvhH, pvTX, pvTY).setDuration(mDuration / 2);
        z2nAnim.setStartDelay(mDuration / 2 * orderId);
        z2nAnim.setInterpolator(new LinearInterpolator());
        z2nAnim.addUpdateListener(this);
        return z2nAnim;
    }

    /**
     * 0 - double
     */
    private ObjectAnimator getZero2DoubleAnim(BallsLoadingShapeHolder ball, int orderId) {
        PropertyValuesHolder pvhW = PropertyValuesHolder.ofFloat("width",
                0, ball.getWidth() * 2);
        PropertyValuesHolder pvhH = PropertyValuesHolder.ofFloat("height",
                0, ball.getHeight() * 2);
        PropertyValuesHolder pvTX = PropertyValuesHolder.ofFloat("x",
                ball.getX() + mBallNormalRadius / 2, ball.getX() - mBallNormalRadius / 2);
        PropertyValuesHolder pvTY = PropertyValuesHolder.ofFloat("y",
                ball.getY() + mBallNormalRadius / 2, ball.getY() - mBallNormalRadius / 2);
        ObjectAnimator z2dAnim = ObjectAnimator.ofPropertyValuesHolder(
                ball, pvhW, pvhH, pvTX, pvTY).setDuration(mDuration / 3 * 2);
        z2dAnim.setStartDelay(mDuration / 3 * 2 * orderId);
        z2dAnim.setInterpolator(new LinearInterpolator());
        z2dAnim.addUpdateListener(this);
        return z2dAnim;
    }

    /**
     * double - 0
     */
    private ObjectAnimator getDouble2ZeroAnim(BallsLoadingShapeHolder ball, int orderId) {
        PropertyValuesHolder pvhW = PropertyValuesHolder.ofFloat("width",
                ball.getWidth() * 2, 0);
        PropertyValuesHolder pvhH = PropertyValuesHolder.ofFloat("height",
                ball.getHeight() * 2, 0);
        PropertyValuesHolder pvTX = PropertyValuesHolder.ofFloat("x",
                ball.getX() - mBallNormalRadius / 2, ball.getX() + mBallNormalRadius / 2);
        PropertyValuesHolder pvTY = PropertyValuesHolder.ofFloat("y",
                ball.getY() - mBallNormalRadius / 2, ball.getY() + mBallNormalRadius / 2);
        ObjectAnimator d2zAnim = ObjectAnimator.ofPropertyValuesHolder(
                ball, pvhW, pvhH, pvTX, pvTY).setDuration(mDuration / 3 * 2);
        d2zAnim.setInterpolator(new LinearInterpolator());
        d2zAnim.addUpdateListener(this);
        return d2zAnim;
    }

    /**
     * double - normal
     */
    private ObjectAnimator getDouble2NormalAnim(BallsLoadingShapeHolder ball, int orderId) {
        PropertyValuesHolder pvhW = PropertyValuesHolder.ofFloat("width",
                ball.getWidth() * 2, ball.getWidth());
        PropertyValuesHolder pvhH = PropertyValuesHolder.ofFloat("height",
                ball.getHeight() * 2, ball.getHeight());
        PropertyValuesHolder pvTX = PropertyValuesHolder.ofFloat("x",
                ball.getX() - mBallNormalRadius / 2, ball.getX());
        PropertyValuesHolder pvTY = PropertyValuesHolder.ofFloat("y",
                ball.getY() - mBallNormalRadius / 2, ball.getY());
        ObjectAnimator d2nAnim = ObjectAnimator.ofPropertyValuesHolder(
                ball, pvhW, pvhH, pvTX, pvTY).setDuration(mDuration / 3 * 2);
        d2nAnim.setInterpolator(new LinearInterpolator());
        d2nAnim.addUpdateListener(this);
        return d2nAnim;
    }

    /**
     * normal - double
     */
    private ObjectAnimator getNormal2DoubleAnim(BallsLoadingShapeHolder ball, int orderId) {
        PropertyValuesHolder pvhW = PropertyValuesHolder.ofFloat("width",
                ball.getWidth(), ball.getWidth() * 2);
        PropertyValuesHolder pvhH = PropertyValuesHolder.ofFloat("height",
                ball.getHeight(), ball.getHeight() * 2);
        PropertyValuesHolder pvTX = PropertyValuesHolder.ofFloat("x",
                ball.getX(), ball.getX() - mBallNormalRadius / 2);
        PropertyValuesHolder pvTY = PropertyValuesHolder.ofFloat("y",
                ball.getY(), ball.getY() - mBallNormalRadius / 2);
        ObjectAnimator n2dAnim = ObjectAnimator.ofPropertyValuesHolder(
                ball, pvhW, pvhH, pvTX, pvTY).setDuration(mDuration / 3 * 2);
        n2dAnim.setStartDelay(mDuration / 3 * 2 * orderId);
        n2dAnim.setInterpolator(new LinearInterpolator());
        n2dAnim.addUpdateListener(this);
        return n2dAnim;
    }

    private ObjectAnimator setExpandAnim(BallsLoadingShapeHolder ball, int orderId) {
        PropertyValuesHolder pvhW = PropertyValuesHolder.ofFloat("width",
                ball.getWidth(), ball.getWidth() * 2);
        PropertyValuesHolder pvhH = PropertyValuesHolder.ofFloat("height",
                ball.getHeight(), ball.getHeight() * 2);
        PropertyValuesHolder pvTX = PropertyValuesHolder.ofFloat("x",
                ball.getX(), ball.getX() - mBallNormalRadius / 2);
        PropertyValuesHolder pvTY = PropertyValuesHolder.ofFloat("y",
                ball.getY(), ball.getY() - mBallNormalRadius / 2);
        ObjectAnimator expandAnim = ObjectAnimator.ofPropertyValuesHolder(
                ball, pvhW, pvhH, pvTX, pvTY).setDuration(mDuration / 2);
        expandAnim.setRepeatCount(1); // -1:Infinite loop
        expandAnim.setRepeatMode(ValueAnimator.REVERSE);
        expandAnim.setStartDelay(mDuration / 2 * orderId);
        expandAnim.setInterpolator(new LinearInterpolator());
        expandAnim.addUpdateListener(this);
        return expandAnim;
    }

    /**
     * the animation will be running
     */
    public void loadStart() {

        if (mIsAnimLoading) {
            return;
        }
        initParams();
        createBeginAnimation();
        bounceAnim.start();
    }

    private void initParams() {
        mIsFinish = false;
        mIsAnimLoading = true;
        bounceAnim = null;
    }

    /**
     * the animation finish
     */
    public void loadFinish() {
        if (mIsAnimLoading) {
            mIsFinish = true;
        }
    }

    public boolean isLoading() {
        return mIsAnimLoading;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float heighoff = mBallNormalRadius / 2;
        float wightoff = mBallNormalRadius / 2;
        float offset = mBallNormalRadius / 2;
        switch (mGravity) {
            case 1: {
                heighoff = this.getHeight() / 2 - mBallNormalRadius / 2;
                wightoff = this.getWidth() / 2 - mSumWidth / 2 + offset;
            }
            break;
            case 2: {
                heighoff = this.getHeight() / 2 - mBallNormalRadius / 2;
            }
            break;
            case 3: {
                wightoff = this.getWidth() / 2 - mSumWidth / 2 + offset;
            }
            break;
        }
        for (BallsLoadingShapeHolder ball : mBalls) {
            canvas.translate(ball.getX() + wightoff, ball.getY() + heighoff);
            ball.getShape().draw(canvas);
            canvas.translate(-ball.getX() - wightoff, -ball.getY() - heighoff);
        }
    }

    /**
     * @see android.view.View#measure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }


    private int measureWidth(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = (int) (mSumWidth + getPaddingLeft()
                    + getPaddingRight());
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

    /**
     * Determines the height of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureHeight(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = (int) (mBallExpandRadius) + getPaddingTop()
                    + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    public int getBallColor() {
        return mBallColor;
    }

    public void setBallColor(int ballColor) {
        this.mBallColor = ballColor;
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int duration) {
        this.mDuration = duration;
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        this.mCount = count;
    }

    public float getBallNormalRadius() {
        return mBallNormalRadius;
    }

    public void setBallNormalRadius(float ballNormalRadius) {
        this.mBallNormalRadius = ballNormalRadius;
        this.mBallExpandRadius = ballNormalRadius * 2;
    }

    public float getBallDistance() {
        return mBallDistance;
    }

    public void setBallDistance(float ballDistance) {
        this.mBallDistance = ballDistance;
    }

    public void initAnimProcessBar() {
        initBall();
    }

    public void addAnimProcessBarListener(AnimProcessBarListener listener) {
        mProcessBarListener = listener;
    }

    public void cancleAnimProcessBarListener(AnimProcessBarListener listener) {
        mProcessBarListener = null;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        invalidate();
    }

    public int getGravity() {
        return mGravity;
    }

    public void setGravity(BallsLoadingGravity gravity) {
        this.mGravity = gravity.ordinal();
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
    public interface AnimProcessBarListener {
        /**
         * when the animation start the method will be called
         */
        void onLoadStart();

        /**
         * when the animation finished the method will be called
         */
        void onLoadFinished();
    }

    public enum BallsLoadingGravity {
        NORMAL, CENTER, CENTER_VERTICAL, CENTER_HORIZONTAL
    }
}