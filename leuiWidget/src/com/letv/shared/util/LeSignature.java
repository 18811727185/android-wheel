package com.letv.shared.util;

import android.text.TextUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Tool to get signed request of letv server.
 *
 * @author Wang Huisong <wanghuisong@letv.com>
 */
public class LeSignature {
    private static final String KEY_TIME ="_time";
    private static final String KEY_AK = "_ak";
    private static final String PARAMS_SEP = "&";
    private static final String REQUEST_CHARSET = "UTF-8";

    private static final char HEX_DIGITS[] = "0123456789abcdef".toCharArray();

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(HEX_DIGITS[(b & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b & 0x0f]);
        }
        return sb.toString();
    }

    private static String join(Iterable<String> strings, String sep) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : strings) {
            if (first) {
                first = false;
            } else {
                sb.append(sep);
            }

            sb.append(item);
        }
        return sb.toString();
    }

    /**
     * Get sign string of request params.
     * <p>
     * See <code>http://wiki.letv.cn/pages/viewpage.action?pageId=37325204</code>
     *
     * @param accessKey Access key of client.
     * @param secretKey Secret key of client.
     * @param params Params of request.
     * @param time Current timestamp.
     * @return Result of sign
     */
    public static String getSignature(String accessKey, String secretKey, Map<String, String> params, long time) {
        if (isEmpty(accessKey) || isEmpty(secretKey)) {
            throw new IllegalArgumentException("You MUST set access key and secret key for the request!");
        }

        SortedSet<String> set = new TreeSet<String>();

        try {

            if (!params.containsKey(KEY_TIME)) {
                set.add(KEY_TIME + "=" + URLEncoder.encode(String.valueOf(time), REQUEST_CHARSET));
            }

            if (!params.containsKey(KEY_AK)) {
                set.add(KEY_AK + "=" + URLEncoder.encode(accessKey, REQUEST_CHARSET));
            }

            if (params != null && params.size() > 0) {
                for (String param : params.keySet()) {
                    String value = params.get(param);
                    if (!TextUtils.isEmpty(value)) {
                        set.add(param + "=" + URLEncoder.encode(value, REQUEST_CHARSET));
                    }
                }
            }

            String paramsString = join(set, PARAMS_SEP);
            String str2Sign = paramsString + secretKey;

            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(str2Sign.getBytes(REQUEST_CHARSET));
            return toHexString(digest.digest());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Get sign string of request params and body.
     * <p>
     * See <code>http://wiki.letv.cn/pages/viewpage.action?pageId=37323874</code>
     *
     * @param secretKey Secret key of client.
     * @param method Method of request, liking POST, GET etc.
     * @param path Path of request, for example "/api/v1/message".
     * @param body Body of request.
     * @param time Current timestamp.
     * @param params Params of request.
     * @return Result of sign
     */

    @Deprecated
    public static String getSignature(String secretKey, String method, String path, byte[] body,
                                      long time, Map<String, String> params) {
        String date = formatDate(time);
        return getSignature(secretKey, method, path, body, date, params);
    }

    /**
     * Get sign string of request params and body.
     * <p>
     * See <code>http://wiki.letv.cn/pages/viewpage.action?pageId=37323874</code>
     *
     * @param secretKey Secret key of client.
     * @param method Method of request, liking POST, GET etc.
     * @param path Path of request, for example "/api/v1/message".
     * @param body Body of request.
     * @param date Date format followed with RFC822(<code>http://www.w3.org/Protocols/rfc822/#z28</code>).
     * @param params Params of request.
     * @return Result of sign
     * @see #formatDate(long)
     */
    public static String getSignature(String secretKey, String method, String path, byte[] body,
                                      String date, Map<String, String> params) {
        if (isEmpty(secretKey)) {
            throw new IllegalArgumentException("You MUST set access key for request!");
        }

        try {
            String bodyMD5 = "";
            if (body != null && body.length != 0) {
                MessageDigest digest;
                digest = MessageDigest.getInstance("MD5");
                digest.update(body);
                bodyMD5 = toHexString(digest.digest());
            }
            String paramString = "";
            if (params != null && params.size() > 0) {
                SortedSet<String> set = new TreeSet<String>();
                for (String param : params.keySet()) {
                    String value = params.get(param);
                    if (!TextUtils.isEmpty(value)) {
                        set.add(param + "=" + value);
                    }
                }
                paramString = join(set, PARAMS_SEP);
            }

            String stringToSign = method.toUpperCase() + "\n" + path + "\n" + bodyMD5 + "\n" + date + "\n" + paramString;
            SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(stringToSign.getBytes());
            return toHexString(rawHmac);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Format time followed RFC822(<code>http://www.w3.org/Protocols/rfc822/#z28</code>).
     * @param time Current time in millisecond.
     * @return Formatted date string.
     */
    public static String formatDate(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss z", Locale.ENGLISH);
        return sdf.format(new Date(time));
    }

    private static boolean isEmpty(CharSequence str) {
        return (str == null || str.toString().trim().length() == 0);
    }
}
