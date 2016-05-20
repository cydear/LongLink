package com.xianglin.fellowvillager.app.longlink.longlink.service;

import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;
import com.xianglin.fellowvillager.app.longlink.longlink.util.Constants;
import com.xianglin.fellowvillager.app.longlink.longlink.util.LogUtil;


public class ReconnCtrl {
	private static final String LOGTAG = ConfigUtils.TAG;

	// 通道重连等待时间
	private static final int TIME_RECONN_INIT = 5; // 单位：秒
	private static final int COUNT_RECONN_MAXVALUE = 30; // 单位：秒

	private static int mCtrlCount = 0;

	private static int waiting = TIME_RECONN_INIT; // 单位：秒
	
	private static String conAction = Constants.CONNECT_ACTION_ACTIVE;

	/**
	 * 获取等待时间
	 * @return
	 */
	public static int getWaitingTime() {
		mCtrlCount = mCtrlCount + 1;
		if (mCtrlCount >= COUNT_RECONN_MAXVALUE) { // 重连次数最大上限
			waiting = -1;
		}

		LogUtil.LogOut(3, LOGTAG, "waiting seconds=" + waiting + ", mCtrlCount="+mCtrlCount);
		return waiting;
	}

	/**
	 * 通道建立且初始化成功后复位连接等待时间
	 */
	public static void resetWaitingTime() {
		mCtrlCount = -1;
		waiting = TIME_RECONN_INIT;
	}

	public static void setConAction(String action) {
		if (action == null) {
			action = "";
		}
		conAction = action;
	}
	
	public static String getConAction() {
		return conAction;
	}
}
