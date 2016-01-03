package com.letv.component.player.Interface;

/**
 * 关键状态时间点接口
 * @author wangk
 * time 2015-03-31 15:30
 */
public interface OnMediaStateTimeListener {

	public void onMediaStateTime(MeidaStateType mStateType,String time); 
	
	/**
	 * mediaplayer当前状态
	 */
	public enum MeidaStateType{
		INITPATH,//FOR SET MEDIA URI
		CREATE, // FOR MEDIA CREATE
		PREPARED,//FOR MEDIA PREPARED
		DIAPLAY,//FOR MEDIA DIAPLAY
		STOP, //FOR MEDIA STOP
		RELEASE,//FOR MEDIA RELEASE
		ERROR, //FOR MEDIA ERROR
		HARD_ERROR //FOR MEDIA ERROR
	}
}

