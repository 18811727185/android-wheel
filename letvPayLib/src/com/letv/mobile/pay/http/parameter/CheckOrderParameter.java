package com.letv.mobile.pay.http.parameter;

import com.letv.mobile.http.parameter.LetvBaseParameter;

public class CheckOrderParameter extends HttpCommonParameter {

    private static final long serialVersionUID = 1L;

    private final String ORDER_ID = "orderId";
    private final String orderId;

    public CheckOrderParameter(String orderId) {
        super();
        this.orderId = orderId;
    }

    @Override
    public LetvBaseParameter combineParams() {
        LetvBaseParameter parameter = super.combineParams();
        parameter.put(this.ORDER_ID, this.orderId);
        return parameter;
    }

}
