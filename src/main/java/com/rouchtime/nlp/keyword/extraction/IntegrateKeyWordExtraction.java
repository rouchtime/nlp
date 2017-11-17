package com.rouchtime.nlp.keyword.extraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.ScoredObject;
import com.rouchtime.nlp.keyword.DictionaryResource;
import com.rouchtime.nlp.keyword.SynonymMerge;
import com.rouchtime.nlp.keyword.Token;
import com.rouchtime.nlp.keyword.lexicalChain.LexicalChainMaxSim;
import com.rouchtime.nlp.keyword.similarity.Word2VectorWordSimiarity;
import com.rouchtime.nlp.keyword.similarity.WordSimiarity;
import com.rouchtime.util.Contants;

import tokenizer.AnsjNlpTokenizerFactory;

/**
 * 混合关键词提取算法
 * 
 * @author 龚帅宾
 *
 */
public class IntegrateKeyWordExtraction extends AbstractKeyWordExtraction {
	private Logger logger = Logger.getLogger(IntegrateKeyWordExtraction.class);
	private WordSimiarity wordSimiarity;
	private Map<String, Double> idfMap;
	private LexicalChainMaxSim lexicalChain;
	
	/**
	 * 
	 * @param tokenizerFactory 分词器工厂，实例出单例的分词器
	 * @param wordSimiarity 相似度计算方法，实例出单例的相似度
	 * <p>
	 * 1、根据word2vector的相似度；
	 * {@link #Word2VectorWordSimiarity}
	 * </p>
	 * <p>
	 * 2、根据同义词词林的相似度
	 * {@link #CiLinWordSimilarity}
	 * </p>
	 */
	public IntegrateKeyWordExtraction(TokenizerFactory tokenizerFactory, WordSimiarity wordSimiarity) {
		super(tokenizerFactory);
		this.wordSimiarity = wordSimiarity;
		idfMap = DictionaryResource.getInstance().getIDFMAP();
	}

