package com.letv.mobile.core.log.critical;

import com.letv.mobile.core.config.LeTVConfig;
import com.letv.mobile.core.log.Logger;
import com.letv.mobile.core.utils.MD5Util;
import com.letv.mobile.core.utils.StringUtils;

public class CriticalPathLogUtil {

    private static final String DEFAULT_TAG = "critical_path_log";

    /**
     * 生成消息
     * @param pageCode
     *            页面唯一code
     * @param msg
     *            日志消息
     * @return 返回形如“letv_4c84365b46aa64a7_msg” or "leso_4c84365b46aa64a7_msg"
     */
    private static String generateLogMsg(BaseCriticalPathInterface pathLog,
            String msg) {
        return LeTVConfig.getsAppName() + "_" + pathLog.getmCode() + "_" + msg;
    }

    /**
     * 打印关键路径log。统一使用log.e()函数
     * @param msg
     *            使用generateLogMsg()函数生成
     */
    public static void log(BaseCriticalPathInterface pathLog, String msg) {
        if (pathLog == null || StringUtils.isBlank(msg)) {
            return;
        }
        String newMsg = generateLogMsg(pathLog, msg);
        Logger.e(DEFAULT_TAG, newMsg);
    }

    /**
     * 生成唯一类名对应码
     * 使用方式：调用此方法用simpleClassName生成类的对应码，会在tag=classNameCode的日志中打印出来，
     * 然后将其加入到CriticalPathLogUtil中，使用“类名 + _CODE”形式
     * 注：生成后删除此段方法。此方法仅为生成code，不计入代码逻辑。
     * @param simpleClassName
     *            请使用报名+类名方式，如：com.letv.core.log.CriticalPathLogUtil
     */
    public static void generateClassNameCode(String simpleClassName) {
        String classNameCode = MD5Util.to16bitMd5(simpleClassName);
        Logger.e("classNameCode", simpleClassName + " — classNameCode = "
                + classNameCode);
    }
}
