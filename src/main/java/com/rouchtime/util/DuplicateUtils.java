package com.rouchtime.util;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Pair;
import com.rouchtime.nlp.common.News;
import com.rouchtime.nlp.common.NewsSig;
import com.rouchtime.nlp.common.Result;
import com.rouchtime.nlp.duplicate.minhash.LSHMinHash;
import com.rouchtime.nlp.duplicate.minhash.MinHash;

public class DuplicateUtils {
	private Map<String, Integer> wordIndexMap;
	private LSHMinHash lshMinHash; /* lsh最小hash */
	private TreeMap<String, NewsSig> bOWMap;
	private TokenizerFactory factory;
	private Integer dicSize;/* 字典词数 */
	private int maxQueueSize; /* bOWSet的队列最大数 */
	private static final int STAGES = 10;
	private static final int BUCKETS = 10000;
	private static final double THRESHOLD = 0.5;

	/**
	 * 初始化
	 * 
	 * @param dupNewsList
	 *            初始化时放入的去重集合,输入id，和内容
	 * @param factory
	 *            分词工厂
	 */
	public DuplicateUtils(List<News> dupNewsList, TokenizerFactory factory, int maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
		this.factory = factory;
		wordIndexMap = new HashMap<String, Integer>();
		bOWMap = new TreeMap<String, NewsSig>(new NewsTimeComparator());
		try {
			lshMinHash = new LSHMinHash(STAGES, BUCKETS, THRESHOLD, System.currentTimeMillis());
			initWordIndexAndHashes(dupNewsList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 短文本去重
	 * 
	 * @param news
	 * @param sim
	 * @return
	 * @throws IOException
	 */
	public List<Result> duplicateShort(News news, double sim) throws IOException {
		Pair<NewsSig, List<NewsSig>> pair = findCandidate(news);
		List<Result> resultList = new ArrayList<Result>();
		for (NewsSig ns : pair.b()) {
			double jaccardSim = MinHash.jaccardIndex(pair.a().getVector(), ns.getVector());
			if (jaccardSim >= sim) {
				if (!RegexUtils.judgeFormat(pair.a().getArticle(), ns.getArticle())) {
					News dupNews = new News();
					dupNews.setUrl(ns.getUrl());
					dupNews.setId(ns.getId());
					Result result = new Result();

					result.setNews(dupNews);
					result.setSimilariy(jaccardSim);
					resultList.add(result);
				}
			}
		}
		return resultList;
	}

	/**
	 * 长文本去重
	 * 
	 * @param news
	 * @param sim
	 * @return
	 * @throws IOException
	 */
	public List<Result> duplicateLong(News news, double sim) throws IOException {
		Pair<NewsSig, List<NewsSig>> pair = findCandidate(news);
		List<Result> resultList = new ArrayList<Result>();
		for (NewsSig ns : pair.b()) {
			double jaccardSim = MinHash.jaccardIndex(pair.a().getVector(), ns.getVector());
			if (jaccardSim >= sim) {
				News dupNews = new News();
				dupNews.setUrl(ns.getUrl());
				dupNews.setId(ns.getId());
				Result result = new Result();

				result.setNews(dupNews);
				result.setSimilariy(jaccardSim);
				resultList.add(result);
			}
		}
		return resultList;
	}

	/**
	 * 根据新闻URL删除Queue中的指定新闻
	 * 
	 * @param news
	 * @return
	 * @throws Exception
	 */
	public boolean removeFromQueue(String url) throws Exception {
		if (null == url) {
			throw new Exception("News's url is NULL!");
		}
		if (bOWMap.keySet().size() == 0) {
			throw new Exception("The Queue size is 0!");
		}
		try {
			if (bOWMap.remove(url) != null) {
				return true;
			}
		} catch (Exception e) {
			throw e;
		}
		return false;
	}

	/**
	 * hash值得初始化，即minhash放大后的hash
	 * 
	 * @param dupNewsList
	 */
	private void initWordIndexAndHashes(List<News> dupNewsList) {
		Long startTime = System.currentTimeMillis();
		Integer wordIndex = 0;
		for (int i = 0; i < dupNewsList.size() && bOWMap.keySet().size() < this.maxQueueSize; i++) {
			String article = dupNewsList.get(i).getArticle();
			String id = dupNewsList.get(i).getId();
			String url = dupNewsList.get(i).getUrl();
			Set<Integer> vector = new TreeSet<Integer>();
			for (String token : factory.tokenizer(article.toCharArray(), 0, article.length())) {
				String word = token.split(Contants.SLASH)[0];
				if (wordIndexMap.get(word) == null) {
					wordIndexMap.put(word, wordIndex++);
					this.dicSize = wordIndex;
				}
				vector.add(wordIndexMap.get(word));
			}
			int[] hash = lshMinHash.hash(vector);
			NewsSig newsSig = new NewsSig();
			newsSig.setId(id);
			newsSig.setHash(hash);
			newsSig.setArticle(article);
			newsSig.setVector(vector);
			newsSig.setUrl(url);
			/* 存队列，记录队列索引 */
			try {
				bOWMap.put(url, newsSig);
			}
			catch(Exception e) {
				continue;
			}
		}
		this.dicSize = wordIndex;
		Long endTime = System.currentTimeMillis();
		System.out.println("初始化时间:"+ (endTime - startTime)+"ms");
	}

	/**
	 * 获得候选对
	 * 
	 * @param id
	 * @param content
	 * @return Pair 二元组对，第一个为参数为待查重文章，第二个List为去重候选对
	 */
	private Pair<NewsSig, List<NewsSig>> findCandidate(News news) {
		Set<Integer> vector = new TreeSet<Integer>();
		String content = news.getArticle();
		String url = news.getUrl();
		String id = news.getId();

		for (String token : factory.tokenizer(content.toCharArray(), 0, content.length())) {
			String word = token.split("/")[0];
			if (null == wordIndexMap.get(word)) {
				wordIndexMap.put(word, this.dicSize);
				vector.add(dicSize);
				dicSize++;
			}
			vector.add(wordIndexMap.get(word));
		}
		int[] checkHash = lshMinHash.hash(vector);
		/* 查找候选集 */
		List<NewsSig> candidate = new ArrayList<NewsSig>();
		for (Entry<String, NewsSig> entry : bOWMap.entrySet()) {
			NewsSig ns = entry.getValue();
			int[] hash1 = ns.getHash();
			for (int stage = 0; stage < STAGES; stage++) {
				if (hash1[stage] == checkHash[stage]) {
					candidate.add(ns);
					break;
				}
			}
		}
		NewsSig underCheckNewsSig = new NewsSig();
		underCheckNewsSig.setVector(vector);
		underCheckNewsSig.setId(id);
		underCheckNewsSig.setHash(checkHash);
		underCheckNewsSig.setArticle(content);
		underCheckNewsSig.setUrl(url);

		/* 上线时再打开注释，目前，在样本集中测试时不打开 */
		if (bOWMap.keySet().size() > this.maxQueueSize) {
			bOWMap.remove(bOWMap.firstKey());
		}
		bOWMap.put(url, underCheckNewsSig);
		return new Pair<NewsSig, List<NewsSig>>(underCheckNewsSig, candidate);
	}

	class NewsTimeComparator implements Comparator<String> {

		@Override
		public int compare(String url1, String url2) {
			if (url1 == null || url2 == null) {
				throw new NullPointerException("Comparing url is Null!");
			}
			Long ltime1 = getTimeStamp(url1);
			Long ltime2 = getTimeStamp(url2);
			if (ltime1 == null || ltime2 == null) {
				throw new NullPointerException("Time parse Exception!");
			}

			return ltime1.compareTo(ltime2);
		}

		private Long getTimeStamp(String url) {
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

	}
}
