package com.letv.shared.widget;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.letv.shared.R;

public class LeDialog extends Dialog {
	private CharSequence title;

	private CharSequence msg;

	private int iconResId;

	protected View contentView;

	protected int dialogRes = R.layout.le_dialog;

	public LeDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public LeDialog(Context context, int theme) {
		super(context, theme);
	}

	public LeDialog(Context context) {
		super(context);
	}

	public CharSequence getMsg() {
		return msg;
	}

	public void setMsg(CharSequence msg) {
		this.msg = msg;
	}

	public void setMeg(int msgId) {
		this.msg = getContext().getResources().getString(msgId);
	}

	public CharSequence getTitle() {
		return title;
	}

	public void setTitle(CharSequence title) {
		this.title = title;
	}

	public void setTitle(int titleId) {
		this.title = getContext().getResources().getString(titleId);
	}

	public void setIcon(int resId) {
		this.iconResId = resId;
	}

	public void setContentView(View view) {
		contentView = view;
	}

	public void preShow() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		View contentViewAll = getLayoutInflater().inflate(dialogRes, null);
		super.setContentView(contentViewAll);
		ImageView icon = (ImageView) findViewById(R.id.icon);
		TextView titleTv = (TextView) findViewById(R.id.title);
		View titlePanel = findViewById(R.id.le_title_panel);
		View contentPanel = findViewById(R.id.le_content_panel);

		if (titlePanel != null && iconResId == 0 && TextUtils.isEmpty(title)) {
			titlePanel.setVisibility(View.GONE);
		} else {

			if (icon != null) {
				if (iconResId == 0 && icon != null) {
					icon.setVisibility(View.GONE);
				} else {
					icon.setVisibility(View.VISIBLE);
					icon.setImageResource(iconResId);
				}
			}

			if (titleTv != null) {
				if (TextUtils.isEmpty(title)) {
					titleTv.setVisibility(View.GONE);
				} else {
					titleTv.setVisibility(View.VISIBLE);
					titleTv.setText(title);
				}
			}
		}

		if (contentPanel != null && contentView != null) {
			contentPanel.setVisibility(View.VISIBLE);
			if (contentView.getParent() != null) {
				((ViewGroup) contentView.getParent()).removeView(contentView);
			}
			((ViewGroup) contentPanel).addView(contentView);
		} else if (contentPanel != null && contentView == null) {
			contentPanel.setVisibility(View.GONE);
		}

	}

	@Override
	public void show() {
		preShow();
		super.show();
	}

}
