package com.xianglin.fellowvillager.app.longlink.longlink.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.xianglin.fellowvillager.app.ILongLinkPacketNotifer;
import com.xianglin.fellowvillager.app.longlink.longlink.sync.LinkSyncConstants;
import com.xianglin.fellowvillager.app.longlink.longlink.sync.LinkSyncInfo;
import com.xianglin.fellowvillager.app.longlink.longlink.sync.LinkSyncPacket;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.connection.ConnectionConfiguration;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.connection.ConnectionConfiguration.SecurityMode;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.connection.PushConnection;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.connection.PushCtrlConfiguration;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.connection.proxy.ProxyInfo;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.connectionListener.ConnectListener;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.connectionListener.ConnectListenerImpl;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.connectionListener.ConnectionListener;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.connectionListener.PushConnectionListenerImpl;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.Packet;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketConstants;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketFactory;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packetListener.ClosePacketListenerImpl;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packetListener.HeartBeatPacketListenerImpl;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packetListener.LinkSyncPacketListenerImpl;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packetListener.NotificationPacketListenerImpl;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packetListener.PacketListener;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packetListener.ReconnectPacketListenerImpl;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packetListener.RegisterPacketListenerImpl;
import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;
import com.xianglin.fellowvillager.app.longlink.longlink.util.Constants;
import com.xianglin.fellowvillager.app.longlink.longlink.util.LogUtil;
import com.xianglin.mobile.common.logging.LogCatLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;



/**
 * This class is to manage the Push connection between client and server.
 */
public class ConnManager {

	private static final String LOGTAG = ConfigUtils.TAG;

	private static final String APN_WIFI = "wifi";

	private Context context;

	private LongLinkService.TaskSubmitter taskSubmitter;
	private LongLinkService.TaskTracker taskTracker;

	private String sslUsed;
	private String pushHost;
	private int pushPort;
	private String proxyHost;
	private int proxyPort;

	private int protocolVersion = PacketConstants.PACKET_VERSION_2;

	private PushConnection connection;

	private String mUsername = "";
	private String xlId = "";
	private String deviceId = "";

	private boolean isRegistered;
	private boolean isForceStopped;
	private Object mLock;
	private LongLinkAddr mLinkAddr;
	private ILongLinkPacketNotifer mPacketNotifer;

	private ConnectionListener connectionListener;
	private RegisterPacketListenerImpl registerListener;
	private ClosePacketListenerImpl closeListener;
	private HeartBeatPacketListenerImpl heartBeatListener;
	private NotificationPacketListenerImpl notificationPacketListener;
	private ReconnectPacketListenerImpl reconnectListener;
	private LinkSyncPacketListenerImpl syncPacketListener;

	private List<Runnable> taskList;
	private boolean running = false;
	boolean isRqe = true;
	private Future<?> futureTask;

	Timer mHeartTimer = null;
	Timer mReconnectedTimer = null;
	TimerTask mHeartBeatTask;//HeartBeatTimerTask
	ReconnTimerTask mReconnectionTask;

	public ConnManager(LongLinkService notificationService) {
		context = notificationService;
		taskSubmitter = notificationService.getTaskSubmitter();
		taskTracker = notificationService.getTaskTracker();

		mUsername = "";
		isRegistered = false;
		isForceStopped = false;
		mLock = new Object();

		connectionListener = new PushConnectionListenerImpl(this);
		registerListener = new RegisterPacketListenerImpl(this);
		closeListener = new ClosePacketListenerImpl(this);
		heartBeatListener = new HeartBeatPacketListenerImpl(this);
		notificationPacketListener = new NotificationPacketListenerImpl(this);
		reconnectListener = new ReconnectPacketListenerImpl(this);
		syncPacketListener = new LinkSyncPacketListenerImpl(this);

		taskList = new ArrayList<Runnable>();

		LogUtil.LogOut(5, LOGTAG, "ConnManager=" + this.hashCode());
	}

