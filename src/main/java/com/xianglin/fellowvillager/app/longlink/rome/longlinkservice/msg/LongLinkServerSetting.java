package com.xianglin.fellowvillager.app.longlink.rome.longlinkservice.msg;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;

public class LongLinkServerSetting {

    private static LongLinkServerSetting mInstance;

    private LongLinkServerSetting() {

    }

    public static synchronized LongLinkServerSetting getInstance() {
        if (mInstance == null) {
            mInstance = new LongLinkServerSetting();
        }
        return mInstance;
    }

    public String getValue(Context context, String uri, String defaultVal) {
        Cursor cursor = context.getContentResolver().query(Uri.parse(uri), null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            String ret = cursor.getString(0);
            cursor.close();
            return ret;
        }
        return defaultVal;
    }

    public final String getLongLinkHost(Context context) {
        if (isDebug(context)) {
            return getValue(context, "content://com.alipay.setting/PushServerUrl",
                    "mobilepmgw.alipay.com");
        } else {
            return "mobilepmgw.alipay.com";
        }
    }

    public final int getLongLinkPort(Context context) {
        String port = "443";

        if (isDebug(context)) {
            port = getValue(context, "content://com.alipay.setting/PushPort",
                    "443");
        } else {
            port = "443";
        }

        return Integer.valueOf(port);
    }

    public final String getLongLinkSSLFlag(Context context) {
        if (isDebug(context)) {
            return getValue(context, "content://com.alipay.setting/PushUseSsl",
                    "1");
        } else {
            return "1";
        }
    }

    public static boolean isDebug(Context context) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            if ((applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                // development mode
                return true;
            } else {
                // release mode
                return false;
            }
        } catch (Exception e) {
        }
        return false;
    }

}
