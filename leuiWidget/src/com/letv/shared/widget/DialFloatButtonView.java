package com.letv.shared.widget;

import android.animation.*;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import com.letv.shared.R;

public class DialFloatButtonView extends ImageView implements ObjectAnimator.AnimatorUpdateListener {

    private AnimatorSet mDialAnim;

    private int ANIM_DURATION;

    private Context mContext;

    private TimeInterpolator mRotateInterpolator = new AccelerateDecelerateInterpolator();
    private static final String ROTATION = "rotation";
    private float START_RORATION;
    private float END_RORATION;

    private OvalShape mCircle;
    private int mSize;
    private TimeInterpolator mCircleColorInterpolator = new AccelerateDecelerateInterpolator();
    private int mCircleColor;
    private static final String CIRCLE_COLOR = "circleColor";
    private int START_COLOR;
    private int END_COLOR;

    private Paint mCirclePaint;

    public DialFloatButtonView(Context context) {
        this(context, null);
    }
    public DialFloatButtonView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }
    public DialFloatButtonView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        Resources res = context.getResources();

        ANIM_DURATION = res.getInteger(R.integer.le_dial_float_button_view_anim_duration);

        START_COLOR = res.getColor(R.color.le_dial_float_button_view_start_color);
        END_COLOR = res.getColor(R.color.le_dial_float_button_view_end_color);

        TypedValue out = new TypedValue();
        res.getValue(R.dimen.le_dial_float_button_view_start_rotation, out, true);
        START_RORATION = out.getFloat();
        res.getValue(R.dimen.le_dial_float_button_view_end_rotation, out, true);
        END_RORATION = out.getFloat();

        mSize = context.getResources().getDimensionPixelSize(R.dimen.le_dial_float_button_view_circle_size);

        // init circle paint
        mCirclePaint = new Paint();
        mCirclePaint.setColor(START_COLOR);
        mCirclePaint.setAntiAlias(true);

        mCircle = new OvalShape();
        mCircle.resize(mSize, mSize);
        setScaleType(ScaleType.CENTER);
    }

    public static View createDefaultFloatingView(Context context, int floatIconId) {
        DialFloatButtonView dialFloatButtonView = new DialFloatButtonView(context);
        dialFloatButtonView.setImageDrawable(context.getResources().getDrawable(floatIconId));
        return dialFloatButtonView;
    }

    public DialFloatButtonView setSize(int dp) {
        mSize = dip2px(mContext, dp);
        return this;
    }

    public DialFloatButtonView setRotateInterpolator(TimeInterpolator timeInterpolator) {
        mRotateInterpolator = timeInterpolator;
        return this;
    }

    public DialFloatButtonView setCircleColorInterpolator(TimeInterpolator timeInterpolator) {
        mCircleColorInterpolator = timeInterpolator;
        return this;
    }

    public void startAnimation() {
        startAnim(null);
    }

    public void startAnim(AnimatorListenerAdapter animListener) {
        cancelAnimation();
        createAnimation();
        if(null != animListener) {
            mDialAnim.addListener(animListener);
        }
        mDialAnim.start();
    }

    public void cancelAnimation() {
        if(null != mDialAnim && mDialAnim.isRunning()) {
            mDialAnim.cancel();
        }

        setRotation(START_RORATION);
        setCircleColor(START_COLOR);

        invalidate();
    }

    private void createAnimation() {
        if(null == mDialAnim) {
            // back color
            ObjectAnimator backColorAnim = ObjectAnimator.ofInt(this, CIRCLE_COLOR, START_COLOR, END_COLOR);
            backColorAnim.setEvaluator(new ArgbEvaluator());
            backColorAnim.setInterpolator(mCircleColorInterpolator);
            backColorAnim.addUpdateListener(this);

            // rotate
            ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(this, ROTATION, START_RORATION, END_RORATION);
            rotateAnim.setInterpolator(mRotateInterpolator);

            mDialAnim = new AnimatorSet();
            mDialAnim.setDuration(ANIM_DURATION);
            mDialAnim.playTogether(backColorAnim, rotateAnim);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(measure(widthMeasureSpec), measure(heightMeasureSpec));
    }

    private int measure(int measureSpec) {
        return MeasureSpec.makeMeasureSpec(mSize, MeasureSpec.EXACTLY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        mCircle.draw(canvas, mCirclePaint);
        super.onDraw(canvas);
    }

    public int getCircleColor() {
        return mCircleColor;
    }
    public void setCircleColor(int circleColor) {
        this.mCircleColor = circleColor;
        mCirclePaint.setColor(mCircleColor);
    }

    public static int dip2px(Context context, float dpValue) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics()));
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        invalidate();
    }

}
