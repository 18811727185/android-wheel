package com.letv.component.player.http.request;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.http.client.utils.URLEncodedUtils;

import android.content.Context;
import android.os.Bundle;

import com.letv.component.core.async.LetvHttpAsyncRequest;
import com.letv.component.core.async.TaskCallBack;
import com.letv.component.core.http.bean.LetvBaseBean;
import com.letv.component.core.http.impl.LetvHttpParameter;
import com.letv.component.player.core.Configuration;
import com.letv.component.player.hardwaredecode.CodecWrapper;
import com.letv.component.player.http.HttpServerConfig;
import com.letv.component.player.utils.CpuInfosUtils;
import com.letv.component.player.utils.LogTag;
import com.letv.component.player.utils.Tools;
import com.media.ffmpeg.FFMpegPlayer;

public class HttpHardSoftDecodeReportRequest extends LetvHttpAsyncRequest {

	public HttpHardSoftDecodeReportRequest(Context context,
			TaskCallBack callback) {
		super(context, callback);
	}

	@Override
	public LetvHttpParameter getRequestParams(Object... params) {
		String appKey = String.valueOf(params[0]);
		String pcode = String.valueOf(params[1]);
		String appVer = String.valueOf(params[2]);
		String player = String.valueOf(params[3]);
		String vid = String.valueOf(params[4]);
		String playUrl = String.valueOf(params[5]);
		String isSuc = String.valueOf(params[6]);
		String errCode = String.valueOf(params[7]);
		String clarity = String.valueOf(params[8]);
		String isSmooth = String.valueOf(params[9]);
		

		Bundle bundle = new Bundle();
		bundle.putString("model", Tools.getDeviceName()); //model：手机型号
		bundle.putString("cpu", String.valueOf(CodecWrapper.getCapbility())); //cpu：cpu类型
		bundle.putString("sysVer", Tools.getOSVersionName()); //sysVer：Android版本号
		String sdkVer = Configuration.SDK_VERSION;
		bundle.putString("sdkVer", sdkVer); //sdkVer：播放组件版本号（播放器发布sdk版本号）
		bundle.putString("cpuCore", String.valueOf(CpuInfosUtils.getNumCores())); //cpuCore：cpu核心数
		bundle.putString("cpuHz", String.valueOf(CpuInfosUtils.getMaxCpuFrequence())); //cpuHz：cpu最大主频
		bundle.putString("did", Tools.generateDeviceId(context)); //did：设备唯一标示
		bundle.putString("appKey", appKey); //appKey：APP KEY
		bundle.putString("pCode", pcode); //pcode：渠道号
		bundle.putString("appVer", appVer); //appVer：APP版本
		bundle.putString("player", player); //player：播放器类型
		bundle.putString("isSuc", isSuc); //isSuc：播放结果
		bundle.putString("vid", vid); //vid：视频id
		try {
			bundle.putString("playUrl", URLEncoder.encode(playUrl, "UTF-8")); //播放地址
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} 
		bundle.putString("clarity", clarity); //clarity：清晰度类型
		bundle.putString("errCode", errCode); //errCode：播放失败错误码
		bundle.putString("isSmooth", isSmooth); //isSmooth：播放是否流畅

		LogTag.i("HttpHardSoftDecodeReportRequest:url=" + HttpServerConfig.getServerUrl() + HttpServerConfig.HARD_SOFT_DECODE_REPORT + ", params=" + bundle.toString());
		return new LetvHttpParameter(HttpServerConfig.getServerUrl(),
				HttpServerConfig.HARD_SOFT_DECODE_REPORT, bundle,
				LetvHttpParameter.Type.GET);
	}

	@Override
	public LetvBaseBean parseData(String sourceData) throws Exception {
		LogTag.i("sourceData=" + sourceData);
		return null;
	}

}
