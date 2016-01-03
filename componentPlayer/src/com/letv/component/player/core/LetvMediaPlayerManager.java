package com.letv.component.player.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.os.Handler;

import com.letv.component.player.http.HttpRequestManager;
import com.letv.component.player.http.HttpServerConfig;
import com.letv.component.player.http.bean.Cmdinfo;
import com.letv.component.player.http.parser.LetvFeedbackInitParser;
import com.letv.component.player.http.request.HttpFeedbackInitRequest;
import com.letv.component.player.http.request.LetvUpLogTask;
import com.letv.component.player.http.request.LetvUpLogTask.FeedCallBack;
import com.letv.component.player.utils.HardDecodeUtils;
import com.letv.component.player.utils.LogTag;
import com.letv.component.player.utils.NativeInfos;
import com.letv.component.player.utils.PreferenceUtil;
import com.letv.component.player.utils.ZipUtils;

public class LetvMediaPlayerManager {

	private static final String TAG = "LetvMediaPlayerManager";
	/**
	 * 播放器默认走m3u8软解，不显示软硬解切换按钮；
	 */
	public static final int HARD_DECODE_BLACK = 0;
	/**
	 * 播放器默认走m3u8硬解（硬解码流的支持度也会提供对应接口），显示软硬解切换按钮；
	 */
	public static final int HARD_DECODE_WHITE = 1;
	/**
	 * 播放器默认走m3u8软解，显示软硬解切换按钮（硬解码流的支持度也会提供对应接口）;
	 */
	public static final int HARD_DECODE_GRAY_CONFIGURABLE  = 2;
	/**
	 * 播放器默认走m3u8软解，不显示软硬解切换按钮；
	 */
	public static final int HARD_DECODE_GRAY_UNCONFIGURABLE  = 3;
	
	/**
	 * 支持码流级别，如一款手机，将返回支持最大码流级别SUPPORT_TS720P_LEVEL，那么SUPPORT_TS720P_LEVEL之下的都将支持，SUPPORT_TS720P_LEVEL之上的码流不支持
	 */
	public static final int SUPPORT_NO_SUPPORT_LEVEL = -1;
	public static final int SUPPORT_MP4_LEVEL = NativeInfos.SUPPORT_MP4_LEVEL;
	public static final int SUPPORT_TS180K_LEVEL = NativeInfos.SUPPORT_TS180K_LEVEL;
	public static final int SUPPORT_TS350K_LEVEL = NativeInfos.SUPPORT_TS350K_LEVEL;
	public static final int SUPPORT_TS800K_LEVEL = NativeInfos.SUPPORT_TS800K_LEVEL;
	public static final int SUPPORT_TS1000K_LEVEL = NativeInfos.SUPPORT_TS1000K_LEVEL;
	public static final int SUPPORT_TS1300K_LEVEL = NativeInfos.SUPPORT_TS1300K_LEVEL;
	public static final int SUPPORT_TS720P_LEVEL = NativeInfos.SUPPORT_TS720P_LEVEL;
	public static final int SUPPORT_TS1080P_LEVEL = NativeInfos.SUPPORT_TS1080P_LEVEL;

	
	/** 上下文 */
	private Context mContext;
	/** 软硬解能力 */
	private Configuration mConfig;
	
	boolean mDecodeRequestFinished = false; //判断是否请求过软硬解配置
	
	private static LetvMediaPlayerManager mManager = new LetvMediaPlayerManager();
	private ExecutorService mExecutorService;
	private File file;
	private String fileName = "VideoPlayLog" + ".log";
	private Handler handler = new Handler();

	private LetvMediaPlayerManager() {
	}

	public static LetvMediaPlayerManager getInstance() {
		return mManager;
	}

	/**
	 * 初始化接口
	 * 
	 * @param context
	 * @param appKey
	 * @param pCode
	 * @param appVer
	 */
	public void init(Context context, String appKey, String appID, String pCode, String appVer) {
		AppInfo.appKey = appKey;
		AppInfo.pCode = pCode;
		AppInfo.appVer = appVer;
		AppInfo.appID = appID;
		mContext = context;
		mConfig = Configuration.getInstance(context);
		HttpRequestManager.getInstance(mContext).requestCapability();
	}

