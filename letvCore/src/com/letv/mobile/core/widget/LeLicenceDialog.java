package com.letv.mobile.core.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.letv.mobile.core.R;
import com.letv.shared.widget.LeCheckBox;

public class LeLicenceDialog {

    public static enum DialogType {
        /** 包含全部控件 */
        LE_DIALOG_COMMON,
        /** 没有title的 */
        LE_DIALOG_NO_TITLE,
        /** 没有checkBox */
        LE_DIALOG_NO_CHECKBOX,
        /** 没有取消按钮的 */
        LE_DIALOG_NO_CANCEL_BTN,
        /** 没有取消按钮,且没有不在提示 **/
        LE_DIALOG_NO_CANCEL_BTN_AND_NO_CHECKBOX;
    }

    public enum KEY {
        BTN_AGREE,
        BTN_CANCEL,
        OUTSIDE
    }

    private final Context mContext;
    private Dialog mAlertDialog;
    private TextView mTvTitle;
    private TextView mTvContent;
    private LeCheckBox mCheckBox;
    private Button mBtnCancel;
    private Button mBtnAgree;
    private View mLine2;

    private boolean checked = true;
    private final DialogType mDialogType;

    public LeLicenceDialog(Context context, DialogType dialogType) {
        this.mContext = context;
        this.mDialogType = dialogType;
        this.initDialog();
    }

    /**
     * 设置对话框的title和content
     * @param titleId
     * @param contentId
     */
    public void setDialogText(int titleId, int contentId) {
        this.setDialogText(this.mContext.getString(titleId),
                this.mContext.getString(contentId));
    }

    /**
     * 设置对话框的title和content
     * @param title
     * @param content
     */
    public void setDialogText(String title, String content) {
        if (title != null) {
            this.mTvTitle.setText(title);
        }
        if (content != null) {
            this.setDialogContent(content);
        }
    }

    /**
     * 设置按钮文案
     * @param confirmBtnStr
     * @param cancelBtnStr
     */
    public void setButtonText(int confirmBtnStrId, int cancelBtnStrId) {
        this.setButtonText(this.mContext.getString(confirmBtnStrId),
                this.mContext.getString(cancelBtnStrId));
    }

    /**
     * 设置按钮文案
     * @param confirmBtnStr
     * @param cancelBtnStr
     */
    public void setButtonText(String confirmBtnStr, String cancelBtnStr) {
        if (confirmBtnStr != null) {
            this.mBtnAgree.setText(confirmBtnStr);
        }
        if (cancelBtnStr != null) {
            this.mBtnCancel.setText(cancelBtnStr);
        }
    }

