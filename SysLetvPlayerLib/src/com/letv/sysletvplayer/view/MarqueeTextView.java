package com.letv.sysletvplayer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.ViewDebug.ExportedProperty;

import com.letv.tv.core.scaleview.ScaleTextView;

/**
 * 不获取焦点也能跑马灯
 * @author zxy
 */
public class MarqueeTextView extends ScaleTextView {
    private boolean isMarquee = true;
    private boolean isFocused;
    private boolean isSelected;
    private OnSelectedListener mOnSelectedListener;

    public MarqueeTextView(Context context) {
        super(context);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean isFocused() {
        return this.isMarquee && this.isFocused;
    }

    @Override
    public void setSelected(boolean selected) {
        this.isFocused = selected;
        this.isSelected = !selected;
        super.setSelected(selected);
    }

    @Override
    @ExportedProperty
    public boolean isSelected() {
        return this.isMarquee && this.isSelected;
    }

    public void setMarquee(boolean isMarquee) {
        if (this.mOnSelectedListener != null) {
            this.mOnSelectedListener.onSelected(isMarquee);
        }
        this.isMarquee = isMarquee;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        this.isSelected = this.isMarquee;
        super.onDraw(canvas);
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        this.isSelected = this.isMarquee;
        return super.setFrame(l, t, r, b);
    }

    public OnSelectedListener getOnSelectedListener() {
        return this.mOnSelectedListener;
    }

    public void setOnSelectedListener(OnSelectedListener mOnSelectedListener) {
        this.mOnSelectedListener = mOnSelectedListener;
    }

}
