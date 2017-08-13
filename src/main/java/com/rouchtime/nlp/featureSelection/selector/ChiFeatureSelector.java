package com.rouchtime.nlp.featureSelection.selector;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.ScoredObject;
import com.rouchtime.nlp.corpus.GuojiCorpus;
import com.rouchtime.nlp.corpus.ICorpus;
import com.rouchtime.nlp.featureSelection.source.DataSource;
import com.rouchtime.nlp.featureSelection.source.DataSourceDF;
import com.rouchtime.nlp.featureSelection.source.SimpleDataSourcePool;

import tokenizer.HanLPTokenizerFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

public class ChiFeatureSelector {
	private ICorpus mCorpus;
	private TokenizerFactory mFactory;

	public ChiFeatureSelector(ICorpus corpus, TokenizerFactory factory) {
		mCorpus = corpus;
		mFactory = factory;
	}

	public List<ScoredObject<String>> getCHI(double threshold) {
		DataSourceDF dsdf = (DataSourceDF) initDataSource(DataSourceDF.class);
		double N = dsdf.getDocCn();
		ObjectToDoubleMap<String> result = new ObjectToDoubleMap<String>();
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
		StopWordTokenierFactory stopWordFactory = new StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		StopNatureTokenizerFactory stopNatureTokenizerFactory = new StopNatureTokenizerFactory(stopWordFactory);
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring-mybatis.xml");
		GuojiCorpus guojiCorpus = (GuojiCorpus) applicationContext.getBean(GuojiCorpus.class);
		ChiFeatureSelector igFeatureSelector = new ChiFeatureSelector(guojiCorpus, stopNatureTokenizerFactory);
		List<ScoredObject<String>>  list = igFeatureSelector.getCHI(0);
		System.out.println(list);
	}
}
