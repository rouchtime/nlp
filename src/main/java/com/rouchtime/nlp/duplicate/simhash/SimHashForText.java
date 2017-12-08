package com.rouchtime.nlp.duplicate.simhash;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.aliasi.tokenizer.NGramTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

public class SimHashForText {
	private TokenizerFactory factory;
	private  BigInteger m = new BigInteger("1000003");
	private  BigInteger mask = new BigInteger("18446744073709551615");
	
	/**
	 * 获得文档的指纹
	 * @param str
	 * @param hashbits
	 * @return
	 */
	public long getFingerPrint(String str, int hashbits) {
		String filterContent = str.trim().replaceAll("\\p{Punct}|\\p{Space}", "");
		int[] bits = new int[hashbits];
		for (String term : factory.tokenizer(filterContent.toCharArray(), 0, filterContent.length())) {
			String _tmp = term;
			long v = hash(_tmp, hashbits).longValue();
			for (int i = hashbits; i >= 1; --i) {
				if (((v >> (hashbits - i)) & 1) == 1)
					++bits[i - 1];
				else
					--bits[i - 1];
			}
		}
		long hash = 0x0000000000000000;
		long one = 0x0000000000000001;
		for (int i = hashbits; i >= 1; --i) {
			if (bits[i - 1] > 0) {
				hash |= one;
			}
			one = one << 1;
		}
		return hash;
	}
	
	public long getFingerPrintMurmur(String doc) {
		int bitLen = 64;
		int[] bits = new int[bitLen];
		for (String t : factory.tokenizer(doc.toCharArray(), 0, doc.length())) {
			long v = Murmur3.hash64(t.getBytes());
			for (int i = bitLen; i >= 1; --i) {
				if (((v >> (bitLen - i)) & 1) == 1)
					++bits[i - 1];
				else
					--bits[i - 1];
			}
		}
		long hash = 0x0000000000000000;
		long one = 0x0000000000000001;
		for (int i = bitLen; i >= 1; --i) {
			if (bits[i - 1] > 0) {
				hash |= one;
			}
			one = one << 1;
		}
		return hash;
	}
	
	public long[] getFingerPrintMurmur128(String doc) {
		int bitLen = 128;
		int[] bits = new int[bitLen];
		for (String t : factory.tokenizer(doc.toCharArray(), 0, doc.length())) {
			long[] vs = Murmur3.hash128(t.getBytes());
			long v0 = vs[0];
			long v1 = vs[1];
			for (int i = bitLen; i >= 65; --i) {
				if (((v0 >> (bitLen - i)) & 1) == 1)
					++bits[i - 1];
				else
					--bits[i - 1];
			}
			for (int i = 64; i >= 1; --i) {
				if (((v1 >> (bitLen - i)) & 1) == 1)
					++bits[i - 1];
				else
					--bits[i - 1];
			}
		}
		long hash0 = 0x0000000000000000;
		long one = 0x0000000000000001;
		for (int i = bitLen; i >= 65; --i) {
			if (bits[i - 1] > 0) {
				hash0 |= one;
			}
			one = one << 1;
		}
		long hash1 = 0x0000000000000000;
		one = 0x0000000000000001;
		for (int i = 64; i >= 1; --i) {
			if (bits[i - 1] > 0) {
				hash1 |= one;
			}
			one = one << 1;
		}
		return new long[] { hash0, hash1 };
	}
	
	public long getFingerPrintByCity(String doc) {
		int bitLen = 64;
		int[] bits = new int[bitLen];
		for (String t : factory.tokenizer(doc.toCharArray(), 0, doc.length())) {
			long v = CityHash.cityHash64WithSeed(t.getBytes(), 0, t.length(), 100l);
			for (int i = bitLen; i >= 1; --i) {
				if (((v >> (bitLen - i)) & 1) == 1)
					++bits[i - 1];
				else
					--bits[i - 1];
			}
		}
		long hash = 0x0000000000000000;
		long one = 0x0000000000000001;
		for (int i = bitLen; i >= 1; --i) {
			if (bits[i - 1] > 0) {
				hash |= one;
			}
			one = one << 1;
		}
		return hash;
	}
	
	
	private  BigInteger hash(String source, int hashbits) {
		if (source == null || source.length() == 0) {
			return new BigInteger("0");
		} else {
			char[] sourceArray = source.toCharArray();
			BigInteger x = BigInteger.valueOf(((long) sourceArray[0]) << 7);
			for (char item : sourceArray) {
				BigInteger temp = BigInteger.valueOf((long) item);
				x = x.multiply(m).xor(temp).and(mask);
			}
			x = x.xor(new BigInteger(String.valueOf(source.length())));
			if (x.equals(new BigInteger("-1"))) {
				x = new BigInteger("-2");
			}
			return x;
		}
	}
	
