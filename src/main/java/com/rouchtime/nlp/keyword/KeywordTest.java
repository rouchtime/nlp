package com.rouchtime.nlp.keyword;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.ScoredObject;
import com.hankcs.hanlp.HanLP;
import com.rouchtime.util.RegexUtils;

import tokenizer.AnsjNlpTokenizerFactory;
import tokenizer.AnsjTokenizerFactory;
import tokenizer.JiebaTokenizerFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

public class KeywordTest {

	static TokenizerFactory tokenFactory = getTokenFactory();

	private static TokenizerFactory getTokenFactory() {
		StopWordTokenierFactory stopWordFactory = new StopWordTokenierFactory(AnsjNlpTokenizerFactory.getIstance());
		StopNatureTokenizerFactory stopNatureTokenizerFactory = new StopNatureTokenizerFactory(stopWordFactory);
		return stopNatureTokenizerFactory;
	}

	public static void main(String[] args) {
		// CiLinWordSimiarity cilin = CiLinWordSimiarity.getInstance();
		// System.out.println(cilin.calcWordsSimilarity("家伙", "东西"));
		// TfIdf tfidf = TfIdf.getInstance();
		// LexicalChain chain = new
		// LexicalChain(0.6,Word2VectorWordSimiarity.getInstance());
		String text = "!@#!@81式自动步枪因81年设计定型生产而得名，它是中国人民解放军装备的一种自动步枪，对于上世纪八九十年代曾在军中服役过的老兵来说，“八一杠”承载了一代人的荣誉，更是人民解放军的象征。81式在1981年开始在中国人民解放军中服役，它是在当时解放军应对对越自卫反击战的需求的历史条件下而产生的，最初的定位是“应急”或“临时性”的产品，作为将来装备小口径突击步枪前的过渡性枪型装备部队的。曾在两山战役及斯里兰卡内战使用，但直到80年代末才广泛装备，逐渐取代56式。斯里兰卡曾在90年代开始尝试装配81式，孟加拉国同样地合法授权生产81式并命名为BD-08自动步枪。此后，我们的“八一杠”身影逐渐遍布全球，饱受多国的称赞。不过，解放军目前已完成81式的换装工作，81式已被新款的95式和03式所取代。!@#!@$#imgidx=0001#$!@#!@虽是过渡枪型 但是81式枪族全部采用相对成熟的短行程活塞式导气系统技术!@#!@作为当时要求在短时间内完成设计的过渡枪型，解决时间短，尺寸问题，羊尾早些令她尖叫，加威三个e一个a，后面带上数字120,不要错了它的其它结构基本与56式冲锋枪类似，但81式枪族全部采用相对成熟的短行程活塞式导气系统技术。56式冲锋枪/自动步枪使用活塞长行程，虽然动作可靠，但是自动射击枪机运动暴躁，难以控制精度，活塞短行程目前已经作为自动步枪最合适的自动方式而被广泛认可。81式步枪全长为950mm，枪管长440mm，介于56式半自动步枪和56式冲锋枪之间。也实现了单发精度接近56半，全自动射击拥有56冲的强大火力的特点，据装备了81式枪族的部队反映，该枪射击精度好，并且“八一杠”曾在一百多米的距离上，用两支81式自动步枪压制敌方碉堡的枪眼，使其无法开火。据说此前63式自动步枪在设计时就有此思路，但是因为文革等种种原因未能实现。!@#!@$#imgidx=0002#$!@#!@“八一杠”导气管可以调节 最大的保证了精度!@#!@除了空仓挂机、快慢机位置以外，“八一杠”的导气管可以调节，正常情况下采取小气孔（2.1）射击以保证精度，恶劣环境下则采用大气孔（4.2）射击保证可靠。自动机的运动、开闭锁也直接影响武器的射击精度，尤其对点射精度的影响更为明显。为提高81式枪族射击精度，围绕自动机设汁采取了一些综合措施。加强对自动机运动的导引，机框导轨长度连同复进到位后的辅助导轨。全长为110mm，相应的机头杆部与机框配合长度为70mm，使每一发枪弹的闭锁状态尽量一致。又靠拢了机匣导轨、复进簧、活塞与枪管中心线之间的距离。尽量减小枪管轴线与活塞轴线之间垂直距离,与枪管中心距离为18.5mm（56式冲锋枪为25mm），机匣导轨位于枪管中心上方6mm，与活塞、复进簧中心靠拢，使自动机运动平稳，运动件质心降低，减小动力偶作用。!@#!@$#imgidx=0003#$!@#!@81式枪族设计经过严格考验 实战中表现优异!@#!@81式包括采用固定木质枪托的称81式自动步枪、采用折叠金属枪托的称81-1式自动步枪。 81式自动步枪与81式7.62毫米轻机枪组成81式枪族。这3种武器的主要结构相同，自动机、复进机、击发机构、导气系统、供弹具都能在族内各枪互换使用，约有65种零部件可以互换通用。连同其他零部件通用率达到70％。该枪族的出现基本适应了一枪多用、枪族系列化、弹药通用化的发展趋势，极大地方便了训练、使用和维修，既加强了战斗分队的战斗力，也为枪械互换、增强火力提供了条件，在实战中表现非常好。而81式枪族设计时，各种严格条件的考验，经过部队装备作战的实践，故障极少。在研制阶段浸水试验就做了26次，早期曾经出现过早发火、发射枪榴弹时机匣盖脱落、表尺自动跳码等问题，但都经过改进得到解决，但防腐性能仍需改善。在大量生产中质量稳定，每次抽枪寿命试验，步枪在15,000发射弹过程中达到了无任何故障、无零部件裂纹、无任何功能失效的状况。!@#!@$#imgidx=0004#$!@#!@81式自动步枪基于56式但仅仅是形式构造区别很明显!@#!@官方资料称，81式自动步枪设计上主要是基于56式自动步枪（56式自动步枪为中国制造的AK-47）结合56式半自动步枪（中国版SKS半自动卡宾枪）的部分结构设计改进而成。但我们看到但很多明显的改良，也不失为是一种全新设计的步枪，虽然形似但区别还是很明显。首先最明显的部分是枪口，其次是准星的位向后移，用作安装榴弹发射器。另外弹匣与扳机间的空隙较56式长了很多，还有的是将解放军以前偏好使用的三棱刺刀，取代为较传统的刀状刺刀，这款刺刀还可作匕首和开瓶器使用。与56式相同，81式也是一个枪族。除了81式及81-1式突击步枪外，还有重枪管的81式班用机枪。 81-1式与81式相似，但改用可折叠枪托，其折叠机构源于56-2式，在解放军士兵中一般被俗称“81杠”，实际上解放军中普遍装备的就是81-1式而非81式。!@#!@$#imgidx=0005#$!@#!@81式自动步枪是7.62×39mm口径枪械中为数不多的原创枪型!@#!@虽然“八一杠”早已脱胎换骨，但是因为其造型的问题不仅被国外视为“AK中国改型”，在国内也被高层一些意见认为“造型苏式武器风格过浓”，因此在后续小口径型号87式自动步枪研制后，一度有“8910”工程搞87A自动步枪，其中一大要求就是在造型上脱离“苏式武器”的烙印。但有报道称，“枪王”卡拉什尼科夫在试射81式自动步枪后，赞扬81式为最好的AK47改型。值得一提的是，81式自动步枪是7.62×39mm口径枪械中为数不多的原创枪型，这一口径的多数枪型都是仿制或改进自AK-47或AKM步枪，真正原创的步枪只有三种，另外两种型号是捷克的Vz58和同样来自中华人民共和国的63式自动步枪。据解放军士兵及外国使用者的资料，81式的单发射击准确度较AK-47有所提高接近M16步枪；连发散布也较AK-47有明显提高，与多数小口径步枪相当。!@#!@$#imgidx=0006#$!@#!@小编有话说：小编算了算，“八一杠”加入解放军已经37年了，依然经得起考验堪称经典。兵器虽然也会老，但81式的生命远未终结，它承载着一代人的记忆，它的直系血统依然不断的衍生新的武器。";
		text = RegexUtils.cleanParaAndImgLabel(text);
		System.out.println(HanLP.extractKeyword(text, 100));
		ObjectToDoubleMap<String> tfMap = new ObjectToDoubleMap<>();
		for (String term : tokenFactory.tokenizer(text.toCharArray(), 0, text.length())) {
//			System.out.println(term);
			@SuppressWarnings("unused")
			String word = term.split("/")[0];
			tfMap.increment(word, 1.0);
		}
		SynonymMerge synMerge = new SynonymMerge();
		ObjectToDoubleMap<String> mergeTFMap = synMerge.mergeBySyn(tfMap);
		System.out.println(mergeTFMap.scoredObjectsOrderedByValueList());
		// int i = 0;
		// TextRank rank = new TextRank();
		// TextRankWithMultiWin rankMult = new TextRankWithMultiWin();
		// List<String> listToken = new ArrayList<String>();
		// for (String term : tokenFactory.tokenizer(text.toCharArray(), 0,
		// text.length())) {
		// @SuppressWarnings("unused")
		// String word = term.split("/")[0];
		// chain.addByMaxSim(word);
		// }
		// System.out.println(chain.getLexicalChain());
		// rankMult.setKeywordNumber(listToken.size());
		// System.out.println(rank.getKeyword(listToken,listToken.size()));
		// System.out.println(rankMult.integrateMultiWindow(listToken, 2, 10));
		// for(ScoredObject<Token> score : tfidf.getTfIdfSingleDoc(listToken)) {
		// System.out.println(String.format("%.5f,%s,%s",
		// score.score(),score.getObject().getWord(),score.getObject().getNature()));
		// }
	}
}
