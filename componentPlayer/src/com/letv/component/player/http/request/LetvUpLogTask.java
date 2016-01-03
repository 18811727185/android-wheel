package com.letv.component.player.http.request;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

import android.os.AsyncTask;
import android.util.Log;

import com.letv.component.player.http.HttpServerConfig;
import com.letv.component.player.utils.Cryptos;
import com.letv.component.player.utils.MD5;

public class LetvUpLogTask extends AsyncTask<String, Integer, String> {
	private static final String TAG = "LetvUpLogTask";
	private static final String CHARSET = "utf-8";
	private File zipFile ;
	private String fbid ;
	private List<File> files;
	private FeedCallBack feedCallBack;
	public LetvUpLogTask(List<File> files,File zipFile,FeedCallBack feedCallBack,String fbid){
		this.zipFile = zipFile;
		this.fbid = fbid;
		this.files = files;
		this.feedCallBack = feedCallBack;
	}
	
    // 可变长的输入参数，与AsyncTask.exucute()对应 
    @Override 
    protected String doInBackground(String... params) { 
		String result = null;
		String filename = "logFile.zip";
		String PREFIX = "--";
		String LINE_END = "\r\n";
		String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
		String CONTENT_TYPE = "multipart/form-data"; // 内容类型
		URL url = null;
		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		InputStream is = null;
		try {
			String urlString = HttpServerConfig.getFeedbackUploadlogUrl();
			urlString = String.format(urlString,fbid);
			url = new URL(urlString);
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10*1000);
			conn.setConnectTimeout(10*1000);
			conn.setDoInput(true); // 允许输入流
			conn.setDoOutput(true); // 允许输出流
			conn.setUseCaches(false); // 不允许使用缓存
			conn.setRequestMethod("POST"); // 请求方式
			conn.setRequestProperty("Connection", "Close"); // changed for tvlive
			
			
			SimpleDateFormat sDateFormat=new SimpleDateFormat("yyyyMMddHHmmss");
			String ts = sDateFormat.format(new java.util.Date());
			StringBuilder rsb = new StringBuilder();
			for (int i = 0; i < 10; i++) {
				if (i != 10) {
					rsb.append((int) (10 * (Math.random())));
				}
			}
			String random = rsb.toString();
			
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
			conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary="+ BOUNDARY);
			
				/**
				 * 当文件不为空，把文件包装并且上传
				 */
				OutputStream outputStream = conn.getOutputStream();
				dos = new DataOutputStream(outputStream);
				StringBuffer sb = new StringBuffer();
				sb.append(PREFIX);
				sb.append(BOUNDARY);
				sb.append(LINE_END);
				/**
				 * 这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
				 * filename是文件的名字，包含后缀名的 比如:abc.png
				 */
				sb.append("Content-Disposition: form-data; name=\"logFile\"; filename=\""+ filename + "\"" + LINE_END);
				sb.append("Content-Type: application/octet-stream; charset="+ CHARSET + LINE_END);
				sb.append(LINE_END);
				dos.write(sb.toString().getBytes());
				if (zipFile != null) {
    				is = new FileInputStream(zipFile);
    				byte[] bytes = new byte[1024 * 1024];
    				int len = 0;
    				while ((len = is.read(bytes)) != -1) {
    					dos.write(bytes, 0, len);
    				}
    				is.close();
				}
				dos.write(LINE_END.getBytes());
				byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
				dos.write(end_data);
				dos.flush();
				/**
				 * 获取响应码 200=成功 当响应成功，获取响应的流
				 */
				int res = conn.getResponseCode();
				Log.i(TAG, "response code:" + res);
				 if(res==200){
    				Log.i(TAG, "request success");
    				InputStream input = conn.getInputStream();
    				StringBuffer sb1 = new StringBuffer();
    				int ss;
    				while ((ss = input.read()) != -1) {
    					sb1.append((char) ss);
    				}
    				result = sb1.toString();
    				result = new String(result.getBytes("iso8859-1"), "utf-8");
				 }else{
				    Log.i(TAG, "request error");
				 }
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally{
			try {
				if(is!=null){
				   is.close();
 	    		}
				if(dos!=null){
	    		   dos.close();
	    		}
	    		if(conn!=null){
	    		   conn.disconnect();
	    		}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return result;
    }
    @Override 
    protected void onPostExecute(String result) { 
    	Log.i(TAG, "result="+result);
    	if(result!=null&&files!=null&&files.size()>0){
    	   for (File file : files) {
			    file.delete();
		   }
    	   if(zipFile!=null && zipFile.exists()){
    		   zipFile.delete();
    	   }
    	}
    }

    

public interface FeedCallBack {
	void response(String res);
}
}
