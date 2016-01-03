package com.letv.component.player.http.parser;

import org.json.JSONObject;

import com.letv.component.player.http.bean.CdeState;

public class CdeStateParser extends BaseParser {


	@Override
	public Object parse(Object data) throws Exception {
		JSONObject jsonObject = (JSONObject) data;
		if (jsonObject == null) {
			return null;
		}
		CdeState state = null;
		if (jsonObject.has("data")) {
			JSONObject dataJSONObject = getJSONObject(jsonObject, "data");
			state = new CdeState();
			if (dataJSONObject != null) {
				state.downloadedDuration = getString(dataJSONObject, "downloadedDuration");
				state.downloadedRate = getString(dataJSONObject, "downloadedRate");
			}
		}
		return state;
	}
}
