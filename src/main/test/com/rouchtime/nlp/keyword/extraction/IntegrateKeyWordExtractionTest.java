package com.rouchtime.nlp.keyword.extraction;

import static org.junit.Assert.*;

import org.junit.Test;

import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.rouchtime.util.RegexUtils;

import tokenizer.AnsjNlpTokenizerFactory;
import tokenizer.AnsjTokenizerFactory;

public class IntegrateKeyWordExtractionTest {
	
	@Test
	public void test() {
		String txt = "http://mini.eastday.com/mobile/171130094811386.html	彭银霸金:11.30黄金原油月末收官大获全胜，后市分析	时间过的很快，行情走的很好，有人又在后悔前面的单边行情自己不敢操作而错过了行情，偶尔自己敢做单了也基本上是亏多赚少。但是现在后悔也太晚了点。奉劝大家不要在别人贪婪时恐惧，在别人恐惧时贪婪，这句话虽然与股神巴菲特所说的有所不同，但是所要表达的意思是一样的。别人都在赚钱的时候，你因为恐惧而不敢操作，别人都在谨慎的对待行情的时候，你却在市场在横冲直撞如同一只无头苍蝇。亏损之后就开始怀疑是不是市场故意与你做对，其实不然，天地不仁以万物为刍狗，市场对待每一位投资者都是公平的，如果你在市场中严格遵守交易纪律，好好的运用投资技巧，合理的控制风险，那么市场回报你的自然是可观的利润。 !@#!@今天黄金原油有人追了空，也应该有人做了多，做了多单的在如此大好阶梯式下跌下已经是被深套出不来了，而做了空单的现在已是微微一笑了！我隔日的操作思路在很早就给了——《彭银霸金:11.29黄金临近千三看回调，原油晚间EIA高空为主》，跟上的我祝贺你；原油于58-57.8附近做了空单目前原油最低跌到56.5一线，我们的空单在虽然在57止盈离场，但也赚了一波！黄金在1295附近果断出击，顺利止盈于1283，也有近12美金的利润；很多人问我行情怎么看，我直接给出黄金原油空单策略，现在事实证明我的判断是正确的！ !@#!@$#imgidx=0001#$!@#!@套单策略  !@#!@由于每个平台点位不同，你的套单点位我也不清楚。所以需要我帮助的就直接带持仓截图来咨询本人。我会尽量帮你挽回损失。而若是有投资者单子不在的，也是说只能帮你找到亏损原因，也愿意帮你纠正。而后尽力给你回本的操作策略。因为亏损已经发生了，我希望我们可以在之后去踩着这个错误活得更光辉。而不是掉在里面就出不来了。因为市场是赚钱的，也有很多投资者是赚钱的，所以你要相信，只要你改正操作错误，按照专业的分析策略来操作，就定能挽回之前的亏损。!@#!@本文由彭银霸金分析师团队独家策划，转载请注明出处。以上内容供参考。投资有风险，入市需谨慎。!@#!@	caijing";
		String[] splits = txt.split("\t");
		String url = splits[0];
		String title = splits[1];
		String raw = RegexUtils.cleanImgLabel(splits[2]);
		String type = splits[3];
//		IntegrateKeyWordExtraction keywordExtraction = IntegrateKeyWordExtraction.getInstance();
//		System.out.println(keywordExtraction.keywordsExtract(title, raw, 10));
		for(String term : AnsjNlpTokenizerFactory.getIstance().tokenizer(raw.toCharArray(), 0, raw.length())) {
			System.out.print(term + " ");
		}
	}

}
