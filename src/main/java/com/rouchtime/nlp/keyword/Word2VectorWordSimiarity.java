package com.rouchtime.nlp.keyword;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * 根据Word2Vector计算近义词，单例
 * 
 * @author 龚帅宾
 *
 */
public class Word2VectorWordSimiarity implements WordSimiarity{
	private WordDictionary wordDictionary;
	private List<double[]> values;
	private Map<String, Integer> w_index_Map;
	private List<String> ws;

	private Word2VectorWordSimiarity() {
		wordDictionary = WordDictionary.getInstance();
		values = wordDictionary.getWORD_VECTOR_LIST();
		w_index_Map = wordDictionary.getWORD_INDEX_MAP();
		ws = wordDictionary.getWORDLIST();

	}

	public void outputAllSynonymTopN(int N, String outPath) throws IOException {
		Queue<ImmutablePair<Integer, Double>> integerPriorityQueue = new PriorityQueue<ImmutablePair<Integer, Double>>(
				N, ascComparator);
		for (int i = 0; i < values.size(); i++) {
			integerPriorityQueue.clear();
			for (int j = 0; j < values.size(); j++) {
				if (i == j) {
					continue;
				}
				Double value = calcos(values.get(i), values.get(j));
				if (value == null) {
					continue;
				}
				ImmutablePair<Integer, Double> pair = new ImmutablePair<Integer, Double>(j, value);
				if (integerPriorityQueue.size() < N) {
					integerPriorityQueue.add(pair);
				} else {
					ImmutablePair<Integer, Double> peek = integerPriorityQueue.peek();
					if ((value - peek.right.doubleValue()) > 0) { // 将新元素与当前堆顶元素比较，保留较小的元素
						integerPriorityQueue.poll();
						integerPriorityQueue.add(pair);
					}
				}
			}
			List<ImmutablePair<Integer, Double>> list = new ArrayList<>(integerPriorityQueue);
			Collections.sort(list, descComparator2);
			for (int c = 0; c < N; c++) {
				FileUtils.write(new File(outPath),
						String.format("%s\t%s\t%f\n", ws.get(i), ws.get(list.get(c).left), list.get(c).right), "utf-8",
						true);
			}
		}
	}

	public void outputAllSynonymByDelt(double delt, String outPath) throws IOException {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < values.size(); i++) {
			List<ImmutablePair<Integer, Double>> list = new ArrayList<>();
			sb.delete(0, sb.length());
			for (int j = 0; j < values.size(); j++) {
				if (i == j) {
					continue;
				}
				Double value = calcos(values.get(i), values.get(j));
				if (value == null) {
					continue;
				}
				if (value.doubleValue() > delt) {
					ImmutablePair<Integer, Double> pair = new ImmutablePair<Integer, Double>(j, value);
					list.add(pair);
				}
			}
			Collections.sort(list, descComparator2);
			for (ImmutablePair<Integer, Double> pair : list) {
				sb.append(String.format("%s,%f ", ws.get(pair.getLeft()), pair.right.doubleValue()));
			}
			FileUtils.write(new File(outPath), String.format("%s %s\n", ws.get(i), sb.toString()), "utf-8", true);
		}

	}

	@Override
	public double calTowWordSimiarity(String word1, String word2) {
		if (word1.equals(word2)) {
			return 1.0;
		}
		if (w_index_Map.get(word1) == null || w_index_Map.get(word2) == null) {
			return 0.0;
		}
		int w_index1 = w_index_Map.get(word1);
		int w_index2 = w_index_Map.get(word2);
		double sim = calcos(values.get(w_index1), values.get(w_index2));
		return sim;
	}

	public boolean isExistWord(String word) {
		if(w_index_Map.get(word) == null) {
			return false;
		}
		return true;
	}
	
	public static Word2VectorWordSimiarity getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {
		private static final Word2VectorWordSimiarity instance = new Word2VectorWordSimiarity();
	}

	/**
	 * 升序比较器
	 */
	private static Comparator<ImmutablePair<Integer, Double>> ascComparator = new Comparator<ImmutablePair<Integer, Double>>() {
		@Override
		public int compare(ImmutablePair<Integer, Double> o1, ImmutablePair<Integer, Double> o2) {
			if (Math.abs(o1.right - o2.right) <= 0) {
				return 0;
			} else {
				if (o1.right.doubleValue() - o2.right.doubleValue() < 0.0) {
					return -1;
				} else {
					return 1;
				}
			}
		}

	};

	/**
	 * 降序比较器
	 */
	private static Comparator<ImmutablePair<Integer, Double>> descComparator2 = new Comparator<ImmutablePair<Integer, Double>>() {
		@Override
		public int compare(ImmutablePair<Integer, Double> o1, ImmutablePair<Integer, Double> o2) {
			if (Math.abs(o1.right - o2.right) <= 0) {
				return 0;
			} else {
				if (o1.right.doubleValue() - o2.right.doubleValue() < 0.0) {
					return 1;
				} else {
					return -1;
				}
			}
		}

	};

	private static Double calcos(double[] v1, double[] v2) {
		if (v1.length != v2.length) {
			return null;
		}
		double sum_fenzi = 0.0;
		double sum_i = 0.0;
		double sum_j = 0.0;
		for (int k = 1; k < v1.length; k++) {
			double fenzi = v1[k] * v2[k];
			sum_fenzi += fenzi;
			sum_i += Math.pow(v1[k], 2.0);
			sum_j += Math.pow(v2[k], 2.0);
		}
		return sum_fenzi / (Math.sqrt(sum_i) * Math.sqrt(sum_j));
	}
}
