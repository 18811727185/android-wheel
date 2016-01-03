package com.letv.component.player.http;

import android.content.Context;
import android.text.TextUtils;

import com.letv.component.core.async.TaskCallBack;
import com.letv.component.player.core.AppInfo;
import com.letv.component.player.core.Configuration;
import com.letv.component.player.core.LetvMediaPlayerManager;
import com.letv.component.player.core.PlayUrl.StreamType;
import com.letv.component.player.http.bean.HardSoftDecodeCapability;
import com.letv.component.player.http.request.HttpHardSoftDecodeCapabilityRequest;
import com.letv.component.player.http.request.HttpHardSoftDecodeReportRequest;
import com.letv.component.player.utils.LogTag;
import com.letv.component.player.utils.PreferenceUtil;

public class HttpRequestManager {

	boolean mDecodeRequestFinished = false; //判断是否请求过软硬解配置
	
	private Context mContext;
	private static HttpRequestManager sRequestManager = null;
	private Configuration mConfiguration;
	
	public synchronized static void createConfig(Context context){
		if(sRequestManager==null){
			sRequestManager = new HttpRequestManager(context);
		}
	}
	
	public static HttpRequestManager getInstance(Context context){
		if(sRequestManager==null){
			createConfig(context);
		}
		return sRequestManager;
	}

	private HttpRequestManager(Context context) {
		mContext = context;
		mConfiguration = Configuration.getInstance(context);
	}
	
	
	/**
	 * 是否适配过这档码流， 用于上报时使用
	 * @param clarity
	 * @return
	 */
	public boolean isAdaptered(StreamType clarity){
		switch(clarity){
		case STREAM_TYPE_1080P:
			return mConfiguration.mHardDecodeCapability.is1080pAdapted;
		case STREAM_TYPE_720P:
			return mConfiguration.mHardDecodeCapability.is720pAdapted;
		case STREAM_TYPE_1300K:
			return mConfiguration.mHardDecodeCapability.is1300kAdapted;
		case STREAM_TYPE_1000K:
			return mConfiguration.mHardDecodeCapability.is1000kAdapted;
		case STREAM_TYPE_350K:
			return mConfiguration.mHardDecodeCapability.is350kAdapted;
		case STREAM_TYPE_180K:
			return mConfiguration.mHardDecodeCapability.is180kAdapted;
		default:
			return false;
		}
	}
	
	/**
	 * 获取软硬解能力
	 */
	public void requestCapability() {
		try {
			if(!mDecodeRequestFinished){
				HttpHardSoftDecodeCapabilityRequest hardSoftDecodeCapabilityRequest = new HttpHardSoftDecodeCapabilityRequest(
						mContext, hardSoftDecodeCapabilityCallback);
				hardSoftDecodeCapabilityRequest.execute(AppInfo.appKey, AppInfo.pCode, AppInfo.appVer);
			}
			
		} catch (Exception e) {

		}
	}

	private TaskCallBack hardSoftDecodeCapabilityCallback = new TaskCallBack() {

		@Override
		public void callback(int code, String msg, Object object) {

			if (code == TaskCallBack.CODE_OK) {
				String result = ((HardSoftDecodeCapability) object).mResult;
				if(!TextUtils.isEmpty(result)){
					String[] temp = result.split(",");
					if(temp.length==2){
						try{
							int status = Integer.parseInt(temp[0]);
							int adatered = Integer.parseInt(temp[1]);
							mConfiguration.update(status,adatered);
							mDecodeRequestFinished = true;
						}catch(NumberFormatException e){
						}
						
					}
				}
				
			} else if (code == TaskCallBack.CODE_ERROR_DATA) {

			} else if (code == TaskCallBack.CODE_ERROR_NETWORK_CONNECT) {

			} else if (code == TaskCallBack.CODE_ERROR_NETWORK_NO) {

			}
		}
	};

	/**
	 * 硬解上报
	 */
	public void hardDecodeReport(int vid, String url, int isSuc, int errCode,
			final StreamType clarity, int isSmooth) {
			int hardDecodeState = LetvMediaPlayerManager.getInstance().getHardDecodeState();
			LogTag.i("clarity:"+clarity);
			LogTag.i(isAdaptered(clarity)+":isAdaptered");
			if (hardDecodeState == LetvMediaPlayerManager.HARD_DECODE_GRAY_CONFIGURABLE &&!isAdaptered(clarity)) {
				String clarityValue = clarity.value();
				int level = -1;
				if(clarityValue.equals("1300K")){
					level = LetvMediaPlayerManager.SUPPORT_TS1300K_LEVEL;
				}else if(clarityValue.equals("1000K")){
					level = LetvMediaPlayerManager.SUPPORT_TS1000K_LEVEL;
				}else if(clarityValue.equals("350K")){
					level = 2;
				}else if(clarityValue.equals("180K")){
					level = 1;
				}else if(clarityValue.equals("720P")){
					level = LetvMediaPlayerManager.SUPPORT_TS720P_LEVEL;
				}else if(clarityValue.equals("1080P")){
					level = LetvMediaPlayerManager.SUPPORT_TS1080P_LEVEL;
				}
				LogTag.i("level:"+level);
				int sharedLevel = PreferenceUtil.getFirsHardDecode(mContext);
				LogTag.i("sharedLevel:"+sharedLevel);
				final int clarityLevel = level;
				if(clarityLevel>sharedLevel){
					try {
						LogTag.i("------>hardDecodeReport");
						HttpHardSoftDecodeReportRequest httpHardSoftDecodeReportRequest = new HttpHardSoftDecodeReportRequest(
								mContext, new TaskCallBack() {

									@Override
									public void callback(int code, String msg,
											Object object) {
										if (code == TaskCallBack.CODE_OK) {
											PreferenceUtil
													.setFirsHardDecode(mContext,clarityLevel);
											LogTag.i("硬解上报成功");
										} else if (code == TaskCallBack.CODE_ERROR_DATA) {
											LogTag.i("数据错误，硬解上报成功");
										} else if (code == TaskCallBack.CODE_ERROR_NETWORK_CONNECT) {
											LogTag.i("网咯连接错误，硬解上报失败");
										} else if (code == TaskCallBack.CODE_ERROR_NETWORK_NO) {
											LogTag.i("无网络，硬解上报失败");
										}
									}
								});
						httpHardSoftDecodeReportRequest.execute(AppInfo.appKey,
								AppInfo.pCode, AppInfo.appVer, WhiteBlackConst.HARD_DECODE_REPORT_TYPE, vid, url, isSuc,
								errCode, clarity.value(), isSmooth);
					} catch (Exception e) {

					}
				}
				
			}
	}
}
