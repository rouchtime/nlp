package tokenizer;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.library.DicLibrary;
import org.ansj.splitWord.analysis.NlpAnalysis;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

public class AnsjNlpTokenizerFactory implements Serializable, TokenizerFactory {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5850115535694987216L;
	private String modelPath;
	private DicLibrary dicLibrary;

	private AnsjNlpTokenizerFactory() {
//		String pPath = AnsjNlpTokenizerFactory.class.getProtectionDomain().getCodeSource().getLocation().getPath();
//		modelPath = new File(pPath).getParent() + "/library/dictionary_20170418_ansj.dic";
//		modelPath = getClass().getClassLoader().getResource("library//dictionary_20170418_ansj.dic").getPath();
//		dicLibrary = new DicLibrary();
//		dicLibrary.put("userdefine", modelPath);
	}

	private static volatile AnsjNlpTokenizerFactory instance;

	@Override
	public Tokenizer tokenizer(char[] ch, int start, int length) {
		return new AnsjNlpTokenizer(ch, start, length);
	}

	public static AnsjNlpTokenizerFactory getIstance() {
		if (instance == null) {
			synchronized (AnsjTokenizerFactory.class) {
				if (instance == null) {
					instance = new AnsjNlpTokenizerFactory();
				}
			}
		}
		return instance;
	}

	public class AnsjNlpTokenizer extends Tokenizer {

		private List<String> parse = new ArrayList<String>();
		private int currentPos = -1;

//		@SuppressWarnings("static-access")
		public AnsjNlpTokenizer(char[] ch, int start, int length) {
			String text = String.valueOf(ch);
//			Result result = NlpAnalysis.parse(text, dicLibrary.get("userdefine"));
			Result result = NlpAnalysis.parse(text);
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
