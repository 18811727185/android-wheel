package com.letv.shared.widget;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.letv.shared.R;

public class LeAlertDialog extends LeDialog {
	// protected int dialogRes = R.layout.le_alert_dialog;
	
	private OnClickListener positiveListener;
	private OnClickListener neutralListener;
	private OnClickListener negativeListener;

	private CharSequence positiveCharSequence;
	private CharSequence negativeCharSequence;
	private CharSequence neutralCharSequence;

	public LeAlertDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		dialogRes = R.layout.le_alert_dialog;
	}

	public LeAlertDialog(Context context, int theme) {
		super(context, theme);
		dialogRes = R.layout.le_alert_dialog;
	}

	public LeAlertDialog(Context context) {
		super(context);
		dialogRes = R.layout.le_alert_dialog;
	}

	@Override
	public void preShow() {
		super.preShow();

		ViewGroup wizaredView = (ViewGroup) findViewById(R.id.le_wizard_panel);
		if (wizaredView != null) {
			if (TextUtils.isEmpty(positiveCharSequence) && TextUtils.isEmpty(negativeCharSequence) && TextUtils.isEmpty(neutralCharSequence)) {
				wizaredView.setVisibility(View.GONE);
			} else {
				wizaredView.setVisibility(View.VISIBLE);
				if (!TextUtils.isEmpty(positiveCharSequence)) {
					((TextView) wizaredView.findViewById(R.id.le_positive_tv)).setText(positiveCharSequence);
					wizaredView.getChildAt(2).setVisibility(View.VISIBLE);
					wizaredView.getChildAt(2).setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							if (positiveListener != null) {
								positiveListener.onClick(LeAlertDialog.this, BUTTON1);
							} else {
								dismiss();
							}
						}
					});
				} else {
					wizaredView.getChildAt(2).setVisibility(View.GONE);
				}

				if (!TextUtils.isEmpty(neutralCharSequence)) {
					((TextView) wizaredView.findViewById(R.id.le_neutral_tv)).setText(neutralCharSequence);
					wizaredView.getChildAt(1).setVisibility(View.VISIBLE);
					wizaredView.getChildAt(1).setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							if (neutralListener != null)
								neutralListener.onClick(LeAlertDialog.this, BUTTON3);
						}
					});
				} else {
					wizaredView.getChildAt(1).setVisibility(View.GONE);
				}

				if (!TextUtils.isEmpty(negativeCharSequence)) {
					((TextView) wizaredView.findViewById(R.id.le_negative_tv)).setText(negativeCharSequence);
					wizaredView.getChildAt(0).setVisibility(View.VISIBLE);
					wizaredView.getChildAt(0).setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							if (negativeListener != null) {
								negativeListener.onClick(LeAlertDialog.this, BUTTON2);
							} else {
								dismiss();
							}
						}
					});
				} else {
					wizaredView.getChildAt(0).setVisibility(View.GONE);
				}

			}
		}

	}

	

	public void setPositiveButton(CharSequence text, OnClickListener listener) {
		this.positiveListener = listener;
		positiveCharSequence = text;
	}

	public void setNegativeButton(CharSequence text, OnClickListener listener) {
		this.negativeListener = listener;
		negativeCharSequence = text;
	}

	public void setNeutralButton(CharSequence text, OnClickListener listener) {
		this.neutralListener = listener;
		neutralCharSequence = text;
	}

	public void setPositiveButton(int text, OnClickListener listener) {
		this.positiveListener = listener;
		positiveCharSequence = getContext().getResources().getString(text);
	}

	public void setNegativeButton(int text, OnClickListener listener) {
		this.negativeListener = listener;
		negativeCharSequence = getContext().getResources().getString(text);
	}

	public void setNeutralButton(int text, OnClickListener listener) {
		this.neutralListener = listener;
		neutralCharSequence = getContext().getResources().getString(text);
	}

}