	public Context getContext() {
		return context;
	}

	public void connect() {
		LogUtil.LogOut(3, LOGTAG, "connect()...");

		// this.stopReconnAlarmTimer();
		addTask(new ConnectTask(new ConnectListenerImpl(this)));
	}

	public void disconnect() {
		LogUtil.LogOut(3, LOGTAG, "disconnect()...");

		stopReconnTimer();
		stopHeartTimer();

		this.setRegistered(false);

		terminatePersistentConnection();
	}

	public void terminatePersistentConnection() {
		LogUtil.LogOut(4, LOGTAG, "terminatePersistentConnection()...");
		// 添加断开连接任务
		addTask(new Runnable() {
			final ConnManager connManager = ConnManager.this;

			@Override
			public void run() {
				LogUtil.LogOut(5, LOGTAG,
						"terminatePersistentConnection()... called. connection:"
								+ connection.hashCode());
				if (isConnected()) {
					LogUtil.LogOut(4, LOGTAG,
							"terminatePersistentConnection()... run()");

					// 取消之前注册所有的数据listener
					connManager.getConnection().removePacketListener(
							connManager.getRegisterPacketListener());
					connManager.getConnection().removePacketListener(
							connManager.getHeartBeatPacketListener());
					connManager.getConnection().removePacketListener(
							connManager.getHeartBeatPacketListener());
					connManager.getConnection().removePacketListener(
							connManager.getReconnectPacketListener());

					getConnection().disconnect();
					LogUtil.LogOut(4, LOGTAG,
							"terminatePersistentConnection()...Done!");
				}
			}
		});
	}

	public PushConnection getConnection() {
		if (connection != null) {
			LogUtil.LogOut(5, LOGTAG, "getConnection()... called. connection:"
					+ connection.hashCode());
		}
		return connection;
	}

	public void setConnection(PushConnection connection) {
		LogUtil.LogOut(5, LOGTAG, "setConnection()... called. connection:"
				+ connection.hashCode());
		this.connection = connection;
	}

	public String getUsername() {
		return mUsername;
	}

	public void setUsername(String username) {
		this.mUsername = username;
	}

	public String getXlId() {
		return xlId;
	}

