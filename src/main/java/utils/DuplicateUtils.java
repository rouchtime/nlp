package utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Pair;

import duplicate.minhash.LSHMinHash;
import duplicate.minhash.MinHash;
import duplicate.minhash.NewsSig;
import pojo.News;
import pojo.Result;

public class DuplicateUtils {
	private Map<String, Integer> wordIndexMap;
	private LSHMinHash lshMinHash; /* lsh最小hash */
	private LinkedList<NewsSig> bOWList; /* 去重队列 */
	private TokenizerFactory factory;
	private Integer dicSize;/* 字典词数 */
	private int maxQueueSize; /* bOWList的队列最大数 */
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
		bOWList = new LinkedList<NewsSig>();
		wordIndexMap = new HashMap<String, Integer>();
		try {
			lshMinHash = new LSHMinHash(STAGES, BUCKETS, THRESHOLD, System.currentTimeMillis());
			initWordIndexAndHashes(dupNewsList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 查重短文本函数
	 * 
	 * @param id
	 *            文章id
	 * @param content
	 *            文章内容
	 * @param sim
	 *            相似度阈值
	 * @return 返回相似文本
	 * @throws IOException
	 */
	public List<Result> duplicateShort(String id, String content, double sim) throws IOException {
		Pair<NewsSig, List<NewsSig>> pair = findCandidate(id, content);
		List<Result> resultList = new ArrayList<Result>();
		for (NewsSig ns : pair.b()) {
			double jaccardSim = MinHash.jaccardIndex(pair.a().getVector(), ns.getVector());
			if (jaccardSim >= sim) {
				if (!RegexUtils.judgeFormat(pair.a().getArticle(), ns.getArticle())) {
					Result result = new Result();
					result.setId(ns.getId());
					result.setSimilariy(jaccardSim);
					resultList.add(result);
				}
			}
		}
		return resultList;
	}

	/**
	 * 排重长文本
	 * 
	 * @param id
	 * @param content
	 * @param sim
	 * @return
	 * @throws IOException
	 */
	public List<Result> duplicateLong(String id, String content, double sim) throws IOException {
		Pair<NewsSig, List<NewsSig>> pair = findCandidate(id, content);
		List<Result> resultList = new ArrayList<Result>();
		for (NewsSig ns : pair.b()) {
			double jaccardSim = MinHash.jaccardIndex(pair.a().getVector(), ns.getVector());
			if (jaccardSim >= sim) {
				Result result = new Result();
				result.setId(ns.getId());
				result.setSimilariy(jaccardSim);
				resultList.add(result);
			}
		}
		return resultList;
	}

	/**
	 * hash值得初始化，即minhash放大后的hash
	 * 
	 * @param dupNewsList
	 */
	private void initWordIndexAndHashes(List<News> dupNewsList) {
		long startTime = System.currentTimeMillis();
		wordIndexMap = new HashMap<String, Integer>();
		for (int i = 0; i < dupNewsList.size() && bOWList.size() < this.maxQueueSize; i++) {
			String article = dupNewsList.get(i).getArticle();
			String id = dupNewsList.get(i).getId();
			Integer wordIndex = 0;
			Set<Integer> vector = new TreeSet<Integer>();
			for (String token : factory.tokenizer(article.toCharArray(), 0, article.length())) {
				String word = token.split("/")[0];
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
			bOWList.offer(newsSig);
		}
		long endTime = System.currentTimeMillis();
		System.out.println(String.format("计算hash时间:%d", (endTime - startTime) / 1000));
	}

	/**
	 * 获得候选对
	 * 
	 * @param id
	 * @param content
	 * @return Pair 二元组对，第一个为参数为待查重文章，第二个List为去重候选对
	 */
	private Pair<NewsSig, List<NewsSig>> findCandidate(String id, String content) {
		Set<Integer> vector = new TreeSet<Integer>();
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
		for (NewsSig ns : bOWList) {
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

		/* 上线时再打开注释，目前，在样本集中测试时不打开 */
		if (bOWList.size() > this.maxQueueSize) {
			bOWList.poll();
		} 
		bOWList.offer(underCheckNewsSig);
		return new Pair<NewsSig, List<NewsSig>>(underCheckNewsSig, candidate);
	}

}
