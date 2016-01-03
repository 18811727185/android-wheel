package com.letv.mobile.pay.payment.alipay;

import android.text.TextUtils;

public class AliPayResult {
    private String resultStatus;
    private String result;
    private String memo;

    public AliPayResult(String rawResult) {

        if (TextUtils.isEmpty(rawResult)) {
            return;
        }

        String[] resultParams = rawResult.split(";");
        for (String resultParam : resultParams) {
            if (resultParam.startsWith("resultStatus")) {
                this.resultStatus = this.gatValue(resultParam, "resultStatus");
            }
            if (resultParam.startsWith("result")) {
                this.result = this.gatValue(resultParam, "result");
            }
            if (resultParam.startsWith("memo")) {
                this.memo = this.gatValue(resultParam, "memo");
            }
        }
    }

    public String getResultStatus() {
        return this.resultStatus;
    }

    public String getMemo() {
        return this.memo;
    }

    public String getResult() {
        return this.result;
    }

    @Override
    public String toString() {
        return "resultStatus={" + this.resultStatus + "};memo={" + this.memo
                + "};result={" + this.result + "}";
    }

    private String gatValue(String content, String key) {
        String prefix = key + "={";
        return content.substring(content.indexOf(prefix) + prefix.length(),
                content.lastIndexOf("}"));
    }
}
