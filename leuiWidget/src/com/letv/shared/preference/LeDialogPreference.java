package com.letv.shared.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.letv.shared.R;
import com.letv.shared.widget.LeAlertDialog;

public class LeDialogPreference extends Preference {

	private LeAlertDialog dialog;

	private int dialogLayoutRes;
	private int dialogIconRes;
	private CharSequence positiveCharSequence;
	private CharSequence negativeCharSequence;
	private CharSequence dialogTitle;
	private CharSequence dialogMsg;

	DialogInterface.OnClickListener negativeClickListener;
	DialogInterface.OnClickListener positiveClickListener;

	DialogInterface.OnClickListener negativeInnerClickListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			onNegativeClick(dialog, which);
			if (negativeClickListener != null) {
				negativeClickListener.onClick(dialog, which);
			}

		}
	};

	void onNegativeClick(DialogInterface dialog, int which) {

	}

	void onPositiveClick(DialogInterface dialog, int which) {

	}

	DialogInterface.OnClickListener positiveInnerClickListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			onPositiveClick(dialog, which);
			if (positiveClickListener != null) {
				positiveClickListener.onClick(dialog, which);
			}

		}
	};

	public DialogInterface.OnClickListener getNegativeClickListener() {
		return negativeClickListener;
	}

	public void setNegativeClickListener(DialogInterface.OnClickListener negativeClickListener) {
		this.negativeClickListener = negativeClickListener;
	}

	public DialogInterface.OnClickListener getPositiveClickListener() {
		return positiveClickListener;
	}

	public void setPositiveClickListener(DialogInterface.OnClickListener positiveClickListener) {
		this.positiveClickListener = positiveClickListener;
	}

	public LeDialogPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LeDialogPreference, defStyle, 0);
        dialogLayoutRes = a.getResourceId(R.styleable.LeDialogPreference_dialogLayout, 0);
        positiveCharSequence = a.getString(R.styleable.LeDialogPreference_positiveButtonText);
        negativeCharSequence = a.getString(R.styleable.LeDialogPreference_negativeButtonText);
        dialogTitle = a.getString(R.styleable.LeDialogPreference_dialogTitle);
        dialogMsg = a.getString(R.styleable.LeDialogPreference_dialogMessage);
        dialogIconRes = a.getResourceId(R.styleable.LeDialogPreference_dialogIcon, 0);
/*		for (int i = a.getIndexCount(); i >= 0; i--) {
			int attr = a.getIndex(i);
			switch (attr) {
			case R.styleable.LeDialogPreference_dialogLayout:
				dialogLayoutRes = a.getResourceId(attr, 0);
				break;
			case R.styleable.LeDialogPreference_positiveButtonText:
				positiveCharSequence = a.getString(attr);
				break;
			case R.styleable.LeDialogPreference_negativeButtonText:
				negativeCharSequence = a.getString(attr);
				break;
			case R.styleable.LeDialogPreference_dialogTitle:
				dialogTitle = a.getString(attr);
				break;
			case R.styleable.LeDialogPreference_dialogMessage:
				dialogMsg = a.getString(attr);
				break;
			case R.styleable.LeDialogPreference_dialogIcon:
				dialogIconRes = a.getResourceId(attr, 0);
				break;

			default:
				break;
			}
		}*/
		a.recycle();
	}

	public LeDialogPreference(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.leDialogPreferenceStyle);
	}

	public LeDialogPreference(Context context) {
		this(context, null);
	}

	public CharSequence getDialogTitle() {
		return dialogTitle;
	}

	public void setDialogTitle(CharSequence dialogTitle) {
		this.dialogTitle = dialogTitle;
	}

	@Override
	protected void onClick() {
		super.onClick();
		showDialog(null);
	}

	protected void preShowDialog(LeAlertDialog dialog) {

	}

	protected boolean shouldInputMethed() {
		return false;
	}

	protected void showDialog(Bundle state) {
		dialog = new LeAlertDialog(getContext());
		dialog.setTitle(dialogTitle);
		dialog.setIcon(dialogIconRes);
		dialog.setMsg(dialogMsg);

		dialog.setNegativeButton(negativeCharSequence, negativeClickListener != null ? negativeInnerClickListener : null);
		dialog.setPositiveButton(positiveCharSequence, positiveClickListener != null ? positiveInnerClickListener : null);

		preShowDialog(dialog);

		dialog.show();

		Window window = dialog.getWindow();

		window.setGravity(shouldInputMethed() ? Gravity.BOTTOM : Gravity.CENTER);
		window.setSoftInputMode(shouldInputMethed() ? WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE : WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		window.setLayout(getContext().getResources().getDisplayMetrics().widthPixels, -2);
		window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
	}

	public int getDialogLayoutRes() {
		return dialogLayoutRes;
	}

	public void setDialogLayoutRes(int dialogLayoutRes) {
		this.dialogLayoutRes = dialogLayoutRes;
	}

	public int getDialogIconRes() {
		return dialogIconRes;
	}

	public void setDialogIconRes(int dialogIconRes) {
		this.dialogIconRes = dialogIconRes;
	}

	public CharSequence getPositiveCharSequence() {
		return positiveCharSequence;
	}

	public void setPositiveCharSequence(CharSequence positiveCharSequence) {
		this.positiveCharSequence = positiveCharSequence;
	}

	public CharSequence getNegativeCharSequence() {
		return negativeCharSequence;
	}

	public void setNegativeCharSequence(CharSequence negativeCharSequence) {
		this.negativeCharSequence = negativeCharSequence;
	}

	public CharSequence getDialogMsg() {
		return dialogMsg;
	}

	public void setDialogMsg(CharSequence dialogMsg) {
		this.dialogMsg = dialogMsg;
	}

}
