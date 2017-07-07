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

import com.aliasi.tokenizer.TokenizerFactory;
import com.alibaba.fastjson.JSONObject;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

public final class RealOrNotRealNewsCorpus implements ICorpus {

	private TokenizerFactory tokenizerFactory;
	private static String url = "D://corpus//realTimeOrNotNews//news_nonnews_json";
	private static Map<String, String> titleToRawNewsMap = new HashMap<String, String>();
	private static Map<String, String> titleToLableMap = new HashMap<String, String>();
	private static Map<String, List<String>> labelToTitleMap = new HashMap<String, List<String>>();
	private static Map<String, String> titleTocategoryMap = new HashMap<String, String>();
	private static Map<String, List<String>> categoryToTitle = new HashMap<String, List<String>>();
	private static Map<String,String> titleToURLMap = new HashMap<String,String>();
	private static Set<String> labels = new HashSet<String>();
	private static Set<String> categories = new HashSet<String>();
	static {
		try {
			List<String> newsList = FileUtils.readLines(new File(url), "utf-8");
			for (String news : newsList) {
				try {
					String title = JSONObject.parseObject(news).getString("title");
					String lable = JSONObject.parseObject(news).getString("label");
					String category = JSONObject.parseObject(news).getString("category");
					String url = JSONObject.parseObject(news).getString("url");
					titleToRawNewsMap.put(title, news);
					titleToLableMap.put(title, lable);
					titleTocategoryMap.put(title, category);
					titleToURLMap.put(title, url);
					labels.add(lable);
					categories.add(category);

					if (labelToTitleMap.get(lable) != null) {
						List<String> titles = labelToTitleMap.get(lable);
						titles.add(title);
					} else {
						List<String> titles = new ArrayList<String>();
						labelToTitleMap.put(lable, titles);
					}

					if (categoryToTitle.get(category) != null) {
						List<String> titles = categoryToTitle.get(category);
						titles.add(title);
					} else {
						List<String> titles = new ArrayList<String>();
						categoryToTitle.put(category, titles);
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

	@Override
	public List<String> fileids() throws Exception {
		List<String> fileids = new ArrayList<String>(titleToRawNewsMap.keySet());
		return fileids;
	}

	public List<String> fileidsFromLabel(String label) {
		List<String> filedis = labelToTitleMap.get(label);
		return filedis;
	}

	public List<String> fileidsFromCategory(String category) {
		List<String> filedis = categoryToTitle.get(category);
		return filedis;
	}

	@Override
	public List<String> words(String fileid) {
		List<String> words = new ArrayList<String>();
		String news = String.valueOf(titleToRawNewsMap.get(fileid));
		String article = JSONObject.parseObject(news).getString("article");
		for (Term term : HanLP.segment(article)) {
			words.add(term.word);
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

	public Set<String> categories() {
		return categories;
	}

	public String category(String fileid) {
		return titleTocategoryMap.get(fileid);
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
	
>>>>>>> branch 'master' of https://github.com/rouchtime/nlp.git
}
