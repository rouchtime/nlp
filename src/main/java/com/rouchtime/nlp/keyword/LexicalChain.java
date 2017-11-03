package com.rouchtime.nlp.keyword;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.rouchtime.util.RegexUtils;

import tokenizer.AnsjTokenizerFactory;

/**
 * 根据相似度计算词汇链
 * 
 * @author 龚帅宾
 *
 */
public class LexicalChain {
	public Synonym syn = Synonym.getInstance();
	List<Set<String>> chainsList = new ArrayList<Set<String>>();
	Set<String> anotherChains = new HashSet<String>();
	private double delt = 0.5;

	public LexicalChain(double delt) {
		this.delt = delt;
		chainsList.add(anotherChains);
	}

	public void add(String word) {
		if (!syn.isExistWord(word)) {
			anotherChains.add(word);
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
