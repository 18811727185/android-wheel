package com.letv.component.player.http.parser;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.letv.component.player.http.bean.Cmdinfo;
import com.letv.component.player.http.bean.Order;
import com.letv.component.player.http.bean.Order.Tag;

public class LetvFeedbackInitParser  {
	private final String TAG = "LetvFeedbackParser";

	public Object parse(Object data) throws Exception {
		if(data == null) {
			return null;
		}
		JSONObject o = new JSONObject((String)data);
		Log.i(TAG, o.getString("code"));
		if (o.getString("code").equals("A000000")) {
			Cmdinfo cmdinfo = new Cmdinfo();
			if (o.has("data")) {
				JSONObject mdata = o.getJSONObject("data");
				Log.i(TAG, "HttpFeedbackRequest:mdata=" + o);
				cmdinfo.setFbCode(mdata.getString("fbCode"));
				cmdinfo.setFbId(mdata.getLong("fbId"));
				cmdinfo.setUpPeriod(mdata.getInt("upPeriod"));
				cmdinfo.setEndTime(mdata.getLong("endTime"));
				JSONArray cmdArray = mdata.getJSONArray("cmdAry");
				Log.i(TAG, "HttpFeedbackRequest:cmdArray=" + cmdArray);
				JSONObject myJson = null;
				if (cmdArray != null) {
					Order order = new Order();
					for (int i = 0; i < cmdArray.length(); i++) {
						myJson = (JSONObject) cmdArray.get(i);
						Tag tag = new Tag();
						tag.setCmdId(myJson.getLong("cmdId"));
						tag.setUpType(myJson.getInt("upType"));
						tag.setCmdType(myJson.getInt("cmdType"));
						tag.setCmdParam(myJson.getString("cmdParam"));
						tag.setCmdUrl(myJson.getString("cmdUrl"));
						order.add(tag);
					}
					Log.i(TAG, "myorder" + order);
					cmdinfo.setCmdAry(order);
				} else {
					cmdinfo.setCmdAry(null);
				}
			}
			return cmdinfo;
		} else {
			return null;
		}
	}

}
