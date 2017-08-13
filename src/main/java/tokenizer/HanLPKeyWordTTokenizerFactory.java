package tokenizer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.NLPTokenizer;

import tokenizer.HanLPTokenizerFactory.HanLPTokenizer;

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
		String content = "最近两个月，全球市场两大主要农作物小麦、大米价格纷纷上涨。 周三，明尼阿波利斯谷物交易所(MGEX)7月硬红春麦合约盘中触及7.13美元，近三年来首次突破7美元/蒲式耳大关。市场担心恶劣天气可能制约高蛋白春小麦产量。 芝加哥商品交易所（CBOT）小麦期货周三上涨0.63%，现价已逼近500美分/蒲式耳关口；4月末至今，CBOT小麦期货累计上涨近15%。 目前，美国小麦的主产区——大平原地区正遭遇严重干旱。美国干旱监测中心周四发布的数据显示，北达科他州、南达科他州这两个州90％以上的区域是干旱的，几十个城镇旱情“严重”或“极端”。本周早些时候，美国农业部数据显示，这两个州超过一半的春小麦被评级为“一般”或“很差”。 虽然当前小麦存货仍然充足，联合国预计今年小麦存货会超出需求三分之一，但面粉加工商需要的是硬红春麦这种优质小麦，CHS Hedging商品经纪人Joe Lardy向英国金融时报表示，“硬红春麦很难替代”。这导致MGEX硬红春麦价格大涨。 涨价的不只是小麦，最近两个月来，国际大米价格也大涨近30%。 据华尔街日报报道，全球大米现货平均价格已上涨至去年8月以来的最高水平。期货价格同时在走高，芝加哥期货交易所(CBOT)糙米期货价格4月底以来上涨近30%。 华尔街见闻此前文章提到，近期，泰国的大米库存减少一定程度上影响了短期供需变化，进而推高了价格。 2012年以来，全球第二大大米出口国泰国的高库存导致大米价格疲软。为增加农民收入、刺激消费，泰国政府曾花费三年时间高价向农民收购大米。但大米补贴计划给泰国政府造成巨大损失，于2014年被叫停。泰国政府在此计划执行过程中储存了大量大米，后来开始出售。经过多次出售，泰国政府国储大米库存从2014年的1750万吨下降至目前的约800万吨。 国际库存也随之下降。据国际水稻研究所首席科学家Samarendu Mohanty称，全球前五大大米出口国—印度、巴基斯坦、泰国、越南和美国—的大米库存总量为2900万吨，为2010年以来的最低水平。2012年时，前五大出口国的库存总量曾达到4100万吨。 大米库存减少的同时，需求却在增加。孟加拉国和斯里兰卡受恶劣天气影响，进口需求相当旺盛，而中东地区对美国大米的需求也在增加。";
		for(String word : HanLPKeyWordTTokenizerFactory.getIstance(5).tokenizer(content.toCharArray(), 0, content.length())) {
			System.out.println(word);
		}
	}

}
