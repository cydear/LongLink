package com.xianglin.fellowvillager.app.longlink.longlink.transport.packetListener;

import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.Packet;

/**
 * Provides a mechanism to listen for packets that pass a specified filter. This
 * allows event-style programming -- every time a new packet is found, the
 * {@link #processPacket(Packet)} method will be called.
 * 
 */
public interface PacketListener {

	/**
	 * Process the next packet sent to this packet listener.
	 * <p>
	 * 
	 * A single thread is responsible for invoking all listeners, so it's very
	 * important that implementations of this method not block for any extended
	 * period of time.
	 * 
	 * @param packet
	 *            the packet to process.
	 */
	public void processPacket(Packet packet);

}
