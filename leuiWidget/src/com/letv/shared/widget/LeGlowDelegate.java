package com.letv.shared.widget;

import android.animation.*;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

public class LeGlowDelegate implements ValueAnimator.AnimatorUpdateListener  {
    private final String TAG = ((Object) this).getClass().getSimpleName();

    private View mView;
    private boolean mEnabled = true;

    private AnimatorSet pressAnim;
    private AnimatorSet releaseAnim;

    private static final int PRESS = 1;
    private static final int RELEASE = 2;

    private static final String BACK_ROUND_RECT_SCALE = "backRoundRectScale";
    private static final String BACK_ROUND_RECT_ALPHA = "backRoundRectAlpha";
    private int DEFAULT_BACK_ROUND_RECT_WIDTH;
    private float backRoundRectScale;
    private int backRoundRectAlpha;
    private Paint backRoundRectPaint;
    private int backRoundRectColor = Color.BLACK;
    private float backRoundRectWidth;

    private int centerX;
    private int centerY;
    private RectF rectF;

    // press
    private static final int PRESS_DURATION = 175;
    private float backRoundRectPressScaleStart = 1.0f;
    public float backRoundRectPressScaleEnd = 1.8f;
    private int backRoundRectPressAlphaStart = 0;
    public int backRoundRectPressAlphaEnd = (int) (255 * 0.1f);
    private ObjectAnimator backRoundRectPressScale;
    private ObjectAnimator backRoundRectPressAlpha;

    // release
    private static final int RELEASE_DURATION = 175;
    private float backRoundRectReleaseScaleStart = backRoundRectPressScaleEnd;
    private float backRoundRectReleaseScaleEnd = backRoundRectPressScaleEnd * 14 / 15;
    private int backRoundRectReleaseAlphaStart = backRoundRectPressAlphaEnd;
    private int backRoundRectReleaseAlphaEnd = backRoundRectPressAlphaStart;
    private ObjectAnimator backRoundRectReleaseScale;
    private ObjectAnimator backRoundRectReleaseAlpha;

    boolean isUp = false;
    boolean isPressAnimEnd = false;

    public LeGlowDelegate(View delegateView) {
        this(null, delegateView);
        
        init();
    }

    public LeGlowDelegate(Context context, View delegateView) {
        mView = delegateView;

        backRoundRectPaint = new Paint();
        backRoundRectPaint.setColor(backRoundRectColor);
        backRoundRectPaint.setAntiAlias(true);
        
        init();
    }

    public void init() {
        // Make sure that animation which is out of mView's bounds will not be clipped
        if (mView.getParent() != null && ((View)mView.getParent()) instanceof ViewGroup) {
            ( (ViewGroup)( mView.getParent()) ).setClipChildren(false);
        }
    }

    public void setAnimationEnabled(boolean enable) {
        mEnabled = enable;

        if (!enable) {
            cancelAnimation();
        }
    }

    public void setBackRoundRectColor(int color) {
        backRoundRectColor = color;
        backRoundRectPaint.setColor(backRoundRectColor);
    }

    public void setBackRoundRectScaleAlpha(int alpha) {
        backRoundRectPressAlphaEnd = alpha;
        backRoundRectReleaseAlphaStart = backRoundRectPressAlphaEnd;
    }

    public void setBackRoundRectScaleMultiple(float multiple) {
        backRoundRectPressScaleEnd = multiple;
        backRoundRectReleaseScaleStart = backRoundRectPressScaleEnd;
        backRoundRectReleaseScaleEnd = backRoundRectPressScaleEnd * 14 / 15;
    }

