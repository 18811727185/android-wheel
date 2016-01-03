package com.letv.mobile.core.imagecache;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 重新封装的ImageView 空间，
 * 实现为观察者模式，
 * 如果还需要特殊实现的ImageView（例如：圆角的ImageView），
 * 请继承该类
 * */
public class LetvImageView extends ImageView implements Observer {

	/**
	 * 被观察的对象
	 * */
	private Observable mObservable;

	public LetvImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LetvImageView(Context context) {
		super(context);
	}
	
	@Override
	public void requestLayout() {
//        if (ViewDebug.TRACE_HIERARCHY) {
//            ViewDebug.trace(this, ViewDebug.HierarchyTraceType.REQUEST_LAYOUT);
//        }
	}

	/**
	 * 绑定被观察的对象
	 * */
	public final void boundObservable(Observable observable) {
		if (mObservable != observable && mObservable != null) {
			mObservable.deleteObserver(this);
		}
		mObservable = observable;
		mObservable.addObserver(this);
	}

	@Override
	public final void update(Observable observable, final Object data) {
		if(mObservable == observable){
			if (data != null) {
				((Activity) getContext()).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						setImageDrawable((Drawable) data);
					}
				});
			}else{
				if (mObservable != null) {
					mObservable.deleteObserver(LetvImageView.this);
					mObservable = null ;
				}
			}
		}
	}

	@Override
	public final void setImageResource(int resId) {
		if (mObservable != null) {
			mObservable.deleteObserver(this);
			mObservable = null ;
		}
		super.setImageResource(resId);
	}

	@Override
	public final void setImageBitmap(Bitmap bm) {
		if (mObservable != null) {
			mObservable.deleteObserver(this);
			mObservable = null ;
		}
		super.setImageBitmap(bm);
	}

	@Override
	public final void setImageDrawable(Drawable drawable) {
		if (mObservable != null) {
			mObservable.deleteObserver(this);
			mObservable = null ;
		}
		super.setImageDrawable(drawable);
	}
}
