/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.letv.shared.widget;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.FloatMath;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

/**
 * This class encapsulates scrolling with the ability to overshoot the bounds
 * of a scrolling operation. This class is a drop-in replacement for
 * {@link android.widget.Scroller} in most cases.
 * 
 * {@hide}
 */
public class LeOverScroller {
	private final static String tag = "LeOverScroller";
	
    private int mMode;

    private MagneticOverScroller mScrollerX;
    private MagneticOverScroller mScrollerY;

    private final Interpolator mInterpolator;

    private static final int DEFAULT_DURATION = 250;
    private static final int SCROLL_MODE = 0;
    private static final int FLING_MODE = 1;

    public int getMode()
    {
    	return mMode;
    }
    
    /**
     * Creates an OverScroller with a viscous fluid scroll interpolator.
     * @param context
     */
    public LeOverScroller(Context context) {
        this(context, null);
    }

    /**
     * Creates an OverScroller with default edge bounce coefficients.
     * @param context The context of this application.
     * @param interpolator The scroll interpolator. If null, a default (viscous) interpolator will
     * be used.
     */
    public LeOverScroller(Context context, Interpolator interpolator) {
        this(context, interpolator, MagneticOverScroller.DEFAULT_BOUNCE_COEFFICIENT,
                MagneticOverScroller.DEFAULT_BOUNCE_COEFFICIENT);
    }

    /**
     * Creates an OverScroller.
     * @param context The context of this application.
     * @param interpolator The scroll interpolator. If null, a default (viscous) interpolator will
     * be used.
     * @param bounceCoefficientX A value between 0 and 1 that will determine the proportion of the
     * velocity which is preserved in the bounce when the horizontal edge is reached. A null value
     * means no bounce.
     * @param bounceCoefficientY Same as bounceCoefficientX but for the vertical direction.
     */
    public LeOverScroller(Context context, Interpolator interpolator,
            float bounceCoefficientX, float bounceCoefficientY) {
        mInterpolator = interpolator;
        mScrollerX = new MagneticOverScroller();
        mScrollerY = new MagneticOverScroller();
        MagneticOverScroller.initializeFromContext(context);

        mScrollerX.setBounceCoefficient(bounceCoefficientX);
        mScrollerY.setBounceCoefficient(bounceCoefficientY);
    }

    /**
     *
     * Returns whether the scroller has finished scrolling.
     *
     * @return True if the scroller has finished scrolling, false otherwise.
     */
    public final boolean isFinished() {
        return mScrollerX.mFinished && mScrollerY.mFinished;
    }

    /**
     * Force the finished field to a particular value. Contrary to
     * {@link #abortAnimation()}, forcing the animation to finished
     * does NOT cause the scroller to move to the final x and y
     * position.
     *
     * @param finished The new finished value.
     */
    public final void forceFinished(boolean finished) {
        mScrollerX.mFinished = mScrollerY.mFinished = finished;
    }

    /**
     * Returns the current X offset in the scroll.
     *
     * @return The new X offset as an absolute distance from the origin.
     */
    public final int getCurrX() {
        return mScrollerX.mCurrentPosition;
    }

    /**
     * Returns the current Y offset in the scroll.
     *
     * @return The new Y offset as an absolute distance from the origin.
     */
    public final int getCurrY() {
        return mScrollerY.mCurrentPosition;
    }

    /**
     * @hide
     * Returns the current velocity.
     *
     * @return The original velocity less the deceleration, norm of the X and Y velocity vector.
     */
    public float getCurrVelocity() {
        float squaredNorm = mScrollerX.mCurrVelocity * mScrollerX.mCurrVelocity;
        squaredNorm += mScrollerY.mCurrVelocity * mScrollerY.mCurrVelocity;
        return FloatMath.sqrt(squaredNorm);
    }

    /**
     * Returns the start X offset in the scroll.
     *
     * @return The start X offset as an absolute distance from the origin.
     */
    public final int getStartX() {
        return mScrollerX.mStart;
    }

    /**
     * Returns the start Y offset in the scroll.
     *
     * @return The start Y offset as an absolute distance from the origin.
     */
    public final int getStartY() {
        return mScrollerY.mStart;
    }

    /**
     * Returns where the scroll will end. Valid only for "fling" scrolls.
     *
     * @return The final X offset as an absolute distance from the origin.
     */
    public final int getFinalX() {
        return mScrollerX.mFinal;
    }

