package tokenizer;
import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.library.DicLibrary;
import org.ansj.splitWord.analysis.ToAnalysis;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.rouchtime.nlp.keyword.KeywordTest;

public class AnsjTokenizerFactory implements Serializable, TokenizerFactory {
	private static final long serialVersionUID = 572943028477125945L;
	private String modelPath;
	private DicLibrary dicLibrary;
	@SuppressWarnings("static-access")
	private AnsjTokenizerFactory() {

	}
	
	private void initSelfDic() {
//		String pPath = AnsjTokenizerFactory.class.getProtectionDomain().getCodeSource().getLocation().getPath();
//		String path = this.getClass().getClassLoader().getResource("library/dictionary_20170418_ansj.dic").getPath();
        ClassLoader classLoader = AnsjTokenizerFactory.class.getClassLoader();  
        URL resource = classLoader.getResource("library/dictionary_20170418_ansj.dic");  
        String path = resource.getPath();  
//		modelPath = new File(pPath).getParent() + "library/dictionary_20170418_ansj.dic";
		dicLibrary = new DicLibrary();
		dicLibrary.put("userdefine", path);
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
					instance.initSelfDic();
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
			Result result = ToAnalysis.parse(text,dicLibrary.get("userdefine"));
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
