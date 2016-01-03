package com.letv.statistic.http;

/**
 * @author lilong
 *         env上报参数
 */
public class EnvParameter extends BaseParameter {

    private static final long serialVersionUID = -6819808012260336388L;

    /** 设备ID_当前时间戳 */
    private static final String UUID = "uuid";
    /** ip地址 */
    private static final String IP = "ip";
    /** 上网类型 */
    private static final String NT = "nt";
    /** 操作系统，值为英文字符串 */
    private static final String OS = "os";
    /** 操作系统版本号 , 值为版本号字符串 */
    private static final String OSV = "osv";
    /** tvlive版本号，值为版本号字符串 */
    private static final String APP = "app";
    /** 品牌 , 品牌自由版：letv 国广:media 海信:Hisense */
    private static final String BD = "bd";
    /** 机器型号，值为机器型号字符串，url编码 */
    private static final String XH = "xh";
    /** 环境上报时用，表示某个环境上报所跟随的时机 , 这里是固定值pl */
    private static final String SRC = "src";
    /** wifi标识，上报无线名字，例如letvoffice，有线连接时不上报此字段 */
    private static final String SSID = "ssid";
    /** 伴随环境日志上报，用来标tv版的启动，一次进入TV版过程中，上报只上报唯一值，退出tv版再进入重新生成 */
    private static final String APPRUNID = "apprunid";
    /** 时间戳 */
    private static final String CTIME = "ctime";

    private String uuid;
    private String ip;
    private String nt;
    private String os;
    private String osv;
    private String app;
    private String bd;
    private String xh;
    private String src;
    private String ssid;
    private String apprunid;
    private Long ctime;

    @Override
    public BaseParameter combineParams() {
        super.combineParams();
        myPut(UUID, uuid);
        myPut(IP, ip);
        myPut(NT, nt);
        myPut(OS, os);
        myPut(OSV, osv);
        myPut(APP, app);
        myPut(BD, bd);
        myPut(XH, xh);
        myPut(SRC, src);
        myPut(SSID, ssid);
        myPut(APPRUNID, apprunid);
        myPut(CTIME, ctime);
        return this;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setNt(String nt) {
        this.nt = nt;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public void setOsv(String osv) {
        this.osv = osv;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public void setBd(String bd) {
        this.bd = bd;
    }

    public void setXh(String xh) {
        this.xh = xh;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public void setApprunid(String apprunid) {
        this.apprunid = apprunid;
    }

    public void setCtime(Long ctime) {
        this.ctime = ctime;
    }
}
