package com.media;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class NativeThumbnail {
	
	private String mUrl;
	private int mNativeContext;
	private int mVideoWidth;
	private int mVideoHeight;
	
    static{
    	try {
			NativeLib loadLib = new NativeLib();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
   public NativeThumbnail(String url){
    	mUrl = url;
    	init(mUrl);
    	mVideoWidth = getVideoWidth();
    	mVideoHeight = getVideoHeight();
    }
    
    public Bitmap getVideoThumbnail(int width, int height, int time)
    {
    	if(mVideoWidth <= 0 || mVideoHeight <= 0){
    		return null ;
    	}
    	Bitmap bitmap = Bitmap.createBitmap(mVideoWidth, mVideoHeight, Bitmap.Config.RGB_565);
    	getThumbnail(time, bitmap);
    	
    	if (width != 0 && height != 0){
    		
    		float sx = (float) width / mVideoWidth;
    		float sy = (float) height / mVideoHeight;
    		
    		Matrix matrix = new Matrix();
    		matrix.postScale(sx, sy);
    		
    		Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, mVideoWidth, mVideoHeight, matrix, true);
    		return newBitmap;
    	}
    	else
    		return bitmap;
    }
    
    public void finalize()
    {
    	release();
    }
    
    public String getResolution(){
    	return getVideoWidth() + "*" + getVideoHeight();
    }
    
    private native void init(String url);
    
    public native int getDuration();
    public native int getVideoWidth();
    public native int getVideoHeight();
    
    //time(ms)
    private native boolean getThumbnail(int time, Bitmap bitmap);
    private native void release();
}
