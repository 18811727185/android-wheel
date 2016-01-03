package com.letv.mobile.pay.model;

public class PayConstants {

    /** 支付渠道 **/
    public static final int PAY_CHANNEL_ALIPAY = 3; // 支付渠道id，支付宝
    public static final int PAY_CHANNEL_WEIXIN = 55; // 微信支付
    public static final int PAY_CHANNEL_WEIXIN_FOR_LIVE = 56; // super live中微信支付

    /** 购买类型 **/
    public static final int PURCHASE_TYPE_VIDEO_SINGLE_PAY = 1; // 影片单点
    public static final int PURCHASE_TYPE_VIP_PACKAGE = 2; // 会员套餐
    public static final int PURCHASE_TYPE_LIVE_SINGLE_PAY = 3; // 直播单点
    public static final int PURCHASE_TYPE_SUPER_LIVE_PAY = 4; // super lvie中付费

    /** 会员类型 **/
    public static final int VIP_TYPE_ORDINARY = 1; // 普通会员
    public static final int VIP_TYPE_SENIOR = 9; // 高级会员

    public static final String APP_ID_LETV_LEADING = "wxec1aaf0d01e71b0b";
    public static final String APP_ID_SUPER_LIVE = "wx117d4a127e22ef6a";

    public static final String ACTION_PAY_RESULT = "com.letv.mobile.pay"; // 支付结果广播action
    public static final String PAY_RESULT = "pay_result"; // 支付结果
    public static final String PURCHASE_TYPE = "purchase_type"; // 购买类型,参见PURCHASE_TYPE_*常量值
    public static final String PRODUCT_ID = "product_id"; // 商品id

}
