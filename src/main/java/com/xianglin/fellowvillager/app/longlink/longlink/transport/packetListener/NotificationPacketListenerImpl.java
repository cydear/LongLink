package com.xianglin.fellowvillager.app.longlink.longlink.transport.packetListener;

import android.os.Bundle;
import android.os.RemoteException;

import com.xianglin.fellowvillager.app.longlink.longlink.msg.MsgInfo;
import com.xianglin.fellowvillager.app.longlink.longlink.msg.MsgRecord;
import com.xianglin.fellowvillager.app.longlink.longlink.msg.PerMsgRecord;
import com.xianglin.fellowvillager.app.longlink.longlink.service.ConnManager;
import com.xianglin.fellowvillager.app.longlink.longlink.sync.LinkSyncConstants;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.Packet;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketConstants;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketFactory;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketFilter;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketIDFilter;
import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;
import com.xianglin.fellowvillager.app.longlink.longlink.util.Constants;
import com.xianglin.fellowvillager.app.longlink.longlink.util.LogUtil;
import com.xianglin.mobile.common.logging.LogCatLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * 通知包监听
 */
public class NotificationPacketListenerImpl implements PacketListener {
	private static final String LOGTAG = ConfigUtils.TAG;

	private final ConnManager mConnManager;

	public NotificationPacketListenerImpl(ConnManager connManager) {
		this.mConnManager = connManager;
	}

	public void processPacket(Packet packet) {
		String jsonString = "";

		// 此处仅处理业务类packet，故用filter过滤
		PacketFilter packetFilter = new PacketIDFilter(
				PacketConstants.MSG_PUSH_MSGDATA);// 4 ID ＝ 4

		if (packetFilter.accept(packet)) {
			LogCatLog.d(LOGTAG, "这是通知包的监听[NotificationPacketListenerImpl]" + packet.toString());
			// 收到通知消息
			LogUtil.LogOut(3, LOGTAG,
					"NotificationPacketListener.processPacket()...");

			MsgInfo msgInfo = null;
			int msgId = packet.getMsgId();
			int msgType = packet.getMsgType();
			int dataLength = packet.getDataLength();
			int packetVersion = packet.getPacketVersion();
			byte[] buffer = packet.getHdrbufforWrite();

			jsonString = packet.getData();// 包数据
			if (jsonString != null && jsonString.length() > 0) {
				try {
					// 提取协议数据内容
					JSONObject jsonObject = new JSONObject(jsonString);

					msgInfo = this.handlePushMsg(jsonObject);
					this.sendMsgData(msgInfo,
							Constants.ACTION_SHOW_NOTIFICATION);
				} catch (JSONException e) {
					e.printStackTrace();
					// 停止后续的动作
					return;
				}
			}

			// 如果是服务端的回复包,则不需要给服务端发送回复
			if (packet.getMsgType() == PacketConstants.MSG_PUSH_TYPE_RESPONSE) {
				return;
			}


			Packet respPacket;
			try {
				respPacket = PacketFactory
						.getPacket(mConnManager.getProtoVer());
				respPacket.setMsgId(PacketConstants.MSG_PUSH_MSGDATA);
				respPacket.setMsgType(PacketConstants.MSG_PUSH_TYPE_RESPONSE);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			// 发送响应
			try {
				JSONObject dataResp = new JSONObject();

				dataResp.put(Constants.CONNECT_TOKEN_USER,
						mConnManager.getUsername());
				dataResp.put(Constants.MSG_KEY, msgInfo.getMsgKey());

				respPacket.setData(dataResp.toString());
				LogUtil.LogOut(3, LOGTAG,
						"发送响应"+dataResp.toString());
				mConnManager.getConnection().sendPacket(respPacket);

				LogUtil.LogOut(3, LOGTAG,
						"processPacket() respPacket is sent. dataResp:"
								+ dataResp.toString());

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private MsgInfo handlePushMsg(JSONObject msgItem) {

		MsgInfo msgInfo = new MsgInfo();

		msgInfo.setUserId(msgItem.optString(Constants.CONNECT_TOKEN_USER));
		msgInfo.setMsgData(msgItem.optString(Constants.MSG_DATA));
		msgInfo.setMsgKey(msgItem.optString(Constants.MSG_KEY));
		msgInfo.setTimestamp(msgItem.optString(Constants.MSG_TIMESTAMP));
		msgInfo.setPerMsgId(msgInfo.getTimestamp() + msgInfo.getMsgKey());

		LogUtil.LogOut(
				3,
				LOGTAG,
				"processMsgList() msgkey=" + msgInfo.getMsgKey()
						+ ", timestamp=" + msgInfo.getTimestamp() + ", userId="
						+ msgInfo.getUserId() + ", msgData="
						+ msgInfo.getMsgData());

		return msgInfo;
	}

	private void sendMsgData(MsgInfo msgInfo, String action) {
		MsgRecord msgRecord = null;

		if (msgInfo.getPerMsgId().length() > 0) {
			msgRecord = new PerMsgRecord(mConnManager.getContext());
		}

		msgRecord.setCurUserId(msgInfo.getUserId());

		if (!msgRecord.isContainMsg(msgInfo)) {
			// 保存收到的内容MsgID
			msgRecord.saveMsgRecord(msgInfo);
			LogUtil.LogOut(4, LOGTAG, "processMsgList() saved ths msg.");

			try {
				if (mConnManager.getPacketNotifier() != null) {
					// 协议适配
					JSONObject pushData = new JSONObject();
					pushData.put(
							LinkSyncConstants.LINK_SYNC_DATA_BIZ,
							LinkSyncConstants.LINK_SYNC_DATA_BIZ_DEFAULT);
					pushData.put(
							LinkSyncConstants.LINK_SYNC_DATA_MD,
							msgInfo.getMsgData());

					JSONArray jsonArray = new JSONArray();
					jsonArray.put(pushData);

					Bundle bundle = new Bundle();
					bundle.putString(LinkSyncConstants.MSG_SYNC_DATA, jsonArray.toString());

					mConnManager.getPacketNotifier().onReceivedPacket(bundle);
				} else {
					LogUtil.LogOut(2, LOGTAG,
							"sendMsgData() getPacketNotifier failed.");
				}

			} catch (RemoteException e1) {
				e1.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			LogUtil.LogOut(4, LOGTAG, "sendMsgData() Done!");
		} else {
			LogUtil.LogOut(2, LOGTAG, "sendMsgData() Drop the Packet!");

			LogUtil.LogOut(3, LOGTAG,
					"sendMsgData() msgkey=" + msgInfo.getMsgKey()
							+ ", timestamp=" + msgInfo.getTimestamp());
		}
	}
}
