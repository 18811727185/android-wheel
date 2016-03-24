package com.letv.mobile.core.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

public class SystemUtil {

    private static final String TAG = "SystemUtil";
    private static final String LETVRELEASEVERSION = "ro.letv.release.version";
    private static final String LOCALMAC = "net.local.mac";
    private static final String HWADDR = "HWaddr";
    private static final String IFCONFIG = "busybox ifconfig";
    private static final String WIRED = "wired";
    private static final String WIFI = "wifi";
    private static final String ETH0 = "eth0";
    private static final String WLAN0 = "wlan0";
    private static final String SYSADDRESS = "/sys/class/net/eth0/address";
    private static final String GET_PRODUECT_NAME_CMD = "getprop net.hostname";
    private static String sMac = null;

    public static String ForMatterTime(String mTime) {
        return null;
    }

    /*
     * get version code of some package
     * @param ctx context
     * packageName the package name, for example, "com.letv.tv"
     * @return version code
     */
    public static String getPackageName(Context ctx) {
        if (ctx != null) {
            return ctx.getPackageName();
        }

        return "";
    }

    /*
     * 获取版本号
     * @return version code
     */
    public static int getVersionCode(Context ctx) {
        try {
            PackageInfo packInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            return packInfo.versionCode;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /*
     * 获取版本名称
     * @return version name
     */
    public static String getVersionName(Context ctx) {
        try {
            PackageInfo packInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            return packInfo.versionName;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /*
     * get version code of some package
     * @param ctx context
     * packageName the package name, for example, "com.letv.tv"
     * @return version code
     */
    public static int getVersionCode(Context ctx, final String packageName) {
        try {
            PackageInfo packInfo = ctx.getPackageManager().getPackageInfo(packageName, 0);
            return packInfo.versionCode;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /*
     * get version name of some package
     * @param ctx context
     * packageName the package name, for example, "com.letv.tv"
     * @return version name
     */
    public static String getVersionName(Context ctx, final String packageName) {
        try {
            PackageInfo packInfo = ctx.getPackageManager().getPackageInfo(packageName, 0);
            return packInfo.versionName;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * 获取操作系统版本4.0.4
     * @return
     */
    public static String getOSVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取SDK version
     * @return SDK version
     */
    public static int getAndroidSDKVersion() {
        int version;
        try {
            version = Build.VERSION.SDK_INT;
        } catch (NumberFormatException e) {
            version = 0;
        }
        return version;
    }

    /**
     * 用户类型（1.4以前为：设备唯一标示）
     * 1.4以前为did，1.4版后变更为user_flag
     * 统计版本1.4后修改为用户类型。如果是新增用户上报n 如果是升级用户上报u 如果是活跃用户上报 - （不区分大小写）
     */
    public static String getUserType(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            return tm.getDeviceId();
        } else {
            return "";
        }
    }

    /*
     * 当前网络信息
     */
    public static NetworkInfo getAvailableNetWorkInfo(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null && activeNetInfo.isAvailable()) {
            return activeNetInfo;
        } else {
            return null;
        }
    }

    /*
     * 当前网络是否可用
     */
    public static boolean isNetworkAvailable(Context context) {
        boolean netSataus = false;
        ConnectivityManager cwjManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cwjManager.getActiveNetworkInfo();
        if (info != null) {
            netSataus = info.isAvailable() || info.isConnected();
        }
        return netSataus;
    }

    /**
     * 获取Rom版本号
     * @return
     */
    public static String getRomVersion() {
        return getSystemProperty(LETVRELEASEVERSION);
    }

    /*
     * 获取mac地址
     */
    public static String getMacAddress() {// get mac from share
        if (sMac != null) {
            return sMac;
        }

        String result = getSystemProperty(LOCALMAC);
        if (TextUtils.isEmpty(result)) {
            result = newGetMacAddress();
            if (TextUtils.isEmpty(result)) {
                String Mac;
                result = callCmd(IFCONFIG, HWADDR);
                if (!StringUtils.equalsNull(result) && result.length() > 0) {
                    Mac = result.substring(result.indexOf(HWADDR) + 6, result.length() - 1);
                    if (Mac.length() > 1) {
                        Mac = Mac.replaceAll(" ", "");
                        result = "";
                        String[] tmp = Mac.split(":");
                        for (int i = 0; i < tmp.length; ++i) {
                            result += tmp[i];
                        }
                    }
                }
            }
        }

        if (StringUtils.isBlank(result)) {
            sMac = "";
            return sMac;
            // NOTE(qingxia): Sometimes the devices may not have mac.
            // throw new RuntimeException("getMacAddress() ==> result IS NULL");
        }

        sMac = result.toUpperCase();
        return sMac;
    }

    private static String getMacAddressFromWifiManager() {
        String macAddress = "";
        try {
            WifiManager wifiManager = (WifiManager) ContextProvider.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            if (wifiInfo == null) {
                return "";
            }

            macAddress = wifiInfo.getMacAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return macAddress;
    }

    /**
     * 新获取Mac地址方法
     * @return
     */
    private static String newGetMacAddress() {
        String result = "";
        String Mac = "";
        String str1 = SYSADDRESS;
        String str2;
        FileReader fr = null;
        BufferedReader localBufferedReader = null;
        try {
            fr = new FileReader(str1);
            localBufferedReader = new BufferedReader(fr, 8192);
            while ((str2 = localBufferedReader.readLine()) != null) {
                Mac = str2;
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } finally {
            try {
                if (localBufferedReader != null) {
                    localBufferedReader.close();
                }
                if (fr != null) {
                    fr.close();
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }

        // 如果返回的Mac == null，则说明网络不可取
        if (StringUtils.isBlank(Mac)) {
            return null;
        }
        if (Mac.length() > 1) {
            String[] tmp = Mac.split(":");
            for (int i = 0; i < tmp.length; ++i) {
                result += tmp[i];
            }
        }
        return StringUtils.stringChangeCapital(result);
    }

    public static String getLocalIP() {
        String ip = "";
        try {
            Enumeration<?> e1 = NetworkInterface.getNetworkInterfaces();
            while (e1.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) e1.nextElement();
                if (!ni.getName().equals(ETH0)) {
                } else {
                    Enumeration<?> e2 = ni.getInetAddresses();
                    while (e2.hasMoreElements()) {
                        InetAddress ia = (InetAddress) e2.nextElement();
                        if (ia instanceof Inet6Address) {
                            continue;
                        }
                        ip = ia.getHostAddress();
                    }
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ip;
    }

    /*
     * 获取本地的IP地址
     * @author xuyi
     */
    public static String getLocalIpAddress() {
        String ipaddress = "";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                    .hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                if (intf.getName().toLowerCase().equals(ETH0)
                        || intf.getName().toLowerCase().equals(WLAN0)) {// 仅过滤无线和有线的ip
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                            .hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            String ip = inetAddress.getHostAddress();
                            if (!ip.contains("::")) {// 过滤掉ipv6的地址
                                return ip;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ipaddress;
    }

    public static String callCmd(String cmd, String filter) {
        String result = "";
        String line;
        try {
            Process proc = Runtime.getRuntime().exec(cmd);
            InputStreamReader is = new InputStreamReader(proc.getInputStream());
            BufferedReader br = new BufferedReader(is);
            // 执行命令cmd，只取结果中含有filter的这一行
            while ((line = br.readLine()) != null && !line.contains(filter)) {
            }
            result = line;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取联网方式
     * @param context
     * @return
     */
    public static String getNetType(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            String typeName = info.getTypeName().toLowerCase(Locale.getDefault()); // WIFI/MOBILE
            if (typeName.equals(WIFI)) {
            } else {
                // typeName = info.getExtraInfo().toLowerCase();
                typeName = info.getTypeName().toLowerCase(Locale.getDefault());
            }
            return typeName;
        } catch (Exception e) {
            return "-";
        }
    }

    /**
     * 根据进程名称获取进程id
     * @param name
     *            进程名
     * @return
     */
    public static int getPidByProcessName(Context context, String name) {
        if (StringUtils.isBlank(name)) {
            return -1;
        }
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        // 通过调用ActivityManager的getRunningAppProcesses()方法获得系统里所有正在运行的进程
        List<ActivityManager.RunningAppProcessInfo> appProcessList = mActivityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.processName.equals(name)) {
                return appProcess.pid;
            }
        }
        return -1;
    }

    /**
     * 判断当前应用是否在前台运行
     * @return true 前台运行
     *         false 后台运行
     */
    public static boolean isAppRunningForeground() {
        ActivityManager am = (ActivityManager) ContextProvider.getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= 21) {
            List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();

            if (processes == null || processes.size() <= 0) {
                return false;
            }

            for (RunningAppProcessInfo info : processes) {
                if (ContextProvider.getApplicationContext().getPackageName()
                        .equals(info.processName)) {
                    return !(info.importance > RunningAppProcessInfo.IMPORTANCE_VISIBLE);
                }
            }
            return false;
        } else {
            List<RunningTaskInfo> runningTaskInfo = am.getRunningTasks(1);
            if (runningTaskInfo == null || runningTaskInfo.size() == 0) {
                return false;
            }

            ComponentName cn = runningTaskInfo.get(0).topActivity;
            String currentPackageName = cn.getPackageName();
            return !TextUtils.isEmpty(currentPackageName)
                    && currentPackageName.equals(ContextProvider.getApplicationContext()
                            .getPackageName());

        }
    }

    /**
     * 从Android的system property里取值 (使用了反射调用隐藏API SystemProperties.get())
     * @param key
     *            property键值
     * @return
     *         'key'对应的property值.如果没有该property则返回空字符串，如果调用隐藏API失败则抛出RuntimeExcep
     *         t
     *         i
     *         o
     *         n
     */
    public static String getSystemProperty(String key) {
        try {
            Class<?> threadClazz = Class.forName("android.os.SystemProperties");
            Method method = threadClazz.getMethod("get", String.class, String.class);
            return (String) method.invoke(null, key, "");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getDeviceName() {
        return callCmd(GET_PRODUECT_NAME_CMD, "");
    }

    /**
     * !! 暂时不使用该方法, 改用getSystemProperty()
     * 从Android的
     * system property里取值 (使用了反射调用隐藏API SystemProperties.get())
     * @param key
     *            property键值
     * @return
     *         'key'对应的property值.如果没有该property则返回空字符串，如果调用隐藏API失败则抛出RuntimeExcep
     *         t
     *         i
     *         o
     *         n
     */
    // public static String getSystemProperty_reflect(String key) {
    // String property = null;
    // try {
    // Class<?> systemPropertyClass = Class
    // .forName("android.os.SystemProperties");
    // Method getMethod = systemPropertyClass.getDeclaredMethod("get",
    // String.class, String.class);
    // property = (String) getMethod.invoke(null, key, "");
    // } catch (ClassNotFoundException e) {
    // logger.e("Exception when invoke SystemProperties: " + e);
    // } catch (NoSuchMethodException e) {
    // logger.e("Exception when invoke SystemProperties: " + e);
    // } catch (InvocationTargetException e) {
    // logger.e("Exception when invoke SystemProperties: " + e);
    // } catch (IllegalAccessException e) {
    // logger.e("Exception when invoke SystemProperties: " + e);
    // }
    // if (property == null) {
    // throw new RuntimeException(
    // "Error invoking hidden API SystemProperties");
    // }
    // return property;
    // }

    /**
     * check apk is exists
     */
    public static boolean isAppExists(String packageName) {
        PackageManager manager = ContextProvider.getApplicationContext().getPackageManager();
        try {
            manager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
