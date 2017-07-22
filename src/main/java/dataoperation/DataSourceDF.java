package dataoperation;

import com.aliasi.util.ObjectToDoubleMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import corpus.ICorpus;
import pojo.News;

import java.io.IOException;
import java.time.Clock;
import java.util.*;

/**
 * Created by py on 16-9-21.
 */
public class DataSourceDF extends DataSource {

	// 整个语料库下，词的文档频率
	// private Map<String, Double> wordDF;

	// 每种类别的文档频率
	// private Map<String, Double> labelDF;

	// 整个语料库下，词的文档频率
	private ObjectToDoubleMap<String> wordDF;

	private ObjectToDoubleMap<String> labelDF;

	// private ObjectToSet<K, M> label_word_df;
	// 每种类别下，词的文档频率
	private Table<String, String, Double> label_word_df;

	private DataSourceDF() throws IOException {
		
	}

	@Override
	protected boolean resetImpl(ICorpus corpus) {

		wordDF = new ObjectToDoubleMap<String>();
		labelDF = new ObjectToDoubleMap<String>();
		label_word_df = HashBasedTable.create();
		return false;
	}

	@Override
	public boolean load(ICorpus corpus) throws IOException {
		long stime = Clock.systemDefaultZone().millis();
		System.out.print("load datasourceDF from " + corpus.path() + "...");

		// 得到各个类文件夹的path
		Set<String> labels = corpus.labels();
		for (String cp : labels) { // 遍历每个类别文件夹，读取文档
			String label = cp; // 类别名称
			List<News> newsList = corpus.newsFromLabel(label);
			// 修改该类别的文档频率
			labelDF.increment(label, newsList.size());
			for (News news : newsList) {// 遍历所有的文档
				// 记录在文档中已经出现的词
				Set<String> appearedWordIndoc = new HashSet<>();
				List<String> words = corpus.words(news.getTitle());
				for (String word : words) {
					if (appearedWordIndoc.contains(word)) {
						wordDF.increment(word, 1);
						addToMap(label_word_df, label, word, 1);
						appearedWordIndoc.add(word);
					}
				}
			}
		}

		System.out.println(" using " + (Clock.systemDefaultZone().millis() - stime) * 0.5 / 1000);
		return true;
	}

	// 得到词典
	@Override
	public Set<String> getDictionary() {
		return wordDF.keySet();
	}

	// 得到所有类别标识
	@Override
	public Set<String> getLabels() {
		return labelDF.keySet();
	}

	// 类别数
	@Override
	public int getLabelCn() {
		return labelDF.size();
	}

	// 得到词典的大小
	@Override
	public int getDicSize() {
		return getDictionary().size();
	}

	// 文档数
	@Override
	public double getDocCn(boolean useSlow) {
		Double sum = 0.0;
		for (String label : labelDF.keySet()) {
			sum += labelDF.get(label);
		}
		return sum;
	}

	// 得到单词在某个类中的文档频率
	public double getWordDF(String label, String word) {
		if (label_word_df.contains(label, word))
			return label_word_df.get(label, word);
		else
			return 0;
	}

	// 得到每个类别的文档频率
	public double getLabelDF(String label) {
		if (labelDF.containsKey(label))
			return labelDF.get(label);
		else
			return 0;
	}

	// 得到在整个语料库中每个词的文档频率, useSlow=true表示不使用缓存的wordDF映射
	// TODO: 16-9-20 测试不缓存和缓存之间的性能差异
	public double getWordDF(String word, boolean useSlow) {
		if (useSlow && label_word_df.containsColumn(word)) {
			Double sum = 0.0;
			Map<String, Double> categoryMap = label_word_df.column(word);
			for (String label : categoryMap.keySet()) {
				sum += categoryMap.get(label);
			}
			return sum;
		}

		else if (!useSlow && wordDF.containsKey(word))
			return wordDF.get(word);
		else
			return 0;
	}
}
