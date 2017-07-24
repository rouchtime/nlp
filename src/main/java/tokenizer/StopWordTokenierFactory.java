package tokenizer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.aliasi.classify.TfIdfClassifierTrainer;
import com.aliasi.tokenizer.ModifyTokenTokenizerFactory;
import com.aliasi.tokenizer.TokenFeatureExtractor;
import com.aliasi.tokenizer.TokenizerFactory;

/**
 * 自定义停用词分词器
 * @author 龚帅宾
 *
 */
public class StopWordTokenierFactory extends ModifyTokenTokenizerFactory implements Serializable {
	private static final long serialVersionUID = -1312129063609071054L;

	private final Set<String> mStopSet;

	public StopWordTokenierFactory(TokenizerFactory factory, Set<String> stopSet) {
		super(factory);
		mStopSet = new HashSet<String>(stopSet);
	}

	/**
	 * 默认用系统自带的停用词
	 * @param factory
	 */
	public StopWordTokenierFactory(TokenizerFactory factory) {
		super(factory);
		InputStream is = getClass().getResourceAsStream("/stopwords.txt");
		mStopSet = readFromFileNames(is);
	}

	public Set<String> stopSet() {
		return Collections.unmodifiableSet(mStopSet);
	}

	@Override
	public String modifyToken(String token) {
		String[] term = token.split("/");
		if (term.length != 2) {
			return null;
		}
		return mStopSet.contains(term[0]) ? null : token;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "\n  stop set=" + mStopSet + "\n  base factory=\n    "
				+ baseTokenizerFactory().toString().replace("\n", "\n    ");
	}

	public Set<String> readFromFileNames(InputStream is) {
		BufferedReader br = null;
		Set<String> set = new HashSet<String>();
		try {
			br = new BufferedReader(new InputStreamReader(is, "utf-8"));
			String s = null;
			while ((s = br.readLine()) != null) {
				set.add(s.trim());
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
			}
		}
		return set;
	}
	public static void main(String[] args) {
		HanLPTokenizerFactory factory = HanLPTokenizerFactory.getIstance();
		@SuppressWarnings("unused")
		StopWordTokenierFactory stopFactory = new StopWordTokenierFactory(factory);
		String text = "　　北京时间周四（7月6日）凌晨两点，美联储如约公布6月会议纪要，由此打响超级周第一枪。纪要显示，美联储决策者对于通胀前景以及其对未来加息步伐的影响的分歧日益加大。会议纪要公布之后，美元先抑后扬，现货黄金和美股震荡下滑。美联储在6月会议上进行了今年的第二次加息。纪要显示，几位委员希望在8月底之前就宣布将开始缩减资产负债表规模，其他决策者则希望等到今年晚些时候。　　美联储主席耶伦在会后的记者会上称，近期通胀下滑是暂时性的，美联储维持今年再加息一次以及明年加息三次的预估。不过在那之后，部分决策者已经表露出对美联储难以将通胀带回到2%目标的担忧加重。对何时开始缩减规模达到4.2万亿美元的国债和MBS组合，以及这对未来升息决定可能有何影响等问题，也引发了美联储内部的热议。在6月会议上，美联储阐明了今年开始缩表的计划概要，但没有给出具体时间表。缩减资产负债表是美联储货币政策正常化进程的最后一章。　　现货黄金先抑后扬再次上演过山车走势，日线上录得一根长下引线十字星，金价连续试探下方1218-1216支撑位，距离近3个月低位1213仅仅一步之遥，若跌破恐千二关口不保，上方至周一的大跌过后，1230成为多头反攻的拦路虎，迟迟无法上破，两次试探均已失败告终，随着非农序幕的渐渐拉开，震荡区间打开是必然之事，目前只需耐心等待，操作上关注1218-1228区间，高空低多即可。美盘及时建议另行通知。行情瞬息万变，现货黄金部分做单策略：　　1228-1230一线空单，止损3美金，目标1222-1220　　1218-1220一线多，止损3个点，目标1226-1228　　油价在最近八个交易日连续上涨，累计涨幅近11%，创下2012年以来最长涨势。路透石油研究数据显示，尽管OPEC产油国实施180万桶/日的减产协议，以平衡全球供应过剩，但6月OPCE石油出口连续第二个月增长。6月OPEC石油出口量增至2592万桶/日，较5月多出45万桶/日，较去年6月增加190万桶/日。上周油轮数据公司ClipperData提供的数据显示，沙特和其他OPEC产油国6月份石油出口呈现上升态势。　　沙特国家石油公司沙特阿美称，该公司将下调对亚洲客户的轻质原油价格，显示产油国为了争夺客户而不惜通过降价来提高竞争力。产油国下调原油售价说明减产机制并不能提振油价。另外，最大的非OPEC产油国俄罗斯表态不支持OPEC进一步加大减产力度令市场承压。　　不可一世的现货原油在经历一波连续上涨过后，隔夜一根大阴线彻底消灭多头的嚣张气焰，主图上来看boll走平运行中，盘中一度击穿boll中轨支撑，API利好刺激收复部分跌幅，目前处于MA30-46美元关口下整理，MA均线金叉上行后渐渐趋于平行，附图sto高位死叉下行，操作上保持高空为主。国际原油部分做单点位：　　45.8-46做空，止损0.3，目标45-44.5";
		for(String word : stopFactory.tokenizer(text.toCharArray(), 0, text.length())) {
			System.out.println(word);
		}
	}
}
