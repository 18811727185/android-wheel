package com.letv.dynamicconfig.http;

import com.letv.mobile.http.model.LetvHttpBaseModel;

/**
 * Created by shibin on 16/4/22.
 */
public class GetConfigResponse extends LetvHttpBaseModel {

    private String version;
    private String configInfo;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getConfigInfo() {
        return configInfo;
    }

    public void setConfigInfo(String configInfo) {
        this.configInfo = configInfo;
    }

}
