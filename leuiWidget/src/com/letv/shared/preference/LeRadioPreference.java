package com.letv.shared.preference;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;

import android.widget.TextView;
import com.letv.shared.R;
import com.letv.shared.preference.LeRadioGroupPreference.LeRadioPreferenceChangeListener;
import com.letv.shared.widget.LeCheckBox;

public class LeRadioPreference extends Preference {

	private LeCheckBox radioButton;
	LeRadioPreferenceChangeListener changeListener;
	String initialcheckedKey;
	boolean isInitialBindView = true;
	boolean initialValue = false;
	boolean mChecked = false;

	private int mode = 0;

	public static int MODE_NORMAL = 0;
	public static int MODE_RADIO = 1;

    private ColorStateList checkedColor;
    private ColorStateList uncheckedColor;
    private int boxArrowColorWithoutBorderColor;

	public int getMode() {
		return mode;
	}



	public void setMode(int mode) {
		this.mode = mode;
	}

	public LeRadioPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LeRadioPreference, defStyle, 0);
		mode = a.getInteger(R.styleable.LeRadioPreference_leRadioPreferenceMode, MODE_NORMAL);
        checkedColor = a.getColorStateList(R.styleable.LeRadioPreference_checkedTextColor);
        uncheckedColor = a.getColorStateList(R.styleable.LeRadioPreference_uncheckedTextColor);
        boxArrowColorWithoutBorderColor = a.getColor(R.styleable.LeRadioPreference_leBoxArrowColorWithoutBorder,0);
		a.recycle();
		
	}

	public LeRadioPreference(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.leRadioPreferenceStyle);
	}

	public LeRadioPreference(Context context) {
		this(context, null);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		if (restorePersistedValue) {
			initialValue = getPersistedBoolean(initialValue);
		}
		super.onSetInitialValue(restorePersistedValue, defaultValue);
	}

	@Override
	protected void onBindView(View view) {
		radioButton = (LeCheckBox) view.findViewById(R.id.le_radio_button);
		if (radioButton != null) {
			if (isInitialBindView) {
				if (!TextUtils.isEmpty(initialcheckedKey) && !TextUtils.isEmpty(getKey())) {
					radioButton.setChecked(getKey().equals(initialcheckedKey));
					mChecked = radioButton.isChecked();
				} else {
					radioButton.setChecked(initialValue);
				}
				isInitialBindView = false;
			} else {
				radioButton.setChecked(mChecked);
			}
            if(boxArrowColorWithoutBorderColor!=0){
                radioButton.setArrowColorWithoutBorder(boxArrowColorWithoutBorderColor);
            }
		}
		
		if(mode == MODE_RADIO){
			radioButton.setClickable(true);
		}

        TextView title = (TextView) view.findViewById(R.id.title);

		super.onBindView(view);

        Log.e(this.getClass().getSimpleName() + getKey(),isChecked()+" "+(checkedColor==null));

        if(isChecked()){
                if(title != null&&checkedColor!=null){
                    title.setTextColor(checkedColor);
                }
        }else{
                if(title !=null){
                    if(uncheckedColor==null) {
                        title.setTextAppearance(getContext(), android.R.attr.textAppearanceMedium);
                    }else{
                        title.setTextColor(uncheckedColor);
                    }
                }
        }

	}

	private OnCheckedChangeListener getOnCheckedChangeListener() {
		return new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (changeListener != null&&isChecked) {
					changeListener.onRadioChanged(true, getKey());
				}
			}
		};
	}

	public void setChecked(boolean isChecked) {

		if (isInitialBindView) {
			isInitialBindView = false;
			mChecked = isChecked;
			return;
		}

		if (isChecked == this.mChecked)
			return;

		this.mChecked = isChecked;

		if (changeListener != null) {
			changeListener.onRadioChanged(this.mChecked, getKey());
		}
		notifyChanged();

		if (shouldPersist()) {
			persistBoolean(mChecked);
		}
	}

	@Override
	protected void onClick() {

		if (mode == MODE_NORMAL) {
			if (changeListener != null && !mChecked) {
				changeListener.onRadioChanged(true, getKey());
			}
			mChecked = true;
			if (shouldPersist()) {
				persistBoolean(isChecked());
			}
		} else {
			super.onClick();
		}
	}

	@Override
	protected void onAttachedToActivity() {
		super.onAttachedToActivity();

	}

	public boolean isChecked() {
		return radioButton == null ? false : mChecked;
	}

	public void registerChangeListener(LeRadioPreferenceChangeListener changeListener, String initialcheckedKey) {
		this.changeListener = changeListener;
		this.initialcheckedKey = initialcheckedKey;
	}

}
