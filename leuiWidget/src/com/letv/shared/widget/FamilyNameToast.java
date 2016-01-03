package com.letv.shared.widget;

import java.util.List;

import com.letv.shared.R;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 显示姓氏控件
 * @author wangziming
 * 加动画可以参考FastScroller的transitionToVisible
 */
public class FamilyNameToast {
    private Context mContext;
    private LayoutInflater mInflater;
    private View mView;
    private ListViewAdapter mListViewAdapter;
    private Handler mHandler;
    private DismissThread mDismissThread;
    private List<Character> mFamilyNameList;
    private OnSelectFamilyNameListener mOnSelectFamilyNameListener;
    private final static int WINDOW_TO_CENTER_X = 200;
    private final static int WINDOW_TO_CENTER_Y = -50;
    private final static int WINDOW_DISMISS_TIME = 3000;
    
    public static interface OnSelectFamilyNameListener {
        /**
         * 当字母被点击时触发
         * @param alphabetPosition 此字母在首字母List中的位置
         * @param firstAlphabet 首字母
         */
        void onFamilyNameSelect(int position, Character familyName);
    }
    
    /**
     * 设置字母被点击监听
     * @param onSelectFamilyNameListener
     */
    public void setOnSelectFamilyNameListener(OnSelectFamilyNameListener onSelectFamilyNameListener) {
        this.mOnSelectFamilyNameListener = onSelectFamilyNameListener;
    }

    public FamilyNameToast(Context context, List<Character> familyNameList) {
        mContext = context;
        mHandler = new Handler();
        mDismissThread = new DismissThread();
        if (familyNameList != null) {
            mFamilyNameList = familyNameList;
            mInflater = LayoutInflater.from(context);
            mView = mInflater.inflate(R.layout.le_famliy_listview, null);
            mView.setVisibility(View.INVISIBLE);
            
            // 调整窗口的高度
            TextView textView = (TextView) mInflater.inflate(R.layout.le_family_text, null);
            int textHeight = textView.getMaxHeight();
            ListView listView = (ListView) mView.findViewById(R.id.list);
            LayoutParams params = listView.getLayoutParams();
            params.height = textHeight * familyNameList.size() + 2 * textView.getPaddingTop();
            listView.setLayoutParams(params);
            
            mListViewAdapter = new ListViewAdapter();
            listView.setAdapter(mListViewAdapter);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    // | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    PixelFormat.TRANSLUCENT);
            WindowManager windowManager = (WindowManager) mContext
                    .getSystemService(Context.WINDOW_SERVICE);
            lp.x = lp.x + WINDOW_TO_CENTER_X;
            lp.y = lp.y + WINDOW_TO_CENTER_Y;
            windowManager.addView(mView, lp);
        }
    }
    
    /**
     * 显示出此窗口
     * @param list 显示出的内容
     */
    public FamilyNameToast show(List<Character> list) {
        this.mFamilyNameList = list;
        return show();
    }

    /**
     * 显示出此窗口，显示的内容已有
     */
    public FamilyNameToast show() {
        mListViewAdapter.notifyDataSetChanged();
        mView.setVisibility(View.VISIBLE);
        mHandler.removeCallbacks(mDismissThread);
        mHandler.postDelayed(mDismissThread, WINDOW_DISMISS_TIME);
        return this;
    }
    
    /**
     * @return mListViewAdapter ListView的适配器
     */
    public Adapter getAdapter() {
    	return mListViewAdapter;
    }
    
    private class DismissThread implements Runnable {
        @Override
        public void run() {
            mView.setVisibility(View.INVISIBLE);
        }
    }
    
    
    private class FamilyNameOnClickListener implements OnClickListener{

        @Override
        public void onClick(View v) {
            if (mOnSelectFamilyNameListener != null && v != null) {
                int position = (Integer) v.getTag();
                mHandler.removeCallbacks(mDismissThread);
                mView.setVisibility(View.INVISIBLE);
                mOnSelectFamilyNameListener.onFamilyNameSelect(position, mFamilyNameList.get(position));
            }
        }
        
    }
    
    protected class ListViewAdapter extends BaseAdapter {
        private FamilyNameOnClickListener familyNameOnClickListener;
        
        public ListViewAdapter() {
            familyNameOnClickListener = new FamilyNameOnClickListener();
        }
        
        @Override
        public int getCount() {
            return mFamilyNameList.size();
        }

        @Override
        public Object getItem(int position) {
            return mFamilyNameList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if(convertView == null){
                convertView = mInflater.inflate(R.layout.le_family_text, null);
                textView = (TextView) convertView;
            } else {
                textView = (TextView) convertView;
            }
            
            textView.setOnClickListener(familyNameOnClickListener);
            textView.setTag(position);
            textView.setText(mFamilyNameList.get(position).toString());
            return convertView;
        }
    }
    
    /**
     * 最后要把view从windowManager中清除
     * */
    public void clear() {
        WindowManager windowManager = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.removeView(mView);
        
        mHandler.removeCallbacks(mDismissThread);
        mDismissThread = null;
        mHandler = null;
        mFamilyNameList.clear();
        mFamilyNameList = null;
    }

}
