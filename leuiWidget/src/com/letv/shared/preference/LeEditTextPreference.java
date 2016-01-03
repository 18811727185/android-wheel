package com.letv.shared.preference;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import com.letv.shared.R;

public class LeEditTextPreference extends Preference implements OnFocusChangeListener, OnClickListener,
        OnAttachStateChangeListener,TextView.OnEditorActionListener {

    public static String clickedKey = null;

    protected EditText mEditText;
    private EditText mPreEditText;
    private TextView titleView;

    private int editRedId;
    private CharSequence editCharSequence;
    private CharSequence hint;

    private TextWatcher textWatcher;

    private OnFocusChangeListener onFocusChangeListener;

    private static boolean softInputShow = false;

    private OnAttachStateChangeListener stateChangeListener;

    private boolean isAttached = false;

    private long preMills = 0;

    private long curMills = 0;

    private int _id=0;

    private static int clickedId=-1;

    private static int curMaxId = 0;

    private int forceColor;

    private ColorStateList colorStateList;


    public void setEditTextStateChangeListener(OnAttachStateChangeListener stateChangeListener) {
        this.stateChangeListener = stateChangeListener;
    }

    private TextWatcher innerWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (textWatcher != null) {
                textWatcher.onTextChanged(s, start, before, count);
            }
            LeEditTextPreference.this.editCharSequence = s;
            if (shouldPersist()) {
                persistString(s.toString());
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (textWatcher != null) {
                textWatcher.beforeTextChanged(s, start, count, after);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (textWatcher != null) {
                textWatcher.afterTextChanged(s);
            }
        }
    };

    public LeEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        _id = ++curMaxId;

        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.LeEditTextPreference,defStyle,0);
        hint =typedArray.getString(R.styleable.LeEditTextPreference_hint);
        forceColor =typedArray.getColor(R.styleable.LeEditTextPreference_focusColor,0);
