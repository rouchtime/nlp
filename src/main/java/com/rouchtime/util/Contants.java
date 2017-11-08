package com.rouchtime.util;

public class Contants {
	public final static String TAB = "\t";
	public final static String ENTER = "\n";
	public final static String SLASH = "/";
	public final static Integer NEWS_URL_LENGTH = 15;
	public final static Integer VIDEO_PIC_URL_LENGTH = 21;
	public final static String DOT = ".";

	public static enum WordArea {
		TITLE, FIRSTPRAR, BODY, LASTPRAR
	};

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

	public static String GUONEI(Object t) {
		return "@relation '" + t + "'\r\n" + "@attribute Text string\r\n"
				+ "@attribute class {一般违法,中央时政,体育,公共基建,其他生活新闻,军事相关,台湾时政,国内趣闻,国际相关,地方人文,地方发展,地方时政,地方活动,基层工作,娱乐,情感话题,意外事故,文艺,暴力犯罪,民生话题,港澳时政,生活休闲,生活气象,生活百态,社会乱象,社会正能量,科技,科研科考,纪检反腐,自然灾害,自然猎奇,金融经济}\r\n"
				+ "@data\r\n";
	}

	public static String GUONEIHEADCOMBINE(Object t) {
		return "@relation '" + t + "'\r\n" + "@attribute Text string\r\n" + "@attribute class {体育_娱乐," + "军事相关,"
				+ "国际相关," + "地方发展_地方活动_公共基建_地方时政_中央时政_基层工作_地方人文_文艺," + "意外事故," + "暴力犯罪_一般犯罪_社会乱象_纪检反腐," + "民生话题,"
				+ "港澳台时政," + "生活百态_生活休闲_国内趣闻_社会正能量_情感话题," + "科技," + "科研科考," + "自然灾害_生活气象," + "自然猎奇," + "金融经济}\r\n"
				+ "@data\r\n";
	}

	public static String GUONEISECONDLABEL(Object t) {
		return "@relation '" + t + "'\r\n" + "@attribute Text string\r\n" + "@attribute class {difangxinwen,"
				+ "guojixiangguan," + "guoneizonghe," + "junshixiangguan," + "lieqixinwen," + "minshengbaitai,"
				+ "shizhengxinwen," + "tianzairenhuo}\r\n" + "@data\r\n";
	}

	public static String GUONEITHIRDLABEL(Object t) {
		return "@relation '" + t + "'\r\n" + "@attribute Text string\r\n" + "@attribute class {difanghuodong,"
				+ "jicenggongzuo," + "guoneijijian," + "difangrenwen," + "difangfazhan," + "difangshizheng,"
				+ "shenghuoqixiang," + "guojixiangguan," + "junshixiangguan," + "shenghuobaitai," + "shehuiluanxiang_s,"
				+ "shehuizhengnengliang," + "qitashenghuo_s," + "qingganhuati_s," + "shehuiminsheng,"
				+ "shenghuoxiuxian," + "guoneiquwen," + "ziranlieqi," + "shehuitiyu," + "shehuiyule," + "shehuikeji,"
				+ "shehuikeyan," + "shehuiwenyi," + "shehuijinrong," + "taiwanshizheng," + "jijianfanfu,"
				+ "gangaoshizheng," + "zhongyangshizheng," + "yiwaishigu_s," + "baolifanzui," + "ziranzaihai_s,"
				+ "yibanweifa}\r\n" + "@data\r\n";
	}

	public static String JUNSHITHIRDLABEL(Object t) {
		return "@relation '" + t + "'\r\n" + "@attribute Text string\r\n" + "@attribute class {" + "shezhongjunshi,"
				+ "guojijunzheng," + "guowaijunren," + "junduijianshe," + "guoneijunzheng," + "zhongguojunren,"
				+ "taihaijushi," + "lengbingqijunshi," + "guojijindaijunshi," + "zhongguojindaijunshi,"
				+ "zhongguoxiandaijunshi," + "yierzhanshi," + "junshijingsai," + "shezhongjunyan," + "guojijunyan,"
				+ "xiandaizhanzheng," + "zhanqushehui," + "baolichongtu," + "danbingzhuangbei," + "lujunzhuangbei,"
				+ "haijunzhuangbei," + "hangkonghangtian," + "daodan," + "zhangluewuqi," + "dianzixinxizhuangbei,"
				+ "jianduanjunbei," + "wuqizonghe," + "junshiqita}\r\n" + "@data\r\n";
	}

	public static String JUNSHISECONDLABEL(Object t) {
		return "@relation '" + t + "'\r\n" + "@attribute Text string\r\n" + "@attribute class {guojijunshi,"
				+ "guoneijunshi," + "junshijiuwen," + "junshiyanxi," + "wuzhuangdongluan," + "wuqizhuangbei,"
				+ "junshiqita}\r\n" + "@data\r\n";
	}

	public static String JUNSHICHINESELABEL(Object t) {
		return "@relation '" + t + "'\r\n" + "@attribute Text string\r\n" + "@attribute class {涉中军事," + "国际军政,"
				+ "国外军人," + "军队建设," + "国内军政," + "中国军人," + "台海局势," + "冷兵器军史," + "国际近代军史," + "中国近代军史," + "中国现代军史,"
				+ "一二战史," + "军事竞赛," + "涉中军演," + "国际军演," + "现代战争," + "战区社会," + "暴力冲突," + "单兵装备," + "陆军装备," + "海军装备,"
				+ "航空航天," + "导弹," + "战略武器," + "电子信息装备," + "尖端军备," + "武器综合,其他}\r\n" + "@data\r\n";
	}

	public static String YULESECONDLABEL(Object t) {
		return "@relation '" + t + "'\r\n" + "@attribute Text string\r\n" + "@attribute class {yulebagua,"
				+ "yuledianshi," + "yuledianying," + "yulemingxing," + "yulezongyi}\r\n" + "@data\r\n";
	}

	public static String YULEBAGUA(Object t) {
		return "@relation '" + t + "'\r\n" + "@attribute Text string\r\n" + "@attribute class {gouzaishijiao,"
				+ "mingxingheiliao," + "mingxingjiating," + "mingxingqingganbagua," + "mingxingweifa,"
				+ "qitabaguaneirong}\r\n" + "@data\r\n";
	}
}
