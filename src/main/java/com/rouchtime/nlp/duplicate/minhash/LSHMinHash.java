package com.rouchtime.nlp.duplicate.minhash;

import java.util.Set;

/**
 * MinHash+LSH
 * 
 * @author 龚帅宾
 *
 */
public class LSHMinHash extends LSH {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6970838822591377865L;
	private final MinHash mh;

	/**
	 * 初始化函数
	 * 
	 * @param band 行条数
	 * @param buckets LSH的hash桶数
	 * @param dicSize 字典数
	 * @param threshold 阈值
	 */
	public LSHMinHash(final int band, final int buckets, final int dicSize, final double threshold) {
		super(band, buckets);
		int signature_size = computeSignatureSize(band, threshold);
		this.mh = new MinHash(signature_size, dicSize);
	}

	/**
	 * 
	 * @param band 行条数
	 * @param buckets LSH的hash桶数
	 * @param dicSize 字典数
	 * @param threshold 阈值
	 * @param seed
	 */
	public LSHMinHash(final int band, final int buckets, final int dicSize, final double threshold, final long seed) {
		super(band, buckets);
		int signature_size = computeSignatureSize(band, threshold);
		this.mh = new MinHash(signature_size, dicSize, seed);
	}
	
	/**
	 * 
	 * @param band 行条数
	 * @param buckets LSH的hash桶数
	 * @param dicSize 字典数
	 * @param threshold 阈值
	 * @param seed
	 */
	public LSHMinHash(final int band, final int buckets, final double threshold, final long seed) {
		super(band, buckets);
		int signature_size = computeSignatureSize(band, threshold);
		this.mh = new MinHash(signature_size, (int)LSH.LARGE_PRIME, seed);
	}
	
	/**
	 * 根据阈值THRESHOLD，来计算签名长度，根据公式阈值近似为t=(1/b)^(1/r),又可知签名长度n=b*r，此处的加一为平滑
	 * @param band
	 * @param threshold
	 * @return
	 */
	private int computeSignatureSize(int band, double threshold) {
		int r = (int) Math.ceil(Math.log(1.0 / band) / Math.log(threshold)) + 1;
		return r * band;
	}

	/**
	 * 通过集合的所在字典中的布尔值(此处布尔矩阵非常稀疏),根据布尔矩阵和最小哈希来计算签名，而后通过LSH将签名进行放大处理1-(1-s^r)^b
	 * 
	 * @param vector
	 * @return
	 */
	public final int[] hash(final boolean[] vector) {
		return hashSignature(this.mh.signature(vector));
	}

	/**
	 * 通过集合的所在字典中的位置，根据最小哈希来计算签名，而后通过LSH将签名进行放大处理1-(1-s^r)^b
	 * 
	 * @param set
	 * @return
	 */
	public final int[] hash(final Set<Integer> set) {
		return hashSignature(this.mh.signature(set));
	}

	/**
	 * 获得最小hash的随机排列转换的参数，即(ax+b)%dicSize的a和b
	 * 
	 * @return
	 */
	public final long[][] getCoefficients() {
		return mh.getCoefficients();
	}

}
