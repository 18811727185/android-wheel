package com.letv.shared.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.letv.shared.widget.LeAlertDialog;

public class LeEditDialogPreference extends LeDialogPreference {

	private EditText editText;

	private CharSequence text;

	public CharSequence getText() {
		return text == null ? "" : text;
	}

	public void setText(CharSequence text) {
		this.text = text;
	}

	public LeEditDialogPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		editText = new EditText(context, attrs);
	}

	public LeEditDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		editText = new EditText(context, attrs);
	}

	public LeEditDialogPreference(Context context) {
		this(context, null);
	}

	@Override
	protected void onBindView(View view) {
		setSummary(getText());
		super.onBindView(view);
	}

	protected boolean shouldInputMethed() {
		return true;
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		if (restorePersistedValue) {
			setText(getPersistedString(""));
		}
		super.onSetInitialValue(restorePersistedValue, defaultValue);
	}

	protected void preShowDialog(LeAlertDialog dialog) {

		dialog.setContentView(editText);
		editText.setText(getText());

		dialog.setPositiveButton(getPositiveCharSequence(), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				setText(editText.getText().toString());
				if (shouldPersist()) {
					persistString(getText().toString());
				}
				notifyChanged();
				dialog.dismiss();
				if(positiveClickListener!=null){
					positiveClickListener.onClick(dialog, which);
				}
			}
		});
	}

}
