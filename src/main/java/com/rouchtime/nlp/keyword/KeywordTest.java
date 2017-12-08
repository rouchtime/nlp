package com.rouchtime.nlp.keyword;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.ansj.recognition.impl.NatureRecognition;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.commons.io.FileUtils;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.ScoredObject;
import com.hankcs.hanlp.HanLP;
import com.rouchtime.nlp.keyword.extraction.BeforeTfIdfKeyWordExtraction;
import com.rouchtime.nlp.keyword.extraction.IntegrateKeyWordExtraction;
import com.rouchtime.nlp.keyword.extraction.TextRankWithMultiWinExtraction;
import com.rouchtime.nlp.keyword.similarity.Word2VectorWordSimiarity;
import com.rouchtime.util.RegexUtils;

import tokenizer.AnsjNlpTokenizerFactory;
import tokenizer.AnsjTokenizerFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

public class KeywordTest {

	static TokenizerFactory tokenFactory = getTokenFactory();
	private static DictionaryResource dr = DictionaryResource.getInstance();
	private static TokenizerFactory getTokenFactory() {
		StopWordTokenierFactory stopWordFactory = new StopWordTokenierFactory(AnsjTokenizerFactory.getIstance().enableFilterSingleWord(true));
		return stopWordFactory;
	}

	public static void main(String[] args) throws IOException {
	}
}
