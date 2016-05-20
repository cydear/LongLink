package com.xianglin.fellowvillager.app.longlink.longlink.msg;

import android.content.Context;

import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;
import com.xianglin.fellowvillager.app.longlink.longlink.util.LogUtil;

import java.io.File;


public abstract class MsgRecord {
	private static final String LOGTAG = ConfigUtils.TAG;

	private static String DIR_PUSH_ROOT = "longlink";

	// expected dir:
	// files/link/msg/
	// userid1
	// userid2
	// ...

	private Context mContext;
	protected String mUserId = "";

	public MsgRecord(Context context) {
		mContext = context;
	}

	public abstract void setCurUserId(String userId);

	/**
	 * used to get all of msg list belong to the user
	 */
	public abstract String[] getMsgList();

	/**
	 * used to check whether show and save the msg information
	 */
	public abstract boolean isContainMsg(MsgInfo msgInfo);

	/**
	 * used to manager and save msg information files
	 */
	public abstract void saveMsgRecord(MsgInfo msgInfo);

	public abstract String getMinMsgid();

	public abstract String getMaxMsgid();

	protected String getMsgDir() {
		String strRoot = mContext.getFilesDir().getPath() + "/";
		String strPush = strRoot + DIR_PUSH_ROOT + File.separatorChar; 	// files/longlink/
		LogUtil.LogOut(4, LOGTAG, "getMsgDir() strPush=" + strPush);

		File fileDR = new File(strRoot);
		fileDR.mkdir();

		File fileDP = new File(strPush);
		fileDP.mkdir();

		return strPush;
	}
}
