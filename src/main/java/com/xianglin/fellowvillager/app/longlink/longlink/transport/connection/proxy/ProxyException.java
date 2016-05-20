package com.xianglin.fellowvillager.app.longlink.longlink.transport.connection.proxy;

import java.io.IOException;

/**
 * An exception class to handle exceptions caused by proxy.
 */
public class ProxyException extends IOException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ProxyException(ProxyInfo.ProxyType type, String ex, Throwable cause) {
		super("Proxy Exception " + type.toString() + " : " + ex + ", " + cause);
	}

	public ProxyException(ProxyInfo.ProxyType type, String ex) {
		super("Proxy Exception " + type.toString() + " : " + ex);
	}

	public ProxyException(ProxyInfo.ProxyType type) {
		super("Proxy Exception " + type.toString() + " : " + "Unknown Error");
	}
}
