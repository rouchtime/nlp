package com.rouchtime.util;

import java.math.BigInteger;
import java.util.List;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import tokenizer.HanLPTokenizerFactory;


/**
 * 
 * @author 岳海亮
 * @email yhl@feheadline.com
 * @date 2014年11月28日
 */
public class SimHashUtil {

	/**
	 * @param str
	 * @param hashbits
	 *            生成的simhash的位数
	 * @return
	 */
	public static BigInteger getSimHash(String str, int hashbits) {
		// 定义特征向量/数组
		int[] v = new int[hashbits];
		for (String term : HanLPTokenizerFactory.getIstance().tokenizer(str.toCharArray(), 0, str.length())) {
			String word = term.split(Contants.SLASH)[0];
			String _tmp = word;
			if (!RegexUtils.containsCNStr(_tmp)) {
				continue;
			}
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
	
	public static BigInteger getSimHash(String str) {
		return getSimHash(str, 64);
	}

	public static BigInteger hash(String source, int hashbits) {
		if (source == null || source.length() == 0) {
			return new BigInteger("0");
		} else {
			char[] sourceArray = source.toCharArray();
			BigInteger x = BigInteger.valueOf(((long) sourceArray[0]) << 7);
			BigInteger m = new BigInteger("1000003");
			BigInteger mask = new BigInteger("2").pow(hashbits).subtract(
					new BigInteger("1"));
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

	public static void main(String[] args) {
		
//		BigInteger fingerprint = new BigInteger("1");
//		for (int i = 0; i < 64; i++) {
//			if (i != 0)
//				fingerprint = fingerprint.add(new BigInteger("1").shiftLeft(i));
//		}
//		System.out.println(fingerprint);
//		System.out.println(fingerprint.toString().length());
//		System.out.println("2305846986622244908".length());
	}
}