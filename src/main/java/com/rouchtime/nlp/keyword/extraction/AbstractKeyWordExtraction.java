package com.rouchtime.nlp.keyword.extraction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.ScoredObject;
import com.rouchtime.nlp.sentence.ChineseSentenceModel;
import com.rouchtime.nlp.sentence.SummarizationSentenceModel;
import com.rouchtime.util.Contants;
import com.rouchtime.util.RegexUtils;

import tokenizer.AnsjNlpTokenizerFactory;
import tokenizer.AnsjTokenizerFactory;

public abstract class AbstractKeyWordExtraction implements KeyWordExtraction {
	private Logger logger = Logger.getLogger(AbstractKeyWordExtraction.class);
	TokenizerFactory tokenizerFactory;
	private TokenizerFactory TOKENIZER_FACTORY_SPLIT_SENTS = AnsjNlpTokenizerFactory.getIstance();
	private ChineseSentenceModel SENTENCE_MODEL = SummarizationSentenceModel.INSTANCE;
	private boolean enableWordAssemble = false;
	List<List<List<String>>> PARAGRAPH = new ArrayList<List<List<String>>>();
	private Map<String, String> DOC_TOKEN_NATURE_MAP = new HashMap<String, String>(); /* 保存文章的词和词性的集合 */
	double docLength = 0.0;

	public AbstractKeyWordExtraction(TokenizerFactory tokenizerFactory) {
		this.tokenizerFactory = tokenizerFactory;
	}

	@Override
	public List<String> keywordsExtract(String title, String article, int keywordNum) {
		List<ScoredObject<String>> sortedList = keywordsScore(title, article, keywordNum);

		/* 是否启动词组合 */
		if (enableWordAssemble) {
			String text = RegexUtils.cleanSpecialWord(title + "," + article);
			for (String token : TOKENIZER_FACTORY_SPLIT_SENTS.tokenizer(text.toCharArray(), 0, text.length())) {
				if (token.equals("")) {
					continue;
				}
				DOC_TOKEN_NATURE_MAP.put(token.split(Contants.SLASH)[0], token.split(Contants.SLASH)[1]);
			}
			ObjectToDoubleMap<String> assembleCandiate = wordAssemble(sortedList, PARAGRAPH);
			sortedList = assembleCandiate.scoredObjectsOrderedByValueList();
		}

		List<String> keywords = new ArrayList<String>();
		int num = 0;
		for (ScoredObject<String> scoreObject : sortedList) {
			if (num >= keywordNum) {
				break;
			}
			keywords.add(scoreObject.getObject());
			num++;
		}
		return keywords;
	}

	@Override
	public List<ScoredObject<String>> keywordsScore(String title, String article, int keywordNum) {
		DOC_TOKEN_NATURE_MAP.clear();
		/* 分句 */
		PARAGRAPH = spiltSentence(title, article);
		List<String> titleTokens = new ArrayList<String>();
		List<String> bodyTokens = new ArrayList<String>();
		for (String term : tokenizerFactory.tokenizer(title.toCharArray(), 0, title.length())) {
			if (term.split("/")[0].length() <= 1) {
				continue;
			}
			titleTokens.add(term);
		}
		for (String term : tokenizerFactory.tokenizer(article.toCharArray(), 0, article.length())) {
			if (term.split("/")[0].length() <= 1) {
				continue;
			}
			bodyTokens.add(term);
		}
		ObjectToDoubleMap<String> sortedKeywordMap = modifyKeywordsSort(titleTokens, bodyTokens);
		List<ScoredObject<String>> sortedList = sortedKeywordMap.scoredObjectsOrderedByValueList();
		return sortedList.subList(0, Math.min(sortedList.size(), keywordNum));
	}

	/**
	 * 词组合
	 * 
	 * @param sortedList
	 * @param paragraph
	 */
	private ObjectToDoubleMap<String> wordAssemble(List<ScoredObject<String>> sortedList,
			List<List<List<String>>> paragraph) {
		Map<String, Double> map = new HashMap<String, Double>();
		for (int i = 0; i < 20 && i < sortedList.size(); i++) {
			map.put(sortedList.get(i).getObject(), sortedList.get(i).score());
		}
		ObjectToDoubleMap<String> candidate = new ObjectToDoubleMap<String>();
		int paragraphSize = paragraph.size();
		if (paragraphSize > 4) {
			/* 标题 */
			for (List<String> sent : paragraph.get(0)) {
				wordAssembleRule(sent, map, candidate);
			}

			/* 第一段 */
			for (List<String> sent : paragraph.get(1)) {
				wordAssembleRule(sent, map, candidate);
			}

			/* 第二段 */
			for (List<String> sent : paragraph.get(2)) {
				wordAssembleRule(sent, map, candidate);
			}

			/* 最后一段 */
			for (List<String> sent : paragraph.get(paragraph.size() - 1)) {
				wordAssembleRule(sent, map, candidate);
			}
		} else {
			for (List<List<String>> para : paragraph) {
				for (List<String> sent : para) {
					wordAssembleRule(sent, map, candidate);
				}
			}
		}
		sortedList.addAll(candidate.scoredObjectsOrderedByValueList());
		ObjectToDoubleMap<String> combineResult = new ObjectToDoubleMap<>();
		subTokenCombine(combineResult,sortedList);
		return combineResult;
	}


