package com.letv.statistic.http;

/**
 * @author lilong
 *         播放上报参数
 */
public class PlayParameter extends BaseParameter {

    private static final long serialVersionUID = -1903697672275157413L;

    /**
     * ac=init/play/block/time/end
     * init：播放器初始化；play：正片开始播放，播放广告不算正片；end：播放结束；time的上报规则见“pt“字段，
     * 具体描述见表下方，block卡顿开始，eblock卡顿结束
     */
    private static final String AC = "ac";

    /**
     * pt=数字，单位：秒
     * pt表示播放时长。只有ac字段为time的play日志才上报此字段。
     * 点播/轮播/直播上报规则：首次15秒上报，然后相隔1分钟上报，播放时长为60；此后每隔3分钟上报一次，最后用户结束播放时上报一次计时，
     * 每次pt上报的时间只取距离上次time上报pt的时间差，不需要累计。用户暂停及卡顿时不需要上报，播放结束时上报的pt以结束时的时长上报，不能报固定值。
     */
    private static final String PT = "pt";

    /** uid=用户注册id(未注册或未登录的报“-”) */
    private static final String UID = "uid";

    /** MAC明文地址 */
    private static final String AUID = "auid";

    /**
     * uuid=MAC地址+时间戳+"_0"
     * uuid用于标识一次播放过程。uuid在一次播放过程中保持不变。
     * 如果一次播放过程出现了切换码率，那么uuid的后缀变为1；
     */
    private static final String UUID = "uuid";

    /**
     * cid=频道id
     * cid明细见“cid定义”选项卡，点播，直播都要报这个字段
     */
    private static final String CID = "cid";

    /**
     * ty=0/1/2
     * 0：点播；1：直播；2：轮播
     */
    private static final String TY = "ty";

    /**
     * vt=点播上报码流参数/直播上报码流信息
     * 是Stream中的rateType
     */
    private static final String VT = "vt";

    /**
     * ref=上一个页面的页面id
     * 上一个页面为站外页面时该字段报“-”，页面id明细见“页面id定义“选项卡
     */
    private static final String REF = "ref";

    /**
     * pv=播放器版本
     */
    private static final String PV = "pv";

    /**
     * channelEname
     */
    private static final String ST = "st";

    /**
     * 0：是登录用户 1：非登录用户
     */
    private static final String ILU = "ilu";

    /**
     * lid=直播节目id channelId
     * lid是直播节目的唯一标识，直播和轮播节目都报
     */
    private static final String LID = "lid";

    /**
     * urrent time：时间戳，单位 毫秒，例如：1423793629851表示2015/2/13 10:13:49.851
     * [必填]播放动作发生时客户端的时间全端都需要上报
     */
    private static final String CTIME = "ctime";

    /**
     * 心跳上报，轮播台的时候上报vid
     */
    private static final String VID = "vid";

    /**
     * CDE version：CDE版本号
     * 播放动作ac=init时必须上报，其他动作时不上报。
     */
    private String CDEV = "cdev";

    /**
     * CDE App ID：cde为每个app指定的唯一ID
     * 播放动作ac=init时必须上报，其他动作时不上报
     */
    private String CAID = "caid";

    /**
     * 收费、免费
     * 0：免费 1：收费视频试看 2：付费观看 ；播放动作ac=play时必须上报，其他动作时不上报。
     */
    private String PAY = "pay";

    /**
     * 启播类型
     * 0：直接点播 1：连播 2：切换码流
     */
    private String IPT = "ipt";

    private String vid;
    private String ac;
    private Long pt;
    private String uid;
    private String auid;
    private String uuid;
    private String cid;
    private String ty;
    private String vt;
    private String ref;
    private String pv;
    private String st;
    private String ilu;
    private String lid;
    private Long ctime;
    private String cdev;
    private String caid;
    private String pay;
    private String ipt;

    @Override
    public BaseParameter combineParams() {
        super.combineParams();
        myPut(AC, ac);
        myPut(PT, pt);
        myPut(UID, uid);
        myPut(AUID, auid);
        myPut(UUID, uuid);
        myPut(CID, cid);
        myPut(TY, ty);
        myPut(VT, vt);
        myPut(REF, ref);
        myPut(PV, pv);
        myPut(ST, st);
        myPut(ILU, ilu);
        myPut(LID, lid);
        myPut(CTIME, ctime);
        myPut(VID, vid);
        myPut(CDEV, cdev);
        myPut(CAID, caid);
        myPut(PAY, pay);
        myPut(IPT, ipt);
        return this;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public void setAc(String ac) {
        this.ac = ac;
    }

    public void setPt(Long pt) {
        this.pt = pt;
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

    public void setCid(String cid) {
        this.cid = cid;
    }

    public void setTy(String ty) {
        this.ty = ty;
    }

    public void setVt(String vt) {
        this.vt = vt;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public void setPv(String pv) {
        this.pv = pv;
    }

    public void setSt(String st) {
        this.st = st;
    }

    public void setIlu(String ilu) {
        this.ilu = ilu;
    }

    public void setLid(String lid) {
        this.lid = lid;
    }

    public void setCtime(Long ctime) {
        this.ctime = ctime;
    }

    public void setCdev(String cdev) {
        this.cdev = cdev;
    }

    public void setCaid(String caid) {
        this.caid = caid;
    }

    public void setPay(String pay) {
        this.pay = pay;
    }

    public void setIpt(String ipt) {
        this.ipt = ipt;
    }
}
