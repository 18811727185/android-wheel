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

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Switch;

import com.letv.shared.R;

public class LeSwitch extends Switch implements LeCheckable,
        ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
    private static final int DISABLE_ALPHA = (int) (255 * 0.3);
    private static final int TOUCH_MODE_IDLE = 0;
    private static final int TOUCH_MODE_DOWN = 1;
    private static final int TOUCH_MODE_DRAGGING = 2;
    private final int mTrackRadius;
    private ObjectAnimator mCurrentAnimator;
    private final ObjectAnimator mThumbOnAnimator;
    private final ObjectAnimator mThumbOffAnimator;
    private int mThumbAnimateTime;
    private int mTrackColor;

    private Drawable mThumbDrawable;
    private Drawable mTrackDrawable;

    private int mTouchMode;
    private final int mTouchSlop;
    private float mTouchX;
    private float mTouchY;
    private final VelocityTracker mVelocityTracker = VelocityTracker.obtain();
    private final int mMinFlingVelocity;

    private float mThumbPosition;
    private final int mSwitchWidth;
    private final int mSwitchHeight;
    private final int mThumbWidth; // Does not include padding

    private int mSwitchLeft;
    private int mSwitchTop;
    private int mSwitchRight;
    private int mSwitchBottom;

    private final Rect mTempRect = new Rect();
    private int mSwitchPivotX;
    private int mSwitchPivotY;
    private final int mThumbHeight;
    private final RectF mTempRectF = new RectF();
    private final TextPaint mPaint;

    private int mAlpha = 255;
    RectF mSaveLayerRectF = new RectF();

    public LeSwitch(Context context) {
        this(context, null);
    }

    public LeSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.checkedTextViewStyle);
    }

    public LeSwitch(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        Resources res = this.getResources();

        // setTextOff("");
        // setTextOn("");
        //this.setShowText(false);

        this.mTrackColor = res.getColor(R.color.le_color_default_switch_on);
        this.mThumbDrawable = res.getDrawable(R.drawable.le_switch_thumb);
        this.mTrackDrawable = res.getDrawable(R.drawable.le_switch_track);
        this.mThumbAnimateTime = res
                .getInteger(R.integer.le_default_switch_animate_time);

        TypedValue outValue = new TypedValue();
        final Resources.Theme theme = context.getTheme();
        //if (theme.resolveAttribute(android.R.attr.colorControlActivated,
            //    outValue, true)) {
            this.mTrackColor = outValue.data;
       // }

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.leSwitch, defStyle, 0);
        if (a != null) {
            int n = a.getIndexCount();
            // NOTE:by letv leading app
            Drawable dw = a.getDrawable(R.styleable.leSwitch_leSwitchTrackBg);
            if (dw != null) {
                this.mThumbDrawable = dw;
            }
            this.mTrackColor = a.getColor(
                    R.styleable.leSwitch_leSwitchTrackColor, this.mTrackColor);
            this.mThumbAnimateTime = a.getDimensionPixelSize(
                    R.styleable.leSwitch_leSwitchThumbAnimateTime,
                    this.mThumbAnimateTime);
            /*
             * for (int i = 0; i < n; i ++) {
             * int attr = a.getIndex(i);
             * switch (attr) {
             * case R.styleable.leSwitch_leSwitchTrackBg:
             * mThumbDrawable = a.getDrawable(attr);
             * break;
             * case R.styleable.leSwitch_leSwitchTrackColor:
             * mTrackColor = a.getColor(attr, mTrackColor);
             * break;
             * case R.styleable.leSwitch_leSwitchThumbAnimateTime:
             * mThumbAnimateTime = a.getDimensionPixelSize(attr,
             * mThumbAnimateTime);
             * break;
             * }
             * }
             */
        }
        a.recycle();

        boolean clickable = true;
        a = context
                .obtainStyledAttributes(attrs, R.styleable.View, defStyle, 0);

        // clickable = a.getBoolean(R.styleable.View_clickable, clickable);
        a.recycle();
        this.setClickable(clickable);

        this.mThumbWidth = this.mThumbDrawable.getIntrinsicWidth();
        this.mThumbHeight = this.mThumbDrawable.getIntrinsicHeight();
        this.mTrackRadius = this.mThumbHeight / 2;
        this.mPaint = this.getPaint();

        ViewConfiguration config = ViewConfiguration.get(context);
        this.mTouchSlop = config.getScaledTouchSlop();
        this.mMinFlingVelocity = config.getScaledMinimumFlingVelocity();

        // Refresh display with current params
        this.refreshDrawableState();
        this.setChecked(this.isChecked());

        if (this.isEnabled()) {
            this.mAlpha = 255;
        } else {
            this.mAlpha = DISABLE_ALPHA;
        }

        final int switchWidth = this.mTrackDrawable.getIntrinsicWidth();
        final int switchHeight = this.mTrackDrawable.getIntrinsicHeight();

        this.mSwitchWidth = switchWidth;
        this.mSwitchHeight = switchHeight;

        this.mThumbOnAnimator = ObjectAnimator.ofFloat(this, "ThumbPosition",
                0, this.getThumbScrollRange());
        this.mThumbOnAnimator.setDuration(this.mThumbAnimateTime);
        this.mThumbOnAnimator
                .setInterpolator(new AccelerateDecelerateInterpolator());
        this.mThumbOnAnimator.addUpdateListener(this);
        this.mThumbOnAnimator.addListener(this);

        this.mThumbOffAnimator = ObjectAnimator.ofFloat(this, "ThumbPosition",
                this.getThumbScrollRange(), 0);
        this.mThumbOffAnimator.setDuration(this.mThumbAnimateTime);
        this.mThumbOffAnimator
                .setInterpolator(new AccelerateDecelerateInterpolator());
        this.mThumbOffAnimator.addUpdateListener(this);
        this.mThumbOffAnimator.addListener(this);

        // reset the parent's useless parameters.
        super.setThumbDrawable(new ColorDrawable(Color.TRANSPARENT));
        super.setThumbTextPadding(0);
    }

    @Override
    public void setSwitchTextAppearance(Context context, int resid) {
    }

    @Override
    public void setSwitchTypeface(Typeface tf, int style) {
    }

    @Override
    public void setSwitchTypeface(Typeface tf) {
    }

    @Override
    public void setThumbTextPadding(int pixels) {
    }

    @Override
    public void setTextOn(CharSequence textOn) {
    }

    @Override
    public void setTextOff(CharSequence textOff) {
    }

    public void setThumbPosition(float position) {
        final int thumbScrollRange = this.getThumbScrollRange();
        if (this.isTheLayoutRtl()) {
            this.mThumbPosition = thumbScrollRange - position;
        } else {
            this.mThumbPosition = position;
        }
    }

    @Override
    public void setTrackDrawable(Drawable track) {
        this.mTrackDrawable = track;
        this.requestLayout();
    }

    public void setTrackColor(int color) {
        if (this.mTrackColor != color) {
            this.mTrackColor = color;
        }
    }

    @Override
    public void setTrackResource(int resId) {
        this.setTrackDrawable(this.getContext().getResources()
                .getDrawable(resId));
    }

    @Override
    public Drawable getTrackDrawable() {
        return this.mTrackDrawable;
    }

    @Override
    public void setThumbDrawable(Drawable thumb) {
        this.mThumbDrawable = thumb;
        this.requestLayout();
    }

    @Override
    public void setThumbResource(int resId) {
        this.setThumbDrawable(this.getContext().getResources()
                .getDrawable(resId));
    }

    @Override
    public Drawable getThumbDrawable() {
        return this.mThumbDrawable;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int switchWidth = this.mTrackDrawable.getIntrinsicWidth();
        final int switchHeight = this.mTrackDrawable.getIntrinsicHeight();

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int measuredHeight = this.getMeasuredHeight();
        if (measuredHeight < switchHeight) {
            this.setMeasuredDimension(this.getMeasuredWidthAndState(),
                    switchHeight);
        }

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        this.setThumbPosition(this.isChecked());

        int switchRight;
        int switchLeft;

        if (this.isTheLayoutRtl()) {
            switchLeft = this.getPaddingLeft();
            switchRight = switchLeft + this.mSwitchWidth;
        } else {
            switchRight = this.getWidth() - this.getPaddingRight();
            switchLeft = switchRight - this.mSwitchWidth;
        }

        int switchTop = 0;
        int switchBottom = 0;
        switch (this.getGravity() & Gravity.VERTICAL_GRAVITY_MASK) {
        default:
        case Gravity.TOP:
            switchTop = this.getPaddingTop();
            switchBottom = switchTop + this.mSwitchHeight;
            break;

        case Gravity.CENTER_VERTICAL:
            switchTop = (this.getPaddingTop() + this.getHeight() - this
                    .getPaddingBottom()) / 2 - this.mSwitchHeight / 2;
            switchBottom = switchTop + this.mSwitchHeight;
            break;

        case Gravity.BOTTOM:
            switchBottom = this.getHeight() - this.getPaddingBottom();
            switchTop = switchBottom - this.mSwitchHeight;
            break;
        }

        this.mSwitchLeft = switchLeft;
        this.mSwitchTop = switchTop;
        this.mSwitchBottom = switchBottom;
        this.mSwitchRight = switchRight;

        this.mSwitchPivotX = (switchLeft + switchRight) / 2;
        this.mSwitchPivotY = (switchTop + switchBottom) / 2;

        this.mSaveLayerRectF.left = switchLeft;
        this.mSaveLayerRectF.top = switchTop;
        this.mSaveLayerRectF.right = switchRight;
        this.mSaveLayerRectF.bottom = switchBottom;
    }

    private boolean hitThumb(float x, float y) {
        this.mThumbDrawable.getPadding(this.mTempRect);
        final int thumbTop = this.mSwitchTop - this.mTouchSlop;
        final int thumbLeft = this.mSwitchLeft
                + (int) (this.mThumbPosition + 0.5f) - this.mTouchSlop;
        final int thumbRight = thumbLeft + this.mThumbWidth
                + this.mTempRect.left + this.mTempRect.right + this.mTouchSlop;
        final int thumbBottom = this.mSwitchBottom + this.mTouchSlop;
        return x > thumbLeft && x < thumbRight && y > thumbTop
                && y < thumbBottom;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        this.mVelocityTracker.addMovement(ev);
        final int action = ev.getActionMasked();
        switch (action) {
        case MotionEvent.ACTION_DOWN: {
            final float x = ev.getX();
            final float y = ev.getY();
            if (this.isEnabled() && this.hitThumb(x, y)) {
                this.mTouchMode = TOUCH_MODE_DOWN;
                this.mTouchX = x;
                this.mTouchY = y;
            }
            break;
        }

        case MotionEvent.ACTION_MOVE: {
            switch (this.mTouchMode) {
            case TOUCH_MODE_IDLE:
                // Didn't target the thumb, treat normally.
                break;
            case TOUCH_MODE_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                if (Math.abs(x - this.mTouchX) > this.mTouchSlop
                        || Math.abs(y - this.mTouchY) > this.mTouchSlop) {
                    this.mTouchMode = TOUCH_MODE_DRAGGING;
                    this.getParent().requestDisallowInterceptTouchEvent(true);
                    this.mTouchX = x;
                    this.mTouchY = y;
                    return true;
                }
                break;
            }
            case TOUCH_MODE_DRAGGING: {
                final float x = ev.getX();
                final float dx = x - this.mTouchX;
                float newPos = Math.max(
                        0,
                        Math.min(this.mThumbPosition + dx,
                                this.getThumbScrollRange()));
                if (newPos != this.mThumbPosition) {
                    this.mThumbPosition = newPos;
                    this.mTouchX = x;
                    this.invalidate();
                }
                return true;
            }
            }
            break;
        }

        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL: {
            if (this.mTouchMode == TOUCH_MODE_DRAGGING) {
                this.stopDrag(ev);
                return true;
            }
            this.mTouchMode = TOUCH_MODE_IDLE;
            this.mVelocityTracker.clear();
            break;
        }
        }

        return super.onTouchEvent(ev);
    }

    private void cancelSuperTouch(MotionEvent ev) {
        MotionEvent cancel = MotionEvent.obtain(ev);
        cancel.setAction(MotionEvent.ACTION_CANCEL);
        super.onTouchEvent(cancel);
        cancel.recycle();
    }

    private void stopDrag(MotionEvent ev) {
        this.mTouchMode = TOUCH_MODE_IDLE;
        // Up and not canceled, also checks the switch has not been disabled
        // during the drag
        boolean commitChange = ev.getAction() == MotionEvent.ACTION_UP
                && this.isEnabled();

        this.cancelSuperTouch(ev);

        if (commitChange) {
            boolean newState;
            this.mVelocityTracker.computeCurrentVelocity(1000);
            float xvel = this.mVelocityTracker.getXVelocity();
            if (Math.abs(xvel) > this.mMinFlingVelocity) {
                newState = this.isTheLayoutRtl() ? (xvel < 0) : (xvel > 0);
            } else {
                newState = this.getTargetCheckedState();
            }
            this.animateThumbToCheckedState(newState);
        } else {
            this.animateThumbToCheckedState(this.isChecked());
        }
    }

    private void animateThumbToCheckedState(boolean newCheckedState) {
        // TODO animate!
        // float targetPos = newCheckedState ? 0 : getThumbScrollRange();
        // mThumbPosition = targetPos;
        super.setChecked(newCheckedState);
        this.setThumbPosition(this.isChecked());
        this.invalidate();
    }

    private boolean getTargetCheckedState() {
        if (this.isTheLayoutRtl()) {
            return this.mThumbPosition <= this.getThumbScrollRange() / 2;
        } else {
            return this.mThumbPosition >= this.getThumbScrollRange() / 2;
        }
    }

    private void setThumbPosition(boolean checked) {
        if (this.isTheLayoutRtl()) {
            this.mThumbPosition = checked ? 0 : this.getThumbScrollRange();
        } else {
            this.mThumbPosition = checked ? this.getThumbScrollRange() : 0;
        }
    }

    @Override
    public void toggle() {
        final boolean checked = this.isChecked();
        this.setChecked(!checked, true);
    }

    @Override
    public void setChecked(boolean checked) {
        this.setChecked(checked, false);
    }

    @Override
    public void setChecked(boolean checked, boolean playAnimation) {
        final boolean oldChecked = this.isChecked();

        if (oldChecked == checked) {
            return;
        }

        if (this.mCurrentAnimator != null) {
            this.mCurrentAnimator.cancel();
            this.mCurrentAnimator = null;
        }

        if (checked && this.mThumbOnAnimator != null) {
            this.mThumbOnAnimator.start();
            this.mCurrentAnimator = this.mThumbOnAnimator;
        } else if (!checked && this.mThumbOffAnimator != null) {
            this.mThumbOffAnimator.start();
            this.mCurrentAnimator = this.mThumbOffAnimator;
        }
        super.setChecked(checked);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            this.mAlpha = 255;
        } else {
            this.mAlpha = DISABLE_ALPHA;
        }
    }

    private final int[] location = new int[2];

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        this.getLocationInWindow(this.location);
        // Draw the switch
        int switchLeft = this.mSwitchLeft;
        int switchTop = this.mSwitchTop;
        int switchRight = this.mSwitchRight;
        int switchBottom = this.mSwitchBottom;
        int switchPivotX = this.mSwitchPivotX;
        int switchPivotY = this.mSwitchPivotY;

        if (!this.isEnabled()) {
            canvas.saveLayerAlpha(this.mSaveLayerRectF, this.mAlpha,
                    Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
                            | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
                            | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
                            | Canvas.CLIP_TO_LAYER_SAVE_FLAG);
        } else {
            canvas.save();
        }

        final Paint paint = this.mPaint;
        final int originColor = paint.getColor();
        paint.setColor(this.mTrackColor);
        final int trackRadius = this.mTrackRadius;
        this.mTempRectF.left = switchLeft + 1;
        this.mTempRectF.top = switchTop + 1;
        this.mTempRectF.right = switchRight - 1;
        this.mTempRectF.bottom = switchBottom - 1;

        canvas.drawRoundRect(this.mTempRectF, trackRadius, trackRadius, paint);

        final int thumbScrollRange = this.getThumbScrollRange();
        final int offset;
        if (this.isTheLayoutRtl()) {
            offset = -(int) ((thumbScrollRange - this.mThumbPosition) * 0.2 + 0.5f);
        } else {
            offset = (int) (this.mThumbPosition * 0.2 + 0.5f);
        }

        int trackPivotX = switchPivotX + offset;
        final int trackPivotY = switchPivotY;

        final double scaleRate = (1 - Math.pow(
                (this.isTheLayoutRtl() ? thumbScrollRange - this.mThumbPosition
                        : this.mThumbPosition) / thumbScrollRange, 2));
        int switchOffWidth = (int) ((switchRight - switchLeft) * scaleRate);
        int switchOffHeight = (int) ((switchBottom - switchTop) * scaleRate);
        int switchOffLeft = trackPivotX - switchOffWidth / 2;
        int switchOffRight = switchOffLeft + switchOffWidth;
        int switchOffTop = trackPivotY - switchOffHeight / 2;
        int switchOffBottom = switchOffTop + switchOffHeight;

        this.mTrackDrawable.setBounds(switchOffLeft, switchOffTop,
                switchOffRight, switchOffBottom);
        this.mTrackDrawable.draw(canvas);

        this.mTrackDrawable.getPadding(this.mTempRect);
        int switchInnerLeft = switchLeft + this.mTempRect.left;
        int switchInnerRight = switchRight - this.mTempRect.right;
        this.mThumbDrawable.getPadding(this.mTempRect);
        final int thumbPos = (int) (this.mThumbPosition + 0.5f);
        int thumbLeft = switchInnerLeft - this.mTempRect.left + thumbPos;
        int thumbRight = switchInnerLeft + thumbPos + this.mThumbWidth
                + this.mTempRect.right;

        canvas.clipRect(switchInnerLeft, switchTop, switchInnerRight,
                switchBottom);

        this.mThumbDrawable.setBounds(thumbLeft, switchTop, thumbRight,
                switchBottom);
        this.mThumbDrawable.draw(canvas);

        paint.setColor(originColor);
        canvas.restore();
    }

    @Override
    public int getCompoundPaddingLeft() {
        if (!this.isTheLayoutRtl()) {
            return super.getCompoundPaddingLeft();
        }
        int padding = super.getCompoundPaddingLeft() + this.mSwitchWidth;

        return padding;
    }

    private boolean isTheLayoutRtl() {
        return (this.getLayoutDirection() == LAYOUT_DIRECTION_RTL);
    }

    @Override
    public int getCompoundPaddingRight() {
        if (this.isTheLayoutRtl()) {
            return super.getCompoundPaddingRight();
        }
        int padding = super.getCompoundPaddingRight() + this.mSwitchWidth;

        return padding;
    }

    private int getThumbScrollRange() {
        if (this.mTrackDrawable == null) {
            return 0;
        }
        this.mTrackDrawable.getPadding(this.mTempRect);
        return this.mSwitchWidth - this.mThumbWidth - this.mTempRect.left
                - this.mTempRect.right;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        int[] myDrawableState = this.getDrawableState();

        // Set the state of the Drawable
        // Drawable may be null when checked state is set from XML, from super
        // constructor
        if (this.mThumbDrawable != null)
            this.mThumbDrawable.setState(myDrawableState);
        if (this.mTrackDrawable != null)
            this.mTrackDrawable.setState(myDrawableState);

        this.invalidate();
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == this.mThumbDrawable
                || who == this.mTrackDrawable;
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        this.mThumbDrawable.jumpToCurrentState();
        this.mTrackDrawable.jumpToCurrentState();

        if (this.mCurrentAnimator != null) {
            this.mCurrentAnimator.cancel();
            this.mCurrentAnimator = null;
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(LeSwitch.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(LeSwitch.class.getName());
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        this.invalidate();
    }

    @Override
    public void onAnimationStart(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (animation == this.mThumbOnAnimator) {
            this.setThumbPosition(true);
        } else if (animation == this.mThumbOffAnimator) {
            this.setThumbPosition(false);
        }
        this.invalidate();

        this.mCurrentAnimator = null;
    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

}
