package com.rouchtime.nlp.keyword.extraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.ScoredObject;
import com.hankcs.hanlp.seg.Segment;
import com.rouchtime.nlp.keyword.Token;
import com.rouchtime.nlp.sentence.ChineseSentenceModel;
import com.rouchtime.nlp.sentence.SummarizationSentenceModel;
import com.rouchtime.util.Contants;
import com.rouchtime.util.RegexUtils;

import tokenizer.AnsjTokenizerFactory;
import tokenizer.HanLPTokenizerFactory;

public abstract class AbstractKeyWordExtraction implements KeyWordExtraction {
	private Logger logger = Logger.getLogger(AbstractKeyWordExtraction.class);
	TokenizerFactory tokenizerFactory;
	private TokenizerFactory TOKENIZER_FACTORY_SPLIT_SENTS = AnsjTokenizerFactory.getIstance();
	private ChineseSentenceModel SENTENCE_MODEL = SummarizationSentenceModel.INSTANCE;
	private boolean enableWordAssemble = false;
	List<String> SENTENCES = new ArrayList<String>();
	double docLength = 0.0;

	public AbstractKeyWordExtraction(TokenizerFactory tokenizerFactory) {
		this.tokenizerFactory = tokenizerFactory;
	}

	@Override
	public List<String> keywordsExtract(String title, String article, int keywordNum) {
		/* 分句 */
		SENTENCES = spiltSentence(article);
		SENTENCES.add(title);
		List<String> titleTokens = new ArrayList<String>();
		List<String> bodyTokens = new ArrayList<String>();
		for (String term : tokenizerFactory.tokenizer(title.toCharArray(), 0, title.length())) {
			if(term.split("/")[0].length() <= 1) {
				continue;
			}
			titleTokens.add(term);
		}
		for (String term : tokenizerFactory.tokenizer(article.toCharArray(), 0, article.length())) {
			if(term.split("/")[0].length() <= 1) {
				continue;
			}
			bodyTokens.add(term);
		}
		ObjectToDoubleMap<String> sortedKeywordMap = modifyKeywordsSort(titleTokens, bodyTokens);
		List<ScoredObject<String>> sortedList = sortedKeywordMap.scoredObjectsOrderedByValueList();

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
	public List<ScoredObject<String>> keywordsScore(String title,String article,int keywordNum) {
		/* 分句 */
		SENTENCES = spiltSentence(article);
		SENTENCES.add(title);
		List<String> titleTokens = new ArrayList<String>();
		List<String> bodyTokens = new ArrayList<String>();
		for (String term : tokenizerFactory.tokenizer(title.toCharArray(), 0, title.length())) {
			if(term.split("/")[0].length() <= 1) {
				continue;
			}
			titleTokens.add(term);
		}
		for (String term : tokenizerFactory.tokenizer(article.toCharArray(), 0, article.length())) {
			if(term.split("/")[0].length() <= 1) {
				continue;
			}
			bodyTokens.add(term);
		}
		ObjectToDoubleMap<String> sortedKeywordMap = modifyKeywordsSort(titleTokens, bodyTokens);
		List<ScoredObject<String>> sortedList = sortedKeywordMap.scoredObjectsOrderedByValueList();
		return sortedList.subList(0, keywordNum);
	}
	

	/**
	 * 只有在包可用的，根据分好词的title和body，抽取关键词
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
	 * 分句
	 * 
	 * @param document
	 * @return
	 */
	private List<String> spiltSentence(String document) {
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
				logger.debug(String.format("Splits Sentence : %s\n", sbSents.toString()));
			}
		}
		return sentences;
	}

    public AbstractKeyWordExtraction enableMultithreading(boolean enable)
    {
    	if(enable) {
    		enableWordAssemble = true;
    	}
        return this;
    }
	
	/**
	 * 关键词抽取的方法
	 * @param titleTokens 标题分词列表
	 * @param bodyTokens 正文分词列表
	 * @return
	 */
	abstract ObjectToDoubleMap<String> modifyKeywordsSort(List<String> titleTokens, List<String> bodyTokens);
}
