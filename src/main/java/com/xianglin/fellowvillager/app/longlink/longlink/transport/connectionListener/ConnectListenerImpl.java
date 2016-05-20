package com.xianglin.fellowvillager.app.longlink.longlink.transport.connectionListener;

import com.xianglin.fellowvillager.app.longlink.longlink.service.ConnManager;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.connection.PushConnection;
import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;
import com.xianglin.fellowvillager.app.longlink.longlink.util.LogUtil;


public class ConnectListenerImpl implements ConnectListener {
	private static final String LOGTAG = ConfigUtils.TAG;

	ConnManager connManager;

	public ConnectListenerImpl(ConnManager pushManager) {
		this.connManager = pushManager;
	}

	@Override
	public void onSuccess(PushConnection xmppConnection) {
		// Make note of the fact that we're now connected.

		connManager.getConnection().setConnected(true);
		LogUtil.LogOut(5, LOGTAG, "getConnection="+ connManager.getConnection().hashCode());
		LogUtil.LogOut(3, LOGTAG, "===== Connected onSuccess()=====");
		// 建立连接后注册连接监听器,当socket失败时重连
		connManager.getConnection().addConnectionListener(connManager.getConnectionListener());
		connManager.submitRegisterTask();// 启动成功 发送注册任务

	}

	/**
	 * socket通道连接失败的操作
	 */
	@Override
	public void onFail() {
		LogUtil.LogOut(2, LOGTAG, "===== Connected onFail()=====");
		// 强策略下，立即发起建链任务
		this.connManager.startReconnectionThread();
	}
}
