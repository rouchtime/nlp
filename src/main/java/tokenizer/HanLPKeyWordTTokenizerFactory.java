package tokenizer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.summary.TextRankKeyword;
import com.rouchtime.util.RegexUtils;

public class HanLPKeyWordTTokenizerFactory  implements Serializable, TokenizerFactory {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1481271450871860259L;
	private int m_num;
	private HanLPKeyWordTTokenizerFactory() {

	}

	private HanLPKeyWordTTokenizerFactory(int num) {
		this.m_num = num;
	}
	
	private static volatile HanLPKeyWordTTokenizerFactory instance;

	@Override
	public Tokenizer tokenizer(char[] ch, int start, int length) {
		return new HanLPKeyWordTTokenizer(ch, start, length);
	}

	public static HanLPKeyWordTTokenizerFactory getIstance(int num) {
		if (instance == null) {
			synchronized (HanLPKeyWordTTokenizerFactory.class) {
				if (instance == null) {
					instance = new HanLPKeyWordTTokenizerFactory(num);
				}
			}
		}
		return instance;
	}

	class HanLPKeyWordTTokenizer extends Tokenizer {

		private List<String> parse = new ArrayList<String>();
		private int currentPos = -1;

		public HanLPKeyWordTTokenizer(char[] ch, int start, int length) {
			String text = String.valueOf(ch);
			parse = HanLP.extractKeyword(text, m_num);

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
	public static void main(String[] args) {
		String content = "!@#!@$#imgidx=0001#$!@#!@许维恩、KID。!@#!@台湾正当红艺人KID今晚如愿走上红毯，还牵着超正女友许维恩，整个人春风满面。不过，他事前就担心女伴许维恩太抢锋头，许维恩服装大展心机，下身开高衩，胸前透视，让人眼睛不知道该看哪。!@#!@KID风光入围同时入围益智及实境节目主持人奖、综艺节目主持人奖，他大破音狂呼：「我终于走上红毯了！我等了3年了！终于感受红毯是什麽感觉！」!@#!@更风光的是，交往稳定的女友许维恩「牵手手」爱相随，KID预告今天真的有准备桥段，随后拿出一个精美小盒子，疑似要求婚，让许维恩大受惊！!@#!@KID不疾不徐打开盒子，原来只是蜜粉，他手一边帮许维恩补妆、一边说：「没有啦！没有要结婚！」还偷酸许维恩结婚「有经验」，成为红毯最爆笑桥段。";
		content = RegexUtils.cleanParaAndImgLabel(content);
		TextRankKeyword tk = new TextRankKeyword();
		tk.setSegment(HanLP.newSegment().enableAllNamedEntityRecognize(true));
		System.out.println(tk.getTermAndRank(content, 20));
	}

}
