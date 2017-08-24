package tokenizer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.NLPTokenizer;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;

import tokenizer.HanLPTokenizerFactory.HanLPTokenizer;
	
public class JiebaTokenizerFactory implements Serializable, TokenizerFactory{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1492728104694780351L;
	private static volatile JiebaTokenizerFactory instance;
	private JiebaTokenizerFactory() {

	}

	@Override
	public Tokenizer tokenizer(char[] ch, int start, int length) {
		return new JiebaTokenizer(ch, start, length);
	
	}
	
	public static JiebaTokenizerFactory getIstance() {
		if (instance == null) {
			synchronized (JiebaTokenizerFactory.class) {
				if (instance == null) {
					instance = new JiebaTokenizerFactory();
				}
			}
		}
		return instance;
	}
	
	class JiebaTokenizer extends Tokenizer {

		private List<SegToken> parse = new ArrayList<SegToken>();
		private int currentPos = -1;

		public JiebaTokenizer(char[] ch, int start, int length) {
			String text = String.valueOf(ch);
			JiebaSegmenter Segmenter = new JiebaSegmenter();
			parse = Segmenter.process(text, SegMode.SEARCH);

		}

		@Override
		public String nextToken() {
			if (parse == null || currentPos >= parse.size() - 1)
				return null;
			else {
				currentPos++;
				SegToken term = parse.get(currentPos);
				if(term.word.getTokenType().equals("")) {
					return term.word.getToken() + "/jieba"; 
				}
				return term.word.getToken() + "/" + term.word.getTokenType(); 
			}
		}

	}
	

	public static void main(String[] args) {
		StopWordTokenierFactory stopWordFactory = new StopWordTokenierFactory(JiebaTokenizerFactory.getIstance());
		StopNatureTokenizerFactory stopNatureTokenizerFactory = new StopNatureTokenizerFactory(stopWordFactory);
		String text = "最近两个月，全球市场两大主要农作物小麦、大米价格纷纷上涨。北京";
		for(String word : stopNatureTokenizerFactory.tokenizer(text.toCharArray(), 0, text.length())) {
			if(word.split("/").length < 2) {
				continue;
			}
			System.out.println(word);
		}
	}
}
