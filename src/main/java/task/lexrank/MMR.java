package task.lexrank;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.aliasi.tokenizer.TokenizerFactory;
import com.rouchtime.util.RegexUtils;

import tokenizer.JiebaTokenizerFactory;

public class MMR implements Summarizer {
	private static final double MMR_LAMBDA = 0.7;
	static final TokenizerFactory TOKENIZER_FACTORY = getTokenizer();

	public static void main(String[] args) throws IOException {
		String dir = "D:\\corpus\\abstract";
		int size = 8;
		for (String line : FileUtils.readLines(new File(dir, "ele.txt"), "utf-8")) {
			String splits[] = line.split("\t+");
			String raw = RegexUtils.cleanImgLabel(splits[2]);
			List<Sentence> sentencesList = new ArrayList<Sentence>();
			List<String> sents = spiltSentence(raw);
			for (String sent : sents) {
				String sentenceText = sent;
				if (sentenceText.length() < 20) {
					continue;
				}
				List<String> sentenceWords = new ArrayList<String>();
				for (String token : TOKENIZER_FACTORY.tokenizer(sentenceText.toCharArray(), 0, sentenceText.length())) {
					sentenceWords.add(token.split("/")[0]);
				}
				Sentence sentence = new Sentence(sentenceText);
				sentence.setWords(sentenceWords);
				sentencesList.add(sentence);
			}
			Text text = new Text("News");
			text.setSentences(sentencesList);
			MMR mMr = new MMR();
			List<Sentence> summary = mMr.summarize(text, size);
			FileUtils.write(new File(dir, "caijing_result_pure_mmr"), String.format("%s\n", splits[0]), "utf-8", true);
			for (int i = 0; i < summary.size(); i++) {
				FileUtils.write(new File(dir, "caijing_result_pure_mmr"),
						String.format("%d:%s\n", i, RegexUtils.removeReportHead(summary.get(i).toString())), "utf-8", true);
			}
			FileUtils.write(new File(dir, "caijing_result_pure_mmr"), String.format("******************\n"), "utf-8",
					true);
		}
	}

	private static TokenizerFactory getTokenizer() {
		TokenizerFactory factory = JiebaTokenizerFactory.getIstance();
		return factory;
	}

	/**
	 * 分句子并且带上最后的标点符号
	 * @param document
	 * @return
	 */
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

	@Override
	public List<Sentence> summarize(Text text, Integer part) {
		List<Sentence> sents = new ArrayList<Sentence>();
		Map<String, Double> stringDoubleMap = inverseDocumentFrequency(text);
		Map<Integer, Double> scores = calSentsScore(text.getSentences(), stringDoubleMap);
		List<Integer> summary = mmr(scores, text.getSentences(), stringDoubleMap, part);
		Collections.sort(summary);
		for (int i : summary) {
			sents.add(text.getSentences().get(i));
		}
		return sents;
	}

	private Map<Integer, Double> calSentsScore(List<Sentence> list, Map<String, Double> stringDoubleMap) {
		Map<Integer, Double> map = new TreeMap<Integer, Double>();
		for (int i = 0; i < list.size(); i++) {
			List<Sentence> sentences = new ArrayList<Sentence>();
			for (int j = 0; j < list.size(); j++) {
				if (i == j) {
					continue;
				}
				sentences.add(list.get(j));
			}
			double sim = similarity(list.get(i), sentences, stringDoubleMap);
			map.put(i, sim);
		}
		return map;
	}

	protected List<Integer> mmr(Map<Integer, Double> rank, List<Sentence> list, Map<String, Double> stringDoubleMap,
			double part) {
		List<Integer> summary = new ArrayList<Integer>();
		int bestSentence = -1;
		while (summary.size() < part && summary.size() < list.size()) {
			double bestMMRScore = -Double.MAX_VALUE;
			for (int i = 0; i < rank.keySet().size(); i++) {
				if (summary.contains(i)) {
					continue;
				}
				double maxSimilarityToSummary = 0.0;
				for (int e : summary) {
					if (e == -1) {
						System.out.println();
					}
					maxSimilarityToSummary = Math.max(similarity(list.get(i), list.get(e), stringDoubleMap),
							maxSimilarityToSummary);
				}
				double mmr = MMR_LAMBDA * rank.get(i) - (1 - MMR_LAMBDA) * maxSimilarityToSummary;
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

	protected double similarity(Sentence sentence1, List<Sentence> sentence2, Map<String, Double> idf) {
		List<String> words1 = sentence1.getWords();
		HashBag<String> bag1 = new HashBag<String>(words1);
		HashBag<String> bag2 = new HashBag<>();
		for (Sentence sent : sentence2) {
			List<String> words2 = sent.getWords();
			bag2.addAll(words2);
		}
		Set<String> allWords = new TreeSet<String>();
		double s1 = 0;
		for (String word : words1) {
			s1 += Math.pow(bag1.get(word) * idf.get(word), 2);
			allWords.add(word);
		}
		double s2 = 0;
		for (Sentence sent : sentence2) {
			for (String word : sent.getWords()) {
				s2 += Math.pow(bag2.get(word) * idf.get(word), 2);
				allWords.add(word);
			}
		}
		double mul = 0;
		for (String word : allWords) {
			mul += bag1.get(word) * bag2.get(word) * Math.pow(idf.get(word), 2);
		}
		return mul / Math.sqrt(s1 * s2);
	}

	private Map<String, Double> inverseDocumentFrequency(Text text) {
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
}
