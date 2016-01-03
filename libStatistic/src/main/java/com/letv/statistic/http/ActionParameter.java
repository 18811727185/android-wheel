package com.letv.statistic.http;

/**
 * @author lilong
 *         Action上报参数
 */
public class ActionParameter extends BaseParameter {

    private static final long serialVersionUID = -9213263399534175005L;

    /** 动作码 */
    private static final String ACODE = "acode";
    /** 当前页面地址 */
    private static final String CUR_URL = "cur_url";
    /** Action result:动作结果 */
    private static final String AR = "ar";
    /** TODO 不明字段 */
    private static final String AP = "ap";
    /** 所属一级分类的ID */
    private static final String CID = "cid";
    /** 视频ID */
    private static final String VID = "vid";
    /** 用户注册id，未注册或未登录的报“-” */
    private static final String UID = "uid";
    /** 直播id */
    private static final String LID = "lid";
    /** 设备ID_当前时间戳_数字 */
    private static final String UUID = "uuid";
    /** MAC明文地址 */
    private static final String AUID = "auid";
    /** 登录标志：是登录用户 1：非登录用户 0 */
    private static final String ILU = "ilu";
    /** 时间戳 */
    private static final String CTIME = "ctime";
    /** 频道英文名 */
    private static final String ST = "st";

    private String acode;
    private String cur_url;
    private String ar;
    private String ap;
    private String cid;
    private String vid;
    private String uid;
    private String lid;
    private String uuid;
    private String auid;
    private String ilu;
    private Long ctime;
    private String st;

    @Override
    public BaseParameter combineParams() {
        super.combineParams();
        myPut(ACODE, acode);
        myPut(CUR_URL, cur_url);
        myPut(AR, ar);
        myPut(AP, ap);
        myPut(CID, cid);
        myPut(VID, vid);
        myPut(UID, uid);
        myPut(LID, lid);
        myPut(UUID, uuid);
        myPut(AUID, auid);
        myPut(ILU, ilu);
        myPut(CTIME, ctime);
        myPut(ST, st);
        return this;
    }

    public void setAcode(String acode) {
        this.acode = acode;
    }

    public void setCur_url(String cur_url) {
        this.cur_url = cur_url;
    }

    public void setAr(String ar) {
        this.ar = ar;
    }

    public void setAp(String ap) {
        this.ap = ap;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setLid(String lid) {
        this.lid = lid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setAuid(String auid) {
        this.auid = auid;
    }

    public void setIlu(String ilu) {
        this.ilu = ilu;
    }

    public void setCtime(Long ctime) {
        this.ctime = ctime;
    }

    public void setSt(String st) {
        this.st = st;
    }

}
