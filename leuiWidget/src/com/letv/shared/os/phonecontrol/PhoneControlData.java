package com.letv.shared.os.phonecontrol;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 尝试解锁返数据对像
 * 手机是否激活
 * @author fengzihua
 * @since 2014.10.27
 * @hide
 */
public class PhoneControlData implements Parcelable {
    
    // 手机是否绑定，(1byte)
    public static final int CONTROL_STATUS_BIND        = 0;
    public static final int CONTROL_STATUS_BIND_UNBIND       = 0;    // 未绑定letv账号，默认状态
    public static final int CONTROL_STATUS_BIND_BIND             = 1;    // 已绑定letv账号
    
    // 手机是否锁定，(1byte)
    public static final int CONTROL_STATUS_LOCK        = 1;
    public static final int CONTROL_STATUS_LOCK_UNLOCK       = 0;    // 手机未锁定，默认状态
    public static final int CONTROL_STATUS_LOCK_LOCKED        = 1;    // 手机已锁定
    
    // 手机是否已清楚数据，(1byte)
    public static final int CONTROL_STATUS_WIPE        = 2;
    public static final int CONTROL_STATUS_WIPE_UNWIPE        = 0;    // 初始状态，默认状态
    public static final int CONTROL_STATUS_WIPE_WIPING        = 1;    // android层置为清楚状态，待recovery清空数据后，置为已清楚状态
    public static final int CONTROL_STATUS_WIPE_WIPE             = 2;    // 已清楚，待下次上报
    
    // 手机是否已经激活
    public static final int PHONE_STATUS_ACTIVATE       = 3;    // 手机是否已激活
    public static final int PHONE_STATUS_ACTIVATE_INIT            = 0;    // 手机未激活，默认状态
    public static final int PHONE_STATUS_ACTIVATE_ACTIVATE  = 1;    // 手机已激活
    public static final int PHONE_STATUS_ACTIVATE_TIMER        = 2;    // 手机已激活, 且连续上报达半个小时
    public static final int PHONE_STATUS_ACTIVATE_INVALID      = 999;    // 手机激活状态不正常
    
    // 请求访问结果码
    public static final int UNLOCKPHONE_ERROR_UNKNOWN                            = 0; //未知错误
    public static final int UNLOCKPHONE_SUCCESS                                                = 1; //接口访问成功
    public static final int UNLOCKPHONE_SERVER_ERROR                                   = 2; //服务器端错误
    public static final int UNLOCKPHONE_ERROR_NO_NETWORK                     = 3; //当前无网络错误
    public static final int UNLOCKPHONE_ERROR_SERVICE_NO_READY           = 4; //服务未准备好(push服务注册或是业务服务还未注册成功)
    public static final int UNLOCKPHONE_ERROR_PSW                                          = 5; //解锁密码错误
    public static final int UNLOCKPHONE_ERROR_COUNT_LIMIT                       = 6; //超过了次数限制，目前只是预留
    
    public final int mResultCode;  // 访问结果code
    public String mDescribe;       // 结果描述 为了答应出来
    
    public PhoneControlData(int resultCode, String describe) {
        mResultCode = resultCode;
        mDescribe = describe;
    }
    
    @Override public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("PhoneControlData: {");
        sb.append("ResultCode: ");
        sb.append(mResultCode);sb.append(" ");
        sb.append("Describe: ");
        sb.append(mDescribe);
        sb.append("} ");
        return sb.toString();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mResultCode);
        dest.writeString(mDescribe);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<PhoneControlData> CREATOR = new Parcelable.Creator<PhoneControlData>() {

        @Override
        public PhoneControlData createFromParcel(Parcel source) {
            return new PhoneControlData(source.readInt(), source.readString());
        }

        @Override
        public PhoneControlData[] newArray(int size) {
            return new PhoneControlData[size];
        }
    };
}