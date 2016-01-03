package com.letv.shared.widget;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;

/**
 * Created by dongshangyong@letv.com on 14-10-16.
 */
public class LePopupDrawable extends ShapeDrawable {
    public static int DEFAULT_ARROW_HEIGHT = 24;
    public static int DEFAULT_ARROW_WIDTH = 48;
    private int mArrowOffset;
    private boolean mIsArrowOnTop;
    
    public LePopupDrawable(LePopupShape s) {
        super(s);
    }

    public LePopupDrawable(float radiusSize, int color, boolean isArrowOnTop) {
        this(radiusSize, ColorStateList.valueOf(color), isArrowOnTop);
    }
    public LePopupDrawable(float radiusSize, ColorStateList fillColorList, boolean isArrowOnTop) {
        this(new LePopupShape(radiusSize, isArrowOnTop));
        setFillColorList(fillColorList);
        mIsArrowOnTop = isArrowOnTop;
        if (isArrowOnTop) {
            setPadding(0, DEFAULT_ARROW_HEIGHT, 0, 0);
        } else {
            setPadding(0, 0, 0, DEFAULT_ARROW_HEIGHT);
        }
    }
    
    public void setArrowOnTop(boolean isArrowOnTop) {
        getShape().setIsArrowOnTop(isArrowOnTop);
        if (isArrowOnTop) {
            setPadding(0, DEFAULT_ARROW_HEIGHT, 0, 0);
        } else {
            setPadding(0, 0, 0, DEFAULT_ARROW_HEIGHT);
        }
    }
    
    public boolean isArrowOnTop() {
        return getShape().isArrowOnTop();
    }
    public void setArrowOffset(int offset) {
        mArrowOffset = offset;
        getShape().setArrowOffset(mArrowOffset);
    }
    
    public boolean isShowArrow() {
        return getShape().isShowArrow();
    }
    
    public void setArrowVisible(boolean show) {
        getShape().setArrowVisible(show);
        if (!show) {
            setPadding(0, 0, 0, 0);
        } else {
            if (isArrowOnTop()) {
                setPadding(0, DEFAULT_ARROW_HEIGHT, 0, 0);
            } else {
                setPadding(0, 0, 0, DEFAULT_ARROW_HEIGHT);
            }
        }
    }

    public ColorStateList getFillColorList() {
        return getShape().getFillColor();
    }

    public void setFillColorList(ColorStateList mFillColorList) {
        getShape().setFillColor(mFillColorList);
    }

    @Override
    public LePopupShape getShape() {
        return (LePopupShape) super.getShape();
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    public int getIntrinsicWidth() {
        return 0;
    }

    @Override
    public int getIntrinsicHeight() {
        return 0;
    }

    @Override
    protected boolean onStateChange(int[] state) {
        final LePopupShape shape = getShape();
        boolean invalid = shape.updateTextColors(state);
        if (invalid) invalidateSelf();

        return super.onStateChange(state);
    }

    public static class LePopupShape extends RectShape {

        private ColorStateList mFillColorList;
        private int mCurFillColor;

        private Path mPath;
        private Path mArrowPath;
        private Path mFillPath;
        private float mArrowHeight = DEFAULT_ARROW_HEIGHT;
        private float mArrowWidth = DEFAULT_ARROW_WIDTH;
        private float mRadius;
        private boolean mIsArrowOnTop;
        private boolean mShowArrow = true;
        private int mArrowOffset;

        public LePopupShape(float radiusSize, boolean isArrowOnTop) {
            this(radiusSize, 0xFFFFFFFF, isArrowOnTop);
        }

        public void setArrowOffset(int arrowOffset) {
            mArrowOffset = arrowOffset;
        }
        
        public void setArrowVisible(boolean show) {
            if (mShowArrow == show) {
                return;
            }
            mShowArrow = show;
            updateShapePath();
        }
        
        public boolean isShowArrow() {
            return mShowArrow;
        }

        public LePopupShape(float radiusSize, int fillColor, boolean isArrowOnTop) {
            mRadius = radiusSize;
            mPath = new Path();
            mArrowPath = new Path();

            mCurFillColor = fillColor;
            mIsArrowOnTop = isArrowOnTop;
        }
        
        public void setIsArrowOnTop(boolean isArrowOnTop) {
            if (mIsArrowOnTop == isArrowOnTop) {
                return;
            }
            mIsArrowOnTop = isArrowOnTop;
            updateShapePath();
        }
        
        public boolean isArrowOnTop() {
            return mIsArrowOnTop;
        }

        @Override
        public void draw(Canvas canvas, Paint paint) {
            final int oldColor = paint.getColor();
            final int fillColor = mCurFillColor;

            paint.setColor(fillColor);
            canvas.drawPath(mPath, paint);
            if (mShowArrow) {
                canvas.save();
                canvas.translate(mArrowOffset, 0);
                canvas.drawPath(mArrowPath, paint);
                canvas.restore();
            }
            paint.setColor(oldColor);
        }

        @Override
        protected void onResize(float w, float h) {
            super.onResize(w, h);
            updateShapePath();
        }

        private void updateShapePath() {
            final float arrowWidth = mArrowWidth;
            final float arrowHeight = mArrowHeight;
            RectF r = new RectF(rect());
            mPath.reset();
            mArrowPath.reset();

            if (mShowArrow) {
                if (mIsArrowOnTop) {
                    r.top += arrowHeight;
                    mArrowPath.moveTo((r.left + r.right - arrowWidth) / 2, r.top);
                    mArrowPath.lineTo((r.left + r.right) / 2, r.top - arrowHeight);
                    mArrowPath.lineTo((r.left + r.right + arrowWidth) / 2, r.top);
                    mArrowPath.lineTo((r.left + r.right - arrowWidth) / 2, r.top);
                } else {
                    r.bottom -= arrowHeight;
                    mArrowPath.moveTo((r.left + r.right - arrowWidth) / 2, r.bottom);
                    mArrowPath.lineTo((r.left + r.right) / 2, r.bottom + arrowHeight);
                    mArrowPath.lineTo((r.left + r.right + arrowWidth) / 2, r.bottom);
                    mArrowPath.lineTo((r.left + r.right - arrowWidth) / 2, r.bottom);
                }    
            }
            mPath.addRoundRect(r, mRadius, mRadius, Path.Direction.CW);
        }

        public void setFillColor(ColorStateList fillColor) {
            mFillColorList = fillColor;
        }

        public ColorStateList getFillColor() {
            return mFillColorList;
        }

        public boolean updateTextColors(int[] state) {
            int color;
            boolean inval = false;

            if (mFillColorList != null) {
                color = mFillColorList.getColorForState(state, 0);
                if (color != mCurFillColor) {
                    mCurFillColor = color;
                    inval = true;
                }
            }

            if (inval) {
                return true;
            }
            return false;
        }

        @Override
        public LePopupShape clone() throws CloneNotSupportedException {
            LePopupShape shape = (LePopupShape) super.clone();
            shape.mPath = new Path(mPath);
            shape.mFillPath = new Path(mFillPath);
            return shape;
        }
    }
}
