package com.rouchtime.nlp.duplicate.bean;
import java.util.Set;

import com.rouchtime.nlp.common.Builder;

public class Fingerprint extends DuplicateBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5632667256148758713L;

	public Fingerprint() {
		super();
	}

	private int[] hash;
	private Set<Integer> vector;
	
	public int[] getHash() {
		return hash;
	}

	public Set<Integer> getVector() {
		return vector;
	}

	public void setHash(int[] hash) {
		this.hash = hash;
	}

	public void setVector(Set<Integer> vector) {
		this.vector = vector;
	}

	public static class FingerprintBuilder implements Builder<Fingerprint>
    {
        // 必须参数
        private final int[] hash;// required
        private final Set<Integer> vector;// required

        // 可选参数
        private String id;
        private String raw;
        private Long timeStamp;

        public FingerprintBuilder(int[] hash, Set<Integer> vector)
        {
            this.hash = hash;
            this.vector = vector;
        }
        
        public FingerprintBuilder id(String id)
        {
            this.id = id;
            return this;
        }

        public FingerprintBuilder raw(String raw)
        {
            this.raw = raw;
            return this;
        }

        public FingerprintBuilder timeStamp(Long timeStamp)
        {
            this.timeStamp = timeStamp;
            return this;
        }
        
		@Override
		public Fingerprint builder() {
			return new Fingerprint(this);
		}
    }
	
    private Fingerprint(FingerprintBuilder builder)
    {
    	hash = builder.hash;
    	vector = builder.vector;
    	if(builder.raw!=null) {
    		super.setRaw(builder.raw);
    	}
    	if(builder.id!=null) {
    		super.setId(builder.id);
    	}
    	if(builder.timeStamp!=null) {
    		super.setTimestamp(builder.timeStamp);
    	}
    }
}