/*        for(int i=0;i<typedArray.getIndexCount();i++){
            int indexAttr = typedArray.getIndex(i);
            switch (indexAttr){
                case R.styleable.LeEditTextPreference_hint:
                    hint =typedArray.getString(indexAttr);
                    break;
                case R.styleable.LeEditTextPreference_focusColor:
                    forceColor =typedArray.getColor(indexAttr,0);
                    break;
            }
        }*/
        typedArray.recycle();
    }

    public LeEditTextPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.leEditTextPreferenceStyle);
    }

    public LeEditTextPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            this.editCharSequence = getPersistedString(null);
        }
        super.onSetInitialValue(restorePersistedValue, defaultValue);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);

        mEditText = (EditText) view.findViewById(android.R.id.edit);
        if (mEditText != null) {

            mEditText.setPadding(0, 0, 0, 0);
            mEditText.setEnabled(true);
            mEditText.setFocusable(true);
            mEditText.setClickable(true);
            mEditText.addTextChangedListener(innerWatcher);
            mEditText.setOnFocusChangeListener(this);
            mEditText.setFocusableInTouchMode(true);
            mEditText.setIncludeFontPadding(false);
            mEditText.addOnAttachStateChangeListener(this);
            mEditText.setCursorVisible(false);
            mEditText.setOnEditorActionListener(this);
            colorStateList = mEditText.getTextColors();
        }
        titleView = (TextView) view.findViewById(R.id.title);

        return view;
    }

    private InputFilter[] filters;
    private int inputType = -1;

    public void setFilters(InputFilter[] filters) {
        this.filters = filters;
    }

    public void setInputType(int type) {
        this.inputType = type;
    }

    public int getLength() {
        return mEditText == null ? 0 : mEditText.getText().toString().length();
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        if(mEditText!=null) {
            if (inputType != -1) {
                mEditText.setInputType(inputType);
            }
            if (filters != null) {
                mEditText.setFilters(filters);
            }

            if (editRedId != 0)
                mEditText.setText(editRedId);
                mEditText.setSelection(mEditText.getText().length());
            if (!TextUtils.isEmpty(editCharSequence)) {
                mEditText.setText(editCharSequence);
                mEditText.setSelection(mEditText.getText().length());
            }

            if (!TextUtils.isEmpty(hint)) {
                mEditText.setHint(hint);
            }

            TextView reflectTitle = (TextView) view.findViewById(R.id.le_reflect_title);
            if (reflectTitle != null) {
                reflectTitle.setText(getTitle());
            }

            ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.le_content_panel);
            if (viewGroup != null) {
                viewGroup.setOnClickListener(this);
            }

            if (mPreEditText != null && mPreEditText != mEditText) {
                mPreEditText.clearFocus();
                mPreEditText.removeCallbacks(showCursor);
            }

            mPreEditText = mEditText;
            if (clickedId == _id) {
                if (mEditText != null) {
                    mEditText.requestFocus();
                    show(getContext(), mEditText);
                    mEditText.setCursorVisible(true);
                    mEditText.postDelayed(showCursor, 300);
                }
            }

            view.addOnAttachStateChangeListener(getItemOnAttachStateChangeListener());
            view.setOnClickListener(this);
            mEditText.setOnClickListener(this);
            preMills = curMills;
            curMills = System.currentTimeMillis();
        }
    }


    private OnAttachStateChangeListener getItemOnAttachStateChangeListener(){
        return new OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                isAttached = true;
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                if(v.isFocused()){
                    hide(v.getContext(),v.getWindowToken());
                }
                clearFocus(v);
                isAttached = false;
            }
        };
    }

    private void clearFocus(View v){
        if(v instanceof ViewGroup){
            for(int i=0;i<((ViewGroup) v).getChildCount();i++){
                if(((ViewGroup) v).getChildAt(i) instanceof ViewGroup){
                    clearFocus(((ViewGroup) v).getChildAt(i));
                }else{
                    ((ViewGroup) v).getChildAt(i).clearFocus();
                }
            }
        }else{
            v.clearFocus();
        }
    }


    private Runnable showCursor = new Runnable() {

        @Override
        public void run() {
            if(isAttached) {
                mEditText.requestFocus();
                mEditText.setCursorVisible(true);
//                clickedId = -1;
            }
        }
    };

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

        if(onFocusChangeListener!=null){
            onFocusChangeListener.onFocusChange(v,hasFocus);
        }

        mEditText.setCursorVisible(hasFocus);

        if(hasFocus){
            mEditText.setSelection(mEditText.getText().toString().length());
            if(forceColor!=0){
                titleView.setTextColor(forceColor);
            }
            clickedId = _id;
        }else{
            if(colorStateList!=null) {
                titleView.setTextColor(colorStateList);
            }
        }
        if (!hasFocus && !callChangeListener(getEditCharSequence().toString())) {
            return;
        }

    }

    public int getEditRedId() {
        return editRedId;
    }

    public EditText getEditText() {
        return mEditText;
    }

    public TextWatcher getTextWatcher() {
        return textWatcher;
    }

    public void setTextWatcher(TextWatcher textWatcher) {
        this.textWatcher = textWatcher;
    }

    public void setEditRedId(int editRedId) {
        this.editRedId = editRedId;
        this.editCharSequence = null;
        notifyChanged();
    }

    public CharSequence getEditCharSequence() {
        return editCharSequence == null ? "" : editCharSequence;
    }

    public void setEditCharSequence(CharSequence editCharSequence) {
        this.editCharSequence = editCharSequence;
        this.editRedId = 0;
        notifyChanged();
    }

    public void setOnFocusChangeListener(OnFocusChangeListener onFocusChangeListener) {
        this.onFocusChangeListener = onFocusChangeListener;
    }

    public void setHint(CharSequence hint) {
        this.hint = hint;
        notifyChanged();
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        if (!TextUtils.equals(mEditText.getText(), editCharSequence)) {
            mEditText.setText(editCharSequence);
            mEditText.setSelection(mEditText.getText().length());
        }

        if(stateChangeListener!=null){
            stateChangeListener.onViewAttachedToWindow(mEditText);
        }
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        if(stateChangeListener!=null){
            stateChangeListener.onViewDetachedFromWindow(mEditText);
        }
        if(v.isFocused()){
            hide(v.getContext(), v.getWindowToken());
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

        if(actionId == EditorInfo.IME_ACTION_DONE){
            if(mEditText!=null){
                softInputShow = false;
                mEditText.setCursorVisible(false);
            }
        }
        return false;
    }

    public static void show(Context context,View view) {
        InputMethodManager imm = getInputMethodManager(context);
        imm.showSoftInput(view,InputMethodManager.SHOW_IMPLICIT);
        softInputShow = true;
    }

    public static InputMethodManager getInputMethodManager(Context context) {
        return (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public static void hide(Context context,IBinder windowToken) {
        InputMethodManager imm = getInputMethodManager(context);
        imm.hideSoftInputFromWindow(windowToken, 0);
    }

    @Override
    public void onClick(View v) {
        if (mEditText != null) {
            mEditText.requestFocus();
            show(getContext(), mEditText);
            clickedKey = getKey();
            mEditText.setCursorVisible(true);

            clickedId = _id;

        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.text = getEditCharSequence() == null ? "" : getEditCharSequence().toString();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setEditCharSequence(myState.text);
    }

    private static class SavedState extends BaseSavedState {
        String text;

        public SavedState(Parcel source) {
            super(source);
            text = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(text);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}
