package com.rouchtime.nlp.summarization.lsa;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.aliasi.matrix.SvdMatrix;
import com.aliasi.symbol.MapSymbolTable;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.rouchtime.nlp.sentence.ChineseSentenceModel;
import com.rouchtime.nlp.sentence.SummarizationSentenceModel;
import com.rouchtime.nlp.summarization.HashBag;
import com.rouchtime.nlp.summarization.Sentence;
import com.rouchtime.nlp.summarization.Summarizer;
import com.rouchtime.nlp.summarization.Text;
import com.rouchtime.util.RegexUtils;

import tokenizer.HanLPTokenizerFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

public class SummarizeForSVD implements Summarizer {
	static final TokenizerFactory TOKENIZER_FACTORY = getTokenizer();
	static final TokenizerFactory TOKENIZER_FACTORY_SPLIT_SENTS = HanLPTokenizerFactory.getIstance();
	static final ChineseSentenceModel SENTENCE_MODEL = SummarizationSentenceModel.INSTANCE;
	static double featureInit = 0.01;
	static double initialLearningRate = 0.005;
	static int annealingRate = 1000;
	static double regularization = 0.00;
	static double minImprovement = 0.0000001;
	static int minEpochs = 10;
	static int maxEpochs = 50000;
	static int NUM_FACTORS = 10;

	public static void main(String[] args) throws IOException {
		String dir = "D:\\corpus\\abstract";
		for (String line : FileUtils.readLines(new File(dir, "ele.txt"), "utf-8")) {
//			 String line =
//			 "!@#!@ 互联网对传统新闻造成的冲击终于让美国报业坐不住了。 !@#!@ 7月10日，美国新闻媒体联盟（News Media Alliance）向美国国会申诉，要求美国国会修改《反垄断法》，以更好地应对谷歌和脸书两家互联网巨头对传统媒体造成的冲击。 !@#!@ 美国新闻媒体联盟发端于美国报业联盟，其中包含《华盛顿邮报》、《华尔街日报》、《纽约时报》等知名美国传统媒体和众多规模不大的媒体，总数近2000家。 !@#!@$#imgidx=0001#$!@#!@美国新闻媒体联盟10日发布的文章截图!@#!@维权不易：报业如想维权需先改法律!@#!@ 在美国新闻媒体联盟网站上公布的这一号召称，消费者对即时、可靠消息的需求日益增长，但是目前，互联网的分配系统却将优秀新闻报道的经济价值分配进行扭曲。由于在数字时代谷歌和脸书的双垄断局面，新闻媒体被迫在内容上做出让步，并按照数字媒体的规则运行。但是这些规则一方面将新闻商品化，另一方面也增加了假新闻出现的风险，而在目前的体系中，区分真假新闻是有一定困难的。 !@#!@ 据美国CNBC电视台报道，根据皮尤研究中心的数据，谷歌和脸书现在基本上占据了美国总额达730亿美元的互联网广告中的70%。然而美国报业去年的广告收入仅有180亿美元，但是10年前，报业广告有500亿美元之多。 !@#!@$#imgidx=0002#$!@#!@由于广告收入大幅下降，《纽约时报》不得不将其总部大楼部分房间出租以赚取租金（社交媒体截图）!@#!@ 不过，想要维权也不并不容易。美国《反垄断法》的初衷是降低垄断性企业对社会的伤害。但是，美国新闻媒体联盟称，在媒体行业上，现存法律却无意地阻止了新闻媒体联合起来在谈判中获得有利地位，以让对民主制度至关重要的新闻媒体能够可持续地为人们服务。 !@#!@ 在面对一个几乎将传统媒体逼入绝路、占尽广告收入资源的双垄断互联网媒体环境时，新闻媒体在与互联网媒体谈判时没有任何主动权。 !@#!@为了民主，支持传统新闻业!@#!@ “立法机构如能允许新闻媒体集体进行谈判，将会对今天的媒体行业的健康及高质量发展提供可能。”新闻媒体联盟的主席大卫·查文（David Chavern）说，“高质量的新闻业是保障民主的重要部分，也是公民社会的核心。为了让这样的新闻业能够有未来，新闻媒体发现必须集体同互联网媒体平台进行谈判。” !@#!@ 美国新闻媒体联盟还称，除了在媒体行业的主导地位，脸书和谷歌无法在其信源和能力上保证新闻报道的真实性。脸书在去年的美国大选中就因为其没有对新闻内容的真实性进行审查而遭到公众质疑。 !@#!@ “脸书和谷歌并不雇佣记者，他们不会通过公开信息去发掘腐败丑闻，也不会派驻战地记者，甚至不会派人去体育比赛现场带来最新报道。但是，他们却榨取了整个新闻行业的经济效益，而所有花钱的事情却都是我们来做的。”查文说，“唯一的维权方式就是大家拧成一股绳。” !@#!@ “如果我们最终能和脸书、谷歌谈判后达成更有利的知识产权保护协议以及更公平的收入分配体系，新闻业才会可持续发展。” !@#!@$#imgidx=0003#$!@#!@互联网给传统报业带来巨大冲击，2016年，英国《独立报》推出最后一期后，停止纸版报纸发行，全面改为互联网媒体（社交媒体截图）!@#!@ 本文系观察者网独家稿件，文章内容纯属作者个人观点，不代表平台观点，未经授权，不得转载，否则将追究法律责任。关注观察者网微信guanchacn，每日阅读趣味文章。";
//			 String rawText = RegexUtils.cleanImgLabel(line);
			String splits[] = line.split("\t+");
			String rawText = RegexUtils.cleanImgLabel(splits[2]);
			List<String> sentences = spiltSentence(rawText);
			MapSymbolTable symbolTable = new MapSymbolTable();
			int sentLocation = 0;
			List<Sentence> sentencesList = new ArrayList<Sentence>();
			for (String sent : sentences) {
				if (sent.length() < 10) {
					continue;
				}
				List<String> sentenceWords = new ArrayList<String>();
				for (String token : TOKENIZER_FACTORY.tokenizer(sent.toCharArray(), 0, sent.length())) {
					String word = token.split("/")[0];
					symbolTable.getOrAddSymbol(word);
					sentenceWords.add(word);
				}
				Sentence sentence = new Sentence(sent);
				sentence.setWords(sentenceWords);
				sentence.setLocationIndex(sentLocation++);
				sentencesList.add(sentence);
			}
			Text text = new Text("News");
			text.setSentences(sentencesList);
			text.setSymbolTable(symbolTable);
			SummarizeForSVD svd = new SummarizeForSVD();
			List<Sentence> summary = svd.summarize(text, 5);
			FileUtils.write(new File(dir, "caijing_result_svd"), String.format("%s\n", splits[0]), "utf-8", true);
			for (int i = 0; i < summary.size(); i++) {
				FileUtils.write(new File(dir, "caijing_result_svd"),
						String.format("%d:%s\n", i, RegexUtils.removeReportHead(summary.get(i).toString())), "utf-8",
						true);
			}
			FileUtils.write(new File(dir, "caijing_result_svd"), String.format("******************\n"), "utf-8", true);
		}

	}

