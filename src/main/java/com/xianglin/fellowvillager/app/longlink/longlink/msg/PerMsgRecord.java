package com.xianglin.fellowvillager.app.longlink.longlink.msg;

import android.content.Context;

import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;
import com.xianglin.fellowvillager.app.longlink.longlink.util.LogUtil;
import com.xianglin.fellowvillager.app.longlink.longlink.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;


public class PerMsgRecord extends MsgRecord {
	private static final String LOGTAG = ConfigUtils.TAG;
	
	private static final int MAX_PER_MSG = 50;
	
	public PerMsgRecord(Context context) {
		super(context);
	}

	@Override
	public void setCurUserId(String userId) {
		mUserId = userId;
	}

	@Override
	public String[] getMsgList() {
		String[] sortPerList = null;

		String perMsgName = getMsgDir() + mUserId; // files/longlink/userId
		LogUtil.LogOut(3, LOGTAG, "getMsgList() perMsgName="+perMsgName);
		
		File file = new File(perMsgName);

		if (file.exists() && file.length() > 0) {
			try {
				// 获取其中的id标示列表
				FileInputStream inputStream = new FileInputStream(file);
				byte[] bs;
				bs = new byte[inputStream.available()];
				inputStream.read(bs);

				String listMsgId = new String(bs, "utf-8");
				LogUtil.LogOut(4, LOGTAG, "getMsgList() listMsgId:" + listMsgId);

				if (listMsgId.trim().length() > 0) {
					sortPerList = StringUtils.strToArray(listMsgId);
					Arrays.sort(sortPerList);
				}
				
				inputStream.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// can't find perMsglist file.
			LogUtil.LogOut(3, LOGTAG, "getMsgList() perMsgName isn't find!");
		}

		return sortPerList;
	}

	@Override
	public boolean isContainMsg(MsgInfo msgInfo) {
		boolean ret = false;

		LogUtil.LogOut(3, LOGTAG,
				"isContainMsg() newPerMsgId=" + msgInfo.getPerMsgId());

		String[] nowMsgList = getMsgList();
		if (nowMsgList != null) {
			ret = StringUtils.isContain(nowMsgList, msgInfo.getPerMsgId());
		}

		return ret;
	}

	private String[] updatePerMsgList(String perMsgId) {
		String[] newPerList = null;
		String[] sortPerList = getMsgList();

		if (sortPerList != null && sortPerList.length > 0) {
			if (sortPerList.length >= MAX_PER_MSG) {
				// replace min msgid with current pub msgid
				sortPerList[0] = perMsgId;
				newPerList = sortPerList;
			} else {
				// still has place to put cur pub msgid
				newPerList = StringUtils.arrayAppend(sortPerList, perMsgId);
			}
		} else {
			// no record before
			newPerList = new String[1];
			newPerList[0] = perMsgId;
		}
		LogUtil.LogOut(3, LOGTAG, "updatePerMsgList() perMsgId=" + perMsgId
				+ ", newPubList:" + newPerList.toString());

		return newPerList;
	}

	@Override
	public void saveMsgRecord(MsgInfo msgInfo) {
		String perMsgId = msgInfo.getPerMsgId();
		LogUtil.LogOut(3, LOGTAG, "saveMsgRecord() perMsgId=" + perMsgId);

		if (perMsgId != null && perMsgId.length() > 0) {
			// 转换为以','分隔的字符串
			String listMsgId = StringUtils
					.arrayToString(updatePerMsgList(perMsgId));
			LogUtil.LogOut(4, LOGTAG, "saveMsgRecord() listMsgId:"
					+ listMsgId);

			try {
				String pubMsgName = getMsgDir() + mUserId; // files/push/msg/pub_msg

				File file = new File(pubMsgName);
				if (!file.exists()) {
					file.createNewFile();
				}
				
				FileOutputStream outputStream = new FileOutputStream(file);
				outputStream.write(listMsgId.toString().getBytes());
				outputStream.flush();
				
				outputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
				LogUtil.LogOut(3, LOGTAG,
						"saveMsgRecord() encounter exception!");
			}
		} else {
			LogUtil.LogOut(2, LOGTAG, "saveMsgRecord() perMsgId is invalid!");
		}
	}

	@Override
	public String getMinMsgid() {
		String mixPubMsgId = "";
		String[] sortPubList = getMsgList();
		if (sortPubList != null && sortPubList.length > 0) {
			mixPubMsgId = sortPubList[0];
		}
		return mixPubMsgId;
	}

	@Override
	public String getMaxMsgid() {
		String maxPubMsgId = "";
		String[] sortPubList = getMsgList();
		if (sortPubList != null && sortPubList.length > 0) {
			maxPubMsgId = sortPubList[sortPubList.length - 1];
		}
		return maxPubMsgId;
	}

}
