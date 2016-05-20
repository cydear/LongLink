package com.xianglin.fellowvillager.app.longlink.longlink.transport.connectionListener;

import com.xianglin.fellowvillager.app.longlink.longlink.transport.connection.PushConnection;


/**
 * 每个任务分成功和失败两种情况
 */
public interface ConnectListener {
	// 任务成功的回调
	public void onSuccess(PushConnection pushConnection);

	// 任务失败的回调
	public void onFail();
}