	/**
	 * 获取硬解支持能力类型
	 * @return HARD_DECODE_BLACK, HARD_DECODE_WHITE, HARD_DECODE_GRAY_CONFIGURABLE, HARD_DECODE_GRAY_UNCONFIGURABLE
	 */
	public int getHardDecodeState(){
		/*return HARD_DECODE_BLACK;*/
		return mConfig.getHardDecodeState();//
	}
	
	/**
	 * 返回硬解支持的最高码流
	 * @return SUPPORT_TS1300K_LEVEL，SUPPORT_TS1000K_LEVEL，SUPPORT_TS350K_LEVEL，SUPPORT_TS180K_LEVEL
	 */
	public int getHardDecodeSupportLevel(){
		/*if (mConfig.mHardDecodeCapability.isSupport1080p) {
			return SUPPORT_TS1080P_LEVEL;
		} else */if (mConfig.mHardDecodeCapability.isSupport720p) {
			return SUPPORT_TS720P_LEVEL;
		} else if (mConfig.mHardDecodeCapability.isSupport1300k) {
			LogTag.i(TAG, "HARD_SUPPORT_TS1300K_LEVEL");
			return SUPPORT_TS1300K_LEVEL;
		} else if (mConfig.mHardDecodeCapability.isSupport1000k) {
			LogTag.i(TAG, "HARD_SUPPORT_TS1000K_LEVEL");
			return SUPPORT_TS1000K_LEVEL;
		} else if (mConfig.mHardDecodeCapability.isSupport350k) {
			LogTag.i(TAG, "HARD_SUPPORT_TS350K_LEVEL");
			return SUPPORT_TS350K_LEVEL;
		} else if (mConfig.mHardDecodeCapability.isSupport180k) {
			LogTag.i(TAG, "HARD_SUPPORT_TS180K_LEVEL");
			return SUPPORT_TS180K_LEVEL;
		} else {
			LogTag.i(TAG, "HARD_SUPPORT_NO_SUPPORT_LEVEL");
			return SUPPORT_NO_SUPPORT_LEVEL;
		}
	}
	
	/**
	 * 返回软解支持的最高码流
	 * @return SUPPORT_TS1300K_LEVEL，SUPPORT_TS1000K_LEVEL，SUPPORT_TS350K_LEVEL，SUPPORT_TS180K_LEVEL,SUPPORT_MP4_LEVEL
	 */
	public int getSoftDecodeSupportLevel(){
		if (mConfig.mSoftDecodeCapability.isSupport720p) {
			LogTag.i(TAG, "SOFT_SUPPORT_TS720p_LEVEL");
			return SUPPORT_TS720P_LEVEL;
		} else if (mConfig.mSoftDecodeCapability.isSupport1300k) {
			LogTag.i(TAG, "SOFT_SUPPORT_TS1300K_LEVEL");
			return SUPPORT_TS1300K_LEVEL;
		} else if (mConfig.mSoftDecodeCapability.isSupport1000k) {
			LogTag.i(TAG, "SOFT_SUPPORT_TS1000K_LEVEL");
			return SUPPORT_TS1000K_LEVEL;
		} else if (mConfig.mSoftDecodeCapability.isSupport350k) {
			LogTag.i(TAG, "SOFT_SUPPORT_TS350K_LEVEL");
			return SUPPORT_TS350K_LEVEL;
		} else if (mConfig.mSoftDecodeCapability.isSupport180k) {
			LogTag.i(TAG, "SOFT_SUPPORT_TS180K_LEVEL");
			return SUPPORT_TS180K_LEVEL;
		} else {
			LogTag.i(TAG, "SOFT_SUPPORT_NO_SUPPORT_LEVEL");
			return SUPPORT_NO_SUPPORT_LEVEL;
		}
	}

	/**
	 * 获取sdk版本号
	 * @return
	 */
	public String getSdkVersion(){
		return Configuration.SDK_VERSION;
	}
	
	/**
	 * 设置debug模式
	 */
	public void setDebugMode(boolean debug) {
		HttpServerConfig.setDebugMode(debug);
	}
	
	/**
	 * 设置log开关, true:打开log  false:关闭log
	 */
	public void setLogMode(boolean debug) {
		LogTag.setDebugMode(debug);
	}
	