    /**
     * Returns where the scroll will end. Valid only for "fling" scrolls.
     *
     * @return The final Y offset as an absolute distance from the origin.
     */
    public final int getFinalY() {
        return mScrollerY.mFinal;
    }

    /**
     * Returns how long the scroll event will take, in milliseconds.
     *
     * @return The duration of the scroll in milliseconds.
     *
     * @hide Pending removal once nothing depends on it
     * @deprecated OverScrollers don't necessarily have a fixed duration.
     *             This function will lie to the best of its ability.
     */
    public final int getDuration() {
        return Math.max(mScrollerX.mDuration, mScrollerY.mDuration);
    }

    /**
     * Extend the scroll animation. This allows a running animation to scroll
     * further and longer, when used with {@link #setFinalX(int)} or {@link #setFinalY(int)}.
     *
     * @param extend Additional time to scroll in milliseconds.
     * @see #setFinalX(int)
     * @see #setFinalY(int)
     *
     * @hide Pending removal once nothing depends on it
     * @deprecated OverScrollers don't necessarily have a fixed duration.
     *             Instead of setting a new final position and extending
     *             the duration of an existing scroll, use startScroll
     *             to begin a new animation.
     */
    public void extendDuration(int extend) {
        mScrollerX.extendDuration(extend);
        mScrollerY.extendDuration(extend);
    }

    /**
     * Sets the final position (X) for this scroller.
     *
     * @param newX The new X offset as an absolute distance from the origin.
     * @see #extendDuration(int)
     * @see #setFinalY(int)
     *
     * @hide Pending removal once nothing depends on it
     * @deprecated OverScroller's final position may change during an animation.
     *             Instead of setting a new final position and extending
     *             the duration of an existing scroll, use startScroll
     *             to begin a new animation.
     */
    public void setFinalX(int newX) {
        mScrollerX.setFinalPosition(newX);
    }

    /**
     * Sets the final position (Y) for this scroller.
     *
     * @param newY The new Y offset as an absolute distance from the origin.
     * @see #extendDuration(int)
     * @see #setFinalX(int)
     *
     * @hide Pending removal once nothing depends on it
     * @deprecated OverScroller's final position may change during an animation.
     *             Instead of setting a new final position and extending
     *             the duration of an existing scroll, use startScroll
     *             to begin a new animation.
     */
    public void setFinalY(int newY) {
        mScrollerY.setFinalPosition(newY);
    }

    /**
     * Call this when you want to know the new location. If it returns true, the
     * animation is not yet finished.
     */
    public boolean computeScrollOffset() {
        if (isFinished()) {
            return false;
        }

        switch (mMode) {
            case SCROLL_MODE:
                long time = AnimationUtils.currentAnimationTimeMillis();
                // Any scroller can be used for time, since they were started
                // together in scroll mode. We use X here.
                final long elapsedTime = time - mScrollerX.mStartTime;
                final int duration = mScrollerX.mDuration;
                if (elapsedTime < duration) {
                    float q = (float) (elapsedTime) / duration;

                    if (mInterpolator == null)
                        q = viscousFluid(q);
                    else
                        q = mInterpolator.getInterpolation(q);

                    mScrollerX.updateScroll(q);
                    mScrollerY.updateScroll(q);
                } else {
                    abortAnimation();
                }
                break;

            case FLING_MODE:
                if (!mScrollerX.mFinished) {
                    if (!mScrollerX.update()) {//update返回false时，表示fling结束
                        if (!mScrollerX.continueWhenFinished()) {//某一阶段的运动已经结束，测试是否需要继续进行另外的运动
                            mScrollerX.finish();
                        }
                    }
                }

                if (!mScrollerY.mFinished) {
                    if (!mScrollerY.update()) {//
                        if (!mScrollerY.continueWhenFinished()) {
                            mScrollerY.finish();
                        }
                    }
                    mLastUpdateTime = AnimationUtils.currentAnimationTimeMillis();
                }

                break;
        }

        return true;
    }
    
    private long mLastUpdateTime = 0;

    /**
     * Start scrolling by providing a starting point and the distance to travel.
     * The scroll will use the default value of 250 milliseconds for the
     * duration.
     *
     * @param startX Starting horizontal scroll offset in pixels. Positive
     *        numbers will scroll the content to the left.
     * @param startY Starting vertical scroll offset in pixels. Positive numbers
     *        will scroll the content up.
     * @param dx Horizontal distance to travel. Positive numbers will scroll the
     *        content to the left.
     * @param dy Vertical distance to travel. Positive numbers will scroll the
     *        content up.
     */
    public void startScroll(int startX, int startY, int dx, int dy) {
        startScroll(startX, startY, dx, dy, DEFAULT_DURATION);
    }

