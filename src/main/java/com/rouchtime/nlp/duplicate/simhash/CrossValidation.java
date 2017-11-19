package com.rouchtime.nlp.duplicate.simhash;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.aliasi.spell.JaccardDistance;
import com.aliasi.symbol.MapSymbolTable;
import com.aliasi.tokenizer.NGramTokenizerFactory;
import com.rouchtime.util.RegexUtils;

import tokenizer.AnsjTokenizerFactory;

public class CrossValidation {

	private static Simhash simhash = new Simhash(new NGramTokenizerFactory(2, 3));

	public static String out(long simhash) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 64; i++) {
			sb.append(simhash >> i & 1);
		}
		return sb.toString();
	}

	
	
	public static void main(String[] args) throws IOException {
		List<String> lines = FileUtils.readLines(new File("E:\\corpus\\duplicate\\test.txt"),
				"utf-8");
		List<Long> simhashvalue = new ArrayList<Long>();
		List<Set<Integer>> articleSet = new ArrayList<Set<Integer>>();
		// Map<Integer,String> indexMap = new HashMap<Integer,String>();
		MapSymbolTable symtable = new MapSymbolTable();
		int idx = 0;
		for (int i = 0; i < lines.size(); i++) {
			String[] splits = lines.get(i).split("\t+");
			if (splits.length != 4) {
				continue;
			}
			String url = splits[1];
			String title = splits[2];
			String raw1 = title + RegexUtils.cleanParaAndImgLabel(splits[3]);
			Set<Integer> set = new HashSet<Integer>();
			for (String term : AnsjTokenizerFactory.getIstance().tokenizer(raw1.toCharArray(), 0, raw1.length())) {
				int id = symtable.getOrAddSymbol(term.split("/")[0]);
				set.add(id);
			}
			//
			articleSet.add(set);
			long b = simhash.simhash64(raw1);
//			long b = SimHashUtil.getSimHashVersion2(raw1, 64);
			simhashvalue.add(b);
			// indexMap.put(idx, title);
			idx++;
		}

		// for(int i=0;i<simhashvalue.size();i++) {
		// FileUtils.write(new File("D://haming3"), String.format(">>>>>%s\n",
		// indexMap.get(i)),"utf-8",true);
		// for(int j=i+1;j<simhashvalue.size();j++) {
		// double jac = jaccardIndex(articleSet.get(i), articleSet.get(j));
		//
		// int haming = simhash.hammingDistance(simhashvalue.get(i),
		// simhashvalue.get(j));
		// if (haming <= 3 && jac<0.6) {
		// System.out.println(out(simhashvalue.get(i)));
		// System.out.println(out(simhashvalue.get(j)));
		// FileUtils.write(new File("D://haming3"), String.format("%s\n",
		// indexMap.get(j)),"utf-8",true);
		// }
		// }
		// FileUtils.write(new File("D://haming3"),
		// "*****************************\n","utf-8",true);
		// }
		for (int f = 0; f <= 0; f++) {
			for (int k =6; k <=6; k++) {
				int TP = 0;
				int FP = 0;
				int FN = 0;
				int TN = 0;
				int count = 0;
				for (int i = 0; i < articleSet.size(); i++) {
					for (int j = i; j < articleSet.size(); j++) {
						if (i == j) {
							continue;
						}
						double jac = jaccardIndex(articleSet.get(i), articleSet.get(j));
						int haming = simhash.hammingDistance(simhashvalue.get(i).longValue(),
								simhashvalue.get(j).longValue());
						if (jac >= 0.6 + f * 0.1) {
							if (haming <= k) {
								TP++;
							} else {
								FN++;
							}
						} else {
							if (haming <= k) {
								FP++;
							} else {
								TN++;
							}
						}
						count++;
						if (count % 100000 == 0) {
							System.out.println(
									String.format("f=%.2f,k= %d\nTP = %d\nFP = %d\nFN = %d\nTN = %d\n*********",
											0.6 + f * 0.1, k, TP, FP, FN, TN));
						}
					}
				}
				FileUtils.write(new File("D://duplicate_j_h_result"),
						String.format("f=%.2f,k= %d\nTP = %d\nFP = %d\nFN = %d\nTN = %d\n*********\n", 0.6 + f * 0.1, k,
								TP, FP, FN, TN),
						"utf-8", true);
			}
		}

	}

	public static double jaccardIndex(final Set<Integer> s1, final Set<Integer> s2) {

		Set<Integer> intersection = new HashSet<Integer>(s1);
		intersection.retainAll(s2);

		Set<Integer> union = new HashSet<Integer>(s1);
		union.addAll(s2);

		if (union.isEmpty()) {
			return 0;
		}

		return (double) intersection.size() / union.size();
	}
}
