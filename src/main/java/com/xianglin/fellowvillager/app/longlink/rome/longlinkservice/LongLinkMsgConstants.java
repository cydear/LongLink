package com.xianglin.fellowvillager.app.longlink.rome.longlinkservice;


public class LongLinkMsgConstants {

    /**
     * 业务内长连接 广播:支持双向数据收发
     */
    public static final String LONGLINK_ACTION_SYNC_TO = "com.xianglin.longlink.SYNCTO";
    public static final String LONGLINK_ACTION_CMD_UPLINK = "com.xianglin.longlink.UPLINK";
    public static final String LONGLINK_ACTION_CMD_TRANSFER = "com.xianglin.longlink.TRANSFER_";

    public static final String LONGLINK_APPID = "appId";
    public static final String LONGLINK_APPDATA = "payload";


    public static final String MSG_PACKET_CHANNEL = "channel";
    public static final String MSG_PACKET_CHANNEL_SYNC = "sync";
    public static final String MSG_PACKET_CHANNEL_PUSH = "push";

    public static final String MSG_PACKET_TYPE = "bizType";
    public static final String MSG_PACKET_TYPE_CHAT = "chat";
    public static final String MSG_PACKET_TYPE_NOTICE = "notice";
    public static final String MSG_PACKET_TYPE_DEFAULT = "default";

}

