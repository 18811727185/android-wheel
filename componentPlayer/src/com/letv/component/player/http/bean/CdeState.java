package com.letv.component.player.http.bean;

import com.letv.component.core.http.bean.LetvBaseBean;

public class CdeState  implements LetvBaseBean{

	private static final long serialVersionUID = -3013099899518810899L;
	
	// 单位ms，已经缓冲到的数据时间长度 
	public String downloadedDuration;
	
	// 单位Byte/s，正在下载的速度
	public String downloadedRate;

}
