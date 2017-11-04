package com.rouchtime.nlp.featureSelection.source;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ObjectToDoubleMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.rouchtime.nlp.common.Term;
import com.rouchtime.nlp.corpus.ICorpus;
import com.rouchtime.nlp.featureSelection.bean.FeatureSelectionBean;

/**
 * Created by py on 16-9-21.
 */
public class DataSourceDF extends DataSource {
	private ObjectToDoubleMap<String> wordDF;
	private ObjectToDoubleMap<String> labelDF;
	private Table<String, String, Double> mLabelWordDf;
	private TokenizerFactory mfactory;
	private List<FeatureSelectionBean> mCorpus;
	private DataSourceDF() throws IOException {
		
	}
	@Override
	protected boolean resetImpl(List<FeatureSelectionBean> corpus,TokenizerFactory factory) {
		mCorpus = corpus;
		mfactory = factory;
		wordDF = new ObjectToDoubleMap<String>();
		labelDF = new ObjectToDoubleMap<String>();
		mLabelWordDf = HashBasedTable.create();
		return false;
	}

	@Override
	public boolean load() throws IOException {
		for(FeatureSelectionBean pair : mCorpus) {
			String raw = pair.getRaw();
			String label = pair.getLabel();
			labelDF.increment(label, 1);
			Set<String> appearedWordIndoc = new HashSet<>();
			for(String term : mfactory.tokenizer(raw.toCharArray(), 0, raw.length())) {
				String word = term.split("/")[0];
				if (!appearedWordIndoc.contains(word)) {
					wordDF.increment(word, 1);
					addToMap(mLabelWordDf, label, word, 1);
					appearedWordIndoc.add(word);
				}
			}
		}
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
	public double getDocCn() {
		Double sum = 0.0;
		for (String label : labelDF.keySet()) {
			sum += labelDF.get(label);
		}
		return sum;
	}

	// 得到单词在某个类中的文档频率
	public double getWordDF(String label, String word) {
		if (mLabelWordDf.contains(label, word))
			return mLabelWordDf.get(label, word);
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
	public double getWordDFMacro(String word) {
		if (wordDF.containsKey(word))
			return wordDF.get(word);
		else
			return 0;
	}
	public Table<String, String, Double> getmLabelWordDf() {
		return mLabelWordDf;
	}
	public ObjectToDoubleMap<String> getWordDF() {
		return wordDF;
	}
	
}
