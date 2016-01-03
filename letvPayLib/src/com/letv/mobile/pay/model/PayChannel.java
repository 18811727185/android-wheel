package com.letv.mobile.pay.model;

import com.letv.mobile.pay.R;

public enum PayChannel {

    ALI_PAY(PayConstants.PAY_CHANNEL_ALIPAY, R.string.paychannel_alipay,
            R.drawable.icon_alipay),
    WEIXIN_PAY(PayConstants.PAY_CHANNEL_WEIXIN, R.string.paychannel_weixin,
            R.drawable.icon_weixin),
    WEIXIN_PAY_FOR_LIVE(PayConstants.PAY_CHANNEL_WEIXIN_FOR_LIVE,
            R.string.paychannel_weixin, R.drawable.icon_weixin);

    private final int id;
    private final int name;
    private final int icon;

    PayChannel(int id, int name, int icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    public int getId() {
        return this.id;
    }

    public int getName() {
        return this.name;
    }

    public int getIcon() {
        return this.icon;
    }

    public static PayChannel getPayChannel(int id) {
        for (PayChannel payChannel : PayChannel.values()) {
            if (payChannel.getId() == id) {
                return payChannel;
            }
        }
        return null;
    }

}
