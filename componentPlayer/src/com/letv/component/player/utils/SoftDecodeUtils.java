package com.letv.component.player.utils;

public class SoftDecodeUtils {

	private static final String TAG = "StreamUtils";
	
	private static final int CPU_CORE_NUM_ONE = 1; //单核
	private static final int CPU_CORE_NUM_TWO = 2; //双核
	private static final int CPU_CORE_NUM_FOUR = 4; //四核
	
	private static final long CPU_FREQUENCE_LIMINT_1000MHZ = 1000000L; // 1GHz
	private static final long CPU_FREQUENCE_LIMINT_1500MHZ = 1500000L; //1.5GHz
	private static final long CPU_FREQUENCE_LIMINT_1800MHZ = 1800000L; //1.8GHz


	/**
	 * 根据手机配置，判断是否支持180k码流播放
	 * 支持条件：双核
	 */
	public static boolean isSupport180k() {
		if ((CpuInfosUtils.getNumCores() >= CPU_CORE_NUM_TWO)
				&& CpuInfosUtils.ifSupportNeon()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 根据手机配置，判断是否支持350k码流播放
	 * 支持条件：双核 1GHz以上
	 */
	public static boolean isSupport350k() {
		if ((CpuInfosUtils.getNumCores() >= CPU_CORE_NUM_TWO && CpuInfosUtils
				.getMaxCpuFrequence() >= CPU_FREQUENCE_LIMINT_1000MHZ)
				&& CpuInfosUtils.ifSupportNeon()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 根据手机配置，判断是否支持1000k码流播放
	 * 支持条件：双核 1.5GHz以上
	 */
	public static boolean isSupport1000k() {
		if ((CpuInfosUtils.getNumCores() >= CPU_CORE_NUM_TWO && CpuInfosUtils
				.getMaxCpuFrequence() >= CPU_FREQUENCE_LIMINT_1500MHZ)
				&& CpuInfosUtils.ifSupportNeon()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 根据手机配置，判断是否支持1300k码流播放
	 * 支持条件：四核 1.5GHz以上
	 */
	public static boolean isSupport1300k() {
		if ((CpuInfosUtils.getNumCores() >= CPU_CORE_NUM_FOUR && CpuInfosUtils
				.getMaxCpuFrequence() >= CPU_FREQUENCE_LIMINT_1500MHZ)
				&& CpuInfosUtils.ifSupportNeon()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 根据手机配置，判断是否支持720p码流播放
	 * 支持条件：默认不支持
	 */
	public static boolean isSupport720p() {
		return isSupport1300k();
	}
	
	/**
	 * 根据手机配置，判断是否支持1080p码流播放
	 * 支持条件：默认不支持
	 */
	public static boolean isSupport1080p() {
		return isSupport1300k();
	}

}
