package com.xianglin.fellowvillager.app.longlink.longlink.transport.packet;

public class PacketConstants {

	/**
	 * used to initialize push link after create connection.
	 */
	public static final int MSG_PUSH_INITIALIZE = 0;

	/**
	 * used to close push link so that notice the linked server side.
	 */
	public static final int MSG_PUSH_CLOSE = 1;

	/**
	 * used to let client reconnect with server side -- not used now.
	 */
	public static final int MSG_PUSH_RECONNECT = 2;

	/**
	 * used to keep push link active with server side.
	 */
	public static final int MSG_PUSH_KEEPLIVE = 3;

	/**
	 * used with application data in it.
	 */
	public static final int MSG_PUSH_MSGDATA = 4;

	/**
	 * used to report the current location for coupon push.
	 */
	public static final int MSG_PUSH_LOCATE = 5;

	/**
	 * used to get one key from server for encryption.
	 */
	public static final int MSG_PUSH_CIPHERKEY = 6;

	/**
	 * used to send business data.
	 */
	public static final int MSG_PUSH_BIZDATA = 7;
	
	/**
	 * used to sync data from server based on synckey.
	 */
	public static final int MSG_PUSH_LINKSYNC = 8;

	// -----------------------------------------------------
	public static final int MSG_PUSH_TYPE_REQUEST = 0;
	public static final int MSG_PUSH_TYPE_RESPONSE = 1;

	// -----------------------------------------------------
	public static int PACKET_VERSION_2 = 3;
	public static int PACKET_HEADER_LEN_2 = 15;

	public static int PACKET_BASE_HEADER_LEN = 2;
	
	// 是否启用GZip压缩
	public static final int MSG_GZIP_ON = 1;
	public static final int MSG_GZIP_OFF = 0;

}