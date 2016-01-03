package com.letv.component.player.http.bean;

import java.util.ArrayList;

import com.letv.component.player.http.bean.Order.Tag;

public class Order extends ArrayList<Tag>{
	
	
	public static class Tag{
	
	/**
	 * 指令ID
	 */
	private  Long cmdId;
	/**
	 * 上报类型
	 */
	private  int upType;
	/**
	 *  指令分类
	 */
	private  int cmdType;
	/**
	 *  指令链接
	 */
	private  String cmdUrl;
	/**
	 * 指令参数
	 */
	private  String cmdParam;
	public Long getCmdId() {
		return cmdId;
	}
	public void setCmdId(Long cmdId) {
		this.cmdId = cmdId;
	}
	public int getUpType() {
		return upType;
	}
	public void setUpType(int upType) {
		this.upType = upType;
	}
	public int getCmdType() {
		return cmdType;
	}
	public void setCmdType(int cmdType) {
		this.cmdType = cmdType;
	}
	public String getCmdUrl() {
		return cmdUrl;
	}
	public void setCmdUrl(String cmdUrl) {
		this.cmdUrl = cmdUrl;
	}
	public String getCmdParam() {
		return cmdParam;
	}
	public void setCmdParam(String cmdParam) {
		this.cmdParam = cmdParam;
	}
	
	}

}
