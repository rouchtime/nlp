package com.rouchtime.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.log4j.Logger;
import org.codehaus.plexus.util.ExceptionUtils;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Pair;
import com.rouchtime.nlp.common.News;
import com.rouchtime.nlp.common.NewsSig;
import com.rouchtime.nlp.common.Result;
import com.rouchtime.nlp.duplicate.minhash.LSHMinHash;
import com.rouchtime.nlp.duplicate.minhash.MinHash;

/**
 * 去重工具
 * 
 * @author 龚帅宾
 *
 */
public class DuplicateUtils {
	private static Logger logger = Logger.getLogger(DuplicateUtils.class);
	private ObjectToCounterMap<String> wordIndexMap;
	private LSHMinHash lshMinHash; /* lsh最小hash */
	private TreeMap<String, NewsSig> bOWMap;
	private TokenizerFactory factory;
	private int maxQueueSize; /* bOWSet的队列最大数 */
	private static final int STAGES = 10;
	private static final int BUCKETS = 10000;
	private static final double THRESHOLD = 0.5;

	/**
	 * 初始化构造器,为了维护之前的短文去重而保留，已开发新的可选择长短文本的构造器
	 * {@link #DuplicateUtils(List,TokenizerFactory,Integer,boolean)}
	 * <p>
	 * 它通过已给定的新闻列表 <code>dupNewsList</code>来进行初始化，
	 * 该初始化会将<code>dupNewsList</code>的每一个文本通过最小hash计算后，得到hash向量，并将该向量
	 * 放入一个队列中，去重时会一直维护这个队列；此构造器默认构造短文本的去重， 在去重短文本时不会讲文章的具体内容加载到队列中。
	 * </p>
	 * 
	 * @param dupNewsList
	 *            新闻列表
	 * @param factory
	 *            分词工厂
	 * @param maxQueueSize
	 *            队列最大维护数量
	 */
	public DuplicateUtils(List<News> dupNewsList, TokenizerFactory factory, int maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
		this.factory = factory;
		wordIndexMap = new ObjectToCounterMap<String>();
		bOWMap = new TreeMap<String, NewsSig>(new NewsTimeComparator());
		try {
			lshMinHash = new LSHMinHash(STAGES, BUCKETS, THRESHOLD, System.currentTimeMillis());
			initWordIndexAndHashes(dupNewsList, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>
	 * 它通过已给定的新闻列表 <code>dupNewsList</code>来进行初始化，
	 * 该初始化会将<code>dupNewsList</code>的每一个文本通过最小hash计算后，得到hash向量，并将该向量
	 * 放入一个队列中，去重时会一直维护这个队列；
	 * </p>
	 * <p>
	 * <code>isLargeFlag</code>标识位来区分长短文本，长文本位true，短文本为false；
	 * 当选择长文本去重时，为了减少队列空间，不会将长文本的原始内容保存，只保存文章的id和url；短文本都保存
	 * </p>
	 * 
	 * @param dupNewsList
	 * @param factory
	 * @param maxQueueSize
	 * @param isLargeFlag
	 */
	public DuplicateUtils(List<News> dupNewsList, TokenizerFactory factory, Integer maxQueueSize, boolean isLargeFlag) {
		this.maxQueueSize = maxQueueSize;
		this.factory = factory;
		wordIndexMap = new ObjectToCounterMap<String>();
		bOWMap = new TreeMap<String, NewsSig>(new NewsTimeComparator());
		try {
			lshMinHash = new LSHMinHash(STAGES, BUCKETS, THRESHOLD, System.currentTimeMillis());
			initWordIndexAndHashes(dupNewsList, isLargeFlag);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 此构造器为冷启动构造器，不会加载任何历史文章列表；
	 * 文本去重，当第一篇文章来查重时，会构建队列，直到队列满足<code>maxQueueSize</code> 的最大数量后，
	 * 会将最旧新闻的pop出去，push进当前新闻。
	 * <p>
	 * <code>isLargeFlag</code>标识位来区分长短文本，长文本位true，短文本为false；
	 * 当选择长文本去重时，为了减少队列空间，不会将长文本的原始内容保存，只保存文章的id和url；短文本都保存
	 * </p>
	 * 
	 * @param factory
	 *            分词工厂
	 * @param maxQueueSize
	 *            队列最大维护数量
	 * @param isLargeFlag
	 *            长短文本标志位
	 */
	public DuplicateUtils(TokenizerFactory factory, int maxQueueSize, boolean isLargeFlag) {
		this.maxQueueSize = maxQueueSize;
		this.factory = factory;
		wordIndexMap = new ObjectToCounterMap<String>();
		bOWMap = new TreeMap<String, NewsSig>(new NewsTimeComparator());
		lshMinHash = new LSHMinHash(STAGES, BUCKETS, THRESHOLD, System.currentTimeMillis());
	}

	/**
	 * 此构造器通过<code>modelPath</code>路径读入模型文件存放位置，通过已存模型来加载队列，速度更快一些。
	 * 
	 * @param modelPath
	 *            模型路径
	 * @param factory
	 *            分词工厂
	 * @param maxQueueSize
	 *            队列最大维护数量
	 */
	public DuplicateUtils(String modelPath, TokenizerFactory factory, int maxQueueSize) {
		FileInputStream fin;
		try {
			fin = new FileInputStream(new File(modelPath));
			ObjectInputStream ois = new ObjectInputStream(fin);
			readExternal(ois);
		} catch (FileNotFoundException e) {
			logger.error(ExceptionUtils.getFullStackTrace(e));
		} catch (IOException e) {
			logger.error(ExceptionUtils.getFullStackTrace(e));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			logger.error(ExceptionUtils.getFullStackTrace(e));
		}
		this.factory = factory;
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
		Pair<NewsSig, List<NewsSig>> pair = findCandidate(news, false);
		if (null == pair) {
			return null;
		}
		List<Result> resultList = findDuplicateFromCandidate(pair, sim, false);
		return resultList;
	}

	/**
	 * 长文本去重
	 * 
	 * @param news
	 * @param sim
	 * @return
	 */
	public List<Result> duplicateLong(News news, double sim) {
		Pair<NewsSig, List<NewsSig>> pair = findCandidate(news, true);
		if (null == pair) {
			return null;
		}
		List<Result> resultList = findDuplicateFromCandidate(pair, sim, true);
		return resultList;

	}

	/**
	 * 根据新闻URL删除Queue中的指定新闻
	 * 
	 * @param news
	 * @return
	 * @throws Exception
	 */
	public synchronized boolean removeFromQueue(String url) throws Exception {
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
	 * 根据指定路径，生成模型
	 * @param path
	 * @return
	 */
	public String writeModel(String path) {
		SimpleDateFormat sdf = new SimpleDateFormat(Contants.URL_TIME_REGEX);
		if (!path.endsWith("/"))
			path += "/LSHModel_";
		else 
			path += "LSHModel_";
		String modelPath = path + sdf.format(new Date()) + ".model";
		FileOutputStream fout;
		try {
			fout = new FileOutputStream(new File(modelPath));
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			writeExternal(oos);
			return modelPath;
		} catch (FileNotFoundException e) {
			logger.error(ExceptionUtils.getThrowables(e));
			return null;
		} catch (IOException e) {
			logger.error(ExceptionUtils.getThrowables(e));
			return null;
		}

	}

	/**
	 * 从候选对中找到重复文章
	 * 
	 * @param pair
	 * @param sim
	 * @return
	 */
	private List<Result> findDuplicateFromCandidate(Pair<NewsSig, List<NewsSig>> pair, double sim,
			boolean isLargeFlag) {
		List<Result> resultList = new ArrayList<Result>();
		for (NewsSig ns : pair.b()) {
			double jaccardSim = MinHash.jaccardIndex(pair.a().getVector(), ns.getVector());
			if (jaccardSim >= sim) {
				// 如果isLargeFlag为true，下个判断就不会进行
				if (isLargeFlag || !RegexUtils.judgeFormat(pair.a().getArticle(), ns.getArticle())) {
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
	 * hash值得初始化，即minhash放大后的hash
	 * 
	 * @param dupNewsList
	 */
	private void initWordIndexAndHashes(List<News> dupNewsList, boolean isLargeFlag) {
		Long startTime = System.currentTimeMillis();
		for (int i = 0; i < dupNewsList.size() && (bOWMap.keySet().size() < this.maxQueueSize); i++) {
			String article = dupNewsList.get(i).getArticle();
			Set<Integer> vector = new TreeSet<Integer>();
			for (String token : factory.tokenizer(article.toCharArray(), 0, article.length())) {
				String word = token.split(Contants.SLASH)[0];
				wordIndexMap.increment(word);
				vector.add(wordIndexMap.get(word).intValue());
			}
			int[] hash = lshMinHash.hash(vector);
			if (null == operateQueue(dupNewsList.get(i), hash, vector, isLargeFlag)) {
				continue;
			}
		}
		Long endTime = System.currentTimeMillis();
		logger.info(String.format("初始化时间：%ds", (endTime - startTime) / 1000));
	}

	/**
	 * 查找候选对
	 * @param news 待检查新闻
	 * @param isLargeFlag 长短文本标志位，长文本true，短文本为false
	 * @return 返回候选集
	 */
	private Pair<NewsSig, List<NewsSig>> findCandidate(News news, Boolean isLargeFlag) {
		Set<Integer> vector = returnVector(news);
		int[] checkHash = lshMinHash.hash(vector);
		NewsSig underCheckNewsSig = operateQueue(news, checkHash, vector, isLargeFlag);
		List<NewsSig> candidate = buildCandidateList(checkHash);
		return new Pair<NewsSig, List<NewsSig>>(underCheckNewsSig, candidate);
	}

	/**
	 * 通过新闻内容返回索引向量
	 * 
	 * @param news
	 * @return
	 */
	private Set<Integer> returnVector(News news) {
		Set<Integer> vector = new TreeSet<Integer>();
		String content = news.getArticle();
		int dicSize = wordIndexMap.keySet().size();
		for (String token : factory.tokenizer(content.toCharArray(), 0, content.length())) {
			String[] term = token.split(Contants.SLASH);
			if (term.length != 2) {
				continue;
			}
			String word = term[0];
			if (null == wordIndexMap.get(word)) {
				wordIndexMap.increment(word);
				vector.add(dicSize);
				dicSize++;
			}
			vector.add(wordIndexMap.get(word).intValue());
		}
		return vector;
	}

	/**
	 * 通过最小hash向量，建立候选集合
	 * 
	 * @param checkHash
	 * @return
	 */
	private List<NewsSig> buildCandidateList(int[] checkHash) {
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
	 * 操作队列
	 * 
	 * @param underCheckNewsSig
	 */
	private NewsSig operateQueue(News news, int[] hash, Set<Integer> vector, boolean isLargeFlag) {
		if (null == news.getUrl() || null == hash || null == vector || null == news.getId()) {
			return null;
		}
		NewsSig underCheckNewsSig = null;
		if (isLargeFlag) {
			underCheckNewsSig = new NewsSig.NewsSigBuilder(hash, vector).id(news.getId()).url(news.getUrl()).builder();
		} else {
			underCheckNewsSig = new NewsSig.NewsSigBuilder(hash, vector).news(news).builder();
		}
		/* 将新来去重的文章，加入样本集 */
		if (bOWMap.keySet().size() > this.maxQueueSize) {
			bOWMap.remove(bOWMap.firstKey());
		}
		bOWMap.put(underCheckNewsSig.getUrl(), underCheckNewsSig);
		return underCheckNewsSig;
	}

	/**
	 * 根据解析URL的时间字符串，来排序的比较器
	 * 
	 * @author Admin
	 *
	 */
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

	private void writeExternal(ObjectOutput out) throws IOException {
		Long startTime = System.currentTimeMillis();
		out.writeObject(lshMinHash);
		out.writeObject(wordIndexMap);
		out.writeObject(this.bOWMap);
		Long endTime = System.currentTimeMillis();
		logger.info(String.format("模型写入时间\t:%ds", (endTime - startTime) / 1000));
	}

	@SuppressWarnings("unchecked")
	private void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		Long startTime = System.currentTimeMillis();
		this.lshMinHash = (LSHMinHash) in.readObject();
		this.wordIndexMap = (ObjectToCounterMap<String>) in.readObject();
		this.bOWMap = (TreeMap<String, NewsSig>) in.readObject();
		Long endTime = System.currentTimeMillis();
		logger.info(String.format("模型载入时间\t:%ds", (endTime - startTime) / 1000));
	}
}