	/**
	 *  反馈接口，该方法由主客户端的反馈按钮触发
	 */
	public void feedbackCommit() {
		file = new File(getSaveDirc() + File.separator+ fileName);
		if(!file.exists()) {
			LogTag.i("file is null or not exist");
			return;
		}
		new HttpFeedbackInitRequest(mContext, new HttpFeedbackInitRequest.getResultCallback() {

			@Override
			public void response(String res) {
				Object object = null;
				try {
					object = new LetvFeedbackInitParser().parse(res);
					if (object != null) {
						Cmdinfo cmdinfo = (Cmdinfo) object;
						final Long fbId = cmdinfo.getFbId();
						boolean supportHWDecodeUseNative = HardDecodeUtils.isSupportHWDecodeUseNative();
						int avcLevel = HardDecodeUtils.getAVCLevel();
						PreferenceUtil.setLocalCapcity(mContext, "本地解码能力：supportHWDecodeUseNative  "+supportHWDecodeUseNative+"  avcLevel = "+avcLevel);
						if(mExecutorService==null){
							mExecutorService = Executors.newCachedThreadPool();
						}
						mExecutorService.execute(new Runnable() {
							@Override
							public void run() {
								writeLog();
								handler.post(new Runnable() {
									@Override
									public void run() {
										upLog(getSaveDirc(), fbId.toString());
									}
								});
							}
						});
						
						
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}).executeOnExecutor(Executors.newCachedThreadPool(), "");
	}
	
	private synchronized static void upLog(String path, String fbid) {
		
		File zipFile = null;
		List<File> files = new ArrayList<File>();
		File fileParent = new File(path);

		if (!fileParent.exists()) {
			return;
		} else {
			files = list2(fileParent);
			if (files.size() == 0) {

			} else {
				try {
					if (files.isEmpty()) {
						return;
					}
					zipFile = new File(fileParent + "/logFile.zip");
					LogTag.i("zipFile path=" + fileParent + "/logFile.zip");
					ZipUtils.zipFiles(files, zipFile);
				} catch (Exception e) {
					return;
				}
				if (zipFile == null || !zipFile.exists()) {
					return;
				}
				new LetvUpLogTask(files, zipFile, new FeedCallBack() {

					@Override
					public void response(String res) {

					}
				}, fbid).executeOnExecutor(Executors.newCachedThreadPool(), "");
			}
		}
	}
	
	private synchronized static List<File> list2(File dir) {
		List<File> fileList = new ArrayList<File>();
		File[] all = dir.listFiles();
		// 递归获得当前目录的所有子目录
		for (int i = 0; i < all.length; i++) {
			File d = all[i];
			if (d.isFile()) {
				fileList.add(d);
			}
		}
		return fileList;
	}
	
	
	private void writeLog() {
		
		String pathDownload = getSaveDirc();
		
		try {
			String filePath = createFileIfNotExist(pathDownload);
			FileOutputStream fos = new FileOutputStream(new File(filePath), true);
			String tmp = PreferenceUtil.getQuestParams(mContext)+"\r\n"+PreferenceUtil.getQuestResult(mContext)+"\r\n"+
			PreferenceUtil.getLocalCapcity(mContext);
			fos.write(tmp.getBytes());
			fos.close();
		} catch (Exception e) {
			return;
		}
	}
	
	private String createFileIfNotExist(String pathDownload){
		try {
			File dir = new File(pathDownload);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			file = new File(pathDownload + File.separator+ fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			return file.getAbsolutePath();
		} catch (Exception e) {
			return "";
		}
	}
	
	private String getSaveDirc() {
		String pathDownload = mContext.getDir(
					"LetvLog",
					Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE
							| Context.MODE_PRIVATE).getPath();
		LogTag.i("pathDownload=" + pathDownload);
		return pathDownload;
	}
	/**
	 * 写文件日志
	 * @param content
	 * @param isRemoveHistorFileInfo
	 */
	public void writePlayLog(final String content){
		LogTag.i(content);
		writePlayLog(content, false);
	}
	public void writePlayLog(final String content,final boolean isRemoveHistoryFileInfo){
		if(mExecutorService == null){
			mExecutorService = Executors.newCachedThreadPool();
		}
		mExecutorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					synchronized (LetvMediaPlayerManager.this) {
						if(isRemoveHistoryFileInfo){
							if(file!=null && file.exists()){
								file.delete();
							}
						}
			            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
			            FileWriter writer = new FileWriter(new File(createFileIfNotExist(getSaveDirc())), true);
			            writer.write(content+"\r\n");
			            writer.flush();
			            writer.close();  
					}
		        } catch (IOException e) {  
		            e.printStackTrace();  
		        }  
			}
		});
	}
	
}
