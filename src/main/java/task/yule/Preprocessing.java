package task.yule;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.aliasi.tokenizer.TokenizerFactory;
import com.rouchtime.nlp.corpus.ClassificationCorpus;
import com.rouchtime.util.RegexUtils;

import tokenizer.JiebaTokenizerFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

public class Preprocessing {
	private static Logger logger = Logger.getLogger(Preprocessing.class);
	private static ClassificationCorpus corpus;
	private static TokenizerFactory tokenFactory;
	static {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring-mybatis.xml");
		corpus = (ClassificationCorpus) applicationContext.getBean(ClassificationCorpus.class);
		tokenFactory = getTokenFactory();
	}
	
	public static void nameEntity() {
		
	}
	
	
	public static void combineBaGua(String dir) {
		File[] listFiles = new File(dir, "八卦").listFiles();
		try {
			for (File file : listFiles) {
				String label = file.getName().replaceAll(".txt", "");
				List<String> lines = FileUtils.readLines(file, "utf-8");
				for (String line : lines) {
					String[] splits = line.split("\t");
					String cleanRaw = RegexUtils.cleanParaAndImgLabel(splits[2]);
					if (cleanRaw.length() < 50) {
						logger.info(line);
						continue;
					}
					FileUtils.write(new File(dir, "yulebagua"),
							String.format("%s\t%s\t%s\t%s\t%s\n", splits[0], splits[1], cleanRaw, "yuelebagua", label),
							"utf-8", true);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static TokenizerFactory getTokenFactory() {
		StopWordTokenierFactory stopWordFactory = new StopWordTokenierFactory(JiebaTokenizerFactory.getIstance());
		StopNatureTokenizerFactory stopNatureTokenizerFactory = new StopNatureTokenizerFactory(stopWordFactory);
		return stopNatureTokenizerFactory;
	}

	public static void combineSecondLabel(String dir) {
		try {
			File file = new File(dir, "yulezongyi.txt");
			String label = file.getName().replaceAll(".txt", "");
			List<String> lines = FileUtils.readLines(file, "utf-8");
			for (String line : lines) {
				String[] splits = line.split("\t");
				String cleanRaw = RegexUtils.cleanParaAndImgLabel(splits[2]);
				if (cleanRaw.length() < 50) {
					logger.info(line);
					continue;
				}
				FileUtils.write(new File(dir, label),
						String.format("%s\t%s\t%s\t%s\t%s\n", splits[0], splits[1], cleanRaw, label, label), "utf-8",
						true);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void pinyinConfig(String dir) throws Exception {
		List<String> lines = FileUtils.readLines(new File(dir,"zongyi.txt"), "utf-8");
		Map<String,Set<String>> map = new HashMap<String,Set<String>>();
		for(String line : lines) {
			String[] splits = line.split("\t");
			if(splits.length < 2) {
				continue;
			}
			if(map.get(splits[1]) == null) {
				Set<String> set = new HashSet<String>();
				set.add(splits[0]);
				map.put(splits[1],set);
			} else {
				map.get(splits[1]).add(splits[0]);
			}
		}
		List<String> lines1 = FileUtils.readLines(new File(dir,"zongyipinyin.txt"), "utf-8");
		Map<String,String> map1 = new HashMap<String,String>();
		for(String line : lines1) {
			String[] splits = line.split("\t");
			if(splits.length < 2) {
				continue;
			}
			map1.put(splits[0],splits[1]);
		}
		
		for(String key : map.keySet()) {
			String pinyin = map1.get(key);
			if(pinyin == null) {
				for(String name : map.get(key)) {
					FileUtils.write(new File(dir,"yule_new_zongyi"), String.format("%s\t%s\t%s\n", name,"无",key),"utf-8",true);
				}
			} else {
				for(String name : map.get(key)) {
					FileUtils.write(new File(dir,"yule_new_zongyi"), String.format("%s\t%s\t%s\n", name,pinyin,key),"utf-8",true);
				}
			}
		}
	} 
	
	
	
	
	public static void main(String[] args) throws Exception {
		pinyinConfig("D:\\corpus\\category\\yule");
		//		String text = "他们真的都犯法坐过牢？盘点明星艺人犯罪入狱事件！	不是演戏，是真的犯了法被判入狱！  明星艺人，他们的一举一动受到大众关注，无论是工作表现，或私生活都被评头论足，很多时候，言谈举止都会被放大评论。  房祖名独座咖啡厅，默默筹备专辑欲暑假重生。  6月10日文章报导，33岁房祖名卷呼麻风波出狱后定居台北，日前被直击独自驾奔驰车游荡街头，在咖 啡厅耗了1小时，难得冒出寂寞背影。据悉，上个月他的 患难兄弟柯震东已解禁赴中国拍新戏《舞樱》，为了不落后于对方，房祖名也接续写歌，默默地完成创作专辑，预计暑假发片重出江湖。同时，他也筹备自导自演拍 新片，但经纪人表示时间表尚未确定，届时会再公布。  想了解更多中国历史、灵异事件、外星人、ufo（不明飞行物）请关注公众号“第一趣事”，探索发现最新世界奇闻异事和未解之谜。  社会也普遍认为，艺人身为公众人物，就应当以身作则，作个大家学习的好榜样。  因此，当任何艺人犯错时，他们的罪行自然被公众大肆谴责，认为他们做了不良示范，罪加一等。  对于犯法入狱的艺人，他们除了得接受法律制裁、面对被亲友指责的压力，还必须应对大众媒体的追访以及 粉丝的眼光。出狱后，选择继续在演艺圈「混」的艺人，除了得看圈内有无工作机会，还得看观众买不买帐…盘点10位因暴力、吸毒、敲诈等不同罪行而被判入狱 的明星，一探他们出狱后的事业走向！    房祖名  罪行：容留他人吸毒、藏毒（2014年）  服刑年数：6个月  2014年8月17日，一名网友在微博发文，称台湾艺人柯震东涉毒被捕，引起大众关注。  隔日，中国北京市公安局官方微博发文，证实柯震东和香港艺人房祖名于8月14日在北京被查获。两人验尿结果皆呈大麻类阳性反应，警方还在房祖名的住所缴获毒品大麻100余克。柯震东被拘留10日后获释离开，由父母亲自接回台湾，并在台举行道歉会。  房祖名则以「容留他人吸毒」的罪名被正式逮捕，被判服刑6个月外，还得付处罚金人民币2000元。    近况：  房祖名刑满后获释，并于隔日举行媒体见面会，为自己的行为向公众道歉。  房祖名出狱后一直留在台北，由母亲林凤娇陪伴。  他接受台湾媒体采访时，大方透露半年的牢狱生活。房祖名也积极为复出演艺圈铺路。他先为尔东升的电影《我是路人甲》主题曲作词，希望透过歌词让年轻人走出迷茫。  导演尔东升更在微博公开替他打气：「你说过希望歌词可以让自己和更多年轻人走出迷茫，重新出发……我相信你一定可以，加油！」  曾经公开表示不会帮助儿子复出的成龙，日前对媒体改口表示：「我对自己说为何我会那么固执？我要帮助我的儿子。」  除了帮儿子找新戏演出，成龙还透露，稍后会出唱片，其中一首歌就是找儿子做监制，实行父子档上阵：「应该有首歌我们会互唱对方，我会说很抱歉你年轻时我很忙。」  有爸爸和尔东升撑腰，希望房祖名从此戒毒，重新做人，别再让疼爱他的前辈和粉丝失望~    Makiyo  罪行：普通伤害（2012年）  服刑年数：徒刑10个月、缓刑3年  台日混血女艺人Makiyo于2000年因接拍手机广告而踏入台湾演艺圈，除了发行唱片，也参与主持和戏剧演出。  个性大剌剌、爱喝酒的Makiyo，和大小S、范玮琪、柳翰雅（阿雅）、范晓萱以及吴佩慈关系亲密，7人组成的「七仙女」当属台湾演艺圈内知名度最高的姐妹淘帮派。  Makiyo出道以来， 事业和爱情皆顺遂，却因2012年的鲁莽行为而从此名誉扫地。2月2日当晚，Makiyo与日籍男友友寄隆辉、圈内好友丫子和王湘莹乘搭出租车，因不肯系 安全带被司机要求下车，双方因此发生口角。下车后，Makiyo猛踢车门，男友开始痛殴出租车司机。司机尝试打电话报警，却再遭友寄殴打倒地。穿高跟鞋的 Makiyo还猛踹司机头部，并教唆友人一同揍司机。  整个殴打过程被另一名出租车司机目击，他以行车记录器拍下图像，事后由司机妻子将一行人告上法庭。Makiyo和友寄以普通伤害罪遭起诉，Makiyo被判10个月徒刑、缓刑3年；友寄被判1年徒刑、缓刑4年，同时遭驱逐出境。    近况：  因殴打事件而形象严重受挫的Makiyo，于2013年出狱后积极出席慈善活动，并将表演尾牙的酬劳全数捐出，力求改过自新。  后来，其母亲Ma妈妈证实患上肺癌，Makiyo减少工作量，悉心照顾病母。最终，Ma妈妈不敌病魔，与世长辞。经历丧母之痛的Makiyo，开始赴中国发展，邀约接踵而来。  据了解，她参加节目录像的酬劳，大约一集6万元人民币，出席一场活动大约是5万人民币。与通告邀约直线下降的丫子和月酬近乎减半的王湘莹相比，Makiyo算是成功复出演艺圈。    PSY（朴载相）  罪行：吸大麻（2001年）  服刑年数：25天  江南大叔PSY在2001年以嘻哈歌手身份出道，2012年凭一曲洗脑歌《Gangnam Style》爆红。PSY在2013年接受英国《周日时报》杂志访问时自爆，出道初期曾因吸大麻而被捕入狱25天。  而服刑期间最令他深感遗憾的事，莫过于无法出席祖父的丧礼。  近况：  虽然成功戒毒，但PSY自嘲还是戒不了烟瘾。他凭《Gangnam Style》一曲名声大噪，事业达到巅峰。除了受邀到国际舞台如全美音乐奖（American Music Awards）表演外，他的歌曲也横扫不少欧美音乐奖项。  2013年，PSY陆续推出单曲《Gentleman》、《Hangover》以及《Father》，销量不俗。    李铭顺  罪行：酒驾、肇事逃逸（2006年）  服刑年数：四个星期  2006年10月8日，新传媒阿哥李铭顺在喝醉酒的情况下撞倒一台摩托车，导致司机和乘客摔车受伤。司机的脚趾因伤势过重，被迫切除。  事发当时，李铭顺并无停下关心伤者，而是驰车而去。警方截住他时，发现他酒醉驾驶。两名伤者亦对李铭顺提出民事起诉。  李铭顺因此被判服刑4个星期，其驾驶执照被吊销3年。  阿哥的人气并没有因为负面新闻而受损。2006和2007年，他还是获选《红星大奖》之十大最受欢迎男艺人。不过，他因此未能出演2007年8频道的警匪剧《破茧而出》的警探角色，改由王沺裁接演。    近况：  阿哥经历监狱风波期间，人气不但不受影响，与其相恋的阿姐范文芳对他更是不离不弃。  2009年，这对受观众肯定的「神雕侠侣」，在众亲友的见证下拉理天窗，并于2014年的国庆日诞下男宝宝Zed。  阿哥的演艺之路也可说是扶摇直上，多年来陆续演出不少本地制作。2010年，李铭顺荣获《红星大奖》的「超级红星」殊荣，成功登上「神台」。  阿哥也积极到台湾发展，凭戏剧节目《亲爱的，我爱上别人了》获得第49届台湾《金钟奖》的「戏剧节目男主角奖」！    古天乐  罪行：抢劫（1990年）  服刑年数：22个月  古天乐1993年出道成为演员，不曾提及入狱一事，但被媒体揭露后，他大方在电视节目坦承此事，也在自传《寻乐记》重述整个事件。  1990年11月，20岁的古天乐曾和朋友参与一宗海港城厕所的抢劫案件，因此被判入狱服刑22个月。  他在书中自述：「当时的我20岁，年轻、有大好的未来，但是身边却围绕一群不务正业的朋友，没多久即惹祸上身。那时讲义气愿意承担一切，而换来的却是因为抢劫一位女子，而成为他人眼中闻之色变的抢劫犯。」  近况：  古天乐从此洗心革面，入行后便努力发展事业。1995年，他因饰演香港TVB剧《神雕侠侣》杨过一角而备受瞩目。他于1999年和2001年两度荣获TVB年度台庆节目《万千星辉贺台庆》的「最佳男主角」后，开始转战电影圈。  近5年，古天乐全力接演电影，每年至少有3部电影上映，口碑相当不错。  可惜，古天乐至今未获得任何电影演技奖项。希望他再接再厉，如愿夺个影帝！    高永旭  罪行：性侵未成年（2013年）  服刑年数：2年6个月  韩国混声组合Roo’Ra前成员高永旭于2012年5月被韩国警方证实，涉嫌以帮助一名17岁少女实现歌手梦的承诺为诱饵，对该少女多次实施性侵犯。  据韩国媒体报道，高永旭自2010年7月到2012年12月，曾4次实施性暴力与性骚扰，受害者共有3名未成年少女，其中年龄最小的为13岁金姓少女。  高永旭最终被判刑2年6个月、出狱后得佩戴电子脚镣3年以及公开个人信息5年。而他入狱前所录制的综艺节目也全部被删剪。  近况：  高永旭刑满获释后，他在记者会上向公众鞠躬道歉：「两年半这期间，日子并不好过，但这段日子让我领悟了许多事。身为公众人物，我为自己对社会带来的种种麻烦，向大家道歉。」  高永旭在记者会上未透露是否将重返演艺圈。    萧淑慎  罪行：吸食古柯碱（2011年）  服刑年数：1年7个月、缓刑4年  台湾女艺人萧淑慎曾是新加坡音乐才子李伟菘和李偲菘两兄弟的爱徒，亦是本地天后孙燕姿的学妹。  她的独特声线受滚石唱片公司青睐，除了有机会到新加坡培训音乐，其专辑里的所有曲子都由李家兄弟打造。当年，她客串梁静茹《勇气》的MV，凭傲人的身材和甜美的长相，一度被封为台湾女神。  星途原本前程似锦，可惜萧淑慎不懂得珍惜。2000年起，她因不断吸毒的负面新闻导致演艺事业全面崩溃。2010年5月，她3度验尿都被检验有吸食冰毒并呈现阳性反应，依3次吸毒记录被判刑1年7个月。    近况：  昔日女神萧淑慎于2012年7月假释出狱，被台湾记者目击暴肥，体重飙到75公斤。但她为了复出演艺圈，积极减肥，5个月内甩肉20公斤。  去年8月，萧淑慎携同门师弟顾又铭以及麻辣女生、Super Group组合一同打造音乐作品《神来了》，其中也收录萧淑慎的个人专辑《女王》，可见萧淑慎积极发展演艺事业。  萧淑慎出道初期，无论歌艺或演技都受观众肯定，希望她把握此次重出江湖的机会，专心发展事业，不要再令疼惜她的粉丝失望了吧！    刘晓庆  罪行：逃漏税（2002年）  服刑年数：一年多  有「不老女星」之称的中国演员刘晓庆，于2002年被中国国税总局查出，自1996年起，刘晓庆及其公司以虚报收入与支出的方式逃漏税，总额多达人民币1400万。  刘晓庆在中国北京被拘捕，被关押一年多。  近况：  刘晓庆出狱后，人气和事业一如往常地旺，电视剧邀约还是一部接一部。  入狱对她而言，似乎只是暂时息影去「度假」    GLAM成员多熙  罪行：恐吓韩星李炳宪（2014年）  服刑年数：1年  韩国演员李炳宪于2013年8月与小10岁女星李敏贞结婚，是韩国娱乐圈中的模范夫妻。不料结婚刚满一年，却发生李炳宪的桃色丑闻事件。  去年9月，李炳宪在一场聚会和好友喝酒，场内有两名女子分别为20和24岁。当时饮酒畅谈的李炳宪谈吐不雅，被两名女子全程录下，并以此勒索男方50亿韩元，威胁若不给钱就在网上公开视频。  李炳宪选择向警方报案，称两名女性以「淫秽言辞视频」勒索自己。  事件查明，确认两名女性为韩国女子团体GLAM成员之一金多熙，以及模特儿李智妍。  金多熙和李智妍认罪，并分别被判刑1年和14个月。金多熙也因此被迫退团。  近况：  受害者同时也是原告的李炳宪事后和妻子到美国逗留。妻子今年3月31日诞下儿子后，李炳宪也重返演艺圈，接下两部电影，其中一部为美国电影《Terminator Genisys》。  金多熙则于今年3月刑满出狱，有无可能复出演艺圈，至今仍是个未知数。  希望金多熙和李智妍吸取教训，以后凡事三思而行~    朴时妍、李丞涓和张美仁爱  罪行：非法注射麻醉药品（2013年）  服刑年数：8个月、缓刑2年  韩国艺人朴时妍、李丞涓和张美仁爱于2013年3月被起诉违法注射麻醉药品Propofol。  韩国法院使用长达8个月的时间审查此案、出庭证人多达25人、检察官提交的证据多达440个。  李丞涓提出上诉，声称自己在拍摄戏剧过程中，因脊椎严重骨折，因此在医生的指引下接受注射治疗，并非滥用药品。  然而，证据显示，李丞涓过去6年内的注射次数共320次，还曾在同一天到不同医院进行注射，难以令人相信纯粹是为了接受医疗目的。  朴时妍则被证实在过去4年里，注射次数多达400多次；张美仁爱在6年内注射410次。  3名女艺人违法使用麻醉药品的罪名最终成立，判刑8个月、缓刑2年。  近况：  朴时妍2014年参与拍摄韩国电视剧《最佳婚姻》，是复出演艺圈后的首部作品。今年，她也有幸参演好莱坞制作《Last Knights》，同巨星Morgan Freeman以及Clive Owen合作。  另两名艺人李丞涓和张美仁爱，获释后并未参与任何演艺活动，相信已淡出娱乐圈。";
//		Long s = System.currentTimeMillis();
//		int count = RegexUtils.countLabelInRaw(text,"的");
//		for(int i=0;i<10000;i++) {
//			if(text.indexOf("你好") == -1) {
//				continue;
//			}
//			 count = RegexUtils.countLabelInRaw(text,"的");
//		}
//		Long d = System.currentTimeMillis() - s;
//		System.out.println(d);
//		System.out.println(count);
	}
}