	public ConnManager setXlId(String xlId) {
		this.xlId = xlId;
		return this;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public ConnManager setDeviceId(String deviceId) {
		this.deviceId = deviceId;
		return this;
	}


	public ILongLinkPacketNotifer getPacketNotifier() {
		if (mPacketNotifer != null){
			LogCatLog.d(LOGTAG,"获取包通知成功，准备回调");
			return mPacketNotifer;
		}else{
			LogCatLog.d(LOGTAG,"获取包通知失败，mPacketNotifer 为null");
			return null;
		}

	}

	public void setPacketNotifier(ILongLinkPacketNotifer notifier) {
		this.mPacketNotifer = notifier;
	}

	public void setLinkAddr(LongLinkAddr addr) {
		this.mLinkAddr = addr;
	}

	public int getProtoVer() {
		return this.protocolVersion;
	}

	private void loadLinkConfig() {
		if (LogUtil.CONFIGURE_ENABLE) {
			// configured address
			pushHost = this.mLinkAddr.getHost();
			pushPort = this.mLinkAddr.getPort();

			sslUsed = this.mLinkAddr.getSSLFlag();
		} else {
			// default address
			pushHost = Constants.NOTIFICATION_DEAFULT_HOST;
			pushPort = Constants.NOTIFICATION_DEAFULT_PORT;

			sslUsed = Constants.NOTIFICATION_DEAFULT_SSL;
		}

		protocolVersion = Constants.NOTIFICATION_DEAFULT_PROTOCAL_VER;

		LogUtil.LogOut(4, LOGTAG, "loadLinkConfig() Host:" + pushHost
				+ ", Port:" + pushPort + ", sslUsed:" + sslUsed
				+ ", protocolVersion:" + protocolVersion);
	}

	@SuppressWarnings("deprecation")
	private void checkConnectType() {
		try {
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (cm != null) {
				NetworkInfo info = cm.getActiveNetworkInfo();
				if (info != null && info.isAvailable()) {
					String typeName = info.getTypeName(); // cmwap/cmnet/wifi/uniwap/uninet/WIFI

					if (typeName.equalsIgnoreCase("MOBILE")) { // 如果是使用的运营商网络
						typeName = info.getExtraInfo();
						LogUtil.LogOut(4, LOGTAG,
								"ActiveNetworkInfo() typeName:" + typeName);
						// 3gnet/3gwap/uninet/uniwap/cmnet/cmwap/ctnet/ctwap

						// 获取默认代理主机ip
						proxyHost = android.net.Proxy.getDefaultHost();
						// 获取端口
						proxyPort = android.net.Proxy.getDefaultPort();
					} else {
						proxyHost = null;
						proxyPort = 0;
					}
				} else {
					proxyHost = null;
					proxyPort = 0;
				}
			}
		} catch (Exception e) {
			proxyHost = null;
			proxyPort = 0;
		}

		LogUtil.LogOut(4, LOGTAG, "checkConnectType() proxyHost:" + proxyHost
				+ ", proxyPort=" + proxyPort);
	}

	public ConnectionListener getConnectionListener() {
		return connectionListener;
	}

	public PacketListener getRegisterPacketListener() {
		return registerListener;
	}
	
	public PacketListener getClosePacketListener() {
		return closeListener;
	}

	public PacketListener getHeartBeatPacketListener() {
		return heartBeatListener;
	}

	public PacketListener getNotificationPacketListener() {
		return notificationPacketListener;
	}

	public PacketListener getReconnectPacketListener() {
		return reconnectListener;
	}

	public PacketListener getLinkSyncPacketListener() {
		return syncPacketListener;
	}

	/**
	 * 开始连接
	 * 
	 */
	public void startReconnectionThread() {
		LogUtil.LogOut(3, LOGTAG, "startReconnectionThread() in ...");

		addTask(new ReconnectTask());
	}

	public void resetWaitingTime() {
		ReconnCtrl.resetWaitingTime();
	}

	public List<Runnable> getTaskList() {
		return taskList;
	}

	public Future<?> getFutureTask() {
		return futureTask;
	}

	public void runTask() {
		LogUtil.LogOut(5, LOGTAG, "runTask()...");

		synchronized (taskList) {
			running = false;
			futureTask = null;

			if (!taskList.isEmpty()) {
				Runnable runnable = (Runnable) taskList.get(0);
				taskList.remove(0);
				running = true;

				LogUtil.LogOut(4, LOGTAG, "runTask() runnable is "
						+ runnable.getClass().getName().toString());
				futureTask = taskSubmitter.submit(runnable);
				if (futureTask == null) {
					taskTracker.decrease();
				}
			} else {
				LogUtil.LogOut(5, LOGTAG, "runTask(),taskList is empty");
			}
		}
		taskTracker.decrease();
		LogUtil.LogOut(5, LOGTAG, "runTask()...done");
	}

	public boolean isConnected() {
		boolean ret = false;

		if (connection != null) {
			LogUtil.LogOut(5, LOGTAG,
					"isConnected() connection=" + connection.hashCode());
		}

		if (connection != null && connection.isConnected()) {
			ret = true;
		}

		LogUtil.LogOut(5, LOGTAG, "isConnected() ret=" + ret);

		return ret;
	}

	public boolean isRegistered() {
		LogUtil.LogOut(5, LOGTAG, "isRegistered=" + isRegistered);
		return isRegistered;
	}

	public void setRegistered(boolean flag) {
		isRegistered = flag;
		if (flag) {
			ReconnCtrl.setConAction("");
		}
	}

	public boolean isForceStopped() {
		boolean ret = false;

		synchronized (mLock) {
			ret = isForceStopped;
		}

		LogUtil.LogOut(5, LOGTAG, "isForceStopped=" + ret);
		return ret;
	}

	public void setForceStopped(boolean flag) {
		synchronized (mLock) {
			isForceStopped = flag;
		}
	}

	public void submitRegisterTask() {
		LogUtil.LogOut(4, LOGTAG, "submitRegisterTask()...");
		addTask(new RegisterTask());
	}
	
	public void submitCloseNoticeTask() {
		LogUtil.LogOut(4, LOGTAG, "submitCloseNoticeTask()...");
		addTask(new CloseConnTask());
	}

	public void submitHeartBeatTask() {
		LogUtil.LogOut(4, LOGTAG, "submitHeartBeatTask()...");
		addTask(new HeartBeatTask());
	}

	public void submitUplinkDataTask(String data) {
		LogUtil.LogOut(4, LOGTAG, "submitUplinkDataTask()...");
		addTask(new UplinkDataTask(data));
	}

	public void submitLinkSyncTask(String data) {
		LogUtil.LogOut(4, LOGTAG, "submitLinkSyncTask()...");
		addTask(new LinkSyncTask(data));
	}

	private void addTask(Runnable runnable) {
		LogUtil.LogOut(4, LOGTAG, "addTask(runnable)...");
		taskTracker.increase();

		synchronized (taskList) {
			LogUtil.LogOut(4, LOGTAG, "addTask taskList=" + taskList.size());

			if (taskList.isEmpty() && !running) {
				running = true;

				LogUtil.LogOut(3, LOGTAG, "addTask() runnable is "
						+ runnable.getClass().getName().toString());
				LogUtil.LogOut(5, LOGTAG, "addTask(runnable)...taskSubmitter:"
						+ taskSubmitter.hashCode());
				futureTask = taskSubmitter.submit(runnable);
				if (futureTask == null) {
					taskTracker.decrease();
				}
			} else {
				taskList.add(runnable);
				runTask();
			}
		}
		LogUtil.LogOut(4, LOGTAG, "addTask(runnable)... done");
	}

	/**
	 * A runnable task to connect the server.
	 */
	private class ConnectTask implements Runnable {

		final ConnManager connManager;

		private ConnectListener taskListener;

		private ConnectTask(ConnectListenerImpl connectInitListener) {
			this.connManager = ConnManager.this;
			taskListener = connectInitListener;
		}

		public void run() {
			LogUtil.LogOut(3, LOGTAG, "===== ConnectTask.run()=====");

			LogUtil.LogOut(5, LOGTAG, "===== ConnectTask connManager=" + connManager.hashCode());

			// 设置连接状态
			if (!connManager.isConnected()) {
				// 加载push连接的配置参数，需要每次连接前重新加载
				loadLinkConfig();

				// 获取当前的代理ip和port
				checkConnectType();

				ProxyInfo proxyInfo;
				if (proxyHost != null && proxyHost.length() > 0
						&& proxyPort != 0) {
					proxyInfo = new ProxyInfo(ProxyInfo.ProxyType.SOCKS,
							proxyHost, proxyPort);
				} else {
					proxyInfo = ProxyInfo.forNoProxy();
				}

				// Create the configuration for this new connection
				ConnectionConfiguration connConfig = new ConnectionConfiguration(
						pushHost, pushPort, proxyInfo);

				// 根据属性配置决定是否需要使用SSL
				if (sslUsed != null && sslUsed.equals("1")) {
					connConfig.setSecurityMode(SecurityMode.required);
				} else {
					connConfig.setSecurityMode(SecurityMode.disabled);
				}

				connConfig.setCompressionEnabled(false);

				PushConnection connection = new PushConnection(connConfig);
				connManager.setConnection(connection);
				connManager.getConnection().setMsgVersion(protocolVersion);

				// Connect to the server 连接服务器
				connManager.getConnection().connect(taskListener,connManager);
			} else {
				// 已经处于连接状态
				LogUtil.LogOut(3, LOGTAG, "The connManager is connected now.");
			}
		}
	}

	/**
	 * A runnable task to register a new user onto the server.
	 */
	private class RegisterTask implements Runnable {

		final ConnManager pushManager;

		private RegisterTask() {
			pushManager = ConnManager.this;
		}

		public void run() {
			LogUtil.LogOut(3, LOGTAG, "===== RegisterTask.run()=====");

			if (!pushManager.isRegistered()) {
				Packet registration;
				try {
					registration = PacketFactory.getPacket(protocolVersion);
					registration.setMsgId(PacketConstants.MSG_PUSH_INITIALIZE);
					registration
							.setMsgType(PacketConstants.MSG_PUSH_TYPE_REQUEST);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}

				JSONObject registerReq = new JSONObject();
				if (registerReq != null) {

					try {
						LongLinkAppInfo appInfo = LongLinkAppInfo.getInstance();

						registerReq.put(Constants.CONNECT_TOKEN_USER,
								appInfo.getUserId());
						
						registerReq.put(Constants.CONNECT_TOKEN_LOGINTIME,
								appInfo.getLoginTime());
						registerReq.put(Constants.CONNECT_TYPE_OS,
								Constants.CONNECT_OS_NAME);
						registerReq.put(Constants.CONNECT_TYPE_NETWORK,
								pushManager.getApnInUse());
						registerReq.put(Constants.LINK_VERSION_KEY,
								Constants.LINK_VERSION_VALUE);  // 此版本支持数据GZIP压缩
						
						// 根据ReconnCtrl中的变量，初始化为活跃状态
						String conAction = ReconnCtrl.getConAction();
						if (conAction != null && !conAction.equals("")) {
							registerReq.put(Constants.CONNECT_ACTION, conAction);
						}
						
						LogUtil.LogOut(3, LOGTAG,
								"RegisterTask() registration will be sent! data:"
										+ registerReq.toString());
						
						registerReq.put(Constants.CONNECT_TOKEN_SESSION,
								appInfo.getExtToken());
						registerReq.put(Constants.CONNECT_TOKEN_USERID,appInfo.getUserId());
						registerReq.put(Constants.CONNECT_TOKEN_DEVICESID,appInfo.getmDeviceId());
						registration.setData(registerReq.toString());

					} catch (JSONException e) {
						e.printStackTrace();
						return;
					}

					// 注册所有的数据listener
					connection.addPacketListener(
							pushManager.getRegisterPacketListener(), null);
					connection.addPacketListener(
							pushManager.getClosePacketListener(), null);
					connection.addPacketListener(
							pushManager.getHeartBeatPacketListener(), null);
					connection.addPacketListener(
							pushManager.getNotificationPacketListener(), null);
					connection.addPacketListener(
							pushManager.getReconnectPacketListener(), null);
					connection.addPacketListener(
							pushManager.getLinkSyncPacketListener(), null);

					LogUtil.LogOut(4, LOGTAG,
							"RegisterTask() registration will be sent! length="
									+ registration.getDataLength());
					pushManager.getConnection().sendPacket(registration);
				}
			} else {
				LogUtil.LogOut(3, LOGTAG, "Account registered already");
			}
		}
	}
	
	/**
	 * A runnable task to send close packet to the server.
	 */
	private class CloseConnTask implements Runnable {

		final ConnManager pushManager;

		private CloseConnTask() {
			pushManager = ConnManager.this;
		}

		public void run() {
			LogUtil.LogOut(4, LOGTAG, "===== CloseConnTask() Runnable=====");

			if (pushManager.isRegistered()) {
				Packet closePacket;
				try {
					closePacket = PacketFactory.getPacket(protocolVersion);
					closePacket.setMsgId(PacketConstants.MSG_PUSH_CLOSE);
					closePacket.setMsgType(PacketConstants.MSG_PUSH_TYPE_REQUEST);
					closePacket.setData("");
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				LogUtil.LogOut(4, LOGTAG,
						"CloseConnTask() heartBeat will be sent! length="
								+ closePacket.getDataLength());

				pushManager.getConnection().sendPacket(closePacket);
			} else {
				LogUtil.LogOut(3, LOGTAG,
						"Account registered has not been done.");

			}
		}
	}

	/**
	 * A runnable task to send heartbeat to the server.
	 */
	private class HeartBeatTask implements Runnable {

		final ConnManager pushManager;

		private HeartBeatTask() {
			pushManager = ConnManager.this;
		}

		public void run() {
			LogUtil.LogOut(4, LOGTAG, "===== HeartBeatTask() Runnable=====");

			if (pushManager.isRegistered()) {
				Packet heartBeat;
				try {
					heartBeat = PacketFactory.getPacket(protocolVersion);
					heartBeat.setMsgId(PacketConstants.MSG_PUSH_KEEPLIVE);
					heartBeat.setMsgType(PacketConstants.MSG_PUSH_TYPE_REQUEST);
					heartBeat.setData("");
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				LogUtil.LogOut(4, LOGTAG,
						"HeartBeatTask() heartBeat will be sent! length="
								+ heartBeat.getDataLength());

				pushManager.getConnection().sendPacket(heartBeat);
			} else {
				LogUtil.LogOut(3, LOGTAG,
						"Account registered has not been done.");
			}
		}
	}

	/**
	 * A runnable task to re-connect to the server.
	 */
	private class ReconnectTask implements Runnable {

		final ConnManager pushManager;

		private ReconnectTask() {
			pushManager = ConnManager.this;
		}

		public void run() {
			LogUtil.LogOut(4, LOGTAG, "===== ReconnectTask() Runnable=====");

			synchronized (ConnManager.this) {
				// 如果有之前的连接存在，则重置连接
				if (pushManager.getConnection() != null) {
					getConnection().resetConnection();
				}
			}
			//停止重新连接的时间
			pushManager.stopReconnTimer();
			//停止心跳时间
			pushManager.stopHeartTimer();
			
			pushManager.setRegistered(false);

			if (pushManager.isForceStopped()) {
				LogUtil.LogOut(3, LOGTAG,
						"ReconnectTask() return because of flag(forcestopped).");
				return;
			}
			// 验证用户name 验证成功 进行重连 ？
			//boolean hasUser = ;//为啥老是 false 呢？TODO james 12-28  修改 测试OK
			if (pushManager.getUsername() != null &&  pushManager.getUsername().length() > 0) {
				LogUtil.LogOut(4, LOGTAG, "ReconnectTask() getUsername="+ pushManager.getUsername());
				try {
					Thread.sleep(100);

					pushManager.startReconnTimer();// 启动重连
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				LogUtil.LogOut(3, LOGTAG,"ReconnectTask() there isn't valind user and give up connecting.");
			}

		}
	}

	/**
	 * A runnable task to send heartbeat to the server.
	 * 发送初始化包
	 */
	private class UplinkDataTask implements Runnable {

		final ConnManager pushManager;
		String mAppData;

		private UplinkDataTask(String data) {
			pushManager = ConnManager.this;
			mAppData = data;
		}

		public void run() {
			LogUtil.LogOut(4, LOGTAG, "===== UplinkDataTask() Runnable=====");

			if (pushManager.isRegistered()) {
				Packet bizPacket;
				try {
					bizPacket = PacketFactory.getPacket(protocolVersion);
					bizPacket.setMsgId(PacketConstants.MSG_PUSH_BIZDATA);
					bizPacket.setMsgType(PacketConstants.MSG_PUSH_TYPE_REQUEST);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}

				JSONObject updataReq = new JSONObject();
				if (updataReq != null) {
					try {
						updataReq.put(Constants.MSG_DATA, mAppData);

						updataReq.put(Constants.MSG_TIMESTAMP,
								System.currentTimeMillis());

						LongLinkAppInfo appInfo = LongLinkAppInfo.getInstance();

						updataReq.put(Constants.CONNECT_TOKEN_USER,
								appInfo.getUserId());

						bizPacket.setData(updataReq.toString());

						LogUtil.LogOut(4, LOGTAG,
								"UplinkDataTask() bizPacket will be sent! bizPacket="
										+ bizPacket.getData().toString());
					} catch (JSONException e) {
						e.printStackTrace();
						return;
					}

					pushManager.getConnection().sendPacket(bizPacket);
				}

			} else {
				LogUtil.LogOut(3, LOGTAG,
						"Account registered has not been done.");
			}
		}
	}

	/**
	 * A runnable task to send sync to the server.
	 * //发送数据包
	 */
	private class LinkSyncTask implements Runnable {

		final ConnManager pushManager;
		String mAppData;

		private LinkSyncTask(String data) {
			pushManager = ConnManager.this;
			mAppData = data;
		}

		public void run() {
			LogUtil.LogOut(4, LOGTAG, "===== LinkSyncTask() Runnable=====");
			//网络断掉 再次有消息之后应该重连 监听链接是否成功失败
			if (!pushManager.isConnected()){
				ReconnCtrl.resetWaitingTime();
				startReconnectionThread();// 重新链接
				LogUtil.LogOut(3, LOGTAG, "重新链接");
			}
			if (pushManager.isRegistered()) {
				Packet bizPacket;
				try {
					bizPacket = PacketFactory.getPacket(protocolVersion);

					bizPacket.setMsgType(PacketConstants.MSG_PUSH_TYPE_REQUEST);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}

				JSONObject syncReq = new JSONObject();
				if (syncReq != null) {
					try {
						Object value;

						LinkSyncPacket syncReqPacket = new LinkSyncPacket();
						if (mAppData != null && mAppData.length() > 0) {// 聊天消息
							    bizPacket.setMsgId(PacketConstants.MSG_PUSH_LINKSYNC);
								syncReqPacket
										.setOpCode(LinkSyncConstants.LINK_SYNC_OPCODE_SEND_RQE);
								LogCatLog.d(LOGTAG, "2001");

							JSONArray jsonArray = new JSONArray();
							JSONObject appJson = new JSONObject(mAppData);
							jsonArray.put(appJson);

							value = jsonArray;
						} else {// 心跳数据发送
							bizPacket.setMsgId(PacketConstants.MSG_PUSH_KEEPLIVE);
							syncReqPacket
									.setOpCode(LinkSyncConstants.LINK_SYNC_OPCODE_SYNC_REQ);
							value = null;

						}

						LinkSyncInfo syncInfo = new LinkSyncInfo(
								pushManager.getContext());
						String userId = pushManager.getUsername();
						/**TODO james updateData :2015-12-5:*/
						syncReqPacket.setSyncKey(System.currentTimeMillis());


						syncReq.put(LinkSyncConstants.LINK_SYNC_KEY,
								syncReqPacket.getSyncKey());
						syncReq.put(LinkSyncConstants.LINK_SYNC_OPCODE,
								syncReqPacket.getOpCode());
						syncReq.put(LinkSyncConstants.LINK_SYNC_DATA, value);

						bizPacket.setData(syncReq.toString());

						LogUtil.LogOut(4, LOGTAG,
								"LinkSyncTask() bizPacket will be sent! userId="
										+ userId + ", the len="+bizPacket.getDataLength()
										+ ", syncReq="+ bizPacket.getData().toString());
					} catch (JSONException e) {
						e.printStackTrace();
						return;
					}

					pushManager.getConnection().sendPacket(bizPacket);

					LogUtil.LogOut(4, LOGTAG,
							"LinkSyncTask() bizPacket is sent. The length="
									+ bizPacket.getDataLength());
				}

			} else {

				LogUtil.LogOut(3, LOGTAG,
						"Account registered has not been done.");
			}
		}
	}

	class HeartBeatTimerTask extends TimerTask {

		public void run() {
			LogUtil.LogOut(4, LOGTAG, "===== HeartBeatTask() TimerTask=====");
			ConnManager.this.submitHeartBeatTask();
		}
	}
	/**
	 * 代替 HeartBeatTimerTask。即用sync心跳代替纯心跳</br>
	 * {"sKey":xxxx,"sData":"","sOpCode":1001}
	 * 
	 * @author alex
	 *
	 */
	class HeartBeatSyncTimerTask extends TimerTask {
		public void run() {
			LogUtil.LogOut(4, LOGTAG, "===== HeartBeatSyncTimerTask() TimerTask=====");
			ConnManager.this.submitLinkSyncTask("");
		}
	}

	/**
	 * 每隔 PushCtrlConfiguration.getKeepAliveInterval() * 1000 ms
	 * 提交一次（心跳）请求
	 */
	public void startHeartTimer() {
//		stopHeartTimer();
//
//		mHeartBeatTask = new HeartBeatTimerTask();
//		this.mHeartTimer = new Timer();
//		this.mHeartTimer.schedule(mHeartBeatTask,
//				PushCtrlConfiguration.getKeepAliveInterval() * 1000);
		
		startSyncHeartTimer();
	}
	/**
	 * 用 {"sKey":1,"sData":"","sOpCode":1001} 代替 心跳包
	 * TODO sKey 要改变
	 * 
	 */
	private void startSyncHeartTimer() {
		//new LinkSyncTask(data)
		stopHeartTimer();
		mHeartBeatTask = new HeartBeatSyncTimerTask();
		this.mHeartTimer = new Timer();
		this.mHeartTimer.schedule(mHeartBeatTask,
				PushCtrlConfiguration.getKeepAliveInterval() * 1000);
		
		
	}

	protected void stopHeartTimer() {
		if (null != this.mHeartTimer) {
			this.mHeartTimer.cancel();

			if (mHeartBeatTask != null) {
				mHeartBeatTask.cancel();
				mHeartBeatTask = null;
			}

			this.mHeartTimer = null;
		}
	}

	/**
	 *  重连任务
	 * @author james
	 *
	 */
	class ReconnTimerTask extends TimerTask {

		public void run() {
			LogUtil.LogOut(4, LOGTAG, "===== ReconnTimerTask() TimerTask=====");
			ConnManager.this.connect();
		}
	}

	/**
	 * 启动定时重连
	 * @throws Exception
	 */
	public void startReconnTimer() throws Exception {
		stopReconnTimer();
		
		int waitTime = ReconnCtrl.getWaitingTime();
		if (waitTime >= 0) {
			mReconnectionTask = new ReconnTimerTask();
			mReconnectedTimer = new Timer();
			mReconnectedTimer.schedule(mReconnectionTask, waitTime * 500);
			LogUtil.LogOut(4, LOGTAG,
					"mReconnectionTask is scheduled after time="+ waitTime*500);
		} else {
			// 达到次数控制上限
			LogUtil.LogOut(2, LOGTAG,
					"Warning! waitTime has reached to the max limit.");
		}

	}

	protected void stopReconnTimer() {
		if (null != mReconnectedTimer) {
			mReconnectedTimer.cancel();

			if (mReconnectionTask != null) {
				mReconnectionTask.cancel();
				mReconnectionTask = null;
			}

			mReconnectedTimer = null;
		}
	}

	private String getApnInUse() {
		String apn = "unknown";

		try {
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = cm.getActiveNetworkInfo();

			if (info != null) {
				String typeName = info.getTypeName(); // WIFI/MOBILE

				if (typeName != null
						&& APN_WIFI.equals(typeName.toLowerCase(Locale.US))) {
					apn = "wifi";
				} else {
					String extra = info.getExtraInfo();
					if (extra != null
							&& extra.toLowerCase(Locale.getDefault()).length() > 0) {
						apn = extra;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return apn;
	}

}
