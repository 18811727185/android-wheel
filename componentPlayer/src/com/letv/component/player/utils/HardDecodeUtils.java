package com.letv.component.player.utils;

import android.util.Log;

import com.letv.component.player.hardwaredecode.CodecWrapper;

public class HardDecodeUtils
{

	/**
	 * 设备硬解能力
	 */
	private static int decodeProfile = -1;

	/**
	 * 支持硬解码的最低值(底层playerCore项目返回)
	 */
	private static final int HIGH_PROFILE_VALUE = 16;
	
	/**
	 * 硬解码所支持的最低Android版本为4.1(API Level 16)
	 */
	private static final int ANDROID_OS_API_LEVEL_HW_LIMIT = 16;

	/**
	 * 是否支持硬解码
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isSupportHWDecodeUseNative()
	{
		
		Log.d("lxb","isSupport");
		int get = getProfileUseNative();
		Log.d("lxb","get="+get);
		return get >= HIGH_PROFILE_VALUE;//xuehui delete HW decoder
	}

	/**
	 * 一次性 赋值 profile
	 * 
	 * @param context
	 * @return
	 */
	private static int getProfileUseNative()
	{
		Log.d("lxb", "decodeProfile="+decodeProfile);
		if (decodeProfile == -1)
		{
			decodeProfile = getProfile();
		}
		return decodeProfile;
	}

	/**
	 * 根据得到解码等级profile值
	 * 
	 * @param context
	 * @return
	 */
	private synchronized static int getProfile()
	{
		return CodecWrapper.getCapbility();
	}
	
	/**
	 * 根据AVCLevel值
	 * 
	 */
	public static int getAVCLevel() {
		return CodecWrapper.getAVCLevel();
	}
}
