package com.rouchtime.nlp.lda;

import com.aliasi.tokenizer.*;
import com.aliasi.symbol.*;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Strings;
import com.rouchtime.util.RegexUtils;

import tokenizer.AnsjNlpSelfDicTokenzierFactory;
import tokenizer.AnsjNlpTokenizerFactory;
import tokenizer.RegexStopTokenzierFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;
import java.io.*;
import java.util.*;

// ftp://ftp.wormbase.org/pub/wormbase/misc/literature/2007-12-01-wormbase-literature.endnote.gz

public class LdaWormbase {

	public static void main(String[] args) throws Exception {
		File corpusFile = new File("D:\\corpus\\keyword\\tb_raw20171130\\tb_raw20171130.txt");
		int minTokenCount = 5;
		short numTopics = 50;
		double topicPrior = 0.1;
		double wordPrior = 0.01;
		int burninEpochs = 0;
		int sampleLag = 1;
		int numSamples = 2000;
		long randomSeed = 6474835;

		System.out.println("Citation file=" + corpusFile);
		System.out.println("Minimum token count=" + minTokenCount);
		System.out.println("Number of topics=" + numTopics);
		System.out.println("Topic prior in docs=" + topicPrior);
		System.out.println("Word prior in topics=" + wordPrior);
		System.out.println("Burnin epochs=" + burninEpochs);
		System.out.println("Sample lag=" + sampleLag);
		System.out.println("Number of samples=" + numSamples);

		CharSequence[] articleTexts = readCorpus(corpusFile);

		// reportCorpus(articleTexts);

		SymbolTable symbolTable = new MapSymbolTable();
		int[][] docTokens = tokenizeDocuments(articleTexts, ANSJTOKENZIERFACTORY, symbolTable, minTokenCount);

		// System.out.println("Number of unique words above count threshold=" +
		// symbolTable.numSymbols());
		//
		// // int numTokens = 0;
		// // for (int[] tokens : docTokens)
		// // numTokens += tokens.length;
		// // System.out.println("Tokenized. #Tokens After Pruning=" + numTokens);
		// //
//		 LdaReportingHandler handler = new LdaReportingHandler(symbolTable);
//		 
//		 LatentDirichletAllocation.GibbsSample sample =
//		 LatentDirichletAllocation.gibbsSampler(docTokens, numTopics,
//		 topicPrior, wordPrior, burninEpochs, sampleLag, numSamples, new
//		 Random(randomSeed), handler);
//		 FileOutputStream fos = new FileOutputStream(new File("D://sample"));
//		 ObjectOutputStream oos = new ObjectOutputStream(fos);
//		 oos.writeObject(sample);

//		 ObjectInputStream oi = new ObjectInputStream(new FileInputStream(new
//		 File("D://sample")));
//		 LatentDirichletAllocation lda = (LatentDirichletAllocation) oi.readObject();
//		 int maxWordsPerTopic = 200;
//		 int maxTopicsPerDoc = 10;
//		 boolean reportTokens = true;
//		 handler.fullReport(sample, maxWordsPerTopic, maxTopicsPerDoc, reportTokens);

		ObjectInputStream oi = new ObjectInputStream(new FileInputStream(new File("D://sample")));
		LatentDirichletAllocation.GibbsSample readSample = (LatentDirichletAllocation.GibbsSample) oi.readObject();
		LatentDirichletAllocation lda = readSample.lda();
		String text = "平安证券：云南白药买入评级	 　　!@#!@$#imgidx=0001#$!@#!@ 　　投资要点!@#!@ 　　事项：11 月27日，公司审议通过《关于公司高级管理人员薪酬管理与考核办法的议案》以及《关于公司独立董事津贴管理办法的议案》，独立董事津贴为每人每年人民币21.6万元(税后) .!@#!@ 　　平安观点：!@#!@ 　　混改后首启高管薪酬调整，更加市场化的管理层激励渐行渐近。1、管理层现有激励与其优秀的管理能力不匹配。在以董事长王明辉核心的管理团队带领下，公司营业收入从2006年的32亿元增长到2016年的224亿元(CAGR达21%)，净利润从2006年的2.7亿元增长到2016年的29亿元(CAGR达27%)，公司市值从2006年的120亿元攀升至2017年过1000亿元(CAGR达21%)，是医药股的常青树。但公司高管总薪酬从2006年的237万元增长到2016年774万元(CAGR仅13%)，2016年高管总薪酬/净利润(扣非归母净利润)仅为0.2g%，在中药龙头企业中处于下游，远低于1%的平均水平。2、公司高管薪酬与考核办法延用2002年至今，此次高管薪酬调整是混改之后首次启动，预计包括现任8名高管，各人涨幅不一，总体上从现有四分之一位涨到市场平均水平，更加市场化的管理层激励渐行渐近。!@#!@ 　　预计高管考核也做相应向上调整，业绩提速值得期待。1、尽管公司管理层激励不够充分到位，但过去10年公司营业收入、净利润增速基本与高管薪酬变动趋势整体保持一致。自2009年公司高管薪酬大幅上调6g%之后，带动营业收入、净利润双双显着增长，分别从2009年的25%、30%提速到2010年的40%、53%。因此与此次高管薪酬调整配套，预计高管考核也做相应向上调整，业绩提速值得期待。2、高管薪酬调整也有利于提高盈利水平。对比前10大市值中药公司，以及公司历年高管薪酬/净利润和ROE，也存在一定正相关关系。自2009年公司高管薪酬大幅上调之后，ROE也随之显着回升。因此预计高管考核相应向上调整之后，盈利水平也有望进一步提升。!@#!@ 　　投资建议：第一步混改已落地，预计国改将进一步推进释放活力，优秀的管理团队将带领品牌中药龙头再次腾飞。以“药”为本，打造药品、健康产品、中药资源的品牌集群，实现从“产品经营”向“产业经营”的跨越，持续推进“新白药、大健康”战略。我们维持2017-19年EPS预测分别为3.26/3.78/4.40元，内生复合增长约16%，对应PE30/25/22X，处于历史低位，应享受估值溢价，维持“强烈推荐”的评级。!@#!@ 　　风险提示：国企改革推进低于预期，海外业务扩展不及预期。";
//		String title = text.split("\t")[0];
		String article = RegexUtils.cleanSpecialWord(RegexUtils.cleanParaAndImgLabel(text));
		int[] tokens = tokenizeDocument(article.subSequence(0, article.length()), ANSJTOKENZIERFACTORY, symbolTable);
		double[] sampleTopics = lda.bayesTopicEstimate(tokens, numSamples, burninEpochs, sampleLag,
				new Random(randomSeed));
		ObjectToDoubleMap<String> scoreMap = new ObjectToDoubleMap<>();
		for (int token : tokens) {
			double numerator = 0.0;
			// for (int topicId = 0; topicId < lda.numTopics(); topicId++) {
			// double v = lda.wordProbability(topicId, token) *
			// readSample.topicCount(topicId) / readSample.wordCount(token);
			// numerator += v * sampleTopics[topicId];
			// }
			double sum = 0.0;
			for (int topicId = 0; topicId < lda.numTopics(); topicId++) {
				sum += readSample.topicWordCount(topicId, token);
			}
			 for (int topicId = 0; topicId < lda.numTopics(); topicId++) {
			 double v = ((readSample.topicWordCount(topicId, token) + readSample.topicWordPrior())/(sum + readSample.numTopics() * readSample.topicWordPrior()));
			 numerator += v * sampleTopics[topicId];
			 }
			double x = 0.0;
			for (int topicId = 0; topicId < lda.numTopics(); topicId++) {
				 double v = ((readSample.topicWordCount(topicId, token) + readSample.topicWordPrior())/(sum + readSample.numTopics() * readSample.topicWordPrior()));
				x += (v * v);
			}
			double y = 0.0;
			for (int topicId = 0; topicId < lda.numTopics(); topicId++) {
				y += (sampleTopics[topicId] * sampleTopics[topicId]);
			}
			double score = numerator / (Math.sqrt(x) * Math.sqrt(y));
			scoreMap.put(symbolTable.idToSymbol(token), score);

		}
		for (ScoredObject<String> v : scoreMap.scoredObjectsOrderedByValueList()) {
			System.out.println(v.getObject() + ":" + v.score());
		}

	}

