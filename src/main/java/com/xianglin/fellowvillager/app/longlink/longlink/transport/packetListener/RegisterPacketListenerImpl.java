package com.xianglin.fellowvillager.app.longlink.longlink.transport.packetListener;

import android.content.Intent;
import android.os.Bundle;

import com.xianglin.fellowvillager.app.longlink.longlink.service.ConnManager;
import com.xianglin.fellowvillager.app.longlink.longlink.service.LongLinkAppInfo;
import com.xianglin.fellowvillager.app.longlink.longlink.sync.LinkSyncConstants;
import com.xianglin.fellowvillager.app.longlink.longlink.sync.LinkSyncPacket;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.connection.PushCtrlConfiguration;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.Packet;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketConstants;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketFilter;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketIDFilter;
import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;
import com.xianglin.fellowvillager.app.longlink.longlink.util.Constants;
import com.xianglin.fellowvillager.app.longlink.longlink.util.LogUtil;
import com.xianglin.mobile.common.logging.LogCatLog;

import org.json.JSONObject;


/**
 * 初始化完成
 * 客户端做初始化链接，服务端给回的消息进行解析
 *
 */
public class RegisterPacketListenerImpl implements PacketListener {
	private static final String LOGTAG = ConfigUtils.TAG;

	private final ConnManager mConnManager;

	public RegisterPacketListenerImpl(ConnManager connManager) {
		this.mConnManager = connManager;
	}

	@Override
	public void processPacket(Packet packet) {// 初始化
		// 此处仅处理心跳packet，故用filter过滤

		PacketFilter packetFilter = new PacketIDFilter(
				PacketConstants.MSG_PUSH_INITIALIZE);// 0 ID = 0

		if (packetFilter.accept(packet)) {
			mConnManager.setRegistered(true);
			LogCatLog.d(LOGTAG, "这是客户端做初始化连接的监听[RegisterPacketListenerImpl]" + packet.toString());
			String registerData = packet.getData();
			JSONObject registerJson;
			try {
				registerJson = new JSONObject(registerData);
				// 心跳间隔时间
				PushCtrlConfiguration.setKeepAliveInterval(registerJson
						.optInt(Constants.CONNECT_KEEPLIVE_TIME));//keepLiveTime

				// 心跳超时时间
				int tempInt = registerJson
						.optInt(Constants.CONNECT_HEART_TIMEOUT);
				PushCtrlConfiguration.setPacketReplyTimeout( tempInt );//heartTimeOut

				// reset waiting time after this inited command successfully
				// or else reconnect machanism will be effected
				mConnManager.resetWaitingTime();
			} catch (Throwable e) {//JSONException IllegalArgumentException
				mConnManager.setRegistered(false);

				e.printStackTrace();
			}

			LongLinkAppInfo appInfo = LongLinkAppInfo.getInstance();
			if(appInfo.getUserId()!= null && !appInfo.getUserId().equals("")){
				LogUtil.LogOut(3, LOGTAG, "===== 长链接注册成功=====");
				Intent intent = new Intent();
				intent.putExtra("ISSUCCESS",true);// 成功
				intent.setAction("android.intent.action.LONGLINKCONNECTHANDLER");
				mConnManager.getContext().sendBroadcast(intent);
				LogUtil.LogOut(3, LOGTAG, "===== 发送注册成功广播=====");
				sendMsgData();
			}
			LogUtil.LogOut(
					3,
					LOGTAG,
					"processPacket() replyTimeout="
							+ PushCtrlConfiguration.getPacketReplyTimeout()/1000
							+ "s, keepLiveTime="
							+ PushCtrlConfiguration.getKeepAliveInterval());

			mConnManager.submitLinkSyncTask("");//重要！
			
			// Start keep alive task
			mConnManager.startHeartTimer();
		}
	}

	private void sendMsgData() {
			try {

				if (mConnManager.getPacketNotifier() != null) {
					Bundle bundle = new Bundle();
					bundle.putBoolean("ISSUCCESS",true);
					bundle.putBoolean("ISREGISTER",true);
					bundle.putString("ACTION","android.intent.action.LONGLINKCONNECTHANDLER");
					mConnManager.getPacketNotifier().onReceivedPacket(bundle);// 发送消息
					LogUtil.LogOut(2, LOGTAG,
							"=======send data register success======");
				} else {
					LogCatLog.e(LOGTAG,
							"=======send data register  failed=======");
				}

			} catch (Exception e) {
				e.printStackTrace();
			}


	}

}
