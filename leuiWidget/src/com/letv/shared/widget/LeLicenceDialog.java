package com.letv.shared.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.CompoundButton;
import com.letv.shared.R;

public class LeLicenceDialog {

    public static final int TYPE_POSITION_NET = R.string.le_licence_dialog_content_type1;
    public static final int TYPE_NET = R.string.le_licence_dialog_content_type2;

    public enum KEY {
        BTN_AGREE, BTN_CANCEL, OUTSIDE
    }

    private Context mContext;
    private Dialog mAlertDialog;
    private Button btnCancel;
    private Button btnAgree;

    private boolean checked = true;

    /**
     * @param context
     * @param name  app name
     * @param type  show type
     */
    public LeLicenceDialog(Context context, String name, int type) {
        mContext = context;

        initDialog(name, type);
    }

    private void initDialog(String name, int type) {
        mAlertDialog = new Dialog(mContext, R.style.leLicenceDialogTheme);
        mAlertDialog.getWindow().setGravity(Gravity.BOTTOM);
        View view = LayoutInflater.from(mContext).inflate(R.layout.le_licence_dialog_content, null);

        // init view
        TextView tvLicenceContent = (TextView) view.findViewById(R.id.tv_licence_content);
        tvLicenceContent.setText(Html.fromHtml(String.format(mContext.getString(R.string.le_licence_dialog_content), name, mContext.getString(type))));
        tvLicenceContent.setMovementMethod(LinkMovementMethod.getInstance());
        final LeCheckBox cbPrompt = (LeCheckBox) view.findViewById(R.id.cb_prompt);
        cbPrompt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checked = isChecked;
            }
        });
        LinearLayout cb_prompt_wrapper = (LinearLayout)view.findViewById(R.id.cb_prompt_wrapper);
        cb_prompt_wrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cbPrompt.isChecked()){
                    cbPrompt.setChecked(false,true);
                }else{
                    cbPrompt.setChecked(true,true);
                }
            }
        });
        btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        btnAgree = (Button) view.findViewById(R.id.btn_agree);
        setOnClickListener(btnCancel, new View.OnClickListener() {
            @Override
            public void onClick(View v) {}
        });
        setOnClickListener(btnAgree, new View.OnClickListener() {
            @Override
            public void onClick(View v) {}
        });

        mAlertDialog.setContentView(view);
    }

    public LeLicenceDialog show() {
        if(null != mAlertDialog && !mAlertDialog.isShowing()) {
            mAlertDialog.show();
        }
        return this;
    }

    public LeLicenceDialog dismiss() {
        if(null != mAlertDialog && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
        return this;
    }

    public LeLicenceDialog setOnCancelListener(View.OnClickListener onCancelListener) {
        setOnClickListener(btnCancel, onCancelListener);
        return this;
    }

    public LeLicenceDialog setOnAgreeListener(View.OnClickListener onAgreeListener) {
        setOnClickListener(btnAgree, onAgreeListener);
        return this;
    }

    private void setOnClickListener(Button btn, final View.OnClickListener onClickListener) {
        if(null != onClickListener) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onClick(v);
                    dismiss();
                }
            });
        }
    }

    public LeLicenceDialog setOnTouchOutCancelListener(DialogInterface.OnCancelListener listener) {
        if(null != mAlertDialog) {
            mAlertDialog.setOnCancelListener(listener);
        }
        return this;
    }

    public LeLicenceDialog setLeLicenceDialogClickListener(final LeLicenceDialogClickListener listener) {
        btnAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClickListener(KEY.BTN_AGREE);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClickListener(KEY.BTN_CANCEL);
            }
        });
        setOnTouchOutCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                listener.onClickListener(KEY.OUTSIDE);
            }
        });
        return this;
    }

    public boolean isChecked() {
        return checked;
    }

    public interface LeLicenceDialogClickListener {
        public void onClickListener(KEY key);
    }
}
