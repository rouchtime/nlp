package com.rouchtime.util;

public class Contants {
	public final static String TAB = "\t";
	public final static String ENTER = "\n";
	public final static String SLASH = "/";
	public final static Integer NEWS_URL_LENGTH = 15;
	public final static Integer VIDEO_PIC_URL_LENGTH = 21;
	public final static String DOT = ".";

	public final static String URL_TIME_REGEX = "yyMMddHHmmssSSS";

	public static String SOUGOUHEAD(Object t) {
		return "@relation '" + t + "'\r\n" + "@attribute Text string\r\n" + "@attribute class {互联网," + "体育," + "健康,"
				+ "军事," + "招聘," + "教育," + "文化," + "旅游," + "财经}\r\n" + "@data\r\n";
	}

	public static String GUOJIHEAD(Object t) {
		return "@relation '" + t + "'\r\n" + "@attribute Text string\r\n"
				+ "@attribute class {gonggongjijian,qitashenghuo,guojitiyu,guojijunshi,guojiwaijiao,guojiyule,guojiwenyi,guojikeji,guojijinrong,qiyidongwu,yiguofengqing,kongbuxiji,qingganhuati,yiwaishigu,huanjierenmian,gaigezhengce,zhengzhichouwen,zhengzhirenwudongtai,zhengnengliangxinwen,wuzhuangyundong,minshenghuati,shezhongshijian,shehuiluanxiang,keyankekao,ziranjingguan,ziranzaihai,jingfeifanzui,quwenyishi,jihuiyouxing,lingtuzhuquan}\r\n"
				+ "@data\r\n";
	}
}
