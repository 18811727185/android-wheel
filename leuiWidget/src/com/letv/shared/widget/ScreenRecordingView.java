package com.letv.shared.widget;

import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RemoteViews.RemoteView;

import java.util.ArrayList;
import java.util.List;


@RemoteView
public class ScreenRecordingView extends View {



	public ScreenRecordingView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

        activeDotColor = Color.WHITE;
        bgLinePaint = new Paint();
        activeLinePaint = new Paint();

        bgLinePaint.setTextAlign(Paint.Align.CENTER);
        bgLinePaint.setStrokeWidth(4);
        bgLinePaint.setColor(bgColor);
        bgLinePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));

        activeLinePaint.setColor(activeDotColor);
        activeLinePaint.setStrokeWidth(5);
        activeLinePaint.setTextAlign(Paint.Align.CENTER);

	}

	public ScreenRecordingView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScreenRecordingView(Context context) {
		this(context, null);
	}

	private Paint bgLinePaint, activeLinePaint;

	private int maxDbNum = 100;

	private long recordTimeMillis = 0;

	private float midY;
	private int width, height;
//	private float offsetX;

	private List<TimeHolder> holders = new ArrayList<TimeHolder>();

	private PointF curPoint = new PointF();

    int bgColor;
    int activeDotColor=0;

    private int getTimeMillsByLength(float lengthPx){
        return (int) (lengthPx *500/43);
    }

    int waveCount = 0;

    @Override
	protected void onDraw(Canvas canvas) {
		width = getWidth();
		height = getHeight();

        waveCount = (int)Math.min((recordTimeMillis)/getTimeMillsByLength(9),(getWidth()>>1)/9+1);

		midY = height >> 1 ;

        canvas.drawColor(bgColor);

        float dotOffsetTime = recordTimeMillis%getTimeMillsByLength(9);
        float dotOffest = dotOffsetTime/getTimeMillsByLength(9)*9;
        canvas.save();
        canvas.translate(-dotOffest,0);

        waveIndex = holders.size()-1;
        int times =0;

        for(int i=width+10;i>=-10;i-=9){
            canvas.drawLine(i,0,i,height, bgLinePaint);
            drawWaveLine(canvas,i-4,(recordTimeMillis/getTimeMillsByLength(9)-times)*getTimeMillsByLength(9));
            times++;
        }

        canvas.restore();


	}

    int waveIndex = 0;


    private void drawWaveLine(Canvas canvas,int startX,long curMills){

        if(curMills<=0){
            return;
        }

        int saveCount = canvas.save();

        float avgDb = getAvgDB(curMills);

        float startYDown =midY+(height-midY)*(avgDb/32768);
        float startYUp = midY-(height-midY)*(avgDb/32768);

        startYDown = startYDown>midY+3?startYDown:midY+3;
        startYUp = startYUp<midY-3?startYUp:midY-3;
        activeLinePaint.setStrokeWidth(5);

        int count = (int)((startYDown - startYUp)/9+0.5);

        count = count<1?1:(count%2==1?count:count+1);

        int startTempY = (int)midY-2-(count/2*9);

        for(int i=0;i<count;i++){
            canvas.drawLine(startX,startTempY,startX,startTempY+5,activeLinePaint);
            startTempY+=9;
        }

        waveIndex--;
        canvas.restoreToCount(saveCount);
    }

    public float getAvgDB(long curMills){

        float totalDb = 0;
        int totalCount =0;

        for(int i=holders.size()-1;i>=0;i--){
            if((curMills - holders.get(i).timeMillis<=getTimeMillsByLength(9))&&(curMills-holders.get(i).timeMillis)>=0){
                totalDb +=holders.get(i).db;
                totalCount++;
            }
        }

        return totalCount==0?totalDb:totalDb/totalCount;

    }

	public void startRecording() {
	}

    public void resumeRecording(){

    }

    public static final String RECORD_TIME_KEY="record_time_key";
    public static final String RECORD_DB_KEY="record_db_key";
    public static final String RECORD_NAME="record_name;";

//    @android.view.RemotableViewMethod
    public void updateRecordUI(Bundle bundle){
        long recordTimeMillis = bundle.getLong(RECORD_TIME_KEY);
        float db = bundle.getFloat(RECORD_DB_KEY);
        updateRecordUI(recordTimeMillis,db);
    }

	public void updateRecordUI(long recordTimeMillis, float db) {
		this.recordTimeMillis = recordTimeMillis;
		if (db > 0) {

            if(holders.size()==0){
                holders.add(new TimeHolder(recordTimeMillis-50,0));
            }

			holders.add(new TimeHolder(recordTimeMillis, db));

			int exrtaNum = holders.size() - maxDbNum;
			if (exrtaNum > 0) {
				for (int i = 0; i < exrtaNum; i++) {
					holders.remove(0);
				}
			}
		}

		invalidate();
	}

	public void stopRecording() {
		holders.clear();
	}

	class TimeHolder {
		long timeMillis;
		float db;

		public TimeHolder(long timeMillis, float db) {
			this.timeMillis = timeMillis;
			this.db = db;
		}
	}

}
