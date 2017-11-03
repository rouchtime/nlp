package com.rouchtime.nlp.keyword;

import java.util.*;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary;
import com.hankcs.hanlp.seg.common.Term;

public class TextRank {
	static final float d = 0.85f; // damping factor, default 0.85
	static final int max_iter = 200; // max iteration times
	static final float min_diff = 0.0001f; // condition to judge whether recurse or not
	private static int nKeyword = 5; // number of keywords to extract,default 5
	private static int coOccuranceWindow = 3; // size of the co-occurance window, default 3

	// change default parameters
	public static void setKeywordNumber(int sysKeywordNum) {
		nKeyword = sysKeywordNum;
	}

	public static void setWindowSize(int window) {
		coOccuranceWindow = window;
	}

	public static List<String> getKeyword(List<String> tokens,int size) {
		nKeyword  =size;
		Map<String, Float> score = TextRank.getWordScore(tokens);
		// rank keywords in terms of their score
		List<Map.Entry<String, Float>> entryList = new ArrayList<Map.Entry<String, Float>>(score.entrySet());
		Collections.sort(entryList, new Comparator<Map.Entry<String, Float>>() {
			@Override
			public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
				return (o1.getValue() - o2.getValue() > 0 ? -1 : 1);
			}
		});

		// System.out.println("After sorting: "+entryList);

		List<String> sysKeywordList = new ArrayList<String>();
		for (int i = 0; i < nKeyword; ++i) {
			try {
				sysKeywordList.add(entryList.get(i).getKey());
			} catch (IndexOutOfBoundsException e) {
				continue;
			}
		}
		return sysKeywordList;
	}
	
	public static Map<String, Float> getWordScore(List<String> tokens) {

		int count = 1; // position of each word
		Map<String, Integer> wordPosition = new HashMap<String, Integer>();

		List<String> wordList = new ArrayList<String>();

		// filter stop words
		for (String t : tokens) {
			wordList.add(t);
			if (!wordPosition.containsKey(t)) {
				wordPosition.put(t, count);
				count++;
			}
		}
		// System.out.println("Keyword candidates:"+wordList);

		// generate word-graph in terms of size of co-occur window
		Map<String, Set<String>> words = new HashMap<String, Set<String>>();
		Queue<String> que = new LinkedList<String>();
		for (String w : wordList) {
			if (!words.containsKey(w)) {
				words.put(w, new HashSet<String>());
			}
			que.offer(w); // insert into the end of the queue
			if (que.size() > coOccuranceWindow) {
				que.poll(); // pop from the queue
			}

			for (String w1 : que) {
				for (String w2 : que) {
					if (w1.equals(w2)) {
						continue;
					}

					words.get(w1).add(w2);
					words.get(w2).add(w1);
				}
			}
		}
		// System.out.println("word-graph:"+words); //each k,v represents all the words
		// in v point to k

		// iterate till recurse
		Map<String, Float> score = new HashMap<String, Float>();
		for (int i = 0; i < max_iter; ++i) {
			Map<String, Float> m = new HashMap<String, Float>();
			float max_diff = 0;
			for (Map.Entry<String, Set<String>> entry : words.entrySet()) {
				String key = entry.getKey();
				Set<String> value = entry.getValue();
				m.put(key, 1 - d);
				for (String other : value) {
					int size = words.get(other).size();
					if (key.equals(other) || size == 0)
						continue;
					m.put(key, m.get(key) + d / size * (score.get(other) == null ? 0 : score.get(other)));
				}

				max_diff = Math.max(max_diff, Math.abs(m.get(key) - (score.get(key) == null ? 1 : score.get(key))));
			}
			score = m;

			// exit once recurse
			if (max_diff <= min_diff)
				break;
		}
		return score;
	}
}
