package com.xianglin.fellowvillager.app.longlink.rome.longlinkservice;

import com.xianglin.mobile.framework.service.ext.ExternalService;


/**
 * 外部服务：业务长连接服务
 *
 * @author alex
 */
public abstract class LongLinkSyncService extends ExternalService {
    public abstract boolean getLinkState();
}
