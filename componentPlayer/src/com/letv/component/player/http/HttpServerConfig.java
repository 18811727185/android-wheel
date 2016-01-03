package com.letv.component.player.http;

public class HttpServerConfig {
	
	private static boolean sIsDebug = false;
	
	/**
	 * 黑白名单测试服地址
	 */
	private static final String WHITE_BLACK_URL_TEST = "http://117.121.2.70/";//"http://10.200.89.35:10020/";
	
	/**
	 * 黑白名单正式服地址
	 */
	private static final String WHITE_BLACK_URL = "http://endecoding.go.letv.com/";
	
	/**
	 * 软硬解情况接口
	 */
	public static final String HARD_SOFT_DECODE_CAPABILITY = "dc/status";
	
	/**
	 * 软硬解上报接口
	 */
	public static String HARD_SOFT_DECODE_REPORT = "dc/rpt";
	
	/**
	 * 反馈初始化接口
	 */
	public static final String FEEDBACK_INIT_URL_TEST = "http://test.push.platform.letv.com/fb/init";
	
	public static final String FEEDBACK_INIT_URL = "http://api.feedback.platform.letv.com/fb/init";
	
	
	/**
	 * 反馈上传log接口
	 */
	public static final String FEEDBACK_UPLOADLOG_URL_TEST = "http://test.push.platform.letv.com/fb/logUpload?fbid=%s";
	
	public static final String FEEDBACK_UPLOADLOG_URL = "http://api.feedback.platform.letv.com/fb/logUpload?fbid=%s";

	/**
	 * 测试接口
	 */
	public static String getServerUrl(){
		if(sIsDebug){
			return WHITE_BLACK_URL_TEST;
		}else{
			return WHITE_BLACK_URL;
		}
	}
	
	public static String getFeedbackInitUrl() {
		if(sIsDebug){
			return FEEDBACK_INIT_URL_TEST;
		}else{
			return FEEDBACK_INIT_URL;
		}
	}
	
	public static String getFeedbackUploadlogUrl() {
		if(sIsDebug){
			return FEEDBACK_UPLOADLOG_URL_TEST;
		}else{
			return FEEDBACK_UPLOADLOG_URL;
		}
	}
	
	public static void setDebugMode(boolean isDebug){
		sIsDebug = isDebug;
	}
}
