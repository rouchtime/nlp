package corpus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.fnlp.nlp.cn.CNFactory;
import org.fnlp.util.exception.LoadModelException;

import junit.framework.TestCase;
import weka.attributeSelection.CorrelationAttributeEval;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * 统计文章特征
 * @author 龚帅宾
 *
 */
public class RealOrNotRealNewsCorpusTest extends TestCase {
	/**
	 * 统计新闻文本中第一句子中，时间特征的数量
	 * 
	 * @throws LoadModelException
	 * @throws IOException
	 */
	public void staticsFisrtSentsDateCountTest() throws LoadModelException, IOException {
		// CNFactory factory = CNFactory.getInstance("models");
		RealOrNotRealNewsCorpus corpus = new RealOrNotRealNewsCorpus();
		String regex = "(\\\\d{1,}年)?(\\d{1,}月)?(\\d{1,}日)?";
		Pattern pattern = Pattern.compile(regex);

		int dateCount = 0;
		for (String title : corpus.fileidsFromLabel("notnews")) {
			String firstSents = corpus.sents(title).get(0);
			Matcher matcher = pattern.matcher(firstSents);
			while (matcher.find()) {
				if (!matcher.group().equals("")) {
					dateCount++;
					break;
				}
			}
		}
		System.out.println(dateCount * 1.0 / corpus.fileidsFromLabel("notnews").size());
	}

	public void statisticsRawDateCountTest() throws LoadModelException, IOException {
		CNFactory factory = CNFactory.getInstance("models");
		FinanceNewsOrNonCorpus corpus = new FinanceNewsOrNonCorpus();
		for (String label : corpus.labels()) {
			for (String title : corpus.fileidsFromLabel(label)) {
				int dateCount = 0;
				String[][] tags = factory.tag(corpus.raws(title));
				for (int i = 0; i < tags[0].length; i++) {
					if (tags[1][i].equals("时间短语")) {
						dateCount++;
					}
				}
				FileUtils.write(new File("D://copus//realTimeOrNotNews//dateCountRaws"), dateCount + "\t" + label+"\n",true);
			}
		}
	}
	
	
	/**
	 * 文章第一句中包含时间词汇的测试
	 * 
	 * @throws LoadModelException
	 * @throws IOException
	 */
	public void staticsFisrtSentsDateCountByNatureTest() throws LoadModelException, IOException {
		CNFactory factory = CNFactory.getInstance("models");
		FinanceNewsOrNonCorpus corpus = new FinanceNewsOrNonCorpus();
		for (String label : corpus.labels()) {
			for (String title : corpus.fileidsFromLabel(label)) {
				int dateCount = 0;
				String[][] tags = factory.tag(corpus.sents(title).get(0));
				for (int i = 0; i < tags[0].length; i++) {
					if (tags[1][i].equals("时间短语")) {
						dateCount++;
					}
				}
			}
		}
	}

	/**
	 * 测试图片的数量
	 * @throws LoadModelException
	 * @throws IOException
	 */
	public void staticsPictureTest() throws LoadModelException, IOException {
		FinanceNewsOrNonCorpus corpus = new FinanceNewsOrNonCorpus();
		for (String label : corpus.labels()) {
			for (String title : corpus.fileidsFromLabel(label)) {
				int picCount = corpus.picCount(title);
				Double ratio_picCount_length = (double) (picCount * 1.0 / corpus.raws(title).length() * 1.0);
				FileUtils.write(new File("D://corpus//realTimeOrNotNews//picCountRaiton"),
						ratio_picCount_length + "\t" + label + "\n", true);
			}
		}
	}

	public static int calImgSize(String raw) {
		int count = 0;
		Pattern pattern = Pattern.compile("！|？|“|”");
		Matcher m = pattern.matcher(raw);
		while (m.find()) {
			if (m.group() != "") {
				count++;
			}
		}
		return count;
	}

