package com.letv.shared.widget;

import android.graphics.Path;
import android.graphics.RectF;

import com.letv.shared.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

public class BlurListView extends ListView {

	// Blur renderer instance
	private GaussianBlurRenderer mBlurRenderer;
    private Path mPath;
    private float[] mRadii;

    public BlurListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public BlurListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public BlurListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}
	
	/**
	 * Initialize layout to handle background blur effect
	 */
	private void init(AttributeSet attrs) {
		mBlurRenderer = new GaussianBlurRenderer(this);

		// Read blur radius from layout variables
		if (attrs != null) {
            // Read blur radius from layout variables
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LeBlurView);
            int radius = a.getDimensionPixelSize(R.styleable.LeBlurView_le_blur_radius, 0);
            mBlurRenderer.setBlurRadius(radius);
            a.recycle();
        }
	}
	
	public void setBlurAfterView(View view) {
		mBlurRenderer.setBlurAfterView(view);
	}

    public void setRadius(float[] radii) {
        if (radii != null && radii.length == 8) {
            mPath = new Path();
            mRadii = radii;
        }
    }

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		mBlurRenderer.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mBlurRenderer.onDetachedFromWindow();
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		// If this is off-screen pass apply blur only
		if (mBlurRenderer.isOffscreenCanvas(canvas)) {
			mBlurRenderer.applyBlur();
		}
		// Otherwise draw blurred background image and continue to child views
		else {
            if (mPath != null) {
                final int height = getMeasuredHeight();
                final int width = getMeasuredWidth();
                mPath.reset();
                mPath.addRoundRect(new RectF(0, 0, width, height), mRadii, Path.Direction.CW);
                canvas.clipPath(mPath);
            }
            mBlurRenderer.drawToCanvas(canvas);
            super.dispatchDraw(canvas);
        }
    }

	/**
	 * Set blur radius in pixels
	 */
	public void setBlurRadius(int radius) {
		mBlurRenderer.setBlurRadius(radius);
		invalidate();
	}
}
