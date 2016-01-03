package com.letv.component.player.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.regex.Pattern;

import android.text.TextUtils;
import android.util.Log;

public class NativeInfos {
	
	private static final String TAG = "NativeInfos";
	
	public static final int SUPPORT_MP4_LEVEL = 0;
	public static final int SUPPORT_TS350K_LEVEL = 1;
	public static final int SUPPORT_TS800K_LEVEL = 2;
	public static final int SUPPORT_TS1000K_LEVEL = 3;
	public static final int SUPPORT_TS1300K_LEVEL = 4;
	public static final int SUPPORT_TS180K_LEVEL = 5;
	public static final int SUPPORT_TS720P_LEVEL = 6;
	public static final int SUPPORT_TS1080P_LEVEL = 7;

	private static final int CPU_Core_Nums_Four = 4;

	private static final int CPU_Core_Nums_Eight = 8;
	public static boolean mOffLinePlay = false;
	public static boolean mIfNative3gpOrMp4 = false;
	public static boolean mIsLive = false;
	
	public static String CPUFeatures = null;
	
	public static String CPUClock = null;
	
	public static String getCPUFeatures() {
		
		if(!TextUtils.isEmpty(CPUFeatures)) {
			return CPUFeatures;
		}
		
        String cmd = "cat /proc/cpuinfo";
        Map<String, Object> map = NativeCMD.runCmd(cmd);
        if (map == null) {
        	CPUFeatures = "Sorry, Run Cmd Failure !!!";
            return CPUFeatures;
        } else {
            InputStream in = (InputStream) map.get("input");
            InputStreamReader is = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(is);
            StringBuilder sb = new StringBuilder();
            String line = "";
            try {
                while ((line = br.readLine()) != null) {
                    if (line.indexOf("Features") != -1)
                    	sb.append(line);
                }
            } catch (Exception e) {
            	CPUFeatures = "Read InputStream Failure !!!";
                return CPUFeatures;
            } finally {
                try {
                    if (br != null)
                        br.close();
                } catch (IOException e) {
                }
                try {
                    if (is != null)
                        is.close();
                } catch (IOException e) {
                }
                try {
                    if (in != null)
                        in.close();
                } catch (IOException e) {
                }
            }
            
            CPUFeatures = sb.toString();
            
            return CPUFeatures;
        }
	}
	 
	 public static String getCPUClock() throws Exception{
		 
		 if(!TextUtils.isEmpty(CPUClock)) {
			 return CPUClock;
		 }
		 
		 String cmd = "cat /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq";
	        Map<String, Object> map = NativeCMD.runCmd(cmd);
	        if (map == null) {
	        	CPUClock = "Sorry, Run Cmd Failure !!!";
	            return CPUClock;
	        } else {
	            InputStream in = (InputStream) map.get("input");
	            InputStreamReader is = new InputStreamReader(in);
	            BufferedReader br = new BufferedReader(is);
	            StringBuilder sb = new StringBuilder();
	            String line = "";
	            try {
	                while ((line = br.readLine()) != null) {
	                    	sb.append(line);
	                }
	            } catch (Exception e) {
	            	CPUClock = "Read InputStream Failure !!!";
	                return CPUClock;
	            } finally {
	                try {
	                    if (br != null)
	                        br.close();
	                } catch (IOException e) {
	                }
	                try {
	                    if (is != null)
	                        is.close();
	                } catch (IOException e) {
	                }
	                try {
	                    if (in != null)
	                        in.close();
	                } catch (IOException e) {
	                }
	            }
	            
	            CPUClock = sb.toString();
	            
	            return CPUClock;
	     }
	 }
	 
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
	 
	 public static boolean ifSupportVfpOrNeon(){
		 String ret = getCPUFeatures();		 
		 if (ret.indexOf("neon") != -1 || ret.indexOf("vfp") != -1 || ret.indexOf("asimd") != -1)//乐视手机用asimd判断
			 return true;
		 else
			 return false;
	 }

//	 public static boolean ifSupportNeon(){
//		 if (getCPUFeatures().indexOf("neon") != -1)
//			 return true;
//		 else
//			 return false;
//	 }
	 
	 /**
	  * cpu频率分级  从5.7后此方法只用于判断支持m3u8还是mp4
	  * */
	 public static int getSupportLevel(){
		 //Log.i(TAG, "这里走了NativeInfos。。。。。。。。。。");
		int cpuClock = 0;
		int NumCores=0;
		int ret = SUPPORT_MP4_LEVEL;
		try {
			cpuClock = Integer.parseInt(getCPUClock());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try{
			NumCores=getNumCores();
		}catch(Exception eu){
			eu.printStackTrace();
		}
      if(NumCores<CPU_Core_Nums_Four)
       {
		if (cpuClock < 900000) {                              // 小于900Mhz, support mp4
			ret = SUPPORT_MP4_LEVEL;
		} else if (cpuClock >= 900000 && cpuClock < 1200000) { // 900Mhz-1.2Ghz, support 350k TS
			ret = SUPPORT_TS350K_LEVEL;
		} else if (cpuClock >= 1200000 && cpuClock < 1536000) { // 1.2Ghz-1.536Ghz, support 1000k TS
			ret = SUPPORT_TS1000K_LEVEL;
		} else {                                                // 大于1.536Ghz, support 1300k TS
			ret = SUPPORT_TS1300K_LEVEL;
		}
       }else if(NumCores>=CPU_Core_Nums_Four && NumCores<CPU_Core_Nums_Eight){
    	   
    	   if(cpuClock<1000000){
    		   ret=SUPPORT_TS180K_LEVEL;
    	   }else if(cpuClock >= 1000000 && cpuClock < 1100000){
    		   ret = SUPPORT_TS350K_LEVEL;
    	   }else if(cpuClock >= 1100000 && cpuClock < 1200000){
    		   ret = SUPPORT_TS1000K_LEVEL;
    	   }else if(cpuClock >= 1200000 && cpuClock < 1600000){
    		   ret = SUPPORT_TS1300K_LEVEL;
    	   }else{
    		   ret = SUPPORT_TS720P_LEVEL;
    	   }
       }else{
    	   if(cpuClock>=2000000){
    		   ret = SUPPORT_TS1080P_LEVEL;
    	   }else{
    		   ret = SUPPORT_TS720P_LEVEL;
    	   }
       }
		return ret;
	 }
	 
	 public static boolean ifNativePlayer(){
		 boolean ret;
		 if(mIsLive) {
			 ret = true;
		 } else if (getSupportLevel() == SUPPORT_MP4_LEVEL || (mOffLinePlay && mIfNative3gpOrMp4) || !ifSupportVfpOrNeon()) {
			 ret = false;
		 } else{
			 ret = true;
		 }
		 
		 return ret;
	 }
	 
	 public static void doWithNativePlayUrl(String url){
		url = url.toLowerCase();
		if (url.indexOf(".letv") != -1 || url.indexOf(".3gp") != -1 ||
			url.indexOf(".mp4") != -1){
			mIfNative3gpOrMp4 = true;    		
		}else if(url.startsWith("content")){
			mIfNative3gpOrMp4 = true;
		}else{
			mIfNative3gpOrMp4 = false;
		}
	 }
}
