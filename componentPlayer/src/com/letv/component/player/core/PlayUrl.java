package com.letv.component.player.core;

public class PlayUrl {

	/**
	 * 码流清晰度
	 */
	public enum StreamType{
		
		STREAM_TYPE_UNKNOWN("0K"),
		STREAM_TYPE_180K("180K"),
		STREAM_TYPE_350K("350K"),
		STREAM_TYPE_1000K("1000K"),
		STREAM_TYPE_1300K("1300K"),
		STREAM_TYPE_720P("720P"),
		STREAM_TYPE_1080P("1080P");
		
		private String mValue;
		private StreamType(String streamType){
			mValue = streamType;
		}
		
		public String value(){
			return mValue;
		}
	}

	/** 视频vid*/
	public int mVid;
	
	/** 码流类型*/
	public StreamType mStreamType;
	
	/** 视频播放地址*/
	public String mUrl;
	
	public PlayUrl(){
		
	}
	
	public PlayUrl(int vid,StreamType streamType,String url ){
		mVid = vid;
		mStreamType = streamType;
		mUrl = url;
	}

	public int getVid() {
		return mVid;
	}

	public void setVid(int vid) {
		this.mVid = vid;
	}

	public StreamType getStreamType() {
		return mStreamType;
	}

	public void setStreamType(StreamType streamType) {
		this.mStreamType = streamType;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		this.mUrl = url;
	}
	
	

}
