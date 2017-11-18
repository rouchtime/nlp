package com.rouchtime.nlp.duplicate.simhash;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.aliasi.spell.EditDistance;
import com.aliasi.symbol.MapSymbolTable;
import com.aliasi.tokenizer.NGramTokenizerFactory;
import com.aliasi.util.Distance;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.ScoredObject;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.rouchtime.util.RegexUtils;

import tokenizer.AnsjTokenizerFactory;

public class KeywordLSH {
	public static void main(String[] args) throws IOException {
		 int count = 0;
		List<String> lines = FileUtils.readLines(new File("D:\\corpus\\duplicate\\duplicate_raws_version0\\xaa"),
				"utf-8");
		long sum = 0l;
		int cc = 0;
		for (String line : lines) {
			String[] splits = line.split("\t+");
			if (splits.length != 4) {
				continue;
			}
			String title = splits[2];
			String raw1 = title + RegexUtils.cleanParaAndImgLabel(splits[3]);
			long s = System.currentTimeMillis();
			if (HanLP.extractKeyword(RegexUtils.cleanSpecialWord(RegexUtils.cleanParaAndImgLabel(raw1)), 30).size() < 7) {
				count++;
			}
			long e = System.currentTimeMillis() - s;
			sum+=e;
			cc++;
			if(cc%100==0) {
				System.out.println(sum*1.0/cc);
			}

		}
		System.out.println(count);
//		List<Set<Integer>> keywordcode = new ArrayList<Set<Integer>>();
//		List<Set<Integer>> articleSet = new ArrayList<Set<Integer>>();
//		// Map<Integer,String> indexMap = new HashMap<Integer,String>();
//		MapSymbolTable symtable = new MapSymbolTable();
//		NGramTokenizerFactory ngram = new NGramTokenizerFactory(2, 2);
//		int idx = 0;
//		for (int i = 0; i < lines.size(); i++) {
//			String[] splits = lines.get(i).split("\t+");
//			if (splits.length != 4) {
//				continue;
//			}
//			String title = splits[2];
//			String raw1 = title + RegexUtils.cleanParaAndImgLabel(splits[3]);
//			Set<Integer> set = new HashSet<Integer>();
//			for (Term term : HanLP.segment(raw1)) {
//				int id = symtable.getOrAddSymbol(term.word);
//				set.add(id);
//			}
//			List<String> keywords = HanLP.extractKeyword(raw1, 30);
//
//			Set<Integer> keywordset = new HashSet<Integer>();
//			for (int k = 0; k < 40 && k < keywords.size(); k++) {
//				keywordset.add(symtable.getOrAddSymbol(keywords.get(k)));
//			}
//			keywordcode.add(keywordset);
//			articleSet.add(set);
//		}
//
//		int TP = 0;
//		int FP = 0;
//		int FN = 0;
//		int TN = 0;
//		int count = 0;
//		for (int i = 0; i < articleSet.size(); i++) {
//			for (int j = i; j < articleSet.size(); j++) {
//				if (i == j) {
//					continue;
//				}
//				double jac = jaccardIndex(articleSet.get(i), articleSet.get(j));
//				double haming = (1 - jaccardIndex(keywordcode.get(i), keywordcode.get(j)));
//				if (jac >= 0.6) {
//					if (haming <= 0.14) {
//						TP++;
//					} else {
//						FN++;
//					}
//				} else {
//					if (haming <= 0.14) {
//						FP++;
//					} else {
//						TN++;
//					}
//				}
//				count++;
//				if (count % 100000 == 0) {
//					System.out.println(String.format("TP = %d\nFP = %d\nFN = %d\nTN = %d\n", TP, FP, FN, TN));
//				}
//			}
//		}
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
