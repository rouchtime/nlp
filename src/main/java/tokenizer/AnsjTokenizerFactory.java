package tokenizer;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

public class AnsjTokenizerFactory implements Serializable, TokenizerFactory {
	private static final long serialVersionUID = 572943028477125945L;

	private AnsjTokenizerFactory() {
		
	}

	private static volatile AnsjTokenizerFactory instance;

	@Override
	public Tokenizer tokenizer(char[] ch, int start, int length) {

		return new AnsjTokenizer(ch, start, length);
	}

	public static AnsjTokenizerFactory getIstance() {
		if (instance == null) {
			synchronized (AnsjTokenizerFactory.class) {
				if (instance == null) {
					instance = new AnsjTokenizerFactory();
				}
			}
		}
		return instance;
	}

	class AnsjTokenizer extends Tokenizer {

		private List<String> parse = new ArrayList<String>();
		private int currentPos = -1;
		public AnsjTokenizer(char[] ch, int start, int length) {
			String text = String.valueOf(ch);
			Result result = ToAnalysis.parse(text);
			for (Term term : result.getTerms()) {
				parse.add(term.getName() + "/" + term.getNatureStr());
			}
		}

		@Override
		public String nextToken() {
			if (parse == null || currentPos >= parse.size() - 1)
				return null;
			else {
				currentPos++;
				return parse.get(currentPos);
			}
		}

	}

}
