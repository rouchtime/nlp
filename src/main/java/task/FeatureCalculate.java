package task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.aliasi.util.ScoredObject;
import com.rouchtime.nlp.corpus.GuojiCorpus;
import com.rouchtime.nlp.corpus.SougouCateCorpus;
import com.rouchtime.nlp.featureSelection.selector.CategoryDiscriminatingFeatureSelector;
import com.rouchtime.nlp.featureSelection.selector.ChiFeatureSelector;
import com.rouchtime.nlp.featureSelection.selector.DocumentFrequencyFeatureSelector;
import com.rouchtime.util.RegexUtils;

import tokenizer.HanLPTokenizerFactory;
import tokenizer.JiebaTokenizerFactory;
import tokenizer.NGramTokenizerBasedOtherTokenizerFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

public class FeatureCalculate {
	public static void main(String[] args) throws IOException {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring-mybatis.xml");
		SougouCateCorpus guojiCorpus = (SougouCateCorpus) applicationContext.getBean(SougouCateCorpus.class);
		List<Pair<String, String>> ListPair = new ArrayList<Pair<String, String>>();
		for (String fileid : guojiCorpus.fileids()) {
			ImmutablePair<String, String> pair = new ImmutablePair<String, String>(RegexUtils.cleanSpecialWord(guojiCorpus.rawFromfileids(fileid)),
					guojiCorpus.labelFromfileid(fileid));
			ListPair.add(pair);
		}
		StopWordTokenierFactory stopWordFactory = new StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		StopNatureTokenizerFactory stopNatureTokenizerFactory = new StopNatureTokenizerFactory(stopWordFactory);
//		NGramTokenizerBasedOtherTokenizerFactory factory = new NGramTokenizerBasedOtherTokenizerFactory(stopNatureTokenizerFactory, 1, 2);
		DocumentFrequencyFeatureSelector dfSelector = new DocumentFrequencyFeatureSelector(ListPair,
				stopNatureTokenizerFactory);
//		CategoryDiscriminatingFeatureSelector cdhFeatureSelector = new CategoryDiscriminatingFeatureSelector(ListPair, factory);
		List<ScoredObject<String>> list = dfSelector.getDocumentFrequency(0,10000);
		for(ScoredObject<String> scoreObject : list) {
			FileUtils.write(new File("D://df"), scoreObject.getObject() + "\t" + scoreObject.score()+"\n","utf-8",true);
		}
		
	}
}
