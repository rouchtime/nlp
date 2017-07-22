package corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.aliasi.tokenizer.TokenizerFactory;
import com.alibaba.fastjson.JSONObject;

import pojo.News;
import tokenizer.HanLPTokenizerFactory;

public final class RealOrNotRealNewsCorpus extends BaseCorpus {

	private Map<String, String> titleTocategoryMap;
	private Map<String, List<String>> categoryToTitle;
	private Set<String> categories;

	public RealOrNotRealNewsCorpus(TokenizerFactory tokenizerFactory, String path, double trainRate)
			throws IOException {
		super(path, tokenizerFactory, trainRate);
		this.tokenizerFactory = tokenizerFactory;
	}

	public RealOrNotRealNewsCorpus(String path) throws IOException {
		super(path, HanLPTokenizerFactory.getIstance(), 0.9);
	}

	public List<String> fileidsFromCategory(String category) {
		List<String> filedis = categoryToTitle.get(category);
		return filedis;
	}

	public Set<String> categories() {
		return categories;
	}

	public String category(String fileid) {
		return titleTocategoryMap.get(fileid);
	}

	@Override
	protected void createCorpusToRAM() {
		titleTocategoryMap = new HashMap<String, String>();
		categoryToTitle = new HashMap<String, List<String>>();
		categories = new HashSet<String>();
		try {
			BufferedReader reader = new BufferedReader(
					(new InputStreamReader(new FileInputStream(new File(path)), Charsets.toCharset("utf-8"))));
			String line = reader.readLine();
			while (line != null) {
				String title = JSONObject.parseObject(line).getString("title");
				String article = JSONObject.parseObject(line).getString("article");
				String label = JSONObject.parseObject(line).getString("label");
				String category = JSONObject.parseObject(line).getString("category");
				String url = JSONObject.parseObject(line).getString("url");
				News news = new News();
				news.setArticle(article);
				news.setCategory(category);
				news.setLabel(label);
				news.setUrl(url);
				news.setTitle(title);
				newsMap_key_title.put(title, news);
				titleTocategoryMap.put(title, category);
				labels.add(label);
				categories.add(category);
				if (labelToNewsMap.get(label) != null) {
					List<News> titles = labelToNewsMap.get(label);
					titles.add(news);
				} else {
					List<News> newses = new ArrayList<News>();
					labelToNewsMap.put(label, newses);
				}
				if (categoryToTitle.get(category) != null) {
					List<String> titles = categoryToTitle.get(category);
					titles.add(title);
				} else {
					List<String> titles = new ArrayList<String>();
					categoryToTitle.put(category, titles);
				}
				line = reader.readLine();
			}
		} catch (Exception e) {
			ExceptionUtils.getRootCauseMessage(e);
		}
	}

	public static void main(String[] args) throws Exception {
		BaseCorpus corpus = new FinanceNewsOrNonCorpus("D://corpus//isnews_caijing.json");
		System.out.println(corpus.fileids().size());
	}
}
