package com.rouchtime.nlp.keyword;

import java.util.List;
import java.util.Map;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.ScoredObject;

public class TfIdf {
	private WordDictionary wordDictionary;
	private Map<String, Double> idfMap;
	private TfIdf() {
		wordDictionary = WordDictionary.getInstance();
		idfMap = wordDictionary.getIDFMAP();
	}
	
	public List<ScoredObject<String>>  getTfIdfSingleDoc(List<String> tokens) {
		ObjectToDoubleMap<String> counter = new ObjectToDoubleMap<>();
		for(String token : tokens) {
			counter.increment(token, 1.0);
		}
		double sumTfIdf = 0.0;
		for(String token : counter.keySet()) {
			String word = token;
			Double idf = idfMap.get(word);
			if(idf == null) {
				double value = counter.get(token) * 0.01;
				sumTfIdf += Math.pow(value,2.0);
			} else {
				double value = counter.get(token) * idf;
				sumTfIdf += Math.pow(value,2.0);
			}
		}
		
		for(String token : counter.keySet()) {
			String word = token;
			Double idf = idfMap.get(word);
			if(idf == null) {
				double value = counter.get(token) * 0.01;
				counter.put(token, value / Math.sqrt(sumTfIdf));
			} else {
				double value = counter.get(token) * idf;
				counter.put(token, value / Math.sqrt(sumTfIdf));
			}
		}
		return counter.scoredObjectsOrderedByValueList();
	}
	
	public static TfIdf getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {
		private static final TfIdf instance = new TfIdf();
	}
}
