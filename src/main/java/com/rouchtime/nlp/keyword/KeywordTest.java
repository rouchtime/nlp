package com.rouchtime.nlp.keyword;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.ansj.recognition.impl.NatureRecognition;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.commons.io.FileUtils;

import com.aliasi.tokenizer.TokenizerFactory;
import com.hankcs.hanlp.HanLP;
import com.rouchtime.nlp.keyword.extraction.BeforeTfIdfKeyWordExtraction;
import com.rouchtime.nlp.keyword.extraction.IntegrateKeyWordExtraction;
import com.rouchtime.nlp.keyword.extraction.TextRankWithMultiWinExtraction;
import com.rouchtime.nlp.keyword.similarity.Word2VectorWordSimiarity;
import com.rouchtime.util.RegexUtils;

import tokenizer.AnsjNlpTokenizerFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

public class KeywordTest {

	static TokenizerFactory tokenFactory = getTokenFactory();

	private static TokenizerFactory getTokenFactory() {
		StopWordTokenierFactory stopWordFactory = new StopWordTokenierFactory(AnsjNlpTokenizerFactory.getIstance());
		StopNatureTokenizerFactory stopNatureTokenizerFactory = new StopNatureTokenizerFactory(stopWordFactory);
		return stopNatureTokenizerFactory;
	}

	public static void main(String[] args) throws IOException {
		// for(String line :FileUtils.readLines(new
		// File("D:\\corpus\\test\\stopwords.txt"), "utf-8") ) {
		// if(line.matches("[\\u4e00-\\u9fa5]+")) {
		// FileUtils.write(new File("D:\\corpus\\test\\chinese.txt"),
		// line+"\n","utf-8",true);
		// } else {
		// FileUtils.write(new File("D:\\corpus\\test\\nonChinese.txt"),
		// line+"\n","utf-8",true);
		// }
		// }

		// for (String line : FileUtils.readLines(new
		// File("D:\\corpus\\keyword\\test.txt"), "utf-8")) {
		// String title = line.split("\t+")[1];
		// String raw = RegexUtils.cleanImgLabel(line.split("\t+")[2]);
		// IntegrateKeyWordExtraction keywordExtraction = (IntegrateKeyWordExtraction)
		// new IntegrateKeyWordExtraction(
		// tokenFactory,
		// Word2VectorWordSimiarity.getInstance()).enableWordAssemble(true);
		// String integrateCombine = keywordExtraction.keywordsExtract(title, raw,
		// 20).toString();
		// keywordExtraction.enableWordAssemble(false);
		// String integrateUncombine = keywordExtraction.keywordsExtract(title, raw,
		// 20).toString();
		//
		// BeforeTfIdfKeyWordExtraction beforeTFidf = new
		// BeforeTfIdfKeyWordExtraction(tokenFactory);
		// String tfidf = beforeTFidf.keywordsExtract(title, raw, 20).toString();
		//
		// TextRankWithMultiWinExtraction textRankMulti = new
		// TextRankWithMultiWinExtraction(2, 10, tokenFactory);
		// String textrankMult = textRankMulti.keywordsExtract(title, raw,
		// 20).toString();
		// FileUtils.write(new File("D://corpus//test//result_compare"),
		// String.format("%s\t\n组合：%s\n非组合：%s\n多窗口Textrank：%s\n线上的TFIDF：%s\n",line.split("\t+")[0],
		// integrateCombine,integrateUncombine,textrankMult,tfidf), "utf-8", true);
		// }

		IntegrateKeyWordExtraction keywordExtraction = (IntegrateKeyWordExtraction) new IntegrateKeyWordExtraction(
				tokenFactory, Word2VectorWordSimiarity.getInstance()).enableWordAssemble(true);
		String integrateCombine = keywordExtraction.keywordsExtract("习近平抵达河内开始对越南社会主义共和国进行国事访问", "11月12日，中共中央总书记、国家主席习近平抵达河内，开始对越南社会主义共和国进行国事访问。越共中央政治局委员、中央书记处常务书记、中央检查委员会主任陈国旺等到舷梯旁迎接。新华社记者 兰红光 摄新华社河内11月12日电（记者陶军　侯丽军）中共中央总书记、国家主席习近平12日抵达河内，开始对越南社会主义共和国进行国事访问。当地时间上午11时10分许，习近平乘坐的专机抵达河内内排国际机场。习近平步出舱门，越共中央政治局委员、中央书记处常务书记、中央检查委员会主任陈国旺等在舷梯旁迎接。越南青年向习近平献上鲜花。礼兵分列红地毯两侧。身着盛装的越南群众挥舞着中越两国国旗欢迎习近平。丁薛祥、刘鹤、杨洁篪等陪同人员同机抵达。习近平是在岘港出席亚太经合组织第二十五次领导人非正式会议后抵达河内的。离开岘港时，越南政府部长等高级官员、香港特别行政区行政长官林郑月娥到机场送行。", 20).toString();
		System.out.println(integrateCombine);
		keywordExtraction.enableWordAssemble(false);
		integrateCombine = keywordExtraction.keywordsExtract("习近平抵达河内开始对越南社会主义共和国进行国事访问", "11月12日，中共中央总书记、国家主席习近平抵达河内，开始对越南社会主义共和国进行国事访问。越共中央政治局委员、中央书记处常务书记、中央检查委员会主任陈国旺等到舷梯旁迎接。新华社记者 兰红光 摄新华社河内11月12日电（记者陶军　侯丽军）中共中央总书记、国家主席习近平12日抵达河内，开始对越南社会主义共和国进行国事访问。当地时间上午11时10分许，习近平乘坐的专机抵达河内内排国际机场。习近平步出舱门，越共中央政治局委员、中央书记处常务书记、中央检查委员会主任陈国旺等在舷梯旁迎接。越南青年向习近平献上鲜花。礼兵分列红地毯两侧。身着盛装的越南群众挥舞着中越两国国旗欢迎习近平。丁薛祥、刘鹤、杨洁篪等陪同人员同机抵达。习近平是在岘港出席亚太经合组织第二十五次领导人非正式会议后抵达河内的。离开岘港时，越南政府部长等高级官员、香港特别行政区行政长官林郑月娥到机场送行。", 20).toString();
		System.out.println(integrateCombine);
	}
}
