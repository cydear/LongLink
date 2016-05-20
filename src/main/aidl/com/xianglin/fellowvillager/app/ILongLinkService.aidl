package com.xianglin.fellowvillager.app;


import com.xianglin.fellowvillager.app.ILongLinkPacketNotifer;


interface ILongLinkService {

	void reConnect();
	void disConnect();
	boolean isConnected();
	
	void setAppUserInfo(String userid, String devicesId ,String extToken, String loginTime);
	void setPacketNotifer(ILongLinkPacketNotifer notifer);
	void sendPacketUplink(String channel, String data); 
	
	void setLinkAddr(String host, int port, String sslFlag); 
	
}