    public void startAnimation(int state) {
        createAnimation();
        switch (state) {
            case PRESS:
                cancelAnimation();
                pressAnim.start();

                pressAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        isPressAnimEnd = true;
                        startAnimation(RELEASE);
                    }
                });
                break;
            case RELEASE:
                if(isUp && isPressAnimEnd) {
                    isUp = false;
                    isPressAnimEnd = false;
                    releaseAnim.start();
                }
                break;
        }
    }

    private void createAnimation() {
        // backDrawable scale
        if(null == pressAnim) {
            backRoundRectPressScale = ObjectAnimator.ofFloat(this, BACK_ROUND_RECT_SCALE, backRoundRectPressScaleStart, backRoundRectPressScaleEnd);
            backRoundRectPressScale.setInterpolator(new AccelerateInterpolator());
            backRoundRectPressScale.addUpdateListener(this);
            // backDrawable alpha
            backRoundRectPressAlpha = ObjectAnimator.ofInt(this, BACK_ROUND_RECT_ALPHA, backRoundRectPressAlphaStart, backRoundRectPressAlphaEnd);
            backRoundRectPressAlpha.setInterpolator(new AccelerateInterpolator());

            pressAnim = new AnimatorSet();
            pressAnim.playTogether(backRoundRectPressScale, backRoundRectPressAlpha);
            pressAnim.setDuration(PRESS_DURATION);
        }

        // release
        if(null == releaseAnim) {
            backRoundRectReleaseScale = ObjectAnimator.ofFloat(this, BACK_ROUND_RECT_SCALE, backRoundRectReleaseScaleStart, backRoundRectReleaseScaleEnd);
            backRoundRectReleaseScale.setInterpolator(new AccelerateInterpolator());
            backRoundRectReleaseScale.addUpdateListener(this);
            // backDrawable alpha
            backRoundRectReleaseAlpha = ObjectAnimator.ofInt(this, BACK_ROUND_RECT_ALPHA, backRoundRectReleaseAlphaStart, backRoundRectReleaseAlphaEnd);
            backRoundRectReleaseAlpha.setInterpolator(new AccelerateInterpolator());

            releaseAnim = new AnimatorSet();
            releaseAnim.playTogether(backRoundRectReleaseScale, backRoundRectReleaseAlpha);
            releaseAnim.setDuration(RELEASE_DURATION);
        }
    }

    public void cancelAnimation() {
        if(null != pressAnim && pressAnim.isRunning()) {
            pressAnim.cancel();
        }
        if(null != releaseAnim && releaseAnim.isRunning()) {
            releaseAnim.cancel();
        }

        setBackRoundRectAlpha(0);
        setBackRoundRectScale(0.0f);

        invalidate();
    }

    public void setPressed(boolean pressed) {
        if (mEnabled) {
            if (pressed) {
                isPressAnimEnd = false;
                startAnimation(PRESS);
            } else {
                isUp = true;
                startAnimation(RELEASE);
            }
        }
    }

    public void draw(Canvas canvas) {
        if(mEnabled) {
            if(centerX == 0 || centerY == 0 || null == rectF) {
                centerX = mView.getWidth() / 2;
                centerY = mView.getHeight() / 2;
                rectF = new RectF();
            }
            rectF.set(centerX - backRoundRectWidth / 2, 0, centerX + backRoundRectWidth / 2, centerY * 2);
            canvas.drawRoundRect(rectF, centerY, centerY, backRoundRectPaint);
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        invalidate();
    }

    public void invalidate() {
        mView.invalidate();
    }

    public int getBackRoundRectAlpha() {
        return backRoundRectAlpha;
    }
    public void setBackRoundRectAlpha(int backRoundRectAlpha) {
        backRoundRectPaint.setAlpha(backRoundRectAlpha);
        this.backRoundRectAlpha = backRoundRectAlpha;
    }

    public float getBackRoundRectScale() {
        return backRoundRectScale;
    }
    public void setBackRoundRectScale(float backRoundRectScale) {
        if(DEFAULT_BACK_ROUND_RECT_WIDTH == 0) {
            DEFAULT_BACK_ROUND_RECT_WIDTH = Math.min(mView.getWidth(), mView.getHeight());
        }
        this.backRoundRectWidth = DEFAULT_BACK_ROUND_RECT_WIDTH * backRoundRectScale;
        this.backRoundRectScale = backRoundRectScale;
    }
    
    
    // add but not used
    public float getGlowScale() {
        return 0f;
    }
    
    public void setGlowScale(float x) {
    }
    
    public float getGlowAlpha() {
        return 0f;
    }
    
    public void setGlowAlpha(float x) {
    }
    
    public float getDrawingAlpha() {
        return 0f;
    }
    
    public void setDrawingAlpha(float x) {
    }
    
    public void setEnabled(boolean enable) {
        setAnimationEnabled(enable);
    }

}
