package com.xianglin.fellowvillager.app.longlink.longlink.transport.connectionListener;

import com.xianglin.fellowvillager.app.longlink.longlink.transport.connection.Connection;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.connection.PushException;


/**
 * Interface that allows for implementing classes to listen for connection
 * closing and reconnection events. Listeners are registered with Connection
 * objects.
 * 
 * @see Connection#addConnectionListener
 * @see Connection#removeConnectionListener
 */
public interface ConnectionListener {

	/**
	 * Notification that the connection was closed normally or that the
	 * reconnection process has been aborted.
	 */
	public void connectionClosed();

	/**
	 * Notification that the connection was closed due to an exception. When
	 * abruptly disconnected it is possible for the connection to try
	 * reconnecting to the server.
	 * 
	 * @param e
	 *            the exception.
	 */
	public void connectionClosedOnError(PushException e);

}