	private static TokenizerFactory getTokenizer() {
		StopWordTokenierFactory stopWordFactory = new StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		StopNatureTokenizerFactory stopNatureTokenizerFactory = new StopNatureTokenizerFactory(stopWordFactory);
		return stopNatureTokenizerFactory;
	}

	public static List<String> spiltSentence(String document) {
		List<String> sentences = new ArrayList<String>();
		for (String line : document.split("!@#!@")) {
			if (line.equals("")) {
				continue;
			}
			line = RegexUtils.cleanSpecialWord(line.trim());
			if (line.length() == 0)
				continue;
			Tokenizer tokenizer = TOKENIZER_FACTORY_SPLIT_SENTS.tokenizer(line.toCharArray(), 0, line.length());
			String[] tokens = tokenizer.tokenize();
			int[] sentenceBoundaries = SENTENCE_MODEL.boundaryIndices(tokens);
			if (sentenceBoundaries.length < 1) {
				System.out.println("未发现句子边界！");
				continue;
			}
			int sentStartTok = 0;
			int sentEndTok = 0;
			for (int i = 0; i < sentenceBoundaries.length; ++i) {
				sentEndTok = sentenceBoundaries[i];
				StringBuffer sbSents = new StringBuffer();
				for (int j = sentStartTok; j <= sentEndTok; j++) {
					sbSents.append(tokens[j]);
				}
				sentStartTok = sentEndTok + 1;
				sentences.add(sbSents.toString());
			}
			
//			String regex = "[(。”)。？?！!；;]";
//			Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
//			Matcher m = pattern.matcher(document);
//			/* 按照句子结束符分割句子 */
//			String[] sents = pattern.split(document);
//			/* 将句子结束符连接到相应的句子后 */
//			if (sents.length > 0) {
//				int count = 0;
//				while (count < sents.length) {
//					if (m.find()) {
//						sents[count] += m.group();
//					}
//					count++;
//				}
//			}
//			for (String sent : sents) {
//				sentences.add(sent);
//			}
		}
		return sentences;
	}

