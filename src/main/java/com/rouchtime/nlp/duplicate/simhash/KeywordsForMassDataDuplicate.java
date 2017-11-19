package com.rouchtime.nlp.duplicate.simhash;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.hankcs.hanlp.HanLP;
import com.rouchtime.util.RegexUtils;

import hbaseDao.HbaseTbKwQueryDao;
import hbaseDao.HbaseTbUrlKwDao;
import hbaseDao.KwBean;
import hbaseDao.UrlKwBean;

public class KeywordsForMassDataDuplicate {
	private String columFamliy = "d";
	private String zk = "";
	HbaseTbKwQueryDao tbKwQueryDao = HbaseTbKwQueryDao.getInstance();
	HbaseTbUrlKwDao tbUrlKwDao = HbaseTbUrlKwDao.getInstance();

	private Map<String, List<Doc>> map = new HashMap<String, List<Doc>>();

	public boolean add(String newsKey, String doc) {
		/* 检查newsKey是否可以转化成Long类型，hbase中的列名取Max(Long) - Long.parse(newsKey)的差值 */
		String urlKey = null;
		try {
			urlKey = String.valueOf(Long.MAX_VALUE - Long.parseLong(newsKey));
		} catch (NumberFormatException e) {
			System.err.println(String.format("%s\nNewsKey parse Long error! NewsKey = %s",
					ExceptionUtils.getFullStackTrace(e), newsKey));
			return false;
		}

		List<String> keywords = HanLP.extractKeyword(doc, 30);
		if (keywords.size() <= 7) {
			System.err.println("keywords size less 7!");
			return false;
		}
		List<String> keys = splitFingerPrint(keywords);

		try {
			/* 查询tb_kw_query表 */
			List<KwBean> kwList = tbKwQueryDao.getListBean(keys, zk);
			if (kwList.size() != keys.size()) {
				System.err.println(String.format("Check Keys Number is Not Equal Result Number %d!=%d", keys.size(),
						kwList.size()));
				return false;
			}

			/* 查询tb_url_kw表 */
			List<String> urlKeys = new ArrayList<String>();
			urlKeys.add(urlKey);
			List<UrlKwBean> listKwBean = tbUrlKwDao.getListBean(urlKeys, zk);
			if (listKwBean.size() != urlKeys.size()) {
				System.err.println(String.format("Check Keys Number is Not Equal Result Number %d!=%d", keys.size(),
						urlKeys.size()));
			}

			/* 更新和插入tb_kw_query表 */
			for (int i = 0; i < kwList.size(); i++) {
				if (kwList.get(i) == null) {
					KwBean kwb = new KwBean();
					kwb.setFamily(columFamliy);
					kwb.setRk(keys.get(i));
					Map<String, String> map = new HashMap<String, String>();
					map.put(urlKey, newsKey);
					kwb.setMap(map);
					kwList.set(i, kwb);
				} else {
					KwBean kwb = kwList.get(i);
					Map<String, String> map = kwb.getMap();
					map.put(urlKey, newsKey);

				}
			}
			tbKwQueryDao.putListBean(kwList, zk);

			/* 更新和插入tb_url_kw表 */
			StringBuffer value = new StringBuffer();
			for (String keyword : keywords) {
				value.append(keyword).append(",");
			}
			value.delete(value.length() - 1, value.length());
			if (listKwBean.get(0) == null) {
				UrlKwBean bean = new UrlKwBean();
				bean.setFamily(columFamliy);
				bean.setRk(urlKey);
				Map<String, String> map = new HashMap<String, String>();
				map.put(value.toString(), null);
				listKwBean.set(0, bean);
			}
			tbUrlKwDao.putListBean(listKwBean, zk);
		} catch (IOException e) {
			System.err.println(ExceptionUtils.getFullStackTrace(e));
			return false;
		}
		return true;
	}

