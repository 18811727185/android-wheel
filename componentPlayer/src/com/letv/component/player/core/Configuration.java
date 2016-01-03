package com.letv.component.player.core;

import android.content.Context;
import android.text.TextUtils;

import com.letv.component.player.utils.DecodeConfigTable;
import com.letv.component.player.utils.HardDecodeUtils;
import com.letv.component.player.utils.LogTag;
import com.letv.component.player.utils.NativeInfos;
import com.letv.component.player.utils.PreferenceUtil;


public class Configuration {
	
	public static final String SDK_VERSION = "v1.6";
	
	public static final float SDK_VERSION_CODE = 1.6f;
	
	public static final int STATUS_NO_VALUE = -1;//未获取到status(软硬解码能力)
	
	public DecodeCapability mHardDecodeCapability;
	
	public DecodeCapability mSoftDecodeCapability;
	
	public static int hardDecodeState = -1;
		
	private int mLocalStatus = STATUS_NO_VALUE;	//从sharedPrefrence获取到的软硬解码能力
	
	private Context mContext;
	private static Configuration sConfiguration = null;
	
	public synchronized static void createConfig(Context context){
		if(sConfiguration==null){
			sConfiguration = new Configuration(context);
		}
	}
	
	public static Configuration getInstance(Context context){
		if(sConfiguration==null){
			createConfig(context);
		}
		return sConfiguration;
	}

	private Configuration(Context context) {
		mContext = context;
		init();
	}

	/**
	 * 初始化，请求黑白名单接口
	 * @modify  从本地读取软硬解码初始值   by wangshenhao 2015/1/15 13:32 
	 */
	private void init() {
		mHardDecodeCapability = new DecodeCapability();
		mSoftDecodeCapability = new DecodeCapability();
		String statusAndAdapterd = getLocalDecodeCapability();//从sharedpreference读取软硬解码能力及是否是配过
		if(!TextUtils.isEmpty(statusAndAdapterd)){
			String[] statusAndAdapterdArray = statusAndAdapterd.split(",");
			mLocalStatus = Integer.parseInt(statusAndAdapterdArray[0]);
			int adatered = Integer.parseInt(statusAndAdapterdArray[1]);
			parser(mLocalStatus,adatered);
			LogTag.i("从sharedpreference读取软硬解码能力及是否是配过");
		}else{
			//从配置文件中读取软硬解码能力
			String configStatus = new DecodeConfigTable().getStatus(mContext);
			if(!TextUtils.isEmpty(configStatus)){
				String[] statusAndAdapterdArray = configStatus.split(",");
				int status = Integer.parseInt(statusAndAdapterdArray[0]);
				int adaptered = Integer.parseInt(statusAndAdapterdArray[1]);
				parser(status, adaptered);
			}else{
				initDecodeCapability();
				LogTag.i("本地计算");
			}
		}
		LogTag.i("初始化，mHardDecodeCapability=" + mHardDecodeCapability.toString() + ", mSoftDecodeCapability=" + mSoftDecodeCapability.toString());
		//获取本地保存的版本号
		float sdkVersionCode = PreferenceUtil.getVersionCode(mContext);
		float currentVersionCode = SDK_VERSION_CODE;
		if(currentVersionCode>sdkVersionCode){
			PreferenceUtil.setVersionCode(mContext, currentVersionCode);
			if(sdkVersionCode!=0){
				PreferenceUtil.removeFirsHardDecode(mContext);
			}
		}
	}
	
