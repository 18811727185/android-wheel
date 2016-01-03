package com.letv.shared.widget;

/**
 * Created by liangchao on 15-1-13.
 */

import android.animation.*;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.*;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;

import java.util.List;
import java.util.Map;

import android.widget.AdapterView;
import com.letv.shared.R;

public class LeActionBarBottomSlider {
    private static final int POSTDELAY_ENTER = 150;
    private static final int DURATION_ENTER = 200;
    private static final int DURATION_EXIT = 200;
    private static final int POSTDELAY_EXIT = 20;
    private static final float LISTVIEW_ITEM_HEIGHT = 56f;
    private static final float LISTVIEW_PADDING = 32f;

    public Dialog getmDialog() {
        return mDialog;
    }

    private Dialog mDialog;
    private ListView listView;
    private static int listitem_height;

    private LeTransLinearLayout mMenuView;

    private int menuViewHeight;

    private int startPos;


    private Activity mContext;


    private View out;
    private boolean responsable = true;
    private static float density;
    private LayoutInflater inflater;

    public int getCheckPos() {
        return checkPos;
    }

    public void setCheckPos(int checkPos) {
        this.checkPos = checkPos;
    }

    private int checkPos = -1;

    public LeActionBarBottomSlider(Activity context, int startPos) {
        this.startPos = startPos;
        mDialog = new Dialog(context,R.style.leActionBarBottomSliderTheme);
        mDialog.getWindow().setGravity(Gravity.TOP);
        mContext = context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        out = inflater.inflate(R.layout.le_actionbarbottomslider_layout, null);
        mMenuView = (LeTransLinearLayout)out.findViewById(R.id.le_actionbarbottomslider_pop_layout);
        LeLayoutTransparentHelper transparentHelper = new LeLayoutTransparentHelper();
        transparentHelper.setTrasparent(true);
        mMenuView.setTransparentHelper(transparentHelper);
        View listViewLayout = inflater.inflate(R.layout.le_bottomsheet_listview,mMenuView);
        listView = (ListView)listViewLayout.findViewById(R.id.le_bottomsheet_list_0);
        listViewLayout.findViewById(R.id.le_bottomsheet_listview_gap).setVisibility(View.GONE);
        listViewLayout.findViewById(R.id.le_bottomsheet_listview_btn).setVisibility(View.GONE);
        listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        density = context.getResources().getDisplayMetrics().density;
        listitem_height = dip2px(LISTVIEW_ITEM_HEIGHT);

        mDialog.setContentView(out,new LayoutParams(LayoutParams.MATCH_PARENT,
                mContext.getWindow().getDecorView().getHeight()));


        out.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int top = (int)mMenuView.getY();
                int bottom = mMenuView.getBottom();
                int y=(int) event.getY();
                if(event.getAction()==MotionEvent.ACTION_UP){
                    if(y<top||y>bottom){
                        disappear();
                    }
                }
                return true;
            }
        });
        mMenuView.setVisibility(View.INVISIBLE);
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mMenuView.postDelayed(new Runnable() {
                @Override
                public void run() {

                    doEnterAnimation();
                }
            }, POSTDELAY_ENTER);
            }
        });

    }
    private static int dip2px(float dp) {

        return (int) (dp * density + 0.5f);
    }
    public void setStyle(BaseAdapter baseAdapter,
                         int dataSize,
                         final AdapterView.OnItemClickListener listener){
        listView.setAdapter(baseAdapter);
        if (listener!=null){
            listView.setOnItemClickListener(listener);
        }
        listView.setDividerHeight(0);
        menuViewHeight = dataSize*listitem_height+dip2px(LISTVIEW_PADDING);
    }
    public void setStyle(List<Map<String,Object>> data,
                         String[] keyName,
                         final AdapterView.OnItemClickListener listener,
                         final boolean hasIcon){
//        SimpleAdapter adapter;
        final ListViewAdapter adapter;
        adapter = new ListViewAdapter(mContext,data,keyName,hasIcon);
        
        listView.setAdapter(adapter);
        
        if (listener!=null){
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    checkPos = position;
                    adapter.notifyDataSetChanged();
                    listener.onItemClick(parent,view,position,id);

                }
            });
        }
        listView.setDividerHeight(0);
        menuViewHeight = data.size()*listitem_height+dip2px(LISTVIEW_PADDING);
    }
    public void appear(){
        mMenuView.setY(startPos - menuViewHeight);
        if (mDialog!=null&&!mDialog.isShowing()&&responsable){
            responsable = false;
            mMenuView.setIntercept(true);
            mDialog.show();
        }

    }
    public void disappear(){
        if (mDialog!=null&&mDialog.isShowing()&&responsable){
            responsable = false;
            doExitAnimation();
        }

    }
    private void doEnterAnimation(){
        PropertyValuesHolder yHolder = PropertyValuesHolder.ofFloat("y",
                mMenuView.getY(),mMenuView.getY()+menuViewHeight);
        PropertyValuesHolder hideHolder = PropertyValuesHolder.ofFloat("hidePercent",1f,0f);
        ObjectAnimator animator = ObjectAnimator
                .ofPropertyValuesHolder(mMenuView,yHolder,hideHolder);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(DURATION_ENTER);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMenuView.invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mMenuView.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                responsable = true;
                mMenuView.setIntercept(false);
            }
        });
        animator.start();
    }

    private void doExitAnimation(){

        PropertyValuesHolder yHolder = PropertyValuesHolder.ofFloat("y",
                mMenuView.getY(),mMenuView.getY()-menuViewHeight);
        PropertyValuesHolder hideHolder = PropertyValuesHolder.ofFloat("hidePercent",0f,1f);
        ObjectAnimator animator = ObjectAnimator
                .ofPropertyValuesHolder(mMenuView,yHolder,hideHolder);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.setDuration(DURATION_EXIT);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMenuView.invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mMenuView.setIntercept(true);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mMenuView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mDialog != null && mDialog.isShowing()) {
                            mDialog.dismiss();
                            responsable = true;
                            mMenuView.setIntercept(false);
                        }
                    }
                }, POSTDELAY_EXIT);

            }
        });
        animator.start();
    }
    private class ListViewAdapter extends BaseAdapter{
        private class GridTemp{
            ImageView icon;
            TextView textView;
            TextView tailImg;
            LeCheckBox leCheckBox;
        }
        private boolean hasIcon;
        private List<Map<String,Object>> data;
        private String[] key;
        private LayoutInflater inflater;
        public ListViewAdapter(Context c,List<Map<String, Object>> data, String[] key,
                               boolean hasIcon/*,boolean WithTailImg*/){
            this.key = key;
            this.data = data;
            this.hasIcon = hasIcon;
            inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final GridTemp temp;
            if(convertView == null){
                temp = new GridTemp();
                if(this.hasIcon){
                    convertView = inflater.inflate(R.layout.le_bottomsheet_list_item_logo, null);
                    temp.icon = (ImageView) convertView.findViewById(R.id.le_bottomsheet_img_logo);
                    temp.textView = (TextView) convertView.findViewById(R.id.le_bottomsheet_text_logo);
                    temp.tailImg = (TextView)convertView.findViewById(R.id.le_bottomsheet_img_logo_tail);
                    temp.leCheckBox = (LeCheckBox)convertView.findViewById(R.id.le_bottomsheet_listview_item_logo_chkbox);

                }else{
                    convertView = inflater.inflate(R.layout.le_bottomsheet_list_item, null);
                    temp.textView = (TextView) convertView.findViewById(R.id.le_bottomsheet_text);
                    temp.tailImg = (TextView)convertView.findViewById(R.id.le_bottomsheet_img_tail);
                    temp.leCheckBox = (LeCheckBox)convertView.findViewById(R.id.le_bottomsheet_listview_item_chkbox);
                }
                temp.leCheckBox.setClickable(false);
                temp.leCheckBox.attachAnimateToTextViewColor(temp.textView,0xff2395ee);
                temp.tailImg.setVisibility(View.GONE);
                convertView.setTag(temp);

            }else{
                temp = (GridTemp) convertView.getTag();
            }
            if(this.hasIcon){
                Object obj = data.get(position).get(key[0]);
                if(obj!=null){
                    if (obj instanceof Integer){
                        temp.icon.setImageResource((Integer)obj);
                    }else if(obj instanceof Drawable){
                        temp.icon.setImageDrawable((Drawable)obj);
                    }
                }

                temp.textView.setText((String) data.get(position).get(key[1]));
            }else{
                temp.textView.setText((String) data.get(position).get(key[0]));

            }

            if (checkPos!=-1){
                if (checkPos!=position&&temp.leCheckBox.isChecked()){
                    temp.leCheckBox.setChecked(false);
                    temp.textView.setTextColor(Color.BLACK);
                }else if(position==checkPos&&!temp.leCheckBox.isChecked()){
                    temp.leCheckBox.setChecked(true,true);
                    if(temp.textView.getCurrentTextColor()!=0xff2395ee){
                        temp.textView.setTextColor(0xff2395ee);
                    }
                }
            }

            return convertView;
        }
    }
}

