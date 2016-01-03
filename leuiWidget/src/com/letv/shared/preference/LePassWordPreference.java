package com.letv.shared.preference;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.letv.shared.R;

public class LePassWordPreference extends LeEditTextPreference {

	CheckBox checkBox;
	boolean checked;

	public LePassWordPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public LePassWordPreference(Context context, AttributeSet attrs) {
		this(context, attrs,R.attr.lePasswordPreferenceStyle);
	}

	public LePassWordPreference(Context context) {
		this(context, null);
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);

		checkBox = (CheckBox) view.findViewById(R.id.checkbox);

		if (checkBox != null) {
			checkBox.setOnCheckedChangeListener(getCheckedChangeListener());
			checkBox.setChecked(checked);
		}

		if (mEditText != null) {
			mEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
			if (checkBox != null) {
				syncEditTextShow(!checkBox.isChecked());
			}
		}

	}

	void syncEditTextShow(boolean isPassWord) {
		if (mEditText != null) {
			if (!isPassWord) {
				mEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
			} else {
				mEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
			}
			mEditText.setSelection(mEditText.getText().toString().length());
		}
	}

	private OnCheckedChangeListener getCheckedChangeListener() {

		return new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				syncEditTextShow(!isChecked);
				checked = isChecked;
			}
		};
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state == null || !state.getClass().equals(SavedState.class)) {
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());
		mEditText.setText(myState.pwd);
		checkBox.setChecked(myState.checked);

	}

	@Override
	protected Parcelable onSaveInstanceState() {

		final Parcelable superState = super.onSaveInstanceState();
		if (isPersistent()) {
			return superState;
		}
		final SavedState myState = new SavedState(superState);

		myState.checked = isChecked();
		myState.pwd = getPwd();

		return myState;
	}

	public String getPwd() {
		return mEditText == null ? "" : mEditText.getText().toString();
	}

	public boolean isChecked() {
		return checkBox == null ? false : checkBox.isChecked();
	}

	class SavedState extends BaseSavedState {

		boolean checked;
		String pwd;

		public SavedState(Parcel source) {
			super(source);
			checked = source.readInt() == 1;
			pwd = source.readString();
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(checked ? 1 : 0);
			dest.writeString(pwd);
		}

		public final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};

	}

}
