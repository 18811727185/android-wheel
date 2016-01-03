package com.letv.component.player.http;

/**
 * 黑白名单常量
 * @author wangshenhao
 *
 */
public class WhiteBlackConst {

	public static final int REPORT_SUCCESS = 1;//上报成功
	public static final int REPORT_FAIL = 0;//上报失败
	public static final int REPORT_SUCCESS_ERRORCODE = -1;//上报成功时，错误码 为-1;
	public static final int REPORT_IS_SMOOTH = 1;//播放流畅
	public static final int REPORT_NOT_SMOOTH = 0;//播放不流畅
	
	
	/**
	 * 硬解上报类型
	 */
	public static final String HARD_DECODE_REPORT_TYPE = "1";
	/**
	 * 软解上报类型
	 */
	public static final String SOFT_DECODE_REPORT_TYPE = "0";

}
