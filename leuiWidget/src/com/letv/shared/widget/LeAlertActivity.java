package com.letv.shared.widget;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.letv.shared.R;


/**
 * Created by liangchao on 15-3-13.
 */
public class LeAlertActivity extends Activity{
    private LayoutInflater mInflater;

    public ViewGroup getMenuContainer() {
        return menuContainer;
    }

    public View getmMenuView() {
        return mMenuView;
    }


    public TextView getTitleView() {
        return titleView;
    }

    public TextView getContentView() {
        return contentView;
    }




    public Button getBtn_confirm() {
        return btn_confirm;
    }

    public Button getBtn_cancel() {
        return btn_cancel;
    }

    public LinearLayout getLayoutForDiy() {
        return layoutForDiy;
    }



    private ViewGroup menuContainer;
    private View mMenuView;
    private TextView titleView;
    private TextView contentView;

    private ImageView gapLine_1;
    private ImageView gapLine_2;
    private Button btn_confirm;
    private Button btn_cancel;
    private LinearLayout layoutForDiy;
    private ImageView diyLine1;
    private ImageView diyLine2;

    public LeCheckBox getCheckBox() {
        return checkBox;
    }

    private LeCheckBox checkBox;

    public LinearLayout getCheckbox_ctn() {
        return checkbox_ctn;
    }

    private LinearLayout checkbox_ctn;

    public static final int BTN_CFM_COLOR_BLUE = 0xff518ef1;
    public static final int BTN_CFM_COLOR_RED = 0xfff34235;


    public int style;
    public View.OnClickListener listener_cfm;
    public View.OnClickListener listener_cle;
    public CompoundButton.OnCheckedChangeListener checkbox_listener;
    public String[] btn_text;
    public String titleText;
    public String contentText;
    public String checkboxText;
    public int[] btnColors;
    public boolean useDiyLayout;


    public void setViewFromUser(View viewFromUser) {
        this.viewFromUser = viewFromUser;
    }

    private View viewFromUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInflater = LayoutInflater.from(this);
        menuContainer = (ViewGroup)mInflater.inflate(R.layout.le_bottomsheet, null);
        getWidget();
        setYourOwnView();
        if(viewFromUser==null){
            setElement();
        }
        if(viewFromUser==null){
            setStyle(style, listener_cfm, listener_cle, checkbox_listener, btn_text,
                    titleText, contentText, checkboxText, btnColors, useDiyLayout);
        }else {
            setStyle(viewFromUser);
        }

