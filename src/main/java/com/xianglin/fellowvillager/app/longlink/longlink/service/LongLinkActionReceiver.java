package com.xianglin.fellowvillager.app.longlink.longlink.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;
import com.xianglin.fellowvillager.app.longlink.longlink.util.LogUtil;

/**
 * 长连接 网络改变 进行重连
 */
public class LongLinkActionReceiver extends BroadcastReceiver {
    private static final String LOGTAG = ConfigUtils.TAG;

    private LongLinkService mLongLinkService;

    public LongLinkActionReceiver() {

    }

    public LongLinkActionReceiver(LongLinkService service) {
        this.mLongLinkService = service;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtil.LogOut(3, LOGTAG, "onReceive() getAction=" + action);

        if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            boolean isNetConnected = false;
            isNetConnected = isNetworkAvailable(context);

            if (isNetConnected) {
                Intent intent1= new Intent();
                intent1.setAction(LongLinkService.ConnectReceiver.CONNECT_ACTION);
                context.sendBroadcast(intent1);
                } else {
                    LogUtil.LogOut(3, LOGTAG, "无网络链接");
                }
        }

    }

    private boolean isNetworkAvailable(Context context) {
        boolean isNetworkAvailable = false;

        try {
            ConnectivityManager connectivity = (ConnectivityManager) (context
                    .getSystemService(Context.CONNECTIVITY_SERVICE));

            NetworkInfo[] networkInfos = connectivity.getAllNetworkInfo();
            if (networkInfos == null) {
                LogUtil.LogOut(2, LOGTAG,
                        "isNetworkAvailable networkInfos Unavailable.");
                return false;
            }

            for (NetworkInfo itemInfo : networkInfos) {
                if (itemInfo != null) {
                    if (itemInfo.getState() == NetworkInfo.State.CONNECTED
                            || itemInfo.getState() == NetworkInfo.State.CONNECTING) {
                        isNetworkAvailable = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.LogOut(2, LOGTAG,
                    "isNetworkAvailable networkInfos Unavailable. exception:" + e.getMessage());
            return false;
        }

        LogUtil.LogOut(4, LOGTAG, "isNetworkAvailable=" + isNetworkAvailable);
        return isNetworkAvailable;
    }


}
