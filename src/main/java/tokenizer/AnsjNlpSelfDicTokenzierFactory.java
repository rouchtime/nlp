package tokenizer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.library.DicLibrary;
import org.ansj.splitWord.analysis.NlpAnalysis;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.rouchtime.util.LoadConf;

public class AnsjNlpSelfDicTokenzierFactory implements Serializable, TokenizerFactory {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6950587625587557105L;
	private DicLibrary dicLibrary;
	LoadConf loadConf;

	@SuppressWarnings("static-access")
	private AnsjNlpSelfDicTokenzierFactory() {
		try {
			loadConf = new LoadConf();
			dicLibrary = new DicLibrary();
			dicLibrary.put("userdefine", loadConf.getProperty("dic") + "dictionary_20170418_ansj.dic");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static volatile AnsjNlpSelfDicTokenzierFactory instance;

	@Override
	public Tokenizer tokenizer(char[] ch, int start, int length) {
		return new AnsjNlpTokenizer(ch, start, length);
	}

	public static AnsjNlpSelfDicTokenzierFactory getIstance() {
		if (instance == null) {
			synchronized (AnsjNlpSelfDicTokenzierFactory.class) {
				if (instance == null) {
					instance = new AnsjNlpSelfDicTokenzierFactory();
				}
			}
		}
		return instance;
	}

	public class AnsjNlpTokenizer extends Tokenizer {

		private List<String> parse = new ArrayList<String>();
		private int currentPos = -1;

		@SuppressWarnings("static-access")
		public AnsjNlpTokenizer(char[] ch, int start, int length) {
			String text = String.valueOf(ch);
			Result result = null;
			result = NlpAnalysis.parse(text, dicLibrary.get("userdefine"));

			for (Term term : result.getTerms()) {
				if (term.getName().length() <= 1) {
					continue;
				}
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
