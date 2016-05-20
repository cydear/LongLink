package com.xianglin.fellowvillager.app.longlink.rome.longlinkservice.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.xianglin.fellowvillager.app.longlink.longlink.LongLinkServiceManager;
import com.xianglin.fellowvillager.app.longlink.longlink.service.ReconnCtrl;
import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;
import com.xianglin.fellowvillager.app.longlink.longlink.util.Constants;
import com.xianglin.mobile.common.logging.LogCatLog;
import com.xianglin.mobile.framework.XiangLinApplication;
import com.xianglin.mobile.framework.service.ext.ExternalService;


public class LongLinkScreenOffService extends ExternalService {
    private final String TAG = ConfigUtils.TAG;


    BroadcastReceiver mScreenOffBroadcastReceiver = null;
    XiangLinApplication mAlipayApplication;//AlipayApplication

    @Override
    protected void onCreate(Bundle params) {
        LogCatLog.d(TAG, "LongLinkScreenOffService is onCreate...");

        mAlipayApplication = getMicroApplicationContext().getApplicationContext();

        mScreenOffBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LongLinkServiceManager servManager = LongLinkServiceManager.getInstance(mAlipayApplication);

                // 在切换到后台、屏幕关闭和初始化时设置此标志位
                // 达到切换到前台时进行active调用的目的。（因为没有切换到前台的事件）
                ReconnCtrl.setConAction(Constants.CONNECT_ACTION_ACTIVE);
                LogCatLog.d(TAG, "LongLinkScreenOffService will stopLink.");
                servManager.stopLink();
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mAlipayApplication.registerReceiver(mScreenOffBroadcastReceiver, intentFilter);

    }

    @Override
    protected void onDestroy(Bundle params) {
        super.destroy(params);

        if (null != mScreenOffBroadcastReceiver) {
            mAlipayApplication.unregisterReceiver(mScreenOffBroadcastReceiver);
        }
    }

}
