package com.letv.component.player.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

/**
 * 内存信息工具类
 * @author Administrator
 */
public class MemoryInfoUtil {

    private static final String TAG = "MemoryInfoUtil";

    /**
     * 获得可用内存
     * @param context
     * @return
     */
    public static long getMemUnused(Context context) {
        long memUnused = 0;

        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);

        MemoryInfo memoryInfo = new MemoryInfo();

        am.getMemoryInfo(memoryInfo);

        memUnused = memoryInfo.availMem / 1024;

        return memUnused;
    }
    
    public static long getTotalInternalMemorySize() {   
        File path = Environment.getDataDirectory();   
        StatFs stat = new StatFs(path.getPath());   
        long blockSize = stat.getBlockSize();   
        long totalBlocks = stat.getBlockCount();   
        return totalBlocks * blockSize;   
    }  

    /**
     * 获得总内存
     * @return
     */
    public static long getMemTotal() {

        // /proc/meminfo读出的内核信息进行解释
        String path = "/proc/meminfo";
        String content = null;
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(path), 8);
            String line;
            if ((line = br.readLine()) != null) {
                content = line;
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "", e);
        } catch (IOException e) {
            Log.e(TAG, "", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        int begin = content.indexOf(':');
        int end = content.indexOf('k');

        // 截取字符串信息
        content = content.substring(begin + 1, end).trim();

        return Integer.parseInt(content);
    }
    
    public static long getSDCardMemory() {   
//        long[] sdCardInfo=new long[2];   
    	Long sdCardInfo = (long) 0;
        String state = Environment.getExternalStorageState();   
        if (Environment.MEDIA_MOUNTED.equals(state)) {   
            File sdcardDir = Environment.getExternalStorageDirectory();   
            StatFs sf = new StatFs(sdcardDir.getPath());   
            long bSize = sf.getBlockSize();   
            long bCount = sf.getBlockCount();   
            long availBlocks = sf.getAvailableBlocks();   
  
           sdCardInfo = bSize * bCount;//总大小   
//            sdCardInfo[1] = bSize * availBlocks;//可用大小   
        }
		return sdCardInfo;   
    } 
}
