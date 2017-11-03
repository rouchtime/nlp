package com.rouchtime.nlp.keyword;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * 初始化
 * @author 龚帅宾
 *
 */
public class WordDictionary {
	
	private Logger logger = Logger.getLogger(WordDictionary.class);
	/**
	 * 词向量
	 */
	private List<double[]> WORD_VECTOR_LIST = new ArrayList<double[]>();
	
	/**
	 * 词的索引
	 */
	private Map<String, Integer> WORD_INDEX_MAP = new HashMap<String, Integer>();
	
	/**
	 * 词的列表
	 */
	private List<String> WORDLIST = new ArrayList<String>();
	private int W2VLENGTH = 200;
	
	/**
	 * 词IDF值
	 */
	private Map<String, Double> IDFMAP = new HashMap<String, Double>();

	private WordDictionary() {
		long s = System.currentTimeMillis();
		logger.info("init w2v, please waiting ...");
		initW2V();
		logger.info("init idf, please waiting ...");
		initIDF();
		logger.info(String.format("init cost:%ds", (System.currentTimeMillis() - s)/1000));
	}

	
	public List<double[]> getWORD_VECTOR_LIST() {
		return WORD_VECTOR_LIST;
	}


	public Map<String, Integer> getWORD_INDEX_MAP() {
		return WORD_INDEX_MAP;
	}


	public List<String> getWORDLIST() {
		return WORDLIST;
	}

	public Map<String, Double> getIDFMAP() {
		return IDFMAP;
	}


	private void initW2V() {
		InputStream in = WordDictionary.class.getClassLoader().getResourceAsStream("w2v");
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		try {
			line = br.readLine();
			String[] splits = null;
			int i = 0;
			while (line != null) {
				splits = line.split("\\s+");
				if (splits.length != (W2VLENGTH + 1)) {
					continue;
				}
				String w = splits[0];
				WORD_INDEX_MAP.put(w, i);
				WORDLIST.add(w);
				double[] v = new double[W2VLENGTH];
				for (int l = 1; l < splits.length; l++) {
					v[l - 1] = Double.parseDouble(splits[l]);
				}
				WORD_VECTOR_LIST.add(v);
				line = br.readLine();
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void initIDF() {
		InputStream in = WordDictionary.class.getClassLoader().getResourceAsStream("IDF_20170418_new.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		try {
			line = br.readLine();
			String[] splits = null;
			while (line != null) {
				splits = line.split("\t");
				if (splits.length != 2) {
					continue;
				}
				String word = splits[0];
				double idf = Double.parseDouble(splits[1]);
				if(IDFMAP.get(word) != null) {
					continue;
				}
				IDFMAP.put(word, idf);
				line = br.readLine();
			}
			br.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static WordDictionary getInstance(){
		return SingletonHolder.instance;
	}
	
	private static class SingletonHolder{        
        private static WordDictionary instance = new WordDictionary();        
    }
}
