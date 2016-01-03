package com.letv.shared.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import com.letv.shared.R;

/**
 * Created by dongshangyong on 14-8-8.
 */
public class LeActivationEditText extends LinearLayout implements View.OnClickListener {

    private static final int FOCUS_STAY = 0;
    private static final int FOCUS_NEXT = 1;
    private static final int FOCUS_PREV = -1;
    private static final int FOCUS_PREV_AND_DELETE = -1;
    private final String mFontFamily;
    private Typeface mTextTypeFace;

    private int mWordsBgResId;
    private int mInputMargin;
    private int mInputTextPaddingBottom;
    private int mTitleTextColor;
    private int mTitleTextSize;
    private int mInputTextColor;
    private int mInputTextSize;
    private TextView mTitleTextView;
    private ImageView mBtnClear;
    private int mBtnClearMeasureWidth;
    private int mInputInnerMargin;
    private int mActivationTextLength;
    private int mPerInputWidth;
    private CharSequence mTitleText;

    private final ArrayList<WeakReference<EditText>> mEditTextCache = new ArrayList<WeakReference<EditText>>();
    private int mCacheThreshold = 6;
    private int mMeasureWidth;
    private int mMeasureHeight;
    private int mAvaliableInputWidth = -1;
    private int mCurrentEditTextId;
    private int imeOptions = -1;
    private int inputType = -1;
    private boolean mIsTextClearing = false;

    private StringBuffer mInputBuffer = new StringBuffer();
    private OnTextChangeListener mTextChangeListener;

    public LeActivationEditText(Context context) {
        this(context, null);
    }

