package duplicate.minhash;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class MinHash implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = -6850587525881908646L;


	/**
     * 计算两个集合的Jaccard距离，字典位置形式的
     * @param s1
     * @param s2
     * @return
     */
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
    
    /**
     * 计算两个集合的Jaccard距离，布尔值形式的
     * @param s1
     * @param s2
     * @return
     */
    public static double jaccardIndex(final boolean[] s1, final boolean[] s2) {
        if (s1.length != s2.length) {
            throw new InvalidParameterException("sets must be same size!");
        }
        return jaccardIndex(convert2Set(s1), convert2Set(s2));
    }

    /**
     * 将布尔值转换为字典位置
     *
     * @param array
     * @return
     */
    public static Set<Integer> convert2Set(final boolean[] array) {
        Set<Integer> set = new TreeSet<Integer>();
        for (int i = 0; i < array.length; i++) {
            if (array[i]) {
                set.add(i);
            }
        }
        return set;
    }

    /**
     * 根据要求的阈值，获得签名长度
     *
     * @param error
     * @return size of the signature
     */
    public static int size(final double error) {
        if (error < 0 && error > 1) {
            throw new IllegalArgumentException("error should be in [0 .. 1]");
        }
        return (int) (1 / (error * error));
    }

    /**
     * 签名长度
     */
    private int n;

    /**
     * 哈希函数的参数，(ax+b)%dicSize的a和b
     */
    private long[][] hash_coefs;
    
    /**
     * 字典长度
     */
    private int dict_size;

    
    public MinHash(final int size, final int dict_size) {
        init(size, dict_size, new Random());
    }
    
    public MinHash(final double error, final int dict_size) {
        init(size(error), dict_size, new Random());
    }
    
    public MinHash(final int size, final int dict_size, final long seed) {
        init(size, dict_size, new Random(seed));
    }
    
    public MinHash(final double error, final int dict_size, final long seed) {
        init(size(error), dict_size, new Random(seed));
    }
    
    public final int[] signature(final boolean[] vector) {
        if (vector.length != dict_size) {
            throw new IllegalArgumentException(
                    "Size of array should be dict_size");
        }

        return signature(convert2Set(vector));
    }
    
    public final int[] signature(final Set<Integer> set) {
        int[] sig = new int[n];

        for (int i = 0; i < n; i++) {
            sig[i] = Integer.MAX_VALUE;
        }
        
        final List<Integer> list = new ArrayList<Integer>(set);
        Collections.sort(list);
        for (final int r : list) {
            for (int i = 0; i < n; i++) {
                sig[i] = Math.min(
                        sig[i],
                        h(i, r));
            }
        }
        return sig;
    }
    
    public final double similarity(final int[] sig1, final int[] sig2) {
        if (sig1.length != sig2.length) {
            throw new IllegalArgumentException(
                    "Size of signatures should be the same");
        }

        double sim = 0;
        for (int i = 0; i < sig1.length; i++) {
            if (sig1[i] == sig2[i]) {
                sim += 1;
            }
        }

        return sim / sig1.length;
    }
    
    public final double error() {
        return 1.0 / Math.sqrt(n);
    }
    
    private void init(final int size, final int dict_size, final Random r) {
        if (size <= 0) {
            throw new InvalidParameterException(
                    "Signature size should be positive");
        }

        if (dict_size <= 0) {
            throw new InvalidParameterException(
                    "Dictionary size (or vector size) should be positive");
        }

        // In function h(i, x) the largest value could be
        // dict_size * dict_size + dict_size
        // throw an error if dict_size * dict_size + dict_size > Long.MAX_VALUE
        if (dict_size > (Long.MAX_VALUE - dict_size) / dict_size) {
            throw new InvalidParameterException(
                    "Dictionary size (or vector size) is too big and will "
                            + "cause a multiplication overflow");
        }

        this.dict_size = dict_size;
        this.n = size;

        // h = (a * x) + b
        // a and b should be randomly generated
        hash_coefs = new long[n][2];
        for (int i = 0; i < n; i++) {
            hash_coefs[i][0] = r.nextInt(dict_size); // a
            hash_coefs[i][1] = r.nextInt(dict_size); // b
        }
    }

    /**
     * 计算 hi(x) as (a_i * x + b_i) % dict_size.
     *
     * @param i
     * @param x
     * @return 
     */
    private int h(final int i, final int x) {
        return (int)
                ((hash_coefs[i][0] * (long) x + hash_coefs[i][1]) % dict_size);
    }
    
    public final long[][] getCoefficients() {
        return hash_coefs;
    }


}
