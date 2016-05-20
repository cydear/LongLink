package com.xianglin.fellowvillager.app.longlink.longlink.sync;

public class LinkSyncData {
	
	private String mBizType = "";
	
	private String mBizData = "";
	
	
	public LinkSyncData() {
		//
	}

	public String getBizType() {
		return this.mBizType;
	}

	public void setBizType(String biz) {
		this.mBizType = biz;
	}

	public String getData() {
		return this.mBizData;
	}

	public void setData(String data) {
		this.mBizData = data;
	}

}
