package tokenizer;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.fnlp.nlp.cn.CNFactory;
import org.fnlp.util.exception.LoadModelException;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

public class FudanNLPTokenzierFactory implements Serializable, TokenizerFactory {
	private String modelPath;
	private static final long serialVersionUID = -173637242987395937L;

	private FudanNLPTokenzierFactory() {
//		String pPath = FudanNLPTokenizer.class.getProtectionDomain().getCodeSource().getLocation().getPath();
//		modelPath = new File(pPath).getParent() + "/models";
		modelPath = getClass().getClassLoader().getResource("models").getPath();
		
	}

	private static volatile FudanNLPTokenzierFactory instance;

	@Override
	public Tokenizer tokenizer(char[] ch, int start, int length) {
		return new FudanNLPTokenizer(ch, start, length);
	}

	public static FudanNLPTokenzierFactory getIstance() {
		if (instance == null) {
			synchronized (FudanNLPTokenzierFactory.class) {
				if (instance == null) {
					instance = new FudanNLPTokenzierFactory();
				}
			}
		}
		return instance;
	}

	class FudanNLPTokenizer extends Tokenizer {

		private List<String> parse = new ArrayList<String>();
		private int currentPos = -1;

		public FudanNLPTokenizer(char[] ch, int start, int length) {
			try {
				String article = String.valueOf(ch);
				CNFactory factory = CNFactory.getInstance(modelPath);
				String[][] tags = factory.tag(article);
				for (int i = 0; i < tags[0].length; i++) {
					String word = tags[0][i];
					String nature = tags[1][i];
					parse.add(word + "/" + nature);
				}
			} catch (LoadModelException e) {
				System.out.println("引入FudanNlp模型报错");
				e.printStackTrace();
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
