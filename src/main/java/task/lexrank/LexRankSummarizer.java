package task.lexrank;

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
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import com.aliasi.tokenizer.TokenizerFactory;
import com.hankcs.hanlp.HanLP;
import com.rouchtime.util.RegexUtils;

import tokenizer.JiebaTokenizerFactory;

public class LexRankSummarizer implements Summarizer {
	static final TokenizerFactory TOKENIZER_FACTORY = getTokenizer();
	protected static final double THRESHOLD = 0.065;
	private static final double DAMPING_FACTOR = 0.1;
	private static final double MMR_LAMBDA = 0.3;

	public List<Sentence> summarize(Text text, Integer size) {
		double[][] similarities = getSimilarities(text);
		// similarities = filter(similarities, THRESHOLD);
		double[] ranks = calcRanks(similarities);
		List<Sentence> sentences = text.getSentences();
		List<RankedSentence> rankedSentences = new ArrayList<RankedSentence>(text.numSentences());
		for (int i = 0; i < sentences.size(); i++) {
			Sentence sentence = sentences.get(i);
			rankedSentences.add(new RankedSentence(ranks[i], sentence));
		}
		StringBuilder rankedSentencesList = new StringBuilder("RankedSentences:\n");
		for (RankedSentence rankedSentence : rankedSentences) {
			rankedSentencesList.append(rankedSentence.toString()).append("\n");
		}
		Collections.sort(rankedSentences);
		List<Sentence> result = new ArrayList<Sentence>((int) (size));
		size = Math.min(size, ranks.length);
		for (int i = rankedSentences.size() - 1; i >= rankedSentences.size() - size; i--) {
			result.add(rankedSentences.get(i).getSentence());
		}
		Collections.sort(result,new LocationCompartor());
		return result;
	}
    static class LocationCompartor implements Comparator<Sentence> {  
		@Override
		public int compare(Sentence o1, Sentence o2) {
			return o1.getLocationIndex().compareTo(o2.getLocationIndex());
		}  
    }
	public List<Sentence> summarizeMMR(Text text, int size) {
		double[][] similarities = getSimilarities(text);
		// similarities = filter(similarities, THRESHOLD);
		double[] ranks = calcRanks(similarities);
		List<Integer> summary = mmr(ranks, similarities, size);
		Collections.sort(summary);
		List<Sentence> sentences = text.getSentences();
		List<Sentence> result = new ArrayList<Sentence>((int) (size));
		size = Math.min(size, ranks.length);
		for (int i = 0; i < size && i < summary.size(); i++) {
			result.add(sentences.get(summary.get(i)));
		}
		return result;
	}

	private static TokenizerFactory getTokenizer() {
		TokenizerFactory factory = JiebaTokenizerFactory.getIstance();
		return factory;
	}

	protected List<Integer> mmr(double[] rank, double[][] similarities, int size) {
		List<Integer> summary = new ArrayList<Integer>();
		int bestSentence = -1;
		while (summary.size() < size && summary.size() < rank.length) {
			double bestMMRScore = -Double.MAX_VALUE;
			for (int i = 0; i < rank.length; i++) {
				if (summary.contains(i)) {
					continue;
				}
				double maxSimilarityToSummary = 0.0;
				for (int e : summary) {
					if (e == -1) {
						System.out.println();
					}

					maxSimilarityToSummary = Math.max(similarities[i][e], maxSimilarityToSummary);
				}
				double mmr = MMR_LAMBDA * rank[i] - (1 - MMR_LAMBDA) * maxSimilarityToSummary;
				if (mmr > bestMMRScore) {
					bestMMRScore = mmr;
					bestSentence = i;
				}
			}
			if (bestSentence == -1) {
				System.out.println();
			}
			summary.add(bestSentence);
		}
		return summary;
	}

	protected double[][] getSimilarities(Text text) {
		int numSentences = text.numSentences();
		double[][] similarities = new double[numSentences][numSentences];
		List<Sentence> sentences = text.getSentences();
		Map<String, Double> stringDoubleMap = inverseDocumentFrequency(text);
		for (int i = 0; i < numSentences; i++) {
			for (int j = 0; j < i; j++) {
				similarities[i][j] = similarity(sentences.get(i), sentences.get(j), stringDoubleMap);
				similarities[j][i] = similarities[i][j];
			}
			similarities[i][i] = 1;
		}
		return similarities;
	}

	protected double[][] filter(double[][] similarities, double threshold) {
		for (int i = 0; i < similarities.length; i++) {
			for (int j = 0; j < similarities.length; j++) {
				if (similarities[i][j] < threshold) {
					similarities[i][j] = 0;
				}
			}
		}
		return similarities;
	}

	protected int numNotZero(double[][] similarities) {
		int result = 0;
		for (double[] similarityRow : similarities) {
			for (int j = 0; j < similarities.length; j++) {
				if (similarityRow[j] > 0) {
					result++;
				}
			}
		}
		return result;
	}

