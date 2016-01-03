package com.letv.statistic.model;

/**
 * @author lilong
 *         action上报的参数传递
 */
public class ActionDataModel {

    /** 动作码：打开，关闭，显示 */
    private String acode;

    /** 当前页面地址 */
    private String cur_url;

    /** Action result:动作结果 */
    private String ar;

    /** TODO(lilong) 含义 */
    private String ap;

    /** 所属一级分类的ID */
    private String cid;

    /** 视频ID */
    private String vid;

    /** 直播id=channelId */
    private String lid;

    /** 频道英文名 = ChannelInfo中channelEname */
    private String st;

    public ActionDataModel() {
    }

    public ActionDataModel(String acode, String cur_url, String ar, String ap, String cid,
            String vid, String lid, String st, String srcCid) {
        this.cur_url = cur_url;
        this.acode = acode;
        this.ar = ar;
        this.ap = ap;
        this.cid = cid;
        this.vid = vid;
        this.lid = lid;
        this.st = st;
    }

    public String getAcode() {
        return acode;
    }

    public void setAcode(String acode) {
        this.acode = acode;
    }

    public String getCur_url() {
        return cur_url;
    }

    public void setCur_url(String cur_url) {
        this.cur_url = cur_url;
    }

    public String getAr() {
        return ar;
    }

    public void setAr(String ar) {
        this.ar = ar;
    }

    public String getAp() {
        return ap;
    }

    public void setAp(String ap) {
        this.ap = ap;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public String getLid() {
        return lid;
    }

    public void setLid(String lid) {
        this.lid = lid;
    }

    public String getSt() {
        return st;
    }

    public void setSt(String st) {
        this.st = st;
    }

}
