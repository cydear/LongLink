package com.xianglin.fellowvillager.app.longlink.longlink.transport.packetListener;

import android.os.Bundle;

import com.xianglin.fellowvillager.app.longlink.longlink.service.ConnManager;
import com.xianglin.fellowvillager.app.longlink.longlink.sync.LinkSyncConstants;
import com.xianglin.fellowvillager.app.longlink.longlink.sync.LinkSyncInfo;
import com.xianglin.fellowvillager.app.longlink.longlink.sync.LinkSyncPacket;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.Packet;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketConstants;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketFilter;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketIDFilter;
import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;
import com.xianglin.fellowvillager.app.longlink.longlink.util.Constants;
import com.xianglin.fellowvillager.app.longlink.longlink.util.LogUtil;
import com.xianglin.mobile.common.logging.LogCatLog;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * 基本数据包监听
 *
 */
public class LinkSyncPacketListenerImpl implements PacketListener {
	private static final String LOGTAG = ConfigUtils.TAG;

	private final ConnManager mConnManager;

	public LinkSyncPacketListenerImpl(ConnManager connManager) {
		this.mConnManager = connManager;
	}

	public void processPacket(Packet packet) {
		String jsonString = "";

		// 此处仅处理业务类packet，故用filter过滤
		PacketFilter packetFilter = new PacketIDFilter(
				PacketConstants.MSG_PUSH_LINKSYNC);// 8 ID ＝ 8

		if (packetFilter.accept(packet)) {
			LogCatLog.d(LOGTAG, "这是基本数据的监听[LinkSyncPacketListenerImpl]" + packet.toString());
			// 收到消息
			LogUtil.LogOut(3, LOGTAG, "processPacket()...");

			LinkSyncPacket syncData = null;
			//
//			PacketHdrVer2 packet1  = (PacketHdrVer2) packet;
//			String json = JSON.toJSONString(packet1);
			jsonString = packet.getData();

			if (jsonString != null && jsonString.length() > 0) {
				try {
					// 提取协议数据内容
					JSONObject jsonObject = new JSONObject(jsonString);
					syncData = this.handleSyncPacket(jsonObject);
					this.sendMsgData(syncData, Constants.ACTION_SHOW_NOTIFICATION);
					if (packet.getMsgType() == 0){
						packet.setMsgType(1);
						mConnManager.getConnection().sendPacket(packet);
						LogCatLog.d(LOGTAG,"请求回应...");
					}

					LogCatLog.d(LOGTAG, "message notice success");
				} catch (JSONException e) {
					e.printStackTrace();
					// 停止后续的动作
					return;
				}
				finally {
					restartSyncHeartTimerBeforeReturn();
				}
			}else{
				LogCatLog.e(LOGTAG,"message notice error");
			}

			// 如果是服务端的回复包,则不需要给服务端发送回复
			if (packet.getMsgType() == PacketConstants.MSG_PUSH_TYPE_RESPONSE
					|| (syncData.getOpCode() == LinkSyncConstants.LINK_SYNC_OPCODE_SEND_ACK)) {
				LogUtil.LogOut(3, LOGTAG, "processPacket() this is TYPE_RESPONSE or OPCODE_SEND_ACK.");
				restartSyncHeartTimerBeforeReturn();
				// 不需要返回ack
				return;
			}


//			Packet respPacket;
//			try {
//				respPacket = PacketFactory
//						.getPacket(mConnManager.getProtoVer());
//				respPacket.setMsgId(PacketConstants.MSG_PUSH_LINKSYNC);
//				respPacket.setMsgType(PacketConstants.MSG_PUSH_TYPE_RESPONSE);
//			} catch (Exception e) {
//				e.printStackTrace();
//				return;
//			} finally {
//				restartSyncHeartTimerBeforeReturn();
//			}
//
//			// 发送响应
//			try {
//				JSONObject dataResp = new JSONObject();
//
//				dataResp.put(LinkSyncConstants.LINK_SYNC_KEY,
//						syncData.getSyncKey());
//				dataResp.put(LinkSyncConstants.LINK_SYNC_OPCODE,
//						LinkSyncConstants.LINK_SYNC_OPCODE_SEND_ACK);
//
//				respPacket.setData(dataResp.toString());
//				mConnManager.getConnection().sendPacket(respPacket);
//				LogUtil.LogOut(3, LOGTAG,
//						"发送响应"
//								+ dataResp.toString());
//				LogUtil.LogOut(3, LOGTAG,
//						"processPacket() respPacket is sent. dataResp:"
//								+ dataResp.toString());
//
//			} catch (Exception e) {
//				e.printStackTrace();
//				return;
//			} finally {
//				restartSyncHeartTimerBeforeReturn();//启动下一个心跳
//			}
			restartSyncHeartTimerBeforeReturn();//启动下一个心跳
			return;
		}
	}

	private void restartSyncHeartTimerBeforeReturn() {
		// 启动下次心跳包时钟
//		mConnManager.startHeartTimer();
		if (mConnManager.isForceStopped()) {
			mConnManager.disconnect();
		} else {
			// 启动下次心跳包时钟
			mConnManager.startHeartTimer();//用sync心跳 代替 纯心跳 后，从 HeartBeatPacketListenerImpl 迁移过来的 by alex
		}
	}
	
	public LinkSyncPacket handleSyncPacket(JSONObject syncData) {
		LinkSyncPacket syncPacket = new LinkSyncPacket();
		syncPacket.setSyncKey(syncData.optLong(LinkSyncConstants.LINK_SYNC_KEY));
		syncPacket.setOpCode(syncData.optInt(LinkSyncConstants.LINK_SYNC_OPCODE));
		syncPacket.setData(syncData.optString(LinkSyncConstants.LINK_SYNC_DATA));
		LinkSyncInfo syncInfo = new LinkSyncInfo(mConnManager.getContext());
		String userId = mConnManager.getUsername();
		if (syncInfo.getSyncKey(userId) < syncPacket.getSyncKey()) {
			// 收到有效的syncKey就持久化存储
			syncInfo.setSyncKey(userId, syncPacket.getSyncKey());
		} else {
		}
		return syncPacket;
	}

	private void sendMsgData(LinkSyncPacket syncPacket, String action) {
		LogUtil.LogOut(3, LOGTAG,
				"sendMsgData() syncKey=" + syncPacket.getSyncKey() + ", data="
						+ syncPacket.getData());

		String syncData = syncPacket.getData();
		long syncOpCode = syncPacket.getOpCode();
		long syncKey = syncPacket.getSyncKey();
		if (syncData != null && syncData.length() > 0) {
			try {

				if (mConnManager.getPacketNotifier() != null) {
					Bundle bundle = new Bundle();
					bundle.putLong(LinkSyncConstants.LINK_SYNC_KEY, syncKey);
					bundle.putLong(LinkSyncConstants.LINK_SYNC_OPCODE,syncOpCode);
					bundle.putString(LinkSyncConstants.MSG_SYNC_DATA, syncData);
					mConnManager.getPacketNotifier().onReceivedPacket(bundle);// 发送消息
					LogUtil.LogOut(2, LOGTAG,
							"=======sendMsgData() getPacketNotifier success======");
				} else {
					LogCatLog.e(LOGTAG,
							"=======sendMsgData() getPacketNotifier failed=======");
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}


	/**
	 * 回复消息
	 */
	public void recvMessage(){

	}

}
