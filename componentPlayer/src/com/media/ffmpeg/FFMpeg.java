package com.media.ffmpeg;

import com.letv.component.player.utils.LogTag;

public class FFMpeg {
  
	private static final String TAG = "FFMpeg";

	private static boolean sLoaded = false;
	

    public FFMpeg() throws Exception {
    	if(!loadLibs()) {
    		throw new Exception("Couldn't load native libs!!");
    	}
    }
    
    private static boolean loadLibs() {
    	if(sLoaded) {
    		return true;
    	}
    	boolean err = false;
    	
    	try{
//	    	if (NativeInfos.ifSupportNeon()){
//	    		    
//		    		System.loadLibrary("ffmpeg_neon");
//					System.loadLibrary("ffmpeg_jni_neon"); 
//	    			LogTag.i("chenyg","SupportNeon");
//
//	    	} else{	    		
//		    		System.loadLibrary("ffmpeg_neon");
//					System.loadLibrary("ffmpeg_jni_neon"); 	    		
//	    			LogTag.i("chenyg","Not SupportNeon");
//	    	}
    		System.loadLibrary("ffmpeg_neon_hs");
			System.loadLibrary("ffmpeg_jni_neon_hs"); 
			LogTag.i("chenyg","SupportNeon");
    	}catch (UnsatisfiedLinkError e) {
    		LogTag.i("FFMpeg", "Couldn't load lib: " + e.getMessage());
			err = true;
    	}
    	
    	if(!err) {
    		sLoaded = true;
    	}
    	return sLoaded;
    }
}
