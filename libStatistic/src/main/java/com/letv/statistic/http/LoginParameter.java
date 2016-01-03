package com.letv.statistic.http;

/**
 * @author lilong
 *         login上报参数
 */
public class LoginParameter extends BaseParameter {

    private static final long serialVersionUID = -6819808012260336388L;

    /** 用户注册id，未注册或未登录的报“-” */
    private static final String UID = "uid";
    /** MAC明文地址 */
    private static final String AUID = "auid";
    /** 设备ID_当前时间戳_数字 */
    private static final String UUID = "uuid";
    /** TODO(lilong) 不明字段 , 固定值initative_login */
    private static final String LP = "lp";
    /** 上一个页面为站外页面时该字段报“-”，页面id明细见“页面id定义“选项卡 */
    private static final String REF = "ref";
    /** 时间戳 , 登录tvlive时间 */
    private static final String TS = "ts";
    /** 0:登录成功 1:退出登录 */
    private static final String ST = "st";

    private String uid;
    private String auid;
    private String uuid;
    private String lp;
    private String ref;
    private String ts;
    private String st;

    @Override
    public BaseParameter combineParams() {
        super.combineParams();
        myPut(UID, uid);
        myPut(AUID, auid);
        myPut(UUID, uuid);
        myPut(LP, lp);
        myPut(REF, ref);
        myPut(TS, ts);
        myPut(ST, st);
        return this;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setAuid(String auid) {
        this.auid = auid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setLp(String lp) {
        this.lp = lp;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public void setSt(String st) {
        this.st = st;
    }
}
