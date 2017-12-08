package com.rouchtime.nlp.duplicate.simhash;

import java.util.Arrays;
import java.util.List;

import com.aliasi.tokenizer.TokenizerFactory;

/**
 * @author zhangcheng
 * 
 */
public class Simhash {

	private TokenizerFactory tokenizerFactory;

	public Simhash(TokenizerFactory tokenizerFactory) {
		this.tokenizerFactory = tokenizerFactory;
	}

	public int hammingDistance(int hash1, int hash2) {
		int i = hash1 ^ hash2;
		i = i - ((i >>> 1) & 0x55555555);
		i = (i & 0x33333333) + ((i >>> 2) & 0x33333333);
		i = (i + (i >>> 4)) & 0x0f0f0f0f;
		i = i + (i >>> 8);
		i = i + (i >>> 16);
		return i & 0x3f;
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

	public int hammingDistance(long hash1[], long hash2[]) {
		int sum = 0;
		long i = hash1[0] ^ hash2[0];
		i = i - ((i >>> 1) & 0x5555555555555555L);
		i = (i & 0x3333333333333333L) + ((i >>> 2) & 0x3333333333333333L);
		i = (i + (i >>> 4)) & 0x0f0f0f0f0f0f0f0fL;
		i = i + (i >>> 8);
		i = i + (i >>> 16);
		i = i + (i >>> 32);
		sum += ((int) i & 0x7f);

		i = hash1[1] ^ hash2[1];
		i = i - ((i >>> 1) & 0x5555555555555555L);
		i = (i & 0x3333333333333333L) + ((i >>> 2) & 0x3333333333333333L);
		i = (i + (i >>> 4)) & 0x0f0f0f0f0f0f0f0fL;
		i = i + (i >>> 8);
		i = i + (i >>> 16);
		i = i + (i >>> 32);
		sum += ((int) i & 0x7f);
		return sum;
	}

	public long[] simhash128(String doc) {
		int bitLen = 128;
		int[] bits = new int[bitLen];
		for (String t : tokenizerFactory.tokenizer(doc.toCharArray(), 0, doc.length())) {
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

	public long simhash64(String doc) {
		int bitLen = 64;
		int[] bits = new int[bitLen];
		for (String t : tokenizerFactory.tokenizer(doc.toCharArray(), 0, doc.length())) {
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

	public long simhash32(String doc) {
		int bitLen = 32;
		int[] bits = new int[bitLen];
		for (String t : tokenizerFactory.tokenizer(doc.toCharArray(), 0, doc.length())) {
			int v = MurmurHash.hash32(t);
			for (int i = bitLen; i >= 1; --i) {
				if (((v >> (bitLen - i)) & 1) == 1)
					++bits[i - 1];
				else
					--bits[i - 1];
			}
		}
		int hash = 0x00000000;
		int one = 0x00000001;
		for (int i = bitLen; i >= 1; --i) {
			if (bits[i - 1] > 1) {
				hash |= one;
			}
			one = one << 1;
		}
		return hash;
	}
}
