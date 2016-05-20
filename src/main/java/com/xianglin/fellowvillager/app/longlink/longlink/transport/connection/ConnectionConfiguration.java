package com.xianglin.fellowvillager.app.longlink.longlink.transport.connection;

import com.xianglin.fellowvillager.app.longlink.longlink.transport.connection.proxy.ProxyInfo;

import javax.net.SocketFactory;


/**
 * Configuration to use while establishing the connection to the server. 
 * 
 * It is also possible to configure if TLS, SASL, and compression are used or
 * not.
 */
public class ConnectionConfiguration implements Cloneable {

	private String host;
	private int port;
	
	// Holds the proxy information (such as proxyhost, proxyport, username,
	// password etc)
	protected ProxyInfo proxy;

	private boolean compressionEnabled = false;
	private SecurityMode securityMode = SecurityMode.enabled;

	// Holds the socket factory that is used to generate the socket in the
	// connection
	private SocketFactory socketFactory;

	/**
	 * Creates a new ConnectionConfiguration for a connection that will connect
	 * to the desired host and port with desired proxy.
	 * 
	 * @param host
	 *            the host where the XMPP server is running.
	 * @param port
	 *            the port where the XMPP is listening.
	 * @param proxy
	 *            the proxy through which XMPP is to be connected
	 */
	public ConnectionConfiguration(String host, int port, ProxyInfo proxy) {
		init(host, port, proxy);
	}

	private void init(String host, int port, ProxyInfo proxy) {
		this.host = host;
		this.port = port;
		this.proxy = proxy;

		// Setting the SocketFactory according to proxy supplied
		socketFactory = proxy.getSocketFactory();
	}

	/**
	 * Returns the host to use when establishing the connection. The host and
	 * port to use might have been resolved by a DNS lookup as specified by the
	 * XMPP spec (and therefore may not match the {@link #getServiceName service
	 * name}.
	 * 
	 * @return the host to use when establishing the connection.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Returns the port to use when establishing the connection. The host and
	 * port to use might have been resolved by a DNS lookup as specified by the
	 * XMPP spec.
	 * 
	 * @return the port to use when establishing the connection.
	 */
	public int getPort() {
		return port;
	}

	public ProxyInfo getProxyInfo() {
		return this.proxy;
	}

	/**
	 * Returns the TLS security mode used when making the connection. By
	 * default, the mode is {@link SecurityMode#enabled}.
	 * 
	 * @return the security mode.
	 */
	public SecurityMode getSecurityMode() {
		return securityMode;
	}

	/**
	 * Sets the TLS security mode used when making the connection. By default,
	 * the mode is {@link SecurityMode#enabled}.
	 * 
	 * @param securityMode
	 *            the security mode.
	 */
	public void setSecurityMode(SecurityMode securityMode) {
		this.securityMode = securityMode;
	}

	/**
	 * Returns true if the connection is going to use stream compression. Stream
	 * compression will be requested after TLS was established (if TLS was
	 * enabled) and only if the server offered stream compression. With stream
	 * compression network traffic can be reduced up to 90%. By default
	 * compression is disabled.
	 * 
	 * @return true if the connection is going to use stream compression.
	 */
	public boolean isCompressionEnabled() {
		return compressionEnabled;
	}

	/**
	 * Sets if the connection is going to use stream compression. Stream
	 * compression will be requested after TLS was established (if TLS was
	 * enabled) and only if the server offered stream compression. With stream
	 * compression network traffic can be reduced up to 90%. By default
	 * compression is disabled.
	 * 
	 * @param compressionEnabled
	 *            if the connection is going to use stream compression.
	 */
	public void setCompressionEnabled(boolean compressionEnabled) {
		this.compressionEnabled = compressionEnabled;
	}

	/**
	 * Sets the socket factory used to create new xmppConnection sockets. This
	 * is useful when connecting through SOCKS5 proxies.
	 * 
	 * @param socketFactory
	 *            used to create new sockets.
	 */
	public void setSocketFactory(SocketFactory socketFactory) {
		this.socketFactory = socketFactory;
	}

	/**
	 * Returns the socket factory used to create new xmppConnection sockets.
	 * This is useful when connecting through SOCKS5 proxies.
	 * 
	 * @return socketFactory used to create new sockets.
	 */
	public SocketFactory getSocketFactory() {
		return this.socketFactory;
	}

	/**
	 * An enumeration for TLS security modes that are available when making a
	 * connection to the XMPP server.
	 */
	public static enum SecurityMode {

		/**
		 * Securirty via TLS encryption is required in order to connect. If the
		 * server does not offer TLS or if the TLS negotiaton fails, the
		 * connection to the server will fail.
		 */
		required,

		/**
		 * Security via TLS encryption is used whenever it's available. This is
		 * the default setting.
		 */
		enabled,

		/**
		 * Security via TLS encryption is disabled and only un-encrypted
		 * connections will be used. If only TLS encryption is available from
		 * the server, the connection will fail.
		 */
		disabled
	}

}
