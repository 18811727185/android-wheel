package com.letv.shared.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;

/**
 * Created by dongshangyong@letv.com on 14-10-16.
 */
public class LeRoundRectDrawable extends ShapeDrawable {

    public LeRoundRectDrawable(LeRoundRectShape s) {
        super(s);
    }

    public LeRoundRectDrawable(Context context, float inset, int borderColorListResId,
            int fillColorListResId) {
        this(new LeRoundRectShape(inset));
        final Resources res = context.getResources();

        setBorderColorList(res.getColorStateList(borderColorListResId));
        setFillColorList(res.getColorStateList(fillColorListResId));
    }

    public LeRoundRectDrawable(float inset, ColorStateList borderColorList,
            ColorStateList fillColorList) {
        this(new LeRoundRectShape(inset));
        setBorderColorList(borderColorList);
        setFillColorList(fillColorList);
    }

    public ColorStateList getBorderColorList() {
        return getShape().getBorderColor();
    }

    public void setBorderColorList(ColorStateList mBorderColorList) {
        getShape().setBorderColor(mBorderColorList);
    }

    public ColorStateList getFillColorList() {
        return getShape().getFillColor();
    }

    public void setFillColorList(ColorStateList mFillColorList) {
        getShape().setFillColor(mFillColorList);
    }

