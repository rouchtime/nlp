package com.rouchtime.nlp.duplicate.simhash;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.aliasi.symbol.MapSymbolTable;
import com.aliasi.tokenizer.NGramTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.rouchtime.util.RegexUtils;

import tokenizer.AnsjTokenizerFactory;

public class test128Hash {
	private static TokenizerFactory factory = new NGramTokenizerFactory(2,2);
	private static  Simhash hash = new Simhash(factory);
	public static void main(String[] args) throws IOException {
		long s = System.currentTimeMillis();
		 String msg = "123";
        int len = msg.getBytes("UTF-8").length;//输出9, 2*3+3=9,一个汉字占3字节,一个英文字母一个字节.  
         System.out.println("UTF-8: "+len); 
		System.out.println(System.currentTimeMillis() - s);
//		List<String> lines = FileUtils.readLines(new File("D:\\corpus\\duplicate\\duplicate_raws_version0\\test.txt"),
//				"utf-8");
//		List<BigInteger> simhashvalue = new ArrayList<BigInteger>();
//		List<Set<Integer>> articleSet = new ArrayList<Set<Integer>>();
//		// Map<Integer,String> indexMap = new HashMap<Integer,String>();
//		MapSymbolTable symtable = new MapSymbolTable();
//		int idx = 0;
//		for (int i = 0; i < lines.size(); i++) {
//			String[] splits = lines.get(i).split("\t+");
//			if (splits.length != 4) {
//				continue;
//			}
//			String url = splits[1];
//			String title = splits[2];
//			String raw1 = title + RegexUtils.cleanParaAndImgLabel(splits[3]);
//			Set<Integer> set = new HashSet<Integer>();
//			for (String term : AnsjTokenizerFactory.getIstance().tokenizer(raw1.toCharArray(), 0, raw1.length())) {
//				int id = symtable.getOrAddSymbol(term.split("/")[0]);
//				set.add(id);
//			}
//			//
//			articleSet.add(set);
//			simhashvalue.add(calSimhash128(raw1));
//			if (i % 5000 == 0) {
//				System.out.println(i);
//			}
//			// indexMap.put(idx, title);
//			idx++;
//		}
//
//		// for(int i=0;i<simhashvalue.size();i++) {
//		// FileUtils.write(new File("D://haming3"), String.format(">>>>>%s\n",
//		// indexMap.get(i)),"utf-8",true);
//		// for(int j=i+1;j<simhashvalue.size();j++) {
//		// double jac = jaccardIndex(articleSet.get(i), articleSet.get(j));
//		//
//		// int haming = simhash.hammingDistance(simhashvalue.get(i),
//		// simhashvalue.get(j));
//		// if (haming <= 3 && jac<0.6) {
//		// System.out.println(out(simhashvalue.get(i)));
//		// System.out.println(out(simhashvalue.get(j)));
//		// FileUtils.write(new File("D://haming3"), String.format("%s\n",
//		// indexMap.get(j)),"utf-8",true);
//		// }
//		// }
//		// FileUtils.write(new File("D://haming3"),
//		// "*****************************\n","utf-8",true);
//		// }
//		for (int f = 0; f <= 0; f++) {
//			for (int k = 3; k <= 10; k++) {
//				int TP = 0;
//				int FP = 0;
//				int FN = 0;
//				int TN = 0;
//				int count = 0;
//				for (int i = 0; i < articleSet.size(); i++) {
//					for (int j = i; j < articleSet.size(); j++) {
//						if (i == j) {
//							continue;
//						}
//						double jac = jaccardIndex(articleSet.get(i), articleSet.get(j));
//						int haming = SimHashUtil.hammingDistance(simhashvalue.get(i), simhashvalue.get(j));
//						if (jac >= 0.6 + f * 0.1) {
//							if (haming <= k) {
//								TP++;
//							} else {
//								FN++;
//							}
//						} else {
//							if (haming <= k) {
//								FP++;
//							} else {
//								TN++;
//							}
//						}
//						count++;
//						if(count%100000 == 0) {
//							System.out.println(String.format("f=%.2f,k= %d\nTP = %d\nFP = %d\nFN = %d\nTN = %d\n$$$$$$$$$$$$",
//									0.6 + f * 0.1, k, TP, FP, FN, TN));
//						}
//					}
//				}
//				FileUtils.write(new File("D://duplicate_j_h128_result"),
//						String.format("f=%.2f,k= %d\nTP = %d\nFP = %d\nFN = %d\nTN = %d\n*********\n", 0.6 + f * 0.1, k,
//								TP, FP, FN, TN),
//						"utf-8", true);
//			}
//			
//		}
		
	}
	
	public static BigInteger calSimhash128(String content) {
		String filterContent = content.trim().replaceAll("\\p{Punct}|\\p{Space}", "");

		// 按照词语的hash值，计算simHashWeight(低位对齐)
		Integer[] weight = new Integer[128];
		Arrays.fill(weight, 0);
		for (String st : factory.tokenizer(filterContent.toCharArray(), 0, filterContent.length())) {
			long[] wordHashs = Murmur3.hash128(st.getBytes());
			for (int i = 0; i < 64; i++) {
				if (((wordHashs[0] >> i) & 1) == 1) weight[i] += 1;
				else weight[i] -= 1;
			}
			for (int i = 64; i < 128; i++) {
				if (((wordHashs[1] >> i) & 1) == 1) weight[i] += 1;
				else weight[i] -= 1;
			}
		}

		// 计算得到Simhash值
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 128; i++) {
			if (weight[i] > 0) sb.append(1);
			else sb.append(0);
		}

		return new BigInteger(sb.toString(), 2);
	}
	
	public static BigInteger calSimhash64(String content) {
		String filterContent = content.trim().replaceAll("\\p{Punct}|\\p{Space}", "");

		// 按照词语的hash值，计算simHashWeight(低位对齐)
		Integer[] weight = new Integer[64];
		Arrays.fill(weight, 0);
		for (String st : factory.tokenizer(filterContent.toCharArray(), 0, filterContent.length())) {
			long wordHashs = Murmur3.hash64(st.getBytes());
			for (int i = 0; i < 64; i++) {
				if (((wordHashs >> i) & 1) == 1) weight[i] += 1;
				else weight[i] -= 1;
			}
		}

		// 计算得到Simhash值
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 64; i++) {
			if (weight[i] > 0) sb.append(1);
			else sb.append(0);
		}

		return new BigInteger(sb.toString(), 2);
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
