package com.xianglin.fellowvillager.app.longlink.longlink.util;

import java.util.StringTokenizer;

/**
 * A collection of utility methods for String objects.
 */
public class StringUtils {

	/**
	 * 拆分以逗号分隔的字符串,并存入String数组中
	 * 
	 * @param sSource
	 *            源字符串
	 * @return String[]
	 */
	public static String[] strToArray(String sSource) {
		String aReturn[] = null;

		StringTokenizer st = null;
		st = new StringTokenizer(sSource, ",");
		aReturn = new String[st.countTokens()];
		int i = 0;
		while (st.hasMoreTokens()) {
			aReturn[i] = st.nextToken();
			i++;
		}

		return aReturn;
	}

	/**
	 * 将数组中的元素连成一个以逗号分隔的字符串
	 * 
	 * @param aSource
	 *            源数组
	 * @return 字符串
	 */
	public static String arrayToString(String[] aSource) {
		String sReturn = "";

		for (int i = 0; i < aSource.length; i++) {
			if (i > 0) {
				sReturn += ",";
			}
			sReturn += aSource[i];
		}

		return sReturn;
	}

	/**
	 * 将两个字符串的所有元素连结为一个字符串数组
	 * 
	 * @param source
	 *            源字符串数组1
	 * @param item
	 *            源字符串数组2
	 * @return String[]
	 */
	public static String[] arrayAppend(String[] source, String item) {
		int iLen = 0;
		int delta = 1;
		String aReturn[] = null;

		if (source == null) {
			source = new String[0];
		}
		if (item == null) {
			delta = 0;
		}
		iLen = source.length;
		aReturn = new String[iLen + delta];

		/**
		 * 将第一个字符串数组的元素加到结果数组中
		 */
		for (int i = 0; i < iLen; i++) {
			aReturn[i] = source[i];
		}

		if (delta == 1) {
			aReturn[iLen] = item;
		}

		return aReturn;
	}

	/**
	 * 查找源字符串数组中是否包含给定字符串
	 * 
	 * @param aSource
	 *            :源字符串数组
	 * @param sItem
	 *            :子串
	 * @return 是否包含
	 */
	public static boolean isContain(String[] aSource, String sItem) {
		boolean isReturn = false;

		for (int i = 0; i < aSource.length; i++) {
			if (sItem.equals(aSource[i])) {
				isReturn = true;
				break;
			}
		}
		return isReturn;
	}
}
