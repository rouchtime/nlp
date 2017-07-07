package tokenizer;

import java.util.ArrayList;
import java.util.List;

import org.fnlp.nlp.cn.CNFactory;
import org.fnlp.util.exception.LoadModelException;

import com.aliasi.tokenizer.Tokenizer;
import com.hankcs.hanlp.seg.common.Term;

public class FudanNLPTokenizer extends Tokenizer{
	
	private List<String> parse = new ArrayList<String>();
	private int currentPos = -1;
	public FudanNLPTokenizer(char[] ch, int start, int length) {
		try {
			String article = String.valueOf(ch);
			CNFactory factory = CNFactory.getInstance("models");
			String[][] tags = factory.tag(article);
			for (int i = 0; i < tags[0].length; i++) {
				String word = tags[0][i];
				String nature = tags[1][i];
				parse.add(word + "/"+ nature);
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
