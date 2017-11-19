package com.rouchtime.nlp.duplicate.simhash;
import java.math.BigInteger;
import java.util.List;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import com.aliasi.tokenizer.NGramTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import tokenizer.AnsjTokenizerFactory;
import tokenizer.NGramTokenizerBasedOtherTokenizerFactory;

/**
 * 
 */
public class SimHashUtil {
	private static TokenizerFactory ng = new NGramTokenizerFactory(2, 3);
	private static BigInteger m = new BigInteger("1000003");
	private static BigInteger mask = new BigInteger("18446744073709551615");
	/**
	 * @param str
	 * @param hashbits
	 *            生成的simhash的位数
	 * @return
	 */
	public static BigInteger getSimHash(String str, int hashbits) {
		String filterContent = str.trim().replaceAll("\\p{Punct}|\\p{Space}", "");
		// 定义特征向量/数组
		int[] v = new int[hashbits];
		for (String term : ng.tokenizer(filterContent.toCharArray(), 0, filterContent.length())) {
			String _tmp = term;
			BigInteger t = hash(_tmp, hashbits);
			for (int i = 0; i < hashbits; i++) {
				BigInteger bitmask = new BigInteger("1").shiftLeft(i);
				// 3、建立一个长度为64的整数数组(假设要生成64位的数字指纹,也可以是其它数字),
				// 对每一个分词hash后的数列进行判断,如果是1000...1,那么数组的第一位和末尾一位加1,
				// 中间的62位减一,也就是说,逢1加1,逢0减1.一直到把所有的分词hash数列全部判断完毕.
				if (t.and(bitmask).signum() != 0) {
					// 这里是计算整个文档的所有特征的向量和
					// 这里实际使用中需要 +- 权重，比如词频，而不是简单的 +1/-1，
					v[i] += 1;
				} else {
					v[i] -= 1;
				}
			}
		}
		BigInteger fingerprint = new BigInteger("0");
		for (int i = 0; i < hashbits; i++) {
			// 4、最后对数组进行判断,大于0的记为1,小于等于0的记为0,得到一个 64bit 的数字指纹/签名.
			if (v[i] >= 0) {
				fingerprint = fingerprint.add(new BigInteger("1").shiftLeft(i));
			}
		}
		return fingerprint;
	}

	public static long getSimHashVersion2(String str, int hashbits) {
		String filterContent = str.trim().replaceAll("\\p{Punct}|\\p{Space}", "");
		// 定义特征向量/数组
		
		int[] bits = new int[hashbits];
		for (String term : ng.tokenizer(filterContent.toCharArray(), 0, filterContent.length())) {
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
	
	public static BigInteger getSimHash(String str) {
		return getSimHash(str, 64);
	}

	public static BigInteger hash(String source, int hashbits) {
		if (source == null || source.length() == 0) {
			return new BigInteger("0");
		} else {
			char[] sourceArray = source.toCharArray();
			BigInteger x = BigInteger.valueOf(((long) sourceArray[0]) << 7);
//			BigInteger m = new BigInteger("1000003");
//			BigInteger mask = new BigInteger("2").pow(hashbits).subtract(new BigInteger("1"));
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

//	public static long hashVersion2(String source, int hashbits) {
//		if (source == null || source.length() == 0) {
//			return 0l;
//		} else {
//			char[] sourceArray = source.toCharArray();
//			long x = ((long) sourceArray[0]) << 7;
//			BigInteger m = new BigInteger("1000003");
//			BigInteger mask = new BigInteger("2").pow(hashbits).subtract(new BigInteger("1"));
//			for (char item : sourceArray) {
//				BigInteger temp = BigInteger.valueOf((long) item);
//				x = x.multiply(m).xor(temp).and(mask);
//			}
//			x = x.xor(new BigInteger(String.valueOf(source.length())));
//			if (x.equals(new BigInteger("-1"))) {
//				x = new BigInteger("-2");
//			}
//			return x;
//		}
//	}
	
	public static int getDistance(String str1, String str2) {
		int distance;
		if (str1.length() != str2.length()) {
			distance = -1;
		} else {
			distance = 0;
			for (int i = 0; i < str1.length(); i++) {
				if (str1.charAt(i) != str2.charAt(i)) {
					distance++;
				}
			}
		}
		return distance;
	}

	public static int hammingDistance(BigInteger bi1, BigInteger bi2) {
		BigInteger x = bi1.xor(bi2);
		int tot = 0;
		// 统计x中二进制位数为1的个数
		// 我们想想，一个二进制数减去1，那么，从最后那个1（包括那个1）后面的数字全都反了，
		// 对吧，然后，n&(n-1)就相当于把后面的数字清0，
		// 我们看n能做多少次这样的操作就OK了。
		while (x.signum() != 0) {
			tot += 1;
			x = x.and(x.subtract(new BigInteger("1")));
		}
		return tot;
	}

	public static int hammingDistance(long hash1, long hash2) {
		long i = hash1 ^ hash2;
		i = i - ((i >>> 1) & 0x5555555555555555L);
		i = (i & 0x3333333333333333L) + ((i >>> 2) & 0x3333333333333333L);
		i = (i + (i >>> 4)) & 0x0f0f0f0f0f0f0f0fL;
		i = i + (i >>> 8);
		i = i + (i >>> 16);
		i = i + (i >>> 32);
		return (int) i & 0x7f;
	}
	
	public static String out(long simhash) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 128; i++) {
			sb.append(simhash >> i & 1);
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		BigInteger mask = new BigInteger("2").pow(64).subtract(new BigInteger("1"));
		long a = 2l;
		System.out.println((a << 64) - 1);
	}
}

