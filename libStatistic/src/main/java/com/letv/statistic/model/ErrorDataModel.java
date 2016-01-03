package com.letv.statistic.model;

/**
 * @author lilong
 *         错误上报的参数传递
 */
public class ErrorDataModel {

    /**
     * 错误类型
     * 如果为播放错误时，上报内容为pl，其他错误不需要上报该字段。
     */
    private String et;

    /**
     * 错误代码
     * 参照error对照表
     */
    private String err;

    /**
     * er property 错误属性
     * 业务线自己维护，可以存储任何值或者多个值，但是必须要进行URL编码，例如：ep上报值为k1=v1&k2=v2，k1=v1&k2=v2
     * 应该URL编码成：k1%3dv1%26k2%3dv2，也就是上报的内容为：ep=k1%3dv1%26k2%3dv2
     */
    private String ep;

    /**
     * 分类ID
     * [选填] 注：如果上报则cid、pid、vid必须与媒资库中的关系对应。
     */
    private String cid;

    /**
     * 视频id
     * [选填] 注：如果上报则cid、pid、vid必须与媒资库中的关系对应。
     */
    private String vid;

    /**
     * uuid=MAC地址+时间戳+"_0"
     * uuid用于标识一次播放过程。uuid在一次播放过程中保持不变。
     * 如果一次播放过程出现了切换码率，那么uuid的后缀变为1；
     */
    private String uuid;

    /**
     * liveId
     */
    private String lid;

    /**
     * 轮播台英文名
     * channelEname
     */
    private String st;

    public ErrorDataModel() {

    }

    public String getEt() {
        return et;
    }

    public void setEt(String et) {
        this.et = et;
    }

    public String getErr() {
        return err;
    }

    public void setErr(String err) {
        this.err = err;
    }

    public String getEp() {
        return ep;
    }

    public void setEp(String ep) {
        this.ep = ep;
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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