	/**
	 * 子词合并
	 * @param combineResult
	 * @param sortedList
	 */
	private void subTokenCombine(ObjectToDoubleMap<String> combineResult, List<ScoredObject<String>> sortedList) {
		for (int i = 0; i < sortedList.size(); i++) {
			int j = 0;
			for (j = i; j < sortedList.size(); j++) {
				if (i == j) {
					continue;
				}
				/*j包含i*/
				if (sortedList.get(j).getObject().indexOf(sortedList.get(i).getObject()) != -1) {
					break;
				}
			}
			if(j >= sortedList.size()) {
				if(isContantsSubString(sortedList.get(i).getObject(),combineResult)) {
					continue;
				} else {
					combineResult.put(sortedList.get(i).getObject(), sortedList.get(i).score());
				}
			}
		}
		
	}

	private boolean isContantsSubString(String object, ObjectToDoubleMap<String> combineResult) {
		for(String word : combineResult.keySet()) {
			if(word.indexOf(object)!=-1) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 根据词性规则组合词
	 * 
	 * @param sent
	 * @param map
	 * @param candidate
	 */
	private void wordAssembleRule(List<String> sent, Map<String, Double> map, ObjectToDoubleMap<String> candidate) {
		if (sent.size() < 3) {
			return;
		}
		for (int i = 0; i < sent.size(); i++) {
			String now = sent.get(i);
			String now_nature = DOC_TOKEN_NATURE_MAP.get(now);
			if (map.get(now) == null || now_nature == null || now_nature.equals("")) {
				continue;
			}
			if (i == 0) {
				String after = sent.get(i + 1);
				String after_nature = DOC_TOKEN_NATURE_MAP.get(after);
				if (null == after_nature || after.equals("")) {
					continue;
				}
				if ((now_nature.charAt(0) == 'a' && after_nature.charAt(0) == 'n')
						|| (now_nature.charAt(0) == 'n' && after_nature.charAt(0) == 'n')) {
					if (map.get(after) == null) {
						candidate.increment(now + after, map.get(now));
					} else {
						candidate.increment(now + after, map.get(now) + map.get(after));
					}
				}
				continue;
			}
			if (i == sent.size() - 1) {
				String before = sent.get(i - 1);
				String before_nature = DOC_TOKEN_NATURE_MAP.get(before);
				if (null == before_nature || before.equals("")) {
					continue;
				}
				if ((before_nature.charAt(0) == 'a' && now_nature.charAt(0) == 'n')
						|| (before_nature.charAt(0) == 'n' && now_nature.charAt(0) == 'n')) {
					if (map.get(before) == null) {
						candidate.increment(before + now, map.get(now));
					} else {
						candidate.increment(before + now, map.get(now) + map.get(before));
					}
				}
				continue;
			}
			String before = sent.get(i - 1);
			String before_nature = DOC_TOKEN_NATURE_MAP.get(before);
			String after = sent.get(i + 1);
			String after_nature = DOC_TOKEN_NATURE_MAP.get(after);
			if (null == before_nature || null == after_nature || before.equals("") || after.equals("")) {
				continue;
			}
			/* 两两组合 */
			if ((now_nature.charAt(0) == 'a' && after_nature.charAt(0) == 'n')
					|| (now_nature.charAt(0) == 'n' && after_nature.charAt(0) == 'n')) {
				if (map.get(after) == null) {
					candidate.increment(now + after, map.get(now));
				} else {
					candidate.increment(now + after, map.get(now) + map.get(after));
				}
			}

			if ((before_nature.charAt(0) == 'n' && now_nature.charAt(0) == 'n')
					|| (before_nature.charAt(0) == 'a' && now_nature.charAt(0) == 'n')) {
				if (map.get(before) == null) {
					candidate.increment(before + now, map.get(now));
				} else {
					candidate.increment(before + now, map.get(now) + map.get(before));
				}
			}

			/* 三个组合 */
			if ((before_nature.charAt(0) == 'n' && now_nature.charAt(0) == 'n' && after_nature.charAt(0) == 'n')
					|| (before_nature.charAt(0) == 'n' && now_nature.charAt(0) == 'a' && after_nature.charAt(0) == 'n')
					|| (before_nature.charAt(0) == 'v' && now_nature.charAt(0) == 'n' && after_nature.charAt(0) == 'n')
					|| (before_nature.charAt(0) == 'a' && now_nature.charAt(0) == 'n'
							&& after_nature.charAt(0) == 'v')) {
				if (map.get(before) != null && map.get(after) != null) {
					candidate.increment(before + now + after, map.get(before) + map.get(now) + map.get(after));
				}
				if (map.get(before) != null && map.get(after) == null) {
					candidate.increment(before + now, map.get(before) + map.get(now));
				}
				if (map.get(before) == null && map.get(after) != null) {
					candidate.increment(now + after, map.get(now) + map.get(after));
				}
			}

		}
	}

	/**
	 * 只有在包可用的，根据分好词的title和body，抽取关键词
	 * 
	 * @param titleTokens
	 * @param bodyTokens
	 * @param keywordNum
	 * @return
	 */
	protected List<String> keywordsExtract(List<String> titleTokens, List<String> bodyTokens, int keywordNum) {
		List<String> keywords = new ArrayList<String>();
		ObjectToDoubleMap<String> sortedKeywordMap = modifyKeywordsSort(titleTokens, bodyTokens);
		List<ScoredObject<String>> sortedList = sortedKeywordMap.scoredObjectsOrderedByValueList();
		int num = 0;
		for (ScoredObject<String> scoreObject : sortedList) {
			if (num >= keywordNum) {
				break;
			}
			keywords.add(scoreObject.getObject());
			num++;
		}
		return keywords;
	}

	/**
	 * 只有在包可用的，根据分好词的title和body，抽取关键词和得分
	 * 
	 * @param titleTokens
	 * @param bodyTokens
	 * @param keywordNum
	 * @return
	 */
	protected List<ScoredObject<String>> keywordsScore(List<String> titleTokens, List<String> bodyTokens) {
		ObjectToDoubleMap<String> sortedKeywordMap = modifyKeywordsSort(titleTokens, bodyTokens);
		List<ScoredObject<String>> sortedList = sortedKeywordMap.scoredObjectsOrderedByValueList();
		return sortedList.subList(0, sortedList.size());
	}

	/**
	 * 分段分句分词
	 * 
	 * @param document
	 * @return
	 */
	private List<List<List<String>>> spiltSentence(String title, String document) {
		List<List<List<String>>> paragraph = new ArrayList<List<List<String>>>();
		List<String> titleToken = new ArrayList<String>();
		for (String token : TOKENIZER_FACTORY_SPLIT_SENTS.tokenizer(title.toCharArray(), 0, title.length())) {
			titleToken.add(token.split(Contants.SLASH)[0]);
			DOC_TOKEN_NATURE_MAP.put(token.split(Contants.SLASH)[0], token.split(Contants.SLASH)[1]);
		}
		List<List<String>> titlePara = new ArrayList<List<String>>();
		titlePara.add(titleToken);
		paragraph.add(titlePara);
		for (String line : document.split(Contants.PARAGRAPHFLAG)) {
			if (line.equals("")) {
				continue;
			}
			List<List<String>> sentences = new ArrayList<List<String>>();
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
				List<String> sbSents = new ArrayList<>();
				for (int j = sentStartTok; j <= sentEndTok; j++) {
					sbSents.add(tokens[j]);
				}
				sentStartTok = sentEndTok + 1;
				sentences.add(sbSents);
			}
			paragraph.add(sentences);
		}
		return paragraph;
	}

	/**
	 * 启动关键词的组合功能
	 * 
	 * @param enable
	 * @return
	 */
	public AbstractKeyWordExtraction enableWordAssemble(boolean enable) {
		if (enable) {
			enableWordAssemble = true;
		} else {
			enableWordAssemble = false;
		}
		return this;
	}

	/**
	 * 倒序比较器
	 */
	private static Comparator<ScoredObject<String>> dascComparator = new Comparator<ScoredObject<String>>() {
		@Override
		public int compare(ScoredObject<String> o1, ScoredObject<String> o2) {
			if (Math.abs(o1.score() - o2.score()) <= 0) {
				return 0;
			} else {
				if (o1.score() - o2.score() < 0.0) {
					return 1;
				} else {
					return -1;
				}
			}
		}
	};

	/**
	 * 关键词抽取的方法
	 * 
	 * @param titleTokens
	 *            标题分词列表
	 * @param bodyTokens
	 *            正文分词列表
	 * @return
	 */
	abstract ObjectToDoubleMap<String> modifyKeywordsSort(List<String> titleTokens, List<String> bodyTokens);
}
