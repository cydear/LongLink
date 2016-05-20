package com.xianglin.fellowvillager.app.longlink.rome.longlinkservice.service;


import android.app.Application;
import android.os.Bundle;

import com.xianglin.fellowvillager.app.ILongLinkService;
import com.xianglin.fellowvillager.app.longlink.longlink.LongLinkServiceManager;
import com.xianglin.fellowvillager.app.longlink.longlink.servicelistener.LongLinkServiceConnectListener;
import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;
import com.xianglin.fellowvillager.app.longlink.rome.longlinkservice.LongLinkSyncService;
import com.xianglin.mobile.common.info.AppInfo;
import com.xianglin.mobile.common.logging.LogCatLog;
import com.xianglin.mobile.framework.XiangLinApplication;


/**
 * 外部服务
 *
 * @author alex
 */
public class LongLinkSyncServiceImpl extends LongLinkSyncService {
    public static final String TAG = ConfigUtils.TAG;

    private LongLinkServiceManager mLLServManager;

    @Override
    protected void onCreate(Bundle params) {
        LogCatLog.i(TAG, "onCreate Enter...");

        Application appContext = XiangLinApplication.getInstance();//AlipayApplication
        mLLServManager = LongLinkServiceManager.getInstance(appContext);

        if (AppInfo.getInstance().isDebuggable()) {
            //设置开启日志,发布时请关闭日志
            LogCatLog.i(TAG, "onCreate: setDebugMode true.");
            mLLServManager.setDebugMode(true);
        }

        mLLServManager.bindService(new LongLinkServiceConnectListener() {
            @Override
            public void connectSuccess(ILongLinkService mService, boolean mIsServiceBound) {

            }

            @Override
            public void connectFailure(boolean mIsServiceBound) {

            }
        });
    }

    @Override
    protected void onDestroy(Bundle params) {
        mLLServManager.unBindService();
        mLLServManager = null;
    }

    @Override
    public boolean getLinkState() {
        return mLLServManager.isConnected();
    }

}
