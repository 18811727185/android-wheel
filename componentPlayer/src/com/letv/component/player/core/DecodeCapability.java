package com.letv.component.player.core;

public class DecodeCapability {
	
	public boolean isBlack;
	
	public boolean isWhite;
	
	public boolean isSupport1080p;
	
	public boolean isSupport720p;
	
	public boolean isSupport1300k;
	
	public boolean isSupport1000k;
	
	public boolean isSupport350k;
	
	public boolean isSupport180k;
	
	public boolean isSwitch;//灰名单开关是否打开
	
	public boolean is1080pAdapted;
	public boolean is720pAdapted;
	public boolean is1300kAdapted;
	public boolean is1000kAdapted;
	public boolean is350kAdapted;
	public boolean is180kAdapted;
	

	@Override
	public String toString() {
		return "isBlack=" + isBlack + ", isWhite=" + isWhite
				+ ", isSupport1080p=" + isSupport1080p + ", isSupport720p="
				+ isSupport720p + ", isSupport1300k=" + isSupport1300k
				+ ", isSupport1000k=" + isSupport1000k + ", isSupport350k="
				+ isSupport350k + ", isSupport180k=" + isSupport180k;
	}
	
	
}
