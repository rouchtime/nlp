package corpus;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.alibaba.fastjson.JSONObject;

public abstract class BaseCorpus implements ICorpus {
	protected Map<String, String> titleToRawNewsMap;
	protected Map<String, String> titleToLableMap;
	protected Map<String, List<String>> labelToTitleMap;
	protected Map<String, String> titleToURLMap;
	protected Set<String> labels;
	protected TokenizerFactory tokenizerFactory;
	protected String path;

	public BaseCorpus(String path, TokenizerFactory factory) throws IOException {
		this.path = path;
		titleToRawNewsMap = new HashMap<String, String>();
		titleToLableMap = new HashMap<String, String>();
		labelToTitleMap = new HashMap<String, List<String>>();
		titleToURLMap = new HashMap<String, String>();
		labels = new HashSet<String>();
		this.tokenizerFactory = factory;
		/* 将语料放入内存中 */
		createCorpusToRAM();
	}

	@Override
	public List<String> fileids() throws Exception {
		List<String> fileids = new ArrayList<String>(titleToRawNewsMap.keySet());
		return fileids;
	}

	@Override
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
		for (String token : tokenzier) {
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

	@Override
	public Set<String> labels() {
		return labels;
	}

	@Override
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
		return path;
	}

	@Override
	public String url(String fileid) {
		return titleToURLMap.get(fileid);
	}

	@Override
	public int picCount(String fileid) {
		JSONObject jsonObject = JSONObject.parseObject(titleToRawNewsMap.get(fileid));
		return jsonObject.getInteger("imgSize");
	}

	@Override
	public int paraCount(String fileid) {
		JSONObject jsonObject = JSONObject.parseObject(titleToRawNewsMap.get(fileid));
		return jsonObject.getInteger("paraSize");
	}

	protected abstract void createCorpusToRAM();
}
