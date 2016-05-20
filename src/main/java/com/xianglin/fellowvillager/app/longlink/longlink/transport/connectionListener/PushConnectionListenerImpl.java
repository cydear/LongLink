package com.xianglin.fellowvillager.app.longlink.longlink.transport.connectionListener;

import com.xianglin.fellowvillager.app.longlink.longlink.service.ConnManager;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.connection.PushException;
import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;
import com.xianglin.fellowvillager.app.longlink.longlink.util.LogUtil;



/**
 * A listener class for monitoring connection closing and reconnection events.
 */
public class PushConnectionListenerImpl implements ConnectionListener {

	private static final String LOGTAG = ConfigUtils.TAG;

	private final ConnManager pushManager;

	public PushConnectionListenerImpl(ConnManager pushManager) {
		this.pushManager = pushManager;
	}

	@Override
	public void connectionClosed() {
		LogUtil.LogOut(3, LOGTAG, "connectionClosed()...");
	}

	/**
	 * 出错重连
	 */
	@Override
	public void connectionClosedOnError(PushException e) {

		String errorType = e.getType();
		LogUtil.LogOut(2, LOGTAG, "connectionClosedOnError()... errorType="+errorType
				+ ", errorInfo: "+e.getMessage());

		if (pushManager.getConnection() != null
				&& pushManager.getConnection().isConnected()) {
			pushManager.setRegistered(false);
			
			// 关闭失败的连接
			pushManager.getConnection().disconnect();
		}

		// 强策略下，立即发起建链任务
		LogUtil.LogOut(
				3,
				LOGTAG,
				"connectionClosedOnError() and then to startReconnectionThread...");

		this.pushManager.startReconnectionThread();

	}

}
