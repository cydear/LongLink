package com.xianglin.fellowvillager.app.longlink.longlink.transport.packetListener;

import com.xianglin.fellowvillager.app.longlink.longlink.service.ConnManager;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.Packet;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketConstants;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketFactory;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketFilter;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketIDFilter;
import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;
import com.xianglin.fellowvillager.app.longlink.longlink.util.LogUtil;
import com.xianglin.mobile.common.logging.LogCatLog;


/**
 * 心跳监听
 */
public class HeartBeatPacketListenerImpl implements PacketListener {
	private static final String LOGTAG = ConfigUtils.TAG;

	private final ConnManager mConnManager;

	public HeartBeatPacketListenerImpl(ConnManager pushManager) {
		this.mConnManager = pushManager;
	}

	@Override
	public void processPacket(Packet packet) {

		// 此处仅处理心跳packet，故用filter过滤
		PacketFilter packetFilter = new PacketIDFilter(
				PacketConstants.MSG_PUSH_KEEPLIVE); // 3 ID = 3

		if (packetFilter.accept(packet)) {
			LogCatLog.d(LOGTAG, "这是心跳包的监听[HeartBeatPacketListenerImpl]" + packet.toString());
			// 收到心跳响应
			LogUtil.LogOut(3, LOGTAG,
					"processPacket() got one HeartBeatPacket from Server!");

			if (packet.getMsgType() == PacketConstants.MSG_PUSH_TYPE_REQUEST) {
				// 心跳请求包
				// 需要给服务器发送响应
				Packet heartBeat;
				try {
					heartBeat = PacketFactory.getPacket(mConnManager
							.getProtoVer());
					heartBeat.setMsgId(PacketConstants.MSG_PUSH_KEEPLIVE);
					heartBeat
							.setMsgType(PacketConstants.MSG_PUSH_TYPE_RESPONSE);
					heartBeat.setData("");
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}

				mConnManager.getConnection().sendPacket(heartBeat);
			}
			
			if (mConnManager.isForceStopped()) {
				mConnManager.disconnect();
			} else {
				// 启动下次心跳包时钟
				mConnManager.startHeartTimer();//用sync心跳 代替 纯心跳 后，再次发起 sync心跳要移动 LinkSyncPacketListenerImpl by alex
			}
		}
	}
}
