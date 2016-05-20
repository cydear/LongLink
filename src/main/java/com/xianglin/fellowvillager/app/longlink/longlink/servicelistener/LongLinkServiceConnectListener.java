package com.xianglin.fellowvillager.app.longlink.longlink.servicelistener;

import com.xianglin.fellowvillager.app.ILongLinkService;

/**
 * 长连接服务链接监听
 * 1:服务链接成功
 * 2:服务链接失败
 * Javadoc
 *
 * @author james
 * @version 0.1, 2015-11-27
 */
public abstract class LongLinkServiceConnectListener {

    /**
     * 连接服务成功
     * @param mService
     * @param mIsServiceBound;
     */
    public abstract void connectSuccess(ILongLinkService mService,boolean mIsServiceBound);

    /**
     * 连接服务失败
     * @param mIsServiceBound
     */
    public abstract void connectFailure(boolean mIsServiceBound);





}
