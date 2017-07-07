package tokenizer;

import java.io.Serializable;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

public class FudanNLPTokenzierFactory implements Serializable,TokenizerFactory{

	private static final long serialVersionUID = -173637242987395937L;

	private FudanNLPTokenzierFactory() {
		
	}
	 private static volatile FudanNLPTokenzierFactory instance;
	
	@Override
	public Tokenizer tokenizer(char[] ch, int start, int length) {
		return new FudanNLPTokenizer(ch, start, length);
	}

	public Tokenizer ner(char[] ch, int start, int length) {
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
}
