/**
 *
 * Copyright 2015 LeTV Technology Co. Ltd., Inc. All rights reserved.
 *
 * @Author : qingxia
 *
 * @Description :
 *
 */

package com.letv.mobile.login.model;

import java.io.Serializable;

import com.letv.mobile.http.model.LetvHttpBaseModel;

/**
 * http://10.154.156.205/mobile/user/tokenLogin.json?token={token}
 * 参数：token 类型 String 是否必传：是
 * 结果：
 * {
 * "status":{status},
 * "data":{
 * "uid":{uid},
 * "userName":{userName},
 * "nickName":{nickName},
 * "picture":{picture}
 * }
 * }
 * 或
 * {
 * "status":{status},
 * "errorCode":{errorCode},
 * "errorMessage":{errorMessage}
 * }
 * 主要字段说明：
 * status:Integer，1业务正常，0业务异常
 * errorCode:String 业务异常码，status为0时返回此字段
 * errorMessage:String 业务异常方案，status为0时返回此字段
 * 例如：
 * http://10.154.156.205/mobile/user/tokenLogin.json?token=10382
 * b8d19P03h0leUyrAwileFMA3x5lz5ZXfbjzKl3N4VKsSzOTBOn5JuZQvYYpORdcO3
 * {
 * "status":1,
 * "data":{
 * "uid":82097576,
 * "userName":"letv_54b0fd0b4f2ee73",
 * "nickName":"1303xxx4997_956",
 * "picture":
 * "http://i1.letvimg.com/img/201207/30/tx298.png,http://i0.letvimg.com/img/201207/30/tx200.png,http://i0.letvimg.com/img/201207/30/tx70.png,http://i3.letvimg.com/img/201207/30/tx50.png"
 * }
 * }
 * 或
 * http://10.154.156.205/mobile/user/tokenLogin.json?token=10382
 * b8d19P03h0leUyrAwileFMA3x5lz5ZXfjzKl3N4VKsSzOTBOn5JuZQvYYpORdcO3
 * {
 * "status": 1,
 * "errorCode": "0001",
 * "errorMessage": "token已失效"
 * }
 * @author xiaqing
 */
public class UserInfo extends LetvHttpBaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username = ""; // 用户名

    private String nickName = ""; // 登录名

    private String uid = "";

    private String picture; // 用户头像地址

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPicture() {
        return this.picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getNickName() {
        return this.nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    @Override
    public String toString() {
        return "UserInfo [username=" + this.username + ", nickname="
                + this.nickName + ", uid=" + this.uid + ", picture="
                + this.picture + "]";
    }
}
