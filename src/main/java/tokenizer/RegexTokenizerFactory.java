package tokenizer;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aliasi.tokenizer.ModifyTokenTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

public class RegexTokenizerFactory extends ModifyTokenTokenizerFactory implements Serializable{

	public RegexTokenizerFactory(TokenizerFactory factory) {
		super(factory);
		// TODO Auto-generated constructor stub
	}
}
