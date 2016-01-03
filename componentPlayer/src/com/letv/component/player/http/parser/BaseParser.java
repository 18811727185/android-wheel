package com.letv.component.player.http.parser;

import org.json.JSONException;
import org.json.JSONObject;

import com.letv.component.core.http.parse.LetvMainParser;

/**
 * 解析基类
 * @author chenyueguo
 *
 */
public abstract class BaseParser extends LetvMainParser {

	/**
	 * 接口信息节点 Y:成功  N:失败
	 * */
	protected final static String CODE = "code" ;
	
	/**
	 * 接口返回数据节点
	 * */
	protected final static String DATA = "data" ;
	
	/**
	 * 调用接口返回时间
	 */
	protected final static String TIMESTAMP = "timestamp";
	
	/**
	 * 状态字段值
	 */
	public interface CODE_VALUES{
		/**成功*/
		public final static String SUCCESS = "A000000";
		/**参数无效*/
		public final static String PARAMETER_INVALID = "A000001";
		/**没有数据*/
		public final static String NODATA = "A000004";
		/**失败*/
		public final static String FAILURE = "A000014";
		/**服务异常*/
		public final static String SERVER_ERR = "E000000";
	}
	
	
	/** 接口状态 */
	private String code;
	
	/** 调用时间 */
	private String timestamp;
	
	public BaseParser(){
		super();
	}
	
	public BaseParser(int from){
		super(from);
	}
	
	@Override
	protected boolean canParse(String data) {
		try {
			JSONObject object = new JSONObject(data);
			if(!object.has(CODE)) {
				return false ;
			}
			code = getString(object, CODE);
			timestamp = getString(object, TIMESTAMP);
			
			if(CODE_VALUES.SUCCESS.equals(code)){
				return true ;
			}else{
				setMessage(code);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	protected JSONObject getData(String data) throws JSONException {
		JSONObject object = null ;
		if(CODE_VALUES.SUCCESS.equals(code)){
			object = new JSONObject(data);
		}else if(CODE_VALUES.FAILURE.equals(code)){
			object = new JSONObject(getLocationData());
		}
		
		return object;
	}
	
	/**
	 * 获取接口返回时间
	 * @return
	 */
	protected String getTimeStamp() {
		return timestamp;
	}
	
	/**
	 * 加载本地数据，需要缓存数据的解析器需要实现
	 * */
	protected String getLocationData() {
		return null;
	}
}
