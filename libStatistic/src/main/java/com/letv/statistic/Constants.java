package com.letv.statistic;

/**
 * @author lilong
 *         上报有关的常量
 */
public class Constants {

    // - - - - - - - - - 网络参数的值 - - - - - - - - - - - - - - - - - - -
    public static final String REPORT_DOMAIN = "http://apple.www.letv.com/";
    public static final String REPORT_ENV_PATH = "env/";
    public static final String REPORT_ACTION_PATH = "op/";
    public static final String REPORT_PLAY_PATH = "pl/";
    public static final String REPORT_LOGIN_PATH = "lg/";
    public static final String REPORT_ERROR_PATH = "er/";
    public static final int HTTP_READ_TIMEOUT = 3 * 1000;
    public static final int HTTP_CONNECT_TIMEOUT = 3 * 1000;

    // - - - - - - - - - - - - - 公共固定字段的值 - - - - - - - - - - - - - - - - - - - - - -
    /** 版本号 */
    public static final String VER = "2.0";
    /** 一级业务线代码：TV站 */
    public static final String P1 = "2";
    /** 二级业务线代码：轮播台 */
    public static final String P2 = "20";
    /** 固定字符串 */
    public static final String P3 = "20_tvlive";

    // - - - - - - - - - - 可变字段的值 - - - - - - - - - - - - - - - - - - -
    /** 动作码：打开 */
    public static final String ACODE_OPEN_0 = "0";
    public static final String ACODE_OPEN_15 = "15";
    public static final String ACODE_OPEN_32 = "32";
    /** 动作码：关闭 */
    public static final String ACODE_CLOSE = "31";
    /** 动作码：点击 */
    public static final String ACODE_CLICK = "0";
    /** 动作码：换台 */
    public static final String ACODE_CHANGE_CHANNEL = "16";

    /** 付费情况：免费 */
    public static final String PAY_FREE = "0";
    /** 付费情况：收费视频试看 */
    public static final String PAY_TRY = "1";
    /** 付费情况：付费观看 */
    public static final String PAY_PAY = "2";

    /** 起播类型：直接点播 */
    public static final String IPT_DIRECT = "0";
    /** 起播类型：连播 */
    public static final String IPT_CONSECUTIVE = "1";
    /** 起播类型：切换码流 */
    public static final String IPT_STREAM_CHANGED = "2";

    /** CDE App ID：cde为每个app指定的唯一ID */
    public static final String CAID = "1009";

    // 下面为cur_url的constants
    // - - - - - - - - - - - - - - - - - - - 超级列表首页 - - - - - - - - - - - - - - - - - - - - -
    /** 打开九宫格页面 */
    public static final String CUR_URL_OPEN_SUPER_LIST = "1.1";
    /** 点击首页直播中／轮播内容 */
    public static final String CUR_URL_CLICK_SUPER_LIST_LIVE_LUNBO_PROGRAM = "1.2.1";
    /** 点击首页未开始内容 */
    public static final String CUR_URL_CLICK_SUPER_LIST_LIVE_NOT_BEGIN_PROGRAM = "1.2.2";
    /** 点击首页＂回看＂内容 */
    public static final String CUR_URL_CLICK_SUPER_LIST_HUIKAN_PROGRAM = "1.2.3";
    /** 点击三路流节目 */
    public static final String CUR_URL_CLICK_SUPER_LIST_STREAM_PROGRAM = "1.2.4";
    /** 点击各分类导航 */
    public static final String CUR_URL_CLICK_SUPER_LIST_SECOND_CATEGORY_PAGE_ENTRY = "1.3";
    /** 点击各分类＂查看全部＂ */
    public static final String CUR_URL_SUPER_LIST_SEE_ALL = "1.4";
    /** 点击首页＂简约列表＂ */
    public static final String CUR_URL_CLICK_SUPER_LIST_SIMPLE_LIST_ENTRY = "1.5";
    /** 退出九宫格首页 */
    public static final String CUR_URL_CLOSE_SUPER_LIST = "1.6";
    // - - - - - - - - - - - - - - - - - - - 超级列表二级页 - - - - - - - - - - - - - - - - - - - - - - - -
    /** 打开二级页面 */
    public static final String CUR_URL_OPEN_SECOND_CATEGORY_PAGE = "2.1";
    /** 点击二级页直播中／轮播内容 */
    public static final String CUR_URL_CLICK_SECOND_CATEGORY_PAGE_LIVE_LUNBO_PROGRAM = "2.2.1";
    /** 点击二级页尚未开始内容 */
    public static final String CUR_URL_CLICK_SECOND_CATEGORY_PAGE_LIVE_NOT_BEGIN_PROGRAM = "2.2.2";
    /** 点击二级页＂回看＂内容 */
    public static final String CUR_URL_CLICK_SECOND_CATEGORY_PAGE_HUIKAN_PROGRAM = "2.2.3";
    /** 退出二级页 */
    public static final String CUR_URL_CLOSE_SECOND_CATEGORY_PAGE = "2.3";
    // - - - - - - - - - - - - - - - - - - - 简约列表页面 - - - - - - - - - - - - - - - - - - - - - - - -
    /** 打开简约列表页 */
    public static final String CUR_URL_OPEN_SIMPLE_LIST = "3.1";
    /** 点击简约列表页直播中／轮播内容 */
    public static final String CUR_URL_CLICK_SIMPLE_LIST_LIVE_LUNBO_PROGRAM = "3.2.1";
    /** 点击简约列表页未开始内容 */
    public static final String CUR_URL_CLICK_SIMPLE_LIST_LIVE_NOT_BEGIN_PROGRAM = "3.2.2";
    /** 点击简约列表页＂回看＂内容 */
    public static final String CUR_URL_CLICK_SIMPLE_LIST_HUIKAN_PROGRAM = "3.2.3";
    /** 点击进入超级列表 */
    public static final String CUR_URL_CLICK_SIMPLE_LIST_SUPER_LIST_ENTRY = "3.3";
    // - - - - - - - - - - - - - - - - - - - 轮播台列表页 - - - - - - - - - - - - - - - - - - - - - - - -
    /** 打开轮播台列表页 */
    public static final String CUR_URL_OPEN_PROGRAM_LIST = "4.1";
    /** 点击列表页中回看内容 */
    public static final String CUR_URL_CLICK_PROGRAM_LIST_HUIKAN_PROGRAM = "4.2";
    // - - - - - - - - - - - - - 多视角／轮播列表页 - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    /** 打开多视角／直播列表页 */
    public static final String CUR_URL_OPEN_MULTI_ANGLE_LIVE_LIST = "5.1";
    /** 点击主视角 */
    public static final String CUR_URL_CLICK_MAIN_ANGLE = "5.2.1";
    /** 点击切换副视角 */
    public static final String CUR_URL_CLICK_TO_SWITCH_TO_SECOND_ANGLE = "5.2.2";
    /** 点击关闭副视角 */
    public static final String CUR_URL_CLICK_TO_CLOSE_SECOND_ANGLE = "5.2.3";
    /** 点击其它直播列表 */
    public static final String CUR_URL_CLICK_OTHER_LIVE_LIST = "5.3";
    // - - - - - - - - - - - - - - - 屏显 - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    /** 点击打开屏显页面 */
    public static final String CUR_URL_OPEN_PINGXIAN = "6.1";
    // - - - - - - - - - - - 推送 - - - - - - - - - - - - - - - - - - - - -
    /** 接收到推送消息 */
    public static final String CUR_URL_BOOLEAN_PUSH_MESSAGE_RECEIVE_SUCCESS = "7.1";
    // - - - - - - - - - - 设置菜单页 - - - - - - - - - - - - - - - - - - - - - - -
    /** 收藏点击量 */
    public static final String CUR_URL_BOOLEAN_BOOKMARK_ADDED_SUCCESS = "8.1";
    /** danmu switch */
    public static final String CUR_URL_BOOLEAN_DANMU_SWITCH = "8.2";
    // - - - - - - - 播放器页面－－－－－－－－－－－－－－－
    public static final String CUR_URL_CHANGE_CHANNEL = "9.1";

