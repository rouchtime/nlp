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
//		for(String line :FileUtils.readLines(new File("D:\\corpus\\test\\stopwords.txt"), "utf-8") ) {
//			if(line.matches("[\\u4e00-\\u9fa5]+")) {
//				FileUtils.write(new File("D:\\corpus\\test\\chinese.txt"), line+"\n","utf-8",true);
//			} else {
//				FileUtils.write(new File("D:\\corpus\\test\\nonChinese.txt"), line+"\n","utf-8",true);
//			}
//		}

		
		for (String line : FileUtils.readLines(new File("D:\\corpus\\keyword\\test.txt"), "utf-8")) {
			String title = line.split("\t+")[1];
			String raw = RegexUtils.cleanImgLabel(line.split("\t+")[2]);
			IntegrateKeyWordExtraction keywordExtraction = (IntegrateKeyWordExtraction) new IntegrateKeyWordExtraction(
					tokenFactory, Word2VectorWordSimiarity.getInstance()).enableWordAssemble(true);
			String integrateCombine = keywordExtraction.keywordsExtract(title, raw, 20).toString();
			keywordExtraction.enableWordAssemble(false);
			String integrateUncombine = keywordExtraction.keywordsExtract(title, raw, 20).toString();
			
			BeforeTfIdfKeyWordExtraction beforeTFidf = new BeforeTfIdfKeyWordExtraction(tokenFactory);
			String tfidf = beforeTFidf.keywordsExtract(title, raw, 20).toString();
			
			TextRankWithMultiWinExtraction textRankMulti = new TextRankWithMultiWinExtraction(2, 10, tokenFactory);
			String textrankMult = textRankMulti.keywordsExtract(title, raw, 20).toString();
			FileUtils.write(new File("D://corpus//test//result_compare"),
					String.format("%s\t\n组合：%s\n非组合：%s\n多窗口Textrank：%s\n线上的TFIDF：%s\n",line.split("\t+")[0], integrateCombine,integrateUncombine,textrankMult,tfidf), "utf-8", true);
		}
	}
}
