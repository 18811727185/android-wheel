package com.letv.statistic.http;

import com.letv.mobile.http.parameter.LetvBaseParameter;
import com.letv.statistic.ReportTool;

import android.text.TextUtils;

/**
 * @author lilong
 *         数据用的Parameter类
 */
public class BaseParameter extends LetvBaseParameter {
    private static final long serialVersionUID = 7136389972395993930L;

    public static final String NULL = "-";// 代表空值

    /** 日志版本号 */
    private static final String VER = "ver";
    /** 一级业务线代码，固定值 */
    private static final String P1 = "p1";
    /** 二级业务线代码，固定值 */
    private static final String P2 = "p2";
    /** 三级业务线代码，tv_ + tvlive版本号 */
    private static final String P3 = "p3";
    /** 时间戳 */
    private static final String R = "r";
    /** 有线mac地址 */
    private static final String MAC = "mac";

    public BaseParameter combineParams() {
        myPut(VER, ReportTool.getVer());
        myPut(P1, ReportTool.getP1());
        myPut(P2, ReportTool.getP2());
        myPut(P3, ReportTool.getP3());
        myPut(R, ReportTool.getR());
        myPut(MAC, ReportTool.getMacWithoutColon());
        return this;
    }

    public void myPut(String key, String value) {
        if (TextUtils.isEmpty(value)) {
            this.put(key, NULL);
        } else {
            this.put(key, value);
        }
    }

    public void myPut(String key, Long value) {
        if (value == null) {
            this.put(key, NULL);
        } else {
            this.put(key, value);
        }
    }

    public void myPut(String key, Integer value) {
        if (value == null) {
            this.put(key, NULL);
        } else {
            this.put(key, value);
        }
    }

}
