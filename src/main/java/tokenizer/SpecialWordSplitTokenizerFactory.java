package tokenizer;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

public class SpecialWordSplitTokenizerFactory implements Serializable, TokenizerFactory {
	private static final long serialVersionUID = 4727081129734961550L;
	private final String specialWord;
	private static volatile SpecialWordSplitTokenizerFactory instance;
	
	private SpecialWordSplitTokenizerFactory(String specialWord) {
		this.specialWord = specialWord;
	}

	private SpecialWordSplitTokenizerFactory() {
		this.specialWord = "\\s+";
	}
	
	@Override
	public Tokenizer tokenizer(char[] ch, int start, int length) {
		return new SpecialWordSplitTokenizer(ch, start, length);
	}

	public static SpecialWordSplitTokenizerFactory getIstance(String specialWord) {
		if (instance == null) {
			synchronized (SpecialWordSplitTokenizerFactory.class) {
				if (instance == null) {
					instance = new SpecialWordSplitTokenizerFactory(specialWord);
				}
			}
		}
		return instance;
	}
	
	public static SpecialWordSplitTokenizerFactory getIstance() {
		if (instance == null) {
			synchronized (SpecialWordSplitTokenizerFactory.class) {
				if (instance == null) {
					instance = new SpecialWordSplitTokenizerFactory("\\s+");
				}
			}
		}
		return instance;
	}
	
	class SpecialWordSplitTokenizer extends Tokenizer {
		private String text;
		private List<String> parse;
		private int currentPos = -1;

		public SpecialWordSplitTokenizer(char[] ch, int start, int length) {
			text = String.valueOf(ch).substring(start, start + length);
			String[] words = text.split(specialWord);
			parse = Arrays.asList(words);

		}

		@Override
		public String nextToken() {
			if (parse == null || currentPos >= parse.size() - 1)
				return null;
			else {
				currentPos++;
				String word = parse.get(currentPos);
				return word + "/" + "null";
			}
		}
	}
	
	public static void main(String[] args) {
		String a = "你好@@大师@@你好@@大师@@大师@@你好";
		for(String word : SpecialWordSplitTokenizerFactory.getIstance("@@").tokenizer(a.toCharArray(), 0, a.length())) {
			System.out.println(word);
		}
	}
}