	/**
	 * 根据手机配置情况 判断是否支持硬软件解码
	 * @modify by wangshenhao 2015/1/15 13:36
	 */
	private void initDecodeCapability(){
		mHardDecodeCapability.isWhite = false;
		mHardDecodeCapability.isBlack = false;
		hardDecodeState = LetvMediaPlayerManager.HARD_DECODE_GRAY_UNCONFIGURABLE;
		if(HardDecodeUtils.isSupportHWDecodeUseNative()) {
			int avcLevel = HardDecodeUtils.getAVCLevel();
			if(avcLevel >= 2048) {
				mHardDecodeCapability.isSupport1080p = true;
				mHardDecodeCapability.isSupport720p = true;
				mHardDecodeCapability.isSupport1300k = true;
				mHardDecodeCapability.isSupport1000k = true;
				mHardDecodeCapability.isSupport350k = true;
				mHardDecodeCapability.isSupport180k = true;
			} else if(avcLevel >= 512) {
				mHardDecodeCapability.isSupport1080p = false;
				mHardDecodeCapability.isSupport720p = true;
				mHardDecodeCapability.isSupport1300k = true;
				mHardDecodeCapability.isSupport1000k = true;
				mHardDecodeCapability.isSupport350k = true;
				mHardDecodeCapability.isSupport180k = true;
			} else if(avcLevel >= 256) {
				mHardDecodeCapability.isSupport1080p = false;
				mHardDecodeCapability.isSupport720p = false;
				mHardDecodeCapability.isSupport1300k = false;
				mHardDecodeCapability.isSupport1000k = false;
				mHardDecodeCapability.isSupport350k = true;
				mHardDecodeCapability.isSupport180k = true;
			} else if(avcLevel >= 128) {
				mHardDecodeCapability.isSupport1080p = false;
				mHardDecodeCapability.isSupport720p = false;
				mHardDecodeCapability.isSupport1300k = false;
				mHardDecodeCapability.isSupport1000k = false;
				mHardDecodeCapability.isSupport350k = false;
				mHardDecodeCapability.isSupport180k = true;
			} else {
				
				mHardDecodeCapability.isSupport1080p = false;
				mHardDecodeCapability.isSupport720p = false;
				mHardDecodeCapability.isSupport1300k = false;
				mHardDecodeCapability.isSupport1000k = false;
				mHardDecodeCapability.isSupport350k = false;
				mHardDecodeCapability.isSupport180k = false;
			}
		} else {
			hardDecodeState = LetvMediaPlayerManager.HARD_DECODE_BLACK;
			mHardDecodeCapability.isSupport1080p = false;
			mHardDecodeCapability.isSupport720p = false;
			mHardDecodeCapability.isSupport1300k = false;
			mHardDecodeCapability.isSupport1000k = false;
			mHardDecodeCapability.isSupport350k = false;
			mHardDecodeCapability.isSupport180k = false;
		}
		
		if(NativeInfos.getSupportLevel()<=NativeInfos.SUPPORT_MP4_LEVEL){
			mSoftDecodeCapability.isWhite = false;
			mSoftDecodeCapability.isBlack = true;
		}else{
			mSoftDecodeCapability.isWhite = true;
			mSoftDecodeCapability.isBlack = false;
		}
		mSoftDecodeCapability.isSupport1080p = false;
		mSoftDecodeCapability.isSupport720p =  NativeInfos.getSupportLevel()>=NativeInfos.SUPPORT_TS720P_LEVEL ? true : false;
		mSoftDecodeCapability.isSupport1300k = NativeInfos.getSupportLevel()>=NativeInfos.SUPPORT_TS1300K_LEVEL ? true : false;
		mSoftDecodeCapability.isSupport1000k = NativeInfos.getSupportLevel()>=NativeInfos.SUPPORT_TS1000K_LEVEL ? true : false;
		mSoftDecodeCapability.isSupport350k = NativeInfos.getSupportLevel()>=NativeInfos.SUPPORT_TS350K_LEVEL ? true : false;
		mSoftDecodeCapability.isSupport180k = NativeInfos.getSupportLevel()>=NativeInfos.SUPPORT_TS350K_LEVEL ? true : false;
	
	}
	
	/**
	 * 更新解码能力
	 * @author wangshenhao 2015/1/15:13:50
	 * @param status
	 */
	public void update(int status,int adaptered){
		LogTag.i("更新解码能力");
		parser(status,adaptered);
		//假如黑白名单有变化，则清除保存的第一次成功或失败的状态
		if(mLocalStatus!=STATUS_NO_VALUE){
			if(mLocalStatus!=status){
				if(hardDecodeState==LetvMediaPlayerManager.HARD_DECODE_GRAY_CONFIGURABLE){
					if(LetvMediaPlayerManager.getInstance().getHardDecodeSupportLevel()!=PreferenceUtil.getFirsHardDecode(mContext)){
						PreferenceUtil.removeFirsHardDecode(mContext);
					}
				}
			}
		}
		saveDecodeCapability(status, adaptered);
		LogTag.i("test status:"+status);
		LogTag.i("test adaper:"+adaptered);
	}
	
