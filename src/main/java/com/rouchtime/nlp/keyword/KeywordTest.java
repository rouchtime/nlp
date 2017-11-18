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
		// for(String line :FileUtils.readLines(new
		// File("D:\\corpus\\test\\stopwords.txt"), "utf-8") ) {
		// if(line.matches("[\\u4e00-\\u9fa5]+")) {
		// FileUtils.write(new File("D:\\corpus\\test\\chinese.txt"),
		// line+"\n","utf-8",true);
		// } else {
		// FileUtils.write(new File("D:\\corpus\\test\\nonChinese.txt"),
		// line+"\n","utf-8",true);
		// }
		// }
		ObjectToDoubleMap<String> tf = new ObjectToDoubleMap<String>();
		ObjectToDoubleMap<String> tfidf = new ObjectToDoubleMap<String>();
		File[] files = new File("D:\\corpus\\test\\appliment").listFiles();
		for (File file : files) {
			for (String line : FileUtils.readLines(file, "utf-8")) {
				String[] splits = line.split("\t+");
				if (splits.length <= 2) {
					continue;
				}
				String url = splits[0];
				String type = splits[1];
				StringBuffer sb = new StringBuffer();
				for (int i = 2; i < splits.length; i++) {
					sb.append(splits[i]);
				}
				String text = sb.toString();
				for (String token : tokenFactory.tokenizer(text.toCharArray(), 0, text.length())) {
					String word = token.split("/")[0];
					tf.increment(word, 1.0);
					Double idf = dr.getIDFMAP().get(word);
					if(idf== null) {
						tfidf.increment(word, 0.01 * 1);
					} else {
						tfidf.increment(word, idf);
					}
				}
				// IntegrateKeyWordExtraction keywordExtraction = (IntegrateKeyWordExtraction)
				// new IntegrateKeyWordExtraction(
				// tokenFactory,
				// Word2VectorWordSimiarity.getInstance()).enableWordAssemble(false);
				// List<String> words = keywordExtraction.keywordsExtract("", text, 5);
				// sb.delete(0, sb.length());
				// for (String word : words) {
				// sb.append(word).append(",");
				// }
				// FileUtils.write(new File("D:\\corpus\\test\\applimentbiaoqian\\" +
				// file.getName()),
				// String.format("%s\t%s\t%s\t%s\n", url, type, text, sb.toString()), "utf-8",
				// true);
			}
		}


		List<ScoredObject<String>> sortedTF = tf.scoredObjectsOrderedByValueList();
		List<ScoredObject<String>> sortedTFIDF = tfidf.scoredObjectsOrderedByValueList();
		for (int i = 0; i < 20000; i++) {
			FileUtils.write(new File("D://tf.txt"),
					String.format("%s\t%.2f\n", sortedTF.get(i).getObject(), sortedTF.get(i).score()), "utf-8", true);
			FileUtils.write(new File("D://tfidf.txt"),
					String.format("%s\t%.2f\n", sortedTFIDF.get(i).getObject(), sortedTFIDF.get(i).score()), "utf-8", true);
		}
	}
}
