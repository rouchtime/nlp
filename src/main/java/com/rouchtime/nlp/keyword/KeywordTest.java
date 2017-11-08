package com.rouchtime.nlp.keyword;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.aliasi.tokenizer.TokenizerFactory;
import com.hankcs.hanlp.HanLP;
import com.rouchtime.nlp.keyword.extraction.IntegrateKeyWordExtraction;
import com.rouchtime.nlp.keyword.extraction.TextRankWithMultiWinExtraction;
import com.rouchtime.nlp.keyword.extraction.TfIdfKeyWordExtraction;
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
		// HashMap<String,Double> map = new HashMap<String,Double>();
		// map.put("all", 1.0);
		// System.out.println(map.get("all"));
		// System.out.println("all".hashCode());
		// CiLinWordSimiarity cilin = CiLinWordSimiarity.getInstance();
		// System.out.println(cilin.calcWordsSimilarity("家伙", "东西"));
		// TfIdf tfidf = TfIdf.getInstance();
		// LexicalChain chain = new
		// LexicalChain(0.6,Word2VectorWordSimiarity.getInstance());
		for(String line : FileUtils.readLines(new File("D:\\corpus\\keyword\\test.txt"), "utf-8")) {
			String title = line.split("\t+")[1];
			String raw = RegexUtils.cleanParaAndImgLabel(line.split("\t+")[2]);
			IntegrateKeyWordExtraction keywordExtraction = (IntegrateKeyWordExtraction) new IntegrateKeyWordExtraction(tokenFactory, Word2VectorWordSimiarity.getInstance()).enableMultithreading(true);
			FileUtils.write(new File("D://corpus//test//keyword_result"), line.split("\t")[0] + "\t" + keywordExtraction.keywordsExtract(title, raw, 50).toString()+"\n","utf-8",true);
		}
//		String title = "19岁嫁了个可以做她爹的老公，如今36岁把一家五口打理得井井有条";
//		String text = "在娱乐圈老夫少妻似乎已经是成了一个见怪不怪的话题了，而有的人在红了以后，就开始变得浮躁了。有的人爆红后，依然能够不忘初心。而今天要说的这个，是娱乐圈的大名人，就连巩俐都和他相爱过，他就是张艺谋。" + 
//				"" + 
//				"说到张艺谋，大家就想得起他是一个大导演，执导了很多大戏。每次在网络上看到“经典电影”这个字眼，脑中立刻浮现的是张艺谋在1994年拍摄的《活着》。" + 
//				"" + 
//				"而当年和巩俐之间轰轰烈烈的爱情，最后却没有在一起。之后张艺谋认识了陈婷，而陈婷这么受关注的原因，其实不止是因为她是张艺谋的妻子，而且还因为她年纪小张艺谋足足31岁。" + 
//				"" + 
//				"不过，陈婷嫁给张艺谋后，不仅多才多艺，还给张艺谋生了3个孩子，可以说是一个很不错的贤内助。而在前段时间张艺谋小女儿张壹娇画一幅画，引起了众多热议，小女儿长得是最像老谋子的，又因为家中最小，所以最受张艺谋宠爱。" + 
//				"" + 
//				"在小女儿上面，还有两个哥哥。大儿子张壹男、二儿子张壹丁和小女儿张壹娇，分别出生于2001年、2004年和2006年。" + 
//				"" + 
//				"看着张艺谋家的全家福，总能想到巩俐，陪伴张艺谋那么多年，一直想得到终身的承诺，结果含泪而去，以至于灰心嫁人，如今50岁了还只身一人，无儿无女。" + 
//				"" + 
//				"也不得不说，或许世间缘分就是这样，巩俐太有感染力了，而张艺谋要的是一个家的感觉，即使现在老来生子，但起码一家5口很是幸福。" + 
//				"" + 
//				"每次过节日的时候，一家人就会聚在一起。" + 
//				"" + 
//				"如今36岁的陈婷把3个孩子养得很好，相信这也是让张艺谋能专心在外赚钱的原因。";
//		text = RegexUtils.cleanParaAndImgLabel(text);
		
		// System.out.println(HanLP.extractKeyword(text, 100));
		// ObjectToDoubleMap<String> tfMap = new ObjectToDoubleMap<>();
		// TextRankWithMultiWin rankMult = new TextRankWithMultiWin();
		// List<String> listToken = new ArrayList<String>();
//		for (Term term : segment.seg("硬盘，内存")) {
//			System.out.println(term);
//		}
		// ObjectToDoubleMap<String> mergeTFMap =
		// SynonymMerge.mergeTFBySynonym(tfMap,Word2VectorWordSimiarity.getInstance());
		//
		// rankMult.setKeywordNumber(100);
		// System.out.println(mergeTFMap.scoredObjectsOrderedByValueList());
		// System.out.println(rankMult.integrateMultiWindow(listToken, 2, 10));
		// int i = 0;
		// TextRank rank = new TextRank();
		// TextRankWithMultiWin rankMult = new TextRankWithMultiWin();
		// List<String> listToken = new ArrayList<String>();
//		IntegrateKeyWordExtraction keywordExtraction = new IntegrateKeyWordExtraction(tokenFactory, Word2VectorWordSimiarity.getInstance());
//		TfIdfKeyWordExtraction tfidfKeyWordExtraction = new TfIdfKeyWordExtraction(tokenFactory);
//		TextRankWithMultiWinExtraction textRankWithMultiWinExtraction = new TextRankWithMultiWinExtraction(2,10,tokenFactory);
//		System.out.println(keywordExtraction.keywordsExtract(title, text, 50));
//		System.out.println(tfidfKeyWordExtraction.keywordsExtract(title, text, 50));
//		System.out.println(textRankWithMultiWinExtraction.keywordsExtract(title, text, 50));
//		System.out.println(HanLP.extractKeyword(title + "," + text, 50));
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
