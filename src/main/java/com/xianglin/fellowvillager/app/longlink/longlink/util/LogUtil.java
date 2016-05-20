package com.xianglin.fellowvillager.app.longlink.longlink.util;

import android.util.Log;

/**
 * Utility class for LogCat.
 */
public class LogUtil {

	public static boolean CONFIGURE_ENABLE = true;//false; alex

	public static int LOG_LEVEL = 1;

	public static int LOG_LEVEL_ERROR = 1;
	public static int LOG_LEVEL_WARNING = 2;
	public static int LOG_LEVEL_INFO = 3;
	public static int LOG_LEVEL_DEBUG = 4;
	public static int LOG_LEVEL_DETAIL = 5;

	public static String makeLogTag(@SuppressWarnings("rawtypes") Class cls) {
		return "LongLink_" + cls.getSimpleName();
	}
	
	public static void refreshDebugMode() {
		if (LogUtil.CONFIGURE_ENABLE == true) {
			LogUtil.LOG_LEVEL = LogUtil.LOG_LEVEL_DEBUG;
			
		} else {
			// 彻底关闭log
			LogUtil.LOG_LEVEL = LogUtil.LOG_LEVEL_ERROR;
		}
	}

	public static void LogOut(int level, String tag, String info) {
//		if (LogUtil.LOG_LEVEL >= level) {
//			switch (level) {
//			case 1:
				Log.d(tag, info);
//				break;
//			case 2:
//				Log.w(tag, info);
//				break;
//			case 3:
//				Log.i(tag, info);
//				break;
//			case 4:
//				Log.d(tag, info);
//				break;
//			case 5:
//				Log.v(tag, info);
//				break;
//			default:
				//
//				break;
//			}
//		}
		return;
	}

}
