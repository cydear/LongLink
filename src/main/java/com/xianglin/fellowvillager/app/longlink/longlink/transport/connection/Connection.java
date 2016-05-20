package com.xianglin.fellowvillager.app.longlink.longlink.transport.connection;

import com.xianglin.fellowvillager.app.longlink.longlink.service.ConnManager;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.connectionListener.ConnectListener;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.connectionListener.ConnectionListener;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.Packet;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketFilter;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packetListener.PacketListener;
import com.xianglin.fellowvillager.app.longlink.longlink.util.LogUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;


public abstract class Connection {

	/**
	 * Counter to uniquely identify connections that are created.
	 */
	private final static AtomicInteger connectionCounter = new AtomicInteger(0);

	public static boolean DEBUG_ENABLED = false;

	static {
		// Use try block since we may not have permission to get a system
		// property (for example, when an applet).
		try {
			DEBUG_ENABLED = Boolean.getBoolean("smack.debugEnabled");
		} catch (Exception e) {
			// Ignore.
		}
		// Ensure the SmackConfiguration class is loaded by calling a method in
		// it.
		PushCtrlConfiguration.getVersion();
	}

	/**
	 * A collection of ConnectionListeners which listen for connection closing
	 * and reconnection events.
	 */
	protected final Collection<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<ConnectionListener>();

	/**
	 * List of PacketListeners that will be notified when a new packet was
	 * received.
	 */
	protected final Map<PacketListener, ListenerWrapper> recvListeners = new ConcurrentHashMap<PacketListener, ListenerWrapper>();

	/**
	 * PacketListener that will be notified when a new packet was received.
	 */
	protected PacketListener recvListener;

	public void registerReceiverListener(PacketListener recvListener) {
		if (recvListener == null) {
			throw new NullPointerException("Packet listener is null.");
		}

		this.recvListener = recvListener;
	}

	/**
	 * List of PacketListeners that will be notified when a new packet was sent.
	 */
	protected final Map<PacketListener, ListenerWrapper> sendListeners = new ConcurrentHashMap<PacketListener, ListenerWrapper>();

	/**
	 * The Reader which is used for the {@see debugger}.
	 */
	protected DataInputStream reader;

	/**
	 * The Writer which is used for the {@see debugger}.
	 */
	protected DataOutputStream writer;

	/**
	 * A number to uniquely identify connections that are created. This is
	 * distinct from the connection ID, which is a value sent by the server once
	 * a connection is made.
	 */
	protected final int connectionCounterValue = connectionCounter
			.getAndIncrement();

	/**
	 * Holds the initial configuration used while creating the connection.
	 */
	protected final ConnectionConfiguration config;

	/**
	 * Create a new Connection to a XMPP server.
	 * 
	 * @param configuration
	 *            The configuration which is used to establish the connection.
	 */
	protected Connection(ConnectionConfiguration configuration) {
		config = configuration;
	}

	/**
	 * Returns the configuration used to connect to the server.
	 * 
	 * @return the configuration used to connect to the server.
	 */
	protected ConnectionConfiguration getConfiguration() {
		return config;
	}

	/**
	 * Returns the host name of the server where the XMPP server is running.
	 * This would be the IP address of the server or a name that may be resolved
	 * by a DNS server.
	 * 
	 * @return the host name of the server where the XMPP server is running.
	 */
	public String getHost() {
		return config.getHost();
	}

	/**
	 * Returns the port number of the XMPP server for this connection. The
	 * default port for normal connections is 5222. The default port for SSL
	 * connections is 5223.
	 * 
	 * @return the port number of the XMPP server.
	 */
	public int getPort() {
		return config.getPort();
	}

	/**
	 * Returns the full XMPP address of the user that is logged in to the
	 * connection or <tt>null</tt> if not logged in yet. An XMPP address is in
	 * the form username@server/resource.
	 * 
	 * @return the full XMPP address of the user logged in.
	 */
	public abstract String getUser();

	/**
	 * Returns the connection ID for this connection, which is the value set by
	 * the server when opening a XMPP stream. If the server does not set a
	 * connection ID, this value will be null. This value will be <tt>null</tt>
	 * if not connected to the server.
	 * 
	 * @return the ID of this connection returned from the XMPP server or
	 *         <tt>null</tt> if not connected to the server.
	 */
	public abstract String getConnectionID();

	/**
	 * Returns true if currently connected to the XMPP server.
	 * 
	 * @return true if connected.
	 */
	public abstract boolean isConnected();

	/**
	 * Establishes a connection to the XMPP server and performs an automatic
	 * login only if the previous connection state was logged (authenticated).
	 * It basically creates and maintains a connection to the server.
	 * <p>
	 * <p/>
	 * Listeners will be preserved from a previous connection if the
	 * reconnection occurs after an abrupt termination.
	 * 
	 * @throws PushException
	 *             if an error occurs while trying to establish the connection.
	 */
	public abstract void connect(ConnectListener taskListener,ConnManager connManager)
			throws PushException;

	/**
	 * Sends the specified packet to the server.
	 * 
	 * @param packet
	 *            the packet to send.
	 */
	public abstract void sendPacket(Packet packet);

