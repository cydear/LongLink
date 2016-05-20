package com.xianglin.fellowvillager.app.longlink.longlink.sync;


public class LinkSyncPacket {
	
	private long mSyncKey = System.currentTimeMillis();
	
	private int mOpCode = LinkSyncConstants.LINK_SYNC_OPCODE_SYNC_REQ;
	
	private String mSyncData ;
	
	
	public LinkSyncPacket() {
		//
	}

	public long getSyncKey() {
		return this.mSyncKey;
	}

	public void setSyncKey(long sKey) {
		this.mSyncKey = sKey;
	}

	public int getOpCode() {
		return this.mOpCode;
	}

	public void setOpCode(int sOpCode) {
		this.mOpCode = sOpCode;
	}

	public String getData() {
		return this.mSyncData;
	}

	public void setData(String data) {
		this.mSyncData = data;
	}

	@Override
	public String toString() {
		return "LinkSyncPacket [mSyncKey=" + mSyncKey + ", mOpCode=" + mOpCode + ", mSyncData=" + mSyncData + "]";
	}


}
