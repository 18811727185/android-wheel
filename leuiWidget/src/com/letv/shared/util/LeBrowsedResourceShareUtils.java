package com.letv.shared.util;

import java.util.Map;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;

public class LeBrowsedResourceShareUtils {

    private final static String TAG = LeBrowsedResourceShareUtils.class.getSimpleName();
    
    private final static String FEATURE_PRO = "com.letv.android.share.pro.share_show_pro_logo";
    private final static String FEATURE_MAX = "com.letv.android.share.max.share_show_max_logo";
    
    private final static String S1_SHARE_PACKAGE_NAME = "com.letv.android.share.preleading";
    private final static String S1_ACTIVITY_WEIBO_NAME = "com.letv.android.share.preleading.LaunchWeiboShareActivity";
    private final static String S1_ACTIVITY_WECHAT_NAME = "com.letv.android.share.preleading.LaunchWeChatShareActivity";
    private final static String S1_ACTIVITY_QQ_NAME = "com.letv.android.share.preleading.LaunchQQShareActivity";
    private final static String S1_ACTIVITY_QZone_NAME = "com.letv.android.share.preleading.LaunchQZoneShareActivity";
    
    private final static String PRO_SHARE_PACKAGE_NAME = "com.letv.android.share.pro";
    private final static String PRO_ACTIVITY_WEIBO_NAME = "com.letv.android.share.pro.LaunchWeiboShareActivity";
    private final static String PRO_ACTIVITY_WECHAT_NAME = "com.letv.android.share.pro.LaunchWeChatShareActivity";
    private final static String PRO_ACTIVITY_QQ_NAME = "com.letv.android.share.pro.LaunchQQShareActivity";
    private final static String PRO_ACTIVITY_QZone_NAME = "com.letv.android.share.pro.LaunchQZoneShareActivity";
    
    private final static String MAX1_SHARE_PACKAGE_NAME = "com.letv.android.share.max";
    private final static String MAX1_ACTIVITY_WEIBO_NAME = "com.letv.android.share.max.LaunchWeiboShareActivity";
    private final static String MAX1_ACTIVITY_WECHAT_NAME = "com.letv.android.share.max.LaunchWeChatShareActivity";
    private final static String MAX1_ACTIVITY_QQ_NAME = "com.letv.android.share.max.LaunchQQShareActivity";
    private final static String MAX1_ACTIVITY_QZone_NAME = "com.letv.android.share.max.LaunchQZoneShareActivity";
    
    public enum LeResourceType {
        leText, leImagePath, leWebUrl, leVideoUrl, leMusicUrl, leTitle, leBitmap, leFilePath, leLinkUrl, leSupportLinkcard
    }
    
    public enum LeMimeType {
        text, image, html, video, audio, application
    }

