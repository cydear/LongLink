package com.xianglin.fellowvillager.app.longlink.longlink.sync;

public class LinkSyncConstants {
	
	public static final String LINK_CHANNEL_SYNC = "sync";
	public static final String LINK_CHANNEL_PUSH = "push";
	
	public static final String MSG_SYNC_DATA = "syncData";
	public static final String MSG_ALL_DATA = "allData";
	
	// sync key-words
	public static final String LINK_SYNC_KEY = "sKey";
	
	public static final String LINK_SYNC_OPCODE = "sOpCode";
	
	public static final String LINK_SYNC_DATA = "sData";
	
	public static final String LINK_SYNC_DATA_BIZ = "biz";
	public static final String LINK_SYNC_DATA_BIZ_DEFAULT = "default";
	public static final String LINK_SYNC_DATA_BIZ_CHAT = "chat";
	public static final String LINK_SYNC_DATA_BIZ_NOTICE = "notice";
	
	public static final String LINK_SYNC_DATA_MD = "md";
	
	
	public static final int LINK_SYNC_OPCODE_SYNC_REQ = 1001;
	public static final int LINK_SYNC_OPCODE_SYNC_END = 1002;
	public static final int LINK_SYNC_OPCODE_SEND_RQE = 2001;
	public static final int LINK_SYNC_OPCODE_SEND_ACK = 2002;

}
