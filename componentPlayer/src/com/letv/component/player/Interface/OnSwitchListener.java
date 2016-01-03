package com.letv.component.player.Interface;


/**
 * 软硬解切换或是MP4切换回调
 * @author chenyueguo
 *
 */
public interface OnSwitchListener {
	
	public int SWITCH_TO_HARD = 200;
	
	public int SWITCH_TO_SOFT = 201;
	
	public int SWITCH_TO_MP4 = 201;
	
	/** videoview未添加到布局*/
	public int FAIL_CODE_NO_PARENT = 300;
	
	/** 不支持软解*/
	public int FAIL_CODE_CANNOT_SOFT_DECODE = 301;
	
	/** 不支持硬解*/
	public int FAIL_CODE_CANNOT_HARD_DECODE = 302;
	
	/** 无效切换*/
	public int FAIL_CODE_LLLEGL_SWITCH = 303;

	public void onSwitchSuccess(int type);
	
	public void onSwtichFail(int type, int failCode);
}