    /**
     * Start scrolling by providing a starting point and the distance to travel.
     *
     * @param startX Starting horizontal scroll offset in pixels. Positive
     *        numbers will scroll the content to the left.
     * @param startY Starting vertical scroll offset in pixels. Positive numbers
     *        will scroll the content up.
     * @param dx Horizontal distance to travel. Positive numbers will scroll the
     *        content to the left.
     * @param dy Vertical distance to travel. Positive numbers will scroll the
     *        content up.
     * @param duration Duration of the scroll in milliseconds.
     */
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        mMode = SCROLL_MODE;
        mScrollerX.startScroll(startX, dx, duration);
        mScrollerY.startScroll(startY, dy, duration);
    }

    /**
     * Call this when you want to 'spring back' into a valid coordinate range.
     *
     * @param startX Starting X coordinate
     * @param startY Starting Y coordinate
     * @param minX Minimum valid X value
     * @param maxX Maximum valid X value
     * @param minY Minimum valid Y value
     * @param maxY Minimum valid Y value
     * @return true if a springback was initiated, false if startX and startY were
     *          already within the valid range.
     */
    //手动去回弹到正常位置
    public boolean springBack(int startX, int startY, int minX, int maxX, int minY, int maxY) {
        mMode = FLING_MODE;

        // Make sure both methods are called.
        final boolean spingbackX = mScrollerX.springback(startX, minX, maxX);
        final boolean spingbackY = mScrollerY.springback(startY, minY, maxY);
        return spingbackX || spingbackY;
    }

    public void fling(int startX, int startY, int velocityX, int velocityY,
            int minX, int maxX, int minY, int maxY) {
        fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, 0, 0);
    }

    /**
     * Start scrolling based on a fling gesture. The distance traveled will
     * depend on the initial velocity of the fling.
     *
     * @param startX Starting point of the scroll (X)
     * @param startY Starting point of the scroll (Y)
     * @param velocityX Initial velocity of the fling (X) measured in pixels per
     *            second.
     * @param velocityY Initial velocity of the fling (Y) measured in pixels per
     *            second
     * @param minX Minimum X value. The scroller will not scroll past this point
     *            unless overX > 0. If overfling is allowed, it will use minX as
     *            a springback boundary.
     * @param maxX Maximum X value. The scroller will not scroll past this point
     *            unless overX > 0. If overfling is allowed, it will use maxX as
     *            a springback boundary.
     * @param minY Minimum Y value. The scroller will not scroll past this point
     *            unless overY > 0. If overfling is allowed, it will use minY as
     *            a springback boundary.
     * @param maxY Maximum Y value. The scroller will not scroll past this point
     *            unless overY > 0. If overfling is allowed, it will use maxY as
     *            a springback boundary.
     * @param overX Overfling range. If > 0, horizontal overfling in either
     *            direction will be possible.
     * @param overY Overfling range. If > 0, vertical overfling in either
     *            direction will be possible.
     */
    public void fling(int startX, int startY, int velocityX, int velocityY,
            int minX, int maxX, int minY, int maxY, int overX, int overY) {
        mMode = FLING_MODE;
        mScrollerX.fling(startX, velocityX, minX, maxX, overX);
        mScrollerY.fling(startY, velocityY, minY, maxY, overY);
    }

    /**
     * Notify the scroller that we've reached a horizontal boundary.
     * Normally the information to handle this will already be known
     * when the animation is started, such as in a call to one of the
     * fling functions. However there are cases where this cannot be known
     * in advance. This function will transition the current motion and
     * animate from startX to finalX as appropriate.
     *
     * @param startX Starting/current X position
     * @param finalX Desired final X position
     * @param overX Magnitude of overscroll allowed. This should be the maximum
     *              desired distance from finalX. Absolute value - must be positive.
     */
    public void notifyHorizontalEdgeReached(int startX, int finalX, int overX) {
    	mMode = FLING_MODE;
        mScrollerX.notifyEdgeReached(startX, finalX, overX);
    }

    /**
     * Notify the scroller that we've reached a vertical boundary.
     * Normally the information to handle this will already be known
     * when the animation is started, such as in a call to one of the
     * fling functions. However there are cases where this cannot be known
     * in advance. This function will animate a parabolic motion from
     * startY to finalY.
     *
     * @param startY Starting/current Y position
     * @param finalY Desired final Y position
     * @param overY Magnitude of overscroll allowed. This should be the maximum
     *              desired distance from finalY.
     */
    public void notifyVerticalEdgeReached(int startY, int finalY, int overY) {
    	mMode = FLING_MODE;
        mScrollerY.notifyEdgeReached(startY, finalY, overY);
    }

    /**
     * Returns whether the current Scroller is currently returning to a valid position.
     * Valid bounds were provided by the
     * {@link #fling(int, int, int, int, int, int, int, int, int, int)} method.
     *
     * One should check this value before calling
     * {@link #startScroll(int, int, int, int)} as the interpolation currently in progress
     * to restore a valid position will then be stopped. The caller has to take into account
     * the fact that the started scroll will start from an overscrolled position.
     *
     * @return true when the current position is overscrolled and in the process of
     *         interpolating back to a valid value.
     */
    public boolean isOverScrolled() {
        return ((!mScrollerX.mFinished &&
                mScrollerX.mState != MagneticOverScroller.FLING) ||
                (!mScrollerY.mFinished &&
                        mScrollerY.mState != MagneticOverScroller.FLING));
    }
    
    private static float sViscousFluidScale;
    private static float sViscousFluidNormalize;
    
    static {
        // This controls the viscous fluid effect (how much of it)
        sViscousFluidScale = 8.0f;
        // must be set to 1.0 (used in viscousFluid())
        sViscousFluidNormalize = 1.0f;
        sViscousFluidNormalize = 1.0f / viscousFluid(1.0f);

    }
    
    static float viscousFluid(float x)
    {
        x *= sViscousFluidScale;
        if (x < 1.0f) {
            x -= (1.0f - (float)Math.exp(-x));
        } else {
            float start = 0.36787944117f;   // 1/e == exp(-1)
            x = 1.0f - (float)Math.exp(1.0f - x);
            x = start + x * (1.0f - start);
        }
        x *= sViscousFluidNormalize;
        return x;
    }

    /**
     * Stops the animation. Contrary to {@link #forceFinished(boolean)},
     * aborting the animating causes the scroller to move to the final x and y
     * positions.
     *
     * @see #forceFinished(boolean)
     */
    public void abortAnimation() {
        mScrollerX.finish();
        mScrollerY.finish();
    }

    /**
     * Returns the time elapsed since the beginning of the scrolling.
     *
     * @return The elapsed time in milliseconds.
     *
     * @hide
     */
    public int timePassed() {
        final long time = AnimationUtils.currentAnimationTimeMillis();
        final long startTime = Math.min(mScrollerX.mStartTime, mScrollerY.mStartTime);
        return (int) (time - startTime);
    }
    
    /**
     * @hide
     */
    public void setEnabledGravityDeceleration(boolean enabled)
    {
        mScrollerX.setEnabledGravityDeceleration(enabled);
        mScrollerY.setEnabledGravityDeceleration(enabled);
    }

    static class MagneticOverScroller {
        // Initial position
    	//初始位置
        int mStart;

        // Current position
        //当前位置
        int mCurrentPosition;

        // Final position
        //最终位置
        int mFinal;

        // Initial velocity
        //初始速度
        int mVelocity;

        // Current velocity
        //当前速度
        float mCurrVelocity;

        // Constant current deceleration
        //当前的加速度
        float mDeceleration;

        // Animation starting time, in system milliseconds
        //动画开始的时间
        long mStartTime;

        // Animation duration, in milliseconds
        //动画完成需要的时间
        int mDuration;

        // Whether the animation is currently in progress
        //动画是否结束
        boolean mFinished;

        // Constant gravity value, used to scale deceleration
        static float GRAVITY;

        // 
        private int mIterateCount = 0;
        private float mLastDistance = 0.0f;
        private float mDelta = 0.0f;
        private float mCoeffDeceleration = 0.0f;
        private int mSpringbackEnd = 0;
        private int mSpringDistance = 0;
        
        private boolean mGravityDeceleration = false;
        //测试用
        private static final int MAXFLINGTESTCOUNT = 4;
        private final static int MAXUPDATECOUNT = 5;
        private int mFlingTestCount = 1;
        private int mUpdateCount = 0;
        
        private static final int DECELERATIONSPEED_FAST = 50;
        private static final int DECELERATIONSPEED_SLOW = 25;
        private int mDecelerationSpeed = DECELERATIONSPEED_FAST;
        private long mAverageTime = 0;
        
        
        static void initializeFromContext(Context context) {
            final float ppi = context.getResources().getDisplayMetrics().density * 160.0f;
            GRAVITY = SensorManager.GRAVITY_EARTH // g (m/s^2)
                    * 39.37f // inch/meter
                    * ppi // pixels per inch
                    * ViewConfiguration.getScrollFriction();
        }

        /*
         * view的fling过程可以分为三个阶段,(以bottom over scroll为例)
         * 
         * view的尺寸边距
         * |----------------------------------| view的bottom
         * 
         * 1.FLING:view正常滚动，其最后一个子view的bottom在它之下
         * 
         * 子views
         * |--------------------------------------------|
         * 									  |		<----向着parent的bottom移动
         * 
         * 2.OVERFLING:最后一个子view的bottom在parent的bottom之上了，但仍要远离parent的bottom移动
         * 
         * 子views
         * |-------------------------- |
         *  远离parent的bottom移动 <-----		  |	
         *  
         * 3.SPRINGBACK:越界后，最终要归位。
         * 
         * 子views
         * |-------------------------- |
         * 		  向着parent的bottom移动 -----> |	
         */

        //fling分三个过程
        private static final int FLING = 0;
        private static final int OVERFLING = 1;
        private static final int SPRINGBACK = 2;
        
        //默认为Fling过程
        private int mState = FLING;

        //允许越界的距离
        // The allowed overshot distance before boundary is reached.
        private int mOver;

        // Duration in milliseconds to go back from edge to edge. Springback is half of it.
        //回弹的时间，
        private static final int OVERSCROLL_SPRINGBACK_DURATION = 618;

        // Oscillation period
        //震荡周期
        private static final float TIME_COEF =
            1000.0f * (float) Math.PI / OVERSCROLL_SPRINGBACK_DURATION;

        // If the velocity is smaller than this value, no bounce is triggered
        // when the edge limits are reached (would result in a zero pixels
        // displacement anyway).
        //允许回弹的最低速度，若少于这个速度，则不会有回弹效果
        private static final float MINIMUM_VELOCITY_FOR_BOUNCE = Float.MAX_VALUE;//140.0f;

        // Proportion of the velocity that is preserved when the edge is reached.
        //当fling到view的边缘时，会根据当前的速度乘以这个比例作为回弹的初速度。
        private static final float DEFAULT_BOUNCE_COEFFICIENT = 0.16f;

        private float mBounceCoefficient = DEFAULT_BOUNCE_COEFFICIENT;

        MagneticOverScroller() {
            mFinished = true;
            mFlingTestCount = 1;
            mAverageTime = 0;
        }

        //更新scroll位置
        void updateScroll(float q) {
        	//
            mCurrentPosition = mStart + Math.round(q * (mFinal - mStart));
        }

        /*
         * Get a signed deceleration that will reduce the velocity.
         */
        //根据速度方向，取得对应的加速度，有方向的
        static float getDeceleration(int velocity) {
            return velocity > 0 ? -GRAVITY : GRAVITY;
        }

        /*
         * Returns the time (in milliseconds) it will take to go from start to end.
         */
        //以初速度做减速运动所需时间。
        static int computeDuration(int start, int end, float initialVelocity, float deceleration) {
            final int distance = start - end;
            final float discriminant = initialVelocity * initialVelocity - 2.0f * deceleration
                    * distance;
            if (discriminant >= 0.0f) {
                float delta = (float) Math.sqrt(discriminant);
                if (deceleration < 0.0f) {
                    delta = -delta;
                }
                return (int) (1000.0f * (-initialVelocity - delta) / deceleration);
            }

            // End position can not be reached
            return 0;
        }

        void startScroll(int start, int distance, int duration) {
            mFinished = false;

            mStart = start;
            mFinal = start + distance;

            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mDuration = duration;

            // Unused
            mDeceleration = 0.0f;
            mVelocity = 0;
        }

        //暂时没有地方使用到这个方法
        void fling(int start, int velocity, int min, int max) {
            mFinished = false;

            mStart = start;
            mStartTime = AnimationUtils.currentAnimationTimeMillis();

            mVelocity = velocity;

            mDeceleration = getDeceleration(velocity);

            // A start from an invalid position immediately brings back to a valid position
            //处理非法位置
            if (mStart < min) {
                mDuration = 0;
                mFinal = min;
                return;
            }

            if (mStart > max) {
                mDuration = 0;
                mFinal = max;
                return;
            }

            // Duration are expressed in milliseconds
            mDuration = (int) (-1000.0f * velocity / mDeceleration);

            mFinal = start - Math.round((velocity * velocity) / (2.0f * mDeceleration));

            // Clamp to a valid final position
            if (mFinal < min) {
                mFinal = min;
                mDuration = computeDuration(mStart, min, mVelocity, mDeceleration);
            }

            if (mFinal > max) {
                mFinal = max;
                mDuration = computeDuration(mStart, max, mVelocity, mDeceleration);
            }
        }

        void finish() {
            mCurrentPosition = mFinal;
            // Not reset since WebView relies on this value for fast fling.
            // mCurrVelocity = 0.0f;
            mFinished = true;
        }

        void setFinalPosition(int position) {
            mFinal = position;
            mFinished = false;
        }

        void extendDuration(int extend) {
            final long time = AnimationUtils.currentAnimationTimeMillis();
            final int elapsedTime = (int) (time - mStartTime);
            mDuration = elapsedTime + extend;
            mFinished = false;
        }

        void setBounceCoefficient(float coefficient) {
            mBounceCoefficient = coefficient;
        }

        //越界了，开始回弹
        //start 当前over scroll 的位置
        boolean springback(int start, int min, int max) {
            mFinished = true;

            mStart = start;

            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mDuration = 0;

            if (start < min) {
                startSpringback(start, min, false);
            } else if (start > max) {
                startSpringback(start, max, true);
            }
        	
            return !mFinished;
        }
        
        private void startSpringback(int start, int end, boolean positive) {
            mFinished = false;
            mState = SPRINGBACK;
            mCoeffDeceleration = 0.7f;
            mFinal = end;
            int distance = end - start;
            mDelta = (float) (distance * (1 - 0.95f) / (1 - Math.pow(0.95, 50)));
            mVelocity = (int) (mDelta * mDecelerationSpeed);
        	mCurrVelocity = mVelocity;
        	mLastDistance = 0.0f;
        	
        	mSpringDistance = distance;
        	
        	if(mSpringDistance == 0)
        		mDuration = 0;
        	else
        		mDuration = OVERSCROLL_SPRINGBACK_DURATION;
        	mStartTime = AnimationUtils.currentAnimationTimeMillis();
        }
        
        /**
         * 正常的fing
         * 
         * @param start 初始位置
         * @param velocity 初始速度
         * @param min 最小位置
         * @param max 最大位置
         * @param over 允许over scroll的范围
         */
        void fling(int start, int velocity, int min, int max, int over) {
            mState = FLING;
            mOver = over;

            mFinished = false;
            mStart = start;
            mCurrentPosition = start;           
            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mVelocity = velocity;
            mCurrVelocity = mVelocity;
            int absVelocity = Math.abs(mVelocity);
            mFlingTestCount++;
            mUpdateCount = 0;
            //初始化fling
            //包括:总的运动时间，最终位置
            
            //mGravityDeceleration == true,使用匀减速方式
            if(mGravityDeceleration)
            {
                //使用匀减速方式，距离根据时间确定
                mDeceleration = getDeceleration(velocity);
                // Duration are expressed in milliseconds
                mDuration = (int) (-1000.0f * velocity / mDeceleration);
                mFinal = start - Math.round((velocity * velocity) / (2.0f * mDeceleration));
            }
            else
            {
                //每一次的距离根据比例减少
                mIterateCount = 0;
                mLastDistance = 0.0f;
                mCoeffDeceleration = 0.97f;
                
                int delta = 0;
                int i = 0;
                
                mDelta = 1.0f*mVelocity/mDecelerationSpeed;
                
                while(true)
                {
                    delta = (int)(mDelta * Math.pow(mCoeffDeceleration, i));
                    if(delta == 0)
                    {
                        mIterateCount = i;
                        break;
                    }
                    i++;
                }
                int totalDistance = (int) (mDelta * (1.0f - Math.pow(mCoeffDeceleration, i)) / (1.0f - mCoeffDeceleration));
                
                if(absVelocity > 2000)
                    mDuration = 5000;
                else if(absVelocity < 200){
                    mDuration = 0;//初速太小，不需要做动画，直接给出当前位置；for  cts test : flingscroll(100,100);
                    mCurrentPosition = mStart + totalDistance;
                }else
                    mDuration = 3000;
                
                mFinal = mStart + totalDistance;
            }
            
            // Clamp to a valid final position
            if (mFinal < min) {
                mFinal = min;
            }

            if (mFinal > max) {
                mFinal = max;
            }
            
            //初始时，位置已经越界
            //如果未达到over fling 界限，继续over fling
            //否则，直接执行spring back
            if (start > max) {
                if (start >= max + over) {
                    springback(max + over, min, max);
                } else {
                    if (velocity <= 0) {
                        springback(start, min, max);
                    } else {
                        long time = AnimationUtils.currentAnimationTimeMillis();
                        //模拟从view 的edge 运动到此距离的时间
                        final double durationSinceEdge =
                            Math.atan((start-max) * TIME_COEF / velocity) / TIME_COEF;
                        mStartTime = (int) (time - 1000.0f * durationSinceEdge);

                        // Simulate a bounce that started from edge
                        
                        /*
                         * 					max over scroll distance
                         *  |----------------|--------| list bottom
                         *  -----------------|---| last child bottom 
                         *  			      <---over scroll(Action Up),begin to over fling
                         *  
                         *  在这种情况下，已经处于over scroll但未到达max over scroll distance,此时，仍要执行一段fling
                         *  这里是模拟从list bottom 的位置开始执行回弹的，实际上是离list bottom的一段距离开始的，所以，要
                         *  算出这段距离在回弹的过程中的
                         */
                        mStart = max;

                        mVelocity = (int) (velocity / Math.cos(durationSinceEdge * TIME_COEF));

                        onEdgeReached();
                    }
                }
            } else {
                if (start < min) {
                    if (start <= min - over) {
                        springback(min - over, min, max);
                    } else {
                        if (velocity >= 0) {
                            springback(start, min, max);
                        } else {
                            long time = AnimationUtils.currentAnimationTimeMillis();
                            final double durationSinceEdge =
                                Math.atan((start-min) * TIME_COEF / velocity) / TIME_COEF;
                            mStartTime = (int) (time - 1000.0f * durationSinceEdge);

                            // Simulate a bounce that started from edge
                            mStart = min;

                            mVelocity = (int) (velocity / Math.cos(durationSinceEdge * TIME_COEF));

                            onEdgeReached();
                        }

                    }
                }
            }
        }
        
//        void notifyEdgeReadchedByScroll(int start, int end)
//        {
//        	mStart = start;
//        	mFinal = end;
//        	long timePassed =  AnimationUtils.currentAnimationTimeMillis() - mStartTime;
//        	mStartTime -= timePassed;
//        }

        //viewfling到了边缘，通过此方法来通知scroller
        /*
         * start 起始位置
         * end 结束位置
         * over 允许越界范围
         */
        void notifyEdgeReached(int start, int end, int over) {
        	if(Math.abs(start) < over && (start != end))
        	{
        		mState = OVERFLING;
            	mCoeffDeceleration = 0.5f;
            	mLastDistance = 0;
            	mStart = start;
            	mSpringbackEnd = end;
            	mFinished = false;
            	mDuration = Integer.MAX_VALUE;
            	
            	int delta = 0;
                int i = 0;
                
                mDelta = mCurrVelocity / 150.0f;
                while(true)
                {
                    delta = (int)(mDelta * Math.pow(mCoeffDeceleration, i));
                    if(delta == 0)
                    {
                    	mIterateCount = i;
                        break;
                    }
                    i++;
                }
                int totalDistance = (int) (mDelta * (1.0f - Math.pow(mCoeffDeceleration, i)) / (1.0f - mCoeffDeceleration));
                mFinal = mStart + totalDistance;
        	}
        	else
        	{
        		springback(start, 0, end);
        	}
        	
        }

        //到达边缘，更改运动方式
        private void onEdgeReached() {
            // mStart, mVelocity and mStartTime were adjusted to their values when edge was reached.
        	//越界距离只是简单的以初速度乘以一个比例
            final float distance = mVelocity / TIME_COEF;
//            Log.d(tag, "chang edge:current distance/over distance"+distance+"/"+mOver);
            if (Math.abs(distance) < mOver) {
                // Spring force will bring us back to final position
                mState = FLING;
                mFinal = mStart;
                mDuration = OVERSCROLL_SPRINGBACK_DURATION;
            } else {
                // Velocity is too high, we will hit the boundary limit
//            	Log.d(tag, "on edge change:to boundary now");
                mState = FLING;
                int over = mVelocity > 0 ? mOver : -mOver;
                mFinal = mStart + over;
//                mDuration = (int) (1000.0f * Math.asin(over / distance) / TIME_COEF);
                mDuration = OVERSCROLL_SPRINGBACK_DURATION;
            }
        }

        boolean continueWhenFinished() { 
            switch (mState) {
                case FLING:
                	return false;
                case SPRINGBACK:
                    return false;
                case OVERFLING:
                	mState = SPRINGBACK;
                	mStart = mFinal;
                	int distance = mSpringbackEnd - mFinal;
                	mFinal = mSpringbackEnd;
                	mCoeffDeceleration = 0.95f;
                	mDelta = (float) (distance * (1 - 0.95f) / (1 - Math.pow(0.95, 50)));
                	mVelocity = (int) (mDelta * 50.0f);
                	mCurrVelocity = mVelocity;
                	mLastDistance = 0.0f;
                	mDuration = OVERSCROLL_SPRINGBACK_DURATION;
                	mSpringDistance = distance;
                	mStartTime = AnimationUtils.currentAnimationTimeMillis();
            }

            update();
            return true;
        }

        /*
         * Update the current position and velocity for current time. Returns
         * true if update has been done and false if animation duration has been
         * reached.
         */
        private boolean update() {
            final long time = AnimationUtils.currentAnimationTimeMillis();
            final long duration = time - mStartTime;

            //超时了，fling结束
            if (duration > mDuration) {
            	if(mIterateCount != 0)
            		mFinal = mCurrentPosition;
                mFinished = true;
                return false;
            }
            
            float distance;
            final float t = duration / 1000.0f;
            //这里同时更新速度和运动距离
            //正常的fling运动
            if (mState == FLING) {
                
                if(mGravityDeceleration)
                {
                    mCurrVelocity = mVelocity + mDeceleration * t;
                    distance = mVelocity * t + mDeceleration * t * t / 2.0f;
                }
                else
                {
                   mUpdateCount++;
                   if(mUpdateCount == MAXUPDATECOUNT)
                   {
                       mAverageTime = (mAverageTime + duration/mUpdateCount)/2;
//                       Log.d(tag, "fling count/update count:"+mFlingTestCount +"/"+mUpdateCount+"/"+mAverageTime);
                       if(mAverageTime < 20)
                       {
                           mDecelerationSpeed = DECELERATIONSPEED_FAST;
                       }
                       else if(mAverageTime < 40)
                       {
                           mDecelerationSpeed =  DECELERATIONSPEED_SLOW;
                       }
                       else if(mAverageTime > 40 && mFlingTestCount >= MAXFLINGTESTCOUNT)
                       {
                           mGravityDeceleration = true;
                       }
                   }
                    
                    mCurrVelocity *= mCoeffDeceleration;
                    distance = mLastDistance + mDelta;
                    mDelta *= mCoeffDeceleration;
                    mLastDistance = distance;
                }
                mCurrentPosition = mStart + (int) distance;
//               Log.d(tag, "current distance＆velocity:"+mCurrentPosition +"/"+mDelta);
            }else if(mState == OVERFLING)
            {
                mCurrVelocity *= mCoeffDeceleration;
                distance = mLastDistance + mDelta;
                mDelta *= mCoeffDeceleration;
                mLastDistance = distance;
                mCurrentPosition = mStart + (int) distance;
//                Log.d(tag, "current distance＆velocity over fling:"+mCurrentPosition);
            }
            else {
               mCurrentPosition = quintic(duration,mStart,mSpringDistance,mDuration);
//               Log.d(tag, "current distance＆velocity springback:"+mCurrentPosition);
            }

            if(mCurrentPosition == mFinal)
            	return false;
            else
            	return true;
        }
        
        /**
         * 
         * @param timePassed 当前时间
         * @param start 起始位置
         * @param distance 总路程
         * @param duration 总的时间
         * @return 当前位置
         */
        //springback位置的计算公式,五次方运算
        private int quintic(long timePassed,int start,int distance,long duration) 
        {
//          Quintic
        	float coeff = 1.0f * timePassed/duration - 1;
        	return (int) (distance * (Math.pow(coeff, 5) + 1) + start);
        }
        
        public void setEnabledGravityDeceleration(boolean enabled)
        {
            mGravityDeceleration = enabled;
        }
    }
    
    
    
}
