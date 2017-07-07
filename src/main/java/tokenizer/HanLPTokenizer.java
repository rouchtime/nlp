package tokenizer;

import java.util.ArrayList;
import java.util.List;

import com.aliasi.tokenizer.Tokenizer;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

public class HanLPTokenizer extends Tokenizer {

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
