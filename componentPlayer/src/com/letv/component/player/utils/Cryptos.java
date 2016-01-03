/*******************************************************************************
 * Copyright (c) 2005, 2014 springside.github.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *******************************************************************************/
package com.letv.component.player.utils;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/**
 * 在反馈初始化接口和上报接口中使用加密认证算法，在请求的头部增加ts、random、sign三个参数，服务端解析并利用key验证签名是否正确。
 * 参数说明：
 *     当前时间ts，格式为yyyyMMddHHmmss
 *     随机数random，10位数字或字符
 *     签名sign
 * 密算法：sign = md5(random前五位+ts后七位+key+ts前七位+random后五位）,其中key线下商议。
 * 考虑到客户端时间不准确，签名有效期定为服务器当前时间-1D<ts<服务器当前时间+1D；
 *
 */
public class Cryptos {
	
	public static final String key = "323cf2ec5d947b8d19073b66cab3b261";
	
}





