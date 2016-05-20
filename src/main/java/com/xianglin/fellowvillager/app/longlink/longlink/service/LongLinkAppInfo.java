package com.xianglin.fellowvillager.app.longlink.longlink.service;


public class LongLinkAppInfo {

	private static LongLinkAppInfo mInstance;
	
	private String mUserId;
	private String mDevicesId;
	private String mExtToken;
	private String mLoginTime;
	private long mSkey;// 客户端最高的消息版本号
	private String mDeviceId;//设备ID

	public LongLinkAppInfo() {
		init();
	}

    public static LongLinkAppInfo getInstance() {
        if (mInstance == null)
            throw new IllegalStateException(
                "PushAppInfo must be created by calling createInstance...");
        return mInstance;
    }

    public static synchronized LongLinkAppInfo createInstance() {
        if (mInstance == null) {
            mInstance = new LongLinkAppInfo();
        }
        return mInstance;
    }

    /**
     * 初始化
     */
    private void init() {
    	mUserId = "";
    	mExtToken = "";
    	mLoginTime = "";
		mSkey = 0;
		mDeviceId = "";
    }

	public void setUserId(String userId) {
		mUserId = userId;
	}

	public String getUserId() {
		return mUserId;
	}

	public void setExtToken(String extToken) {
		mExtToken = extToken;
	}

	public String getExtToken() {
		return mExtToken;
	}
	
	public void setLoginTime(String time) {
		mLoginTime = time;
	}

	public String getLoginTime() {
		return mLoginTime;
	}


	public long getmSkey() {
		return mSkey;
	}

	public LongLinkAppInfo setmSkey(long mSkey) {
		this.mSkey = mSkey;
		return this;
	}

	public String getmDeviceId() {
		return mDeviceId;
	}

	public LongLinkAppInfo setmDeviceId(String mDeviceId) {
		this.mDeviceId = mDeviceId;
		return this;
	}


}
