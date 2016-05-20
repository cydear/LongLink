package com.xianglin.fellowvillager.app.longlink.rome.longlinkservice.msg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.xianglin.fellowvillager.app.longlink.longlink.LongLinkServiceManager;
import com.xianglin.fellowvillager.app.longlink.longlink.service.ReconnCtrl;
import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;
import com.xianglin.fellowvillager.app.longlink.longlink.util.Constants;
import com.xianglin.fellowvillager.app.longlink.rome.longlinkservice.LongLinkMsgConstants;
import com.xianglin.fellowvillager.app.longlink.rome.longlinkservice.service.LongLinkPacketHandler;
import com.xianglin.mobile.common.info.AppInfo;
import com.xianglin.mobile.common.logging.LogCatLog;
import com.xianglin.mobile.framework.XiangLinApplication;
import com.xianglin.mobile.framework.service.ext.security.bean.UserInfo;


/**
 * 接收系统信息
 * 
 * 有很多用户信息方面的改动
 * 
 * @author alex
 *
 */
public class LongLinkeMsgReceiver extends BroadcastReceiver {
	private final String TAG = ConfigUtils.TAG;

	@Override
	public void onReceive(final Context context, Intent intent) {
		LogCatLog.i(TAG, "onReceive: actionType = " + intent.getAction());

		XiangLinApplication appContext = XiangLinApplication.getInstance();

		// 登录成功事件广播
		if (com.xianglin.mobile.common.msg.MsgCodeConstants.SECURITY_LOGIN
				.equals(intent.getAction())) {
			ReconnCtrl.setConAction(Constants.CONNECT_ACTION_ACTIVE);
			// 获取当前用户信息 
//			TODO BY ALEX
//			AuthService mAuthService = appContext.getMicroApplicationContext()
//					.getExtServiceByInterface(AuthService.class.getName());
//			UserInfo userInfo = mAuthService.getUserInfo();
			UserInfo userInfo = null;
//			if (userInfo != null) {
//				LogCatLog.i(TAG,
//						"onReceive: login getUserId=" + userInfo.getUserId());
//			} else {
//				LogCatLog.w(TAG,
//						"onReceive: SECURITY_LOGIN getUserInfo in null!");
//				return;
//			}

			// 注册packet回调函数
			LongLinkServiceManager servManager = LongLinkServiceManager
					.getInstance(appContext);
			servManager
					.registerCommonFunc(LongLinkPacketHandler.getInstance(appContext));
			LogCatLog.i(TAG, "onReceive: register LongLinkPacketHandler...");

			// debug模式，则可以设置连接地址
			if (AppInfo.getInstance().isDebuggable()) {
				LongLinkServerSetting instance = LongLinkServerSetting
						.getInstance();
				String test_host = instance.getLongLinkHost(context);
				int test_port = instance.getLongLinkPort(context);
				String test_ssl = instance.getLongLinkSSLFlag(context);

				servManager.setLinkAddr(test_host, test_port, test_ssl);
				LogCatLog.i(TAG,
						"LongLinkServiceManager: setLinkAddr test_host:"
								+ test_host + ", test_port:" + test_port
								+ ", test_ssl:" + test_ssl);
			}

			// 设置用户信息
			servManager.setAppUserInfo(userInfo.getUserId(),userInfo.getDeviceId(),
					userInfo.getSessionId(), userInfo.getLoginTime());

		} else if (com.xianglin.mobile.common.msg.MsgCodeConstants.SECURITY_CLEANACCOUNT_ACTION
				.equals(intent.getAction())) {
			String delUserId = intent
					.getStringExtra(com.xianglin.mobile.common.msg.MsgCodeConstants.SECURITY_LOGIN_USERID);

			if (delUserId != null && delUserId.length() > 0) {
				LogCatLog.i(TAG, "onReceive: clear userId=" + delUserId);

				LongLinkServiceManager servManager = LongLinkServiceManager
						.getInstance(appContext);

				// 获取当前用户信息
//				TODO BY ALEX
//				AuthService mAuthService = appContext
//						.getMicroApplicationContext().getExtServiceByInterface(
//								AuthService.class.getName());
//				UserInfo userInfo = mAuthService.getUserInfo();

//				if (userInfo != null) {
//					LogCatLog.i(TAG,
//							"onReceive: curUserId=" + userInfo.getUserId());
//
//					if (delUserId.equals(userInfo.getUserId())) {
//						// 清除當前用戶信息
//						servManager.setAppUserInfo("", "", "");
//					} else {
//						// 非当前用户，不做后续处理
//						LogCatLog
//								.i(TAG,
//										"onReceive: cleard userId is not the cur-userId!");
//					}
//
//				} else {
//					// 当前无有效用户，清除长连接服务的当前用戶信息
//					servManager.setAppUserInfo("", "", "");
//
//					return;
//				}

			}

		} else if (com.xianglin.mobile.common.msg.MsgCodeConstants.SECURITY_LOGOUT
				.equals(intent.getAction())
				|| intent.getAction().equals("com.xianglin.security.startlogin")) {

			LongLinkServiceManager servManager = LongLinkServiceManager
					.getInstance(appContext);
			servManager.setAppUserInfo("","", "", "");

		} else if (com.xianglin.mobile.framework.msg.MsgCodeConstants.FRAMEWORK_ACTIVITY_START
				.equals(intent.getAction())) {
			LongLinkServiceManager servManager = LongLinkServiceManager
					.getInstance(appContext);
			servManager.startLink();

		} else if (com.xianglin.mobile.framework.msg.MsgCodeConstants.FRAMEWORK_ACTIVITY_USERLEAVEHINT
				.equals(intent.getAction())) {
			// 在切换到后台、屏幕关闭和初始化时设置此标志位
			// 达到切换到前台时进行active调用的目的。（因为没有切换到前台的事件）
			ReconnCtrl.setConAction(Constants.CONNECT_ACTION_ACTIVE);
			LongLinkServiceManager servManager = LongLinkServiceManager
					.getInstance(appContext);
			servManager.stopLink();

		} else if (LongLinkMsgConstants.LONGLINK_ACTION_CMD_UPLINK
				.equals(intent.getAction())) {

			String channel = intent
					.getStringExtra(LongLinkMsgConstants.MSG_PACKET_CHANNEL);
			if (channel != null && channel.equals(LongLinkMsgConstants.MSG_PACKET_CHANNEL_PUSH)) {

				String appId = intent
						.getStringExtra(LongLinkMsgConstants.LONGLINK_APPID);
				String appData = intent
						.getStringExtra(LongLinkMsgConstants.LONGLINK_APPDATA);
				LogCatLog.i(TAG,
						"onReceive: uplink appId=" + appId
								+ ", appData=" + appData);
				
				if (appData != null) {
					// 请求的净数据长度限制为4k
					if (appData.length() > 0 && appData.length() < 1024*4) {
						LongLinkServiceManager servManager = LongLinkServiceManager
								.getInstance(context);
						servManager.sendPacketUplink(channel, appId, appData);
					} else {
						LogCatLog.w(TAG,
								"onReceive: uplink appdata warning length=" + appData.length());
					}
				}
			
			}

		} else if (LongLinkMsgConstants.LONGLINK_ACTION_SYNC_TO
				.equals(intent.getAction())) {

			String channel = intent
					.getStringExtra(LongLinkMsgConstants.MSG_PACKET_CHANNEL);
			if (channel != null && channel.equals(LongLinkMsgConstants.MSG_PACKET_CHANNEL_SYNC)) {
				String bizType = intent
						.getStringExtra(LongLinkMsgConstants.MSG_PACKET_TYPE);
				String bizData = intent
						.getStringExtra(LongLinkMsgConstants.LONGLINK_APPDATA);
				LogCatLog.i(TAG,
						"onReceive: syncto bizType=" + bizType
								+ ", bizData=" + bizData);
				
				if (bizData != null) {
					// 请求的净数据长度限制为4k
					if (bizData.length() > 0 && bizData.length() < 1024*4) {
						LongLinkServiceManager servManager = LongLinkServiceManager
								.getInstance(context);
						servManager.sendPacketUplink(channel, bizType, bizData);
					} else {
						LogCatLog.w(TAG,
								"onReceive: syncto appdata warning length=" + bizData.length());
					}
				}
			}
			

		}

	}
}
