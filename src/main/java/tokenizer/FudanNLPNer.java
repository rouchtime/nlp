package tokenizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.fnlp.nlp.cn.CNFactory;
import org.fnlp.util.exception.LoadModelException;

import com.aliasi.tokenizer.Tokenizer;

public class FudanNLPNer extends Tokenizer{

	
	private List<String> parse = new ArrayList<String>();
	private int currentPos = -1;
	public FudanNLPNer(char[] ch, int start, int length) {
		try {
			String article = String.valueOf(ch);
			CNFactory factory = CNFactory.getInstance("models");
			HashMap<String, String> tags = factory.ner(article);
			for (String key : tags.keySet()) {
				String word = key;
				String recogize = tags.get(key);
				parse.add(word + "/"+ recogize);
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
