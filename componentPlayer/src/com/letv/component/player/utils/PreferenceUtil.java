package com.letv.component.player.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PreferenceUtil {

	private final static String DECODE_CAPABILITY = "decode_capability";
	private final static String SDK_VERSION_CODE = "sdk_version_code";
	private final static String ERROR_CODE = "error_code";
	private final static String FIRST_DECODE = "first_deocode";
	private final static String REQUEST_PARAMS = "request_params";
	private final static String REQUEST_RESULT = "request_result";
	private final static String LOCAL_CAPCITY = "local_capcity";
	

	public static SharedPreferences getDefault(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public static Editor getEditor(Context context) {
		return getDefault(context).edit();
	}
    
	/**
	 * 是否使用软解第一次播放此码流
	 */
	public static boolean isFirsSoftDecode(Context context,String type) {
        return getDefault(context).getBoolean(type, true);
    }
	
	public static void setFirsSoftDecode(Context context,String type){
      getEditor(context).putBoolean(type, false).commit();
	}
	
	/**
	 * 是否使用硬解第一次播放此码流
	 */
	public static int getFirsHardDecode(Context context) {
        return getDefault(context).getInt(FIRST_DECODE, -1);
    }
	
	public static void setFirsHardDecode(Context context,int type){
      getEditor(context).putInt(FIRST_DECODE, type).commit();
	}
	
    /**
     * 删除第一次播放成功或失败的保存值
     */
	public static void removeFirsHardDecode(Context context){
	      getEditor(context).remove(FIRST_DECODE).commit();
	}
    
    /**
     * 设置软硬解码能力 
     * @modify by wangshenhao 2015/1/15 13:22
     */
    public static void setDecodeCapability(Context context,int capability,int adaptered){
    	getEditor(context).putString(DECODE_CAPABILITY, capability+","+adaptered).commit();
    }
    
    /**
     * 获取软硬解码能力
     * @modify by wangshenhao 2015/1/15 13:22
     */
    public static String getDecodeCapability(Context context){
    	return getDefault(context).getString(DECODE_CAPABILITY, "");
    } 
    
    /**
     * sdk版本号保存
     */
    public static void setVersionCode(Context context,float versionCode){
    	getEditor(context).putFloat(SDK_VERSION_CODE, versionCode).commit();
    }
    
    /**
     * 获取文件中保存的版本号
     */
    public static float getVersionCode(Context context){
    	return getDefault(context).getFloat(SDK_VERSION_CODE, 0);
    }
	
	/**
     * 存储播放器出错信息
     */
    public static void setErrorCode(Context context, String errorCode){
    	getEditor(context).putString(ERROR_CODE, errorCode).commit();
    }
    
    /**
     * 获取播放器出错信息
     */
    public static String getErrorCode(Context context){
    	return getDefault(context).getString(ERROR_CODE, "");
    }
    
    /**
     * 存储播放器软硬解请求参数
     */
    public static void setQuestParams(Context context, String params){
    	getEditor(context).putString(REQUEST_PARAMS, params).commit();
    }
    
    /**
     * 获取软硬解请求参数
     */
    public static String getQuestParams(Context context){
    	return getDefault(context).getString(REQUEST_PARAMS, "");
    }
    
    /**
     * 存储播放器软硬解请求结果
     */
    public static void setQuestResult(Context context, String result){
    	getEditor(context).putString(REQUEST_RESULT, result).commit();
    }
    
    /**
     * 获取软硬解请求结果
     */
    public static String getQuestResult(Context context){
    	return getDefault(context).getString(REQUEST_RESULT, "");
    }
    
    /**
     * 存储本地计算 软硬解码能力
     */
    public static void setLocalCapcity(Context context, String localCapcity){
    	getEditor(context).putString(LOCAL_CAPCITY, localCapcity).commit();
    }
    
    /**
     * 获取本地计算的 软硬解码能力
     */
    public static String getLocalCapcity(Context context){
    	return getDefault(context).getString(LOCAL_CAPCITY, "");
    }
    
}