	/**
	 * Closes the connection. This method cleans up all resources used by the
	 * connection.
	 * 
	 * @param unavailablePresence
	 *            the presence packet to send during shutdown.
	 */
	public abstract void disconnect();

	/**
	 * Adds a connection listener to this connection that will be notified when
	 * the connection closes or fails. The connection needs to already be
	 * connected or otherwise an IllegalStateException will be thrown.
	 * 
	 * @param connectionListener
	 *            a connection listener.
	 */
	public void addConnectionListener(ConnectionListener connectionListener) {
		if (!isConnected()) {
			throw new IllegalStateException("Not connected to server.");
		}
		if (connectionListener == null) {
			return;
		}
		if (!connectionListeners.contains(connectionListener)) {
			connectionListeners.add(connectionListener);
		}
	}

	/**
	 * Removes a connection listener from this connection.
	 * 
	 * @param connectionListener
	 *            a connection listener.
	 */
	public void removeConnectionListener(ConnectionListener connectionListener) {
		connectionListeners.remove(connectionListener);
	}

	/**
	 * Get the collection of listeners that are interested in connection events.
	 * 
	 * @return a collection of listeners interested on connection events.
	 */
	protected Collection<ConnectionListener> getConnectionListeners() {
		return connectionListeners;
	}

	/**
	 * Registers a packet listener with this connection. A packet filter
	 * determines which packets will be delivered to the listener. If the same
	 * packet listener is added again with a different filter, only the new
	 * filter will be used.
	 * 
	 * @param packetListener
	 *            the packet listener to notify of new received packets.
	 * @param packetFilter
	 *            the packet filter to use.
	 */
	public void addPacketListener(PacketListener packetListener,
			PacketFilter packetFilter) {
		if (packetListener == null) {
			throw new NullPointerException("Packet listener is null.");
		}
		ListenerWrapper wrapper = new ListenerWrapper(packetListener,
				packetFilter);
		recvListeners.put(packetListener, wrapper);
		LogUtil.LogOut(5, "Connection",
				"packageListener in the list is " + recvListeners.size());
	}

	/**
	 * Removes a packet listener for received packets from this connection.
	 * 
	 * @param packetListener
	 *            the packet listener to remove.
	 */
	public void removePacketListener(PacketListener packetListener) {
		recvListeners.remove(packetListener);
	}

	/**
	 * Get a map of all packet listeners for received packets of this
	 * connection.
	 * 
	 * @return a map of all packet listeners for received packets.
	 */
	protected Map<PacketListener, ListenerWrapper> getPacketListeners() {
		return recvListeners;
	}

	/**
	 * Registers a packet listener with this connection. The listener will be
	 * notified of every packet that this connection sends. A packet filter
	 * determines which packets will be delivered to the listener. Note that the
	 * thread that writes packets will be used to invoke the listeners.
	 * Therefore, each packet listener should complete all operations quickly or
	 * use a different thread for processing.
	 * 
	 * @param packetListener
	 *            the packet listener to notify of sent packets.
	 * @param packetFilter
	 *            the packet filter to use.
	 */
	public void addPacketSendingListener(PacketListener packetListener,
			PacketFilter packetFilter) {
		if (packetListener == null) {
			throw new NullPointerException("Packet listener is null.");
		}
		ListenerWrapper wrapper = new ListenerWrapper(packetListener,
				packetFilter);
		sendListeners.put(packetListener, wrapper);
	}

	/**
	 * Removes a packet listener for sending packets from this connection.
	 * 
	 * @param packetListener
	 *            the packet listener to remove.
	 */
	public void removePacketSendingListener(PacketListener packetListener) {
		sendListeners.remove(packetListener);
	}

	/**
	 * Get a map of all packet listeners for sending packets of this connection.
	 * 
	 * @return a map of all packet listeners for sent packets.
	 */
	protected Map<PacketListener, ListenerWrapper> getPacketSendingListeners() {
		return sendListeners;
	}

	/**
	 * Process all packet listeners for sending packets.
	 * 
	 * @param packet
	 *            the packet to process.
	 */
	protected void firePacketSendingListeners(Packet packet) {
		// Notify the listeners of the new sent packet
		for (ListenerWrapper listenerWrapper : sendListeners.values()) {
			listenerWrapper.notifyListener(packet);
		}
	}

	/**
	 * A wrapper class to associate a packet filter with a listener.
	 */
	protected static class ListenerWrapper {

		private PacketListener packetListener;
		private PacketFilter packetFilter;

		/**
		 * Create a class which associates a packet filter with a listener.
		 * 
		 * @param packetListener
		 *            the packet listener.
		 * @param packetFilter
		 *            the associated filter or null if it listen for all
		 *            packets.
		 */
		public ListenerWrapper(PacketListener packetListener,
				PacketFilter packetFilter) {
			this.packetListener = packetListener;
			this.packetFilter = packetFilter;
		}

		/**
		 * Notify and process the packet listener if the filter matches the
		 * packet.
		 * 
		 * @param packet
		 *            the packet which was sent or received.
		 */
		public void notifyListener(Packet packet) {
			if (packetFilter == null || packetFilter.accept(packet)) {
				packetListener.processPacket(packet);
			}
		}
	}

}
