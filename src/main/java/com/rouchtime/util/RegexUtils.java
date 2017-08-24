package com.rouchtime.util;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import com.aliasi.util.Pair;

/**
 * 正则工具包，注意：在写分组时，如果不需要后向引用（重复的情况，如go go）,要加(?:xxxx),可以减少内存消耗
 * 
 * @author 龚帅宾
 *
 */
public class RegexUtils {

	private static List<String> dateFormat = new ArrayList<String>();
	private static List<String> duplicateRegex;
	static {
		dateFormat.add("MM月dd日");
		dateFormat.add("MM月dd");
		dateFormat.add("yyyy年MM月dd日");
		dateFormat.add("yyyy年MM月dd");
		dateFormat.add("dd日");
		dateFormat.add("yyyy/MM/dd");
		dateFormat.add("MM/dd");
		dateFormat.add("yyyy-MM-dd");
		dateFormat.add("MM-dd");
		dateFormat.add("yyyy.MM.dd");
		dateFormat.add("MM.dd");
		try {
			duplicateRegex = IOUtils.readLines(RegexUtils.class.getResourceAsStream("/nlpdic/duplicate_regex.txt"), "utf-8");
		} catch (IOException e) {
			duplicateRegex = new ArrayList<String>();
			e.printStackTrace();
		}
	}

	public static Boolean isExistsNewsReportWords(String raw) {
		String regex = "据?.*报道|(?:(?:[0-1]?[0-9]{1})[-/\\\\.月])?(?:[0-3]?[0-9]{1})[日]?讯|讯|获悉|消息人士透露|最新公告显示";
		Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
		Matcher m = pattern.matcher(raw);
		return m.find(0);
	}

	public static Boolean isExistsTimeWord(String raw) {
		String regex = "[上下]午|今[天日早](?:大盘)?|北京时间|日前|昨[日天]|(?:上)?周[一二三四五六日天]?|近(?:日|几天|期)|最近|过去[一二三四五六七]天";
		Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
		Matcher m = pattern.matcher(raw);
		return m.find(0);
	}

	public static Boolean isExistsDateWord(String raw) {
		String regex = "(?:[0-9]{4})[-/年](?:[0-1]?[0-9]{1})[-/月](?:[0-3]?[0-9]{1})[日]?|(?:[0-1]?[0-9]{1})[-/月](?:[0-3]?[0-9]{1})[日号]?|(?:[0-3]?[0-9]{1})[日号]";
		Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
		Matcher m = pattern.matcher(raw);
		return m.find(0);
	}

	public static List<String> findDateWord(String raw) {
		String regex = "(?:[0-9]{4})[-/年](?:[0-1]?[0-9]{1})[-/月](?:[0-3]?[0-9]{1})[日]?|(?:[0-1]?[0-9]{1})[-/月](?:[0-3]?[0-9]{1})[日号]?|(?:[0-3]?[0-9]{1})[日号]";
		Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
		Matcher m = pattern.matcher(raw);
		List<String> dates = new ArrayList<String>();
		while (m.find()) {
			String date = m.group();
			if (date != null) {
				dates.add(date);
			}
		}
		return dates;
	}

	public static String cleanSpecialWord(String text) {
		String regex = "\\s+|　+|&nbsp+|#+| +|[\\u0000]+|(?:\\r)+|(?:\\n)+|[\\u2003]+|;+";
		return text.replaceAll(regex, "");
	}
	
    public static String stringToHexString(String s) {  
        String str = "";  
        for (int i = 0; i < s.length(); i++) {  
            int ch = (int) s.charAt(i);  
            String s4 = Integer.toHexString(ch);  
            str = str + s4;  
        }  
        return str;  
    }  
	/**
	 * 解析包含点的日期，如2017.2.2等
	 * 
	 * @param raw
	 * @return
	 */
	public static List<String> findDateWordContainsDot(String raw) {
		String regex = "(?:[0-9]{4})[-/年](?:[0-1]?[0-9]{1})[-/\\.月](?:[0-3]?[0-9]{1})[日]?|(?:[0-1]?[0-9]{1})[-/\\.月](?:[0-3]?[0-9]{1})[日号]?|(?:[0-3]?[0-9]{1})[日号]";
		Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
		Matcher m = pattern.matcher(raw);
		List<String> dates = new ArrayList<String>();
		while (m.find()) {
			String date = m.group();
			if (date != null) {
				dates.add(date);
			}
		}
		return dates;
	}

	public static Long findDateConvertToTime(String raw) {

		List<String> strDates = findDateWord(raw);
		if (strDates != null && strDates.size() != 0) {
			for (String df : dateFormat) {
				for (String date : strDates) {
					SimpleDateFormat sdf = new SimpleDateFormat(df);
					try {
						return sdf.parse(date).getTime();
					} catch (ParseException e) {
						continue;
					}
				}
			}
		}
		return null;
	}

	public static Long findDateContainsDotConvertToTime(String raw) {

		List<String> strDates = findDateWordContainsDot(raw);
		if (strDates != null && strDates.size() != 0) {
			for (String df : dateFormat) {
				for (String date : strDates) {
					SimpleDateFormat sdf = new SimpleDateFormat(df);
					try {
						return sdf.parse(date).getTime();
					} catch (ParseException e) {
						continue;
					}
				}
			}
		}
		return null;
	}

