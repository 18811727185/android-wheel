package com.letv.component.player.http.request;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.letv.component.player.core.AppInfo;
import com.letv.component.player.core.LetvMediaPlayerManager;
import com.letv.component.player.http.HttpServerConfig;
import com.letv.component.player.utils.CpuInfosUtils;
import com.letv.component.player.utils.Cryptos;
import com.letv.component.player.utils.MD5;
import com.letv.component.player.utils.MemoryInfoUtil;
import com.letv.component.player.utils.Tools;

public class HttpFeedbackInitRequest extends AsyncTask<String, Integer, String> {
	
	private String Tag = "HttpFeedbackRequest";
	private getResultCallback getResultCallBack;
	private Context mContext;

	public HttpFeedbackInitRequest(Context context, getResultCallback getResultCallBack) {
		this.getResultCallBack = getResultCallBack;
		mContext = context;
	}

	@Override
	protected String doInBackground(String... params) {
		String result = null;
		HttpURLConnection conn = null;
		OutputStream output = null;
		InputStream input = null;
		Map<String, String> bundle = new HashMap<String, String>();
		bundle.put("upType", "2");
		bundle.put("appId", AppInfo.appID);
		bundle.put("appVersion", AppInfo.appVer);
		bundle.put("devOsVersion", Tools.getOSVersionName());
		bundle.put("devId", Tools.generateDeviceId(mContext));
		bundle.put("devVendor", Tools.getBrandName());
		bundle.put("devModel", Tools.getDeviceName());
		bundle.put("devMac", Tools.getMacAddress(mContext));
		bundle.put("devCpu", CpuInfosUtils.getCpuInfo() + " NumCores:"
				+ CpuInfosUtils.getNumCores());
		bundle.put("devRam", MemoryInfoUtil.getMemTotal() / 1024 + "");
		bundle.put("devRom", MemoryInfoUtil.getTotalInternalMemorySize() / 1048576 + "");
		bundle.put("devSdcard", MemoryInfoUtil.getSDCardMemory() / 1048576 + "");
		bundle.put("resolution", Tools.getResolution(mContext));
		bundle.put("snNo", Tools.getIMSI(mContext));
		bundle.put("imeiNo", Tools.getIMEI(mContext));
		bundle.put("netType", Tools.getNetTypeName(mContext));
		bundle.put("sdkVersion", "player_" + LetvMediaPlayerManager.getInstance().getSdkVersion());
		
		Gson gson = new Gson();
		String body = gson.toJson(bundle);
		String urlString = HttpServerConfig.getFeedbackInitUrl();
		try {
			URL url = new URL(urlString);
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10 * 1000);
			conn.setConnectTimeout(10 * 1000);
			conn.setDoInput(true); // 允许输入流
			conn.setDoOutput(true); // 允许输出流
			conn.setUseCaches(false); // 不允许使用缓存
			conn.setRequestMethod("POST"); // 请求方式
			conn.setRequestProperty("Connection", "Close"); // changed for tvlive
			
			SimpleDateFormat sDateFormat=new SimpleDateFormat("yyyyMMddHHmmss");
			String ts = sDateFormat.format(new java.util.Date());
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 10; i++) {
				if (i != 10) {
					sb.append((int) (10 * (Math.random())));
				}
			}
			String random = sb.toString();
			
			StringBuffer md5 = new StringBuffer(random.substring(0, 5));
			md5.append(ts.substring(7));
			md5.append(Cryptos.key);
			md5.append(ts.substring(0, 7));
			md5.append(random.substring(5));
			String sign = MD5.toMd5(md5.toString());
			
			conn.addRequestProperty("ts", ts);
			conn.addRequestProperty("random", random);
			conn.addRequestProperty("sign", sign);
			conn.setRequestProperty("Charset", "utf-8"); // 设置编码
			
			conn.setRequestProperty("connection", "keep-alive");
			output = conn.getOutputStream();
			output.write(body.getBytes("utf-8"));
			conn.connect();
			int res = conn.getResponseCode();
			if (res == 200) {
				Log.i("GetRequestUrl", "request success");
				input = conn.getInputStream();
				StringBuffer sb1 = new StringBuffer();
				int ss;
				while ((ss = input.read()) != -1) {
					sb1.append((char) ss);
				}
				result = sb1.toString();
				result = new String(result.getBytes("iso8859-1"), "utf-8");
				input.close();
			} else {
				Log.i("GetRequestUrl", "request faild");
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (output != null)
				try {
					output.close();
				} catch (IOException logOrIgnore) {
				}
			if (conn != null) {
				conn.disconnect();
			}

		}
		return result;
	}

	@Override
	protected void onPostExecute(String result) {
		getResultCallBack.response(result);
		Log.i(Tag, "urlresult=" + result);
		super.onPostExecute(result);
	}
	
	public interface getResultCallback {
		void response(String res);
	}
}
