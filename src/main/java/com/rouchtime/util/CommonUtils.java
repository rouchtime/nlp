package com.rouchtime.util;

public class CommonUtils {
	
	/**
	 * 标题和内容的拼接
	 * @param multiple 标题倍数
	 * @param title
	 * @param raw
	 * @return
	 */
	public static StringBuffer jointMultipleTitleAndRaw(int multiple, String title, String raw) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < multiple; i++) {
			sb.append(title).append(",");
		}
		sb.append(raw);
		return sb;
	}
}
