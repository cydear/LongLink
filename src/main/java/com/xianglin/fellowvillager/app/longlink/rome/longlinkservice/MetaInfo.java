package com.xianglin.fellowvillager.app.longlink.rome.longlinkservice;

import com.xianglin.fellowvillager.app.longlink.rome.longlinkservice.msg.LongLinkeMsgReceiver;
import com.xianglin.fellowvillager.app.longlink.rome.longlinkservice.service.LongLinkScreenOffService;
import com.xianglin.fellowvillager.app.longlink.rome.longlinkservice.service.LongLinkSyncServiceImpl;
import com.xianglin.mobile.framework.BaseMetaInfo;
import com.xianglin.mobile.framework.msg.BroadcastReceiverDescription;
import com.xianglin.mobile.framework.service.ServiceDescription;


public class MetaInfo extends BaseMetaInfo {

    public MetaInfo() {

        BroadcastReceiverDescription frmReceiverDesp = new BroadcastReceiverDescription();
        frmReceiverDesp.setName("LongLinkeMsgReceiver");
        frmReceiverDesp.setClassName(LongLinkeMsgReceiver.class.getName());
        frmReceiverDesp
                .setMsgCode(new String[]{
                        com.xianglin.mobile.framework.msg.MsgCodeConstants.FRAMEWORK_ACTIVITY_START,
                        com.xianglin.mobile.framework.msg.MsgCodeConstants.FRAMEWORK_ACTIVITY_USERLEAVEHINT,
                        com.xianglin.mobile.common.msg.MsgCodeConstants.SECURITY_LOGIN,
                        com.xianglin.mobile.common.msg.MsgCodeConstants.SECURITY_LOGOUT,
                        com.xianglin.mobile.common.msg.MsgCodeConstants.SECURITY_CLEANACCOUNT_ACTION,
                        com.xianglin.mobile.common.msg.MsgCodeConstants.SECURITY_START_LOGIN,
                        com.xianglin.mobile.common.msg.MsgCodeConstants.LONGLINK_ACTION_CMD_UPLINK});

        broadcastReceivers.add(frmReceiverDesp);

        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription
                .setClassName(LongLinkSyncServiceImpl.class.getName());
        serviceDescription.setInterfaceClass(LongLinkSyncService.class
                .getName());
        serviceDescription.setLazy(false);    //不能延迟加载
        services.add(serviceDescription);

        ServiceDescription screenOnOffServiceDescription = new ServiceDescription();
        screenOnOffServiceDescription.setClassName(LongLinkScreenOffService.class.getName());
        screenOnOffServiceDescription.setInterfaceClass(LongLinkScreenOffService.class.getName());
        screenOnOffServiceDescription.setLazy(false);
        services.add(screenOnOffServiceDescription);

    }

}
