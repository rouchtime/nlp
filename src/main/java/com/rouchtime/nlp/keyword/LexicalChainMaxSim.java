package com.rouchtime.nlp.keyword;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 根据相似度计算词汇链
 * 
 * @author 龚帅宾
 *
 */
public class LexicalChainMaxSim {
	public WordSimiarity syn;
	List<Set<String>> chainsList = new ArrayList<Set<String>>();
	Map<String,Double> tfMap;
	private double delt = 0.5;

	public LexicalChainMaxSim(double delt,WordSimiarity simiarity) {
		this.syn = simiarity;
		this.delt = delt;
	}
	
	public void add(String word) {
		if (!syn.isExistWord(word)) {
			Set<String> set = new HashSet<String>();
			set.add(word);
			chainsList.add(set);
			return;
		}
		double simMaxTotalChain = -Double.MIN_VALUE;
		int maxIndex = 0;
		for (int i = 1; i < chainsList.size(); i++) {
			double simMaxInChain = -Double.MIN_VALUE;
			for (String chain : chainsList.get(i)) {
				double sim = syn.calTowWordSimiarity(chain, word);
				if (simMaxInChain < sim) {
					simMaxInChain = sim;
				}
			}
			if (simMaxTotalChain < simMaxInChain) {
				simMaxTotalChain = simMaxInChain;
				maxIndex = i;
			}
		}
		if (simMaxTotalChain >= delt) {
			chainsList.get(maxIndex).add(word);
		} else {
			Set<String> chains = new HashSet<String>();
			chains.add(word);
			chainsList.add(chains);
		}
	}

	public List<Set<String>> getLexicalChain() {
		return chainsList;
	}

	public static void main(String[] args) {
	}
}
