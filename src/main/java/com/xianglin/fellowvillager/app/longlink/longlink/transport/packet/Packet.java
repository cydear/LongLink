package com.xianglin.fellowvillager.app.longlink.longlink.transport.packet;

import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;

import java.io.UnsupportedEncodingException;

public abstract class Packet {
	private static final String LOGTAG = ConfigUtils.TAG;
	
	/**
	 * Represents the version of a packet.
	 */
	private int mVer = 0;//协议版本号
	protected int mId = 0;//通信指令标识 ?
	private int mType = 0;//通信指令类型
	protected int mHdrLen = 0;//协议数据头部长度


	private int mLen = 0;//协议数据长度
	private int mIsDataGziped = PacketConstants.MSG_GZIP_OFF;  // 数据是否进行了Gzip压缩  1:压缩  0：不压缩
	
	private String mExtField = "";//扩展信息

	private String mData = "";//应该是数据

	public static boolean isSupport(Packet packet) {
		boolean ret = true;

		if (packet.getPacketVersion() != PacketConstants.PACKET_VERSION_2) {
			ret = false;
		}
		return ret;
	}

	public Packet() {
		//
	}

	public Packet(int id, int type, String data) {
		this.mId = id;
		this.mType = type;

		if (data != null)
			this.mData = data;
		this.mExtField = "extend";
	}

	public int getMsgId() {
		return this.mId;
	}

	public void setMsgId(int id) {
		this.mId = id;
	}

	public int getMsgType() {
		return this.mType;
	}

	public void setMsgType(int type) {
		this.mType = type;
	}

	public int getDataLength() {
		return this.mLen;
	}

	public void setDataLength(int len) {
		this.mLen = len;
	}

	public String getData() {
		return this.mData;
	}

	public void setData(String data) {
		this.mData = data;

		try {
			if (data != null && !data.equals("")) {
				byte[] b_utf8 = data.toString().getBytes("utf8");
				this.mLen = b_utf8.length;
			} else {
				this.mData = "[]";
				this.mLen = 0;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void setData(byte[] data) {
		String rawData = "";

		try {
			rawData = new String(data, "utf8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		this.mData = rawData;
	}

	public String getExtField() {
		return this.mExtField;
	}

	public int getPacketVersion() {
		return this.mVer;
	}
	
	public int getmIsDataGziped() {
		return mIsDataGziped;
	}

	public void setmIsDataGziped(int mIsDataGziped) {
		this.mIsDataGziped = mIsDataGziped;
	}

	protected void setPacketVersion(int ver) {
		this.mVer = ver;
	}

	protected void setPacketHdrLen(int len) {
		this.mHdrLen = len;
	}

	/**
	 * used to get the header of this packet
	 */
	public int getPacketHdrLen() {
		return this.mHdrLen;
	}

	/**
	 * used to get the header of this packet
	 */
	public abstract byte[] getHdrbufforWrite();

	/**
	 * used to get the header from buffer
	 */
	public abstract void initHdrfromRead(byte[] readBuf);

	/**
	 * used to get the base header from buffer
	 */
	public abstract void initBaseHdrfromRead(byte[] readBuf);

	/**
	 * 将int类型的数据转换为byte数组 原理：将int数据中的四个byte取出，分别存储
	 * 
	 * @param n
	 *            int数据
	 * @return 生成的byte数组
	 */
	protected static byte[] intToBytes(int n) {
		byte[] b = new byte[4];
		for (int i = 0; i < 4; i++) {
			b[i] = (byte) (n >> (24 - i * 8));
		}
		return b;
	}

	/**
	 * 字节数组到int的转换
	 * 
	 * @param b
	 * @return
	 */
	protected static int bytesToInt(byte[] b) {
		int mask = 0xff;
		int temp = 0;
		int n = 0;
		for (int i = 0; i < 4; i++) {
			n <<= 8;
			temp = b[i] & mask;
			n |= temp;
		}
		return n;
	}

	/**
	 * Returns the packet as byte array.
	 * 
	 * @return the content of the packet as byte array .
	 * @throws UnsupportedEncodingException
	 */
	public byte[] toByteBuf() throws UnsupportedEncodingException {
		// 判断是否需要对数据GZIP压缩
		byte[] data = this.getData().getBytes("utf8");
		
//		if (data.length > 512) {
//			data = ZipUtils.GZipBytes(data);
//			this.setmIsDataGziped(PacketConstants.MSG_GZIP_ON);
//			this.setDataLength(data.length);
//			LogUtil.LogOut(4, LOGTAG, "toByteBuf() is ziped and len="
//					+ this.getDataLength());
//		}

		byte[] buffer = new byte[this.getPacketHdrLen() + data.length];

		System.arraycopy(this.getHdrbufforWrite(), 0, buffer, 0,
				this.getPacketHdrLen());

		System.arraycopy(data, 0, buffer, this.getPacketHdrLen(), data.length);
		//
		
		if(127==buffer.length) {// 为了 跟踪 发往服务器端的数据
			System.out.println("");
		} else if(51==buffer.length) {//51
			System.out.println("");
		} else if(168==buffer.length) {//168
			System.out.println("");
		} else if(15 == buffer.length) {//15
			System.out.println("");
		}
		
		return buffer;
	}
    //alex
	@Override
	public String toString() {
		return "Packet [mVer=" + mVer + ", mId=" + mId + ", mType=" + mType + ", mHdrLen=" + mHdrLen + ", mLen=" + mLen
				+ ", mIsDataGziped=" + mIsDataGziped + ", mExtField=" + mExtField + ", mData=" + mData + "]";
	}
	
	
}