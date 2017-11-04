package com.rouchtime.util;

public class CommonUtils {
	public static String jointMultipleTitleWithCleanRaw(String raw, String title, int multiple) {
		if (multiple <= 0) {
			multiple = 0;
		}
		if (String.valueOf(raw).equals("null")) {
			String msg = "raw is null";
			throw new IllegalArgumentException(msg);
		}
		if (String.valueOf(title).equals("null")) {
			String msg = "title is null";
			throw new IllegalArgumentException(msg);
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < multiple; i++) {
			sb.append(title).append("，");
		}
		sb.append(RegexUtils.cleanSpecialWord(raw));
		return sb.toString();
	}
}
