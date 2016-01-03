package com.letv.shared.text;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.shapes.Shape;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class HandleShape extends Shape {
    public static final int HANDLE_START = 0;
    public static final int HANDLE_END = 1;
    public static final int HANDLE_INSERT = 2;
    public static final int HANDLE_SECTION = 3;
    private final int mColor;
    private final float mStickWidth;
    private float mStickHeight;
    private float mBallRadius;

    private int mType;

    public HandleShape (Context context, int type, int color) {
        mType = type;
        final Resources res = context.getResources();
        mColor = color;

        DisplayMetrics metrics = res.getDisplayMetrics();
        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, metrics);
        float height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 58, metrics);

        mStickWidth = 4;
        mBallRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, metrics);
        mStickHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 17, metrics);

        if (type == HANDLE_INSERT) {
            mStickHeight = (float) (0.42 * mBallRadius);
            height = 0.75f * height;
        } else if (type == HANDLE_SECTION) {
            mBallRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, metrics);
            width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 22, metrics);
            height = width;
            mStickHeight = height / 2 - mBallRadius;
        }
        resize(width, height);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {

        final float width = getWidth();
        final float height = getHeight();
        float stickHeight = mStickHeight;
        final float stickWidth = mStickWidth;
        final float ballRadius = mBallRadius;
        final int color = mColor;
        final int type = mType;

        paint.setColor(color);

        if (type == HANDLE_START) {
            canvas.save();
            canvas.rotate(-180, width / 2, height / 2);
        }
        if (type != HANDLE_INSERT && type != HANDLE_SECTION) {
            canvas.drawRect((int)((width - stickWidth) / 2), 0, (int)((width + stickWidth) / 2),
                    stickHeight, paint);
        }

        canvas.save();
        canvas.translate((int) (width / 2 - ballRadius), stickHeight);
        canvas.rotate(-45, ballRadius, ballRadius);
        canvas.drawCircle(ballRadius, ballRadius, ballRadius, paint);
        if (type != HANDLE_SECTION) {
            canvas.drawRect(ballRadius, 0, ballRadius * 2, ballRadius, paint);
        }
        canvas.restore();

        if (type == HANDLE_START) {
            canvas.restore();
        }
    }
}
