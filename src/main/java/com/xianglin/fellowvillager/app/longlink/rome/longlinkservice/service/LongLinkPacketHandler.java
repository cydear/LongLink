package com.xianglin.fellowvillager.app.longlink.rome.longlinkservice.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.xianglin.fellowvillager.app.longlink.longlink.PacketHanlder;
import com.xianglin.fellowvillager.app.longlink.longlink.sync.LinkSyncConstants;
import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;
import com.xianglin.fellowvillager.app.longlink.rome.longlinkservice.LongLinkMsgConstants;
import com.xianglin.mobile.common.logging.LogCatLog;


/**
 * 长连接
 * 长连接 收到消息处理
 *
 * @author alex
 */
public class LongLinkPacketHandler implements PacketHanlder {

    private final String TAG = ConfigUtils.TAG;
    public static final String MESSAGERCV_RECEIVED_MESSAGE = "message";

    private Context mContext;

    public volatile static LongLinkPacketHandler longLinkPacketHandler;

    private LongLinkPacketHandler(Context ctx) {
        mContext = ctx;
    }

    public static synchronized LongLinkPacketHandler getInstance(Context context) {
        if (longLinkPacketHandler == null) {
            longLinkPacketHandler = new LongLinkPacketHandler(context);
        }
        return longLinkPacketHandler;
    }

    @Override
    public void processPacket(String bizType, String appId, String appData, long syncKey, long opCode) {
        LogCatLog.d(TAG,
                "长连接包处理:" +
                        "\nbizType=" + bizType +
                        "\nappId=" + appId +
                        "\nsyncKey= " + syncKey +
                        "\nopCode= " + opCode +
                        "\nappData=" + appData);

        if (bizType != null && bizType.length() > 0) {
            if (bizType.equals(LongLinkMsgConstants.MSG_PACKET_TYPE_DEFAULT)) {// 默认
                if (appId != null && appId.length() > 0) {
                    LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);
                    String action = LongLinkMsgConstants.LONGLINK_ACTION_CMD_TRANSFER;
                    //=========================================//
                    Intent broadIntent = new Intent(action);
                    broadIntent.putExtra(LongLinkMsgConstants.LONGLINK_APPDATA, appData);
                    //=========================================//
                    broadcastManager.sendBroadcast(broadIntent);
                } else {
                    LogCatLog.w(TAG, "processPacket: appId is invalid!");
                }
            } else if (bizType.equals(LongLinkMsgConstants.MSG_PACKET_TYPE_CHAT)) {// 聊天
                LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);
                String action = LongLinkMsgConstants.LONGLINK_ACTION_CMD_TRANSFER + "CHAT";
                //=========================================//
                Intent broadIntent = new Intent(action);
                broadIntent.putExtra(LongLinkMsgConstants.LONGLINK_APPDATA, appData);
                broadIntent.putExtra(LinkSyncConstants.LINK_SYNC_OPCODE,opCode);
                broadIntent.putExtra(LinkSyncConstants.LINK_SYNC_KEY,syncKey);
                //=========================================//
                broadcastManager.sendBroadcast(broadIntent);
            } else if (bizType.equals(LongLinkMsgConstants.MSG_PACKET_TYPE_NOTICE)) {// 通知
                LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);
                String action = LongLinkMsgConstants.LONGLINK_ACTION_CMD_TRANSFER + "NOTICE";
                //=========================================//
                Intent broadIntent = new Intent(action);
                broadIntent.putExtra(LongLinkMsgConstants.LONGLINK_APPDATA, appData);
                broadIntent.putExtra(LinkSyncConstants.LINK_SYNC_OPCODE,opCode);
                broadIntent.putExtra(LinkSyncConstants.LINK_SYNC_KEY,syncKey);
                //=========================================//
                broadcastManager.sendBroadcast(broadIntent);

            } else {
                LogCatLog.w(TAG, "processPacket: unsupported bizType=" + bizType);
            }
        } else {
            LogCatLog.w(TAG, "processPacket: warning bizType is invalid!");
        }
    }

    @Override
    public void processPacket(boolean isRegoster,int state) {
        LogCatLog.d(TAG,"注册包处理");
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);
        String action = LongLinkMsgConstants.LONGLINK_ACTION_CMD_TRANSFER+"DEFAULT";
        Intent broadIntent = new Intent(action);
        broadIntent.putExtra("ISSUCCESS",isRegoster);
        broadcastManager.sendBroadcast(broadIntent);

    }
}
