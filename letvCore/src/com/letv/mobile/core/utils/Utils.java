package com.letv.mobile.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.View;

import com.letv.mobile.core.log.Logger;

public final class Utils {
    private static final Logger sLogger = new Logger("Utils");
    public static final String sShotCutStartActivity = "."
            + "welcome.WelcomeActivity";
    // 最大任务数，用于checkApplicationRunningState方法中
    private static final int MAX_TASK_NUM = 100;
    /* Maximum pixels size for created bitmap: 8M */
    private static final int MAX_NUM_PIXELS = 1024 * 2048;
    private static final int UNCONSTRAINED = -1;
    /*
     * Render failed if exceeds 2048 on Android 4.1.1 Solutions: 1) Limit image
     * size; 2)
     * Disable hardware acceleration. Use the 2nd solution here.
     */
    private static final int MAX_IMAGE_SIZE = Integer.MAX_VALUE;
    private static final String NULL_STRING = "null";

    /**
     * Check the url is valid or not.
     * @param url
     *            the url need check
     * @return boolean
     */
    public static boolean isValidUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * 截取字符串中数字 注意，返回值是字符串中出现的第一串数字。可能会抛运行时异常。 例如在content中没有数字的情况下，会抛invalidInt。
     * @param content
     * @return 字符串中出现的第一串数字。
     */
    public static int getNumbersFromStr(String content) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(0));
        }
        return Integer.parseInt("");
    }

    /**
     * 把十六进制的String转为int。 如"3f3f3f"->4144959
     * @param hexStr
     * @return
     * @throws Exception
     */
    public static int hexStr2Integer(String hexStr) {
        Map<Character, Integer> map = new HashMap<Character, Integer>();

        map.put('0', 0);
        map.put('1', 1);
        map.put('2', 2);
        map.put('3', 3);
        map.put('4', 4);
        map.put('5', 5);
        map.put('6', 6);
        map.put('7', 7);
        map.put('8', 8);
        map.put('9', 9);
        map.put('a', 10);
        map.put('b', 11);
        map.put('c', 12);
        map.put('d', 13);
        map.put('e', 14);
        map.put('f', 15);
        map.put('A', 10);
        map.put('B', 11);
        map.put('C', 12);
        map.put('D', 13);
        map.put('E', 14);
        map.put('F', 15);

        int result = 0;
        String tmpString = hexStr;
        if ('#' == tmpString.charAt(0)) {
            tmpString = hexStr.substring(1, tmpString.length());
        }
        for (int i = 0; i < tmpString.length(); i++) {
            result <<= 4;
            if (null == map.get(tmpString.charAt(i))) {
                throw new InvalidParameterException(
                        "hexStr2Integer invalid parameter");
            }
            result += map.get(tmpString.charAt(i));
        }
        return result;
    }

    /**
     * 检测是否已经创建了快捷方式
     * @param context
     * @param shortCutName
     *            the application name
     *            数据库快捷方式表在packages/apps/Launcher2/src/com/android/launcher2
     *            /LauncherProvider.java
     * @return
     */
    public static boolean hasShortCut(Context context, String shortCutName) {
        String url = "content://com.android.launcher.settings/favorites?notify=true";
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Uri.parse(url), null,
                "title=? and iconPackage=?", new String[] { shortCutName,
                        context.getPackageName() }, null);

        if (null == cursor || !cursor.moveToFirst()) {
            url = "content://com.android.launcher2.settings/favorites?notify=true";
            cursor = resolver.query(Uri.parse(url), null,
                    "title=? and iconPackage=?", new String[] { shortCutName,
                            context.getPackageName() }, null);
        }

        if (cursor != null) {
            boolean haved = cursor.moveToFirst();
            cursor.close();
            return haved;
        }
        return false;
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     * @param spValue
     * @param fontScale
     *            （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int sp2px(float spValue) {
        final float fontScale = ContextProvider.getApplicationContext()
                .getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 将dip值转换为px值
     * @param paramContext
     * @param paramFloat
     *            （dip 值）
     * @return
     */
    public static int dip2px(Context paramContext, float paramFloat) {
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        ((Activity) paramContext).getWindowManager().getDefaultDisplay()
                .getMetrics(localDisplayMetrics);
        return (int) FloatMath.ceil(paramFloat * localDisplayMetrics.density);
    }

    /**
     * 将px值转换为dip值
     * @param paramContext
     * @param paramInt
     *            （px 值）
     * @return
     */
    public static float px2dip(Context paramContext, int paramInt) {
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        ((Activity) paramContext).getWindowManager().getDefaultDisplay()
                .getMetrics(localDisplayMetrics);
        if (localDisplayMetrics.density > 0) {
            return paramInt / localDisplayMetrics.density;
        } else {
            return paramInt;
        }
    }

    /**
     * 删除对应启动界面名对应名称的快捷方式。
     * @param context
     * @param startClass
     *            快捷方式启动的界面。
     * @param shortCutName
     *            快捷方式名称。
     */
    public static void delShortCut(Context context, Class<?> startClass,
            String shortCutName) {
        Intent shortcut = new Intent(
                "com.android.launcher.action.UNINSTALL_SHORTCUT");
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortCutName);

        // 不能用intent.setAction("xxx").setComponent来设置intent。这样会有手机不能启动。
        Intent startIntent = new Intent(context, startClass).setAction(
                "android.intent.action.MAIN").addCategory(
                "android.intent.category.LAUNCHER");
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, startIntent);

        context.sendBroadcast(shortcut);
    }

    /**
     * @param context
     * @param startClass
     *            要开启的界面的class
     * @param shortCutName
     *            快捷方式名称
     * @param shortCutResId
     *            快捷方式图标的资源id。
     * @param coverOld
     *            有快捷方式的情况下，coverOld == true，覆盖原快捷方式；coverOld == false，取消创建；
     */
    public static void addShortcut(Context context, Class<?> startClass,
            String shortCutName, int shortCutResId, boolean coverOld) {
        if (Utils.hasShortCut(context, shortCutName)) {
            if (!coverOld) {
                return;
            } else {
                delShortCut(context, startClass, shortCutName);
            }
        }
        Intent shortcut = new Intent(
                "com.android.launcher.action.INSTALL_SHORTCUT");
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortCutName);
        shortcut.putExtra("duplicate", false);

        Intent startIntent = new Intent(context, startClass).setAction(
                "android.intent.action.MAIN").addCategory(
                "android.intent.category.LAUNCHER");
        // 不能用intent.setAction("xxx").setComponent来设置intent。这样会有手机不能启动。
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, startIntent);
        ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(
                context, shortCutResId);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);

        context.sendBroadcast(shortcut);
    }

    /**
     * 把bitmap转为byte数组。
     * @param bm
     * @return 如果bm==null， return 空数组。
     */
    public static byte[] bitmap2bytes(Bitmap bm) {
        if (null == bm) {
            return new byte[] {};
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] result = baos.toByteArray();
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 在桌面创建快捷方式到指定activity。
     * @param activity
     * @param startActivity
     *            点击快捷方式要启动的activity.
     */
    // NOTE(chuanbei）桌面长按-》快捷方式 这里有微博订阅的时候，在判断有没有快捷方式的时候，
    // 有些手机一直判断不成功，有些手机一直判断成功。先不用。
    // TODO(chuanbei): This function should not depend on WelcomeActivity
    // public static void addShortcut(Activity activity) {
    // if (Utils.hasShortCut(activity))
    // return;
    // Intent addShortcut;
    // addShortcut = new Intent();
    // addShortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME,
    // activity.getString(R.string.app_name));
    // Parcelable icon = Intent.ShortcutIconResource.fromContext(activity,
    // R.drawable.yunyun_icon);
    // addShortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
    // // 不能用intent.setAction("xxx").setComponent来设置intent。这样会有手机不能启动。
    // addShortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(activity,
    // WelcomeActivity.class)
    // .setAction("android.intent.action.MAIN").addCategory("android.intent.category.LAUNCHER"));
    // activity.setResult(Activity.RESULT_OK, addShortcut);
    // }

    /**
     * Check a Object array is empty or not.
     * @param objs
     * @return
     */
    public static boolean isArrayEmpty(Object[] objs) {
        if (objs == null || objs.length == 0) {
            return true;
        }

        boolean isEmpty = true;
        for (Object obj : objs) {
            if (obj != null) {
                isEmpty = false;
                break;
            }
        }

        return isEmpty;
    }

    /**
     * Check two string is equals or not.
     * @param str
     * @return boolean
     */
    public static boolean isStringEquals(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }

        if (str1 != null && str2 == null) {
            return str1.equals(str2);
        }

        if (str2 != null && str1 == null) {
            return str2.equals(str1);
        }

        return str1.equals(str2);
    }

    /**
     * Check a string is empty or not.
     * @param str
     * @return boolean
     */
    public static boolean isStringEmpty(String str) {
        return str == null || str.length() == 0 || str.trim().length() == 0;
    }

    /**
     * Check a string is empty or is "null".
     * @param str
     * @return boolean
     */
    public static boolean isStrValid(String str) {
        return !isStringEmpty(str) && !"null".equals(str);
    }

    /**
     * 对json数据进行处理，得到处理后的字符串，当数据为字符串“null”时，返回null
     * @param rawString
     *            未经处理的字符串
     * @return 处理后的字符串
     */
    public static String getStringFromJson(String rawString) {
        if (rawString == null || rawString.equals(NULL_STRING)) {
            return null;
        }
        return rawString;
    }

    /**
     * If a object is null, it must be transfer to a json data with
     * JSONObject.NULL.
     * @param obj
     * @return
     */
    public static Object getJsonObject(Object obj) {
        if (obj == null) {
            return JSONObject.NULL;
        } else {
            return obj;
        }
    }

    /**
     * 找到一个字符串里面的所有img标签中的url。
     * @param rawData
     * @return
     */
    public static List<String> findImageUrls(String rawData) {
        String patternImgStr = "<img[\\s]*[^>]+>";
        Pattern imgLabelPattern = Pattern.compile(patternImgStr);
        Matcher matcher = imgLabelPattern.matcher(rawData);
        List<String> imgUrls = new LinkedList<String>();
        while (matcher.find()) {
            imgUrls.add(matcher.group(0));
        }
        ArrayList<String> resultUrls = new ArrayList<String>();
        for (Iterator<String> iterator = imgUrls.iterator(); iterator.hasNext();) {
            resultUrls.add(iterator.next().replaceAll("<img.*?src=\"", "")
                    .replaceAll("\".*", ""));
        }
        return resultUrls;
    }

    /**
     * Decode a file path into a bitmap. If the bitmap is too large, it will be
     * scaled. If
     * the specified file name is null, or cannot be decoded into a bitmap, the
     * function
     * returns null.
     * @param pathName
     *            complete path name for the file to be decoded.
     * @return The decoded bitmap, or null if the image data could not be
     *         decoded
     */
    public static Bitmap decodeBitmap(String pathName) {
        // Try load normally
        try {
            return BitmapFactory.decodeFile(pathName);
        } catch (OutOfMemoryError e) {
            sLogger.e("Could not load large image" + e.getMessage());
        }

        // Try sample large image
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(pathName, opts);

            opts.inSampleSize = computeSampleSize(opts, UNCONSTRAINED,
                    MAX_NUM_PIXELS);
            opts.inJustDecodeBounds = false;

            return BitmapFactory.decodeFile(pathName, opts);
        } catch (OutOfMemoryError e) {
            sLogger.e("Could not load image" + e.getMessage());
        }
        return null;
    }

    public static int getVersionCode(Context context) {
        int versionCode = 0;
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi;
            pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            versionCode = pi.versionCode;
        } catch (NameNotFoundException e) {
            sLogger.e("get app version code error!" + e.getMessage());
            e.printStackTrace();
        }
        return versionCode;
    }

    public static String getVersionName(Context context) {
        String versionName = "";
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi;
            pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            versionName = pi.versionName;
        } catch (NameNotFoundException e) {
            sLogger.e("get app version name error! " + e.getMessage());
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * return a String represents the current date and time. For example,
     * 2012-08-22
     * 10:23:27:333
     * @return a String represents the current date and time.
     */
    public static String getTimeAsString() {
        DateFormat mFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        String time = mFormatter.format(new Date());
        return time;
    }

    /**
     * Start browser to open one url.
     * @param context
     * @param url
     */
    public static boolean startBrowser(Context context, String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(url);
        intent.setData(uri);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            sLogger.e(e.toString());
            return false;
        }
        return true;
    }

    /**
     * Compute the sample size as a function of minSideLength and
     * maxNumOfPixels.
     * minSideLength is used to specify that minimal width or height of a
     * bitmap.
     * maxNumOfPixels is used to specify the maximal size in pixels that is
     * tolerable in
     * terms of memory usage. The function returns a sample size based on the
     * constraints.
     * Both size and minSideLength can be passed in as IImage.UNCONSTRAINED,
     * which indicates
     * no care of the corresponding constraint. The functions prefers returning
     * a sample
     * size that generates a smaller bitmap, unless minSideLength =
     * IImage.UNCONSTRAINED.
     * Also, the function rounds up the sample size to a power of 2 or multiple
     * of 8 because
     * BitmapFactory only honors sample size this way. For example,
     * BitmapFactory
     * downsamples an image by 2 even though the request is 3. So we round up
     * the sample
     * size to avoid OOM.
     */
    private static int computeSampleSize(BitmapFactory.Options options,
            int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
            int minSideLength, int maxNumOfPixels) {
        if ((maxNumOfPixels == UNCONSTRAINED)
                && (minSideLength == UNCONSTRAINED)) {
            return 1;
        }

        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 : (int) Math
                .ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == UNCONSTRAINED) ? 128 : (int) Math
                .min(Math.floor(w / minSideLength),
                        Math.floor(h / minSideLength));

        // Android 4.1.1 bugs: Render failed if exceeds 2048, limit maximum
        // image size
        if (w > MAX_IMAGE_SIZE || h > MAX_IMAGE_SIZE) {
            int scale = (int) Math.ceil(Math.max(w / MAX_IMAGE_SIZE, h
                    / MAX_IMAGE_SIZE));
            lowerBound = Math.max(scale, lowerBound);
        }

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        return minSideLength == UNCONSTRAINED ? lowerBound : upperBound;
    }

    /**
     * MD5 encryption: 32 bits
     * @param str
     *            plan text
     * @return cipher text
     */
    public static String md5(String str) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(str.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            String hexString = "";
            String hexTempString;
            for (int i = 0; i < messageDigest.length; i++) {
                hexTempString = Integer.toHexString(0xFF & messageDigest[i]);
                if (hexTempString.length() == 1) {
                    hexString = hexString + "0" + hexTempString;
                } else {
                    hexString = hexString + hexTempString;
                }
            }

            return hexString;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * Base64 encode
     * @param str
     *            plain text
     * @return cipher text
     */
    public static String base64Encode(String str) {
        return Base64.encodeToString(str.getBytes(), Base64.NO_PADDING
                | Base64.NO_WRAP | Base64.URL_SAFE);
    }

    /**
     * Base64 decode
     * @param str
     *            cipher text
     * @return plain text
     */
    public static String base64Decode(String str) {
        String retString = "";
        try {
            retString = new String(Base64.decode(str, Base64.DEFAULT));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retString;
    }

    /**
     * @return Current date time string in "yyyyMMddHHmmss" format
     */
    public static String now() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        return format.format(new Date());
    }

    /**
     * 该方法用于检查包名为packageName的应用是否处于启动状态。
     * 算法描述：获取当前活跃的任务列表，该方法中获取了前MAX_TASK_NUM个任务，然后检查它们的包名是否与packageName相同，
     * 如果相同，则该应用处于活跃状态。
     * @param ctx
     *            上下文
     * @param packageName
     *            app的包名
     * @return
     */
    @SuppressWarnings("unused")
    private boolean checkApplicationRunningState(Context ctx, String packageName) {
        boolean isRunning = false;
        ActivityManager am = (ActivityManager) ctx
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> list = am.getRunningTasks(MAX_TASK_NUM);
        for (RunningTaskInfo info : list) {
            if (info.topActivity.getPackageName().equals(packageName)
                    && info.baseActivity.getPackageName().equals(packageName)) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    private static final String LAYER_TYPE_METHOD = "setLayerType";
    // Indicates that the view does not have a layer.
    public static final int LAYER_TYPE_NONE = 0;

    /**
     * <p> Indicates that the view has a software layer. A software layer is
     * backed by a
     * bitmap and causes the view to be rendered using Android's software
     * rendering
     * pipeline, even if hardware acceleration is enabled. </p>
     */
    public static final int LAYER_TYPE_SOFTWARE = 1;

    /**
     * <p> Indicates that the view has a hardware layer. A hardware layer is
     * backed by a
     * hardware specific texture (generally Frame Buffer Objects or FBO on
     * OpenGL hardware)
     * and causes the view to be rendered using Android's hardware rendering
     * pipeline, but
     * only if hardware acceleration is turned on for the view hierarchy. When
     * hardware
     * acceleration is turned off, hardware layers behave exactly as
     * {@link #LAYER_TYPE_SOFTWARE software layers}. </p>
     */
    public static final int LAYER_TYPE_HARDWARE = 2;

    /**
     * Disable hardware acceleration. View.setLayerType is available on android
     * 11+, so use
     * reflection instead.
     * @param view
     * @param renderMode
     *            渲染模式。{@link #LAYER_TYPE_NONE}, {@link #LAYER_TYPE_SOFTWARE},
     *            {@link #LAYER_TYPE_HARDWARE}其中一个。
     */
    public static void adjustRenderMode(View view, int renderMode) {
        try {
            // View.setLayerType
            Method method = View.class.getMethod(LAYER_TYPE_METHOD, int.class,
                    Paint.class);
            method.invoke(view, renderMode, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断当前界面是自己的app
     * @author : junfengli
     * @param : void
     * @return : 返回判断结果
     */
    public static boolean isRunningForeground(Context context) {
        if (context != null) {
            try {
                ActivityManager am = (ActivityManager) context
                        .getSystemService(Context.ACTIVITY_SERVICE);
                ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                String currentPackageName = cn.getPackageName();
                if (!TextUtils.isEmpty(currentPackageName)
                        && currentPackageName.equals(context.getPackageName())) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * gzip compress
     * @author : junfengli
     * @param : string
     * @return : byte[]
     */
    public static byte[] GZipCompress(String data) {
        ByteArrayOutputStream outPut = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutPut = null;

        try {
            gzipOutPut = new GZIPOutputStream(outPut);
            gzipOutPut.write(data.getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (gzipOutPut != null) {
                try {
                    gzipOutPut.close();
                } catch (IOException ignore) {
                    ignore.printStackTrace();
                }
            }
        }
        return outPut.toByteArray();
    }

    // Avoid this class to be instantiated
    private Utils() {
    }

    /**
     * Join all the elements of a string array into a single String. If the
     * given array
     * empty an empty string will be returned. Null elements of the array are
     * allowed and
     * will be treated like empty Strings.
     * @param array
     *            Array to be joined into a string.
     * @param delimiter
     *            String to place between array elements.
     * @return Concatenation of all the elements of the given array with the the
     *         delimiter
     *         in between.
     * @throws NullPointerException
     *             if array or delimiter is null.
     * @since ostermillerutils 1.05.00
     */
    public static String join(String[] array, String delimiter) {
        // Cache the length of the delimiter
        // has the side effect of throwing a NullPointerException if
        // the delimiter is null.
        int delimiterLength = delimiter.length();
        // Nothing in the array return empty string
        // has the side effect of throwing a NullPointerException if
        // the array is null.
        if (array.length == 0) {
            return "";
        }
        // Only one thing in the array, return it.
        if (array.length == 1) {
            if (array[0] == null) {
                return "";
            }
            return array[0];
        }
        // Make a pass through and determine the size
        // of the resulting string.
        int length = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) {
                length += array[i].length();
            }
            if (i < array.length - 1) {
                length += delimiterLength;
            }
        }
        // Make a second pass through and concatenate everything
        // into a string buffer.
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) {
                result.append(array[i]);
            }
            if (i < array.length - 1) {
                result.append(delimiter);
            }
        }
        return result.toString();
    }

    /**
     * Get a string random array by given string array.
     * @return
     */
    public static String[] getRandomStringArray(String[] array) {
        final String INIT_STR = "IMPOSSIBLE_STR_STR_IMPOSSIBLE";
        if (Utils.isArrayEmpty(array)) {
            return null;
        }

        String[] newArray = new String[array.length];
        // Init return array
        for (int i = 0; i < array.length; i++) {
            newArray[i] = INIT_STR;
        }

        Random random = new Random(System.currentTimeMillis());
        for (String orgStr : array) {
            while (true) {
                int i = random.nextInt(array.length);
                if (newArray[i].equals(INIT_STR)) {
                    newArray[i] = orgStr;
                    break;
                }
            }
        }

        return newArray;
    }

    /**
     * 从完整url中解析出不包含domain的url
     * @param domain
     * @param fullUrl
     *            完整url
     * @return
     */
    public static String parseBaseUrl(String domain, String fullUrl) {
        String baseUrl = fullUrl;
        if (!StringUtils.equalsNull(fullUrl) && !StringUtils.equalsNull(domain)) {
            int index = fullUrl.indexOf(domain) + domain.length();
            if (index != -1) {
                baseUrl = fullUrl.substring(index);
            }
        }
        return baseUrl;
    }

    /**
     * 关闭数据库Cursor对象
     */
    public static void closeCursor(Cursor cursor) {
        if (null != cursor) {
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
    }
}
