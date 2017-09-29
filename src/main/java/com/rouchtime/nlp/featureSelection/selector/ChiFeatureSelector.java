package com.rouchtime.nlp.featureSelection.selector;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.ScoredObject;
import com.rouchtime.nlp.corpus.GuojiCorpus;
import com.rouchtime.nlp.corpus.ICorpus;
import com.rouchtime.nlp.featureSelection.bean.FeatureSelectionBean;
import com.rouchtime.nlp.featureSelection.source.DataSource;
import com.rouchtime.nlp.featureSelection.source.DataSourceDF;
import com.rouchtime.nlp.featureSelection.source.SimpleDataSourcePool;

import tokenizer.HanLPTokenizerFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

public class ChiFeatureSelector {
	private List<FeatureSelectionBean> mCorpus;
	private TokenizerFactory mFactory;
	private ObjectToDoubleMap<String> result;

	public ChiFeatureSelector(List<FeatureSelectionBean> corpus, TokenizerFactory factory) {
		mCorpus = corpus;
		mFactory = factory;
	}

	public List<ScoredObject<String>> getCHI(int remainCount) {
		if (result != null) {
			return result.scoredObjectsOrderedByValueList().subList(0, remainCount);
		} else {
			return getCHI().subList(0, remainCount);
		}
	}

	private List<ScoredObject<String>> getCHI() {
		DataSourceDF dsdf = (DataSourceDF) initDataSource(DataSourceDF.class);
		double N = dsdf.getDocCn();
		result = new ObjectToDoubleMap<String>();
		for (String word : dsdf.getDictionary()) {
			double chi_max = 0.0;
			for (String label : dsdf.getLabels()) {
				double A = dsdf.getWordDF(label, word);
				double C = dsdf.getLabelDF(label) - A;
				double B = dsdf.getWordDFMacro(word) - A;
				double D = dsdf.getDocCn() - A - B - C;
				double chi_ti_Cj = N * Math.pow((A * D - C * B), 2) / ((A + C) * (B + D) * (A + B) * (C + D));
				if (chi_ti_Cj > chi_max) {
					chi_max = chi_ti_Cj;
				}
			}
			result.increment(word, chi_max);

		}
		return result.scoredObjectsOrderedByValueList();
	}

	private DataSource initDataSource(Class dataClazz) {
		DataSource dsdf = null;
		try {
			dsdf = SimpleDataSourcePool.create(mCorpus, DataSourceDF.class, mFactory);
		} catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException
				| IOException e) {
			System.err.println("Build DF instance fail: " + e.getMessage());
		}
		return dsdf;
	}

	public static void main(String[] args) {
	}
}