    public LeActivationEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LeActivationEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);


        final Resources res = context.getResources();

        mPerInputWidth = -1;
        mTitleText = res.getString(R.string.le_default_activation_title_text);
        mInputMargin = res.getDimensionPixelSize(
                R.dimen.le_default_activation_input_margin);
        mInputInnerMargin = res.getDimensionPixelSize(
                R.dimen.le_default_activation_input_inner_margin);
        mInputTextPaddingBottom = res.getDimensionPixelSize(
                R.dimen.le_default_activation_input_padding_bottom);
        mActivationTextLength = res.getInteger(R.integer.le_default_activation_input_text_length);

        mTitleTextColor = res.getColor(R.color.le_default_color_activation_title);
        mTitleTextSize = res.getDimensionPixelSize(R.dimen.le_default_activation_title_text_size);
        mInputTextColor = res.getColor(R.color.le_default_color_activation_input_text);
        mInputTextSize = res.getDimensionPixelSize(R.dimen.le_default_activation_input_text_size);
        mWordsBgResId = R.drawable.le_activation_word_background;
        Drawable imageClearSrc = res.getDrawable(R.drawable.le_btn_edit_text_clear);

        TypedArray a = context.obtainStyledAttributes(
                        attrs, R.styleable.LeActivationEditText, defStyle, 0);

        if (a.hasValue(R.styleable.LeActivationEditText_leActivationTitleText)) {
            mTitleText = a.getText(R.styleable.LeActivationEditText_leActivationTitleText);
        }

        mTitleTextColor = a.getColor(
                R.styleable.LeActivationEditText_leActivationTitleColor, mTitleTextColor);
        mTitleTextSize = a.getDimensionPixelSize(
                R.styleable.LeActivationEditText_leActivationTitleSize, mTitleTextSize);
        mInputTextSize = a.getDimensionPixelSize(
                R.styleable.LeActivationEditText_leActivationEditorTextSize, mInputTextSize);
        mInputTextColor = a.getColor(
                R.styleable.LeActivationEditText_leActivationEditorTextColor, mInputTextColor);
        mInputMargin = a.getDimensionPixelSize(
                R.styleable.LeActivationEditText_leActivationEditorMargin, mInputMargin);
        mInputInnerMargin = a.getDimensionPixelSize(
                R.styleable.LeActivationEditText_leActivationEditorInnerMargin, mInputInnerMargin);
        mInputTextPaddingBottom = a.getDimensionPixelSize(
                R.styleable.LeActivationEditText_leActivationTextPaddingBottom,
                mInputTextPaddingBottom);
        mPerInputWidth = a.getDimensionPixelSize(
                R.styleable.LeActivationEditText_leActivationPerTextWidth,
                mPerInputWidth);

        mActivationTextLength = a.getInteger(R.styleable.LeActivationEditText_leActivationTextLength,
                mActivationTextLength);

        if (a.hasValue(R.styleable.LeActivationEditText_leActivationClearSrc)) {
            imageClearSrc = a.getDrawable(R.styleable.LeActivationEditText_leActivationClearSrc);
        }

        mFontFamily = a.getString(R.styleable.LeActivationEditText_android_fontFamily);
        mWordsBgResId = a.getResourceId(R.styleable.LeActivationEditText_leActivationEditorWordBg, mWordsBgResId);

        imeOptions = a.getInt(R.styleable.LeActivationEditText_android_imeOptions, imeOptions);
        inputType = a.getInt(R.styleable.LeActivationEditText_android_inputType, inputType);
        a.recycle();

        setOrientation(HORIZONTAL);
        LayoutParams editTextParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mTitleTextView = new TextView(context);
        mTitleTextView.setLayoutParams(editTextParams);
        mTitleTextView.setTextColor(mTitleTextColor);
        mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTitleTextSize);
        mTitleTextView.setText(mTitleText);
        addView(mTitleTextView);

        mBtnClear = new ImageView(context);
        mBtnClear.setVisibility(View.INVISIBLE);
        mBtnClear.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        mBtnClear.setImageDrawable(imageClearSrc);
        mBtnClearMeasureWidth = imageClearSrc.getIntrinsicWidth();
        mBtnClear.setOnClickListener(this);

        if (!TextUtils.isEmpty(mFontFamily)) {
            mTextTypeFace = Typeface.create(mFontFamily, 0);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mMeasureWidth = getMeasuredWidth();
        mMeasureHeight = getMeasuredHeight();

        if (mAvaliableInputWidth == -1) {
            final int titleTextWidth = mTitleTextView.getMeasuredWidth();
            mAvaliableInputWidth = mMeasureWidth - titleTextWidth - mBtnClearMeasureWidth;
            setActivitionTextLength(mActivationTextLength);
        }

        setMeasuredDimension(mMeasureWidth, mMeasureHeight + mInputTextPaddingBottom);
    }

    public void setOnTextChangeListener(OnTextChangeListener listener) {
        mTextChangeListener = listener;
    }

    public void setActivitionTextLength(int length) {

        final int avaliableInputWidth = mAvaliableInputWidth - 2 * mInputMargin;
        final int perTextWidth = mPerInputWidth != -1 ?
                mPerInputWidth :(avaliableInputWidth - mInputInnerMargin * (length - 1)) / length;

        EditText editText = null;
        removeAllEditTextViews();
        for (int i = 0; i < length; ++i) {

            editText = getCacheEditText();
            editText.setTag(i);
            LayoutParams params = (LayoutParams) editText.getLayoutParams();
            if (params == null) {
                params = new LayoutParams(perTextWidth, LayoutParams.WRAP_CONTENT);
                editText.setLayoutParams(params);
            }

            if (i == 0) {
                params.leftMargin = mInputMargin;
            }else {
                params.leftMargin = mInputInnerMargin;
            }

            if (i == length - 1) {
                params.rightMargin = mInputMargin;
            }else {
                params.rightMargin = 0;
            }

            editText.setPadding(0, 0, 0, mInputTextPaddingBottom);

            addView(editText);
        }
        if (length > 0) {
            mCurrentEditTextId = 0;
        }
        mInputBuffer.delete(0, mInputBuffer.length());
        addView(mBtnClear);
    }

    public String getText() {
        return mInputBuffer.toString();
    }

    public void setText(String text) {

        clearAllText();
        final int length = text.length();
        for (int i = 0; i < length; i++) {
            View view = findViewWithTag(mCurrentEditTextId);
            if (view instanceof SingleEditText) {
                ((SingleEditText)view).setText(text.substring(i, i + 1));
            }
        }
    }

    private EditText getCacheEditText() {
        EditText editText = null;

        final int cacheSize = mEditTextCache.size();
        if (cacheSize > 0) {
            editText = mEditTextCache.remove(cacheSize - 1).get();
        }

        if (editText == null) {
            editText = new SingleEditText(getContext());
            editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mInputTextSize);
            editText.setTextColor(mInputTextColor);
            editText.setBackgroundResource(mWordsBgResId);
            editText.setSingleLine();
            editText.setGravity(Gravity.CENTER);
            editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(1) });
            editText.setIncludeFontPadding(false);

            if (imeOptions != -1) {
                editText.setImeOptions(imeOptions);
            }

            if (inputType != -1) {
                editText.setInputType(inputType);
            }

            if (mTextTypeFace != null) {
                editText.setTypeface(mTextTypeFace);
            }
        }
        editText.setText("");
        return editText;
    }

    private void recycle(EditText editText) {
        if (mEditTextCache.size() < mCacheThreshold) {
            mEditTextCache.add(new WeakReference<EditText>(editText));
        }
    }

    private void removeAllEditTextViews() {
        final int size = getChildCount();

        View view = null;
        for (int i = size - 1; i >= 0; --i) {
            view = getChildAt(i);
            if (view instanceof EditText) {
                recycle((EditText) view);
                removeViewAt(i);
            } else if (view instanceof ImageView) {
                removeViewAt(i);
            }
        }
    }

    private void moveInputFocus(CharSequence s, int type) {
        if (type == FOCUS_NEXT) {
            ++ mCurrentEditTextId;
        } else if (type < 0) {
            -- mCurrentEditTextId;
        }
        View view = findViewWithTag(mCurrentEditTextId);
        if (view != null) {
            if (type == FOCUS_PREV_AND_DELETE ) {
                clearTextAtIndex(mCurrentEditTextId);
            }
            view.requestFocus();
        }

    }

    private void clearTextAtIndex(int index) {
        View view = findViewWithTag(index);
        if (view instanceof SingleEditText) {
            if (mInputBuffer.length() > index) {
                mInputBuffer.deleteCharAt(index);
            }
            ((SingleEditText) view).setText("");
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mBtnClear) {
            clearAllText();
        }
    }

    public void clearAllText() {
        mIsTextClearing = true;
        for (int i = mCurrentEditTextId; i >= 0; --i) {
            clearTextAtIndex(i);
        }
        mCurrentEditTextId = 0;
        moveInputFocus("", FOCUS_STAY);
        mBtnClear.setVisibility(View.INVISIBLE);
        if (mTextChangeListener != null) {
            mTextChangeListener.onTextChange("");
        }
        mIsTextClearing = false;
    }

    private class SingleEditText extends EditText {

        public SingleEditText(Context context) {
            super(context);
        }

        public SingleEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public SingleEditText(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override
        protected void onTextChanged(CharSequence text, int start, int lengthBefore,
                int lengthAfter) {
            super.onTextChanged(text, start, lengthBefore, lengthAfter);

            // control the visible of clear btn.
            if (mCurrentEditTextId == 0 && lengthBefore < lengthAfter) {
                mBtnClear.setVisibility(View.VISIBLE);
            } else if (mCurrentEditTextId == 0 && lengthAfter == 0) {
                mBtnClear.setVisibility(View.INVISIBLE);
            }

            if (lengthBefore < lengthAfter) {
                mInputBuffer.append(text.charAt(0));
                if (mCurrentEditTextId < mActivationTextLength - 1) {
                    moveInputFocus(text, FOCUS_NEXT);
                } else {
                    setSelection(length());
                }
            }

            if (mTextChangeListener != null && !mIsTextClearing) {
                mTextChangeListener.onTextChange(mInputBuffer.toString());
            }
        }

        @Override
        protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);

            final int tagId = (Integer) getTag();
            if (focused && tagId != mCurrentEditTextId) {
                moveInputFocus(getText(), FOCUS_STAY);
            }
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                if (mCurrentEditTextId > 0 && getText().length() == 0) {
                    moveInputFocus(getText(), FOCUS_PREV_AND_DELETE);
                } else if (getText().length() > 0) {
                    final int tagId = (Integer) getTag();
                    if (tagId < mInputBuffer.length()) {
                        mInputBuffer.deleteCharAt(tagId);
                    }
                }
            }
            return super.onKeyDown(keyCode, event);
        }

    }

    public interface OnTextChangeListener {
        public void onTextChange(String s);
    }
}
