package com.letv.mobile.core.widget;

import android.content.Context;
import android.view.View;

import com.letv.shared.widget.LeBottomSheet;

/**
 * @author liulei
 */

public class LetvBottomDialog {

    // protected LetvDialogListener mListener;

    public interface LetvDialogListener {

        public void onCancelBtnClick();

        public void onConfirmBtnClick();

    }

    public interface LetvSingleBtnDialogListener {

        public void onBtnClick();

    }

    private static LeBottomSheet mBottomSheet;

    /**
     * show leui bottom dialog
     * @param context
     *            context
     * @param listener
     *            按钮的监听回调
     * @param title
     *            标题 传null 则是没标题
     * @param content
     *            主题信息 传null 则是没内容
     * @param cancelBtnText
     *            按钮的文本内容, 传null 则是一个按钮
     * @param confirmBtnText
     *            按钮的文本内容，传null 则是一个按钮
     * @param isCancelOutside
     *            是否点击其他地方取消dialog
     */
    public static void showNormalDialog(Context context,
            final LetvDialogListener listener, String title, String content,
            String confirmBtnText, String cancelBtnText, boolean isCancelOutside) {
        mBottomSheet = new LeBottomSheet(context);
        mBottomSheet.setStyle(LeBottomSheet.BUTTON_DEFAULT_STYLE,
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        mBottomSheet.disappear();
                        listener.onConfirmBtnClick();
                    }
                }, new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        mBottomSheet.disappear();
                        listener.onCancelBtnClick();
                    }
                }, null, new String[] { cancelBtnText, confirmBtnText }, title,
                content, null, 0xff518ef1, false);
        // mBottomSheet.setContentAtCenter(true);
        mBottomSheet.setCanceledOnTouchOutside(isCancelOutside);
        mBottomSheet.appear();
    }

    /**
     * show leui single bottom no title dialog
     * @param context
     *            context
     * @param listener
     *            按钮的监听回调
     * @param title
     *            标题 传null 则是没标题
     * @param content
     *            主题信息 传null 则是没内容
     * @param cancelBtnText
     *            按钮的文本内容, 传null 则是一个按钮
     * @param confirmBtnText
     *            按钮的文本内容，传null 则是一个按钮
     * @param isCancelOutside
     *            是否点击其他地方取消dialog
     */
    public static void showSingleBtnNoTitleDialog(Context context,
            final LetvSingleBtnDialogListener listener, String content,
            String confirmBtnText, boolean isCancelOutside) {
        mBottomSheet = new LeBottomSheet(context);
        mBottomSheet.setStyle(LeBottomSheet.BUTTON_DEFAULT_STYLE,
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        mBottomSheet.disappear();
                        listener.onBtnClick();
                    }
                }, null, null, new String[] { confirmBtnText }, null, content,
                null, 0xff518ef1, false);
        // mBottomSheet.setContentAtCenter(true);
        mBottomSheet.setCanceledOnTouchOutside(isCancelOutside);
        mBottomSheet.appear();
    }

    /**
     * show leui bottom no title dialog
     * @param context
     *            context
     * @param listener
     *            按钮的监听回调
     * @param title
     *            标题 传null 则是没标题
     * @param content
     *            主题信息 传null 则是没内容
     * @param cancelBtnText
     *            按钮的文本内容, 传null 则是一个按钮
     * @param confirmBtnText
     *            按钮的文本内容，传null 则是一个按钮
     * @param isCancelOutside
     *            是否点击其他地方取消dialog
     */
    public static void showNoTitleDialog(Context context,
            final LetvDialogListener listener, String content,
            String confirmBtnText, String cancelBtnText, boolean isCancelOutside) {
        mBottomSheet = new LeBottomSheet(context);
        mBottomSheet.setStyle(LeBottomSheet.BUTTON_DEFAULT_STYLE,
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        mBottomSheet.disappear();
                        listener.onConfirmBtnClick();
                    }
                }, new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        mBottomSheet.disappear();
                        listener.onCancelBtnClick();
                    }
                }, null, new String[] { cancelBtnText, confirmBtnText }, null,
                content, null, 0xff518ef1, false);
        // mBottomSheet.setContentAtCenter(true);
        mBottomSheet.setCanceledOnTouchOutside(isCancelOutside);
        mBottomSheet.appear();
    }
}
