package com.rouchtime.nlp.keyword.extraction;

import java.util.List;
import java.util.Map;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.ScoredObject;
import com.rouchtime.nlp.keyword.DictionaryResource;
import com.rouchtime.nlp.keyword.Token;

public class TfIdfKeyWordExtraction extends AbstractKeyWordExtraction {
	private DictionaryResource wordDictionary;
	private Map<String, Double> idfMap;
	public TfIdfKeyWordExtraction(TokenizerFactory tokenizerFactory) {
		super(tokenizerFactory);
		wordDictionary = DictionaryResource.getInstance();
		idfMap = wordDictionary.getIDFMAP();
	}

	@Override
	ObjectToDoubleMap<String> modifyKeywordsSort(List<String> titleTokens, List<String> bodyTokens) {
		ObjectToDoubleMap<String> tf = new ObjectToDoubleMap<>();
		titleTokens.addAll(bodyTokens);
		for(String word : titleTokens) {
			tf.increment(word.split("/")[0], 1.0);
		}
		ObjectToDoubleMap<String> tfidf = new ObjectToDoubleMap<>();
		double sumTfIdf = 0.0;
		for(String word : tf.keySet()) {
			Double idf = idfMap.get(word);
			if(idf == null) {
				double value = tf.get(word) * 0.01;
				sumTfIdf += Math.pow(value,2.0);
			} else {
				double value = tf.get(word) * idf;
				sumTfIdf += Math.pow(value,2.0);
			}
		}
		
		for(String word : tf.keySet()) {
			Double idf = idfMap.get(word);
			if(idf == null) {
				double value = tf.get(word) * 0.01;
				tfidf.put(word, value / Math.sqrt(sumTfIdf));
			} else {
				double value = tf.get(word) * idf;
				tfidf.put(word, value / Math.sqrt(sumTfIdf));
			}
		}
		return tfidf;
	}
}
