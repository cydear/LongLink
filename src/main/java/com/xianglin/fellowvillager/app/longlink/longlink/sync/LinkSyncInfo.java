package com.xianglin.fellowvillager.app.longlink.longlink.sync;

import android.content.Context;

import com.xianglin.fellowvillager.app.longlink.longlink.util.PreferenceUtil;


public class LinkSyncInfo {

	private Context mContext;

	public LinkSyncInfo(Context context) {
		mContext = context;
	}

	public void setSyncKey(String userId, long syncKey) {
		
		if (userId != null && userId.length() >0) {
			PreferenceUtil pushStore = PreferenceUtil.getInstance(mContext);
			pushStore.putString(userId,
					String.valueOf(syncKey));
		}
		
	}

	public long getSyncKey(String userId) {
		long syncKey = 0;

		if (userId != null && userId.length() >0) {
			PreferenceUtil pushStore = PreferenceUtil.getInstance(mContext);
			String value = pushStore.getString(userId);//null
	
			if (value != null && value.length() > 0) {
				syncKey = Long.parseLong(value);
			}
		}

		return syncKey;
	}

}