	/**
	 * 无参构造器默认使用ansj的NLP分词器，未用用户自定义词典，词的相似度使用word2Vector相似度
	 */
	public IntegrateKeyWordExtraction() {
		super(AnsjNlpTokenizerFactory.getIstance());
		this.wordSimiarity = Word2VectorWordSimiarity.getInstance();
		idfMap = DictionaryResource.getInstance().getIDFMAP();
	}

	
	@Override
	ObjectToDoubleMap<String> modifyKeywordsSort(List<String> titleTokens, List<String> bodyTokens) {
		Map<String, Token> tokenMap = wordProcess(titleTokens, bodyTokens);
		/* 合并之前的TF */
		Map<String, Double> tfMap = new HashMap<String, Double>();
		for (String key : tokenMap.keySet()) {
			tfMap.put(key, tokenMap.get(key).getTfIndoc());
		}
		/* 同义词合并后的tf值 */
		Map<String, Double> mergedTF = SynonymMerge.mergeTFBySynonym(tfMap, wordSimiarity);
		int size = mergedTF.keySet().size();

		double[] word_area_weight = new double[size];
		double[] word_span_weight = new double[size];
		double[] word_firstpos_weight = new double[size];
		double[] word_POS_weight = new double[size];
		double[] word_tfidf_weight = new double[size];
		double[] word_lexical_weight = new double[size];
		double[] word_textRank_weight = new double[size];
		try {
			/* 词的区域权重设置 */
			wordAreaWeightCalcultate(tokenMap, mergedTF, word_area_weight);

			/* 词的覆盖区域权重，（第一次出现和最后一次出现的范围/总词数） */
			wordSpanWeightCalculate(tokenMap, mergedTF, word_span_weight);

			/* 词出现首次的位置在文章中的区域，越在开头的和结尾的词越重要 */
			wordFirstPosWeight(tokenMap, mergedTF, word_firstpos_weight);

			/* 词性权重 */
			wordPOSWeight(tokenMap, mergedTF, word_POS_weight);

			/* tfidf权重设置 */
			tfidfWeightCalcultate(mergedTF, word_tfidf_weight);

			/* 词汇链权重设置 */
			wordLexicalWeight(tokenMap, mergedTF, word_lexical_weight);

			textRankWeight(titleTokens, bodyTokens, mergedTF, word_textRank_weight);

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		double[] weight = weightCombine(word_area_weight, word_span_weight, word_firstpos_weight, word_POS_weight,
				word_tfidf_weight, word_lexical_weight, word_textRank_weight, size);
		ObjectToDoubleMap<String> weightMap = new ObjectToDoubleMap<>();
		int index = 0;
		for (String word : mergedTF.keySet()) {
			weightMap.put(word, weight[index++]);
		}
		return weightMap;
	}
	
	private void textRankWeight(List<String> titleTokens, List<String> bodyTokens, Map<String, Double> mergedTF,
			double[] word_textRank_weight) throws Exception {
		TextRankWithMultiWinExtraction textRank = new TextRankWithMultiWinExtraction(2, 10, super.tokenizerFactory);
		List<ScoredObject<String>> list = textRank.keywordsScore(titleTokens, bodyTokens);
		Map<String, Double> scoreMap = new HashMap<String, Double>();
		for (ScoredObject<String> score : list) {
			scoreMap.put(score.getObject(), score.score());
		}
		if (list.size() < word_textRank_weight.length) {
			logger.warn(String.format("TextRank:size is not equal %d<%d", list.size(),word_textRank_weight.length));
		}
		int index = 0;
		for (String word : mergedTF.keySet()) {
			Double value = scoreMap.get(word);
			if(value.isNaN()) {
				word_textRank_weight[index] = 0.0;
			} else {
				word_textRank_weight[index] = scoreMap.get(word);
			}
			index++;
		}
		normalize(word_textRank_weight);
	}

	private double[] weightCombine(double[] word_area_weight, double[] word_span_weight, double[] word_firstpos_weight,
			double[] word_POS_weight, double[] word_tfidf_weight, double[] word_lexical_weight,
			double[] word_textRank_weight, int size) {
		double[] weight = new double[size];
		for (int i = 0; i < size; i++) {
			weight[i] = word_area_weight[i] * 1.2 + word_span_weight[i] * 1.0 + word_firstpos_weight[i] * 1.0
					+ word_POS_weight[i] * 1.0 + word_tfidf_weight[i] * 0.5 + word_lexical_weight[i] * 1.5
					+ word_textRank_weight[i] * 0.5;
		}
		return weight;
	}

	private double[] normalize(double[] values) {
		double sum = 0.0;
		for (double value : values) {
			sum += value;
		}
		for (int i = 0; i < values.length; i++) {
			values[i] = values[i] / sum;
		}
		return values;
	}

	private void wordLexicalWeight(Map<String, Token> tokenMap, Map<String, Double> mergedTF,
			double[] word_lexical_weight) throws Exception {
		if (lexicalChain == null) {
			throw new Exception("lexicalChain is null!");
		}
		List<Set<String>> chains = lexicalChain.getLexicalChain();
		List<Double> lexWeight = new ArrayList<Double>();
		for (Set<String> chain : chains) {
			/* 获得链长度 */
			double l_length = chain.size();
			int pos_min = Integer.MAX_VALUE;
			int pos_max = -Integer.MAX_VALUE;
			double l_sent = 0.0;
			double l_rel = 0.0;
			for (String word : chain) {
				int last = tokenMap.get(word).getLastPosition();
				int first = tokenMap.get(word).getWordPosition();
				if (last > pos_max) {
					pos_max = last;
				}
				if (first < pos_min) {
					pos_min = first;
				}
				l_sent += tokenMap.get(word).getTokenExistsSentsNum();
				for (String word1 : chain) {
					if (word.equals(word1)) {
						continue;
					}
					double sim = wordSimiarity.calTowWordSimiarity(word, word1);
					l_rel += sim;
				}
			}
			if (chain.size() <= 1) {
				l_rel = 0.0;
			} else {
				l_rel = (l_rel / 2.0) / (l_length - 1);
			}

			/* 获得链中词的最大覆盖范围 */
			double l_span = pos_max - pos_min;
			double weight = (0.1 * l_length + 0.3 * l_span + 0.2 * l_sent + 0.4 * l_rel) / Math.sqrt(
					Math.pow(l_length, 2.0) + Math.pow(l_span, 2.0) + Math.pow(l_sent, 2.0) + Math.pow(l_rel, 2.0));
			lexWeight.add(weight);
		}
		int index = 0;
		for (String word : mergedTF.keySet()) {
			for (int i = 0; i < chains.size(); i++) {
				if (chains.get(i).contains(word)) {
					word_lexical_weight[index] = lexWeight.get(i);
				}
			}
			index++;
		}
		normalize(word_lexical_weight);
	}

	private void wordPOSWeight(Map<String, Token> tokenMap, Map<String, Double> mergedTF, double[] word_POS_weight)
			throws Exception {
		if (mergedTF.size() != word_POS_weight.length) {
			throw new Exception("POS :size is not equal!");
		}
		int index = 0;
		for (String word : mergedTF.keySet()) {
			String nature = tokenMap.get(word).getNature();
			if (nature.charAt(0) == 'n') {
				word_POS_weight[index++] = 0.8;
				continue;
			}
			if (nature.charAt(0) == 'g') {
				word_POS_weight[index++] = 0.8;
				continue;
			}
			if (nature.charAt(0) == 'i') {
				word_POS_weight[index++] = 0.8;
				continue;
			}
			if (nature.charAt(0) == 'a') {
				word_POS_weight[index++] = 0.3;
				continue;
			}
			word_POS_weight[index++] = 0;
		}
		normalize(word_POS_weight);
	}

	private void wordFirstPosWeight(Map<String, Token> tokenMap, Map<String, Double> mergedTF,
			double[] word_firstpos_weight) throws Exception {
		if (mergedTF.size() != word_firstpos_weight.length) {
			throw new Exception("WORD FirstPos:size is not equal!");
		}
		int index = 0;
		for (String word : mergedTF.keySet()) {
			word_firstpos_weight[index] = Math.abs(tokenMap.get(word).getWordPosition() / super.docLength - 0.5);
			index++;
		}
		normalize(word_firstpos_weight);
	}

	private void wordSpanWeightCalculate(Map<String, Token> tokenMap, Map<String, Double> mergedTF,
			double[] word_span_weight) throws Exception {
		if (mergedTF.size() != word_span_weight.length) {
			throw new Exception("WORD SPAN:size is not equal!");
		}
		int index = 0;
		for (String word : mergedTF.keySet()) {
			word_span_weight[index] = tokenMap.get(word).getWordSpan() * 1.0 / super.docLength;
			index++;
		}
		normalize(word_span_weight);
	}

	private void wordAreaWeightCalcultate(Map<String, Token> tokenMap, Map<String, Double> mergedTF,
			double[] word_area_weight) throws Exception {
		if (mergedTF.size() != word_area_weight.length) {
			throw new Exception("WORD AREA:size is not equal!");
		}
		int index = 0;
		for (String word : mergedTF.keySet()) {
			switch (tokenMap.get(word).getArea()) {
			case TITLE:
				word_area_weight[index] = 3;
				break;
			case BODY:
				word_area_weight[index] = 0.5;
				break;
			default:
				word_area_weight[index] = 0.5;
				break;
			}
			index++;
		}
		normalize(word_area_weight);
	}

	/**
	 * 计算tfidf值
	 * 
	 * @param mergedTF
	 *            词频Map
	 * @param word_tfidf_weight
	 *            权重数组
	 * @throws Exception
	 */
	private void tfidfWeightCalcultate(Map<String, Double> mergedTF, double[] word_tfidf_weight) throws Exception {
		if (mergedTF.size() != word_tfidf_weight.length) {
			throw new Exception("TFIDF:size is not equal!");
		}
		double sumTfIdf = 0.0;
		for (String word : mergedTF.keySet()) {
			Double idf = idfMap.get(word);
			if (idf == null) {
				double value = mergedTF.get(word) * 0.01;
				sumTfIdf += Math.pow(value, 2.0);
			} else {
				double value = mergedTF.get(word) * idf;
				sumTfIdf += Math.pow(value, 2.0);
			}
		}
		int index = 0;
		for (String word : mergedTF.keySet()) {
			Double idf = idfMap.get(word);
			if (idf == null) {
				double value = mergedTF.get(word) * 0.01;
				word_tfidf_weight[index++] = value / Math.sqrt(sumTfIdf);
			} else {
				double value = mergedTF.get(word) * idf;
				word_tfidf_weight[index++] = value / Math.sqrt(sumTfIdf);
			}
		}
	}

	private Map<String, Token> wordProcess(List<String> titleTokens, List<String> bodyTokens) {
		lexicalChain = new LexicalChainMaxSim(0.6, wordSimiarity);
		Map<String, Token> mapToken = new HashMap<String, Token>();
		int index = 0;
		int position = 0;
		/* 标题词汇统计 */
		for (String term : titleTokens) {
			docLength++;
			String word = term.split(Contants.SLASH)[0];
			String pos = term.split(Contants.SLASH)[1];
			if (mapToken.get(word) != null) {
				Token token = mapToken.get(word);
				token.setWordSpan(position - token.getWordPosition());
				token.setTfIndoc(token.getTfIndoc() + 1.0);
				token.setLastPosition(position);
			} else {
				int numInSents = 0;
				for (List<List<String>> paragraph : PARAGRAPH) {
					for(List<String> sent : paragraph) {
						for(String token : sent) {
							if (token.equals(word)) {
								numInSents++;
								break;
							}
						}
					}
				}
				Token token = new Token(index++, word, pos, Contants.WordArea.TITLE, position - position, position,
						position);
				token.setTfIndoc(1.0);
				token.setTokenExistsSentsNum(numInSents);
				mapToken.put(word, token);
			}
			/* 将词加入词汇链 */
			lexicalChain.add(word);
			position++;
		}

		/* 正文词汇统计 */
		for (String term : bodyTokens) {
			docLength++;
			String word = term.split(Contants.SLASH)[0];
			String pos = term.split(Contants.SLASH)[1];
			if (mapToken.get(word) != null) {
				Token token = mapToken.get(word);
				token.setWordSpan(position - token.getWordPosition());
				token.setTfIndoc(token.getTfIndoc() + 1.0);
				token.setLastPosition(position);
			} else {
				int numInSents = 0;
				for (List<List<String>> paragraph : PARAGRAPH) {
					for(List<String> sent : paragraph) {
						for(String token : sent) {
							if (token.equals(word)) {
								numInSents++;
								continue;
							}
						}
					}
				}
				Token token = new Token(index++, word, pos, Contants.WordArea.TITLE, position - position, position,
						position);
				token.setTokenExistsSentsNum(numInSents);
				token.setTfIndoc(1.0);
				mapToken.put(word, token);
			}
			lexicalChain.add(word);
			position++;
		}
		return mapToken;
	}

}