	public List<String> select(String doc) {
		List<String> keywords = HanLP.extractKeyword(doc, 30);
		if (keywords.size() <= 7) {
			System.err.println("keywords size less 7!");
			return null;
		}
		List<String> keys = splitFingerPrint(keywords);
		StringBuffer value = new StringBuffer();
		for (String keyword : keywords) {
			value.append(keyword).append(",");
		}
		value.delete(value.length() - 1, value.length());
		try {
			List<KwBean> kwBeanList = tbKwQueryDao.getListBean(keys, zk);
			Set<String> resultUrlSet = new HashSet<String>();
			for (KwBean bean : kwBeanList) {
				if (bean == null) {
					continue;
				}
				Set<String> selectUrlSet = new HashSet<String>();
				for (Entry<String, String> entry : bean.getMap().entrySet()) {
					selectUrlSet.add(entry.getKey());
				}
				List<String> selectUrlList = new ArrayList<String>(selectUrlSet);
				List<UrlKwBean> resultUrlKwBean = tbUrlKwDao.getListBean(selectUrlList, zk);
				if (selectUrlList.size() != resultUrlKwBean.size()) {
					continue;
				}
				for (int i = 0; i < resultUrlKwBean.size(); i++) {
					if (resultUrlKwBean.get(i) == null) {
						continue;
					}
					String tmpKeywords = resultUrlKwBean.get(i).getMap().get(selectUrlList.get(i));
					if (resultUrlSet.contains(tmpKeywords)) {
						continue;
					} else {
						String[] array_keyswords = tmpKeywords.split(",");
						Set<String> set1 = new HashSet<String>(Arrays.asList(array_keyswords));
						Set<String> set2 = new HashSet<String>(keywords);
						if (jaccardIndex(set1, set2) >= 0.9) {
							resultUrlSet.add(value.toString());
						}
					}

				}
			}
			return new ArrayList<String>(resultUrlSet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public void addToRAM(String newskey, String doc) {
		List<String> keywords = HanLP.extractKeyword(doc, 30);
		if (keywords.size() <= 7) {
			System.out.println("keywords size less 7!");
			return;
		}
		Collections.sort(keywords);
		for (int i = 0; i < keywords.size() - 7; i++) {
			StringBuffer sb = new StringBuffer();
			for (int j = 0; j < 7; j++) {
				sb.append(keywords.get(i + j));
			}
			String key = sb.toString();
			Doc d = new Doc();
			StringBuffer sbk = new StringBuffer();
			for (String keyword : keywords) {
				sbk.append(keyword).append(",");
			}
			sbk.delete(sbk.length() - 1, sbk.length());
			d.setKeywords(sbk.toString());
			d.setRowkey(newskey);
			if (map.get(key) == null) {
				List<Doc> listDoc = new ArrayList<Doc>();
				listDoc.add(d);
				map.put(key, listDoc);
			} else {
				map.get(key).add(d);
			}
			sb.setLength(0);
		}
	}

	public Set<String> selectFromRAM(String doc) {
		List<String> keywords = HanLP.extractKeyword(doc, 30);
		if (keywords.size() <= 7) {
			System.out.println("keywords size less 7!");
			return null;
		}
		Collections.sort(keywords);
		Set<String> urlSet = new HashSet<String>();
		for (int i = 0; i < keywords.size() - 7; i++) {
			StringBuffer sb = new StringBuffer();
			for (int j = 0; j < 7; j++) {
				sb.append(keywords.get(i + j));
			}
			String key = sb.toString();
			if (map.get(key) != null) {
				for (Doc d : map.get(key)) {
					if (!urlSet.contains(d.getRowkey())) {
						String[] array_keyswords = d.getKeywords().split(",");
						Set<String> set1 = new HashSet<String>(Arrays.asList(array_keyswords));
						Set<String> set2 = new HashSet<String>(keywords);
						if (jaccardIndex(set1, set2) >= 0.9) {
							urlSet.add(d.getRowkey());
						}
					}
				}
			}
			sb.setLength(0);
		}
		return urlSet;
	}

	private double jaccardIndex(final Set<String> s1, final Set<String> s2) {

		Set<String> intersection = new HashSet<String>(s1);
		intersection.retainAll(s2);

		Set<String> union = new HashSet<String>(s1);
		union.addAll(s2);
		if (union.isEmpty()) {
			return 0;
		}
		return (double) intersection.size() / union.size();
	}

	private List<String> splitFingerPrint(List<String> keywords) {
		Collections.sort(keywords);
		List<String> keys = new ArrayList<String>();
		for (int i = 0; i < keywords.size() - 7; i++) {
			StringBuffer sb = new StringBuffer();
			for (int j = 0; j < 7; j++) {
				sb.append(keywords.get(i + j));
			}
			String key = sb.toString();
			keys.add(key);
		}
		return keys;
	}

	public static void main(String[] args) throws Exception {
		// long a = 121334l;
		// System.out.println(out(a));
		// splitSimhash(a);
		DupKWUtils utils = new DupKWUtils();
		// String[] test = new String[5];
		// for(int i=0;i<5;i++) {
		// test[i] = String.valueOf(i);
		// }
		// List<String[]> arrays = new ArrayList<String[]>();
		// combinationSelect(test,2,arrays);
		// for(int i=0;i<arrays.size();i++) {
		// System.out.println(arrays.get(i)[0]+","+arrays.get(i)[1]);
		// }
		// String text1 = "身上持续出现4个现象，说不定你已经被肿瘤盯上了
		// !@#!@癌症之所以那么可怕，是因为它来得快。据很多癌友表示，每年都进行体检，可突然就得了癌症。其实，恶性肿瘤的潜伏期很长，也正因为这个，中老年人才是癌症的高危人群。不过，即使是老年癌友，他们也认为癌症是突然来临的，为什么会这样呢？!@#!@$#imgidx=0001#$!@#!@其实，癌症来临前都会给身体发出警告，只是这些警告太不明显，常被误当成其他疾病，甚至被忽略。!@#!@很多的权威数据告诉我们，早发现、早治疗，大部分的恶性肿瘤都能治好。那如何才能早发现呢？这就需要你有一双敏锐的眼睛，能看透这些身体报警信号。!@#!@1、局部表现!@#!@（1）肿块!@#!@肿块是瘤细胞异常增生所形成的，可以出现在体表，也可隐藏在深层部位。!@#!@而肿块也有良性和恶性之分，一般来说，良性肿瘤生长较慢，边界明显，活动性好；反之，恶性肿瘤生长快，不易推动。!@#!@如果是肿瘤，身体出现肿块的同时还会伴有痛感，且具有逐渐加重、夜间较白天严重的特征。!@#!@$#imgidx=0002#$!@#!@（2）溃疡!@#!@&nbsp;溃疡多是肿瘤组织坏死所形成的，呈菜花样或火山口状，不一定会有痛感，但有时会出现恶臭的血性分泌物。!@#!@（3）出血!@#!@出血多半是由于肿瘤侵犯到血管或肿瘤破裂所致。如果肿瘤在体表，出血很容易就能发现；如果肿瘤是在体内，出血可表现为其他形式，如便血、血性白带或呕血。!@#!@2、全身性症状!@#!@乏力和消瘦是由于肿瘤生长过快、消耗能量所致，此外，一些消化道肿瘤会引起消化道不适，从而使人消瘦。!@#!@肿瘤如果供血不足，或发生坏死，可引起发热。!@#!@$#imgidx=0003#$!@#!@3、皮肤症状!@#!@皮肤症状虽然能轻易察觉，但因皮肤病较常见，所以常被忽视。专家提醒，如果身上出现不明原因的斑块、疹子和瘙痒时，一定要提高警惕。!@#!@4、其他症状!@#!@比如指甲上出现圆点或裂纹，有可能是皮肤癌的表现。!@#!@还有一些症状跟肿瘤原发部位有关：如乳腺癌会出现乳头溢液或乳房颜色的改变；肺癌来临前会出现咳嗽，严重时可发生咳血；肝癌出现时会出现肝痛；鼻咽癌来临时会流鼻血；大肠癌来临时会出现便血；膀胱癌早期会出现血尿。!@#!@";
		// String text2 =
		// "生活早报║这鱼钓的！​鱼竿被鱼拽走，男子下河捞鱼竿，结果溺亡…!@#!@!@#!@!@#!@!@#!@今天是7月21日/星期五/农历六月廿八!@#!@!@#!@$#imgidx=0001#$!@#!@!@#!@今日生活女郎!@#!@姓名：胡敏!@#!@职业：教师!@#!@年龄：46岁!@#!@身高：1米65!@#!@星座： 射手座!@#!@爱好：书法、旗袍走秀、芭蕾!@#!@女郎征集令：474472616@qq.com!@#!@!@#!@天气预报!@#!@!@#!@$#imgidx=0002#$!@#!@!@#!@!@#!@$#imgidx=0003#$!@#!@!@#!@!@#!@!@#!@蜱虫又害人，致章丘多人死伤!@#!@仅仅因为一只蜱虫叮咬，济南市章丘区官庄镇西八井村68岁的村民刘女士住进了重症监护室，如今她已经昏迷十余天，医药费花了7万余元。据了解，刘女士7月1日下地干活回家后出现了头昏、高烧不退的症状，在被送至济南市传染病医院后发现左腿上趴着一只蜱虫，最终她被确诊为发热伴血小板减少综合征。7月20日下午，记者见到了刘女士的大儿媳妇于女士，她今年46岁，穿着一件黑色T恤，神情有些疲惫。“7月1日去山上了，结果回来之后就发烧。”于女士介绍，她婆婆下地干完农活回家后就出现了头晕、拉肚子以及发高烧的症状。!@#!@“我们村附近的山上有很多蜱虫，邻村一名60多岁的老人就因为蜱虫叮咬去世了。”章丘区官庄镇西八井村村民李先生介绍，以前他们村也有被蜱虫叮咬生病或死亡的案例，但是今年比较严重，除了刘女士被蜱虫叮咬后住进重症监护室，东八井村一名60多岁的老太太也被蜱虫叮咬，后来送至济南某医院抢救，但是抢救无效身亡。!@#!@!@#!@$#imgidx=0004#$!@#!@!@#!@!@#!@!@#!@!@#!@!@#!@俩飞车党3天疯抢了7条金项链!@#!@!@#!@$#imgidx=0005#$!@#!@!@#!@他们从四川流窜来济作案，目前警方已打掉这一飞车团伙今年5月，两名惯犯结伙从四川来到济南，他们骑着一辆摩托车，盯着受害人的金项链疯狂实施飞车抢夺，短短三天时间便连续作案八起，涉案价值高达20余万元。猖狂的是，他们竟然在两个小时内就作案三起。日前，经过一个月的艰苦奋战，天桥警方经过缜密侦查，最终抓获了5名涉案犯罪嫌疑人，打掉了这个飞车抢夺团伙。!@#!@来山东旅游老头把老伴丢了!@#!@近日，吉林一名六旬老汉来山东旅游，大巴车经过高速服务区休息时，他居然将老伴遗忘在服务区，青岛高速交警潍莱大队民警接到求助后，驱车往返40公里将老伴接回。!@#!@鱼竿被鱼拽走，下水捞鱼竿身亡 !@#!@20日6时45分，一名垂钓者在章丘区世纪大道绣源河钓鱼时不慎落水。济南消防明水中队赶赴现场救援，经过现场侦查得知：一名60岁左右的男子在垂钓途中鱼竿被鱼带走，男子随即下水捞鱼竿，由于体力不支沉入水中，水深约2米左右。经过紧张搜救，于7时34分成功将落水者救出，但男子已无生命迹象，随后将现场移交给120急救人员。!@#!@蔬菜进入“伏缺期”
		// 价格普涨!@#!@!@#!@$#imgidx=0006#$!@#!@!@#!@进入三伏天，连连上升的不只是气温，还有省城的蔬菜价格，随着伏天的到来，蔬菜供给步入“伏缺期”，市民的“菜篮子”又重了。据济南市商务局生活必需品监测数据显示，7月中旬涨幅前五名的蔬菜分别为：生菜2.75元/公斤，环比上涨26.15%；豆角2.95元/公斤，环比上涨20.41%；油菜2.05元/公斤，环比上涨10.81%；土豆1.55元/公斤，环比上涨9.15%；芹菜1.6元/公斤，环比上涨8.11%。!@#!@想让警察找手机，醉汉谎称被抢!@#!@!@#!@$#imgidx=0007#$!@#!@!@#!@男子在济南某工地打工，一晚上喝了十几杯扎啤，醉酒后将手机丢失，在寻找手机的途中又不慎掉沟里摔伤，随后男子报警，竟“自导自演”两次谎称自己被出租车司机殴打抢劫。目前，该男子因严重扰乱公共秩序，被市中警方行政拘留3日。!@#!@不文明养狗行为严重扰民!@#!@济南最近几天虽有下雨，但炎热的气温却没能缓解，不少市民会选择晚上出来散步纳凉。可居住在槐荫区顺安苑小区的居民，却有些提心吊胆，因为小区内不仅有多只流浪狗流窜，还有居民遛狗不拴狗绳，时刻威胁居民日常生活安全。20日上午，槐荫分局治安大队和兴福派出所来到顺安苑小区，对小区内的流浪狗进行了收容，并针对不文明养犬的行为进行了处理。!@#!@女子见网友被“骗财骗色”欲轻生!@#!@19日，一外地女子满心欢喜来济与男网友相聚，没想到被欺骗了感情还被盗走了钱财，女子一时心生绝望，欲跳桥轻生，幸好被市民发现并报了警。 获教后的女子自称姓刘，24岁，湖南人，四天前被网友骗来济南。后来趁她不备把她的证件和钱物偷走。目前，该案件正在进一步调查处理中。 !@#!@防治甲状腺结节，专家给您支招!@#!@本周六“生活大讲堂”健康讲座继续开讲，赶紧报名吧 !@#!@田斌，主任医师，留日学者。中华医学会山东省外科分会乳腺、
		// 甲状腺学组委员，山东省抗癌协会普外肿瘤委员会委员。济南市医学会乳腺甲状腺专业委员会副主任委员。 !@#!@报名电话：87976666 82921919
		// 85193404!@#!@济南东去机场小车限速80!@#!@20日，记者从齐鲁交通济南公司获悉，自2017年7月20日起，对济南G2001绕城高速公路济南东收费站到机场收费站部分路段进行封闭施工。施工时间：2017年7月20日9时至2017年8月30日12时；限速限行路段：济南G2001绕城高速公路济南东收费站到机场收费站（桩号K0+000~K19+000）。
		// 通行车辆根据限速限行规定驾驶，小型车辆限速80公里/小时、大型车辆限速70公里/小时。!@#!@高温天气将持续到周末!@#!@由于空气湿度大，本轮高温天改“烧烤”为“清蒸”模式，闷热难耐。此轮高温天还将持续3日，根据预报，24日开始有降雨，气温将下降。记者从济南市气象局获悉，截至20日下午16时，济南市区最高温度35.2℃，济阳的最高温达到了37℃。根据济南市气象局自动观测站数据，以泉城广场为例，下午16时，实时温度35.6℃，相对湿度48%，体感温度已经达到了40℃以上。
		// 山东省气象台20日11时也发布高温黄色预警信号，预计20日开始到23日，我省内陆大部地区最高温度在35℃以上，局部地区可达37℃左右，沿海地区最高温度33℃左右。!@#!@飞跃大道新建供热管网!@#!@20日，记者从济南热力集团有限公司了解到，历城区飞跃大道将于7月25日到9月11日组织实施供热管网新建工程，工期49天。管网建成后，附近“中建新悦城、万科幸福里”等小区今冬将加入集中供暖，新增供暖面积约80万平方米，还可保障此后新建小区的正常用热。!@#!@为百名贫困斜视弱视儿童点亮心灯!@#!@!@#!@$#imgidx=0008#$!@#!@!@#!@斜视和弱视是儿童常见的眼部疾病，弱视患病率为2%～4%，斜视的患病率为3%。虽然斜视和弱视治疗对孩子眼睛康复具有重大意义，但目前仍有一些贫困家庭患儿由于经济原因而得不到及时的诊治。为此，济南爱尔眼科医院联合湖南爱眼基金会开展”点亮心灯”项目，将为符合条件的山东省内100名贫困家庭斜、弱视患儿，提供部分斜视手术或弱视训练治疗费用资助。!@#!@!@#!@$#imgidx=0009#$!@#!@!@#!@36种高价药品纳入医保!@#!@本次纳入药品目录的36个药品中包括31个西药和5个中成药。西药中有15个是肿瘤治疗药，覆盖了肺癌、胃癌、乳腺癌、结直肠癌、淋巴瘤、骨髓瘤等癌种，包括曲妥珠单抗、利妥昔单抗、硼替佐米、来那度胺等，其他分别为治疗心血管病、肾病、眼病、精神病、抗感染、糖尿病等重大疾病或慢性病的药物，以及治疗血友病的重组人凝血因子Ⅶa和治疗多发性硬化症的重组人干扰素β-1b两种罕见病药。中成药中有3个是肿瘤药，另外2个是心脑血管用药。!@#!@上海摩拜单车“自燃”事件系恶意纵火 !@#!@!@#!@$#imgidx=0010#$!@#!@!@#!@18日，一则“上海莲花路地铁站南广场上摩拜单车轮胎发生自燃”的消息在网上引发市民关注，相关微博网友转发超千次，评论超850次。20日，记者从闵行警方获悉，该事件实为人为纵火，目前嫌疑人陶某已被警方控制。!@#!@借四千元4个月后欠贷30万遭逼债!@#!@!@#!@$#imgidx=0011#$!@#!@!@#!@为买一部Iphone手机，18岁的合肥女大学生方晴(化名)找借贷公司借了四千元高利贷，只还了两周就还不起了。在“知心姐姐”范某牵线下，她4个月间在省城十多家贷款公司，借高利贷超过了30万元。其间，为偿还巨债，方晴被范某介绍到夜总会做陪酒女，还被一家借贷公司拍了裸照。甚至，还被借贷公司追到老家讨债……7月18日，方晴和家人来到庐阳刑警部门报案。目前，庐阳警方已就此事介入调查。!@#!@看热闹4人从15米高高架桥跌落!@#!@7月18日23时左右，广珠西线中山市东升段往广州方向高速路高架桥处发生一宗四车连环追尾交通事故，造成高速路单向约10公里范围内大塞车，被堵车辆上等得不耐烦的乘客前往事故现场围观看热闹，结果有4人不慎失足从15米高的高架桥跌落。!@#!@男子死于私家车内或因开空调睡觉!@#!@7月17日中午11时左右，合肥市合作化路与清溪路交口附近一小区，一男子被发现死于车库内的私家车内。而事发时，该车四门紧闭，发动机未熄火，车载空调开了一夜。记者从辖区警方了解到，男子车内死亡初步排除案件可能，疑睡车上开空调时门窗紧闭无法通风，最终导致一氧化碳中毒死亡。!@#!@女出纳挪用９００余万买“六合彩”!@#!@!@#!@记者日前从深圳市宝安区人民法院获悉，深圳一女出纳挪用９００余万元公款买“六合彩”案尘埃落定，法院以挪用公款罪判处其有期徒刑１０年。据法院介绍，被告人叶晓珍２００２年３月在深圳市宝路华顺达交通服务有限公司观澜汽车站担任出纳，２０１４年６月被任命为观澜汽车站办公室副主任。叶晓珍担任出纳期间的主要职责是负责观澜汽车站的全部资金收入与支出。!@#!@考文德当选印度新总统系第二位“贱民”总统 !@#!@!@#!@$#imgidx=0012#$!@#!@!@#!@印度选举委员会20日公布的计票结果显示，全国民主联盟总统候选人拉姆·纳特·考文德当选印度新一任总统。目前，印度大约有2亿多“贱民”。他们是印度最为贫穷的阶层，缺乏教育和其他发展机会。1997年，纳拉亚南成功当选印度历史上第一位出身“贱民”阶层的总统。此次选举将诞生第二位出身“贱民”阶层的印度总统。!@#!@斧头砍出平头!@#!@!@#!@$#imgidx=0013#$!@#!@!@#!@据英国《每日邮报》7月19报道，一位“勇士”接受了一次特殊的理发服务，理发工具居然是斧子与锤头，理发师轻松应对，用斧头“砍”出平头，顾客全程淡定，画风清奇，却引发网友不满。更令人震惊的是，理发师全程边理发边用葡萄牙语对着相机说话，让人心里不禁捏了一把汗，但顾客倒是非常淡定，众多网友在看到视频后非常不淡定，纷纷表示太危险。!@#!@金毛奋不顾身救小鹿!@#!@!@#!@$#imgidx=0014#$!@#!@!@#!@据英国《每日邮报》7月18日报道，近日，美国纽约州一男子马克·弗里带着两只爱犬斯托姆(Storm)与莎拉(Sarah)在长岛海湾散步。期间，金毛猎犬托姆发现水里有东西一起一落，于是立刻跳进海里游了过去。托姆游回岸边时，嘴里竟叼了一头受伤的小鹿。!@#!@女模特机场拍摄，飞机身后起飞!@#!@!@#!@$#imgidx=0015#$!@#!@!@#!@据英国《每日邮报》7月17日报道，近日，9名女模特在印度西北部拉贾斯坦邦的一处飞机跑道上进行拍摄，期间一架飞机自她们身后起飞，并从几人头顶呼啸而过，场面十分危险。!@#!@熊坐摩托招摇过市!@#!@!@#!@$#imgidx=0016#$!@#!@!@#!@据英国《每日邮报》7月17日报道，近日，在俄罗斯联邦科米共和国境内，司机尼古拉斯·帕森科夫拍摄到了令人不可思议的一幕：在一条公共道路上，一头棕色巨熊无比淡定地坐在一辆摩托车的副驾上。只见它时不时地向空中伸出爪子，仿佛在享受这段路程。!@#!@美国８６岁珠宝大盗“阴沟里翻船”!@#!@一名臭名昭著的珠宝大盗本周在美国一家超市行窃时落网，此次所窃财物价值８６.２２美元。此人年近九旬，专盗珠宝６０多年，足迹踏遍美国、法国、摩纳哥、日本等，还曾被拍成纪录片。英国广播公司报道，这名女盗贼名为多丽丝·佩恩，现年８６岁。她最近承认偷盗一条价值２０００美元的钻石项链，被判软禁在家，不料１７日在佐治亚州亚特兰大市一家沃尔玛超市“顺走”８６.２２美元商品，再度落网。佩恩声称，她只是“忘记付钱”，随后获保释出狱。!@#!@世界第二大钻流拍或被切割!@#!@据外媒报道，世界第二大的钻石原石“Lesedi
		// La
		// Rona”为了寻找买家，可能将不得不被切割。这颗据报道!@#!@有“网球大小”的钻石，曾于去年夏天拍卖，但因最高竞拍价低于7000万美元的拍卖底价而流拍。据悉，该钻石属于加拿大卢卡拉钻石公司，开采自南非的茨瓦纳，重1109克拉，已有25亿到30亿年的历史，其名字在茨瓦纳语中的意思是“我们的光芒”。!@#!@涉性侵１１名儿童，墨西哥女幼师被捕!@#!@墨西哥哈利斯科州检察长爱德华多·阿尔马格１８日说，因涉嫌性侵１１名儿童、导致孩子身心严重受创，一名幼儿园女教师当天被捕。阿尔马格在新闻发布会上说，当地警方在１８日凌晨逮捕现年２９岁的嫌疑人安娜·Ｎ。她是查帕拉市一所幼儿园的教师，受害者都是学生。根据证词，这名女教师“脱下受害者的衣服并侵害他们的身体”。!@#!@ＣＢＡ公司完成改选，姚明当选董事长!@#!@!@#!@$#imgidx=0017#$!@#!@!@#!@中篮联（北京）体育有限公司（简称“ＣＢＡ公司”）１９日、２０日在广东东莞召开了公司第一届第二次股东大会及第三次董事会、监事会会议。会议通过了《公司章程》及《章程修正案》，并完成了董事会、监事会改选。中国篮协主席姚明将出任董事长。!@#!@跳水——世锦赛：女子10米台决赛赛况!@#!@!@#!@$#imgidx=0018#$!@#!@!@#!@7月19日，中国选手任茜在比赛中。当日，在匈牙利布达佩斯进行的第17届国际泳联游泳世锦赛跳水项目女子10米台决赛中，马来西亚选手张俊虹以397.50分夺冠，中国选手司雅杰和任茜分获亚军和季军。!@#!@宋仲基为宋慧乔拒绝日本车广告鼓掌!@#!@!@#!@$#imgidx=0019#$!@#!@!@#!@19日下午，电影《军舰岛》媒体试映会在首尔龙山CGV举行，当被问到宋慧乔曾拒拍日本某品牌汽车广告一事时，宋仲基表示：“当时是通过新闻知道的，看到新闻后心为她鼓掌。这个广告如果让我拍我也会拒绝。慧乔现在成了我爱的人，我觉得她真的做得很好。”!@#!@王上源爆粗口“你们侮辱了下水道”!@#!@!@#!@$#imgidx=0020#$!@#!@!@#!@腾讯体育7月20日
		// 昨晚足协杯8强战首回合，恒大客场2-4不敌同城对手富力。首发边后卫王上源的表现遭遇球迷批评。而今天王上源也在社交平台上表示：那些恶毒攻击自己以及自己家人的网友，他们生长在黑暗肮脏的下水道，喷子只要不喷死自己，总有一天会被自己打脸。!@#!@电影《建军大业》１９日首映!@#!@!@#!@$#imgidx=0021#$!@#!@!@#!@热血战争巨制《建军大业》１９日在南昌举行发布会，总策划兼艺术总监韩三平、导演刘伟强携演员刘烨等亮相。据悉，当晚将举行电影的首映礼。《建军大业》是“建国三部曲”系列的第三部，献礼今年的建军９０周年。影片讲述了１９２７年第一次国内革命战争失败后，中国共产党为挽救革命，于当年８月１日在江西南昌举行武装起义，从而创建中国共产党领导的人民军队的故事。该片由刘伟强执导，韩三平担任总策划及艺术总监，黄建新监制。!@#!@《建军大业》将于７月２８日在中国内地公映，８月３日在中国香港及澳门上映!@#!@!@#!@$#imgidx=0022#$!@#!@!@#!@饭后适宜吃的健康零食!@#!@1、葵瓜子-养颜；2、花生-防皮肤病；3、核桃-秀甲；4、大枣-防坏血病；5、奶酪固齿；6、无花果-促进血液循环；7、南瓜子和开心果；8、奶糖-补充大脑能量；9、巧克力-心情愉悦及美容；10、芝麻糊-乌发润发养血；11、葡萄干-益气补血悦颜；12、薄荷糖-润喉、除口臭散火气；13、柑桔、橙子、苹果等；14、牛肉干、烤鱼片；15乳饮料。!@#!@交通银行“沃德杯”!@#!@广场舞大赛火爆报名!@#!@冠军队奖金100万!@#!@!@#!@$#imgidx=0023#$!@#!@!@#!@销售冠军甲子山绿茶又回来了!@#!@地址：老济南特产店（经四纬二大观园路口往北50米路西）!@#!@电话：82070027 !@#!@!@#!@$#imgidx=0024#$!@#!@!@#!@欢迎扫码加入!@#!@生活日报官微群!@#!@发现更多精彩内容!@#!@编辑!@#!@李云霞!@#!@";
		// Simhash sim = new Simhash(tokenizerFactory);
		// System.out.println(utils.out(sim.simhash64(RegexUtils.cleanParaAndImgLabel(text1))));
		// System.out.println(utils.out(sim.simhash64(RegexUtils.cleanParaAndImgLabel(text2))));
		// utils.add("1", RegexUtils.cleanParaAndImgLabel(text1));
		// System.out.println(utils.select(RegexUtils.cleanParaAndImgLabel(text2)));
		File[] files = new File("D:\\corpus\\duplicate\\duplicate_raws_version0").listFiles();
		InputStream in = null;
		BufferedReader br = null;
		int i = 0;
		long s = System.currentTimeMillis();
		for (File file : files) {
			if (!file.getName().equals("xaa")) {
				continue;
			}
			try {
				in = new FileInputStream(file);
				br = new BufferedReader(new InputStreamReader(in));
				String line;
				line = br.readLine();
				String[] splits = null;
				while (line != null) {
					splits = line.split("\t+");
					if (splits.length != 4) {
						line = br.readLine();
						continue;
					}
					String rawKey = splits[1];
					String title = splits[2];
					String raw = RegexUtils.cleanParaAndImgLabel(splits[3]);
					// long a = System.currentTimeMillis();
					utils.add(rawKey + ":" + title, title + raw);
					// System.out.println(System.currentTimeMillis() - a);
					line = br.readLine();
					if (i % 1000 == 0) {
						System.out.println(i);
						long e = System.currentTimeMillis();
						System.out.println(e - s);
						s = System.currentTimeMillis();

					}
					i++;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}

		try {
			in = new FileInputStream(new File("D:\\corpus\\duplicate\\duplicate_raws_version0\\xaa"));
			br = new BufferedReader(new InputStreamReader(in));
			String line;
			line = br.readLine();
			String[] splits = null;
			long sum = 0;
			int c = 0;
			while (line != null) {
				splits = line.split("\t+");
				if (splits.length != 4) {
					line = br.readLine();
					continue;
				}
				String title = splits[2];
				String raw = RegexUtils.cleanParaAndImgLabel(splits[3]);
				long st = System.currentTimeMillis();
				Set<String> list = utils.select(title + raw);
				long et = System.currentTimeMillis() - st;
				sum += et;
				if (c % 1000 == 0) {
					System.out.println(sum * 1.0 / c);
				}
				// if (list.size() == 1) {
				// line = br.readLine();
				// continue;
				// }
				// FileUtils.write(new File("D://simhashResult"), String.format(">>>>>>%s:%s\n",
				// splits[1], title),
				// "utf-8", true);
				// for (String url : list) {
				// if (url.equals(splits[1] + ":" + title)) {
				// continue;
				// }
				// FileUtils.write(new File("D://simhashResult"), String.format("%s\n", url),
				// "utf-8", true);
				// }
				// FileUtils.write(new File("D://simhashResult"), "*************************\n",
				// "utf-8", true);
				line = br.readLine();
				c++;
			}

			System.out.println(sum * 1.0 / c);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