	public static List<String> split(String document) {
		List<String> sentences = new ArrayList<String>();
		String regex = "[。？?！!；;]";
		Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
		Matcher m = pattern.matcher(document);
		/* 按照句子结束符分割句子 */
		String[] sents = pattern.split(document);
		/* 将句子结束符连接到相应的句子后 */
		if (sents.length > 0) {
			int count = 0;
			while (count < sents.length) {
				if (m.find()) {
					sents[count] += m.group();
				}
				count++;
			}
		}
		for (String sent : sents) {
			sentences.add(sent);
		}
		return sentences;
	}
	
	
	
	@Override
	public List<Sentence> summarize(Text text, Integer part) {
		Map<String, Double> stringDoubleMap = inverseDocumentFrequency(text);
		double[][] matrix = getMatrix(text, stringDoubleMap);
		SvdMatrix svdMatrix = SvdMatrix.svd(matrix, Math.min(NUM_FACTORS, text.getSentences().size()), featureInit,
				initialLearningRate, annealingRate, regularization, null, minImprovement, minEpochs, maxEpochs);
		 double[] scales = svdMatrix.singularValues();
		 double[][] termVectors = svdMatrix.leftSingularVectors();
		double[][] docVectors = svdMatrix.rightSingularVectors();
//		 for (int j = 0; j < docVectors.length; ++j) {
//		 System.out.print("(");
//		 for (int k = 0; k < NUM_FACTORS; ++k) {
//		 if (k > 0)
//		 System.out.print(", ");
//		 System.out.printf("% 5.2f", docVectors[j][k]);
//		 }
//		 System.out.print(") ");
//		 System.out.println(text.getSentences().get(j).getSentence());
//		 }

		Set<Integer> summarySentsIndex = new HashSet<Integer>();
		for (int j = 0; j < part && j < text.getSentences().size(); ++j) {
			int maxIndex = 0;
			double max = -Double.MAX_VALUE;
			for (int k = 0; k < docVectors.length; ++k) {
				double v = Math.abs(docVectors[k][j]);
				if (max < v) {
					max = v;
					maxIndex = k;
				}
			}
			summarySentsIndex.add(maxIndex);
		}
		List<Sentence> summarySent = new ArrayList<Sentence>();
		for (int index : summarySentsIndex) {
			summarySent.add(text.getSentences().get(index));
		}
		Collections.sort(summarySent, new LocationCompartor());
		return summarySent;
	}

	static class LocationCompartor implements Comparator<Sentence> {
		@Override
		public int compare(Sentence o1, Sentence o2) {
			return o1.getLocationIndex().compareTo(o2.getLocationIndex());
		}
	}

	protected Map<String, Double> inverseDocumentFrequency(Text text) {
		List<Sentence> sentences = text.getSentences();
		Set<String> allWords = new TreeSet<String>();
		for (Sentence sentence : sentences) {
			List<String> wordsOfSentence = sentence.getWords();
			for (String word : wordsOfSentence) {
				allWords.add(word);
			}
		}

		double textSize = sentences.size();
		Map<String, Double> result = new HashMap<String, Double>();
		for (String word : allWords) {
			int num = 0;
			for (Sentence sentence : sentences) {
				if (sentence.contains(word)) {
					num++;
				}
			}
			result.put(word, Math.log(textSize / num));
		}
		return result;
	}

	private double[][] getMatrix(Text text, Map<String, Double> stringDoubleMap) {
		int sentCount = text.getSentences().size();
		double[][] matric = new double[text.getSymbolTable().idSet().size()][sentCount];
		MapSymbolTable symbolTable = text.getSymbolTable();
		for (int i = 0; i < sentCount; i++) {
			Sentence sent = text.getSentences().get(i);
			HashBag<String> bag = new HashBag<String>(sent.getWords());
			for (String word : bag.getKeySet()) {
				int wordIndex = symbolTable.symbolToID(word);
				matric[wordIndex][i] = bag.get(word) * stringDoubleMap.get(word);
			}
		}
		return matric;
	}
}
