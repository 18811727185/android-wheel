package com.letv.shared.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.letv.shared.R;
/**
 * Created by liangchao on 14-12-11.
 */
public class LeBottomSheetImageAdapter extends BaseAdapter{
    public static int KEY_FRAME_DURATION = 33;
    private static final float DELAY_MULTIPLIER = 0.66f;
    private static final float DURATION_MULTIPLIER = 0.8f;
    private class GridTemp{
        ImageView imageView;
        TextView textView;

    }
    private List<Map<String,Object>> data;
    private String[] key;
    private List<Animation> animationList;
    private LayoutInflater inflater;
    public LeBottomSheetImageAdapter(Context c, List<Map<String, Object>> data, String[] key){
        this.key = key;
        this.data = data;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        animationList = new ArrayList<Animation>();
        for(int i = 0;i<data.size();i++){
            Animation animation = AnimationUtils.loadAnimation(c, R.anim.le_licence_slide_bottom_in);
            animationList.add(animation);
        }
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
        GridTemp temp;
        if(convertView == null){
            convertView = inflater.inflate(R.layout.le_bottomsheet_grid_item, null);
            temp = new GridTemp();
            temp.imageView = (ImageView) convertView.findViewById(R.id.le_bottomsheet_gridview_img);
            temp.textView = (TextView) convertView.findViewById(R.id.le_bottomsheet_gridview_text);
            convertView.setTag(temp);

        }else{
            temp = (GridTemp) convertView.getTag();
        }
        Object obj = data.get(position).get(key[0]);
        if(obj instanceof Integer){
            temp.imageView.setImageResource((Integer)obj);
        }else if(obj instanceof Drawable){
            temp.imageView.setImageDrawable((Drawable)obj);
        }

        temp.textView.setText((String) data.get(position).get(key[1]));
        Animation animation = animationList.get(position);

        animation.setStartOffset((long)(KEY_FRAME_DURATION*(position+1)*DELAY_MULTIPLIER));
        if (position<6){
            animation.setDuration((long)(KEY_FRAME_DURATION*10*DURATION_MULTIPLIER));
        }else{
            animation.setDuration((long)(KEY_FRAME_DURATION*9*DURATION_MULTIPLIER));
        }
        convertView.setAnimation(animation);
        return convertView;
    }

}
