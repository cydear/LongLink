package com.xianglin.fellowvillager.app.longlink.longlink.message;

import com.xianglin.fellowvillager.app.longlink.longlink.model.Md;
import com.xianglin.fellowvillager.app.longlink.longlink.service.LongLinkService;
import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;

/**
 * 消息发送
 * Javadoc
 *
 * @author james
 * @version 0.1, 2015-12-11
 */
public class LongLinkMessageSender {

    private static final String TAG = ConfigUtils.TAG;

    public LongLinkMessageSender() {

    }

    /**
     * 初始化长链接
     */
    public void initLongLink(LongLinkService longLinkService) {
        String mUserId = longLinkService.getConnManager().getXlId();// 获取相邻ID
        String mDeviceId = longLinkService.getConnManager().getDeviceId(); //获取设备ID
        Md md = new Md();
        md.setFromid(mUserId);
        md.setDeviceId(mDeviceId);
        longLinkService.getConnManager().submitLinkSyncTask(md.toString());
    }
}
