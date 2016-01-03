package com.letv.component.player.http.request;

import android.content.Context;
import android.os.Bundle;

import com.letv.component.core.async.LetvHttpAsyncRequest;
import com.letv.component.core.async.TaskCallBack;
import com.letv.component.core.http.bean.LetvBaseBean;
import com.letv.component.core.http.impl.LetvHttpBaseParameter;
import com.letv.component.core.http.impl.LetvHttpParameter;
import com.letv.component.core.http.impl.LetvHttpStaticParameter;
import com.letv.component.player.core.Configuration;
import com.letv.component.player.hardwaredecode.CodecWrapper;
import com.letv.component.player.http.HttpServerConfig;
import com.letv.component.player.http.parser.HardSoftDecodeCapabilityPareser;
import com.letv.component.player.utils.CpuInfosUtils;
import com.letv.component.player.utils.LogTag;
import com.letv.component.player.utils.Tools;

public class HttpGetCdeStateRequest extends LetvHttpAsyncRequest{

	public HttpGetCdeStateRequest(Context context, TaskCallBack callback) {
		super(context, callback);
	}

	@Override
	public LetvHttpBaseParameter getRequestParams(Object... params) {
		String url = String.valueOf(params[0]);

//		LogTag.i("HttpGetStateFromCdeRequest:url=" + url);
		return new LetvHttpStaticParameter(url, "", "", null);
	}

	@Override
	public LetvBaseBean parseData(String sourceData) throws Exception {
		LogTag.i("sourceData=" + sourceData);
		return (LetvBaseBean) new HardSoftDecodeCapabilityPareser()
				.initialParse(sourceData);
	}

	@Override
	protected boolean isSync() {
		return false;
	}

}
