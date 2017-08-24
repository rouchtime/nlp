package com.rouchtime.nlp.featureSelection.selector;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.ScoredObject;
import com.rouchtime.nlp.corpus.GuojiCorpus;
import com.rouchtime.nlp.corpus.ICorpus;
import com.rouchtime.nlp.featureSelection.source.DataSource;
import com.rouchtime.nlp.featureSelection.source.DataSourceDF;
import com.rouchtime.nlp.featureSelection.source.DataSourceDTF;
import com.rouchtime.nlp.featureSelection.source.SimpleDataSourcePool;

import tokenizer.HanLPTokenizerFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

public class CategoryDiscriminatingFeatureSelector {
	private List<Pair<String,String>> mCorpus;
	private TokenizerFactory mFactory;
	private ObjectToDoubleMap<String> result;

	public CategoryDiscriminatingFeatureSelector(List<Pair<String,String>> corpus, TokenizerFactory factory) {
		mCorpus = corpus;
		mFactory = factory;
	}

	public List<ScoredObject<String>> getCategoryDiscrimination(double threshold) {
		if (result != null) {
			List<ScoredObject<String>> list = new ArrayList<ScoredObject<String>>();
			for (ScoredObject<String> scoreObject : result.scoredObjectsOrderedByValueList()) {
				if (scoreObject.score() > threshold) {
					list.add(scoreObject);
				}
			}
			return list;
		} else {
			List<ScoredObject<String>> list = new ArrayList<ScoredObject<String>>();
			for (ScoredObject<String> scoreObject : getCategoryDiscimination()) {
				if (scoreObject.score() > threshold) {
					list.add(scoreObject);
				}
			}
			return list;
		}
	}
	
	public List<ScoredObject<String>> getCategoryDiscrimination(int remainCount) {
		if (result != null) {
			return result.scoredObjectsOrderedByValueList().subList(0, remainCount);
		} else {
			return getCategoryDiscimination().subList(0, remainCount);
		}
	}
	
	public List<ScoredObject<String>> getCategoryDiscrimination(int start,int end) {
		if (result != null) {
			return result.scoredObjectsOrderedByValueList().subList(start, end);
		} else {
			return getCategoryDiscimination().subList(start, end);
		}
	}
	
 	private List<ScoredObject<String>> getCategoryDiscimination() {
		result = new ObjectToDoubleMap<String>();
		DataSourceDTF dsdf = (DataSourceDTF) initDataSource(DataSourceDTF.class);
		double V = dsdf.getDictionary().size();
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
			result.put(word, value);
		}
		return result.scoredObjectsOrderedByValueList();
	}

	public Map<String, Long> getWordDistributionFromCategoryDiscrimination() {
		return null;
	}

	private DataSource initDataSource(Class dataClazz) {
		DataSource dsdf = null;
		try {
			dsdf = SimpleDataSourcePool.create(mCorpus, dataClazz, mFactory);
		} catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException
				| IOException e) {
			System.err.println("Build DF instance fail: " + e.getMessage());
		}
		return dsdf;
	}

	public static void main(String[] args) {
	}
}
