package com.xianglin.fellowvillager.app.longlink.longlink.util;


/**
 * Static constants for this package.
 */
public class Constants {

	public static final String SERV_HOST = "SERV_HOST";
	public static final String SERV_PORT = "SERV_PORT";
	public static final String PROTOCOL_VERSION = "PROTOCOL_VERSION";

	public static final String ACTION_PUSH_CONNECT = "com.xianglin.pushsdk.push.CONNECT";
	public static final String ACTION_KEEPLIVE_TIMER = "com.xianglin.pushsdk.push.KEEPLIVE";

	// INTENT ACTIONS
	public static final String ACTION_SHOW_NOTIFICATION = "com.xianglin.pushsdk.SHOW_NOTIFICATION";
	public final static String PUSH_NOTIFIRE = "notifier_parcelable";
	public static final String PUSH_PREFERENCE_NAME = "linkinfo_preferences";

	// default address
	public static final String NOTIFICATION_DEAFULT_HOST = "ll.xianglin.cn";//"mobilepmgw.xianglin.com"; alex
	public static final int NOTIFICATION_DEAFULT_PORT = 443;
	public static final String NOTIFICATION_DEAFULT_SSL = "1";
	public static final int NOTIFICATION_DEAFULT_PROTOCAL_VER = 3;


	// ------------------------------------------------------------------------
	public static final String LINK_VERSION_KEY = "linkVersion";
	public static final String LINK_VERSION_VALUE = "2.0.0";
	
	public static final String LINK_VERSION_VALUE_GZIP = "1.1.0";  // 此版本支持数据GZIP压缩

	// used to initial message (client to server)
	public static final String OSTYPE = "osType";
	public static final String OSVERSION = "osVersion";
	public static final String PRODUCTVERSION = "productVersion";

	// the identification of the long-link connection 
	public static final String CONNECT_TOKEN_USER = "linkToken";
	public static final String CONNECT_TOKEN_SESSION = "linkExtInfo";
	public static final String CONNECT_TOKEN_LOGINTIME = "loginServerTime";
	public static final String CONNECT_TYPE_NETWORK = "network";

	public static final String CONNECT_OS_NAME = "android";
	public static final String CONNECT_ACTION = "action"; 
	public static final String CONNECT_ACTION_ACTIVE = "active";

	/*****1.5.8版本修改 新增 TODO 2016-2-29 james*****/
	/**
	 * app 类型
	 */
	public static final String CONNECT_TYPE_OS = "appType";
	/**
	 * 连接客户id
	 */
	public static final String CONNECT_TOKEN_USERID = "userId";
	/**
	 * 连接客户设备id
	 */
	public static final String CONNECT_TOKEN_DEVICESID = "deviceId";



	// 初始化请求的响应
	public static final String CONNECT_KEEPLIVE_TIME = "keepLiveTime";
	public static final String CONNECT_HEART_TIMEOUT = "heartTimeOut";
	/**
	 * 修改更新1.5.8
	 * TODO 2016-2-29 james
	 */
	public static final String CONNECT_ENCRYPTKEY = "encryptKey";

	// used to push content message (server to client)
	public static final String MSG_KEY = "msgKey";
	public static final String MSG_DATA = "msgData";
	public static final String MSG_TIMESTAMP = "msgCreateTime";
	public static final String MSG_APPID = "appId";
	public static final String MSG_PAYLOAD = "payload";
}
