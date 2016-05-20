package com.xianglin.fellowvillager.app.longlink.longlink.transport.packetListener;

import com.xianglin.fellowvillager.app.longlink.longlink.service.ConnManager;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.Packet;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketConstants;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketFilter;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketIDFilter;
import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;
import com.xianglin.mobile.common.logging.LogCatLog;


public class ReconnectPacketListenerImpl implements PacketListener {
	private final ConnManager mConnManager;

	private static final String TAG = ConfigUtils.TAG;
	public ReconnectPacketListenerImpl(ConnManager connManager) {
		this.mConnManager = connManager;
	}

	@Override
	public void processPacket(Packet packet) {
		// 重连包

		PacketFilter packetFilter = new PacketIDFilter(
				PacketConstants.MSG_PUSH_RECONNECT);// ID  ID = 2

		if (packetFilter.accept(packet)) {
			LogCatLog.d(TAG, "这是重连包的监听[ReconnectPacketListenerImpl]" + packet.toString());
//			String registerData = packet.getData();
//			JSONObject registerJson;
			try {
//				registerJson = new JSONObject(registerData);
//				PushCtrlConfiguration.setReconnectInterval(1);// 重连时间
				// 关闭当前的连接
				mConnManager.disconnect();
				this.mConnManager.startReconnectionThread();
			} catch (Exception e) {
				e.printStackTrace();
			}


		}
	}
}