	/**
	 * 把解码能力状态保存到本地
	 * @author wangshenhao 2015/1/15 13:49
	 * @param status
	 */
	private void saveDecodeCapability(int status,int adaptered){
		PreferenceUtil.setDecodeCapability(mContext, status,adaptered);
	}
	
	/**
	 * 获取本地保存的软硬解码状态
	 * @return
	 */
	private String getLocalDecodeCapability(){
		return PreferenceUtil.getDecodeCapability(mContext);
	}
	
	/**
	 * 解析解码状态
	 * @param status黑白名单解码能力 adapterd是否适配过
	 * @modify 修改status&的值 wangshenhao 2015/1/15 15:13
	 */
	private void parser(int status,int adapterd) {
		mHardDecodeCapability.isBlack = ((status & 2048) == 2048) ? true : false;
		mHardDecodeCapability.isWhite = ((status & 1024) == 1024) ? true : false;
		mHardDecodeCapability.isSwitch = ((status & 16777216) == 16777216) ? true : false;
		if(mHardDecodeCapability.isBlack){
			hardDecodeState = LetvMediaPlayerManager.HARD_DECODE_BLACK;
		}else if(mHardDecodeCapability.isWhite){
			if(HardDecodeUtils.isSupportHWDecodeUseNative()){
				hardDecodeState = LetvMediaPlayerManager.HARD_DECODE_WHITE;
			}else{
				hardDecodeState = LetvMediaPlayerManager.HARD_DECODE_BLACK;
			}
		}else if(!mHardDecodeCapability.isBlack &&!mHardDecodeCapability.isWhite){
			if(mHardDecodeCapability.isSwitch){
				hardDecodeState = LetvMediaPlayerManager.HARD_DECODE_GRAY_CONFIGURABLE;
			}else{
				hardDecodeState = LetvMediaPlayerManager.HARD_DECODE_GRAY_UNCONFIGURABLE;
			}
		}
		LogTag.i("hardDecodeStae:"+hardDecodeState);
		mHardDecodeCapability.isSupport1080p = ((status & 32) == 32) ? true : false;
		mHardDecodeCapability.isSupport720p = ((status & 16) == 16) ? true : false;
		mHardDecodeCapability.isSupport1300k = ((status & 8) == 8) ? true : false;
		mHardDecodeCapability.isSupport1000k = ((status & 4) == 4) ? true : false;
		mHardDecodeCapability.isSupport350k = ((status & 2) == 2) ? true : false;
		mHardDecodeCapability.isSupport180k = ((status & 1) == 1) ? true : false;
		//是否适配过
		mHardDecodeCapability.is1080pAdapted = ((adapterd & 32) == 32) ? true : false;
		mHardDecodeCapability.is720pAdapted = ((adapterd & 16) == 16) ? true : false;
		mHardDecodeCapability.is1300kAdapted = ((adapterd & 8) == 8) ? true : false;
		mHardDecodeCapability.is1000kAdapted = ((adapterd & 4) == 4) ? true : false;
		mHardDecodeCapability.is350kAdapted = ((adapterd & 2) == 2) ? true : false;
		mHardDecodeCapability.is180kAdapted = ((adapterd & 1) == 1) ? true : false;
		
		mSoftDecodeCapability.isBlack = ((status & 8388608) == 8388608) ? true : false;
		mSoftDecodeCapability.isWhite = ((status & 4194304) == 4194304) ? true : false;
		mSoftDecodeCapability.isSupport1080p = ((status & 131072) == 131072) ? true : false;
		mSoftDecodeCapability.isSupport720p = ((status & 65536) == 65536) ? true : false;
		mSoftDecodeCapability.isSupport1300k = ((status & 32768) == 32768) ? true : false;
		mSoftDecodeCapability.isSupport1000k = ((status & 16384) == 16384) ? true : false;
		mSoftDecodeCapability.isSupport350k = ((status & 8192) == 8192) ? true : false;
		mSoftDecodeCapability.isSupport180k = ((status & 4096) == 4096) ? true : false;
	}
	
	public int getHardDecodeState(){
		return hardDecodeState;
	}
	
	
	
	@Override
	public String toString() {
		return "hardDecodeCapability("
				+ mHardDecodeCapability.toString() + "), softDecodeCapability("
				+ mSoftDecodeCapability.toString() + ")";
	}	
}
