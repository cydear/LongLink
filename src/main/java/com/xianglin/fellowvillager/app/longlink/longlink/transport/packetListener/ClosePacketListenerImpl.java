package com.xianglin.fellowvillager.app.longlink.longlink.transport.packetListener;

import com.xianglin.fellowvillager.app.longlink.longlink.service.ConnManager;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.Packet;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketConstants;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketFilter;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketIDFilter;
import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;
import com.xianglin.mobile.common.logging.LogCatLog;


/**
 * 关闭连接
 */
public class ClosePacketListenerImpl implements PacketListener {
	private final ConnManager mConnManager;

	private static final String TAG = ConfigUtils.TAG;
	public ClosePacketListenerImpl(ConnManager connManager) {
		this.mConnManager = connManager;
	}

	@Override
	public void processPacket(Packet packet) {

		// 关闭连接包
		PacketFilter packetFilter = new PacketIDFilter(
				PacketConstants.MSG_PUSH_CLOSE);//1 ID =1

		if (packetFilter.accept(packet)) {
			LogCatLog.d(TAG,"这是关闭连接包的监听[ClosePacketListenerImpl]"+packet.toString());
			mConnManager.disconnect();
			
		}
		
	}

}
