package com.letv.component.player.utils;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.letv.component.utils.MD5;

public class Tools {

	public static final int NETTYPE_NO = 0;
	public static final int NETTYPE_WIFI = 1;
	public static final int NETTYPE_2G = 2;
	public static final int NETTYPE_3G = 3;
	public static final int NETTYPE_4G = 4;
	
	/**
	 * 查看wifi是否打开，打开返回ture，关闭状态返回false
	 * 
	 * @param inContext
	 * @return
	 */
	public static boolean isWiFiConnected(Context context) {

		WifiManager mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
		int ipAddress = (wifiInfo == null) ? 0 : wifiInfo.getIpAddress();
		if (mWifiManager.isWifiEnabled() && ipAddress != 0) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isMobileConnected(Context context) {

		boolean isMobileConnected = false;
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		connMgr.getActiveNetworkInfo();
		NetworkInfo networkInfo = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (networkInfo != null) {
			isMobileConnected = networkInfo.isConnected();
		}
		return isMobileConnected;
	}

	public static int getNetType(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isAvailable()) {
			if (ConnectivityManager.TYPE_WIFI == networkInfo.getType()) {
				return NETTYPE_WIFI;
			} else {
				TelephonyManager telephonyManager = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);

				switch (telephonyManager.getNetworkType()) {
				case TelephonyManager.NETWORK_TYPE_GPRS:
				case TelephonyManager.NETWORK_TYPE_CDMA:
				case TelephonyManager.NETWORK_TYPE_EDGE:
					return NETTYPE_2G;
				case TelephonyManager.NETWORK_TYPE_LTE:
					return NETTYPE_4G;
				default:
					return NETTYPE_3G;
				}
			}
		} else {
			return NETTYPE_NO;
		}
	}
	
	public static String getNetTypeName(Context context) {
		String net = "无网";
		int type = getNetType(context);
		if(type == NETTYPE_WIFI) {
			net = "wifi";
		} else if(type == NETTYPE_4G) {
			net = "4G";
		} else if(type == NETTYPE_3G) {
			net = "3G";
		} else if(type == NETTYPE_2G) {
			net = "2G";
		} else if(type == NETTYPE_NO) {
			net = "无网";
		} 
		return net;
	}

	/**
	 * 获取当前程序的版本号
	 */
	public static int getVersionCode(Context context) {
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(),
					0);
			return info.versionCode;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * 获取当前程序的版本号
	 */
	public static String getVersionName(Context context) {
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(),
					0);
			return info.versionName;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 得到设备名字
	 * */
	public static String getDeviceName() {
		String model = android.os.Build.MODEL;
		if (model == null || model.length() <= 0) {
			return "";
		} else {
			return model;
		}
	}

	/**
	 * 得到品牌名字
	 * */
	public static String getBrandName() {
		String brand = android.os.Build.BRAND;
		if (brand == null || brand.length() <= 0) {
			return "";
		} else {
			return brand;
		}
	}

	/**
	 * 得到操作系统版本号
	 */
	public static String getOSVersionName() {
		return android.os.Build.VERSION.RELEASE;
	}

	/**
	 * 获取设备唯一标识
	 * 
	 * @param context
	 * @return
	 */
	public static String generateDeviceId(Context context) {
		String str = getIMEI(context) + getIMSI(context) + getDeviceName()
				+ getBrandName() + getMacAddress(context);
		return MD5.toMd5(str);
	}

	/**
	 * 获取uuid
	 * 
	 * @param context
	 * @return
	 */
	public static String getUUID(Context context) {
		return generateDeviceId(context) + "_" + System.currentTimeMillis();
	}

	/**
	 * 获取移动设备国际识别码
	 * 
	 * @param context
	 * @return
	 */
	public static String getIMEI(Context context) {
		if (context == null) {
			return "";
		}
		try {
			String deviceId = ((TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
			if (null == deviceId || deviceId.length() <= 0) {
				return "";
			} else {
				return deviceId.replace(" ", "");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 获取国际移动用户识别码
	 * 
	 * @param context
	 * @return
	 */
	public static String getIMSI(Context context) {
		if (context == null) {
			return "";
		}
		String subscriberId = null;

		try {
			subscriberId = ((TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE))
					.getSubscriberId();
			if (null == subscriberId || subscriberId.length() <= 0) {
				subscriberId = generate_DeviceId(context);
			} else {
				subscriberId.replace(" ", "");
				if (TextUtils.isEmpty(subscriberId)) {
					subscriberId = generate_DeviceId(context);
				}
			}
			return subscriberId;
		} catch (Exception e) {
			e.printStackTrace();
			return subscriberId;
		}
	}

	private static String generate_DeviceId(Context context) {
		String str = getIMEI(context) + getDeviceName() + getBrandName()
				+ getMacAddress(context);
		return MD5.toMd5(str);
	}

	/**
	 * 获取MAC地址
	 * 
	 * @param context
	 * @return
	 */
	public static String getMacAddress(Context context) {
		if (context == null) {
			return "";
		}
		try {
			String macAddress = null;
			WifiInfo wifiInfo = ((WifiManager) context
					.getSystemService(Context.WIFI_SERVICE))
					.getConnectionInfo();
			macAddress = wifiInfo.getMacAddress();
			if (macAddress == null || macAddress.length() <= 0) {
				return "";
			} else {
				return macAddress;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * 分辨率
	 * @param context
	 * @return
	 */
	public static String getResolution(Context context) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		return new StringBuilder().append(dm.widthPixels).append("*")
				.append(dm.heightPixels).toString();
	}
	/**
	  * 获取当前时间
	  * 
	  * @return返回字符串格式 yyyy-MM-dd HH:mm:ss
	  */
	public static String getCurrentDate() {
	  Date currentTime = new Date();
	  long time = currentTime.getTime();
	  SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	  String dateString = formatter.format(currentTime);
	  return dateString+"("+time+")";
	}
}
