package com.xianglin.fellowvillager.app.longlink.longlink.transport.connection;

/**
 * Represents the configuration of Smack. The configuration is used for:
 * <ul>
 * <li>Initializing classes by loading them at start-up.
 * <li>Getting the current Smack version.
 * <li>Getting and setting global library behavior, such as the period of time
 * to wait for replies to packets from the server. Note: setting these values
 * via the API will override settings in the configuration file.
 * </ul>
 * 
 * Configuration settings are stored in META-INF/smack-config.xml (typically
 * inside the smack.jar file).
 */
public final class PushCtrlConfiguration {

	private static final String LONGLINK_VERSION = "1.0.0";
	private static final int LONGLINK_RESP_TIMEOUT = 10; // 单位：秒

	// 默认的时间属性
	private static int mPacketReplyTimeout = 5 * 1000; // 单位：毫秒milliseconds
	
	private static int mKeepAliveConfiguredInterval = 30; // 单位：秒
	
	private static int mReconnectInterval = 2; // 单位：秒

	private PushCtrlConfiguration() {
		//
	}

	/**
	 * Loads the configuration from the smack-config.xml file.
	 * <p>
	 * 
	 * So far this means that: 1) a set of classes will be loaded in order to
	 * execute their static init block 2) retrieve and set the current Smack
	 * release
	 */
	static {
		try {
			// Get an array of class loaders to try loading the providers files
			// from.
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the Smack version information, eg "1.3.0".
	 * 
	 * @return the Smack version information.
	 */
	public static String getVersion() {
		return LONGLINK_VERSION;
	}

	/**
	 * Returns the number of milliseconds to wait for a response from the
	 * server. The default value is 5000 ms.
	 * 
	 * @return the milliseconds to wait for a response from the server
	 */
	public static int getPacketReplyTimeout() {
		// The timeout value must be greater than 0 otherwise we will answer the
		// default value
		if (mPacketReplyTimeout <= 0) {
			mPacketReplyTimeout = LONGLINK_RESP_TIMEOUT * 1000;
		}
		return mPacketReplyTimeout;
	}

	/**
	 * Sets the number of milliseconds to wait for a response from the server.
	 * 
	 * @param timeout
	 *            the milliseconds to wait for a response from the server
	 */
	public static void setPacketReplyTimeout(int timeout) {
		if (timeout <= 0) {
			throw new IllegalArgumentException();
		}
		
		mPacketReplyTimeout = timeout * 1000;
	}

	public static void setReconnectInterval(int reconInterval) {
		// 转化到秒
		mReconnectInterval = reconInterval;
	}

	public static int getReconnectInterval() {
		return mReconnectInterval;
	}

	/**
	 * Returns the number of milleseconds delay between sending keep-alive
	 * requests to the server. The default value is 30000 ms. A value of -1 mean
	 * no keep-alive requests will be sent to the server.
	 * 
	 * @return the milliseconds to wait between keep-alive requests, or -1 if no
	 *         keep-alive should be sent.
	 */
	public static int getKeepAliveInterval() {
		
		return mKeepAliveConfiguredInterval;
	}

	/**
	 * Sets the number of milleseconds delay between sending keep-alive requests
	 * to the server. The default value is 30000 ms. A value of -1 mean no
	 * keep-alive requests will be sent to the server.
	 * 
	 * @param interval
	 *            the milliseconds to wait between keep-alive requests, or -1 if
	 *            no keep-alive should be sent.
	 */
	public static void setKeepAliveInterval(int interval) {
		mKeepAliveConfiguredInterval = interval;
	}

}
