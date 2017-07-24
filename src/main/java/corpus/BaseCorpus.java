package corpus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.Handler;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.alibaba.fastjson.JSONObject;

import pojo.News;

public abstract class BaseCorpus implements ICorpus {
	protected Set<String> labels;
	protected TokenizerFactory tokenizerFactory;
	protected Set<News> trainSet;
	protected Set<News> testSet;
	protected Map<String,News> titleNewsMap;
	protected Map<String,News> idNewsMap;
	protected Map<String,List<News>> labelNewsMap;
	protected String path;

	public BaseCorpus(String path, TokenizerFactory factory, double trainRate) throws IOException {
		this.path = path;
		trainSet = new HashSet<News>();
		testSet = new HashSet<News>();
		titleNewsMap = new HashMap<String,News>();
		labelNewsMap = new HashMap<String, List<News>>(); 
		idNewsMap = new HashMap<String,News>();
		labels = new HashSet<String>();
		this.tokenizerFactory = factory;
		/* 将语料放入内存中 */
		createCorpusToRAM();
		createTrainAndTestCorpus(trainRate);
	}

	protected abstract void createTrainAndTestCorpus(double trainRate);

	@Override
	public List<String> fileids() throws Exception {
		List<String> fileids = new ArrayList<String>(titleNewsMap.keySet());
		return fileids;
	}

	@Override
	public List<News> newsFromLabel(String label) {
		List<News> filedis = labelNewsMap.get(label);
		return filedis;
	}

	@Override
	public List<String> words(String fileid) {

		List<String> words = new ArrayList<String>();
		try {
			String news = String.valueOf(titleNewsMap.get(fileid).getArticle());
			if (news.equals("null") || news.equals("")) {
				return null;
			}
			String article = news;
			Tokenizer tokenzier = tokenizerFactory.tokenizer(article.toCharArray(), 0, article.length());
			for (String token : tokenzier) {
				words.add(token.split("/")[0]);
			}
		} catch (Exception e) {
			return null;
		}
		return words;
	}

	@Override
	public List<String> sents(String fileid) {
		List<String> sents = new ArrayList<String>();
		String news = String.valueOf(titleNewsMap.get(fileid).getArticle());
		try {
			if (news.equals("null") || news.equals("")) {
				return null;
			}
			String article = news;
			String[] sentences = article.split("！|。|？|；");
			for (String sentence : sentences) {
				sents.add(sentence);
			}
		} catch (Exception e) {
			System.err.println("错误语料：\t\t\t" + news);
			return null;
		}
		return sents;
	}

	@Override
	public Set<String> labels() {
		return labels;
	}

	@Override
	public String label(String fileid) {
		return titleNewsMap.get(fileid).getLabel();
	}

	@Override
	public String raws(String fileid) {
		String news = String.valueOf(titleNewsMap.get(fileid).getArticle());
		return news;
	}

	@Override
	public String path() {
		return path;
	}

	@Override
	public String url(String fileid) {
		return titleNewsMap.get(fileid).getUrl();
	}

	@Override
	public int picCount(String fileid) {
		return titleNewsMap.get(fileid).getPicSize();
	}

	@Override
	public int paraCount(String fileid) {
		return titleNewsMap.get(fileid).getPageSize();
	}

	public Set<News> train(){
		return trainSet;
	} 
	
	public Set<News> test(){
		return testSet;
	}
	
	protected abstract void createCorpusToRAM();
}
