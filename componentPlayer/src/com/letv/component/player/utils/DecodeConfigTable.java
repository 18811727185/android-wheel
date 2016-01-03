package com.letv.component.player.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class DecodeConfigTable {

	public String getStatus(Context context) {
		String status = "";
		String config = sConfig;
		try {
			JSONArray jsonArray = new JSONArray(config);
			int length = jsonArray.length();
			for (int i = 0; i < length; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String model = jsonObject.getString("model");
				String sysVer = jsonObject.getString("sysVer");
				if (model.trim().equalsIgnoreCase(Tools.getDeviceName())
						&& sysVer.trim().equalsIgnoreCase(
								Tools.getOSVersionName())) {
					status = jsonObject.getString("status");
					break;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return status;
	}

	String sConfig = new String(
			"[{\"model\":\"XIAOMI\",\"sysVer\":\"ANDROID OS 4.2\",\"status\":\"256,23\"}]");
}
