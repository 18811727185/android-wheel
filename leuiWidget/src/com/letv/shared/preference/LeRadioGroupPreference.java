package com.letv.shared.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.text.TextUtils;
import android.util.AttributeSet;

public class LeRadioGroupPreference extends PreferenceCategory {

	private String checkedKey;
	private String mDefault;

	public LeRadioGroupPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public LeRadioGroupPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		mDefault = a.getString(index);
		return mDefault;
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		if (restorePersistedValue) {
			checkedKey = getPersistedString(null);
		} else {
			if (!TextUtils.isEmpty(mDefault)) {
				checkedKey = mDefault;
			}
		}
		super.onSetInitialValue(restorePersistedValue, defaultValue);
	}

	public String getCheckedKey() {
		return checkedKey;
	}
	
	public void setCheckedKey(String checkedKey){
		this.checkedKey = checkedKey;
		updateRadioChangeUI(true, checkedKey);
	}


	@Override
	protected boolean onPrepareAddPreference(Preference preference) {
		if (TextUtils.isEmpty(preference.getKey())) {
			throw new IllegalArgumentException("LeRadioPreference must have key");
		}

		if (preference instanceof LeRadioPreference) {
			((LeRadioPreference) preference).registerChangeListener(new LeRadioPreferenceChangeListener() {

				@Override
				public void onRadioChanged(boolean isChecked, String key) {
					if(isChecked&&!callChangeListener(key)){
						return;
					}
					if(isChecked){
						updateRadioChangeUI(isChecked, key);
						checkedKey = key;
					}
					
				}
			}, checkedKey);
		} else {
			throw new IllegalArgumentException("not a LeRadioPreference in LeRadioGroupPreference ");
		}

		return super.onPrepareAddPreference(preference);
	}

	private void updateRadioChangeUI(boolean isChecked, String key) {
		for (int i = 0; i < getPreferenceCount(); i++) {
			LeRadioPreference radioPreference = (LeRadioPreference) getPreference(i);
			radioPreference.setChecked(radioPreference.getKey().equals(key));
		}
		if (shouldPersist()) {
			persistString(key);
		}
	}
	
	public interface LeRadioPreferenceChangeListener {
		void onRadioChanged(boolean isChecked, String key);
	}

}
