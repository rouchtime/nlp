package com.rouchtime.nlp.keyword;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aliasi.util.ObjectToDoubleMap;

public class LexicalChainMaxTF {

	public WordSimiarity syn;
	List<Set<String>> chainsList = new ArrayList<Set<String>>();
	List<String> representWordList = new ArrayList<String>();
	Map<String, Double> tfMap;
	private double delt = 0.9;

	public LexicalChainMaxTF(double delt, WordSimiarity simiarity, Map<String, Double> tfMap) {
		this.syn = simiarity;
		this.delt = delt;
		this.tfMap = tfMap;
	}

	public void add(String word) throws Exception {
		if (chainsList.size() != representWordList.size()) {
			throw new Exception();
		}
		if (representWordList.size() == 0) {
			representWordList.add(word);
			Set<String> set = new HashSet<String>();
			set.add(word);
			chainsList.add(set);
			return;
		}
		double maxSim = -Double.MIN_VALUE;
		int idx = -1;
		for (int i = 0; i < representWordList.size(); i++) {
			double sim = syn.calTowWordSimiarity(representWordList.get(i), word);
			if (sim > delt) {
				if (sim > maxSim) {
					maxSim = sim;
					idx = i;
				}
			}
		}
		if (idx != -1) {
			String represent = (tfMap.get(representWordList.get(idx)) > tfMap.get(word)) ? representWordList.get(idx)
					: word;
			representWordList.set(idx, represent);
			chainsList.get(idx).add(word);
		} else {
			representWordList.add(word);
			Set<String> set = new HashSet<String>();
			set.add(word);
			chainsList.add(set);
		}
	}

	public List<Set<String>> getLexicalChain() {
		return chainsList;
	}

	public List<String> getRepresentWordList() {
		return representWordList;
	}

	public static void main(String[] args) {
		ObjectToDoubleMap<String> tfMap = new ObjectToDoubleMap<>();
		tfMap.increment("群众", 3.0);
		tfMap.increment("大众", 1.0);
		tfMap.increment("公众", 1.0);
		tfMap.increment("良民", 5.0);
		tfMap.increment("玉米", 3.0);
		tfMap.increment("老玉米", 1.0);
		tfMap.increment("清洁工", 2.0);
		tfMap.increment("勤杂人员", 5.0);
		System.out.println(CiLinWordSimiarity.getInstance().calTowWordSimiarity("清洁工", "勤杂人员"));
		LexicalChainMaxTF lctf = new LexicalChainMaxTF(0.9, CiLinWordSimiarity.getInstance(), tfMap);
		for(String word : tfMap.keySet()) {
			try {
				lctf.add(word);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println(lctf.chainsList);
		System.out.println(lctf.representWordList);
	}

}
