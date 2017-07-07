package corpus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.alibaba.fastjson.JSONObject;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

import tokenizer.HanLPTokenizerFactory;

public class FinanceNewsOrNonCorpus implements ICorpus {

	
	private TokenizerFactory tokenizerFactory;
	public FinanceNewsOrNonCorpus(TokenizerFactory tokenizerFactory) {
		this.tokenizerFactory = tokenizerFactory;
	}
	/*
	 * 默认为HanLP分词器
	 */
	public FinanceNewsOrNonCorpus() {
		this.tokenizerFactory =  HanLPTokenizerFactory.getIstance();
	}
	@Override
	public List<String> fileids() throws Exception {
		List<String> fileids = new ArrayList<String>(titleToRawNewsMap.keySet());
		return fileids;
	}

	public List<String> fileidsFromLabel(String label) {
		List<String> filedis = labelToTitleMap.get(label);
		return filedis;
	}

	@Override
	public List<String> words(String fileid) {
		List<String> words = new ArrayList<String>();
		String news = String.valueOf(titleToRawNewsMap.get(fileid));
		String article = JSONObject.parseObject(news).getString("article");
		Tokenizer tokenzier = tokenizerFactory.tokenizer(article.toCharArray(), 0, article.length());
		for (String token:tokenzier) {
			words.add(token.split("/")[0]);
		}
		return words;
	}

	@Override
	public List<String> sents(String fileid) {
		List<String> sents = new ArrayList<String>();
		String news = String.valueOf(titleToRawNewsMap.get(fileid));
		String article = JSONObject.parseObject(news).getString("article");
		String[] sentences = article.split("！|。|？|；");
		for (String sentence : sentences) {
			sents.add(sentence);
		}
		return sents;
	}

	public Set<String> labels() {
		return labels;
	}

	public String label(String fileid) {
		return titleToLableMap.get(fileid);
	}

	@Override
	public String raws(String fileid) {
		String news = String.valueOf(titleToRawNewsMap.get(fileid));
		String article = JSONObject.parseObject(news).getString("article");
		return article;
	}

	@Override
	public String path() {
		return url;
	}

	public String url(String fileid) {
		return titleToURLMap.get(fileid);
	}
	public int picCount(String fileid) {
		JSONObject jsonObject = JSONObject.parseObject(titleToRawNewsMap.get(fileid));
		return jsonObject.getInteger("imgSize");
	}
	public int paraCount(String fileid) {
		JSONObject jsonObject = JSONObject.parseObject(titleToRawNewsMap.get(fileid));
		return jsonObject.getInteger("paraSize");
	}
	
	private static String url = "D://corpus//realTimeOrNotNews//isnews_caijing.json";
	private static Map<String, String> titleToRawNewsMap = new HashMap<String, String>();
	private static Map<String, String> titleToLableMap = new HashMap<String, String>();
	private static Map<String, List<String>> labelToTitleMap = new HashMap<String, List<String>>();
	private static Map<String, String> titleToURLMap = new HashMap<String, String>();
	private static Set<String> labels = new HashSet<String>();

	static {
		try {
			List<String> newsList = FileUtils.readLines(new File(url), "utf-8");
			for (String news : newsList) {
				try {
					String title = JSONObject.parseObject(news).getString("title");
					String lable = JSONObject.parseObject(news).getString("label");
					String url = JSONObject.parseObject(news).getString("url");
					titleToRawNewsMap.put(title, news);
					titleToLableMap.put(title, lable);
					titleToURLMap.put(title, url);
					labels.add(lable);
					if (labelToTitleMap.get(lable) != null) {
						List<String> titles = labelToTitleMap.get(lable);
						titles.add(title);
					} else {
						List<String> titles = new ArrayList<String>();
						labelToTitleMap.put(lable, titles);
					}
				} catch (Exception e) {
					System.out.println(ExceptionUtils.getRootCauseMessage(e) + ":" + news);
					continue;
				}
			}
		} catch (IOException e) {
			System.out.println(ExceptionUtils.getRootCauseMessage(e));
			e.printStackTrace();
		}
	}
}
