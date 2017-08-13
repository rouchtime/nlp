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
import com.rouchtime.nlp.featureSelection.source.DataSourceDTF;
import com.rouchtime.nlp.featureSelection.source.SimpleDataSourcePool;

import tokenizer.HanLPTokenizerFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

public class IGFeatureSelector {
	private ICorpus mCorpus;
	private TokenizerFactory mFactory;

	public IGFeatureSelector(ICorpus corpus, TokenizerFactory factory) {
		mCorpus = corpus;
		mFactory = factory;
	}

	public List<ScoredObject<String>> getInformationGainByTF(double threshold) {
		DataSourceDTF dsdf = (DataSourceDTF) initDataSource(DataSourceDTF.class);
		double V = dsdf.getDictionary().size();
		ObjectToDoubleMap<String> result = new ObjectToDoubleMap<String>();
		double entropyLabel = 0.0;
		double docCN = dsdf.getDocCn();
		for (String label : dsdf.getLabels()) {
			double p_label = dsdf.getLabelCn(label) / docCN;
			entropyLabel += (Math.log(p_label) * p_label);
		}
		for (String word : dsdf.getDictionary()) {
			double p_ti = 0.0;
			double p_no_ti = 0.0;
			/* 计算p(t) */
			for (String label : dsdf.getLabels()) {
				// 获得词的总频数
				double totalwordlabelTF = dsdf.getLabelWordSize(label);
				// P(Ci)
				double probLabel = (dsdf.getLabelCn(label) * 1.0) / (dsdf.getLabelCn() * 1.0);
				double wordLabelTF = dsdf.getWordlabelCn(label, word);
				double probWordBylabel = (1 + wordLabelTF) / (V + totalwordlabelTF);
				p_ti += probWordBylabel * probLabel;
				p_no_ti += ((1 - probWordBylabel) * probLabel);
			}
			double entropy_hasWord = 0.0;
			double entropy_hasnot_Word = 0.0;
			for (String label : dsdf.getLabels()) {
				double totalwordlabelTF = dsdf.getLabelWordSize(label);
				// P(Ci)
				double probLabel = (dsdf.getLabelCn(label) * 1.0) / (dsdf.getLabelCn() * 1.0);
				double wordLabelTF = dsdf.getWordlabelCn(label, word);
				double probWordBylabel = (1 + wordLabelTF) / (V + totalwordlabelTF);
				
				
				double probPosteriorWord = (probWordBylabel * probLabel) / p_ti;
				double probPosteriorNotWord = ((1-probWordBylabel) * probLabel) / p_no_ti;
				entropy_hasWord += (probPosteriorWord * Math.log(probPosteriorWord));
				entropy_hasnot_Word += (probPosteriorNotWord * Math.log(probPosteriorNotWord));
			}
			double ig = -entropyLabel - (p_ti * (-entropy_hasWord) + p_no_ti*(-entropy_hasnot_Word));
			result.put(word, ig);
		}
		return result.scoredObjectsOrderedByValueList();

	}

	public List<ScoredObject<String>> getInformationGainByDF(double threshold) {
		DataSourceDF dsdf = (DataSourceDF) initDataSource(DataSourceDF.class);
		double docCN = dsdf.getDocCn();
		double EntropyLabel = 0;
		ObjectToDoubleMap<String> result = new ObjectToDoubleMap<String>();
		for (String label : dsdf.getLabels()) {
			double p_label = dsdf.getLabelDF(label) / docCN;
			EntropyLabel += (Math.log(p_label) * p_label);
		}

		for (String word : dsdf.getDictionary()) {
			double p_ti = dsdf.getWordDFMacro(word) / dsdf.getDocCn();
			double sum_1 = 0.0;
			double sum_2 = 0.0;
			for (String label : dsdf.getLabels()) {
				// p(ti|Cj)
				double p_ti_cj = (dsdf.getWordDF(label, word) + 1) / (dsdf.getDicSize() + dsdf.getLabelDF(label));
				// p(Cj)
				double p_label = dsdf.getLabelDF(label) / docCN;
				// P(cj|ti)
				double p_cj_ti = p_label * p_ti_cj / p_ti;
				sum_1 += p_cj_ti * Math.log(p_cj_ti);

				// p(^ti|cj)
				double p_no_ti_cj = 1 - p_ti_cj;
				double p_no_ti = 1 - p_ti;
				double p_cj_no_ti = p_label * p_no_ti_cj / p_no_ti;
				sum_2 += p_cj_no_ti * Math.log(p_cj_no_ti);
			}
			double ig = (-EntropyLabel) - (p_ti * (-sum_1) + (1 - p_ti) * (-sum_2));
			result.increment(word, ig);
		}
		return result.scoredObjectsOrderedByValueList();
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
		StopWordTokenierFactory stopWordFactory = new StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		StopNatureTokenizerFactory stopNatureTokenizerFactory = new StopNatureTokenizerFactory(stopWordFactory);
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring-mybatis.xml");
		GuojiCorpus guojiCorpus = (GuojiCorpus) applicationContext.getBean(GuojiCorpus.class);
		IGFeatureSelector igFeatureSelector = new IGFeatureSelector(guojiCorpus, stopNatureTokenizerFactory);
		List<ScoredObject<String>>  list = igFeatureSelector.getInformationGainByDF(0);
		System.out.println(list);
	}
}
