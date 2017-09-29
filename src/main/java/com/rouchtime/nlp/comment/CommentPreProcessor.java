package com.rouchtime.nlp.comment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.aliasi.tokenizer.TokenizerFactory;
import com.rouchtime.nlp.common.Term;
import com.rouchtime.util.RegexUtils;

import tokenizer.HanLPTokenizerFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

public class CommentPreProcessor {

	private Map<String, String> SPECIALNUMMAP;
	private TokenizerFactory TOKENNIZERFACTORY;
	private String regexSequenceNumBySplitWords = "(?:[a-zA-Z0-9一二三四五六七八九](?:[^一二三四五六七八九a-zA-Z0-9\\u4e00-\\u9fa5]+)){6,}";
	private String regexSequenceNumByNoSpliteWord = "(?:[a-zA-Z0-9]){6,}";
	private String regexSequenceNumAndAhpla = "(?:[a-zA-Z0-9一二三四五六七八九](?:[^一二三四五六七八九a-zA-Z0-9\\u4e00-\\u9fa5]*)){6,}";
	private String regexRMBPrice = "(?:\\d*)(?:\\s*)元";

	private CommentPreProcessor() {
		CommentDataLoad data = CommentDataLoad.getInstance();
		SPECIALNUMMAP = data.getSPECIALNUMMAP();
		TOKENNIZERFACTORY = getTokenFactory();
	}

	public List<Term> getCommentListTerm(String raw) {
		List<Term> terms = new ArrayList<Term>();
		/* 清除评论换行符 */
		raw = raw.replaceAll("@!#n!@", ",");
		/* 特殊数字字符替换成阿拉伯数字 */
		raw = replaceSpecialNumberToNormal(raw);
		raw = recognizeSequenceNumAndReturnRemain(regexSequenceNumBySplitWords, raw, terms);
		raw = recognizeSequenceNumAndReturnRemain(regexSequenceNumByNoSpliteWord, raw, terms);
		raw = recognizeSequenceNumAndReturnRemain(regexSequenceNumAndAhpla, raw, terms);
		raw = recognizeSequenceNumAndReturnRemain(regexRMBPrice, raw, terms);
		return terms;
	}

	public static CommentPreProcessor getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {
		private static CommentPreProcessor instance = new CommentPreProcessor();
	}

	private TokenizerFactory getTokenFactory() {
		StopWordTokenierFactory stopWordFactory = new StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		StopNatureTokenizerFactory stopNatureTokenizerFactory = new StopNatureTokenizerFactory(stopWordFactory);
		return stopNatureTokenizerFactory;
	}

	/**
	 * 将特殊数字符号替换成阿拉伯数字
	 * 
	 * @param raw
	 * @param configMap
	 * @return
	 */
	private String replaceSpecialNumberToNormal(String raw) {
		String regex = "[㈠㈡㈢㈣㈤㈥㈦㈧㈨ⅠⅡⅢⅣⅤⅥⅦⅧⅨ①②③④⑤⑥⑥⑦⑧⑨⒈⒉⒊⒋⒌⒍⒎⒏⒐⑴⑵⑶⑷⑸⑹⑺⑻⑼零一二三四五六七八九 壹贰叁肆伍陆柒捌玖拾]{6,}";
		Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
		Matcher m = pattern.matcher(raw);
		StringBuffer sb = new StringBuffer(raw);
		while (m.find()) {
			String capture = m.group();
			StringBuffer subSb = new StringBuffer();
			if (capture != null) {
				for (int i = 0; i < capture.length(); i++) {
					String key = capture.substring(i, i + 1);
					String value = SPECIALNUMMAP.get(key);
					if (value == null) {
						continue;
					}
					subSb.append(value);
				}
				if (subSb.toString().equals("")) {
					continue;
				}
				sb.replace(m.start(), m.end(), subSb.toString());
			}
		}
		return sb.toString();
	}

	/**
	 * 识别指定regex的实体，并返回去除实体后的文本，并把相应实体存在list中
	 * 
	 * @param regex
	 * @param raw
	 * @param list
	 * @return
	 */
	private String recognizeSequenceNumAndReturnRemain(String regex, String raw, List<Term> list) {
		Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
		Matcher m = pattern.matcher(raw);
		int regexFlag = 0;
		StringBuffer sb = new StringBuffer(raw);
		int dis = 0;
		while (m.find()) {
			String capture = m.group();
			if (capture != null) {
				String entity = subJudge(RegexUtils.removeNonNumAndAlpha(capture));
				if (null == entity) {
					continue;
				}
				Term term = new Term(entity, "nz");
				list.add(term);
				int start = m.start() - dis;
				int end = m.end() - dis;
				sb.delete(start, end);
				dis += end - start;
				regexFlag++;
			}
		}
		if (regexFlag != 0) {
			return sb.toString();
		}
		return sb.toString();
	}

	private String subJudge(String match) {
		String symbol = "";
		if (RegexUtils.findCellPhoneNum(match) != null) {
			symbol = "手机号标识符%s";
			return symbol;
		}
		if (RegexUtils.findQQNum(match) != null) {
			symbol = "QQ号标识符";
			return symbol;
		}
		if (RegexUtils.findWeiXin(match) != null) {
			symbol = "微信标识符";
			return symbol;
		}
		if (RegexUtils.findRMBPrice(match) != null) {
			symbol = "人民币价格";
			return symbol;
		}
		return null;
	}
	public static void main(String[] args) throws IOException {
		CommentPreProcessor preProcessor = CommentPreProcessor.getInstance();
		for(String line : FileUtils.readLines(new File("D:\\comment\\08_comment_article\\SPAM"),"utf-8")){
			List<Term> terms = preProcessor.getCommentListTerm(line.split("\t")[0]);
			FileUtils.write(new File("D:\\comment\\08_comment_article\\weixin"), terms + "\t" + line.split("\t")[0]+"\n","utf-8",true);
		}
//		String text = "西水股份就是在她的文章中看到的，老粉丝了，他的位號是 g  k  83 2 83，真正的好人是有好报的，分享给大家，共勉！";
//		List<Term> terms = preProcessor.getCommentListTerm(text);
//		System.out.println(terms);
		
	}
}
