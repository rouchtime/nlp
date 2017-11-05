package com.rouchtime.nlp.keyword;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aliasi.util.ObjectToDoubleMap;

public class SynonymMerge {
	private WordSimiarity cilinSimiarity = CiLinWordSimiarity.getInstance();

	/**
	 * 基于近义词相似度计算词频
	 * 
	 * @param tokens
	 * @return
	 */
	public ObjectToDoubleMap<String> mergeBySyn(HashMap<String, Double> tf) {
		LexicalChainMaxTF lctf = new LexicalChainMaxTF(0.6, Word2VectorWordSimiarity.getInstance(), tf);
		for (String word : tf.keySet()) {
			try {
				lctf.add(word);
			} catch (Exception e) {
				continue;
			}
		}
		List<Set<String>> listLexicalChain = lctf.getLexicalChain();
		List<String> representWords= lctf.getRepresentWordList();
		ObjectToDoubleMap<String> newTF = new ObjectToDoubleMap<String>();
		for (int i = 0; i < listLexicalChain.size(); i++) {
			String represent = representWords.get(i);
			double sum = 0.0;
			for (String word : listLexicalChain.get(i)) {
				sum += (tf.get(word) * cilinSimiarity.calTowWordSimiarity(word, represent));
			}
			newTF.put(represent, sum);
		}
		return newTF;
	}

	public static void main(String[] args) {
		ObjectToDoubleMap<String> map = new ObjectToDoubleMap<>();
		map.increment("年后", 1.0);
		map.increment("年后", 1.0);
		map.increment("年后", 1.0);
		SynonymMerge sm = new SynonymMerge();
		sm.mergeBySyn(map);
	}
}
