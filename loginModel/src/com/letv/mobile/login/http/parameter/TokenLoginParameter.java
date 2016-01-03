/**
 *
 * Copyright 2015 LeTV Technology Co. Ltd., Inc. All rights reserved.
 *
 * @Author : qingxia
 *
 * @Description :
 *
 */

package com.letv.mobile.login.http.parameter;

import com.letv.mobile.http.parameter.LetvBaseParameter;

public class TokenLoginParameter extends LoginHttpCommonParameter {

    private static final long serialVersionUID = 5034410253150375279L;

    private final String TOKEN = "token";
    private String mToken = "";

    private LetvBaseParameter mParameter;

    public TokenLoginParameter(String token) {
        super();
        this.mToken = token;
    }

    @Override
    public LetvBaseParameter combineParams() {
        this.mParameter = super.combineParams();
        this.mParameter.put(this.TOKEN, this.mToken);
        return this.mParameter;
    }

}
