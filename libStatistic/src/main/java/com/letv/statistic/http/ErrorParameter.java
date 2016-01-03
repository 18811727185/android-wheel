package com.letv.statistic.http;

/**
 * @author lilong
 *         Error上报参数
 */
public class ErrorParameter extends BaseParameter {

    private static final long serialVersionUID = -9213263399534175005L;

    /**
     * 错误类型
     * 如果为播放错误时，上报内容为pl，其他错误不需要上报该字段。
     */
    private static final String ET = "et";

    /**
     * 错误代码
     * 参照error对照表
     */
    private static final String ERR = "err";

    /**
     * mac
     */
    private static final String AUID = "auid";

    /**
     * Net type:上网类型
     * [必填] 参见：附表十五：网络类型 注：pc端web由于无法区分网络类型无需上报。
     */
    private static final String NT = "nt";

    /**
     * er property 错误属性
     * 业务线自己维护，可以存储任何值或者多个值，但是必须要进行URL编码，例如：ep上报值为k1=v1&k2=v2，k1=v1&k2=v2
     * 应该URL编码成：k1%3dv1%26k2%3dv2，也就是上报的内容为：ep=k1%3dv1%26k2%3dv2
     */
    private static final String EP = "ep";

    /**
     * 分类ID
     * [选填] 注：如果上报则cid、pid、vid必须与媒资库中的关系对应。
     */
    private static final String CID = "cid";

    /**
     * 视频id
     * [选填] 注：如果上报则cid、pid、vid必须与媒资库中的关系对应。
     */
    private static final String VID = "vid";

    /**
     * uuid=MAC地址+时间戳+"_0"
     * uuid用于标识一次播放过程。uuid在一次播放过程中保持不变。
     * 如果一次播放过程出现了切换码率，那么uuid的后缀变为1；
     */
    private static final String UUID = "uuid";

    /**
     * app_run_id：每次启动时生成的唯一标识
     * 【必填】apprunid在每次启动时生成，且唯一，下次打开时重新生成，标识每一次启动到关闭的会话。生成规则：基于web的上报sessionid；非基于web的上报“设备id _
     * 时间戳”。
     */
    private static final String APPRUNID = "apprunid";

    /**
     * liveId
     */
    private static final String LID = "lid";

    /**
     * 轮播台英文名
     * channelEname
     */
    private static final String ST = "st";

    private String et;
    private String err;
    private String auid;
    private String nt;
    private String ep;
    private String cid;
    private String vid;
    private String uuid;
    private String apprunid;
    private String lid;
    private String st;

    @Override
    public BaseParameter combineParams() {
        super.combineParams();
        myPut(ET, et);
        myPut(ERR, err);
        myPut(AUID, auid);
        myPut(NT, nt);
        myPut(EP, ep);
        myPut(CID, cid);
        myPut(VID, vid);
        myPut(UUID, uuid);
        myPut(APPRUNID, apprunid);
        myPut(LID, lid);
        myPut(ST, st);
        return this;
    }

    public void setEt(String et) {
        this.et = et;
    }

    public void setErr(String err) {
        this.err = err;
    }

    public void setAuid(String auid) {
        this.auid = auid;
    }

    public void setNt(String nt) {
        this.nt = nt;
    }

    public void setEp(String ep) {
        this.ep = ep;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setApprunid(String apprunid) {
        this.apprunid = apprunid;
    }

    public void setLid(String lid) {
        this.lid = lid;
    }

    public void setSt(String st) {
        this.st = st;
    }
}