    private void setDialogContent(String text) {
        this.mTvContent.setText(Html.fromHtml(text));
        this.mTvContent.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void initDialog() {
        this.mAlertDialog = new Dialog(this.mContext,
                R.style.leLicenceDialogTheme);
        this.mAlertDialog.setCancelable(true);
        this.mAlertDialog.setCanceledOnTouchOutside(true);
        this.mAlertDialog.getWindow().setGravity(Gravity.BOTTOM);
        this.initDialogView();
    }

    private void initDialogView() {
        View view = LayoutInflater.from(this.mContext).inflate(
                R.layout.le_licence_dialog_content, null);

        this.mTvTitle = (TextView) view.findViewById(R.id.tv_licence_title);
        this.mTvContent = (TextView) view.findViewById(R.id.tv_licence_content);
        this.mCheckBox = (LeCheckBox) view.findViewById(R.id.cb_prompt);
        this.mLine2 = view.findViewById(R.id.line2);
        this.mCheckBox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                            boolean isChecked) {
                        LeLicenceDialog.this.checked = isChecked;
                    }
                });
        LinearLayout cb_prompt_wrapper = (LinearLayout) view
                .findViewById(R.id.cb_prompt_wrapper);
        cb_prompt_wrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LeLicenceDialog.this.mCheckBox.isChecked()) {
                    LeLicenceDialog.this.mCheckBox.setChecked(false, true);
                } else {
                    LeLicenceDialog.this.mCheckBox.setChecked(true, true);
                }
            }
        });
        this.mBtnCancel = (Button) view.findViewById(R.id.btn_cancel);
        this.mBtnAgree = (Button) view.findViewById(R.id.btn_agree);
        this.setOnClickListener(this.mBtnCancel, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        this.setOnClickListener(this.mBtnAgree, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        this.initDialogByType();
        this.mAlertDialog.setContentView(view);
    }

    /** 根据类型显示不同dialog */
    private void initDialogByType() {
        switch (this.mDialogType) {
        case LE_DIALOG_NO_TITLE:
            this.mTvTitle.setVisibility(View.GONE);
            int padding16 = this.mContext.getResources()
                    .getDimensionPixelOffset(R.dimen.letv_dimens_16);
            int padding18 = this.mContext.getResources()
                    .getDimensionPixelOffset(R.dimen.letv_dimens_18);
            this.mTvContent.setPadding(padding16, padding18, padding16, 0);
            break;
        case LE_DIALOG_NO_CANCEL_BTN:
            this.mLine2.setVisibility(View.GONE);
            this.mBtnCancel.setVisibility(View.GONE);
            break;
        case LE_DIALOG_NO_CANCEL_BTN_AND_NO_CHECKBOX:
            this.mLine2.setVisibility(View.GONE);
            this.mBtnCancel.setVisibility(View.GONE);
            this.mCheckBox.setVisibility(View.GONE);
            break;
        case LE_DIALOG_NO_CHECKBOX:
            this.mCheckBox.setVisibility(View.GONE);
            break;
        default:
            break;
        }
    }

    public LeLicenceDialog show() {
        if (null != this.mAlertDialog && !this.mAlertDialog.isShowing()) {
            this.mAlertDialog.show();
        }
        return this;
    }

    public LeLicenceDialog dismiss() {
        if (null != this.mAlertDialog && this.mAlertDialog.isShowing()) {
            this.mAlertDialog.dismiss();
        }
        return this;
    }

    public LeLicenceDialog setOnCancelListener(
            View.OnClickListener onCancelListener) {
        this.setOnClickListener(this.mBtnCancel, onCancelListener);
        return this;
    }

    public LeLicenceDialog setOnAgreeListener(
            View.OnClickListener onAgreeListener) {
        this.setOnClickListener(this.mBtnAgree, onAgreeListener);
        return this;
    }

    private void setOnClickListener(Button btn,
            final View.OnClickListener onClickListener) {
        if (null != onClickListener) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onClick(v);
                    LeLicenceDialog.this.dismiss();
                }
            });
        }
    }

    public LeLicenceDialog setOnTouchOutCancelListener(
            DialogInterface.OnCancelListener listener) {
        if (null != this.mAlertDialog) {
            this.mAlertDialog.setOnCancelListener(listener);
        }
        return this;
    }

    public LeLicenceDialog setLeLicenceDialogClickListener(
            final LeLicenceDialogClickListener listener) {
        this.mBtnAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClickListener(KEY.BTN_AGREE);
            }
        });
        this.mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClickListener(KEY.BTN_CANCEL);
            }
        });
        this.setOnTouchOutCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                listener.onClickListener(KEY.OUTSIDE);
            }
        });
        return this;
    }

    public void setCancelable(boolean isCancelable) {
        this.mAlertDialog.setCancelable(isCancelable);
    }

    public void setCanceledOnTouchOutside(boolean isCanceled) {
        this.mAlertDialog.setCanceledOnTouchOutside(isCanceled);
    }

    public boolean isChecked() {
        return this.checked;
    }

    public boolean isDialogShown() {
        return this.mAlertDialog.isShowing();
    }

    public interface LeLicenceDialogClickListener {
        public void onClickListener(KEY key);
    }
}
