package com.xianglin.fellowvillager.app.longlink.longlink.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;

import com.xianglin.fellowvillager.app.ILongLinkPacketNotifer;
import com.xianglin.fellowvillager.app.ILongLinkService;
import com.xianglin.fellowvillager.app.longlink.longlink.sync.LinkSyncConstants;
import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;
import com.xianglin.fellowvillager.app.longlink.longlink.util.LogUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;



/**
 * Service that continues to run in background. This should be registered as
 * service in AndroidManifest.xml.
 */
public class LongLinkService extends Service {

	private static final String LOGTAG = ConfigUtils.TAG;

	private ExecutorService executorService;
	private TaskSubmitter taskSubmitter;
	private TaskTracker taskTracker;

	public static ConnManager mConnManager = null;
	private BroadcastReceiver mActionReceiver;
	private BroadcastReceiver connectReceiver;

	public LongLinkService() {
		executorService = Executors.newSingleThreadExecutor();
		taskSubmitter = new TaskSubmitter();
		taskTracker = new TaskTracker();

		//实例 网络改变的
		mActionReceiver = new LongLinkActionReceiver(this);
		connectReceiver = new ConnectReceiver();
	}
	
	private IBinder binder = new LongLinkBinder();
	@Override
	public IBinder onBind(Intent intent) {
		LogUtil.LogOut(4, LOGTAG, "onBind()...");
		return binder;
		
	}


