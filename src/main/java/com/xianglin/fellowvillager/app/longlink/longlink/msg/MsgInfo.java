package com.xianglin.fellowvillager.app.longlink.longlink.msg;

import java.io.Serializable;


public class MsgInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String msgKey;
	private String msgTimestamp;
	private String perMsgId;


	private String msgData = "";
	private String userId = "";

	public MsgInfo() {
		super();

		this.userId = "";
		this.msgData = "";
		this.msgKey = "";
		this.msgTimestamp = "";
		this.perMsgId = "";

	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String user) {
		this.userId = user;
	}
	
	public String getMsgData() {
		return msgData;
	}

	public void setMsgData(String data) {
		this.msgData = data;
	}
	
	public String getMsgKey() {
		return msgKey;
	}

	public void setMsgKey(String key) {
		this.msgKey = key;
	}

	public String getTimestamp() {
		return msgTimestamp;
	}

	public void setTimestamp(String timeStamp) {
		this.msgTimestamp = timeStamp;
	}

	public String getPerMsgId() {
		return perMsgId;
	}

	public void setPerMsgId(String id) {
		this.perMsgId = id;
	}

}