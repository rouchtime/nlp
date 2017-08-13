package com.rouchtime.nlp.featureSelection.selector;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Clock;
import java.util.List;
import java.util.Map;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.ScoredObject;
import com.rouchtime.nlp.corpus.ICorpus;
import com.rouchtime.nlp.featureSelection.source.DataSource;
import com.rouchtime.nlp.featureSelection.source.DataSourceDF;
import com.rouchtime.nlp.featureSelection.source.DataSourceDTF;
import com.rouchtime.nlp.featureSelection.source.SimpleDataSourcePool;

public class CategoryDiscriminatingFeatureSelector {
	private ICorpus mCorpus;
	private TokenizerFactory mFactory;
	public CategoryDiscriminatingFeatureSelector(ICorpus corpus, TokenizerFactory factory) {
		mCorpus = corpus;
		mFactory = factory;
	}
	
	public List<ScoredObject<String>> getCategoryDiscimination(double threhold) {
		DataSourceDTF dsdf = (DataSourceDTF) initDataSource();
		double V = dsdf.getDictionary().size();
		ObjectToDoubleMap<String> result = new ObjectToDoubleMap<String>();
		for (String word : dsdf.getDictionary()) {
			double probWord = 0.0;

			/* 计算p(t) */
			for (String label : dsdf.getLabels()) {
				// 获得词的总频数
				double totalwordlabelTF = dsdf.getLabelWordSize(label);
				// P(Ci)
				double probLabel = (dsdf.getLabelCn(label) * 1.0) / (dsdf.getLabelCn() * 1.0);
				double wordLabelTF = dsdf.getWordlabelCn(label, word);
				double probWordBylabel = (1 + wordLabelTF) / (V + totalwordlabelTF);
				probWord += probWordBylabel * probLabel;
			}
			double maxProbPosteriorWord = 0.0;
			double secondMaxProbPosteriorWord = 0.0;
			for (String label : dsdf.getLabels()) {
				double totalwordlabelTF = dsdf.getLabelWordSize(label);
				// P(Ci)
				double probLabel = (dsdf.getLabelCn(label) * 1.0) / (dsdf.getLabelCn() * 1.0);
				double wordLabelTF = dsdf.getWordlabelCn(label, word);
				double probWordBylabel = (1 + wordLabelTF) / (V + totalwordlabelTF);

				// 找到该词的第一大后验概率和第二大后验概率
				double probPosteriorWord = (probWordBylabel * probLabel) / probWord;
				if (probPosteriorWord > maxProbPosteriorWord) {
					secondMaxProbPosteriorWord = maxProbPosteriorWord;
					maxProbPosteriorWord = probPosteriorWord;
				} else if (probPosteriorWord < maxProbPosteriorWord && probPosteriorWord > secondMaxProbPosteriorWord) {
					secondMaxProbPosteriorWord = probPosteriorWord;
				}
			}
			double value = maxProbPosteriorWord - secondMaxProbPosteriorWord;
			if(threhold  < value) {
				continue;
			}
			result.put(word, value);
		}
		return result.scoredObjectsOrderedByValueList();
	}
	
	public Map<String,Long> getWordDistributionFromCategoryDiscrimination() {
		return null;
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
