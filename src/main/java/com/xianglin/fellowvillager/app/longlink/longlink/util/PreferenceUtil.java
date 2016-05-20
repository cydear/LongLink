package com.xianglin.fellowvillager.app.longlink.longlink.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.ConcurrentHashMap;

public class PreferenceUtil {

	private static PreferenceUtil mInstance = null;

	private Context mContext;
	private ConcurrentHashMap<String, String> cacheDataMap; // HashMap不是线程安全的

	// 同步锁
	private Object mLock = new Object();
	

	public static synchronized PreferenceUtil getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new PreferenceUtil(context);
		}

		return mInstance;
	}

	private PreferenceUtil(Context context) {
		mContext = context;

		cacheDataMap = new ConcurrentHashMap<String, String>();
	}

	public String getString(String key) {
		String caData = cacheDataMap.get(key);
		if (null != caData && !"".equals(caData)) {
			return caData;
		}

		try {
			SharedPreferences sharePrefs = mContext.getSharedPreferences(
					Constants.PUSH_PREFERENCE_NAME, Context.MODE_PRIVATE);

			String val = null;
			synchronized (mLock) {
				val = sharePrefs.getString(key, null);
			}
			
			if (val != null) {
				cacheDataMap.put(key, val);
			}

			return val;
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public boolean putString(String key, String value) {
		try {
			cacheDataMap.put(key, value);

			SharedPreferences sharePrefs = mContext.getSharedPreferences(
					Constants.PUSH_PREFERENCE_NAME, Context.MODE_PRIVATE);

			synchronized (mLock) {
				sharePrefs.edit().putString(key, value).commit();
			}
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}

}
