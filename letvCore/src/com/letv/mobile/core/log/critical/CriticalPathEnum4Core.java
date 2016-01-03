package com.letv.mobile.core.log.critical;

public enum CriticalPathEnum4Core implements BaseCriticalPathInterface {
    DownloadManager("fe48f363cee22d74"),
    CacheTask("02fb16e5d46e18f0");

    private final String mCode;

    CriticalPathEnum4Core(String code) {
        this.mCode = code;
    }

    @Override
    public String getmCode() {
        return this.mCode;
    }
}
