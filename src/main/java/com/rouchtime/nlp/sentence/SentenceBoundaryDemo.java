package com.rouchtime.nlp.sentence;

import com.aliasi.sentences.HeuristicSentenceModel;
import com.aliasi.tokenizer.CharacterTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.hankcs.hanlp.HanLP;

import tokenizer.HanLPTokenizerFactory;

import com.aliasi.tokenizer.Tokenizer;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class SentenceBoundaryDemo {

	static final TokenizerFactory TOKENIZER_FACTORY = HanLPTokenizerFactory.getIstance();
	static final ChineseSentenceModel SENTENCE_MODEL = SummarizationSentenceModel.INSTANCE;

	public static void main(String[] args) throws IOException {
		String text = "上世纪七八十年代，哥本哈根港口曾被严重污染。但经过努力，现在海水已得到净化，夏天人们可在港口游泳。（记者 姚冬琴 摄）　　丹麦，是“童话王国”，也是绿色奇迹的缔造者。从上世纪80年代至今，丹麦经济累计增长超过70%，能源消耗却几乎没有增长，二氧化碳排放量反而降低了13%……　　丹麦在发展低碳经济和绿色产业方面所取得的成就引发世界关注，也是其扩大出口的制胜法宝。然而，丹麦也曾遭遇过环境危机。是什么样的契机促使丹麦走上了绿色发展之路？丹麦政府如何通过监管促使企业和居民注重环保？丹麦企业又如何在绿色发展中获取商业价值？　　近日，《中国经济周刊》记者就绿色经济话题赴丹麦调研采访。　　“绿色生意是好的生意”　　“绿色生意是好的生意。”在丹麦政府推广绿色经济的官方机构——绿色国度总部，该机构宣传经理Anne Vestergaard Andersen对记者说。　　Anne表示，2015 年，丹麦绿色能源技术出口额55 亿欧元（约合409亿丹麦克朗），占丹麦总出口额的6.4%。自2000年以来，丹麦绿色能源技术出口额增长了300%。在这当中，丹麦在水处理等方面的绿色技术占据全球领先地位。　　自动化、智慧化，是丹麦水处理工厂的特征。当记者来到丹麦第二大城市奥胡斯的市立水务水厂时，该水厂运营主管Rasmus Brentzen为了接待中国记者特意赶来。平常，这里一两周都不会出现一个工作人员，所有的设备、监测系统自动运行，工作人员可以在家里的电脑上或者手机上看到实时数据，并进行相关操作。　　Rasmus告诉记者，只需经过曝气和砂滤两道工序，就能保证奥胡斯市民喝上干净、安全的自来水，而绝不使用氯气或其他药物消毒。　　在能源领域，丹麦早在2011年便已实现了欧盟2020年可再生能源发展目标（即到2020年可再生能源占能源结构比例达到20%），并计划2020年将可再生能源的比例提高到35%。　　事实上，1973年石油危机爆发前，丹麦能源对外依存度曾高达99%，经过多年削减化石能源、大力发展清洁能源，到2015年，丹麦的能源出口额已占到丹麦总出口额的11.1%。　　在丹麦女王宫殿的海港对岸、直线距离不过2000米的地方，一座现代化的垃圾焚烧厂正在建设。这座名为Amager Ressource Center的工厂预计今年内将建成，届时将是欧洲最节能高效的垃圾焚烧厂之一，年处理垃圾量将达40万吨，可提供16万户居民的直接供热和6.25万户居民的供电。　　令人印象深刻的是，在工厂斜坡状的屋顶，还设计了一条对公众开放的滑雪道。&nbsp;　　　　丹麦的绿色蜕变　　上世纪七八十年代，哥本哈根港口曾被严重污染。彼时，丹麦的经济增长率很高，曾经达到16%～17%，但环境污染却使幸福感下降。如今在回忆那段历程时，丹麦人莫不认为，绿色发展与政府的严格控制密不可分。　　丹麦四面环水，但淡水对丹麦来说仍然是一种稀缺资源，对水的重视和保护从水价上可见一斑。从1989年到2017年，丹麦居民支付的水费从每立方2欧元涨到了7欧元（约56元人民币）。高水价倒逼了高效供水和节约用水。丹麦人均用水量从1989年的每天170升下降到现在的每天114升。　　对于供水公司而言，“水漏损率”（供水途中损失的水量）则是一道硬指标。与全世界20%～25%的平均水平相比，丹麦水漏损率已降至7%～8%。那些水漏损率超过10%的水务公司将被严惩。　　丹麦环境及食品部环保署副主任Mikkel Hall告诉记者，每年都有水务公司因此承受沉重的罚单——政府会下调水务公司收取居民水费的价格。“这会让他们更紧张。如果只是简单开个罚单，太便宜他们了。”　　对于水质的监测，也是Mikkel所在部门的重要工作之一。不过令人惊讶的是，整个丹麦只有约25名工作人员负责这项工作。Mikkel告诉记者，所有工厂和水处理厂都必须聘用第三方实验室来监测水质。而环保署直接从这些实验室收取数据，实时监测，一旦发现监测结果不达标，排放单位将被吊销执照。　　对于绿色发展，政府也不总是严格监管的冷面孔。Mikkel说，丹麦政府鼓励企业向国外输出绿色技术。尽管坚持“商业行为归商业行为”，政府对此没有财政方面的支持，但却会从政府层面向别的国家推介绿色技术。　　丹麦政府有良好的PPP机制，支持企业技术研发。哥本哈根最大的医院——赫勒福医院的污水处理项目就是一个典型的例子。该污水处理项目采用世界循环泵巨头格兰富集团开发创新的一体化生物膜污水处理技术，也是全世界首家试点全面处理医院污水的污水处理厂。含有药物、病原体和抗生素等有害物质的医院污水，经处理后已达到直接饮用标准。看到记者有些疑虑，陪同参观的格兰富集团公共事务部高级经理Morten Riis及该污水处理项目的产品经理，随即喝下一杯经处理后的水。哥本哈根赫勒福医院，污水经由格兰富集团一体化生物膜污水处理技术处理后，达到直接饮用标准。（记者 姚冬琴 摄）格兰富集团公共事务部高级经理Morten Riis及该污水处理项目的产品经理，在饮用经处理后的水。（记者 姚冬琴 摄）中丹环境合作“一拍即合”　　目前，丹麦正在大力发展环保产业，并把增加环境产品出口列入经济增长计划。而像中国这样的发展中国家，则需要符合国情、有较低成本的先进环保技术。过去几年，中丹两国能源、环保等领域的官员频繁会面，合作也陆续展开。　　数据显示，中国已成为丹麦清洁空气技术与方案的第三大市场，2015年出口额约为13.7亿丹麦克朗。同一年，丹麦向中国出口的水技术金额约为8.78亿丹麦克朗，而这个数字在2006年为2.98亿丹麦克朗，10年间涨了近2倍。　　丹麦企业无疑是推动绿色技术创新和出口的生力军。格兰富集团即是其中的代表。格兰富集团副总裁Kim Nhr Skibsted对《中国经济周刊》记者表示，中国市场是最有潜力的，他们把中国作为第二本土市场。　　“中国在可持续发展方面表现得非常积极，并且进展很快。今后，我们将加大技术创新、数字化解决方案和服务等方面的投入，以满足高速增长的中国市场需求。”Kim说。　　格兰富中国副总裁张小岩告诉记者，近年来，格兰富不断加强与中国各级政府的合作。今年5月，中国环保部环境保护对外合作中心、广东省肇庆市人民政府和丹麦驻华大使馆签署了关于开展环境保护合作及共同支持建设中丹环保产业园（肇庆）的谅解备忘录。格兰富将为肇庆市中丹环保产业园及两家新建医院提供最新的水处理解决方案。　　记者 姚冬琴 | 丹麦哥本哈根、奥胡斯报道";
//		System.out.println(HanLP.segment(text));
		Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(text.toCharArray(), 0, text.length());
		String[] tokens = tokenizer.tokenize();
		int[] sentenceBoundaries = SENTENCE_MODEL.boundaryIndices(tokens);
		if (sentenceBoundaries.length < 1) {
			System.out.println("未发现句子边界！");
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