    // - - - - - interactive card－－－－－－－－－－－－－
    /** anytime buy card shows */
    public static final String CUR_URL_OPEN_ANYTIME_BUY_CARD = "11.1";
    /** anytime buy card is clicked */
    public static final String CUR_URL_CLICK_ANYTIME_BUY_CARD = "11.2";
    /** anytime buy card detail page shows */
    public static final String CUR_URL_OPEN_ANYTIME_BUY_CARD_DETAIL_PAGE = "11.3";

    // 下面是ty的取值
    /** 点播 */
    public static final String TY_DIANBO = "0";
    /** 直播 */
    public static final String TY_ZHIBO = "1";
    /** 轮播 */
    public static final String TY_LUNBO = "2";

    // 播放上报中ac的取值
    public static final String AC_INIT = "init";
    public static final String AC_PLAY = "play";
    public static final String AC_BLOCK = "block";
    public static final String AC_EBLOCK = "eblock";
    public static final String AC_TIME = "time";
    public static final String AC_END = "end";

    // 页面id的值
    /** 超级列表首页 */
    public static final String PAGE_ID_SUPER_LIST = "1";
    /** 第一路小屏 */
    public static final String PAGE_ID_SMALL_SCREEN_FIRST = "1.7.1";
    /** 第二路小屏 */
    public static final String PAGE_ID_SMALL_SCREEN_SECOND = "1.7.2";
    /** 第三路小屏 */
    public static final String PAGE_ID_SMALL_SCREEN_THIRD = "1.7.3";
    /** 预览用小屏 */
    public static final String PAGE_ID_SMALL_SCREEN_PREVIEW = "1.8";
    /** 超级列表二级页 */
    public static final String PAGE_ID_SUPER_LIST_SECOND_CATEGORY = "2";
    /** 简约列表页 */
    public static final String PAGE_ID_SIMPLE_LIST = "3";
    /** 轮播台列表页 */
    public static final String PAGE_ID_PROGRAM_LIST = "4";
    /** 多视角小屏 */
    public static final String PAGE_ID_BIG_SCREEN_SECONDARY = "5";
    /** 屏显页面 */
    public static final String PAGE_ID_PINGXIAN = "6";
    /** 菜单页 */
    public static final String PAGE_ID_SETTING_MENU = "8";
    /** 大屏 */
    public static final String PAGE_ID_BIG_SCREEN_MAIN = "9";

    /** 添加收藏 */
    public static final String ADD_COLLECTION = "0";
    /** 取消收藏 */
    public static final String DEL_COLLECTION = "1";
    /** danmu ON */
    public static final String DANMU_ON = "0";
    /** danmu OFF */
    public static final String DANMU_OFF = "1";
    /** push接收成功 */
    public static final String PUSH_RECEIVE_SUCCESS = "0";
    /** push接收失败 */
    public static final String PUSH_RECEIVE_FAIL = "1";
    /** 登陆成功 */
    public static final String LOGIN_SUCCESS = "0";
    /** 登陆失败 */
    public static final String LOGIN_FAIL = "1";
    /** 播放器错误类型 */
    public static final String PLAY_ERROR = "pl";
}
