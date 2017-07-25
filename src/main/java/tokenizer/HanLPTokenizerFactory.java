package tokenizer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

public class HanLPTokenizerFactory implements Serializable, TokenizerFactory {

	private static final long serialVersionUID = 2293162031266034831L;

	private HanLPTokenizerFactory() {

	}

	private static volatile HanLPTokenizerFactory instance;

	@Override
	public Tokenizer tokenizer(char[] ch, int start, int length) {
		return new HanLPTokenizer(ch, start, length);
	}

	public static HanLPTokenizerFactory getIstance() {
		if (instance == null) {
			synchronized (HanLPTokenizerFactory.class) {
				if (instance == null) {
					instance = new HanLPTokenizerFactory();
				}
			}
		}
		return instance;
	}

	class HanLPTokenizer extends Tokenizer {

		private List<Term> parse = new ArrayList<Term>();
		private int currentPos = -1;

		public HanLPTokenizer(char[] ch, int start, int length) {
			String text = String.valueOf(ch);
			parse = HanLP.segment(text);

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

	}
}
