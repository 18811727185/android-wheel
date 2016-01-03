package com.letv.mobile.core.utils;

public class Base64Utils {

    public static String encodeString(String args) {
        return Base64.encodeToString(args.getBytes(), Base64.DEFAULT)
                .replaceAll("\n", "");
    }

    public static String encodeByteArray(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static byte[] decodeString(String str) {
        return Base64.decode(str, Base64.DEFAULT);
    }

}
