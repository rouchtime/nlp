package com.rouchtime.nlp.keyword.similarity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;////
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
/**
 * {@link #CiLinWordSimilarity}
 * @author Admin
 *
 */
public class CiLinWordSimilarity implements WordSimiarity{
	private Logger logger = Logger.getLogger(CiLinWordSimilarity.class);
	private HashMap<String, List<String>> keyWord_Identifier_HashMap;// <关键词，编号List集合>哈希
	private HashMap<String, Integer> first_KeyWord_Depth_HashMap;// <第一层编号，深度>哈希
	private HashMap<String, Integer> second_KeyWord_Depth_HashMap;// <前二层编号，深度>哈希
	private HashMap<String, Integer> third_KeyWord_Depth_HashMap;// <前三层编号，深度>哈希
	private HashMap<String, Integer> fourth_KeyWord_Depth_HashMap;// <前四层编号，深度>哈希
	// public HashMap<String, HashSet<String>> ciLin_Sort_keyWord_HashMap = new
	// HashMap<String, HashSet<String>>();//<(同义词)编号，关键词Set集合>哈希

	private CiLinWordSimilarity() {
		keyWord_Identifier_HashMap = new HashMap<String, List<String>>();
		first_KeyWord_Depth_HashMap = new HashMap<String, Integer>();
		second_KeyWord_Depth_HashMap = new HashMap<String, Integer>();
		third_KeyWord_Depth_HashMap = new HashMap<String, Integer>();
		fourth_KeyWord_Depth_HashMap = new HashMap<String, Integer>();
		initCiLin();
	}

	public static CiLinWordSimilarity getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {
		private static final CiLinWordSimilarity instance = new CiLinWordSimilarity();
	}

