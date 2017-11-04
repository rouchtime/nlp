package tokenizer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.NLPTokenizer;

import tokenizer.HanLPTokenizerFactory.HanLPTokenizer;

public class CommentTokenzierFactory  implements Serializable, TokenizerFactory{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1878375938617494223L;

	private CommentTokenzierFactory() {

	}

	private static volatile CommentTokenzierFactory instance;

	@Override
	public Tokenizer tokenizer(char[] ch, int start, int length) {
		return new CommentTokenzier(ch, start, length);
	}

	public static CommentTokenzierFactory getIstance() {
		if (instance == null) {
			synchronized (CommentTokenzierFactory.class) {
				if (instance == null) {
					instance = new CommentTokenzierFactory();
				}
			}
		}
		return instance;
	}
	
	class CommentTokenzier extends Tokenizer {
		private List<Term> parse = new ArrayList<Term>();
		private int currentPos = -1;
		private String raw;
		private String remain;
		public CommentTokenzier(char[] ch, int start, int length) {
			String text = String.valueOf(ch);
			parse = NLPTokenizer.segment(text);

		}
		public boolean hasNext() {
			if (parse == null || currentPos >= parse.size() - 1)
				return false;
			return true;
		}
		
		@Override
		public String nextToken() {
			if (parse == null || currentPos >= parse.size() - 1)
				return null;
			else {
				currentPos++;
				Term term = parse.get(currentPos);
				return term.word + "/" + term.nature;
			}
		}
        
		public void recognizeSpecialSequence(String text) {
			
		}
		
		
		
	}
	
}
