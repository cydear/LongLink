package com.xianglin.fellowvillager.app.longlink.longlink.service;

import com.xianglin.fellowvillager.app.longlink.longlink.util.Constants;



public class LongLinkAddr {
	
	private String servHost;
	private int servPort;
	private String sslUsed;
	
	public LongLinkAddr() {
		// 加载默认的配置数据
		servHost = Constants.NOTIFICATION_DEAFULT_HOST;
		servPort = Constants.NOTIFICATION_DEAFULT_PORT;
		sslUsed = Constants.NOTIFICATION_DEAFULT_SSL;
	}
	/**
	 * add by alex for try
	 * @return
	 */
	public static LongLinkAddr getMe() {
		LongLinkAddr xl = new LongLinkAddr("172.16.12.51",80,"0");
		return xl;
	}
	
	public LongLinkAddr(String host, int port, String sslFlag) {
		// 加载默认的配置数据
		servHost = host;
		servPort = port;
		sslUsed = sslFlag;
	}
	
	public String getHost() {
		return servHost;
	}
	
	public int getPort() {
		return servPort;
	}
	
	public String getSSLFlag() {
		return sslUsed;
	}

}
