package com.rouchtime.nlp.keyword;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aliasi.util.ObjectToDoubleMap;
import com.rouchtime.nlp.keyword.lexicalChain.LexicalChainMaxTF;
import com.rouchtime.nlp.keyword.similarity.WordSimiarity;

public class SynonymMerge {

	/**
	 * 基于近义词相似度计算合并后的词频
	 * 
	 * @param tokens
	 * @return
	 */
	public static Map<String,Double> mergeTFBySynonym(Map<String,Double> tf, WordSimiarity simiarity) {
		LexicalChainMaxTF lctf = new LexicalChainMaxTF(0.7, simiarity, tf);
		for (String word : tf.keySet()) {
			try {
				lctf.add(word);
			} catch (Exception e) {
				continue;
			}
		}
		List<Set<String>> listLexicalChain = lctf.getLexicalChain();
		List<String> representWords = lctf.getRepresentWordList();
		Map<String,Double> newTF = new HashMap<String,Double>();
		for (int i = 0; i < listLexicalChain.size(); i++) {
			String represent = representWords.get(i);
			double sum = 0.0;
			for (String word : listLexicalChain.get(i)) {
				sum += (tf.get(word) * simiarity.calTowWordSimiarity(word, represent));
			}
			newTF.put(represent, sum);
		}
		return newTF;
	}

	public static void main(String[] args) {
	}
}
