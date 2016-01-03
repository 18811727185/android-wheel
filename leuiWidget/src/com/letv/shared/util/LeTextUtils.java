package com.letv.shared.util;

import android.os.Parcel;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.TypefaceSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LeTextUtils {
    
    public static final int THIN = 0;
    public static final int LIGHT = 1;
    public static final int NORMAL = 2;
    public static final int MEDIUM = 3;
    public static final int BOLD = 4;
    
    //according to http://zh.wikipedia.org/wiki/Unicode%E5%AD%97%E7%AC%A6%E5%B9%B3%E9%9D%A2%E6%98%A0%E5%B0%84 
    public static final Pattern PATTERN_CJK = Pattern.compile("[\u2E80-\u2EFF\u3000-\u303F\u31C0-\u31EF\u3200-\u32FF" +
    		"\u3300-\u33FF\u3400-\u4DBF\u4E00-\u9FFF\uF900-\uFAFF\uFE30-\uFE4F\uFF00-\uFFEF]+");
    
    public static Spannable convertCJKTypeface(Spannable text, String cjkFontFamily) {
        return convertCJKTypeface(text, cjkFontFamily, -1);
    }
    
    public static Spannable convertCJKTypeface(Spannable text, String cjkFontFamily, float textSize) {
        
        if (!TextUtils.isEmpty(text)) {
            LeTypefaceSpan[] old = text.getSpans(0, text.length(), LeTypefaceSpan.class);
            for (int i = old.length - 1; i >=0; i --) {
                text.removeSpan(old[i]);
            }
            
            applyTypeface(text, PATTERN_CJK, cjkFontFamily, textSize);
        }
        return text;
    }
    
    public static final void applyTypeface(Spannable s, Pattern pattern, String fontFamily) {
        applyTypeface(s, pattern, fontFamily, -1);
    }
    
    public static final void applyTypeface(Spannable s, Pattern pattern, String fontFamily, float textSize) {

        Matcher m = pattern.matcher(s);

        while (m.find()) {
            int start = m.start();
            int end = m.end();

            LeTypefaceSpan span = new LeTypefaceSpan(fontFamily, textSize);
            s.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    
    public static class LeTypefaceSpan extends TypefaceSpan {

        private float mTextSize = -1;
        
        public LeTypefaceSpan(String family) {
            super(family);
        }
        
        public LeTypefaceSpan(String family, float textSize) {
            super(family);
            setTextSize(textSize);
        }

        public LeTypefaceSpan(Parcel src) {
            super(src);
        }
        
        /**
         * @param size unit in pixel.
         * 
         **/
        public void setTextSize(float size) {
            mTextSize = size;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            if (mTextSize > 0) {
                ds.setTextSize(mTextSize);
            }
            super.updateDrawState(ds);
        }

        @Override
        public void updateMeasureState(TextPaint paint) {
            if (mTextSize > 0) {
                paint.setTextSize(mTextSize);
            }
            super.updateMeasureState(paint);
        }
        
        
    }
}
