package com.rouchtime.nlp.duplicate.minhash;

import java.io.Serializable;

public abstract class LSH implements Serializable{


    /**
	 * 
	 */
	private static final long serialVersionUID = -4396505279134864136L;
	protected static final long LARGE_PRIME =  433494437;
    private static final int DEFAULT_STAGES = 3;
    private static final int DEFAULT_BUCKETS = 10;

    private int stages = DEFAULT_STAGES;
    private int buckets = DEFAULT_BUCKETS;
    
    public LSH(final int stages, final int buckets) {
        this.stages = stages;
        this.buckets = buckets;
    }
    
    public LSH() {

    }
    
    /**
     * LSH的根据签名的放大处理
     * @param signature
     * @return
     */
    public final int[] hashSignature(final int[] signature) {

        // Create an accumulator for each stage
        int[] hash = new int[stages];

        // Number of rows per stage
        int rows = signature.length / stages;

        for (int i = 0; i < signature.length; i++) {
            int stage = Math.min(i / rows, stages - 1);
            hash[stage] = (int)
                    ((hash[stage] + (long) signature[i] * LARGE_PRIME)
                    % buckets);

        }

        return hash;
    }
    
    public final int[] hashSignature(final boolean[] signature) {

        // Create an accumulator for each stage
        long[] acc = new long[stages];
        for (int i = 0; i < stages; i++) {
            acc[i] = 0;
        }

        // Number of rows per stage
        int rows = signature.length / stages;

        for (int i = 0; i < signature.length; i++) {
            long v = 0;
            if (signature[i]) {
                v = (i + 1) * LARGE_PRIME;
            }

            // current stage
            int j = Math.min(i / rows, stages - 1);
            acc[j] = (acc[j] + v) % Integer.MAX_VALUE;
        }

        int[] r = new int[stages];
        for (int i = 0; i < stages; i++) {
            r[i] = (int) (acc[i] % buckets);
        }

        return r;
    }



}
