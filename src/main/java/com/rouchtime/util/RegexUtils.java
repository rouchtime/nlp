package com.rouchtime.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import com.aliasi.util.Pair;
import com.rouchtime.nlp.common.Term;

import tokenizer.HanLPTokenizerFactory;
import tokenizer.NGramTokenizerBasedOtherTokenizerFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

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
			duplicateRegex = IOUtils.readLines(RegexUtils.class.getResourceAsStream("/nlpdic/duplicate_regex.txt"),
					"utf-8");
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

	public static Boolean isQQorWeiXinNum(String raw) {
		String regex = "(?:[a-zA-Z0-9](?:[^a-zA-Z0-9\\u4e00-\\u9fa5]*)){6,}";
		Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
		Matcher m = pattern.matcher(raw);
		return m.find(0);
	}

	public static Boolean isQQorWeiXinNumWithOutAlphNum(String raw) {
		String regex = "(?:[a-zA-Z0-9](?:[^a-zA-Z0-9\\u4e00-\\u9fa5]*)){6,}";
		Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
		Matcher m = pattern.matcher(raw);
		while (m.find()) {
			String special = m.group();
			if (special != null) {
				if (!isNumber(special)) {
					return false;
				} else if (!isAlphabet(special)) {
					return false;
				} else {
					return true;
				}
			}
		}
		return false;
	}

	public static String cleanSequenceLetterOrNum(String raw) {
		String regex = "(?:[a-zA-Z0-9](?:[^a-zA-Z0-9\\u4e00-\\u9fa5]*)){6,}";
		return raw.replaceAll(regex, "");
	}

	/**
	 * 找到qq或微信的特殊符号
	 * 
	 * @param raw
	 * @return
	 */
	public static String findQQorWeiXinNum(String raw) {
		String regex = "(?:[\\-_a-zA-Z0-9](?:[^\\-_a-zA-Z0-9\\u4e00-\\u9fa5]*)){6,}";
		Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
		Matcher m = pattern.matcher(raw);
		while (m.find()) {
			String date = m.group();
			if (date != null) {
				return date.replaceAll("[^-_a-zA-Z0-9]", "");
			}
		}
		return null;
	}

	/**
	 * 是否包含qq或者微信时，中间是否有特殊符号分割
	 * 
	 * @param raw
	 * @return
	 */
	public static Boolean isContantsSpecialSignalWithQQOrWeiXin(String raw) {
		String regex = "(?:[\\-_a-zA-Z0-9](?:[^\\-_a-zA-Z0-9\\u4e00-\\u9fa5]{1,})){6,}";
		Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
		Matcher m = pattern.matcher(raw);
		return m.find(0);
	}

	public static Boolean isSpamWord(String raw) {
		Set<String> mStopSet = new HashSet<String>();
		InputStream is = RegexUtils.class.getResourceAsStream("/commentfilter/spam_word.txt");
		mStopSet = readFromFileNames(is);
		for (String word : mStopSet) {
			if (raw.indexOf(word) != -1) {
				return true;
			}
		}
		return false;
	}

	public static double spamWordRatio(Set<String> wordsets) {
		Set<String> result = new HashSet<String>();
		Set<String> mStopSet = new HashSet<String>();
		InputStream is = RegexUtils.class.getResourceAsStream("/commentfilter/spam_word_v2.txt");
		mStopSet = readFromFileNames(is);
		result.clear();
		result.addAll(wordsets);
		result.retainAll(mStopSet);
		return (result.size() * 1.0) / (wordsets.size() * 1.0);
	}

	public static Set<String> readFromFileNames(InputStream is) {
		BufferedReader br = null;
		Set<String> set = new HashSet<String>();
		try {
			br = new BufferedReader(new InputStreamReader(is, "utf-8"));
			String s = null;
			while ((s = br.readLine()) != null) {
				set.add(s);
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
			}
		}
		return set;
	}

	/**
	 * 是否匹配特殊的数字字符，如①②③④⑤⑥⑥⑦⑧⑨一二三四五六七八九 壹贰叁肆伍陆柒捌玖拾
	 * 
	 * @param raw
	 * @return
	 */
	public static Boolean isSpecialNumberSignal(String raw) {
		String regex = "[❶❷❸❹❺❻❼❽❾㈠㈡㈢㈣㈤㈥㈦㈧㈨ⅠⅡⅢⅣⅤⅥⅦⅧⅨ①②③④⑤⑥⑥⑦⑧⑨零一二三四五六七八九 壹贰叁肆伍陆柒捌玖拾]{6,}";
		Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
		Matcher m = pattern.matcher(raw);
		return m.find(0);
	}

	public static Boolean isSexWord(String raw) {
		String regex = "[阳痿早泄]{3,}";
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
		String regex = "\\s+|　+|&nbsp+|#+| +|[\\u0000]+|(?:\\r)+|(?:\\n)+|[\\u2003]+";
		return text.replaceAll(regex, "");
	}

	public static String chineseOnly(String text) {
		String regex = "[^\\u4e00-\\u9fa5]";
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
			if (!underCheckRegex.equals(candidateRegex)) {
				return false;
			}
			if (underCheckValue.equals(candidateValue)) {
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

	public static Long getTimeStamp(String url) {
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

	public static String cleanImgLabel(String raw) {
		return raw.replaceAll("\\$#imgidx=\\d{4}#\\$", "");
	}
	
	public static String toSemiangle(String src) {
		char[] c = src.toCharArray();
		for (int index = 0; index < c.length; index++) {
			if (c[index] == 12288) {// 全角空格
				c[index] = (char) 32;
			} else if (c[index] > 65280 && c[index] < 65375) {// 其他全角字符
				c[index] = (char) (c[index] - 65248);
			}
		}
		return String.valueOf(c);
	}

	public static int countpPunct(String source) {
		Pattern pattern = Pattern.compile("\\pP", Pattern.CANON_EQ);
		Matcher m = pattern.matcher(source);
		int count = 0;
		while (m.find()) {
			String v = m.group();
			if (v != null && !v.equals("")) {
				count++;
			}
		}
		return count;
	}

	public static int countNoChineseNoNumberNoEnglish(String source) {
		Pattern pattern = Pattern.compile("[^\\u4e00-\\u9fa5\\pPa-zA-Z0-9\\pZ]", Pattern.CANON_EQ);
		Matcher m = pattern.matcher(source);
		int count = 0;
		while (m.find()) {
			String v = m.group();
			if (v != null && !v.equals("")) {
				count++;
			}
		}
		return count;
	}

	public static String replacePunct(String raw) {
		return raw.replaceAll("\\pP", "");
	}

	/********************************摘要相关正则********************************/
	
	public static String removeReportHead(String match) {
		return match.replaceAll("据?.*[网|社|记者].{0,5}(?:\\d*月\\d*日)?.{0,10}(?:电|讯|报道称|报道)，?", "");
	}
	
	/**
	 * 分句
	 * @param document
	 * @return
	 */
	public static List<String> spiltSentence(String document,String paraLabel) {
		List<String> sentences = new ArrayList<String>();
		for (String line : document.split(paraLabel)) {
			if (line.equals("")) {
				continue;
			}
			line = RegexUtils.cleanSpecialWord(line.trim());
			if (line.length() == 0)
				continue;
			String regexQuot = "“(?:.+?)”";
			Pattern pQuot = Pattern.compile(regexQuot);
			Matcher mQuot = pQuot.matcher(line);
			int sentenceStart = 0;
			int sentenceEnd = 0;
			while(mQuot.find()) {
				String quotSents = mQuot.group();
				sentenceEnd = mQuot.start();
				List<String> subSentences = split(line.substring(sentenceStart,sentenceEnd));
				sentenceStart = mQuot.end();
				sentences.addAll(subSentences);
				sentences.add(quotSents);
			}
			sentenceEnd = line.length();
			sentences.addAll(split(line.substring(sentenceStart,sentenceEnd)));
		}
		return sentences;
	}

	public static List<String> split(String document) {
		List<String> sentences = new ArrayList<String>();
		String regex = "[。？?！!；;]";
		Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
		Matcher m = pattern.matcher(document);
		/* 按照句子结束符分割句子 */
		String[] sents = pattern.split(document);
		/* 将句子结束符连接到相应的句子后 */
		if (sents.length > 0) {
			int count = 0;
			while (count < sents.length) {
				if (m.find()) {
					String e = m.group();
					sents[count] += m.group();
				}
				count++;
			}
		}
		for (String sent : sents) {
			if(String.valueOf(sent).equals("null")) {
				continue;
			}
			sentences.add(sent);
		}
		return sentences;
	}
	
	/******************************** 评论相关正则 *******************************/

	public static String findSpecialNumberSignal(String raw) {
		String regex = "[❶❷❸❹❺❻❼❽❾㈠㈡㈢㈣㈤㈥㈦㈧㈨ⅠⅡⅢⅣⅤⅥⅦⅧⅨ①②③④⑤⑥⑥⑦⑧⑨⒈⒉⒊⒋⒌⒍⒎⒏⒐⑴⑵⑶⑷⑸⑹⑺⑻⑼零一二三四五六七八九 壹贰叁肆伍陆柒捌玖拾]{6,}";
		Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
		Matcher m = pattern.matcher(raw);
		while (m.find()) {
			String date = m.group();
			if (date != null) {
				return date;
			}
		}
		return null;
	}

	/**
	 * 移除除了字母和数字的数
	 * 
	 * @param match
	 * @return
	 */
	public static String removeNonNumAndAlpha(String match) {
		return match.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "");
	}

	/**
	 * 找到电话号码
	 * 
	 * @param raw
	 * @return
	 */
	public static String findCellPhoneNum(String raw) {
		String regex = "(?:0|86|17951)?(?:(?:13[0-9])|(?:14[5|7])|(?:15(?:[0-3]|[5-9]))|(?:18[0,5-9]))\\d{8}";
		Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
		Matcher m = pattern.matcher(raw);
		while (m.find()) {
			String capture = m.group();
			if (capture != null) {
				return capture;
			}
		}
		return null;
	}

	/**
	 * 找到qq号码
	 * 
	 * @param raw
	 * @return
	 */
	public static String findQQNum(String raw) {
		if (raw.length() > 10) {
			return null;
		}
		String regex = "[1-9][0-9]{4,9}";
		Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
		Matcher m = pattern.matcher(raw);
		while (m.find()) {
			String capture = m.group();
			if (capture != null) {
				return capture;
			}
		}
		return null;
	}

	public static String findWeiXin(String raw) {
		if (raw.length() > 20 || raw.length() < 6) {
			return null;
		}
		String regex = "(?:[a-zA-Z0-9]){6,}";
		Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
		Matcher m = pattern.matcher(raw);
		while (m.find()) {
			String capture = m.group();
			if (capture != null) {
				return capture;
			}
		}
		return null;
	}

	public static String findRMBPrice(String raw) {
		String regex = "(?:\\d*)(?:\\s*)元";
		Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
		Matcher m = pattern.matcher(raw);
		while (m.find()) {
			String capture = m.group();
			if (capture != null) {
				return capture;
			}
		}
		return null;
	}

	/**
	 * 是否存在数字
	 * 
	 * @param raw
	 * @return
	 */
	public static Boolean isNumber(String raw) {
		String regex = "[0-9]+";
		Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
		Matcher m = pattern.matcher(raw);
		return m.find(0);
	}

	/**
	 * 是否存在字母
	 * 
	 * @param raw
	 * @return
	 */
	public static Boolean isAlphabet(String raw) {
		String regex = "[a-zA-Z]+";
		Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
		Matcher m = pattern.matcher(raw);
		return m.find(0);
	}

	public static void main(String[] args) {
		String testStr = "!@#!@ 互联网对传统新闻造成的冲击终于让美国报业坐不住了。 !@#!@ 7月10日，美国新闻媒体联盟（News Media Alliance）向美国国会申诉，要求美国国会修改《反垄断法》，以更好地应对谷歌和脸书两家互联网巨头对传统媒体造成的冲击。 !@#!@ 美国新闻媒体联盟发端于美国报业联盟，其中包含《华盛顿邮报》、《华尔街日报》、《纽约时报》等知名美国传统媒体和众多规模不大的媒体，总数近2000家。 !@#!@$#imgidx=0001#$!@#!@美国新闻媒体联盟10日发布的文章截图!@#!@维权不易：报业如想维权需先改法律!@#!@ 在美国新闻媒体联盟网站上公布的这一号召称，消费者对即时、可靠消息的需求日益增长，但是目前，互联网的分配系统却将优秀新闻报道的经济价值分配进行扭曲。由于在数字时代谷歌和脸书的双垄断局面，新闻媒体被迫在内容上做出让步，并按照数字媒体的规则运行。但是这些规则一方面将新闻商品化，另一方面也增加了假新闻出现的风险，而在目前的体系中，区分真假新闻是有一定困难的。 !@#!@ 据美国CNBC电视台报道，根据皮尤研究中心的数据，谷歌和脸书现在基本上占据了美国总额达730亿美元的互联网广告中的70%。然而美国报业去年的广告收入仅有180亿美元，但是10年前，报业广告有500亿美元之多。 !@#!@$#imgidx=0002#$!@#!@由于广告收入大幅下降，《纽约时报》不得不将其总部大楼部分房间出租以赚取租金（社交媒体截图）!@#!@ 不过，想要维权也不并不容易。美国《反垄断法》的初衷是降低垄断性企业对社会的伤害。但是，美国新闻媒体联盟称，在媒体行业上，现存法律却无意地阻止了新闻媒体联合起来在谈判中获得有利地位，以让对民主制度至关重要的新闻媒体能够可持续地为人们服务。 !@#!@ 在面对一个几乎将传统媒体逼入绝路、占尽广告收入资源的双垄断互联网媒体环境时，新闻媒体在与互联网媒体谈判时没有任何主动权。 !@#!@为了民主，支持传统新闻业!@#!@ “立法机构如能允许新闻媒体集体进行谈判，将会对今天的媒体行业的健康及高质量发展提供可能。”新闻媒体联盟的主席大卫·查文（David Chavern）说，“高质量的新闻业是保障民主的重要部分，也是公民社会的核心。为了让这样的新闻业能够有未来，新闻媒体发现必须集体同互联网媒体平台进行谈判。” !@#!@ 美国新闻媒体联盟还称，除了在媒体行业的主导地位，脸书和谷歌无法在其信源和能力上保证新闻报道的真实性。脸书在去年的美国大选中就因为其没有对新闻内容的真实性进行审查而遭到公众质疑。 !@#!@ “脸书和谷歌并不雇佣记者，他们不会通过公开信息去发掘腐败丑闻，也不会派驻战地记者，甚至不会派人去体育比赛现场带来最新报道。但是，他们却榨取了整个新闻行业的经济效益，而所有花钱的事情却都是我们来做的。”查文说，“唯一的维权方式就是大家拧成一股绳。” !@#!@ “如果我们最终能和脸书、谷歌谈判后达成更有利的知识产权保护协议以及更公平的收入分配体系，新闻业才会可持续发展。” !@#!@$#imgidx=0003#$!@#!@互联网给传统报业带来巨大冲击，2016年，英国《独立报》推出最后一期后，停止纸版报纸发行，全面改为互联网媒体（社交媒体截图）!@#!@ 本文系观察者网独家稿件，文章内容纯属作者个人观点，不代表平台观点，未经授权，不得转载，否则将追究法律责任。关注观察者网微信guanchacn，每日阅读趣味文章。";
		testStr = RegexUtils.cleanImgLabel(testStr);
		List<String> sents = spiltSentence(testStr,"!@#!@");
		for(int i=0;i<sents.size();i++) {
			System.out.println(sents.get(i));
		}
	}

}