	public static int[][] tokenizeDocuments(CharSequence[] texts, TokenizerFactory tokenizerFactory,
			SymbolTable symbolTable, int minCount) {
		ObjectToCounterMap<String> tokenCounter = new ObjectToCounterMap<String>();
		for (CharSequence text : texts) {
			char[] cs = Strings.toCharArray(text);
			Tokenizer tokenizer = tokenizerFactory.tokenizer(cs, 0, cs.length);
			for (String token : tokenizer) {
				String[] terms = token.split("/");
				if (terms.length != 2) {
					continue;
				}
				tokenCounter.increment(terms[0]);
			}
		}
		tokenCounter.prune(minCount);
		Set<String> tokenSet = tokenCounter.keySet();
		for (String token : tokenSet)
			symbolTable.getOrAddSymbol(token);

		int[][] docTokenId = new int[texts.length][];
		for (int i = 0; i < docTokenId.length; ++i) {
			docTokenId[i] = tokenizeDocument(texts[i], tokenizerFactory, symbolTable);
		}
		return docTokenId;
	}

	public static int[] tokenizeDocument(CharSequence text, TokenizerFactory tokenizerFactory,
			SymbolTable symbolTable) {
		char[] cs = Strings.toCharArray(text);
		Tokenizer tokenizer = tokenizerFactory.tokenizer(cs, 0, cs.length);
		List<Integer> idList = new ArrayList<Integer>();
		for (String token : tokenizer) {
			String[] terms = token.split("/");
			if (terms.length != 2) {
				continue;
			}
			int id = symbolTable.symbolToID(terms[0]);
			if (id >= 0)
				idList.add(id);
		}
		int[] tokenIds = new int[idList.size()];
		for (int i = 0; i < tokenIds.length; ++i)
			tokenIds[i] = idList.get(i);

		return tokenIds;
	}

