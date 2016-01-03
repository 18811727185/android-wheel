package com.letv.component.player.http.bean;

import java.util.List;

import com.letv.component.core.http.bean.LetvBaseBean;
import com.letv.component.player.http.bean.Order.Tag;

public class Cmdinfo implements LetvBaseBean{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8468410886834671381L;
	/**
	 * 反馈码
	 */
	private  String fbCode;
	/**
	 * 反馈ID
	 */
	private  Long fbId;
	/**
	 *  上报周期
	 */
	private  int upPeriod;
	/**
	 *  截止时间
	 */
	private  Long endTime;
	/**
	 *  指令信息
	 */
	private  List<Tag> cmdAry;
	
	
	public List<Tag> getCmdAry() {
		return cmdAry;
	}
	public void setCmdAry(List<Tag> cmdAry) {
		this.cmdAry = cmdAry;
	}
	public String getFbCode() {
		return fbCode;
	}
	public void setFbCode(String fbCode) {
		this.fbCode = fbCode;
	}
	public Long getFbId() {
		return fbId;
	}
	public void setFbId(Long fbId) {
		this.fbId = fbId;
	}
	public int getUpPeriod() {
		return upPeriod;
	}
	public void setUpPeriod(int upPeriod) {
		this.upPeriod = upPeriod;
	}
	public Long getEndTime() {
		return endTime;
	}
	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}
	
	
}
