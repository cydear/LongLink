package com.xianglin.fellowvillager.app.longlink.longlink;


public interface PacketHanlder {
	void processPacket(String bizType, String appId, String appData,long syncKey ,long opCode);

	void processPacket(boolean isRegoster,int state);
}