	public static String filterPunct(String raw) {
		String cleanFileid = raw.replaceAll("[\\p{P}+~$`^=|<>～｀＄＾＋＝｜＜＞￥×]", "");
		return cleanFileid;
	}

	/**
	 * 返回匹配的正则和匹配的元组
	 * 
	 * @param raw
	 * @return
	 */
	public static List<Pair<String, String>> getMatchRegexPair(String raw) {
		List<Pair<String, String>> listPair = new ArrayList<Pair<String, String>>();
		for (String regex : duplicateRegex) {
			Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
			Matcher m = pattern.matcher(raw);
			while (m.find()) {
				String v = m.group();
				if (v != null && !v.equals("")) {
					Pair<String, String> pair = new Pair<String, String>(regex, v);
					listPair.add(pair);
				}
			}
		}
		if (listPair.size() == 0) {
			return null;
		}
		return listPair;
	}

	public static String getMatchValue(String raw, String regex) {
		Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
		Matcher m = pattern.matcher(raw);
		while (m.find()) {
			String v = m.group();
			if (v != null && !v.equals("")) {
				return v;
			}
		}
		return null;
	}

	/**
	 * 判断两篇文本是否是相同形式的
	 * 
	 * @param underChecked
	 * @param candidate
	 * @return
	 */
	public static boolean judgeFormat(String underChecked, String candidate) {
		/* 通过待检查的文章获得所匹配正则，如果没有匹配到则返回false */
		List<Pair<String, String>> underCheckPairList = RegexUtils.getMatchRegexPair(underChecked);
		List<Pair<String, String>> candidatePairList = RegexUtils.getMatchRegexPair(candidate);
		if (null == underCheckPairList || null == candidatePairList) {
			return false;
		}
		if (underCheckPairList.size() != candidatePairList.size()) {
			return false;
		}
		for (int i = 0; i < underCheckPairList.size(); i++) {
			String underCheckRegex = underCheckPairList.get(i).a();
			String candidateRegex = candidatePairList.get(i).a();
			String underCheckValue = underCheckPairList.get(i).b();
			String candidateValue = candidatePairList.get(i).b();
			if(!underCheckRegex.equals(candidateRegex)) {
				return false;
			}
			if(underCheckValue.equals(candidateValue)) {
				continue;
			} else {
				return true;
			}
		}
		return false;
	}

	public static String convertURLToNewsKey(String url) {
		String s_time = url.substring(url.lastIndexOf(Contants.SLASH) + 1, url.lastIndexOf(Contants.DOT));
		if (s_time.length() == Contants.NEWS_URL_LENGTH) {
			return s_time;
		}
		if (s_time.length() == Contants.VIDEO_PIC_URL_LENGTH) {
			s_time = s_time.substring(0, s_time.length() - (Contants.VIDEO_PIC_URL_LENGTH - Contants.NEWS_URL_LENGTH));
			return s_time;
		}
		return null;
	}
	
	public static Date convertURLToDateTime(String url) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(Contants.URL_TIME_REGEX);
		String s_time = url.substring(url.lastIndexOf(Contants.SLASH) + 1, url.lastIndexOf(Contants.DOT));
		if (s_time.length() == Contants.NEWS_URL_LENGTH) {
			return sdf.parse(s_time);
		}
		if (s_time.length() == Contants.VIDEO_PIC_URL_LENGTH) {
			s_time = s_time.substring(0, s_time.length() - (Contants.VIDEO_PIC_URL_LENGTH - Contants.NEWS_URL_LENGTH));
			return sdf.parse(s_time);
		}
		return null;
	}
	
	public static  Long getTimeStamp(String url) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(Contants.URL_TIME_REGEX);
			String s_time = url.substring(url.lastIndexOf(Contants.SLASH) + 1, url.lastIndexOf(Contants.DOT));
			if (s_time.length() == Contants.NEWS_URL_LENGTH) {
				Date date = sdf.parse(s_time);
				return date.getTime();
			}
			if (s_time.length() == Contants.VIDEO_PIC_URL_LENGTH) {
				s_time = s_time.substring(0,
						s_time.length() - (Contants.VIDEO_PIC_URL_LENGTH - Contants.NEWS_URL_LENGTH));
				Date date = sdf.parse(s_time);
				return date.getTime();
			}
		} catch (ParseException exception) {
			return null;
		}
		return null;
	}
	
	public static String cleanParaAndImgLabel(String raw) {
		return raw.replaceAll("\\$#imgidx=\\d{4}#\\$", "").replaceAll("!@#!@", "");
	}
	
	public static void main(String[] args) {
		System.out.println(cleanParaAndImgLabel("$#imgidx=0001#$!@#!@ 泰中铁路示意图!@#!@ 原标题：泰国内阁批准泰中铁路合作项目曼谷"));
//		System.out.println(judgeFormat("中式台球教学A29题型", "中式台球教学A28题型"));
//		System.out.println(judgeFormat("【理臣】2017年经济法-葛江静-第七章第6-8节b", "【理臣】2017年经济法-葛江静-第五章第9节h"));
	}
}
