package com.letv.statistic.model;

/**
 * @author lilong
 *         播放上报的参数传递
 */
public class PlayDataModel {

    /**
     * 直播节目单上报，轮播节目单插入直播节目上报
     */
    private String vid;
    /**
     * ac=init/play/block/time/end
     * init：播放器初始化；play：正片开始播放，播放广告不算正片；end：播放结束；time的上报规则见“pt“字段，
     * 具体描述见表下方，block卡顿开始，eblock卡顿结束
     */
    private String ac;

    /**
     * pt=数字，单位：秒
     * pt表示播放时长。只有ac字段为time的play日志才上报此字段。
     * 点播/轮播/直播上报规则：首次15秒上报，然后相隔1分钟上报，播放时长为60；此后每隔3分钟上报一次，最后用户结束播放时上报一次计时，
     * 每次pt上报的时间只取距离上次time上报pt的时间差，不需要累计。用户暂停及卡顿时不需要上报，播放结束时上报的pt以结束时的时长上报，不能报固定值。
     */
    private Long pt;

    /**
     * 一次播放过程，播放器生成唯一的UUID, 如果一次播放过程出现了切换码率，那么uuid的后缀加1
     */
    private String uuid;

    /**
     * cid=频道id
     * cid明细见“cid定义”选项卡，点播，直播都要报这个字段
     */
    private String cid;

    /**
     * ty=0/1/2
     * 0：点播；1：直播；2：轮播
     */
    private String ty = "ty";

    /**
     * vt=点播上报码流参数/直播上报码流信息
     * 点播上报码流参数信息，直播上报code信息
     */
    private String vt;

    /**
     * ref=上一个页面的页面id
     * 上一个页面为站外页面时该字段报“-”，页面id明细见“页面id定义“选项卡
     */
    private String ref;

    /**
     * 直播：直播code，其它播放类型：st=-
     */
    private String st;

    /**
     * lid=直播节目id
     * lid是直播节目的唯一标识，直播必报此字段，点播和轮播不报
     */
    private String lid;

    /**
     * CDE version：CDE版本号
     * 播放动作ac=init时必须上报，其他动作时不上报。
     */
    private String cdev;

    /**
     * CDE App ID：cde为每个app指定的唯一ID
     * 播放动作ac=init时必须上报，其他动作时不上报
     */
    private String caid;

    /**
     * 收费、免费
     * 0：免费 1：收费视频试看 2：付费观看 ；播放动作ac=play时必须上报，其他动作时不上报。
     */
    private String pay;

    /**
     * 启播类型
     * 0：直接点播 1：连播 2：切换码流
     */
    private String ipt;

    public PlayDataModel() {
    }

    public PlayDataModel(String ac, Long pt, String uuid, String cid, String ty, String vt,
            String ref, String st, String lid, String vid, String cdev, String caid, String pay,
            String ipt) {
        this.ac = ac;
        this.pt = pt;
        this.uuid = uuid;
        this.cid = cid;
        this.ty = ty;
        this.vt = vt;
        this.ref = ref;
        this.st = st;
        this.lid = lid;
        this.vid = vid;
        this.cdev = cdev;
        this.caid = caid;
        this.pay = pay;
        this.ipt = ipt;
    }

    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public String getAc() {
        return ac;
    }

    public void setAc(String ac) {
        this.ac = ac;
    }

    public Long getPt() {
        return pt;
    }

    public void setPt(Long pt) {
        this.pt = pt;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getTy() {
        return ty;
    }

    public void setTy(String ty) {
        this.ty = ty;
    }

    public String getVt() {
        return vt;
    }

    public void setVt(String vt) {
        this.vt = vt;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getSt() {
        return st;
    }

    public void setSt(String st) {
        this.st = st;
    }

    public String getLid() {
        return lid;
    }

    public void setLid(String lid) {
        this.lid = lid;
    }

    public String getCdev() {
        return cdev;
    }

    public void setCdev(String cdev) {
        this.cdev = cdev;
    }

    public String getCaid() {
        return caid;
    }

    public void setCaid(String caid) {
        this.caid = caid;
    }

    public String getPay() {
        return pay;
    }

    public void setPay(String pay) {
        this.pay = pay;
    }

    public String getIpt() {
        return ipt;
    }

    public void setIpt(String ipt) {
        this.ipt = ipt;
    }
}
