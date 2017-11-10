package com.rouchtime.nlp.keyword.extraction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ObjectToDoubleMap;
import com.rouchtime.nlp.keyword.DictionaryResource;
import com.rouchtime.util.Contants;

public class BeforeTfIdfKeyWordExtraction extends AbstractKeyWordExtraction {
	private DictionaryResource wordDictionary;
	private Map<String, Double> idfMap;
	public static HashMap<String, Double> POS_SCORE = null;

	public BeforeTfIdfKeyWordExtraction(TokenizerFactory tokenizerFactory) {
		super(tokenizerFactory);
		wordDictionary = DictionaryResource.getInstance();
		idfMap = wordDictionary.getIDFMAP();
		POS_SCORE = new HashMap<String, Double>();
		POS_SCORE.put("nr", 3.0); // 人名
		POS_SCORE.put("nrf", 3.0);// 音译人名
		POS_SCORE.put("nw", 0.1);// 新词
		POS_SCORE.put("nt", 2.0);// 机构团体名
		POS_SCORE.put("nz", 2.8);// 其它专名
		// POS_SCORE.put("v", 0.4);
		POS_SCORE.put("kw", 6.0); // 关键词词性
		// POS_SCORE.put("n", 0.5); //名词
		POS_SCORE.put("n", 2.5);
		POS_SCORE.put("ns", 2.5);// 地名

		POS_SCORE.put("nsf", 3.0);// 音译地名
		POS_SCORE.put("nrj日语人名", 3.0);
		POS_SCORE.put("nr1 汉语姓氏", 3.0);
		POS_SCORE.put("nr2 汉语名字", 3.0);
		POS_SCORE.put("nl 名词性惯用语", 3.0);
	}

	@Override
	ObjectToDoubleMap<String> modifyKeywordsSort(List<String> titleTokens, List<String> bodyTokens) {
		int wordCount = 0;
		ObjectToDoubleMap<String> tf = new ObjectToDoubleMap<>();
		Map<String,String> tokenMap = new HashMap<String,String>();
		titleTokens.addAll(bodyTokens);
		for (String term : titleTokens) {
			if(term.split(Contants.SLASH).length != 2) {
				continue;
			}
			String word = term.split(Contants.SLASH)[0];
			String nature = term.split(Contants.SLASH)[1];
			tf.increment(word.split("/")[0], 1.0);
			tokenMap.put(word, nature);
			wordCount++;
		}
		ObjectToDoubleMap<String> tfidf = new ObjectToDoubleMap<>();
		for (String word : tokenMap.keySet()) {
			double tfidfvalue = 0.0;
			Double idf = idfMap.get(word);
			if (idf == null) {
				tfidfvalue = ((double) tf.get(word) / (double) wordCount) * 0.01;
			} else {
				tfidfvalue = ((double) tf.get(word) / (double) wordCount) * idf;
			}
			Double weight = POS_SCORE.get(tokenMap.get(word));
			if(weight==null) {
				weight = 0.0;
			}
			double tfidfvalue2 = (-1) * Math.log(tfidfvalue) - Math.log(weight);
			tfidf.put(word, tfidfvalue2);
		}
		return tfidf;
	}

}
