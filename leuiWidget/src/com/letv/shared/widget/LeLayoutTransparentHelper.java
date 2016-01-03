package com.letv.shared.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

/**
 * Created by liangchao on 15-1-15.
 */
public class LeLayoutTransparentHelper {
    public static final boolean MODE_TOP = true;
    public static final boolean MODE_BOTTOM = false;
    public void setHidePercent(float hidePercent) {
        this.hidePercent = hidePercent;
    }

    private float hidePercent;

    public void setTrasparent(boolean isTrasparent) {
        this.isTrasparent = isTrasparent;
    }

    public boolean isTrasparent() {
        return isTrasparent;
    }

    private boolean isTrasparent = false;

    public void setHideMode(boolean hideMode) {
        this.hideMode = hideMode;
    }

    private boolean hideMode = MODE_TOP;

    public void draw(Canvas canvas) {
        if (isTrasparent){
            Paint mPaint = new Paint();
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
            if (hideMode == MODE_TOP) {
                canvas.drawRect(new RectF(0, 0, canvas.getWidth(), hidePercent*canvas.getHeight()), mPaint);
            } else {
                canvas.drawRect(new RectF(0, canvas.getHeight()*(1-hidePercent), canvas.getWidth(), canvas.getHeight()), mPaint);

            }
        }
    }
}
