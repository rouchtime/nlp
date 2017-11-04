package com.rouchtime.nlp.sentence;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.CharacterTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;

public class SummarizationSentenceModel extends ChineseHeuristicSentenceModel implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1742750116346026764L;

	public SummarizationSentenceModel() {
		super(POSSIBLE_STOPS,IMPOSSIBLE_PENULTIMATES, IMPOSSIBLE_SENTENCE_STARTS, true, true);
	}

	private static final Set<String> POSSIBLE_STOPS = new HashSet<String>();
	static {
		POSSIBLE_STOPS.add("。");
		POSSIBLE_STOPS.add("；"); 
		POSSIBLE_STOPS.add("！");
		POSSIBLE_STOPS.add("？");
	}
	
	private static final Set<String> IMPOSSIBLE_PENULTIMATES = new HashSet<String>();
	static {
		
	}
	private static final Set<String> IMPOSSIBLE_SENTENCE_STARTS = new HashSet<String>();
	static {
		IMPOSSIBLE_SENTENCE_STARTS.add("因此");
		IMPOSSIBLE_SENTENCE_STARTS.add("所以");
		IMPOSSIBLE_SENTENCE_STARTS.add("但是");
		IMPOSSIBLE_SENTENCE_STARTS.add("但");
		IMPOSSIBLE_SENTENCE_STARTS.add("而且");
	}

	public static final SummarizationSentenceModel INSTANCE = new SummarizationSentenceModel();

	static class Serializer extends AbstractExternalizable {
		static final long serialVersionUID = 8384392069391677984L;

		public Serializer() {
		}

		public void writeExternal(ObjectOutput out) {
		}

		public Object read(ObjectInput in) {
			return MedlineSentenceModel.INSTANCE;
		}
	}
}
