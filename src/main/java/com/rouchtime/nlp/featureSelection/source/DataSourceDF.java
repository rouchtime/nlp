package com.rouchtime.nlp.featureSelection.source;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ObjectToDoubleMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.rouchtime.nlp.common.Term;
import com.rouchtime.nlp.corpus.ICorpus;

/**
 * Created by py on 16-9-21.
 */
public class DataSourceDF extends DataSource {
	private ObjectToDoubleMap<String> wordDF;
	private ObjectToDoubleMap<String> labelDF;
	private Table<String, String, Double> mLabelWordDf;
	private TokenizerFactory mfactory;
	private ICorpus mCorpus;
	private DataSourceDF() throws IOException {
		
	}
	@Override
	protected boolean resetImpl(ICorpus corpus,TokenizerFactory factory) {
		mCorpus = corpus;
		mfactory = factory;
		wordDF = new ObjectToDoubleMap<String>();
		labelDF = new ObjectToDoubleMap<String>();
		mLabelWordDf = HashBasedTable.create();
		return false;
	}

	@Override
	public boolean load() throws IOException {
		for(String label : mCorpus.labels()) {
			for(String fileid : mCorpus.fileidFromLabel(label)) {
				labelDF.increment(label, 1);
				Set<String> appearedWordIndoc = new HashSet<>();
				for(Term term : mCorpus.wordFromfileids(fileid, mfactory)) {
					String word = term.getWord();
					if (!appearedWordIndoc.contains(word)) {
						wordDF.increment(word, 1);
						addToMap(mLabelWordDf, label, word, 1);
						appearedWordIndoc.add(word);
					}
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
