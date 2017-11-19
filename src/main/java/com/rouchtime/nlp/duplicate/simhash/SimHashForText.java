package com.rouchtime.nlp.duplicate.simhash;

import java.math.BigInteger;

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
	
	

	private SimHashForText() {
		this.factory = new NGramTokenizerFactory(2, 3);
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