	static void reportCorpus(CharSequence[] cSeqs) {
		ObjectToCounterMap<String> tokenCounter = new ObjectToCounterMap<String>();
		for (CharSequence cSeq : cSeqs) {
			char[] cs = cSeq.toString().toCharArray();
			for (String token : ANSJTOKENZIERFACTORY.tokenizer(cs, 0, cs.length))
				tokenCounter.increment(token);
		}
		System.out.println("TOKEN COUNTS");
		for (String token : tokenCounter.keysOrderedByCountList())
			System.out.printf("%9d %s\n", tokenCounter.getCount(token), token);
	}

	static CharSequence[] readCorpus(File file) throws IOException {
		FileInputStream fileIn = new FileInputStream(file);
		InputStreamReader isReader = new InputStreamReader(fileIn, "utf-8");
		BufferedReader bufReader = new BufferedReader(isReader);

		List<CharSequence> articleTextList = new ArrayList<CharSequence>(15000);
		StringBuilder docBuf = new StringBuilder();
		String line;
		while ((line = bufReader.readLine()) != null) {
			String splits[] = line.split("\t");
			if (splits.length != 4) {
				continue;
			}
			if (splits[3].equals("caijing")) {
				articleTextList.add(splits[1] + "," + RegexUtils.cleanParaAndImgLabel(splits[2]));
			}
		}
		bufReader.close();
		CharSequence[] articleTexts = articleTextList.<CharSequence>toArray(new CharSequence[articleTextList.size()]);
		return articleTexts;
	}

	static TokenizerFactory ANSJTOKENZIERFACTORY = getTokenizerFactory();

	private static TokenizerFactory getTokenizerFactory() {
		TokenizerFactory factory = AnsjNlpTokenizerFactory.getIstance();
		factory = new StopWordTokenierFactory(factory);
		factory = new RegexStopTokenzierFactory(factory);
		factory = new StopNatureTokenizerFactory(factory);
		return factory;
	}

}