package com.xianglin.fellowvillager.app.longlink.longlink.transport.packetListener;

import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.Packet;
import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;
import com.xianglin.mobile.common.logging.LogCatLog;


/**
 * 不支持的包类型,供分析包数据使用
 */
public class UnSupportedPacketListenerImpl implements PacketListener {

	private static final String TAG = ConfigUtils.TAG;
	@Override
	public void processPacket(Packet packet) {
		// 不支持的包类型,供分析包数据使用
		LogCatLog.d(TAG,"不支持的包类型,供分析包数据使用");
		return ;
	}
}
