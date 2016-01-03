package com.letv.component.player.http.parser;

import org.json.JSONObject;

import com.letv.component.player.http.bean.HardSoftDecodeCapability;

public class HardSoftDecodeCapabilityPareser extends BaseParser {

	@Override
	protected String getLocationData() {
		return null;
	}

	@Override
	public Object parse(Object data) throws Exception {
		JSONObject jsonObject = (JSONObject) data;
		if (jsonObject == null) {
			return null;
		}
		HardSoftDecodeCapability capability = null;
		if (jsonObject.has("data")) {
			JSONObject dataJSONObject = getJSONObject(jsonObject, "data");
			capability = new HardSoftDecodeCapability();
			if (dataJSONObject != null) {
				capability.mResult = getString(dataJSONObject, "status");
			}
		}
		return capability;
	}

}
