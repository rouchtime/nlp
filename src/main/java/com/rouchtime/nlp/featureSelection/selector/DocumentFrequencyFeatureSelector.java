package com.rouchtime.nlp.featureSelection.selector;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.ScoredObject;
import com.google.common.collect.Table;
import com.rouchtime.nlp.corpus.ICorpus;
import com.rouchtime.nlp.featureSelection.bean.FeatureSelectionBean;
import com.rouchtime.nlp.featureSelection.source.DataSource;
import com.rouchtime.nlp.featureSelection.source.DataSourceDF;
import com.rouchtime.nlp.featureSelection.source.SimpleDataSourcePool;

public class DocumentFrequencyFeatureSelector {

	private List<FeatureSelectionBean> mCorpus;
	private TokenizerFactory mFactory;

	public DocumentFrequencyFeatureSelector(List<FeatureSelectionBean> corpus, TokenizerFactory factory) {
		mCorpus = corpus;
		mFactory = factory;
	}

	
	
	/**
	 * 获得给定词在某一类中的文档频率
	 * 
	 * @param label
	 * @param word
	 * @return
	 */
	public double getDocumentFrequencyByWordAndLabel(String label, String word) {
		DataSource dsdf = initDataSource();
		return ((DataSourceDF) dsdf).getWordDF(label, word);
	}

	/**
	 * 获得词在给类别下的文档频率
	 * 
	 * @param word
	 * @return <code>ObjectToDoubleMap</code>,为该词在各类的文档频率
	 */
	public ObjectToDoubleMap<String> getDocumentFrequencyByWordInEveryLabel(String word) {
		DataSource dsdf = initDataSource();
		ObjectToDoubleMap<String> objectToDoubleMap = new ObjectToDoubleMap<String>();
		for (String label : dsdf.getLabels()) {
			objectToDoubleMap.increment(label, ((DataSourceDF) dsdf).getWordDF(label, word));
		}
		return objectToDoubleMap;
	}

	/**
	 * 获得词在整个语料中的文档频率
	 * 
	 * @param word
	 * @return
	 */
	public Double getDocumentFrequencyByWord(String word) {
		DataSource dsdf = initDataSource();
		return ((DataSourceDF) dsdf).getWordDFMacro(word);
	}

	/**
	 * 获得指定类下的TOPN词
	 * 
	 * @param label
	 * @param topN
	 * @return
	 */
	public Map<String, Double> getDocumentFrequencyByLabel(String label, int topN) {
		DataSource dsdf = initDataSource();
		Table<String, String, Double> table = ((DataSourceDF) dsdf).getmLabelWordDf();
		Map<String, Double> map = table.row(label);
		List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				return -(o1.getValue()).compareTo(o2.getValue());
			}
		});
		Map<String, Double> result = new LinkedHashMap<String, Double>();
		int topCount = 0;
		for (Map.Entry<String, Double> entry : list) {
			if (topCount == topN) {
				break;
			}
			result.put(entry.getKey(), entry.getValue());
			topCount++;
		}
		return result;
	}

	/**
	 * 根据指定类，获得max和min之间的DF
	 * 
	 * @param label
	 * @param max
	 * @param min
	 * @return
	 */
	public Map<String, Double> getDocumentFrequencyByLabel(String label, double max, double min) {
		DataSource dsdf = initDataSource();
		Table<String, String, Double> table = ((DataSourceDF) dsdf).getmLabelWordDf();
		Map<String, Double> map = table.row(label);
		List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				return -(o1.getValue()).compareTo(o2.getValue());
			}
		});
		Map<String, Double> result = new LinkedHashMap<String, Double>();
		for (Map.Entry<String, Double> entry : list) {
			if (entry.getValue() <= max && entry.getValue() >= min) {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}

	public List<ScoredObject<String>> getDocumentFrequency(int topN) {
		DataSource dsdf = initDataSource();
		ObjectToDoubleMap<String> table = ((DataSourceDF) dsdf).getWordDF();
		List<ScoredObject<String>> list = table.scoredObjectsOrderedByValueList();
		return list.subList(0, topN);
	}

	
	
	public List<ScoredObject<String>> getDocumentFrequency(int startIndex, int endIndex) {
		DataSource dsdf = initDataSource();
		ObjectToDoubleMap<String> table = ((DataSourceDF) dsdf).getWordDF();
		List<ScoredObject<String>> list = table.scoredObjectsOrderedByValueList();
		return list.subList(startIndex, endIndex);
	}

	public Map<String, Double> getDocumentFrequency(double max, double min) {
		DataSource dsdf = initDataSource();
		ObjectToDoubleMap<String> table = ((DataSourceDF) dsdf).getWordDF();
		List<ScoredObject<String>> list = table.scoredObjectsOrderedByValueList();
		Map<String, Double> result = new LinkedHashMap<String, Double>();
		for (ScoredObject<String> entry : list) {
			if (entry.score() <= max && entry.score() >= min) {
				result.put(entry.getObject(), entry.score());
			}
		}
		return result;
	}

	public Set<String> combineTopNDocumentFrequency(int TopN) {
		DataSource dsdf = initDataSource();
		Table<String, String, Double> table = ((DataSourceDF) dsdf).getmLabelWordDf();
		Set<String> words = new HashSet<String>();
		for (String label : ((DataSourceDF) dsdf).getLabels()) {
			Map<String, Double> map = table.row(label);
			List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(map.entrySet());
			Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
				@Override
				public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
					return -(o1.getValue()).compareTo(o2.getValue());
				}
			});
			for (int i = 0; i < TopN; i++) {
				words.add(list.get(i).getKey());
			}
		}

		return words;
	}

	public Set<String> getWordDictionary() {
		DataSource dsdf = initDataSource();
		return ((DataSourceDF) dsdf).getDictionary();
	}

	private DataSource initDataSource() {
		DataSource dsdf = null;
		try {
			dsdf = SimpleDataSourcePool.create(mCorpus, DataSourceDF.class, mFactory);
		} catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException
				| IOException e) {
			System.err.println("Build DF instance fail: " + e.getMessage());
		}
		return dsdf;
	}

}
