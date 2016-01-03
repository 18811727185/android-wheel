package com.letv.mobile.pay.http.parameter;

import com.letv.mobile.http.parameter.LetvBaseParameter;

public class PurchaseOrderParameter extends HttpCommonParameter {

    private static final long serialVersionUID = 1L;

    private final String CORDERID = "corderid"; // 订单号　
    private final String PURCHASE_TYPE = "purchaseType"; // 消费类型
    private final String PRODUCTID = "productid"; // 商品id
    private final String PAYMENT_CHANNEL = "paymentChannel"; // 支付渠道id
    private final String PRICE = "price"; // 商品价格
    private final String VIP_TYPE = "vipType"; // 　会员类型 会员支付时必传
    private final String ACTIVITY_IDS = "activityIds";
    private final String PHONE = "phone"; // 手机号 手机支付时必传

    private String corderid;
    private int purchaseType;
    private String productid;
    private int paymentChannel;
    private String price;
    private int vipType;
    private String activityIds;
    private String phone;

    public PurchaseOrderParameter(String corderid, int purchaseType,
            String productid, int paymentChannel, String price, int vipType,
            String activityIds, String phone) {
        super();
        this.corderid = corderid;
        this.purchaseType = purchaseType;
        this.productid = productid;
        this.paymentChannel = paymentChannel;
        this.price = price;
        this.vipType = vipType;
        this.activityIds = activityIds;
        this.phone = phone;
    }

    public PurchaseOrderParameter(String vipType) {
        super();
    }

    @Override
    public LetvBaseParameter combineParams() {
        LetvBaseParameter parameter = super.combineParams();
        parameter.put(this.CORDERID, this.corderid);
        parameter.put(this.PURCHASE_TYPE, this.purchaseType);
        parameter.put(this.PRODUCTID, this.productid);
        parameter.put(this.PAYMENT_CHANNEL, this.paymentChannel);
        parameter.put(this.PRICE, this.price);
        parameter.put(this.VIP_TYPE, this.vipType);
        parameter.put(this.ACTIVITY_IDS, this.activityIds);
        parameter.put(this.PHONE, this.phone);
        return parameter;
    }

}
