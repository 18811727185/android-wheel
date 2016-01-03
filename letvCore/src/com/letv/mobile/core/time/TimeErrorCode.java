package com.letv.mobile.core.time;

public enum TimeErrorCode {
    TIME_ERROR_OK(0, ""),
    TIME_ERROR_FETCH_ERROR(1, "Fetch time error");

    public final int errorCode;
    public final String errorString;

    private TimeErrorCode(int errorCd, String errorStr) {
        this.errorCode = errorCd;
        this.errorString = errorStr;
    }
}