    @Override
    public LeRoundRectShape getShape() {
        return (LeRoundRectShape) super.getShape();
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    protected boolean onStateChange(int[] state) {

        final LeRoundRectShape shape = getShape();
        boolean invalid = shape.updateTextColors(state);
        if (invalid) invalidateSelf();

        return super.onStateChange(state);
    }

    public static class LeRoundRectShape extends RectShape {

        private ColorStateList mBorderColorList;
        private ColorStateList mFillColorList;
        private int mCurBorderColor;
        private int mCurFillColor;
        private float[] mOuterRadii;
        private float[] mDefaultOuterRadii;
        private RectF mInset;
        private float[] mInnerRadii;
        private float[] mDefaultInnerRadii;

        private RectF mInnerRect;
        private Path mPath;
        private Path mFillPath;

        public LeRoundRectShape(float inset) {
            this(null, inset, 0xFFcbd0d1, 0xFFFFFFFF);
        }

        public LeRoundRectShape(float inset, int borderColor, int fillColor) {
            this(null, inset, borderColor, fillColor);
        }

        /**
         * LeRoundRectShape constructor.
         * Specifies an outer (round)rect and an optional inner (round)rect.
         *
         * @param outerRadii An array of 8 radius values, for the outer roundrect.
         *                   The first two floats are for the
         *                   top-left corner (remaining pairs correspond clockwise).
         *                   For no rounded corners on the outer rectangle,
         *                   pass null.
         * @param inset      A value that specifies the distance from the inner
         *                   rect to each side of the outer rect.
         *                   For no inner, pass null.
         */
        public LeRoundRectShape(float[] outerRadii, float inset, int borderColor, int fillColor) {
            if (outerRadii != null && outerRadii.length < 8) {
                throw new ArrayIndexOutOfBoundsException("outer radii must have >= 8 values");
            }
            mOuterRadii = outerRadii;

            if (outerRadii != null) {
                float[] innerRadii = new float[outerRadii.length];
                for (int i = 0; i < outerRadii.length; i++) {
                    innerRadii[i] = outerRadii[i] - inset;
                }
                mInnerRadii = innerRadii;
            }

            final RectF insets = new RectF(inset, inset, inset, inset);
            mInset = insets;
            if (insets != null) {
                mInnerRect = new RectF();
            }
            mPath = new Path();
            mFillPath = new Path();

            mCurBorderColor = borderColor;
            mCurFillColor = fillColor;
        }

        /**
         * LeRoundRectShape constructor.
         * Specifies an outer (round)rect and an optional inner (round)rect.
         *
         * @param outerRadii An array of 8 radius values, for the outer roundrect.
         *                   The first two floats are for the
         *                   top-left corner (remaining pairs correspond clockwise).
         *                   For no rounded corners on the outer rectangle,
         *                   pass null.
         * @param inset      A RectF that specifies the distance from the inner
         *                   rect to each side of the outer rect.
         *                   For no inner, pass null.
         * @param innerRadii An array of 8 radius values, for the inner roundrect.
         *                   The first two floats are for the
         *                   top-left corner (remaining pairs correspond clockwise).
         *                   For no rounded corners on the inner rectangle,
         *                   pass null.
         *                   If inset parameter is null, this parameter is ignored.
         */
        public LeRoundRectShape(float[] outerRadii, RectF inset,
                float[] innerRadii) {
            if (outerRadii != null && outerRadii.length < 8) {
                throw new ArrayIndexOutOfBoundsException("outer radii must have >= 8 values");
            }
            if (innerRadii != null && innerRadii.length < 8) {
                throw new ArrayIndexOutOfBoundsException("inner radii must have >= 8 values");
            }
            mOuterRadii = outerRadii;
            mInset = inset;
            mInnerRadii = innerRadii;

            if (inset != null) {
                mInnerRect = new RectF();
            }
            mPath = new Path();
        }

        @Override
        public void draw(Canvas canvas, Paint paint) {
            final int oldColor = paint.getColor();
            final int borderColor = mCurBorderColor;
            final int fillColor = mCurFillColor;

            if (mFillColorList != null) {
                paint.setColor(fillColor);
                canvas.drawPath(mFillPath, paint);
            }

            if (mBorderColorList != null) {
                paint.setColor(borderColor);
            }
            canvas.drawPath(mPath, paint);

            if (mFillColorList != null || mBorderColorList != null) {
                paint.setColor(oldColor);
            }

        }

        @Override
        protected void onResize(float w, float h) {
            super.onResize(w, h);

            RectF r = rect();
            mPath.reset();
            mFillPath.reset();

            if (mOuterRadii != null) {
                mPath.addRoundRect(r, mOuterRadii, Path.Direction.CW);
            } else {
                if (mDefaultOuterRadii == null) {
                    mDefaultOuterRadii = new float[8];
                }
                float radius = r.height() / 2;
                for (int i = 0; i < mDefaultOuterRadii.length; i ++) {
                    mDefaultOuterRadii[i] = radius;
                }

                mPath.addRoundRect(r, mDefaultOuterRadii, Path.Direction.CW);
            }

            if (mInnerRect != null) {
                mInnerRect.set(r.left + mInset.left, r.top + mInset.top,
                        r.right - mInset.right, r.bottom - mInset.bottom);
                if (mInnerRect.width() < w && mInnerRect.height() < h) {
                    if (mInnerRadii != null) {
                        mPath.addRoundRect(mInnerRect, mInnerRadii, Path.Direction.CCW);
                        mFillPath.addRoundRect(mInnerRect, mInnerRadii, Path.Direction.CCW);
                    } else {
                        if (mDefaultInnerRadii == null) {
                            mDefaultInnerRadii = new float[8];
                        }
                        float radius = r.height() / 2;
                        mDefaultInnerRadii[0] = radius - mInset.left;
                        mDefaultInnerRadii[1] = radius - mInset.left;
                        mDefaultInnerRadii[2] = radius - mInset.top;
                        mDefaultInnerRadii[3] = radius - mInset.top;
                        mDefaultInnerRadii[4] = radius - mInset.right;
                        mDefaultInnerRadii[5] = radius - mInset.right;
                        mDefaultInnerRadii[6] = radius - mInset.bottom;
                        mDefaultInnerRadii[7] = radius - mInset.bottom;

                        mPath.addRoundRect(mInnerRect, mDefaultInnerRadii, Path.Direction.CCW);
                        mFillPath.addRoundRect(mInnerRect, mDefaultInnerRadii, Path.Direction.CCW);
                    }
                }
            }
        }

        public void setBorderColor(ColorStateList borderColor) {
            mBorderColorList = borderColor;
        }

        public ColorStateList getBorderColor() {
            return mBorderColorList;
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

            if (mBorderColorList != null) {
                color = mBorderColorList.getColorForState(state, 0);
                if (color != mCurBorderColor) {
                    mCurBorderColor = color;
                    inval = true;
                }
            }

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
        public LeRoundRectShape clone() throws CloneNotSupportedException {
            LeRoundRectShape shape = (LeRoundRectShape) super.clone();
            shape.mOuterRadii = mOuterRadii != null ? mOuterRadii.clone() : null;
            shape.mInnerRadii = mInnerRadii != null ? mInnerRadii.clone() : null;
            shape.mDefaultOuterRadii = mDefaultOuterRadii != null ? mDefaultOuterRadii.clone() : null;
            shape.mDefaultInnerRadii = mDefaultInnerRadii != null ? mDefaultInnerRadii.clone() : null;

            shape.mInset = new RectF(mInset);
            shape.mInnerRect = new RectF(mInnerRect);
            shape.mPath = new Path(mPath);
            shape.mFillPath = new Path(mFillPath);
            return shape;
        }
    }
}
