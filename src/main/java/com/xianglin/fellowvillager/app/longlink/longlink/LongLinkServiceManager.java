package com.xianglin.fellowvillager.app.longlink.longlink;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.xianglin.fellowvillager.app.ILongLinkPacketNotifer;
import com.xianglin.fellowvillager.app.ILongLinkService;
import com.xianglin.fellowvillager.app.longlink.longlink.service.LongLinkService;
import com.xianglin.fellowvillager.app.longlink.longlink.servicelistener.LongLinkServiceConnectListener;
import com.xianglin.fellowvillager.app.longlink.longlink.sync.LinkSyncConstants;
import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;
import com.xianglin.fellowvillager.app.longlink.longlink.util.Constants;
import com.xianglin.fellowvillager.app.longlink.longlink.util.LogUtil;
import com.xianglin.fellowvillager.app.longlink.rome.longlinkservice.service.LongLinkPacketHandler;
import com.xianglin.mobile.common.logging.LogCatLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This class is to manage the 
 * notification service 
 * and to load the
 * configuration.
 * 
 */
public final class LongLinkServiceManager {

	private static final String LOGTAG = ConfigUtils.TAG;
	private static LongLinkServiceManager instance;

	private Context mContext;
	private boolean mIsServiceBound;
	private ILongLinkService mService;

	private LongLinkServiceConnectListener longLinkServiceConnectListener;

	private PacketHanlder mComPktHanlder;
	private ConcurrentHashMap<String, PacketHanlder> mAppPktHanlderMap = new ConcurrentHashMap<String, PacketHanlder>();

	private LongLinkServiceManager(Context context) {
		this.mContext = context;
	}

	public static synchronized LongLinkServiceManager getInstance(
			Context context) {
		if (instance == null) {
			instance = new LongLinkServiceManager(context);
		}

		return instance;
	}

	/**
	 * 绑定服务
	 * @param longLinkServiceConnectListener 服务监听 1:连接服务成功 2:连接服务失败
	 */
	public void bindService(LongLinkServiceConnectListener longLinkServiceConnectListener) {
		if (mService == null) {
			this.longLinkServiceConnectListener = longLinkServiceConnectListener;
			mContext.getApplicationContext().bindService(
					new Intent(mContext, LongLinkService.class), mServiceConn,//com.xianglin.mobile.longlink.service.LongLinkService
					Context.BIND_AUTO_CREATE);

			LogUtil.LogOut(3, LOGTAG,
					"LongLinkServiceManager bindService done.");
		} else {
			LogUtil.LogOut(2, LOGTAG,
					"LongLinkServiceManager mService is null.");
		}
	}

	public void unBindService() {
		if (mService != null) {
			try {
				mService.disConnect();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		this.clearAppHanlderMap();
		this.unRegisterCommonFunc();

		if (mIsServiceBound) {
			mContext.getApplicationContext().unbindService(mServiceConn);
			mIsServiceBound = false;
		}
		mContext.getApplicationContext().stopService(
				new Intent(mContext, LongLinkService.class));
		mService = null;
	}

	private ServiceConnection mServiceConn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = ILongLinkService.Stub.asInterface(service);

			if (mService != null) {
				mIsServiceBound = true;
				longLinkServiceConnectListener.connectSuccess(mService,mIsServiceBound);
			}
			LogUtil.LogOut(3, LOGTAG, "onServiceConnected");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mIsServiceBound = false;
			longLinkServiceConnectListener.connectSuccess(mService,mIsServiceBound);
			LogUtil.LogOut(3, LOGTAG, "onServiceDisconnected");
		}

	};