	// 3.初始化词林相关
	public void initCiLin() {
		int i;
		String str = null;
		String[] strs = null;
		List<String> list = null;
		BufferedReader inFile = null;
		try {
			logger.info("init cilin dictionary, please waiting ...");
			// 初始化<关键词， 编号set>哈希
			inFile = new BufferedReader(new InputStreamReader(
					CiLinWordSimilarity.class.getClassLoader().getResourceAsStream("cilin/keyWord_Identifier_HashMap.txt"),
					"utf-8"));// 读取文本
			while ((str = inFile.readLine()) != null) {
				strs = str.split(" ");
				list = new Vector<String>();
				for (i = 1; i < strs.length; i++)
					list.add(strs[i]);
				keyWord_Identifier_HashMap.put(strs[0], list);
			}

			// 初始化<第一层编号，高度>哈希
			inFile.close();
			inFile = new BufferedReader(new InputStreamReader(
					CiLinWordSimilarity.class.getClassLoader().getResourceAsStream("cilin/first_KeyWord_Depth_HashMap.txt"),
					"utf-8"));// 读取文本
			while ((str = inFile.readLine()) != null) {
				strs = str.split(" ");
				first_KeyWord_Depth_HashMap.put(strs[0], Integer.valueOf(strs[1]));
			}

			// 初始化<前二层编号，高度>哈希
			inFile.close();
			inFile = new BufferedReader(new InputStreamReader(
					CiLinWordSimilarity.class.getClassLoader().getResourceAsStream("cilin/second_KeyWord_Depth_HashMap.txt"),
					"utf-8"));// 读取文本
			while ((str = inFile.readLine()) != null) {
				strs = str.split(" ");
				second_KeyWord_Depth_HashMap.put(strs[0], Integer.valueOf(strs[1]));
			}

			// 初始化<前三层编号，高度>哈希
			inFile.close();
			inFile = new BufferedReader(new InputStreamReader(
					CiLinWordSimilarity.class.getClassLoader().getResourceAsStream("cilin/third_KeyWord_Depth_HashMap.txt"),
					"utf-8"));// 读取文本
			while ((str = inFile.readLine()) != null) {
				strs = str.split(" ");
				third_KeyWord_Depth_HashMap.put(strs[0], Integer.valueOf(strs[1]));
			}

			// 初始化<前四层编号，高度>哈希
			inFile.close();
			inFile = new BufferedReader(new InputStreamReader(
					CiLinWordSimilarity.class.getClassLoader().getResourceAsStream("cilin/fourth_KeyWord_Depth_HashMap.txt"),
					"utf-8"));// 读取文本
			while ((str = inFile.readLine()) != null) {
				strs = str.split(" ");
				fourth_KeyWord_Depth_HashMap.put(strs[0], Integer.valueOf(strs[1]));
			}
			inFile.close();

		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (inFile != null) {
				try {
					inFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// 根据两个关键词计算相似度
	@Override
	public double calTowWordSimiarity(String key1, String key2) {
		List<String> identifierList1 = null, identifierList2 = null;// 词林编号list
		if (key1.equals(key2))
			return 1.0;

		if (!keyWord_Identifier_HashMap.containsKey(key1) || !keyWord_Identifier_HashMap.containsKey(key2)) {// 其中有一个不在词林中，则返回相似度为0.1
			// System.out.println(key1 + " " + key2 + "有一个不在同义词词林中！");
			return 0.1;
		}
		identifierList1 = keyWord_Identifier_HashMap.get(key1);// 取得第一个词的编号集合
		identifierList2 = keyWord_Identifier_HashMap.get(key2);// 取得第二个词的编号集合

		return getMaxIdentifierSimilarity(identifierList1, identifierList2);
	}

	private double getMaxIdentifierSimilarity(List<String> identifierList1, List<String> identifierList2) {
		int i, j;
		double maxSimilarity = 0, similarity = 0;
		for (i = 0; i < identifierList1.size(); i++) {
			j = 0;
			while (j < identifierList2.size()) {
				similarity = getIdentifierSimilarity(identifierList1.get(i), identifierList2.get(j));
				logger.debug(identifierList1.get(i) + "  " + identifierList2.get(j) + "  " + similarity);
				if (similarity > maxSimilarity)
					maxSimilarity = similarity;
				if (maxSimilarity == 1.0)
					return maxSimilarity;
				j++;
			}
		}
		return maxSimilarity;
	}

	private double getIdentifierSimilarity(String identifier1, String identifier2) {
		int n = 0, k = 0;// n是分支层的节点总数, k是两个分支间的距离.
		// double a = 0.5, b = 0.6, c = 0.7, d = 0.96;
		double a = 0.65, b = 0.8, c = 0.9, d = 0.96;
		if (identifier1.equals(identifier2)) {// 在第五层相等
			if (identifier1.substring(7).equals("="))
				return 1.0;
			else
				return 0.5;
		} else if (identifier1.substring(0, 5).equals(identifier2.substring(0, 5))) {// 在第四层相等 Da13A01=
			n = fourth_KeyWord_Depth_HashMap.get(identifier1.substring(0, 5));
			k = Integer.valueOf(identifier1.substring(5, 7)) - Integer.valueOf(identifier2.substring(5, 7));
			if (k < 0)
				k = -k;
			return Math.cos(n * Math.PI / 180) * ((double) (n - k + 1) / n) * d;
		} else if (identifier1.substring(0, 4).equals(identifier2.substring(0, 4))) {// 在第三层相等 Da13A01=
			n = third_KeyWord_Depth_HashMap.get(identifier1.substring(0, 4));
			k = identifier1.substring(4, 5).charAt(0) - identifier2.substring(4, 5).charAt(0);
			if (k < 0)
				k = -k;
			return Math.cos(n * Math.PI / 180) * ((double) (n - k + 1) / n) * c;
		} else if (identifier1.substring(0, 2).equals(identifier2.substring(0, 2))) {// 在第二层相等
			n = second_KeyWord_Depth_HashMap.get(identifier1.substring(0, 2));
			k = Integer.valueOf(identifier1.substring(2, 4)) - Integer.valueOf(identifier2.substring(2, 4));
			if (k < 0)
				k = -k;
			return Math.cos(n * Math.PI / 180) * ((double) (n - k + 1) / n) * b;
		} else if (identifier1.substring(0, 1).equals(identifier2.substring(0, 1))) {// 在第一层相等
			n = first_KeyWord_Depth_HashMap.get(identifier1.substring(0, 1));
			k = identifier1.substring(1, 2).charAt(0) - identifier2.substring(1, 2).charAt(0);
			if (k < 0)
				k = -k;
			return Math.cos(n * Math.PI / 180) * ((double) (n - k + 1) / n) * a;
		}

		return 0.1;
	}

	@Override
	public boolean isExistWord(String word) {
		if(keyWord_Identifier_HashMap.get(word) == null) {
			return false;
		}
		return true;
	}
}
