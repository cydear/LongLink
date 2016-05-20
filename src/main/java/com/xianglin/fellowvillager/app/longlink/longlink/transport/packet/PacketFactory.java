package com.xianglin.fellowvillager.app.longlink.longlink.transport.packet;
/**
 * 协议版本号为3的数据包
 * @author alex
 *
 */
public class PacketFactory {

	public static Packet getPacket(int protoVer) throws Exception {

		Packet packet;

		// 判断逻辑，返回具体的协议包
		if (PacketConstants.PACKET_VERSION_2 == protoVer) {
			packet = new PacketHdrVer2();
		} else {
			packet = null;
			throw new Exception("Don't support this protovern:" + protoVer);
		}

		return packet;
	}

}