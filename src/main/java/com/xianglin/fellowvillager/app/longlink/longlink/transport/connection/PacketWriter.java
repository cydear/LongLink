package com.xianglin.fellowvillager.app.longlink.longlink.transport.connection;

import com.xianglin.fellowvillager.app.longlink.longlink.message.LongLinkMessageFilter;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.Packet;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketConstants;
import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;
import com.xianglin.fellowvillager.app.longlink.longlink.util.LogUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;



/**
 * Writes packets to a XMPP server. Packets are sent using a dedicated thread.
 * Packet interceptors can be registered to dynamically modify packets before
 * they're actually sent. Packet listeners can be registered to listen for all
 * outgoing packets.
 * 
 * @see Connection#addPacketInterceptor
 * @see Connection#addPacketSendingListener
 */
class PacketWriter {
	private static final String LOGTAG = ConfigUtils.TAG;

	private Thread writerThread;
	private DataOutputStream writer;
	private PushConnection connection;
	private static BlockingQueue<Packet> queue;
	private static boolean done;

	/**
	 * Timestamp when the last stanza was sent to the server. This information
	 * is used by the keep alive process to only send heartbeats when the
	 * connection has been idle.
	 */
	private long lastActive = System.currentTimeMillis();

	/**
	 * Creates a new packet writer with the specified connection.
	 * 
	 * @param connection
	 *            the connection.
	 */
	protected PacketWriter(PushConnection connection) {
		this.queue = new ArrayBlockingQueue<Packet>(500, true);
		this.connection = connection;
		init();
	}

	/**
	 * Initializes the writer in order to be used. It is called at the first
	 * connection and also is invoked if the connection is disconnected by an
	 * error.
	 */
	protected void init() {
		this.writer = connection.writer;
		done = false;

		writerThread = new Thread() {
			public void run() {
				writePackets(this);
			}
		};
		writerThread.setName("Packet Writer ("
				+ connection.connectionCounterValue + ")");
		writerThread.setDaemon(true);
	}

	/**
	 * Sends the specified packet to the server.
	 * 
	 * @param packet
	 *            the packet to send.
	 */
	public void sendPacket(Packet packet) {
		LogUtil.LogOut(4, LOGTAG, "sendPacket() enter... done=" + done);
		if (!done) {
			// Invoke interceptors for the new packet that is about to be sent.
			// Interceptors
			// may modify the content of the packet.
			// connection.firePacketInterceptors(packet);

			try {
				queue.put(packet);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
				return;
			}

			synchronized (queue) {
				LogUtil.LogOut(4, LOGTAG,
						"sendPacket queue len=" + queue.size());
				queue.notifyAll();
			}

			// Process packet writer listeners. Note that we're using the
			// sending
			// thread so it's expected that listeners are fast.
			// connection.firePacketSendingListeners(packet);
		}
	}





	/**
	 * Starts the packet writer thread and opens a connection to the server. The
	 * packet writer will continue writing packets until {@link #shutdown} or an
	 * error occurs.
	 */
	public void startup() {
		writerThread.start();
	}

	void setWriter(DataOutputStream writer) {
		this.writer = writer;
	}

	/**
	 * Shuts down the packet writer. Once this method has been called, no
	 * further packets will be written to the server.
	 */
	public void shutdown() {
		done = true;
		synchronized (queue) {
			queue.notifyAll();
		}
	}

	/**
	 * Cleans up all resources used by the packet writer.
	 */
	void cleanup() {
		connection.sendListeners.clear();
	}

	/**
	 * Returns the next available packet from the queue for writing.
	 * 
	 * @return the next packet for writing.
	 */
	private Packet nextPacket() {
		Packet packet = null;
		// Wait until there's a packet or we're done.
		while (!done && (packet = queue.poll()) == null) {
			try {
				synchronized (queue) {
					queue.wait();
					LogUtil.LogOut(4, LOGTAG,
							"nextPacket queue len=" + queue.size());
				}
			} catch (InterruptedException ie) {
				// Do nothing
			}
		}
		return packet;
	}

	private void writePackets(Thread thisThread) {
		try {
			// Open the stream.
			// openStream();

			// Write out packets from the queue.
			while (!done && (writerThread == thisThread)) {
				Packet packet = nextPacket();
				if (packet != null) {
					synchronized (writer) {
						LongLinkMessageFilter.getInstance().packetFilter(this.connection.connManager,packet,LongLinkMessageFilter.PACKETWRITER);
						writer.write(packet.toByteBuf());
						writer.flush();

						// Keep track of the last time a stanza was sent to the
						// server
						lastActive = System.currentTimeMillis();

						if (packet.getMsgType() == PacketConstants.MSG_PUSH_TYPE_REQUEST
								&& packet.getMsgId() != PacketConstants.MSG_PUSH_LINKSYNC) {
							// 设置
							connection
									.startTimer(lastActive, packet.getMsgId());
						}
					}
				}

				synchronized (queue) {
					LogUtil.LogOut(4, LOGTAG,
							"writePackets queue len=" + queue.size());
				}

			}

			// Delete the queue contents (hopefully nothing is left).
			queue.clear();

			// Close the stream.
			try {
				writer.flush();
			} catch (Exception e) {
				// Do nothing
			} finally {
				try {
					writer.close();
				} catch (Exception e) {
					// Do nothing
				}
			}
		} catch (IOException ioe) {
			if (!done) {
				done = true;

				String errorMessage = "IOException happened when writer to write.";
				Exception err = new Exception("IOException : write");
				PushException pushE = new PushException(errorMessage, err);
				pushE.setType(PushException.PUSH_EXCEPTION_WRITEERROR);

				connection.packetReader.notifyConnectionError(pushE);
			}
		}
	}

}