package com.letv.shared.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.CheckBox;
import android.widget.TextView;

import com.letv.shared.R;

/**
 * Created by dongshangyong on 14-8-4.
 */
public class LeCheckBox extends CheckBox implements LeCheckable,
        ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

    private static final int DISABLE_ALPHA = (int) (255 * 0.3);
    private int mBoxBorderColor;
    private int mBoxTrackColor;
    private final int mMeasureSize;
    private AnimatorSet mZoomOutAnimator;
    private AnimatorSet mZoomInAnimator;
    private ObjectAnimator mArrowShownAnimator;
    private ObjectAnimator mArrowHiddenAnimator;
    private Animator mCurrentAnimatior;
    private final Animator mShowAnimatior;
    private final Animator mHiddenAnimatior;
    private final int mMaxCircleRadius;
    private int mBoxTrackColorOn;
    private int mArrowColor;
    private int mArrowColorWithoutBorder;
    private boolean mIsBoxTextOnRight;
    private boolean mWithoutBoxBorder;
    private int mBoxSize;

    private int mDynimacRadius = 0;
    private float mArrowInterpolatedTime = 0;
    private final Rect mInvalidateRect = new Rect();
    private final Path mCirclePath = new Path();
    private final int mCircleBoxRadius;
    private ArgbEvaluator mArgbEvaluator;
    private TextView mAnimateTextView;
    private ColorStateList mTextColorOnChecked;
    private int mAnimateTextColorOnChecked;
    private int mAnimateTextColorOrigin;
    private int mAnimateTextViewColor;
    private int mBoxTop;
    private int mBoxBottom;
    private int mBoxLeft;
    private int mBoxRight;
    private int mBoxInnerPadding;
    private final LeArrowShape mArrawShape;

    private int mAlpha = 255;
    RectF mSaveLayerRectF = new RectF();

    public LeCheckBox(Context context) {
        this(context, null);
    }

    public LeCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.checkedTextViewStyle);
    }

    @SuppressLint("NewApi")
    public LeCheckBox(Context context, AttributeSet attrs, int defStyle) {
        // API Level > 15, 构造函数由4参数改为使用3参数
        super(context, attrs, defStyle);

        final Resources res = context.getResources();

        this.mIsBoxTextOnRight = true;
        this.mWithoutBoxBorder = false;
        this.mBoxTrackColorOn = res
                .getColor(R.color.le_color_default_checkbox_track_on);
        this.mBoxBorderColor = res
                .getColor(R.color.le_color_default_checkbox_track_border);
        this.mBoxTrackColor = res
                .getColor(R.color.le_color_default_checkbox_track);
        this.mArrowColor = res
                .getColor(R.color.le_color_default_checkbox_arrow);
        this.mArrowColorWithoutBorder = res
                .getColor(R.color.le_color_default_checkbox_track_on);
        this.mBoxSize = res
                .getDimensionPixelSize(R.dimen.le_default_box_size_with_border);

        // read the theme color and set the default color
        TypedValue outValue = new TypedValue();
        final Resources.Theme theme = context.getTheme();
        //if (theme.resolveAttribute(android.R.attr.colorControlActivated,
              //  outValue, true)) {
            this.mBoxTrackColorOn = outValue.data;
            this.mArrowColorWithoutBorder = outValue.data;
       // }

        // if(theme.resolveAttribute(R.attr.colorControlNormal, outValue, true))
        // {
        // mBoxTrackColor = outValue.data;
        // }

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.leCheckbox, defStyle, 0);
        if (a != null) {
            int n = a.getIndexCount();

            this.mWithoutBoxBorder = a.getBoolean(
                    R.styleable.leCheckbox_leBoxWithoutBorder,
                    this.mWithoutBoxBorder);
            if (this.mWithoutBoxBorder) {
                this.mBoxSize = res
                        .getDimensionPixelSize(R.dimen.le_default_box_size);
            }
            this.mBoxInnerPadding = a.getDimensionPixelSize(
                    R.styleable.leCheckbox_leBoxInnerPadding,
                    this.mBoxInnerPadding);
            this.mTextColorOnChecked = a
                    .getColorStateList(R.styleable.leCheckbox_leTextOnColor);
            this.mBoxTrackColorOn = a.getColor(
                    R.styleable.leCheckbox_leBoxOnColor, this.mBoxTrackColorOn);
            this.mArrowColor = a.getColor(
                    R.styleable.leCheckbox_leBoxArrowColor, this.mArrowColor);
            this.mArrowColorWithoutBorder = a.getColor(
                    R.styleable.leCheckbox_leBoxArrowColorWithoutBorder,
                    this.mArrowColorWithoutBorder);
            this.mBoxSize = a.getDimensionPixelSize(
                    R.styleable.leCheckbox_leBoxSize, this.mBoxSize);
            this.mIsBoxTextOnRight = a.getBoolean(
                    R.styleable.leCheckbox_leBoxIsTextOnRight,
                    this.mIsBoxTextOnRight);
            this.mBoxTrackColor = a
                    .getColor(R.styleable.leCheckbox_leBoxTrackColor,
                            this.mBoxTrackColor);
            this.mBoxBorderColor = a.getColor(
                    R.styleable.leCheckbox_leBoxBorderColor,
                    this.mBoxBorderColor);

            /*
             * for (int i = 0; i < n; i ++) {
             * int attr = a.getIndex(i);
             * switch (attr) {
             * case R.styleable.leCheckbox_leBoxWithoutBorder:
             * mWithoutBoxBorder = a.getBoolean(attr, mWithoutBoxBorder);
             * if (mWithoutBoxBorder) {
             * mBoxSize =
             * res.getDimensionPixelSize(R.dimen.le_default_box_size);
             * }
             * break;
             * case R.styleable.leCheckbox_leBoxInnerPadding:
             * mBoxInnerPadding = a.getDimensionPixelSize(attr,
             * mBoxInnerPadding);
             * break;
             * case R.styleable.leCheckbox_leTextOnColor:
             * mTextColorOnChecked = a.getColorStateList(attr);
             * break;
             * case R.styleable.leCheckbox_leBoxOnColor:
             * mBoxTrackColorOn = a.getColor(attr, mBoxTrackColorOn);
             * break;
             * case R.styleable.leCheckbox_leBoxArrowColor:
             * mArrowColor = a.getColor(attr, mArrowColor);
             * break;
             * case R.styleable.leCheckbox_leBoxArrowColorWithoutBorder:
             * mArrowColorWithoutBorder = a.getColor(attr,
             * mArrowColorWithoutBorder);
             * break;
             * case R.styleable.leCheckbox_leBoxSize:
             * mBoxSize = a.getDimensionPixelSize(attr, mBoxSize);
             * break;
             * case R.styleable.leCheckbox_leBoxIsTextOnRight:
             * mIsBoxTextOnRight = a.getBoolean(attr, mIsBoxTextOnRight);
             * break;
             * case R.styleable.leCheckbox_leBoxTrackColor:
             * mBoxTrackColor = a.getColor(attr, mBoxTrackColor);
             * break;
             * case R.styleable.leCheckbox_leBoxBorderColor:
             * mBoxBorderColor = a.getColor(attr, mBoxBorderColor);
             * break;
             * }
             * }
             */
        }
        a.recycle();

        boolean clickable = true;
        a = context
                .obtainStyledAttributes(attrs, R.styleable.View, defStyle, 0);
        // clickable = a.getBoolean(android.R.styleable.View_clickable,
        // clickable);
        a.recycle();

        this.setClickable(clickable);

        this.mMaxCircleRadius = this.mBoxSize / 2;
        if (this.mWithoutBoxBorder) {
            final int arrowDuration = 300;
            this.mMeasureSize = this.mBoxSize;

            this.mArrowShownAnimator = ObjectAnimator.ofFloat(this,
                    "ArrowInterpolatedTime", 0, 1);
            this.mArrowShownAnimator
                    .setInterpolator(new AccelerateDecelerateInterpolator());
            this.mArrowShownAnimator.setDuration(arrowDuration);
            this.mArrowShownAnimator.addListener(this);
            this.mArrowShownAnimator.addUpdateListener(this);

            this.mArrowHiddenAnimator = ObjectAnimator.ofFloat(this,
                    "ArrowInterpolatedTime", 1, 0);
            this.mArrowHiddenAnimator
                    .setInterpolator(new AccelerateDecelerateInterpolator());
            this.mArrowHiddenAnimator.setDuration(arrowDuration);
            this.mArrowHiddenAnimator.addListener(this);
            this.mArrowHiddenAnimator.addUpdateListener(this);
        } else {
            final float enLargeRate = 1.2f;
            final int arrowDuration = 100;
            this.mMeasureSize = (int) (this.mBoxSize * enLargeRate);

            this.mZoomOutAnimator = new AnimatorSet();
            ObjectAnimator animatorOutEnlarge = ObjectAnimator.ofInt(this,
                    "DynimacRadius", 0, this.mMaxCircleRadius);
            ObjectAnimator animatorArrowShown = ObjectAnimator.ofFloat(this,
                    "ArrowInterpolatedTime", 0, 1);

            animatorOutEnlarge.setInterpolator(new OvershootInterpolator());
            animatorArrowShown
                    .setInterpolator(new AccelerateDecelerateInterpolator());

            animatorOutEnlarge.setDuration(200);
            animatorArrowShown.setDuration(arrowDuration);

            animatorOutEnlarge.addUpdateListener(this);
            animatorArrowShown.addUpdateListener(this);

            this.mZoomOutAnimator.play(animatorOutEnlarge).before(
                    animatorArrowShown);
            this.mZoomOutAnimator.addListener(this);

            this.mZoomInAnimator = new AnimatorSet();
            ObjectAnimator animatorArrowHidden = ObjectAnimator.ofFloat(this,
                    "ArrowInterpolatedTime", 1, 0);
            ObjectAnimator animatorInNarrow = ObjectAnimator.ofInt(this,
                    "DynimacRadius", this.mMaxCircleRadius, 0);
            animatorArrowHidden
                    .setInterpolator(new AccelerateDecelerateInterpolator());
            animatorInNarrow
                    .setInterpolator(new AnticipateOvershootInterpolator());

            animatorArrowHidden.setDuration(arrowDuration);
            animatorInNarrow.setDuration(200);

            animatorArrowHidden.addUpdateListener(this);
            animatorInNarrow.addUpdateListener(this);

            this.mZoomInAnimator.play(animatorArrowHidden).before(
                    animatorInNarrow);
            this.mZoomInAnimator.addListener(this);
        }
        this.setMinHeight(this.mMeasureSize);

        if (this.mWithoutBoxBorder) {
            this.mShowAnimatior = this.mArrowShownAnimator;
            this.mHiddenAnimatior = this.mArrowHiddenAnimator;
        } else {
            this.mShowAnimatior = this.mZoomOutAnimator;
            this.mHiddenAnimatior = this.mZoomInAnimator;
        }
        final boolean checked = this.isChecked();
        this.mDynimacRadius = checked ? this.mMaxCircleRadius : 0;
        this.mArrowInterpolatedTime = checked ? 1 : 0;

        this.mCircleBoxRadius = this.mBoxSize / 2;

        this.mArrawShape = new LeArrowShape(this.mBoxSize,
                this.mWithoutBoxBorder, this.mWithoutBoxBorder);

        if (this.isEnabled()) {
            this.mAlpha = 255;
        } else {
            this.mAlpha = DISABLE_ALPHA;
        }

        if (this.mTextColorOnChecked != null) {
            this.attachAnimateToTextViewColor(this,
                    this.mTextColorOnChecked.getDefaultColor());
        }
    }

    public void setTrackBoxColor(int colorOn, int colorOff) {
        if (this.mBoxTrackColor != colorOff) {
            this.mBoxTrackColor = colorOff;
        }

        if (this.mBoxTrackColorOn != colorOn) {
            this.mBoxTrackColorOn = colorOn;
        }
    }

    public void setBoxBorderColor(int color) {
        if (this.mBoxBorderColor != color) {
            this.mBoxBorderColor = color;
        }
    }

    public void setArrowColor(int color) {
        if (this.mArrowColor != color) {
            this.mArrowColor = color;
        }
    }

    public void setArrowColorWithoutBorder(int color) {
        if (this.mArrowColorWithoutBorder != color) {
            this.mArrowColorWithoutBorder = color;
        }
    }

    public void attachAnimateToTextViewColor(TextView textView, int colorOnCheck) {
        if (this.mArgbEvaluator == null) {
            this.mArgbEvaluator = new ArgbEvaluator();
        }

        if (this.mTextColorOnChecked == null) {
            this.mTextColorOnChecked = ColorStateList.valueOf(colorOnCheck);
        }
        this.mAnimateTextView = textView;
        this.mAnimateTextColorOrigin = textView.getCurrentTextColor();
        this.mAnimateTextColorOnChecked = colorOnCheck;
    }

    public void setDynimacRadius(int radius) {
        this.mDynimacRadius = radius;
    }

    public void setArrowInterpolatedTime(float interpolatedTime) {
        this.mArrowInterpolatedTime = interpolatedTime;

        if (this.mAnimateTextView != null) {
            this.mAnimateTextViewColor = (Integer) this.mArgbEvaluator
                    .evaluate(interpolatedTime, this.mAnimateTextColorOrigin,
                            this.mAnimateTextColorOnChecked);
        }
    }

    private boolean isBoxOnRight() {
        return !this.mIsBoxTextOnRight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (this.mMeasureSize > height) {
            height = this.mMeasureSize;
            heightMode = MeasureSpec.EXACTLY;
        }
        super.onMeasure(widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(height, heightMode));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        final int verticalGravity = this.getGravity()
                & Gravity.VERTICAL_GRAVITY_MASK;
        final int padidngLeft = super.getCompoundPaddingLeft();
        final int paddingRight = super.getCompoundPaddingRight();

        final int mMeasureBorderSize = (this.mMeasureSize - this.mBoxSize) / 2;

        int boxTop = mMeasureBorderSize + super.getCompoundPaddingTop();
        switch (verticalGravity) {
        case Gravity.BOTTOM:
            boxTop = this.getHeight() - this.mBoxSize - mMeasureBorderSize
                    - super.getCompoundPaddingBottom();
            break;
        case Gravity.CENTER_VERTICAL:
            boxTop = (this.getHeight() - this.mBoxSize) / 2;
            break;
        }

        int boxBottom = boxTop + this.mBoxSize;
        int boxLeft = this.isBoxOnRight() ? this.getWidth() - paddingRight
                - this.mBoxSize - mMeasureBorderSize : mMeasureBorderSize
                + padidngLeft;
        int boxRight = this.isBoxOnRight() ? this.getWidth() - paddingRight
                - mMeasureBorderSize : this.mBoxSize + mMeasureBorderSize
                + paddingRight;

        this.mInvalidateRect.left = boxLeft - mMeasureBorderSize;
        this.mInvalidateRect.right = boxRight + mMeasureBorderSize;
        this.mInvalidateRect.top = boxTop - mMeasureBorderSize;
        this.mInvalidateRect.bottom = boxBottom + mMeasureBorderSize;

        this.mBoxTop = boxTop;
        this.mBoxBottom = boxBottom;
        this.mBoxLeft = boxLeft;
        this.mBoxRight = boxRight;

        this.mSaveLayerRectF.left = boxLeft;
        this.mSaveLayerRectF.top = boxTop;
        this.mSaveLayerRectF.right = boxRight;
        this.mSaveLayerRectF.bottom = boxBottom;
    }

    @Override
    public int getCompoundPaddingLeft() {
        int padding = super.getCompoundPaddingLeft();
        if (!this.isBoxOnRight()) {
            padding += this.mMeasureSize + this.mBoxInnerPadding;
        }
        return padding;
    }

    @Override
    public int getCompoundPaddingRight() {
        int padding = super.getCompoundPaddingRight();
        if (this.isBoxOnRight()) {
            padding += this.mMeasureSize + this.mBoxInnerPadding;
        }
        return padding;
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(LeCheckBox.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(LeCheckBox.class.getName());
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

        if (this.mCurrentAnimatior != null) {
            this.mCurrentAnimatior.cancel();
            this.mCurrentAnimatior = null;
        }

        if (checked && this.mShowAnimatior != null) {
            this.mShowAnimatior.start();
            this.mCurrentAnimatior = this.mShowAnimatior;
        } else if (!checked && this.mHiddenAnimatior != null) {
            this.mHiddenAnimatior.start();
            this.mCurrentAnimatior = this.mHiddenAnimatior;
        }
        super.setChecked(checked);
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();

        if (this.mCurrentAnimatior != null) {
            this.mCurrentAnimatior.cancel();
            this.mCurrentAnimatior = null;
        }
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (this.mShowAnimatior != null && this.mHiddenAnimatior != null) {
            final boolean checked = this.isChecked();

            final int left = this.mBoxLeft;
            final int top = this.mBoxTop;
            final int boxRadius = this.mCircleBoxRadius;

            final Paint paint = this.getPaint();
            final boolean isEnabled = this.isEnabled();
            final int originColor = paint.getColor();
            final Paint.Style originStyle = paint.getStyle();

            if (!this.mWithoutBoxBorder) {
                if (!isEnabled) {
                    canvas.saveLayerAlpha(this.mSaveLayerRectF, this.mAlpha,
                            Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
                                    | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
                                    | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
                                    | Canvas.CLIP_TO_LAYER_SAVE_FLAG);
                }
                final int pivotX = left + boxRadius;
                final int pivotY = top + boxRadius;

                paint.setColor(this.mBoxBorderColor);
                canvas.drawCircle(pivotX, pivotY, boxRadius, paint);

                paint.setColor(this.mBoxTrackColor);
                canvas.drawCircle(pivotX, pivotY, boxRadius - 1, paint);

                paint.setColor(this.mBoxTrackColorOn);
                canvas.drawCircle(pivotX, pivotY, this.mDynimacRadius, paint);

                if (!isEnabled) {
                    canvas.restore();
                }
            }

            if (!isEnabled && this.mWithoutBoxBorder) {
                canvas.saveLayerAlpha(this.mSaveLayerRectF, this.mAlpha,
                        Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
                                | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
                                | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
                                | Canvas.CLIP_TO_LAYER_SAVE_FLAG);
            } else {
                canvas.save();
            }
            canvas.translate(left, top);
            if (!this.mWithoutBoxBorder) {
                this.mCirclePath.reset();
                this.mCirclePath.addCircle(boxRadius, boxRadius, boxRadius,
                        Path.Direction.CW);
                canvas.clipPath(this.mCirclePath);
                paint.setColor(this.mArrowColor);
            } else {
                paint.setColor(this.mArrowColorWithoutBorder);
            }

            paint.setStyle(Paint.Style.FILL);

            this.mArrawShape.setIsShowUp(checked);
            this.mArrawShape.draw(canvas, paint, this.mArrowInterpolatedTime);

            canvas.restore();

            paint.setStyle(originStyle);
            paint.setColor(originColor);
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        if (this.mWithoutBoxBorder && this.mAnimateTextView != null) {
            this.mAnimateTextView.setTextColor(this.mAnimateTextViewColor);
            if (this.mAnimateTextView == this) {
                return;
            }
        }
        this.invalidate(this.mInvalidateRect);
    }

    @Override
    public void onAnimationStart(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {

        if (animation == this.mShowAnimatior) {
            this.mArrowInterpolatedTime = 1;
            this.mDynimacRadius = this.mMaxCircleRadius;
        } else if (animation == this.mHiddenAnimatior) {
            this.mArrowInterpolatedTime = 0;
            this.mDynimacRadius = 0;
        }
        this.mCurrentAnimatior = null;
        if (this.mAnimateTextView != null && this.mTextColorOnChecked != null) {
            this.mAnimateTextView
                    .setTextColor(this.isChecked() ? this.mAnimateTextColorOnChecked
                            : this.mAnimateTextColorOrigin);
        }
        this.invalidate(this.mInvalidateRect);
    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }
}
