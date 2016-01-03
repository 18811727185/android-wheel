package com.letv.shared.util;

import android.text.TextPaint;
import android.text.style.CharacterStyle;

public class LeAlignSpan extends CharacterStyle{

    public static final int TYPE_START = 1;
    public static final int TYPE_END = 2;
    
    public int mType = 0;
    public float mPadding = 0;
    public int spanStart;
    public int spanEnd;
    
    public LeAlignSpan(int type, int start, int end) {
        this.mType = type;
        spanStart = start;
        spanEnd = end;
    }
    @Override
    public void updateDrawState(TextPaint tp) {
        
    }

}