    /**
     * @param platformId
     *            0--sina weibo 1--weixin friend 2--weixin friend group
     * @param resourceId
     *            1--text 2--image 3--webpage url 4--music 5--video  6--file
     * @param resourceMap
     * @param activity
     * @return
     */
    public static boolean shareBrowsedResourcesByIntent(int platformId, String mimeType, Map<LeResourceType, Object> resourceMap,
            Context mContext) {
    
        if ( null == mimeType || "".equals(mimeType) ){
         Log.e(TAG, "Type should not be empty");
         return false;
        }
        
        int resourceId = -1;
        
        String linkedUrl = "";
        String imageUrl = "";
        boolean supportLinkcard = false;
//        if( resourceMap.containsKey(LeResourceType.leSupportLinkcard)){
//            if( resourceMap.get(LeResourceType.leSupportLinkcard) != null && resourceMap.get(LeResourceType.leSupportLinkcard) instanceof Boolean){
//                if( (Boolean)resourceMap.get(LeResourceType.leSupportLinkcard) ){
//                    supportLinkcard = true;
//                }
//            }
//        }
        
        if( resourceMap.containsKey(LeResourceType.leLinkUrl)){
            Object obj = resourceMap.get(LeResourceType.leLinkUrl);
            if( obj != null && !(obj instanceof String )){
                Log.e(TAG, "The type of leLinkUrl should be string.");
                return false;
            } else if( obj != null){
                linkedUrl = (String)obj;
            }
        }
        if( resourceMap.containsKey(LeResourceType.leImagePath)){
            Object obj = resourceMap.get(LeResourceType.leImagePath);
            if( obj != null && !(obj instanceof String )){
                Log.e(TAG, "The type of leImagePath should be string.");
                return false;
            } else if( obj != null){
                imageUrl = (String)obj;
            }
        }
        if( mimeType.startsWith(LeMimeType.text.name())){
            if( !"".equals(linkedUrl)){
                if ( !linkedUrl.startsWith("http")){
                    Log.e(TAG, "web page url should start wtih 'http' or 'https'.");
                    return false;
                }
                resourceMap.put(LeResourceType.leWebUrl, linkedUrl);
                resourceId = 3;
            } else if( resourceMap.get(LeResourceType.leText) == null || "".equals(resourceMap.get(LeResourceType.leText))){
                outputLog("text resource is null !");
                return false;
            } else {
                resourceId = 1;
            }
            
        } else if ( mimeType.startsWith(LeMimeType.image.name())){
            if( resourceMap.get(LeResourceType.leBitmap) == null && "".equals(imageUrl)){
                outputLog("image resource is null !");
                return false;
            } else {
                resourceId = 2;
            }
        } else if ( mimeType.startsWith(LeMimeType.audio.name())){
                if( "".equals(linkedUrl)){
                   outputLog("audio url is null !");
                   return false;
                } else {
                    // qq音乐分享特殊，需要musicUrl和linkedUrl
                    if( 3 != platformId){
                        resourceMap.put(LeResourceType.leMusicUrl, linkedUrl);
                    } 
                }
            resourceId = 4;
        } else if ( mimeType.startsWith(LeMimeType.video.name())){
            if( "".equals(linkedUrl)){
                outputLog("video url is null !");
                return false;
            } else {
                resourceMap.put(LeResourceType.leVideoUrl, linkedUrl);
            }
            resourceId = 5;
        }  
//        else if ( mimeType.startsWith(LeMimeType.application.name())){
//            if( "".equals(linkedUrl)){
//                outputLog("app url is null !");
//                return false;
//            } else {
//                resourceMap.put(LeResourceType.leFilePath, linkedUrl);
//            }
//            resourceId = 6;
//        }  
        if( -1 == resourceId){
            outputLog("No match share type found!");
            return false;
        }
        
        Intent intent = new Intent(Intent.ACTION_SEND);
        ComponentName component;
        switch (platformId) {
        case 0:
        	if( mContext.getPackageManager().hasSystemFeature(FEATURE_PRO)){
        		component = new ComponentName(PRO_SHARE_PACKAGE_NAME,
        				PRO_ACTIVITY_WEIBO_NAME);
        	} else if (mContext.getPackageManager().hasSystemFeature(FEATURE_MAX)){
        		component = new ComponentName(MAX1_SHARE_PACKAGE_NAME,
        				MAX1_ACTIVITY_WEIBO_NAME);
        	} else {
        		component = new ComponentName(S1_SHARE_PACKAGE_NAME,
        				S1_ACTIVITY_WEIBO_NAME);
        	}
            intent.setComponent(component);
            if( !setIntentValue(intent, resourceMap, mContext) ){
                return false;
            }
            if( resourceMap.containsKey(LeResourceType.leWebUrl) || resourceMap.containsKey(LeResourceType.leMusicUrl) || 
                    resourceMap.containsKey(LeResourceType.leVideoUrl) ){
                
                String textStr = "";
                if ( !resourceMap.containsKey(LeResourceType.leTitle) || "".equals(resourceMap.get(LeResourceType.leTitle)) || null == resourceMap.get(LeResourceType.leTitle) ){
                    intent.putExtra(LeResourceType.leTitle.name(), "来自乐视超级手机");
                } 
//                else {
//                    textStr = intent.getStringExtra(LeResourceType.leTitle.name());
//                }
                if ( !resourceMap.containsKey(LeResourceType.leText) ||"".equals(resourceMap.get(LeResourceType.leText)) || null == resourceMap.get(LeResourceType.leText) ){
                    intent.putExtra(LeResourceType.leText.name(), (String)(resourceMap.get(LeResourceType.leLinkUrl)));
                } else {
                    textStr = intent.getStringExtra(LeResourceType.leText.name());
                }
//                if( !resourceMap.containsKey(LeResourceType.leText) || !resourceMap.containsKey(LeResourceType.leTitle) ||
//                        ( !resourceMap.containsKey(LeResourceType.leBitmap) && !resourceMap.containsKey(LeResourceType.leImagePath))){
//                    outputLog("Text, title or image info is empty.");
//                    return false;
//                }
                // 如果不支持linkcard功能，拼接linkedUrl
                if( !supportLinkcard){
                    textStr = textStr + " " + linkedUrl;
                    resourceId = 0;
                } 
                intent.putExtra(LeResourceType.leText.name(),  textStr);
            }
            intent.putExtra("dataType", resourceId);
            try {
                mContext.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return true;
        case 1:
        case 2:
            if (platformId == 1) {
                intent.putExtra("isFriendGroup", false);
            } else {
                intent.putExtra("isFriendGroup", true);
            }
        	if( mContext.getPackageManager().hasSystemFeature(FEATURE_PRO)){
        		component = new ComponentName(PRO_SHARE_PACKAGE_NAME,
        				PRO_ACTIVITY_WECHAT_NAME);
        	} else if (mContext.getPackageManager().hasSystemFeature(FEATURE_MAX)){
        		component = new ComponentName(MAX1_SHARE_PACKAGE_NAME,
        				MAX1_ACTIVITY_WECHAT_NAME);
        	} else {
        		component = new ComponentName(S1_SHARE_PACKAGE_NAME,
        				S1_ACTIVITY_WECHAT_NAME);
        	}
            intent.setComponent(component);
            if( !setIntentValue(intent, resourceMap, mContext) ){
                return false;
            }
            intent.putExtra("dataType", resourceId);
            mContext.startActivity(intent);
            return true;
   case 3:
   	if( mContext.getPackageManager().hasSystemFeature(FEATURE_PRO)){
		component = new ComponentName(PRO_SHARE_PACKAGE_NAME,
				PRO_ACTIVITY_QQ_NAME);
	} else if (mContext.getPackageManager().hasSystemFeature(FEATURE_MAX)){
		component = new ComponentName(MAX1_SHARE_PACKAGE_NAME,
				MAX1_ACTIVITY_QQ_NAME);
	} else {
		component = new ComponentName(S1_SHARE_PACKAGE_NAME,
				S1_ACTIVITY_QQ_NAME);
	}
    intent.setComponent(component);
  if ( resourceId == 5 ){
      if (  null == resourceMap.get(LeResourceType.leVideoUrl) || "".equals(resourceMap.get(LeResourceType.leVideoUrl)) ) {
          Log.e(TAG, "video url is empty for qq imageAndText share");
          return false;
         } 
      resourceMap.put(LeResourceType.leWebUrl, resourceMap.get(LeResourceType.leVideoUrl));
         
         if (  null == resourceMap.get(LeResourceType.leTitle) || "".equals(resourceMap.get(LeResourceType.leTitle)) ) {
          Log.e(TAG, "title is empty for qq imageAndText share");
          intent.putExtra(LeResourceType.leTitle.name(), "来自乐视超级手机");
         } 
    } 
    if( !setIntentValue(intent, resourceMap, mContext) ){
     return false;
    }
    
    if ( resourceId == 1 || resourceId == 3 || resourceId == 5){
     if (  null == resourceMap.get(LeResourceType.leWebUrl) || "".equals(resourceMap.get(LeResourceType.leWebUrl)) ) {
     Log.e(TAG, "webPage url is empty for qq imageAndText share");
     return false;
    } 
    
    if (  null == resourceMap.get(LeResourceType.leTitle) || "".equals(resourceMap.get(LeResourceType.leTitle)) ) {
     Log.e(TAG, "title is empty for qq imageAndText share");
     resourceMap.put(LeResourceType.leTitle, "乐视分享");
    } 
   } else if ( resourceId == 2 ){
    if (null == resourceMap.get(LeResourceType.leImagePath)|| "".equals(resourceMap.get(LeResourceType.leImagePath))) {
     Log.e(TAG, "image url is empty for qq image share");
     return false;
    } 
   } else if ( resourceId == 4 ){
    if (  null == resourceMap.get(LeResourceType.leTitle) || "".equals(resourceMap.get(LeResourceType.leTitle)) ) {
     Log.e(TAG, "title is empty for qq music share");
     resourceMap.put(LeResourceType.leTitle, "乐视分享");
    } 
    if (  null == resourceMap.get(LeResourceType.leLinkUrl) || "".equals(resourceMap.get(LeResourceType.leLinkUrl)) ) {
     Log.e(TAG, "target url is empty for qq music share");
     return false;
				} else {
					intent.putExtra(LeResourceType.leWebUrl.name(),
							(String) resourceMap.get(LeResourceType.leLinkUrl));
				}
				if (null == resourceMap.get(LeResourceType.leMusicUrl)|| "".equals(resourceMap.get(LeResourceType.leMusicUrl))) {
					Log.e(TAG, "music url is empty for qq music share");
					resourceMap.put(LeResourceType.leMusicUrl, resourceMap.get(LeResourceType.leLinkUrl));
				}
				intent.putExtra(LeResourceType.leMusicUrl.name(),
						(String) resourceMap.get(LeResourceType.leMusicUrl));
   } else {
    Log.e(TAG, "No support for other types for qq");
    return false;
   }
   intent.putExtra("dataType", resourceId);
    mContext.startActivity(intent);
    return true;
   case 4:
	   	if( mContext.getPackageManager().hasSystemFeature(FEATURE_PRO)){
			component = new ComponentName(PRO_SHARE_PACKAGE_NAME,
					PRO_ACTIVITY_QZone_NAME);
		} else if (mContext.getPackageManager().hasSystemFeature(FEATURE_MAX)){
			component = new ComponentName(MAX1_SHARE_PACKAGE_NAME,
					MAX1_ACTIVITY_QZone_NAME);
		} else {
			component = new ComponentName(S1_SHARE_PACKAGE_NAME,
					S1_ACTIVITY_QZone_NAME);
		}
    intent.setComponent(component);
  if ( resourceId == 5 ){
      if (  null == resourceMap.get(LeResourceType.leVideoUrl) || "".equals(resourceMap.get(LeResourceType.leVideoUrl)) ) {
          Log.e(TAG, "video url is empty for qq imageAndText share");
          return false;
         } 
      resourceMap.put(LeResourceType.leWebUrl, resourceMap.get(LeResourceType.leVideoUrl));
         
         if (  null == resourceMap.get(LeResourceType.leTitle) || "".equals(resourceMap.get(LeResourceType.leTitle)) ) {
          Log.e(TAG, "title is empty for qq imageAndText share");
          intent.putExtra(LeResourceType.leTitle.name(), "来自乐视超级手机");
         } 
    }  else if ( resourceId == 4 ) {
      if (  null == resourceMap.get(LeResourceType.leMusicUrl) || "".equals(resourceMap.get(LeResourceType.leMusicUrl)) ) {
       Log.e(TAG, "music url is empty for qq imageAndText share");
       return false;
      } 
      resourceMap.put(LeResourceType.leWebUrl, resourceMap.get(LeResourceType.leMusicUrl));
      
      if (  null == resourceMap.get(LeResourceType.leTitle) || "".equals(resourceMap.get(LeResourceType.leTitle)) ) {
       Log.e(TAG, "title is empty for qq imageAndText share");
       intent.putExtra(LeResourceType.leTitle.name(), "来自乐视超级手机");
      } 
    }
    if( !setIntentValue(intent, resourceMap, mContext) ){
     return false;
    }
    if (  null == resourceMap.get(LeResourceType.leWebUrl) || "".equals(resourceMap.get(LeResourceType.leWebUrl)) ) {
    Log.e(TAG, "webPage url is empty for qq imageAndText share");
    return false;
   } 
   if (  null == resourceMap.get(LeResourceType.leTitle) || "".equals(resourceMap.get(LeResourceType.leTitle)) ) {
    Log.e(TAG, "title is empty for qq imageAndText share");
    resourceMap.put(LeResourceType.leTitle, "乐视分享");
   } 
   intent.putExtra("dataType", resourceId);
    mContext.startActivity(intent);
    return true;
   default:
            Log.e(TAG, "Only support WeiXin, Weibo or QQ at present.");
            return false;
        }
    }

    private static boolean setIntentValue(Intent intent, Map<LeResourceType, Object> resourceMap, Context mContext) {
        if (null != resourceMap.get(LeResourceType.leText)){
            if(resourceMap.get(LeResourceType.leText) instanceof String) {
                intent.putExtra(LeResourceType.leText.name(),
                        (String) resourceMap.get(LeResourceType.leText));
            } else {
                outputLog(" the value of LeResourceType.leText must be in type of String");
                return false;
            }
        }
        if (null != resourceMap.get(LeResourceType.leWebUrl)) {
            if (resourceMap.get(LeResourceType.leWebUrl) instanceof String) {
                intent.putExtra(LeResourceType.leWebUrl.name(),
                        (String) resourceMap.get(LeResourceType.leWebUrl));
            } else {
                outputLog(" the value of LeResourceType.leWebUrl must be in type of String");
                return false;
            }
        }
        if (null != resourceMap.get(LeResourceType.leBitmap)) {
            if (resourceMap.get(LeResourceType.leBitmap) instanceof Bitmap) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(LeResourceType.leBitmap.name(),
                        (Bitmap) resourceMap.get(LeResourceType.leBitmap));
                intent.putExtra(LeResourceType.leBitmap.name(), bundle);
            } else {
                outputLog(" the value of LeResourceType.leBitmap must be in type of String");
                return false;
            }
        }
        if (null != resourceMap.get(LeResourceType.leImagePath)) {
            if (resourceMap.get(LeResourceType.leImagePath) instanceof String) {
                String imagePath = (String) resourceMap
                        .get(LeResourceType.leImagePath);
                if ( imagePath.startsWith("content:")){
                    imagePath = getRealFilePath(mContext, Uri.parse(imagePath));
                }
                if( null == imagePath){
                    outputLog(" Convert string to uri failed! ");
                    return false;
                }
                if (imagePath.startsWith("file:")) {
                    imagePath = imagePath.substring(5);
                }
                intent.putExtra(LeResourceType.leImagePath.name(), imagePath);
            } else {
                outputLog(" the value of LeResourceType.leImagePath must be in type of String");
                return false;
            }
        }
        if (null != resourceMap.get(LeResourceType.leTitle)) {
            if (resourceMap.get(LeResourceType.leTitle) instanceof String) {
                intent.putExtra(LeResourceType.leTitle.name(),
                        (String) resourceMap.get(LeResourceType.leTitle));
            } else {
                outputLog(" the value of LeResourceType.leTitle must be in type of String");
                return false;
            }
        }
        if (null != resourceMap.get(LeResourceType.leMusicUrl)) {
            if (resourceMap.get(LeResourceType.leMusicUrl) instanceof String) {
                intent.putExtra(LeResourceType.leMusicUrl.name(),
                        (String) resourceMap.get(LeResourceType.leMusicUrl));
            } else {
                outputLog(" the value of LeResourceType.leMusicUrl must be in type of String");
                return false;
            }
        }
        if (null != resourceMap.get(LeResourceType.leVideoUrl)) {
            if (resourceMap.get(LeResourceType.leVideoUrl) instanceof String) {
                intent.putExtra(LeResourceType.leVideoUrl.name(),
                        (String) resourceMap.get(LeResourceType.leVideoUrl));
            } else {
                outputLog(" the value of LeResourceType.leVideoUrl must be in type of String");
                return false;
            }
        }
        if (null != resourceMap.get(LeResourceType.leFilePath)) {
            if (resourceMap.get(LeResourceType.leFilePath) instanceof String) {
                intent.putExtra(LeResourceType.leFilePath.name(),
                        (String) resourceMap.get(LeResourceType.leFilePath));
            } else {
                outputLog(" the value of LeResourceType.leFilePath must be in type of String");
                return false;
            }
        }
        
        return true;
    }
    
    private static void outputLog(String errorMsg){
        Log.e(TAG, errorMsg);
    }
    
    /**
     * Try to return the absolute file path from the given Uri
     *
     * @param context
     * @param uri
     * @return the file path or null
     */
    private static String getRealFilePath( final Context context, final Uri uri ) {
        if ( null == uri ) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if ( scheme == null )
            data = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();
        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri, new String[] { ImageColumns.DATA }, null, null, null );
            if ( null != cursor ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( ImageColumns.DATA );
                    if ( index > -1 ) {
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

}
