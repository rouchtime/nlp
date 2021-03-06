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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.aliasi.symbol.MapSymbolTable;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Pair;
import com.rouchtime.nlp.duplicate.bean.DuplicateBean;
import com.rouchtime.nlp.duplicate.bean.Fingerprint;
import com.rouchtime.nlp.duplicate.bean.Result;
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
	private MapSymbolTable wordIndexMap;
	private LSHMinHash lshMinHash; /* lsh最小hash */
	private TreeMap<Long, Fingerprint> bOWMap;
	private TokenizerFactory factory;
	private int maxQueueSize; /* bOWSet的队列最大数 */
	private static final int STAGES = 10;
	private static final int BUCKETS = 10000;
	private static final double THRESHOLD = 0.5;

	private static volatile DuplicateUtils instance;

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
	public static DuplicateUtils getIstance(List<DuplicateBean> dupNewsList, TokenizerFactory factory,
			int maxQueueSize) {
		if (instance == null) {
			synchronized (DuplicateUtils.class) {
				if (instance == null) {
					instance = new DuplicateUtils(dupNewsList, factory, maxQueueSize);
				}
			}
		}
		return instance;
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
	public static DuplicateUtils getIstance(List<DuplicateBean> dupNewsList, TokenizerFactory factory,
			Integer maxQueueSize, boolean isLargeFlag) {
		if (instance == null) {
			synchronized (DuplicateUtils.class) {
				if (instance == null) {
					instance = new DuplicateUtils(dupNewsList, factory, maxQueueSize, isLargeFlag);
				}
			}
		}
		return instance;
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
	public static DuplicateUtils getIstance(TokenizerFactory factory, int maxQueueSize, boolean isLargeFlag) {
		if (instance == null) {
			synchronized (DuplicateUtils.class) {
				if (instance == null) {
					instance = new DuplicateUtils(factory, maxQueueSize, isLargeFlag);
				}
			}
		}
		return instance;
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
	public static DuplicateUtils getIstance(String modelPath, TokenizerFactory factory, int maxQueueSize) {
		if (instance == null) {
			synchronized (DuplicateUtils.class) {
				if (instance == null) {
					instance = new DuplicateUtils(modelPath, factory, maxQueueSize);
				}
			}
		}
		return instance;
	}

	/**
	 * 短文本去重
	 * 
	 * @param news
	 * @param sim
	 * @return
	 * @throws IOException
	 */
	public List<Result> duplicateShort(DuplicateBean duplicateBean, double sim) throws IOException {
		Pair<Fingerprint, List<Fingerprint>> pair = findCandidate(duplicateBean, false);
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
	public List<Result> duplicateLong(DuplicateBean duplicateBean, double sim) {
		Pair<Fingerprint, List<Fingerprint>> pair = findCandidate(duplicateBean, true);
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
	public synchronized boolean removeFromQueue(Long url) throws Exception {
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
	 * 
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
	private List<Result> findDuplicateFromCandidate(Pair<Fingerprint, List<Fingerprint>> pair, double sim,
			boolean isLargeFlag) {
		List<Result> resultList = new ArrayList<Result>();
		for (Fingerprint ns : pair.b()) {
			double jaccardSim = MinHash.jaccardIndex(pair.a().getVector(), ns.getVector());
			if (jaccardSim >= sim) {
				// 如果isLargeFlag为true，下个判断就不会进行
				if (isLargeFlag) {
					DuplicateBean dupBean = new DuplicateBean();
					dupBean.setId(ns.getId());
					Result result = new Result();
					result.setDuplicateBean(dupBean);
					result.setSimilariy(jaccardSim);
					resultList.add(result);
				} else {
					if (!RegexUtils.judgeFormat(pair.a().getRaw(), ns.getRaw())) {
						DuplicateBean dupBean = new DuplicateBean();
						dupBean.setId(ns.getId());
						Result result = new Result();
						result.setDuplicateBean(dupBean);
						result.setSimilariy(jaccardSim);
						resultList.add(result);
					}
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
	private void initWordIndexAndHashes(List<DuplicateBean> dupNewsList, boolean isLargeFlag) {
		Long startTime = System.currentTimeMillis();
		for (int i = 0; i < dupNewsList.size() && (bOWMap.keySet().size() < this.maxQueueSize); i++) {
			String article = dupNewsList.get(i).getRaw();
			Set<Integer> vector = new TreeSet<Integer>();
			for (String token : factory.tokenizer(article.toCharArray(), 0, article.length())) {
				String word = token.split(Contants.SLASH)[0];
				vector.add(wordIndexMap.getOrAddSymbolInteger(word));
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
	 * 
	 * @param news
	 *            待检查新闻
	 * @param isLargeFlag
	 *            长短文本标志位，长文本true，短文本为false
	 * @return 返回候选集
	 */
	private Pair<Fingerprint, List<Fingerprint>> findCandidate(DuplicateBean duplicateBean, Boolean isLargeFlag) {
		Set<Integer> vector = returnVector(duplicateBean);
		int[] checkHash = lshMinHash.hash(vector);
		List<Fingerprint> candidate = buildCandidateList(checkHash);
		Fingerprint underCheckNewsSig = operateQueue(duplicateBean, checkHash, vector, isLargeFlag);
		return new Pair<Fingerprint, List<Fingerprint>>(underCheckNewsSig, candidate);
	}

	/**
	 * 通过新闻内容返回索引向量
	 * 
	 * @param news
	 * @return
	 */
	private Set<Integer> returnVector(DuplicateBean duplicateBean) {
		Set<Integer> vector = new TreeSet<Integer>();
		String content = duplicateBean.getRaw();
		for (String token : factory.tokenizer(content.toCharArray(), 0, content.length())) {
			String word = "";
			if (token.indexOf(Contants.SLASH) != -1) {
				String[] term = token.split(Contants.SLASH);
				if (term.length != 2) {
					continue;
				}
				word = term[0];
			} else {
				word = token;
			}
			vector.add(wordIndexMap.getOrAddSymbolInteger(word));
		}
		return vector;
	}

	/**
	 * 通过最小hash向量，建立候选集合
	 * 
	 * @param checkHash
	 * @return
	 */
	private List<Fingerprint> buildCandidateList(int[] checkHash) {
		List<Fingerprint> candidate = new ArrayList<Fingerprint>();
		for (Entry<Long, Fingerprint> entry : bOWMap.entrySet()) {
			Fingerprint ns = entry.getValue();
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
	private Fingerprint operateQueue(DuplicateBean duplicateBean, int[] hash, Set<Integer> vector,
			boolean isLargeFlag) {
		if (null == hash || null == vector || null == duplicateBean.getId()) {
			return null;
		}
		Fingerprint underCheckFp = null;
		if (isLargeFlag) {
			underCheckFp = new Fingerprint.FingerprintBuilder(hash, vector).id(duplicateBean.getId())
					.timeStamp(duplicateBean.getTimestamp()).builder();
		} else {
			underCheckFp = new Fingerprint.FingerprintBuilder(hash, vector).id(duplicateBean.getId())
					.timeStamp(duplicateBean.getTimestamp()).raw(duplicateBean.getRaw()).builder();
		}
		/* 将新来去重的文章，加入样本集 */
		if (bOWMap.keySet().size() > this.maxQueueSize) {
			bOWMap.remove(bOWMap.firstKey());
		}
		bOWMap.put(underCheckFp.getTimestamp(), underCheckFp);
		return underCheckFp;
	}

	/**
	 * 根据解析URL的时间字符串，来排序的比较器
	 * 
	 * @author Admin
	 *
	 */
	class NewsTimeComparator implements Comparator<Long> {

		@Override
		public int compare(Long timestamp1, Long timestamp2) {
			if (timestamp1 == null || timestamp2 == null) {
				throw new NullPointerException("Comparing timestamp is Null!");
			}
			return timestamp1.compareTo(timestamp2);
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
		this.wordIndexMap = (MapSymbolTable) in.readObject();
		this.bOWMap = (TreeMap<Long, Fingerprint>) in.readObject();
		Long endTime = System.currentTimeMillis();
		logger.info(String.format("模型载入时间\t:%ds", (endTime - startTime) / 1000));
	}

	private DuplicateUtils(List<DuplicateBean> dupNewsList, TokenizerFactory factory, int maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
		this.factory = factory;
		wordIndexMap = new MapSymbolTable();
		bOWMap = new TreeMap<Long, Fingerprint>(new NewsTimeComparator());
		try {
			lshMinHash = new LSHMinHash(STAGES, BUCKETS, THRESHOLD, System.currentTimeMillis());
			initWordIndexAndHashes(dupNewsList, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private DuplicateUtils(List<DuplicateBean> dupNewsList, TokenizerFactory factory, Integer maxQueueSize,
			boolean isLargeFlag) {
		this.maxQueueSize = maxQueueSize;
		this.factory = factory;
		wordIndexMap = new MapSymbolTable();
		bOWMap = new TreeMap<Long, Fingerprint>(new NewsTimeComparator());
		try {
			lshMinHash = new LSHMinHash(STAGES, BUCKETS, THRESHOLD, System.currentTimeMillis());
			initWordIndexAndHashes(dupNewsList, isLargeFlag);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private DuplicateUtils(TokenizerFactory factory, int maxQueueSize, boolean isLargeFlag) {
		this.maxQueueSize = maxQueueSize;
		this.factory = factory;
		wordIndexMap = new MapSymbolTable();
		bOWMap = new TreeMap<Long, Fingerprint>(new NewsTimeComparator());
		lshMinHash = new LSHMinHash(STAGES, BUCKETS, THRESHOLD, System.currentTimeMillis());
	}

	private DuplicateUtils(String modelPath, TokenizerFactory factory, int maxQueueSize) {
		FileInputStream fin;
		try {
			fin = new FileInputStream(new File(modelPath));
			ObjectInputStream ois = new ObjectInputStream(fin);
			readExternal(ois);
		} catch (FileNotFoundException e) {
			logger.error(ExceptionUtils.getRootCauseMessage(e));
		} catch (IOException e) {
			logger.error(ExceptionUtils.getRootCauseMessage(e));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			logger.error(ExceptionUtils.getRootCauseMessage(e));
		}
		this.factory = factory;
	}
}
