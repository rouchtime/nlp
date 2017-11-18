package com.rouchtime.nlp.duplicate.simhash;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.aliasi.symbol.MapSymbolTable;
import com.aliasi.tokenizer.NGramTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import tokenizer.AnsjNlpTokenizerFactory;
import tokenizer.AnsjTokenizerFactory;

public class DuplicateTest {
	private static TokenizerFactory TOKENIZERFACTORY = tokenizerFactory();

	public static void main(String[] args) throws IOException {
		Simhash simhash = new Simhash(TOKENIZERFACTORY);
		MapSymbolTable table = new MapSymbolTable();
		List<Set<Integer>> listDoc = new ArrayList<Set<Integer>>();
		List<Long> listhash = new ArrayList<Long>();
		List<String> lines = FileUtils.readLines(new File("D:\\corpus\\test.txt"));
		int readCount = 0;
		for (String line : lines) {
			JSONObject jsonObject = JSON.parseObject(line);
			String article = jsonObject.getString("article");
			Set<Integer> set = new HashSet<Integer>();
			for (String token : TOKENIZERFACTORY.tokenizer(article.toCharArray(), 0, article.length())) {
				int id = table.getOrAddSymbolInteger(token);
				set.add(id);
			}
			listDoc.add(set);
			listhash.add(simhash.simhash64(article));
			readCount++;
			if(readCount%1000==0) {
				System.out.println(readCount);
			}
		}
		int TP = 0;
		int TN = 0;
		int FP = 0;
		int FN = 0;
		int count = 0;
		for (int i = 0; i < listhash.size(); i++) {
			for(int j=i+1;j<listhash.size();j++) {
				double jaccard = jaccardIndex(listDoc.get(i), listDoc.get(j));
				int ham = simhash.hammingDistance(listhash.get(i), listhash.get(j));
				
				if(jaccard >= 0.6) {
					if(ham<=3) {
						TP++;
					} else {
						FP++;
					}
				}else {
					if(ham<=3) {
						FN++;
					}else {
						TN++;
					}
					
				}
				count++;
				if(count % 10000 == 0) {
					System.out.println(String.format("TP = %s\nFP = %s\nFN = %s\nTN = %s\n", TP,FP,FN,TN));
				}
			}
		}

	}

    public static double jaccardIndex(
            final Set<Integer> s1, final Set<Integer> s2) {

        Set<Integer> intersection = new HashSet<Integer>(s1);
        intersection.retainAll(s2);

        Set<Integer> union = new HashSet<Integer>(s1);
        union.addAll(s2);

        if (union.isEmpty()) {
            return 0;
        }

        return (double) intersection.size() / union.size();
    }
	
	private static TokenizerFactory tokenizerFactory() {
//		NGramTokenizerFactory factory = new NGramTokenizerFactory(2, 2);
//		return factory;
		return AnsjTokenizerFactory.getIstance().enableUserSelfDic(false);
	}
}