        getWindow().setGravity(Gravity.BOTTOM);
        setContentView(menuContainer);
    }

    private void getWidget(){
        menuContainer.removeAllViews();
        inflateCustomLayout(R.layout.le_bottomsheet_btn_default);
        titleView = (TextView)mMenuView.findViewById(R.id.le_bottomsheet_default_title);
        contentView= (TextView)mMenuView.findViewById(R.id.le_bottomsheet_default_content);


        checkBox = (LeCheckBox)mMenuView.findViewById(R.id.le_bottomsheet_default_checkbox);
        checkbox_ctn = (LinearLayout)mMenuView.findViewById(R.id.le_bottomsheet_default_chk_ctn);
        gapLine_1 = (ImageView)mMenuView.findViewById(R.id.le_bottomsheet_default_gapline1);
        gapLine_2 = (ImageView)mMenuView.findViewById(R.id.le_bottomsheet_default_gapline2);
        btn_confirm = (Button)mMenuView.findViewById(R.id.le_bottomsheet_default_confirm);
        btn_cancel = (Button)mMenuView.findViewById(R.id.le_bottomsheet_default_cancel);
        layoutForDiy = (LinearLayout)mMenuView.findViewById(R.id.le_bottomsheet_default_layout_diy);
        diyLine1 = (ImageView)mMenuView.findViewById(R.id.le_bottomsheet_default_gaplinediy1);
        diyLine2 = (ImageView)mMenuView.findViewById(R.id.le_bottomsheet_default_gaplinediy2);
    }

    private void inflateCustomLayout(int resource){
        mMenuView = mInflater.inflate(resource, null);

    }
    public void setElement(){

    }

    public void setYourOwnView(){

    }

    private void setStyle(int style,
                         final View.OnClickListener listener_cfm,
                         final View.OnClickListener listener_cle,
                         CompoundButton.OnCheckedChangeListener checkbox_listener,
                         String[] btn_text,
                         String titleText,
                         String contentText,
                         String checkboxText,int[] btnColors,boolean useDiyLayout
    ){
//        this.style = style;
//        if(this.style==BUTTON_PROGRESS){
//            setProgress(titleText,btn_text,btnCfmColor,listener_cfm);
//            return;
//        }


        if((checkbox_listener==null||checkboxText==null)&&titleText==null&&contentText==null&&!useDiyLayout){
            gapLine_1.setVisibility(View.GONE);
        }
        if(titleText==null&&contentText==null&&useDiyLayout){
            diyLine1.setVisibility(View.GONE);
        }
        if(!useDiyLayout){
            layoutForDiy.setVisibility(View.GONE);
            diyLine1.setVisibility(View.GONE);
            diyLine2.setVisibility(View.GONE);
        }

        if(titleText==null||titleText.isEmpty()){
            titleView.setVisibility(View.GONE);
        }else{
            titleView.setText(titleText);
        }

        if(contentText==null||contentText.isEmpty()){
            contentView.setVisibility(View.GONE);
        }else{
            contentView.setText(contentText);
        }

        if(checkbox_listener==null||checkboxText==null||checkboxText.isEmpty()){
            checkBox.setVisibility(View.GONE);
            checkbox_ctn.setVisibility(View.GONE);
            if(useDiyLayout){
                gapLine_1.setVisibility(View.GONE);
            }


        }else{
            checkBox.setText(checkboxText);
            if(checkbox_listener!=null){
                checkBox.setOnCheckedChangeListener(checkbox_listener);
            }

            checkbox_ctn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!checkBox.isChecked()){
                        checkBox.setChecked(true,true);
                    }else{
                        checkBox.setChecked(false,true);
                    }


                }
            });

        }

        if (listener_cfm!=null){
            btn_confirm.setOnClickListener(listener_cfm);

        }
        if (listener_cle!=null){
            btn_cancel.setOnClickListener(listener_cle);
        }
        if(btn_text!=null&&btn_text.length>=1){
            btn_confirm.setText(btn_text[0]);
            btn_confirm.setTextColor(btnColors[0]);
        }
        if (btn_text.length==1){
            btn_cancel.setVisibility(View.GONE);
            gapLine_2.setVisibility(View.GONE);



        }else if(btn_text.length==2){
            btn_cancel.setText(btn_text[1]);
            btn_cancel.setTextColor(btnColors[1]);
        }

        setContentView();

    }

    private void setContentView(){
        menuContainer.addView(mMenuView);
//        if (mDialog!=null){
//            mDialog.setContentView(menuContainer);
//        }
//        final ViewTreeObserver vto = menuContainer.getViewTreeObserver();
//        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                if(maxHeightInPixel !=-1){
//                    int height = menuContainer.getMeasuredHeight();
//                    ViewGroup.LayoutParams lp = menuContainer.getLayoutParams();
//                    if(height> maxHeightInPixel){
//                        lp.height = maxHeightInPixel;
//                        menuContainer.setLayoutParams(lp);
//                    }
//                }
//            }
//        });
    }

    private void setStyle(View view){
        menuContainer.removeAllViews();
        inflateCustomLayout(R.layout.le_bottomsheet_blank);
        layoutForDiy = (LinearLayout)mMenuView.findViewById(R.id.le_bottomsheet_layout_blank);
        layoutForDiy.addView(view);
        setContentView();
    }



}