	protected double similarity(Sentence sentence1, Sentence sentence2, Map<String, Double> idf) {
		List<String> words1 = sentence1.getWords();
		List<String> words2 = sentence2.getWords();
		HashBag<String> bag1 = new HashBag<String>(words1);
		HashBag<String> bag2 = new HashBag<String>(words2);
		Set<String> allWords = new TreeSet<String>();
		double s1 = 0;
		for (String word : words1) {
			s1 += Math.pow(bag1.get(word) * idf.get(word), 2);
			allWords.add(word);
		}
		double s2 = 0;
		for (String word : words2) {
			s2 += Math.pow(bag2.get(word) * idf.get(word), 2);
			allWords.add(word);
		}
		double mul = 0;
		for (String word : allWords) {
			mul += bag1.get(word) * bag2.get(word) * Math.pow(idf.get(word), 2);
		}
		return mul / Math.sqrt(s1 * s2);
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

	protected double[][] normalize(double[][] similarities) {
		for (int i = 0; i < similarities.length; i++) {
			double[] similarity = similarities[i];
			double sum = 0;
			for (int j = 0; j < similarity.length; j++) {
				sum += similarity[j];
			}
			for (int j = 0; j < similarity.length; j++) {
				similarity[j] = similarity[j] / sum;
			}
		}
		return similarities;
	}

	protected double[] calcRanks(double[][] copy) {
		double[][] similarities = copy.clone();
		similarities = normalize(similarities);
		final int n = similarities.length;
		double[] p = new double[n];
		for (int i = 0; i < p.length; i++) {
			p[i] = 1.0 / n;
		}
		double[][] kernelData = new double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				kernelData[i][j] = DAMPING_FACTOR / n + (1 - DAMPING_FACTOR) * similarities[i][j];
			}
		}
		RealMatrix transitionKernel = new Array2DRowRealMatrix(kernelData, false).transpose();
		RealMatrix ranks = new Array2DRowRealMatrix(p);
		double eps = 0;
		int iterations = 0;
		do {
			RealMatrix oldRanks = ranks;
			ranks = transitionKernel.multiply(ranks);
			eps = ranks.subtract(oldRanks).getNorm();
			iterations++;
		} while (eps > 0.0001);
		double[] result = ranks.transpose().getData()[0];
		normalise(result);
		return result;
	}

	protected void normalise(double[] ranks) {
		double max = 0;
		double min = 1;
		for (int i = 0; i < ranks.length; i++) {
			if (max < ranks[i]) {
				max = ranks[i];
			}
			if (min > ranks[i]) {
				min = ranks[i];
			}
		}
		max = max - min;
		if (max == 0) {
			return;
		}
		for (int i = 0; i < ranks.length; i++) {
			ranks[i] = (ranks[i] - min) / max;
		}
	}

	protected void printArr(double[][] arr) {
		for (int i = 0; i < arr.length; i++) {
			double[] doubles = arr[i];
			StringBuilder builder = new StringBuilder("{");
			for (int j = 0; j < doubles.length; j++) {
				double val = doubles[j];
				builder.append(String.format("%f ,", val));
			}
			builder.append("},");
			System.out.println(builder.toString());
		}
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
			String regex = "[。？?！!；;]";
			Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
			Matcher m = pattern.matcher(line);
			/* 按照句子结束符分割句子 */
			String[] sents = pattern.split(line);
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
		}
		return sentences;
	}

	public static void main(String[] args) throws IOException {
		String dir = "D:\\corpus\\abstract";
		int size = 8;
		for (String line : FileUtils.readLines(new File(dir, "ele.txt"), "utf-8")) {
			String splits[] = line.split("\t+");
			String raw = RegexUtils.cleanImgLabel(splits[2]);
			List<Sentence> sentencesList = new ArrayList<Sentence>();
			List<String> sents = LexRankSummarizer.spiltSentence(raw);
			int sentLocation = 0;
			for (String sent : sents) {
				if (sent.length() < 15) {
					continue;
				}
				String sentenceText = sent;
				List<String> sentenceWords = new ArrayList<String>();
				for (String token : TOKENIZER_FACTORY.tokenizer(sentenceText.toCharArray(), 0, sentenceText.length())) {
					sentenceWords.add(token.split("/")[0]);
				}
				Sentence sentence = new Sentence(sentenceText);
				sentence.setWords(sentenceWords);
				sentence.setLocationIndex(sentLocation++);
				sentencesList.add(sentence);
			}
			Text text = new Text("News");
			text.setSentences(sentencesList);
			LexRankSummarizer summarizer = new LexRankSummarizer();
			List<Sentence> summaryMMR = summarizer.summarizeMMR(text, size);
			FileUtils.write(new File(dir, "caijing_result_lexrank_MMR"), String.format("%s\n", splits[0]), "utf-8",
					true);
			for (int i = 0; i < summaryMMR.size(); i++) {
				FileUtils.write(new File(dir, "caijing_result_lexrank_MMR"),
						String.format("%d:%s\n", i, RegexUtils.removeReportHead(summaryMMR.get(i).toString())), "utf-8", true);
			}
			FileUtils.write(new File(dir, "caijing_result_lexrank_MMR"), String.format("******************\n"), "utf-8",
					true);

			List<Sentence> summary = summarizer.summarize(text, size);
			FileUtils.write(new File(dir, "caijing_result_lexrank"), String.format("%s\n", splits[0]), "utf-8", true);
			for (int i = 0; i < summary.size(); i++) {
				FileUtils.write(new File(dir, "caijing_result_lexrank"),
						String.format("%d:%s\n", i, RegexUtils.removeReportHead(summary.get(i).toString())), "utf-8", true);
			}
			FileUtils.write(new File(dir, "caijing_result_lexrank"), String.format("******************\n"), "utf-8",
					true);
		}
	}
}