	@Override
	public void onCreate() {
		super.onCreate();
		LogUtil.LogOut(3, LOGTAG, "onCreate()...");
		LogUtil.LogOut(3, LOGTAG, ".....服务已开启");

		LongLinkAppInfo.createInstance();
		mConnManager = new ConnManager(this);
		LogUtil.LogOut(5, LOGTAG, "onCreate=" + mConnManager.hashCode());

		registerActionReceiver();

		if (executorService.isShutdown()) {
			executorService = Executors.newSingleThreadExecutor();
		}

		ReconnCtrl.resetWaitingTime();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtil.LogOut(5, LOGTAG, "onStartCommand Received start id " + startId
				+ ", intent: " + intent);

		return Service.START_NOT_STICKY;
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public TaskSubmitter getTaskSubmitter() {
		return taskSubmitter;
	}

	public TaskTracker getTaskTracker() {
		return taskTracker;
	}

	public ConnManager getConnManager() {
		return mConnManager;
	}

	private void registerActionReceiver() {
//		IntentFilter filter = new IntentFilter();
//		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
//		registerReceiver(mActionReceiver, filter);
		IntentFilter filterConnect = new IntentFilter();
		filterConnect.addAction(ConnectReceiver.CONNECT_ACTION);
		registerReceiver(connectReceiver,filterConnect);
	}

	private void unregisterActionReceiver() {
		try {
		//	unregisterReceiver(mActionReceiver);
			unregisterReceiver(connectReceiver);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

		mActionReceiver = null;
	}

	/**
	 * Class for summiting a new runnable task.
	 */
	public class TaskSubmitter {
		@SuppressWarnings("rawtypes")
		public Future submit(Runnable task) {
			Future result = null;

			if (!getExecutorService().isTerminated()
					&& !getExecutorService().isShutdown() && task != null) {
				result = getExecutorService().submit(task);
				LogUtil.LogOut(5, LOGTAG, "Future result is "
						+ result.getClass().getName().toString());
			}
			return result;
		}
	}

	/**
	 * Class for monitoring the running task count.
	 */
	public class TaskTracker {
		public int count;

		public TaskTracker() {
			this.count = 0;
		}

		public void increase() {
			synchronized (getTaskTracker()) {
				getTaskTracker().count++;
				LogUtil.LogOut(4, LOGTAG, "Incremented task count to " + count);
			}
		}

		public void decrease() {
			synchronized (getTaskTracker()) {
				getTaskTracker().count--;
				LogUtil.LogOut(4, LOGTAG, "Decremented task count to " + count);
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		LogUtil.LogOut(4, LOGTAG, "onDestroy()...");

		mConnManager.disconnect();

		unregisterActionReceiver();

		LogUtil.LogOut(5, LOGTAG,
				"onDestroy() executorService will be shutdown!");
		executorService.shutdown();
	}

	
	@Override
	public void onRebind(Intent intent) {
		LogUtil.LogOut(4, LOGTAG, "onRebind()...");
	}

	@Override
	public boolean onUnbind(Intent intent) {
		LogUtil.LogOut(4, LOGTAG, "onUnbind()...");
		return true;
	}

	/**
	 * 长连接 管理
	 */
	private class LongLinkBinder extends ILongLinkService.Stub {

		@Override
		public void reConnect() throws RemoteException {
			ReconnCtrl.resetWaitingTime();
			mConnManager.setForceStopped(false);

			mConnManager.startReconnectionThread();
		}

		@Override
		public void disConnect() throws RemoteException {
			mConnManager.setForceStopped(true);
			
			mConnManager.submitCloseNoticeTask();
			mConnManager.disconnect();
		}

		@Override
		public boolean isConnected() throws RemoteException {
			return mConnManager.isConnected();
		}

		/**
		 * 设置当前app 的用户信息
		 * 1:用户ID
		 * 2:通道建立且初始化成功后复位连接等待时间
		 * 3:启动重连接的线程
		 * 1:userID  XLID
		 * 2:deviceID extToken
		 * 3:messageKey longintime
		 */
		@Override
		public void setAppUserInfo(String userId, String deviceId,String extToken,String loginTime) throws RemoteException {
			LongLinkAppInfo appInfo = LongLinkAppInfo.getInstance();
			appInfo.setUserId(userId);
			appInfo.setExtToken(extToken);
			appInfo.setLoginTime(loginTime);
			appInfo.setmDeviceId(deviceId);
			LogUtil.LogOut(3, LOGTAG, "set app user info = userID ="+userId+"\n"+
					                                    "deviceID ="+deviceId+"\n");
			mConnManager.setXlId(userId);// 相邻ID
			mConnManager.setDeviceId(deviceId);// 设备ID
			mConnManager.setUsername(userId);//乡邻ID
			mConnManager.setForceStopped(false);
			ReconnCtrl.resetWaitingTime();// 设置 重连等待的时间
			mConnManager.startReconnectionThread();// 设置连接
		}

		@Override
		public void setPacketNotifer(ILongLinkPacketNotifer notifer)
				throws RemoteException {
			mConnManager.setPacketNotifier(notifer);
		}

		/**
		 * 设置连接地址
		 * 
		 */
		@Override
		public void setLinkAddr(String host, int port, String sslFlag)
				throws RemoteException {
			LongLinkAddr addr = new LongLinkAddr(host, port, sslFlag);
			mConnManager.setLinkAddr(addr);
		}

		@Override
		public void sendPacketUplink(String channel, String data)
				throws RemoteException {
			LogUtil.LogOut(3, LOGTAG, "sendPacketUplink() channel=" + channel
					+ ", data=" + data.toString());
			
			if (channel != null && channel.equals(LinkSyncConstants.LINK_CHANNEL_SYNC)) {
				mConnManager.submitLinkSyncTask(data);
			} 
			else if (channel != null && channel.equals(LinkSyncConstants.LINK_CHANNEL_PUSH)) {
				mConnManager.submitUplinkDataTask(data);
			} else {
				LogUtil.LogOut(2, LOGTAG, "sendPacketUplink(): warning invalid channel.");
			}

		}

	}

	public class ConnectReceiver extends  BroadcastReceiver{

		public static final String CONNECT_ACTION= "android.conn.CONNECT_ACTION";
		@Override
		public void onReceive(Context context, Intent intent) {
			if (getConnManager().isConnected()) {
				// 原有连接有效，不做新的处理 发送一个数据包
				getConnManager().submitHeartBeatTask();
				LogUtil.LogOut(1, LOGTAG, "原有连接有效，不做新的处理");
			} else {
//					// 虽然之前和服务端有过数据交互，但是现在没有有效连接
				LogUtil.LogOut(1, LOGTAG, "虽然之前和服务端有过数据交互，但是现在没有有效连接"
						+getConnManager().getUsername());
				if (getConnManager().getUsername() != null
						&& getConnManager()
						.getUsername().length() > 0 && !getConnManager().isForceStopped()) {
					// 立即建链  链接初始化
					LogUtil.LogOut(1, LOGTAG, "立即建链－－－－－－－－－");
					getConnManager().connect();//建立链接
					LogUtil.LogOut(1, LOGTAG, "立即建链成功 初始化长链接－－－－－－－－－");
				} else {
					LogUtil.LogOut(3, LOGTAG, "onReceive() no valid user.");
				}
			}
		}
	}
}
