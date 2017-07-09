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

import tokenizer.HanLPTokenizerFactory;

public final class RealOrNotRealNewsCorpus extends BaseCorpus {

	private Map<String, String> titleTocategoryMap;
	private Map<String, List<String>> categoryToTitle;
	private Set<String> categories;

	public RealOrNotRealNewsCorpus(TokenizerFactory tokenizerFactory, String path) throws IOException {
		super(path, tokenizerFactory);
		this.tokenizerFactory = tokenizerFactory;
	}

	public RealOrNotRealNewsCorpus(String path) throws IOException {
		super(path, HanLPTokenizerFactory.getIstance());
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
		titleTocategoryMap = new HashMap<String,String>();
		categoryToTitle = new HashMap<String,List<String>>();
		categories = new HashSet<String>();
		try {
			BufferedReader reader = new BufferedReader(
					(new InputStreamReader(new FileInputStream(new File(path)), Charsets.toCharset("utf-8"))));
			String line = reader.readLine();
			while (line != null) {
				String title = JSONObject.parseObject(line).getString("title");
				String lable = JSONObject.parseObject(line).getString("label");
				String category = JSONObject.parseObject(line).getString("category");
				String url = JSONObject.parseObject(line).getString("url");
				titleToRawNewsMap.put(title, line);
				titleToLableMap.put(title, lable);
				titleToURLMap.put(title, url);
				titleTocategoryMap.put(title, category);
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