	public boolean isConnected() {
		boolean ret = false;

		if (mService != null) {
			try {
				ret = mService.isConnected();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		LogUtil.LogOut(3, LOGTAG, "isConnected ret=" + ret);
		return ret;

	}

	public void startLink() {
		if (mService != null) {// null! alex
			try {
				LogUtil.LogOut(3, LOGTAG, "startLink will be called.");

				if (mService.isConnected()) {
					LogUtil.LogOut(3, LOGTAG, "LongLink is still working...");
				} else {
					mService.reConnect();
				}

			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

	}

	public void stopLink() {
		if (mService != null) {
			try {
				LogUtil.LogOut(3, LOGTAG, "stopLink will be called.");

				mService.disConnect();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

	}

	public void setAppUserInfo(String userId,String deviceId, String extToken, String loginTime) {
		if (mService != null) {
			try {
				LogUtil.LogOut(3, LOGTAG, "setAppUserInfo is called. userId="
						+ userId + ", loginTime=" + loginTime);

				mService.setAppUserInfo(userId, deviceId ,extToken, loginTime);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

	}

	private void setPacketListener(ILongLinkPacketNotifer packetNotifer) {
		if (mService != null) {
			try {
				mService.setPacketNotifer(packetNotifer);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private ILongLinkPacketNotifer mPacketNotifer = new ILongLinkPacketNotifer.Stub() {

		@Override
		public void onReceivedPacket(Bundle bundle) throws RemoteException {
					if (!bundle.getBoolean("ISREGISTER")) {
							String syncData = bundle.getString(LinkSyncConstants.MSG_SYNC_DATA);
							long syncKey = bundle.getLong(LinkSyncConstants.LINK_SYNC_KEY);
							long opCode = bundle.getLong(LinkSyncConstants.LINK_SYNC_OPCODE);

							try {
								JSONArray appArray = new JSONArray(syncData);
								LogUtil.LogOut(3, LOGTAG, "接收到的消息长度="
										+ appArray.length());
								JSONObject msgData = new JSONObject();
								for (int i = 0; i < appArray.length(); i++) {
									msgData = appArray.getJSONObject(i);

									handleSyncMsgData(msgData, syncKey, opCode);
								}


							} catch (Exception e) {
								LogUtil.LogOut(2, LOGTAG, "=====> process info fail");
							}
					}else {
							LogUtil.LogOut(2,LOGTAG,"register success notify biz ");
							onRegisterHandler(bundle.getBoolean("ISSUCCESS"));
					}
		}

	};
	
	private void handleSyncMsgData(JSONObject msgData,long syncKey ,long opCode) {
		if (msgData != null) {
			onCommonHanlder(msgData,syncKey,opCode);
			LogUtil.LogOut(3, LOGTAG,"onCommonHanlder --------------- onCommonHanlder");
//			if (onAppHanlder(msgData,syncKey,opCode)) {
//				LogUtil.LogOut(3, LOGTAG,"onAppHanlder --------------- onAppHanlder");
//			} else {
				//TODO 暂时这样放 后面在分析
//			}

		}
	}

	private void onRegisterHandler(boolean isRegister){
		try{
			do {
				LogCatLog.d(LOGTAG,"自旋注册回调");
				registerCommonFunc(LongLinkPacketHandler.getInstance(this.mContext));
				if (mComPktHanlder == null) {
					Thread.sleep(100);
				}
			}while (mComPktHanlder == null);

			if (mComPktHanlder != null) {
				mComPktHanlder.processPacket(isRegister,0);
			}
		}catch(Exception e){
			LogCatLog.e(LOGTAG,e);
		}

	}

	private void onCommonHanlder(JSONObject appPacket,long syncKey ,long opCode) {
		String bizType = appPacket.optString(LinkSyncConstants.LINK_SYNC_DATA_BIZ);
		String bizData = appPacket.optString(LinkSyncConstants.LINK_SYNC_DATA_MD);

		if ((bizData != null && bizData.length() > 0)
				&& (bizType != null && bizType.length() > 0)) {
			
			if (bizType.equals(LinkSyncConstants.LINK_SYNC_DATA_BIZ_DEFAULT)) {// default
				try {
					JSONObject jsonData = new JSONObject(bizData);
	
					String appId = jsonData.optString(Constants.MSG_APPID);
					String appData = jsonData.optString(Constants.MSG_PAYLOAD);
	
					if (appId != null && appId.length() > 0) {
						LogUtil.LogOut(3, LOGTAG, "onCommonHanlder appId=" + appId);
						do {
							LogCatLog.d(LOGTAG,"自旋注册回调");
							//去 mComPktHanlder ！＝ null
							//告诉A进程 去吧这个注册一下
							registerCommonFunc(LongLinkPacketHandler.getInstance(this.mContext));
							if (mComPktHanlder == null) {
								Thread.sleep(100);
							}
						}while (mComPktHanlder == null);

						if (mComPktHanlder != null) {
							mComPktHanlder.processPacket(bizType, appId, appData,syncKey,opCode);
							LogUtil.LogOut(3, LOGTAG,
									"mComPktHanlder processPacket done. ");
						} else {
							LogUtil.LogOut(2, LOGTAG,
									"Warning! mComPktHanlder is invalid.");
						}
					} else {
						LogUtil.LogOut(2, LOGTAG,
								"Warning! mComPktHanlder appId is invalid.");
					}
	
				} catch (Exception e) {
					LogUtil.LogOut(2, LOGTAG, "Warning! mComPktHanlder Exception.");
					e.printStackTrace();
				}
			} else if(bizType.equals(LinkSyncConstants.LINK_SYNC_DATA_BIZ_NOTICE)){//notice
				try {

					String appId = "chat";
					bizType ="chat";
					if (appId != null && appId.length() > 0) {
						LogUtil.LogOut(3, LOGTAG, "onCommonHanlder appId=" + appId);

						if (mComPktHanlder != null) {
							mComPktHanlder.processPacket(bizType, appId, bizData,syncKey,opCode);

							LogUtil.LogOut(3, LOGTAG,
									"mComPktHanlder processPacket done. ");
						} else {
							LogUtil.LogOut(2, LOGTAG,
									"Warning! mComPktHanlder is invalid.");
						}
					} else {
						LogUtil.LogOut(2, LOGTAG,
								"Warning! mComPktHanlder appId is invalid.");
					}

				} catch (Exception e) {
					LogUtil.LogOut(2, LOGTAG, "Warning! mComPktHanlder Exception.");
					e.printStackTrace();
				}
			}else {// chat
				// sync data
				try {
	
					String appId = "chat";
					if (appId != null && appId.length() > 0) {
						LogUtil.LogOut(3, LOGTAG, "onCommonHanlder appId=" + appId);
	
						if (mComPktHanlder != null) {
							mComPktHanlder.processPacket(bizType, appId, bizData,syncKey,opCode);
	
							LogUtil.LogOut(3, LOGTAG,
									"mComPktHanlder processPacket done. ");
						} else {
							LogUtil.LogOut(2, LOGTAG,
									"Warning! mComPktHanlder is invalid.");
						}
					} else {
						LogUtil.LogOut(2, LOGTAG,
								"Warning! mComPktHanlder appId is invalid.");
					}
	
				} catch (Exception e) {
					LogUtil.LogOut(2, LOGTAG, "Warning! mComPktHanlder Exception.");
					e.printStackTrace();
				}
			}

		} else {
			LogUtil.LogOut(2, LOGTAG,
					"Warning! mComPktHanlder bizType or bizData is invalid.");
		}

	}

	/**
	 *  暂放
	 * @param appPacket
	 * @param syncKey
	 * @param opCode
	 * @return
	 */
	private boolean onAppHanlder(JSONObject appPacket,long syncKey ,long opCode) {
		boolean ret = true;

		String bizType = appPacket.optString(LinkSyncConstants.LINK_SYNC_DATA_BIZ);
		String bizData = appPacket.optString(LinkSyncConstants.LINK_SYNC_DATA_MD);

		if ((bizData != null && bizData.length() > 0)
				&& (bizType != null && bizType.length() > 0)) {
			
			if (bizType.equals(LinkSyncConstants.LINK_SYNC_DATA_BIZ_DEFAULT)) {// default

				try {
					JSONObject jsonData = new JSONObject(bizData);
	
					String appId = jsonData.optString(Constants.MSG_APPID);
					String appData = jsonData.optString(Constants.MSG_PAYLOAD);
	
					if (appId != null && appId.length() > 0) {
						LogUtil.LogOut(3, LOGTAG, "onCommonHanlder appId=" + appId);
	
						if (mAppPktHanlderMap != null
								&& mAppPktHanlderMap.containsKey(appId)) {
	
							PacketHanlder appPacketHanlder = mAppPktHanlderMap
									.get(appId);
	
							if (appPacketHanlder != null) {
								appPacketHanlder.processPacket(bizType, appId,appData,syncKey,opCode);
								LogUtil.LogOut(3, LOGTAG,
										"onAppHanlder processPacket done. ");
	
							} else {
								ret = false;
							}
						} else {
							ret = false;
						}
					} else {
						LogUtil.LogOut(2, LOGTAG,
								"Warning! mComPktHanlder appId is invalid.");
					}
	
				} catch (Exception e) {
					LogUtil.LogOut(2, LOGTAG, "Warning! mComPktHanlder Exception.");
					e.printStackTrace();
				}
			}else if (bizType.equals(LinkSyncConstants.LINK_SYNC_DATA_BIZ_NOTICE))//通知
			{
				// sync data
				try {

					String appId = "chat";
					bizType ="chat";
					if (appId != null && appId.length() > 0) {
						LogUtil.LogOut(3, LOGTAG, "onCommonHanlder appId=" + appId);

						if (mAppPktHanlderMap != null
								&& mAppPktHanlderMap.containsKey(appId)) {

							PacketHanlder appPacketHanlder = mAppPktHanlderMap
									.get(appId);

							if (appPacketHanlder != null) {
								appPacketHanlder.processPacket(bizType, appId,bizData,syncKey,opCode);
								LogUtil.LogOut(3, LOGTAG,
										"onAppHanlder processPacket done. ");

							} else {
								ret = false;
							}
						} else {
							ret = false;
						}
					} else {
						LogUtil.LogOut(2, LOGTAG,
								"Warning! mComPktHanlder appId is invalid.");
					}

				} catch (Exception e) {
					LogUtil.LogOut(2, LOGTAG, "Warning! mComPktHanlder Exception.");
					e.printStackTrace();
				}

		}else {// chat
				// sync data
				try {
	
					String appId = "chat";
					if (appId != null && appId.length() > 0) {
						LogUtil.LogOut(3, LOGTAG, "onCommonHanlder appId=" + appId);
	
						if (mAppPktHanlderMap != null
								&& mAppPktHanlderMap.containsKey(appId)) {
	
							PacketHanlder appPacketHanlder = mAppPktHanlderMap
									.get(appId);
	
							if (appPacketHanlder != null) {
								appPacketHanlder.processPacket(bizType, appId,bizData,syncKey,opCode);
								LogUtil.LogOut(3, LOGTAG,
										"onAppHanlder processPacket done. ");
	
							} else {
								ret = false;
							}
						} else {
							ret = false;
						}
					} else {
						LogUtil.LogOut(2, LOGTAG,
								"Warning! mComPktHanlder appId is invalid.");
					}
	
				} catch (Exception e) {
					LogUtil.LogOut(2, LOGTAG, "Warning! mComPktHanlder Exception.");
					e.printStackTrace();
				}
			}

		} else {
			LogUtil.LogOut(2, LOGTAG,
					"Warning! mComPktHanlder bizType or bizData is invalid.");
		}

		LogUtil.LogOut(3, LOGTAG, "onAppHanlder is done. ret=" + ret);
		return ret;
	}

	private void clearAppHanlderMap() {
		if (mAppPktHanlderMap != null && !mAppPktHanlderMap.isEmpty()) {
			mAppPktHanlderMap.clear();
		}
		LogUtil.LogOut(3, LOGTAG, "clearAppHanlderMap is done.");
	}

	public void registerAppAppHanlder(String appId, PacketHanlder packetHandler) {
		if (mAppPktHanlderMap != null) {
			LogUtil.LogOut(4, LOGTAG, "registerAppAppHanlder the size is "
					+ mAppPktHanlderMap.size());

			if ((appId != null && appId.length() > 0) && packetHandler != null) {
				mAppPktHanlderMap.put(appId, packetHandler);

				LogUtil.LogOut(3, LOGTAG, "registerAppAppHanlder is done.");
			} else {
				LogUtil.LogOut(2, LOGTAG,
						"registerAppAppHanlder params are invalid. AppId="
								+ appId);
			}
		} else {
			LogUtil.LogOut(2, LOGTAG,
					"registerAppAppHanlder mAppPktHanlderMap is null.");
		}

	}

	/**
	 * 暂放
	 * @param appId
	 */
	public void unRegisterAppAppHanlder(String appId) {
		if (appId != null && appId.length() > 0) {
			LogUtil.LogOut(3, LOGTAG, "unRegisterAppAppHanlder Enter... AppId="
					+ appId);

			if (mAppPktHanlderMap != null
					&& mAppPktHanlderMap.containsKey(appId)) {
				mAppPktHanlderMap.remove(appId);

				LogUtil.LogOut(3, LOGTAG, "unRegisterAppAppHanlder is done.");
			} else {
				LogUtil.LogOut(2, LOGTAG,
						"unRegisterAppAppHanlder AppId is not exist.");
			}
		} else {
			LogUtil.LogOut(2, LOGTAG,
					"unRegisterAppAppHanlder params are invalid. AppId="
							+ appId);
		}
	}

	public void sendPacketUplink(String channel, String biz, String appData) {
		if (mService != null) {

			LogUtil.LogOut(3, LOGTAG, "sendPacketUplink is called. channel="
					+ channel + ", biz=" + biz + ", appData=" + appData);

			JSONObject bizData = new JSONObject();
			try {
				if (channel.equals(LinkSyncConstants.LINK_CHANNEL_PUSH)) {
					bizData.put(Constants.MSG_APPID, biz);

					JSONObject appJson = new JSONObject(appData);
					bizData.put(Constants.MSG_PAYLOAD, appJson);

					mService.sendPacketUplink(
							LinkSyncConstants.LINK_CHANNEL_PUSH,
							bizData.toString());
				} else if (channel.equals(LinkSyncConstants.LINK_CHANNEL_SYNC)) {
					bizData.put(LinkSyncConstants.LINK_SYNC_DATA_BIZ, biz);

					JSONObject appJson = new JSONObject(appData);
//					JSONArray appJson =  new JSONArray(appData);
					bizData.put(LinkSyncConstants.LINK_SYNC_DATA_MD, appJson);

					mService.sendPacketUplink(
							LinkSyncConstants.LINK_CHANNEL_SYNC,
							bizData.toString());
				}

			} catch (JSONException je) {
				LogUtil.LogOut(2, LOGTAG,
						"sendPacketUplink get bizData failed.");

				je.printStackTrace();

				return;

			} catch (RemoteException e) {
				LogUtil.LogOut(2, LOGTAG, "sendPacketUplink failed.");

				e.printStackTrace();
			}
		}
	}


	/**
	 * 发送数组包
	 * @param channel
	 * @param biz
	 * @param list
	 */
	public void sendArrayPacketUplink(String channel, String biz, List<String> list) {
		if (mService != null) {

			LogUtil.LogOut(3, LOGTAG, "发送数组包");

			JSONObject bizData = new JSONObject();
			try {

					bizData.put(LinkSyncConstants.LINK_SYNC_DATA_BIZ, biz);

					JSONArray appJson =  new JSONArray();
					for (String str : list){
						JSONObject jsonObject = new JSONObject(str);
						appJson.put(jsonObject);
					}
					bizData.put(LinkSyncConstants.LINK_SYNC_DATA_MD, appJson);

					mService.sendPacketUplink(
							LinkSyncConstants.LINK_CHANNEL_SYNC,
							bizData.toString());

			} catch (JSONException je) {
				LogUtil.LogOut(2, LOGTAG,
						"sendPacketUplink get bizData failed.");

				je.printStackTrace();

				return;

			} catch (RemoteException e) {
				LogUtil.LogOut(2, LOGTAG, "sendPacketUplink failed.");

				e.printStackTrace();
			}
		}
	}

	public void setLinkAddr(String host, int port, String sslFlag) {
		if (mService != null) {
			try {
				LogUtil.LogOut(3, LOGTAG, "setLinkAddr is called. host=" + host
						+ ", port=" + port + ", sslFlag=" + sslFlag);

				mService.setLinkAddr(host, port, sslFlag);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

	}

	public void setDebugMode(boolean logFlag) {
		LogUtil.CONFIGURE_ENABLE = logFlag;

		LogUtil.refreshDebugMode();
	}

	public void registerCommonFunc(PacketHanlder packetHandler) {
		mComPktHanlder = packetHandler;
		this.setPacketListener(mPacketNotifer);

		LogUtil.LogOut(3, LOGTAG, "registerCommonFunc set packetHandler done.");
	}

	public void unRegisterCommonFunc() {
		mComPktHanlder = null;
	}
}
