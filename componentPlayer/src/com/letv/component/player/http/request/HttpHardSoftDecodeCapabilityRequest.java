package com.letv.component.player.http.request;

import android.content.Context;
import android.os.Bundle;

import com.letv.component.core.async.LetvHttpAsyncRequest;
import com.letv.component.core.async.TaskCallBack;
import com.letv.component.core.http.bean.LetvBaseBean;
import com.letv.component.core.http.impl.LetvHttpBaseParameter;
import com.letv.component.core.http.impl.LetvHttpParameter;
import com.letv.component.player.core.Configuration;
import com.letv.component.player.core.LetvMediaPlayerManager;
import com.letv.component.player.hardwaredecode.CodecWrapper;
import com.letv.component.player.http.HttpServerConfig;
import com.letv.component.player.http.parser.HardSoftDecodeCapabilityPareser;
import com.letv.component.player.utils.CpuInfosUtils;
import com.letv.component.player.utils.HardDecodeUtils;
import com.letv.component.player.utils.LogTag;
import com.letv.component.player.utils.PreferenceUtil;
import com.letv.component.player.utils.Tools;
import com.media.ffmpeg.FFMpegPlayer;

public class HttpHardSoftDecodeCapabilityRequest extends LetvHttpAsyncRequest{

	public HttpHardSoftDecodeCapabilityRequest(Context context, TaskCallBack callback) {
		super(context, callback);
	}

	@Override
	public LetvHttpBaseParameter getRequestParams(Object... params) {
		String appKey = String.valueOf(params[0]);
		String pCode = String.valueOf(params[1]);
		String appVer = String.valueOf(params[2]);

		Bundle bundle = new Bundle();
		bundle.putString("model", Tools.getDeviceName()); //model：手机型号
		bundle.putString("cpu", String.valueOf(CodecWrapper.getCapbility())); //cpu：cpu类型
		bundle.putString("sysVer", Tools.getOSVersionName()); //sysVer：Android版本号
		String sdkVer = Configuration.SDK_VERSION;
		bundle.putString("sdkVer", sdkVer); //sdkVer：播放组件版本号（播放器发布sdk版本号）
		bundle.putString("cpuCore", String.valueOf(CpuInfosUtils.getNumCores())); //cpuCore：cpu核心数
		bundle.putString("cpuHz", String.valueOf(CpuInfosUtils.getMaxCpuFrequence())); //cpuHz：cpu最大主频
		bundle.putString("decodeProfile", String.valueOf(CodecWrapper.getProfile()));
		bundle.putString("AVCLevel", String.valueOf(CodecWrapper.getAVCLevel()));
		bundle.putString("did", Tools.generateDeviceId(context)); //did：设备唯一标示
		bundle.putString("appKey", appKey); //appKey：APP KEY
		bundle.putString("pCode", pCode); //pcode：渠道号
		bundle.putString("appVer", appVer); //appVer：APP版本
		
		LogTag.i("HttpHardSoftDecodeCapabilityRequest:url=" + HttpServerConfig.getServerUrl() + HttpServerConfig.HARD_SOFT_DECODE_CAPABILITY + ", params=" + bundle.toString());
		String currentDate = Tools.getCurrentDate();
		String requestParams = "系统当前时间:  "+currentDate+" 软硬解码请求接口参数  ："+"HttpHardSoftDecodeCapabilityRequest:url=" + HttpServerConfig.getServerUrl() + HttpServerConfig.HARD_SOFT_DECODE_CAPABILITY + ", params=" + bundle.toString();
		PreferenceUtil.setQuestParams(context, requestParams);
		return new LetvHttpParameter(HttpServerConfig.getServerUrl(),
				HttpServerConfig.HARD_SOFT_DECODE_CAPABILITY, bundle,
				LetvHttpParameter.Type.POST);
	}

	@Override
	public LetvBaseBean parseData(String sourceData) throws Exception {
		LogTag.i("sourceData=" + sourceData);
		String currentDate = Tools.getCurrentDate();
		String requestResult = "系统当前时间:  "+currentDate+"  软硬解码能力返回值  ："+sourceData;
		PreferenceUtil.setQuestResult(context, requestResult);
		return (LetvBaseBean) new HardSoftDecodeCapabilityPareser()
				.initialParse(sourceData);
	}
	@Override
	protected int getReadTimeOut() {
		return 50*1000;
	}

}
