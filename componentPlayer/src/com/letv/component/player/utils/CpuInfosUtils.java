package com.letv.component.player.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import android.util.Log;

public class CpuInfosUtils {

	private static final String TAG = "CpuInfosUtils";

	/**
	 * 获取Cpu核数
	 * 
	 * @return
	 */
	public static int getNumCores() {
		// Private Class to display only CPU devices in the directory listing
		class CpuFilter implements FileFilter {
			@Override
			public boolean accept(File pathname) {
				// Check if filename is "cpu", followed by a single digit number
				if (Pattern.matches("cpu[0-9]", pathname.getName())) {
					return true;
				}
				return false;
			}
		}

		try {
			// Get directory containing CPU info
			File dir = new File("/sys/devices/system/cpu/");
			// Filter to only list the devices we care about
			File[] files = dir.listFiles(new CpuFilter());
			// Return the number of cores (virtual CPU devices)
			return files.length;
		} catch (Exception e) {
			// Print exception
			e.printStackTrace();
			// Default to return 1 core
			return 1;
		}
	}
	
	/**
	 * 获取cpu当前主频
	 * @return	单位 GHZ
	 * @modify cpufreq/cpuinfo_cur_freq改成 scaling_cur_freq  by wangshenhao 2015/1/16 14:01
	 */

	public static float getCurCpuFrequence() {
		ProcessBuilder cmd;
		try {
//			String[] args = { "/system/bin/cat",
//					"/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_cur_freq" };
			String[] args = { "/system/bin/cat",
			"/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq" };
			cmd = new ProcessBuilder(args);

			Process process = cmd.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line = reader.readLine();
			if(line != null) {
				return Long.parseLong(line)/1000000f;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.i(TAG, "获取cpu当前主频发生错误。");
			return getMaxCpuFrequence();
		}
		return 0;
	}

	/**
	 * 获取cpu最大主频
	 * 
	 * @return
	 */
	public static float getMaxCpuFrequence() {
		ProcessBuilder cmd;
		try {
			String[] args = { "/system/bin/cat",
					"/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" };
			cmd = new ProcessBuilder(args);

			Process process = cmd.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line = reader.readLine();
			if(line != null) {
				return Long.parseLong(line)/1000000f;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.i(TAG, "获取cpu最大主频发生错误。");
		}
		return 0;
	}

	/**
	 * 获取cpu最小频率
	 * 
	 * @return	单位 GHZ
	 */
	public static float getMinCpuFrequence() {
		ProcessBuilder cmd;
		try {
			String[] args = { "/system/bin/cat",
					"/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq" };
			cmd = new ProcessBuilder(args);

			Process process = cmd.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line = reader.readLine();
			LogTag.i(TAG, "cpu最小主频 =" + line);
			if(line != null) {
				return Long.parseLong(line)/1000000f;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			LogTag.i(TAG, "获取cpu最小主频发生错误。");
		}
		return 0;
	}

	/**
	 * 判断ARM是否支持NEON
	 * 
	 * @return
	 */
	public static boolean ifSupportNeon() {
		StringBuilder result = new StringBuilder();
		ProcessBuilder cmd;
		try {
			String[] args = { "/system/bin/cat", "/proc/cpuinfo" };
			cmd = new ProcessBuilder(args);

			Process process = cmd.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String l = null;
			while ((l = reader.readLine()) != null) {
				if (l.indexOf("Features") != -1) {
					result.append(l);
				}
			}
			LogTag.i(TAG, result.toString());
		} catch (IOException ex) {
			result.append("Read InputStream Failure !!!");
			ex.printStackTrace();
		}

		if (result.toString().indexOf("neon") != -1) {
			return true;
		} else {
			return false;
		}
	}
	
	
	 /**
     * 获得cpu型号
     * @param context
     * @return
     */
    public static String getCpuInfo() {   
        String str1 = "/proc/cpuinfo";   
        String str2="";   
        String[] cpuInfo={"",""};   
        String[] arrayOfString;   
        try {   
            FileReader fr = new FileReader(str1);   
            BufferedReader localBufferedReader = new BufferedReader(fr, 8192);   
            str2 = localBufferedReader.readLine();   
            arrayOfString = str2.split("\\s+");   
            for (int i = 2; i < arrayOfString.length; i++) {   
                cpuInfo[0] = cpuInfo[0] + arrayOfString[i] + " ";   
            }   
            str2 = localBufferedReader.readLine();   
            arrayOfString = str2.split("\\s+");   
            cpuInfo[1] += arrayOfString[2];   
            localBufferedReader.close();   
        } catch (IOException e) {   
        }   
        return cpuInfo[0].toString();   
    }  
}
