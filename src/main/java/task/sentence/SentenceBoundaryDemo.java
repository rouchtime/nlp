package task.sentence;

import com.aliasi.sentences.HeuristicSentenceModel;
import com.aliasi.tokenizer.CharacterTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.hankcs.hanlp.HanLP;

import tokenizer.HanLPTokenizerFactory;

import com.aliasi.tokenizer.Tokenizer;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

/** Use SentenceModel to find sentence boundaries in text */
public class SentenceBoundaryDemo {

	static final TokenizerFactory TOKENIZER_FACTORY = HanLPTokenizerFactory.getIstance();
	static final ChineseSentenceModel SENTENCE_MODEL = SummarizationSentenceModel.INSTANCE;

	public static void main(String[] args) throws IOException {
		String text = "互联网对传统新闻造成的冲击终于让美国报业坐不住了。因此，我不停止句子。 7月10日，美国新闻媒体联盟（News Media Alliance）向美国国会申诉，要求美国国会修改《反垄断法》，以更好地应对谷歌和脸书两家互联网巨头对传统媒体造成的冲击。  美国新闻媒体联盟发端于美国报业联盟，其中包含《华盛顿邮报》、《华尔街日报》、《纽约时报》等知名美国传统媒体和众多规模不大的媒体，总数近2000家。 美国新闻媒体联盟10日发布的文章截图维权不易：报业如想维权需先改法律 在美国新闻媒体联盟网站上公布的这一号召称，消费者对即时、可靠消息的需求日益增长，但是目前，互联网的分配系统却将优秀新闻报道的经济价值分配进行扭曲。由于在数字时代谷歌和脸书的双垄断局面，新闻媒体被迫在内容上做出让步，并按照数字媒体的规则运行。但是这些规则一方面将新闻商品化，另一方面也增加了假新闻出现的风险，而在目前的体系中，区分真假新闻是有一定困难的。  据美国CNBC电视台报道，根据皮尤研究中心的数据，谷歌和脸书现在基本上占据了美国总额达730亿美元的互联网广告中的70%。然而美国报业去年的广告收入仅有180亿美元，但是10年前，报业广告有500亿美元之多。 由于广告收入大幅下降，《纽约时报》不得不将其总部大楼部分房间出租以赚取租金（社交媒体截图） 不过，想要维权也不并不容易。美国《反垄断法》的初衷是降低垄断性企业对社会的伤害。但是，美国新闻媒体联盟称，在媒体行业上，现存法律却无意地阻止了新闻媒体联合起来在谈判中获得有利地位，以让对民主制度至关重要的新闻媒体能够可持续地为人们服务。  在面对一个几乎将传统媒体逼入绝路、占尽广告收入资源的双垄断互联网媒体环境时，新闻媒体在与互联网媒体谈判时没有任何主动权。 为了民主，支持传统新闻业 “立法机构如能允许新闻媒体集体进行谈判，将会对今天的媒体行业的健康及高质量发展提供可能。”新闻媒体联盟的主席大卫·查文（David Chavern）说，“高质量的新闻业是保障民主的重要部分，也是公民社会的核心。为了让这样的新闻业能够有未来，新闻媒体发现必须集体同互联网媒体平台进行谈判。”  美国新闻媒体联盟还称，除了在媒体行业的主导地位，脸书和谷歌无法在其信源和能力上保证新闻报道的真实性。脸书在去年的美国大选中就因为其没有对新闻内容的真实性进行审查而遭到公众质疑。  “脸书和谷歌并不雇佣记者，他们不会通过公开信息去发掘腐败丑闻，也不会派驻战地记者，甚至不会派人去体育比赛现场带来最新报道。但是，他们却榨取了整个新闻行业的经济效益，而所有花钱的事情却都是我们来做的。”查文说，“唯一的维权方式就是大家拧成一股绳。”  “如果我们最终能和脸书、谷歌谈判后达成更有利的知识产权保护协议以及更公平的收入分配体系，新闻业才会可持续发展。” 互联网给传统报业带来巨大冲击，2016年，英国《独立报》推出最后一期后，停止纸版报纸发行，全面改为互联网媒体（社交媒体截图） 本文系观察者网独家稿件，文章内容纯属作者个人观点，不代表平台观点，未经授权，不得转载，否则将追究法律责任。关注观察者网微信guanchacn，每日阅读趣味文章。";
		List<String> tokenList = new ArrayList<String>();
		Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(text.toCharArray(), 0, text.length());
		String[] tokens = tokenizer.tokenize();
		int index=0;
		for(String token : tokens) {
			tokens[index] = token.split("/")[0];
			index++;
		}
		int[] sentenceBoundaries = SENTENCE_MODEL.boundaryIndices(tokens);
		if (sentenceBoundaries.length < 1) {
			System.out.println("No sentence boundaries found.");
			return;
		}
		int sentStartTok = 0;
		int sentEndTok = 0;
		for (int i = 0; i < sentenceBoundaries.length; ++i) {
			sentEndTok = sentenceBoundaries[i];
			System.out.println("SENTENCE " + (i + 1) + ": ");
			for (int j = sentStartTok; j <= sentEndTok; j++) {
				System.out.print(tokens[j]);
			}
			System.out.println();
			sentStartTok = sentEndTok + 1;
		}

	}
}
