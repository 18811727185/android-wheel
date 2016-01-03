package com.letv.mobile.login.http;

public class LoginHttpContants {

    // http公共请求参数,同letv AppConfig中属性值,暂时写法
    public static final String TERMINAL_BRAND = "letv";
    public static final String BS_CHANNEL = "_";
    public static final String APPLICATION_ID = "1001";
    public static final String CLIENT = "android";

    /************************** 用户 ***************************/
    /**
     * 1 Token登录
     */
    public static final String TOKEN_LOGIN = "/live/user/tokenLogin.json";

    /**
     * 2 获取用户信息
     */
    public static final String GET_USER_ACCOUNT_INFO = "/live/vip/getAccount.json";

    /**
     * 3 查询机卡绑定
     */
    public static final String GET_DEVICE_BIND = "/live/vip/devicebind/get.json";

    /**
     * 4 领取机卡绑定
     */
    public static final String RECEIVE_DEVICE_BIND = "/live/vip/devicebind/receive.json";

}
