package com.rouchtime.util;

import java.io.BufferedReader;
import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
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

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.codehaus.plexus.util.ExceptionUtils;

import com.aliasi.classify.TradNaiveBayesClassifier;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Pair;
import com.alibaba.fastjson.JSONObject;
import com.rouchtime.nlp.common.News;
import com.rouchtime.nlp.common.NewsSig;
import com.rouchtime.nlp.common.Result;
import com.rouchtime.nlp.duplicate.minhash.LSHMinHash;
import com.rouchtime.nlp.duplicate.minhash.MinHash;

import tokenizer.HanLPTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

public class DuplicateUtils implements Externalizable {
	private static Logger logger = Logger.getLogger(DuplicateUtils.class);
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

	public DuplicateUtils(String modelPath, TokenizerFactory factory, int maxQueueSize) {

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
	 * 根据模型来去重
	 * 
	 * @param path
	 *            模型存放地址
	 * @return
	 */
	public List<Result> duplicateLong(String path, News news, double sim) {
		FileInputStream fin;
		try {
			fin = new FileInputStream(new File(path));
			ObjectInputStream ois = new ObjectInputStream(fin);
			readExternal(ois);
		} catch (FileNotFoundException e) {
			logger.error(ExceptionUtils.getFullStackTrace(e));
			return null;
		} catch (IOException e) {
			logger.error(ExceptionUtils.getFullStackTrace(e));
			return null;
		} catch (ClassNotFoundException e) {
			logger.error(ExceptionUtils.getFullStackTrace(e));
			return null;
		}
		Pair<NewsSig, List<NewsSig>> pair = findCandidate(news, true);
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

	private static File file;

	@SuppressWarnings({ "unchecked", "resource" })
	public static void readModel(TokenizerFactory factory, File file) throws IOException, ClassNotFoundException {
		Long readModelStart = System.currentTimeMillis();
		FileInputStream fin = new FileInputStream(
				new File("C:\\Users\\Admin\\AppData\\Local\\Temp\\lshModel5445222580386401134.ser"));
		ObjectInputStream ois = new ObjectInputStream(fin);
		LSHMinHash saved_lsh = (LSHMinHash) ois.readObject();
		List<NewsSig> listNewsSig = (ArrayList<NewsSig>) ois.readObject();
		Map<String, Integer> wordDicIndex = (HashMap<String, Integer>) ois.readObject();
		Long endModel = System.currentTimeMillis();
		System.out.println("Read Model:" + (endModel - readModelStart));

		/***********************************************************************/
		int wordIndex = wordDicIndex.keySet().size();
		int dupCount = 1;

		BufferedReader reader = new BufferedReader(
				(new InputStreamReader(new FileInputStream(file), Charsets.toCharset("utf-8"))));
		String line = reader.readLine();
		int totalIndex = 1;
		while (line != null) {
			if (totalIndex % 1000 == 0) {
				System.out.println(totalIndex);
			}
			if (totalIndex > 80000) {
				break;
			}
			JSONObject jsonObject = JSONObject.parseObject(line);
			String content = jsonObject.getString("content");
			String newsKey = jsonObject.getString("newsKey");
			String url = jsonObject.getString("url");
			String title = jsonObject.getString("title");
			if (content.matches("\\s+") || newsKey.equals("")) {
				line = reader.readLine();
				continue;
			}
			// Long dupStartTime = System.currentTimeMillis();
			Set<Integer> vector = new TreeSet<Integer>();
			for (String token : factory.tokenizer(content.toCharArray(), 0, content.length())) {
				String word = token.split("/")[0];
				if (null == wordDicIndex.get(word)) {
					wordDicIndex.put(word, wordIndex);
					vector.add(wordIndex);
					wordIndex++;
				}
				vector.add(wordDicIndex.get(word));
			}
			int[] checkHash = saved_lsh.hash(vector);
			List<NewsSig> candidate = new ArrayList<NewsSig>();
			for (NewsSig ns : listNewsSig) {
				if (ns.getId().equals(newsKey)) {
					continue;
				}
				int[] hash1 = ns.getHash();
				for (int stage = 0; stage < STAGES; stage++) {
					if (hash1[stage] == checkHash[stage]) {
						candidate.add(ns);
						break;
					}
				}
			}

			List<JSONObject> jsonList = new ArrayList<JSONObject>();
			for (NewsSig candidateNews : candidate) {
				double jaccardSim = MinHash.jaccardIndex(vector, candidateNews.getVector());
				if (jaccardSim >= 0.7 && jaccardSim <= 0.8) {
					JSONObject printJsonObject = new JSONObject();
					printJsonObject.put("url", candidateNews.getUrl());
					printJsonObject.put("sim", jaccardSim);
					printJsonObject.put("title", candidateNews.getTitle());
					jsonList.add(printJsonObject);
				}
			}
			totalIndex++;
			// Long dupEndTime = System.currentTimeMillis();
			// System.out.println("DupTime:"+(dupEndTime-dupStartTime));
			if (jsonList.size() < 1) {
				line = reader.readLine();
				continue;
			} else {
				FileUtils.write(new File("D:\\corpus\\duplicate\\long_dup_result87"),
						dupCount + ":\t\t" + title + "\t\t" + url + "\n", "utf-8", true);
				for (JSONObject j : jsonList) {
					FileUtils.write(new File("D:\\corpus\\duplicate\\long_dup_result87"), j.toJSONString() + "\n",
							"utf-8", true);
				}

			}
			// Long endStart = System.currentTimeMillis();
			// System.out.println("Duplicate Time:"+ (endStart- duStart));
			FileUtils.write(new File("D:\\corpus\\duplicate\\long_dup_result87"),
					"===========================================\n", "utf-8", true);
			dupCount++;
			line = reader.readLine();
		}
		// for (NewsSig newsSig : listNewsSig) {
		// Long duStart = System.currentTimeMillis();
		// Set<Integer> vector = new TreeSet<Integer>();
		// for (String token : factory.tokenizer(newsSig.getArticle().toCharArray(), 0,
		// content.length())) {
		// String word = token.split("/")[0];
		// if (null == wordDicIndex.get(word)) {
		// wordDicIndex.put(word, wordIndex);
		// vector.add(wordIndex);
		// wordIndex++;
		// }
		// vector.add(wordDicIndex.get(word));
		// }
		// int[] checkHash = saved_lsh.hash(vector);
		// int[] checkHash = newsSig.getHash();
		// List<NewsSig> candidate = new ArrayList<NewsSig>();
		// for (NewsSig ns : listNewsSig) {
		// if (ns.getId().equals(newsSig.getId())) {
		// continue;
		// }
		// int[] hash1 = ns.getHash();
		// for (int stage = 0; stage < STAGES; stage++) {
		// if (hash1[stage] == checkHash[stage]) {
		// candidate.add(ns);
		// break;
		// }
		// }
		// }
		//
		// List<JSONObject> jsonList = new ArrayList<JSONObject>();
		// for (NewsSig candidateNews : candidate) {
		// double jaccardSim = MinHash.jaccardIndex(newsSig.getVector(),
		// candidateNews.getVector());
		// if (jaccardSim >= 0.6) {
		// JSONObject jsonObject = new JSONObject();
		// jsonObject.put("url", candidateNews.getUrl());
		// jsonObject.put("sim", jaccardSim);
		// jsonObject.put("title", candidateNews.getTitle());
		// jsonList.add(jsonObject);
		// }
		// }
		// if (jsonList.size() < 1) {
		// continue;
		// } else {
		// FileUtils.write(new File("D:\\corpus\\duplicate\\long_dup_result"),
		// dupCount + ":\t\t" + newsSig.getTitle() + "\t\t" + newsSig.getUrl() + "\n",
		// "utf-8", true);
		// for (JSONObject jsonObject : jsonList) {
		// FileUtils.write(new File("D:\\corpus\\duplicate\\long_dup_result"),
		// jsonObject.toJSONString() + "\n", "utf-8",
		// true);
		// }
		//
		// }
		// // Long endStart = System.currentTimeMillis();
		// // System.out.println("Duplicate Time:"+ (endStart- duStart));
		// FileUtils.write(new File("D:\\corpus\\duplicate\\long_dup_result"),
		// "===========================================\n",
		// "utf-8", true);
		// dupCount++;
		// }
		reader.close();
	}

	/**
	 */
	public static void writeModelFromJSON(File file, TokenizerFactory factory) throws IOException {
		File tempfile = File.createTempFile("lshModel", ".ser");
		System.out.println(tempfile.getAbsolutePath());
		FileOutputStream fout = new FileOutputStream(tempfile);
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		BufferedReader reader = new BufferedReader(
				(new InputStreamReader(new FileInputStream(file), Charsets.toCharset("utf-8"))));
		LSHMinHash lshMinHash = new LSHMinHash(STAGES, BUCKETS, THRESHOLD, System.currentTimeMillis());
		Map<String, Integer> wordDicIndex = new HashMap<String, Integer>();
		int wordIndex = 0;
		// 序列化最小哈希
		oos.writeObject(lshMinHash);
		// 序列化分词工厂
		// oos.writeObject(factory);
		String line = reader.readLine();
		List<NewsSig> listNewsSig = new ArrayList<NewsSig>();
		int totalIndex = 0;
		while (line != null) {
			if (totalIndex > 80000) {
				break;
			}
			JSONObject jsonObject = JSONObject.parseObject(line);
			String content = jsonObject.getString("content");
			String newsKey = jsonObject.getString("newsKey");
			String url = jsonObject.getString("url");
			String title = jsonObject.getString("title");
			if (content.matches("\\s+") || newsKey.equals("")) {
				line = reader.readLine();
				continue;
			}
			Set<Integer> vector = new TreeSet<Integer>();
			for (String token : factory.tokenizer(content.toCharArray(), 0, content.length())) {
				String word = token.split(Contants.SLASH)[0];
				if (wordDicIndex.get(word) == null) {
					wordDicIndex.put(word, wordIndex++);
				}
				vector.add(wordDicIndex.get(word));
			}
			int[] hash = lshMinHash.hash(vector);
			NewsSig newsSig = new NewsSig();
			newsSig.setId(newsKey);
			newsSig.setHash(hash);
			newsSig.setVector(vector);
			newsSig.setUrl(url);
			newsSig.setTitle(title);
			listNewsSig.add(newsSig);
			totalIndex++;
			line = reader.readLine();
		}

		// 序列化集合数量
		oos.writeObject(listNewsSig);
		oos.writeObject(wordDicIndex);
		reader.close();
		oos.close();
	}

	public void readLSHAndHASHMatrix() throws IOException, ClassNotFoundException {
		FileInputStream fin = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fin);
		LSHMinHash saved_lsh = (LSHMinHash) ois.readObject();
		System.out.println(saved_lsh);
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		File file = new File("D:\\corpus\\duplicate\\duplicate_clean_json_version2");
		StopWordTokenierFactory stopFactory = new StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		readModel(stopFactory, file);
		// File file = new File("D:\\corpus\\duplicate\\duplicate_clean_json_version2");
		// StopWordTokenierFactory stopFactory = new
		// StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		// try {
		// writeModelFromJSON(file, stopFactory);
		// } catch (IOException e) {
		// System.out.println(ExceptionUtils.getFullStackTrace(e));
		// }
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
			} catch (Exception e) {
				continue;
			}
		}
		this.dicSize = wordIndex;
		Long endTime = System.currentTimeMillis();
		System.out.println("初始化时间:" + (endTime - startTime) + "ms");
	}

	private Set<Integer> returnVector(News news) {
		Set<Integer> vector = new TreeSet<Integer>();
		String content = news.getArticle();
		int dicSize = wordIndexMap.keySet().size();
		for (String token : factory.tokenizer(content.toCharArray(), 0, content.length())) {
			String word = token.split("/")[0];
			if (null == wordIndexMap.get(word)) {
				wordIndexMap.put(word, this.dicSize);
				vector.add(dicSize);
				dicSize++;
			}
			vector.add(wordIndexMap.get(word));
		}
		return vector;
	}

	private List<NewsSig> buildCandidate(int[] checkHash) {
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
		return candidate;
	}

	/**
	 * 获得候选对
	 * 
	 * @param id
	 * @param content
	 * @return Pair 二元组对，第一个为参数为待查重文章，第二个List为去重候选对
	 */
	private Pair<NewsSig, List<NewsSig>> findCandidate(News news) {
		Set<Integer> vector = returnVector(news);
		int[] checkHash = lshMinHash.hash(vector);
		/* 查找候选集 */
		NewsSig underCheckNewsSig = new NewsSig(checkHash, vector, news);
		/* 将新来去重的文章，加入样本集 */
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

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(lshMinHash);
		out.writeObject(wordIndexMap);
		out.writeObject(this.bOWMap);

	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.lshMinHash = (LSHMinHash) in.readObject();
		this.wordIndexMap = (Map<String, Integer>) in.readObject();
		this.bOWMap = (TreeMap<String, NewsSig>) in.readObject();
	}
}