	public int hammingDistance(long hash1, long hash2) {
		long i = hash1 ^ hash2;
		i = i - ((i >>> 1) & 0x5555555555555555L);
		i = (i & 0x3333333333333333L) + ((i >>> 2) & 0x3333333333333333L);
		i = (i + (i >>> 4)) & 0x0f0f0f0f0f0f0f0fL;
		i = i + (i >>> 8);
		i = i + (i >>> 16);
		i = i + (i >>> 32);
		return (int) i & 0x7f;
	}
	
	public long[] splitFingerPrint(Long simhash) {
		StringBuilder sb = new StringBuilder();
		String[] splitHashValue = new String[9];

		/* 分8份，C(2,8) */
		// for (int i = 0; i < 64; i++) {
		// sb.append(simhash >> i & 1);
		// if ((i + 1) % 8 == 0) {
		// splitHashValue[(i + 1) / 8 - 1] = sb.toString();
		// sb.setLength(0);
		// }
		// }

		for (int i = 0; i < 56; i++) {
			sb.append(simhash >> i & 1);
			if ((i + 1) % 7 == 0) {
				splitHashValue[(i + 1) / 7 - 1] = sb.toString();
				sb.setLength(0);
			}
		}
		for (int i = 56; i < 64; i++) {
			sb.append(simhash >> i & 1);
			splitHashValue[8] = sb.toString();
			sb.setLength(0);
		}
		List<String[]> arrays = new ArrayList<String[]>();
		combinationSelect(splitHashValue, 3, arrays);
		long[] result = new long[arrays.size()];
		int k = 0;
		for (String[] list : arrays) {
			String _tmp = list[0] + list[1] + list[2];
			BigInteger bigInt = new BigInteger(_tmp, 2);
			result[k++] = bigInt.longValue();
		}
		return result;
	}

	/**
	 * 组合选择（从列表中选择n个组合）
	 * 
	 * @param dataList
	 *            待选列表
	 * @param n
	 *            选择个数
	 */
	private void combinationSelect(String[] dataList, int n, List<String[]> arrays) {
		combinationSelect(dataList, 0, new String[n], 0, arrays);
	}

	/**
	 * 组合选择
	 * 
	 * @param dataList
	 *            待选列表
	 * @param dataIndex
	 *            待选开始索引
	 * @param resultList
	 *            前面（resultIndex-1）个的组合结果
	 * @param resultIndex
	 *            选择索引，从0开始
	 */
	private void combinationSelect(String[] dataList, int dataIndex, String[] resultList, int resultIndex,
			List<String[]> arrays) {
		int resultLen = resultList.length;
		int resultCount = resultIndex + 1;
		if (resultCount > resultLen) { // 全部选择完时，输出组合结果
			String[] list = new String[resultList.length];
			for (int i = 0; i < resultList.length; i++) {
				list[i] = resultList[i];
			}
			arrays.add(list);
			return;
		}
		// 递归选择下一个
		for (int i = dataIndex; i < dataList.length + resultCount - resultLen; i++) {
			resultList[resultIndex] = dataList[i];
			combinationSelect(dataList, i + 1, resultList, resultIndex + 1, arrays);
		}
	}

	private SimHashForText() {
		this.factory = new NGramTokenizerFactory(2, 4);
	}
	
	public static SimHashForText getInstance() {
		return SingletonHolder.instance;
	}

	public static SimHashForText getInstance(TokenizerFactory factory) {
		SimHashForText simHashForText =  SingletonHolder.instance;
		simHashForText.factory = factory;
		return simHashForText;
	}
	
	private static class SingletonHolder {
		private static SimHashForText instance = new SimHashForText();
	}

}
