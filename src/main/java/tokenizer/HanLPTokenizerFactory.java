package tokenizer;

import java.io.Serializable;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

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
}
