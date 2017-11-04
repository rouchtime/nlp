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
	 * @param tokens
	 * @return
	 */
	public Map<String,Double> mergeBySyn(HashMap<String,Double> tf) {
		@SuppressWarnings("unchecked")
		List<Set<String>> chainsList = new ArrayList<Set<String>>();
//		List<List<String>> chainsList
		for(String token : tf.keySet()) {}
		return null;
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