	public static int calDateWordSize(String raw) {
		int count = 0;
		Pattern pattern = Pattern.compile("今天|今|昨天|昨|上午|下午");
		Matcher m = pattern.matcher(raw);
		while (m.find()) {
			if (m.group() != "") {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * 统计词 (...讯)
	 * @param raw
	 * @return
	 */
	public static int calWordXunSize(String raw) {
		int count = 0;
		Pattern pattern = Pattern.compile("(\\w*)讯");
		Matcher m = pattern.matcher(raw);
		while (m.find()) {
			if (m.group() != "") {
				count++;
			}
		}
		return count;
	}
	
	public void staticsWordXunCountTest() throws LoadModelException, IOException {
		RealOrNotRealNewsCorpus corpus = new RealOrNotRealNewsCorpus();
		
		for (String label : corpus.labels()) {
			
			for (String title : corpus.fileidsFromLabel(label)) {
				int count = 0;
				String firstSents = corpus.sents(title).get(0);
				count += calWordXunSize(firstSents);
				FileUtils.write(new File("D://corpus//realTimeOrNotNews//xunWordCount2"), count + "\t" + label + "\n",
						true);
			}

		}
	}
	
	public static int calJINRIize(String raw) {
		int count = 0;
		Pattern pattern = Pattern.compile("近日");
		Matcher m = pattern.matcher(raw);
		while (m.find()) {
			if (m.group() != "") {
				count++;
			}
			
		}
		return count;
	}
	
	/**
	 * 测试标点符号的数量
	 * @throws LoadModelException
	 * @throws IOException
	 */
	public void staticsPincTest() throws LoadModelException, IOException {
		FinanceNewsOrNonCorpus corpus = new FinanceNewsOrNonCorpus();
		for (String label : corpus.labels()) {
			for (String title : corpus.fileidsFromLabel(label)) {
				String raw = corpus.raws(title);
				int count = calImgSize(raw);
//				double pincRatio = (count * 1.0) / (raw.length() * 1.0+0.0001);
				FileUtils.write(new File("D://corpus//realTimeOrNotNews//pincRatio"), count + "\t" + label + "\n",
						true);
			}
		}
	}

	/**
	 * 测试标题长度
	 * @throws LoadModelException
	 * @throws IOException
	 */
	public void staticsTitleLengthTest() throws LoadModelException, IOException {
		FinanceNewsOrNonCorpus corpus = new FinanceNewsOrNonCorpus();
		for (String label : corpus.labels()) {
			for (String title : corpus.fileidsFromLabel(label)) {
				
//				double pincRatio = (count * 1.0) / (raw.length() * 1.0+0.0001);
				FileUtils.write(new File("D://corpus//realTimeOrNotNews//titleLength"), title.length() + "\t" + label + "\n",
						true);
			}
		}
	}
	
	public void staticsParaLengthTest() throws LoadModelException, IOException {
		FinanceNewsOrNonCorpus corpus = new FinanceNewsOrNonCorpus();
		for (String label : corpus.labels()) {
			for (String title : corpus.fileidsFromLabel(label)) {
				
//				double pincRatio = (count * 1.0) / (raw.length() * 1.0+0.0001);
				FileUtils.write(new File("D://corpus//realTimeOrNotNews//paraCount"), corpus.paraCount(title) + "\t" + label + "\n",
						true);
			}
		}
	}
	
	public void staticsParaLengthRatioTest() throws LoadModelException, IOException {
		FinanceNewsOrNonCorpus corpus = new FinanceNewsOrNonCorpus();
		for (String label : corpus.labels()) {
			for (String title : corpus.fileidsFromLabel(label)) {
				String raws = corpus.raws(title);
				int count = corpus.paraCount(title);
				double pincRatio = (count * 1.0) / (raws.length() * 1.0+0.0001);
				FileUtils.write(new File("D://corpus//realTimeOrNotNews//paraCountRatio"), pincRatio + "\t" + label + "\n",
						true);
			}
		}
	}
	
	public void staticsDateWordTest() throws LoadModelException, IOException {
		FinanceNewsOrNonCorpus corpus = new FinanceNewsOrNonCorpus();
		for (String label : corpus.labels()) {
			for (String title : corpus.fileidsFromLabel(label)) {
				StringBuffer sb = new StringBuffer();
				sb.append(corpus.sents(title).get(0));
				try {
					String secondSent  =corpus.sents(title).get(1);
					sb.append(secondSent);
					String thirdSent  =corpus.sents(title).get(2);
					sb.append(thirdSent);
				}catch(Exception e) {
					int count = calJINRIize(sb.toString());
					FileUtils.write(new File("D://corpus//realTimeOrNotNews//jinri"), count + "\t" + label + "\n",
							true);
					continue;
				}
				int count = calJINRIize(sb.toString());
				FileUtils.write(new File("D://corpus//realTimeOrNotNews//jinri"), count + "\t" + label + "\n",
						true);
			}
		}
	}
	
	/**
	 * 统计文章长度
	 * @throws LoadModelException
	 * @throws IOException
	 */
	public void staticsArticleLengthTest() throws LoadModelException, IOException {
		FinanceNewsOrNonCorpus corpus = new FinanceNewsOrNonCorpus();
		for (String label : corpus.labels()) {
			for (String title : corpus.fileidsFromLabel(label)) {
				String article = corpus.raws(title);
//				double pincRatio = (count * 1.0) / (raw.length() * 1.0+0.0001);
				FileUtils.write(new File("D://corpus//realTimeOrNotNews//articleLength"), article.length() + "\t" + label + "\n",
						true);
			}
		}
	}
	
	public void printFileTest() throws IOException {
		RealOrNotRealNewsCorpus corpus = new RealOrNotRealNewsCorpus();
		for (String label : corpus.labels()) {
			List<String> fileids = corpus.fileidsFromLabel(label);
			for (String fileid : fileids) {
				String cleanFileid = fileid.replaceAll("[\\p{P}+~$`^=|<>～｀＄＾＋＝｜＜＞￥×]", "");
				FileUtils.write(new File("D://corpus//realTimeOrNotNews//" + label + "//" + corpus.category(fileid)
						+ "//" + cleanFileid + ".txt"), corpus.url(fileid) + "\n" + corpus.raws(fileid), true);
			}
		}
	}

	public void printFileFinanceTest() throws IOException {
		FinanceNewsOrNonCorpus corpus = new FinanceNewsOrNonCorpus();
		for (String label : corpus.labels()) {
			List<String> fileids = corpus.fileidsFromLabel(label);
			for (String fileid : fileids) {
				String cleanFileid = fileid.replaceAll("[\\p{P}+~$`^=|<>～｀＄＾＋＝｜＜＞￥×]", "");
				FileUtils.write(new File("D://corpus//realTimeOrNotNews//" + label + "//" + cleanFileid + ".txt"),
						corpus.url(fileid) + "\n" + corpus.raws(fileid), true);
			}
		}
	}
	
	public void testNumWordCount() throws LoadModelException, IOException {
		CNFactory factory = CNFactory.getInstance("models");
		FinanceNewsOrNonCorpus corpus = new FinanceNewsOrNonCorpus();
		for (String label : corpus.labels()) {
			for (String title : corpus.fileidsFromLabel(label)) {
				int dateCount = 0;
				String[][] tags = factory.tag(corpus.raws(title));
				for (int i = 0; i < tags[0].length; i++) {
					if (tags[1][i].equals("数词")) {
						dateCount++;
					}
				}
				double ratio = (dateCount*1.0)/(corpus.raws(title).length()*1.0);
				FileUtils.write(new File("D://copus//realTimeOrNotNews//ShuciCountRawsRaito"), ratio + "\t" + label+"\n",true);
			}
		}
	}

	public void testPersonNameCount() throws LoadModelException, IOException {
		CNFactory factory = CNFactory.getInstance("models");
		FinanceNewsOrNonCorpus corpus = new FinanceNewsOrNonCorpus();
		for (String label : corpus.labels()) {
			for (String title : corpus.fileidsFromLabel(label)) {
				int countPersonName = 0;
				HashMap<String, String> result = factory.ner(corpus.sents(title).get(0));
				for(String key : result.keySet()) {
					if(result.get(key).equals("机构名")) {
						countPersonName++;
					}
				}
				FileUtils.write(new File("D://corpus//realTimeOrNotNews//agentNameCount1"), countPersonName + "\t" + label+"\n",true);
			}
		}
	}
	
	public void nerTest() throws LoadModelException {
	 	CNFactory factory = CNFactory.getInstance("models");

	 	// 使用标注器对包含实体名的句子进行标注，得到结果
	 	HashMap<String, String> result = factory.ner("\r\n" + 
	 			"“十三五”开局之年，上海国际金融中心建设也进入纵深攻坚。金融改革创新持续深化，金融基础设施不断完善，制度创新深度推进，要素定价能力和话语权显著提升……新华社上海分社、新华社中国经济信息社上海中心、中国金融信息中心9日联合发布“2016上海国际金融中心建设十大事件”。1、自贸区金改继续深化，率先启动金融综合监管试点在上海自贸区建设迎来三周年之际，“金改40条”实施细则陆续出台，金融创新精彩纷呈。金融监管机制和风险监测体系逐步完善，上海提出编制“分业监管机构清单”和“重点监测金融行为清单”，搭建金融综合监管联席会议平台，开展金融综合监管试点、探索功能监管，为国家层面金融监管改革探索路径、积累经验，守住不发生区域性系统性金融风险的底线。2、金融支持科创中心建设力度进一步加大2016年4月，张江国家自主创新示范区被列为全国首批投贷联动试点地区之一，上海银行、华瑞银行、浦发硅谷银行等三家法人银行入选试点银行，国开行等5家银行的在沪分行也获准开展试点。上海股权托管交易中心“科技创新板”开盘以来，挂牌企业总数达102家，全部为科技型创新型企业，融资总额11.45亿元；上海市中小微企业政策性融资担保基金成立，首期筹集资金为50亿元。3、上海保险交易所揭牌，航运保险市场快速发展上海保险交易所于2016年6月正式揭牌运营，填补了保险要素市场空白，进一步完善了上海国际金融中心市场体系和功能，吸引国际保险、再保险主体集聚。中国保险行业国家级投资平台——中国保险投资基金落户自贸区，首批基金规模达400亿元。上海航运保险协会代表中国加入全球最大航运保险协会组织——国际海上保险联盟（IUMI），并发布上海航运保险指数（SMII），进一步提高我国航运保险企业的风险管理和定价能力。4、上海票据交易所、中国信托登记公司成立2016年12月，上海票据交易所、中国信托登记公司相继开业，成为我国金融要素市场的新起点和里程碑事件。上海票据交易所将搭建票据交易平台、风险防范平台、货币政策操作平台、业务创新平台以及信息平台等五大模块，大大丰富上海金融基础设施布局，创建我国票据市场发展新高地。中国信托登记公司是全国唯一的信托登记机构，将搭建全国信托产品的集中登记平台、信托产品的统一发行流转平台以及信托业运行监测平台等三个平台。5、中国互联网金融协会在上海成立中国互联网金融协会落户上海，它承担制定全国行业规则和行业标准、建立行业自律惩戒机制等职能，对行政监管形成补充和支撑，提高金融监管的弹性和有效性。与此同时，按照国家统一部署，上海互联网金融风险专项整治正式启动。通过第一阶段摸底排查，初步梳理掌握风险底数，并启动了清理整顿工作。6、全球中央对手方协会（CCP12）落户上海2016年6月，全球中央对手方协会法人实体落户上海。该协会是唯一的全球性中央对手清算机构同业组织，覆盖了全球最主要交易所市场和场外市场，在国际金融体系改革中的作用逐步凸显。率先将全球中央对手方协会引入我国，将极大地提升我国在这一领域的影响力。7、上海黄金交易所推出人民币计价“上海金”全球首个以人民币计价的集中定价合约“上海金”在上海黄金交易所正式上线，是继黄金国际板推出后中国黄金市场国际化发展的又一标志性事件。“上海金”是中国增强全球要素定价权的重要尝试，国际黄金市场将形成以人民币标价的“上海金”基准价格，与美元标价的LBMA黄金基准价格互为补充，推动国际黄金市场体系平衡发展。8、金砖国家新开发银行、浦发银行发行人民币绿色金融债券2016年7月，金砖国家新开发银行在银行间市场发行人民币计价的绿色金融债券，总值30亿元人民币，这是首只由总部设在中国的国际金融机构发行的人民币绿色债券。债券的发行体现了国际金融机构对人民币国际化和“熊猫债”市场的信心与认可，体现了金砖国家新开发银行对推动全球绿色经济增长与发展的贡献。2016年1月，浦发银行成功发行境内首单绿色金融债券，发行规模200亿元，实现了国内绿色金融债券从制度框架到产品的正式落地。9、国内首只自贸区地方债发行, 上交所首次发行地方债2016年12月，上海市财政局通过中央国债登记结算公司面向上海自贸区内已开立自由贸易账户的区内及境外机构投资者，成功发行30亿元上海市政府债券。这是我国首只自贸区债券，将为境外投资者提供优质人民币资产，拓宽境外人民币回流渠道，对推动上海自贸区金融改革、助推人民币国际化进程和地方政府债券市场发展具有深远而重要的意义。2016年11月，上海市财政局通过上海证券交易所的政府债券发行系统，招标发行300亿元地方债，这是该系统启用后在上交所招标发行的首只地方债，有利于拓宽地方债市场发行渠道，优化地方债投资者结构，提高地方债流动性，丰富交易所市场债券品种。10、国开行上海业务总部成立，陆家嘴区域率先试水“业界共治”模式2016年6月，国家开发银行上海业务总部在沪正式成立。这将促进国开行各驻沪机构、长三角地区分行、公司业务的信息交流、资源共享、协调发展，推进功能性机构及子公司在沪集聚，推动业务协同和产品创新。2016年8月，上海陆家嘴金融城正式开展体制改革试点，在全国率先实施“业界共治”的公共治理架构，即以陆家嘴金融城理事会作为金融城业界共治和社会参与的公共平台，以上海陆家嘴金融城发展局作为金融城法定的管理服务机构。这一对标国际规则的重大改革举措，将提升陆家嘴金融城在全球金融市场的影响力，打造国际一流金融城，加快上海国际金融中心和全球城市的建设步伐。（原题为《2016上海国际金融中心建设十大事件发布》）");

	 	// 显示标注结果
	 	System.out.println(result);
	}

	
	
}
