package task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ScoredObject;
import com.rouchtime.nlp.corpus.GuojiCorpus;
import com.rouchtime.nlp.corpus.SougouCateCorpus;
import com.rouchtime.nlp.featureSelection.selector.CategoryDiscriminatingFeatureSelector;
import com.rouchtime.nlp.featureSelection.selector.ChiFeatureSelector;
import com.rouchtime.nlp.featureSelection.selector.DocumentFrequencyFeatureSelector;
import com.rouchtime.nlp.featureSelection.selector.IGFeatureSelector;
import com.rouchtime.util.Contants;
import com.rouchtime.util.RegexUtils;
import com.rouchtime.util.WekaUtils;

import tokenizer.HanLPTokenizerFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

public class GuojiFeatureSelect {

	
	public static void cdh(int start, int end, int para, String outputPath, List<Pair<String, String>> trainSet,
			TokenizerFactory factory) throws IOException {
		CategoryDiscriminatingFeatureSelector cDFeatureSelector = new CategoryDiscriminatingFeatureSelector(trainSet,
				factory);
		for (int i = start; i <= end; i++) {
			Set<String> dic = new HashSet<String>();
			List<ScoredObject<String>> list = cDFeatureSelector.getCategoryDiscrimination(para * i);
			for (ScoredObject<String> score : list) {
				dic.add(score.getObject());
			}
			FileUtils.write(new File(outputPath + "//cdh//guoji_cdh_" + i + ".arff"), Contants.GUOJIHEAD(i), "utf-8",
					true);
			for (Pair<String, String> pair : trainSet) {
				String raw = pair.getLeft();
				String wekaText = WekaUtils.formWekaArffTextFromRawAndDic(RegexUtils.cleanSpecialWord(raw), factory,
						pair.getRight(), dic);
				FileUtils.write(new File(outputPath + "//cdh//guoji_cdh_" + i + ".arff"), wekaText, "utf-8", true);
			}

		}
	}

	public static void chi(int start, int end, int para, String outputPath, List<Pair<String, String>> trainSet,
			TokenizerFactory factory) throws IOException {
		ChiFeatureSelector chiSelector = new ChiFeatureSelector(trainSet, factory);
		for (int i = start; i <= end; i++) {
			Set<String> dic = new HashSet<String>();
			List<ScoredObject<String>> list = chiSelector.getCHI(para * i);
			for (ScoredObject<String> score : list) {
				dic.add(score.getObject());
			}
			FileUtils.write(new File(outputPath + "//chi//guoji_chi_" + i + ".arff"), Contants.GUOJIHEAD(i), "utf-8",
					true);
			for (Pair<String, String> pair : trainSet) {
				String raw = pair.getLeft();
				String wekaText = WekaUtils.formWekaArffTextFromRawAndDic(RegexUtils.cleanSpecialWord(raw), factory,
						pair.getRight(), dic);
				FileUtils.write(new File(outputPath + "//chi//guoji_chi_" + i + ".arff"), wekaText, "utf-8", true);
			}

		}
	}

	public static void ig(int start, int end, int para, String outputPath, List<Pair<String, String>> trainSet,
			TokenizerFactory factory) throws IOException {
		IGFeatureSelector igSelector = new IGFeatureSelector(trainSet, factory);
		for (int i = start; i <= end; i++) {
			Set<String> dic = new HashSet<String>();
			List<ScoredObject<String>> list = igSelector.getInformationGainByDF(i * para);
			for (ScoredObject<String> score : list) {
				dic.add(score.getObject());
			}
			FileUtils.write(new File(outputPath + "//ig//guoji_ig_" + i + ".arff"), Contants.GUOJIHEAD(i), "utf-8",
					true);
			for (Pair<String, String> pair : trainSet) {
				String raw = pair.getLeft();
				String wekaText = WekaUtils.formWekaArffTextFromRawAndDic(RegexUtils.cleanSpecialWord(raw), factory,
						pair.getRight(), dic);
				FileUtils.write(new File(outputPath + "//ig//guoji_ig_" + i + ".arff"), wekaText, "utf-8", true);
			}

		}
	}

	public static void df(int start, int end, int para, String outputPath, List<Pair<String, String>> trainSet,
			TokenizerFactory factory) throws IOException {
		DocumentFrequencyFeatureSelector dfSelector = new DocumentFrequencyFeatureSelector(trainSet, factory);
		for (int i = start; i <= end; i++) {
			Set<String> dic = new HashSet<String>();
			List<ScoredObject<String>> list = dfSelector.getDocumentFrequency(i * para);
			for (ScoredObject<String> score : list) {
				dic.add(score.getObject());
			}
			FileUtils.write(new File(outputPath + "//df//guoji_df_" + i + ".arff"), Contants.GUOJIHEAD(i), "utf-8",
					true);
			for (Pair<String, String> pair : trainSet) {
				String raw = pair.getLeft();
				String wekaText = WekaUtils.formWekaArffTextFromRawAndDic(RegexUtils.cleanSpecialWord(raw), factory,
						pair.getRight(), dic);
				FileUtils.write(new File(outputPath + "//df//guoji_df_" + i + ".arff"), wekaText, "utf-8", true);
			}

		}
	}

	public static List<Pair<String, String>> formTrainAndPrintTest(String outputPath, TokenizerFactory factory,
			int size) throws IOException {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring-mybatis.xml");
		GuojiCorpus guojiCorpus = (GuojiCorpus) applicationContext.getBean(GuojiCorpus.class);
		Map<String, List<String>> trainAndTest = guojiCorpus.produceTrainAndTestByRate(0.7, 10l);
		List<Pair<String, String>> trainPair = new ArrayList<Pair<String, String>>();
		for (String fileid : trainAndTest.get("train")) {
			ImmutablePair<String, String> pair = new ImmutablePair<String, String>(guojiCorpus.rawFromfileids(fileid),
					guojiCorpus.labelFromfileid(fileid));
			trainPair.add(pair);
		}
		FileUtils.write(new File(outputPath + "//test.arff"), Contants.GUOJIHEAD("test"), "utf-8", true);
		for (String fileid : trainAndTest.get("test")) {
			String wekaText = WekaUtils.formWekaArffTextFromRaw(
					RegexUtils.cleanSpecialWord(guojiCorpus.rawFromfileids(fileid)), factory,
					guojiCorpus.labelFromfileid(fileid));
			FileUtils.write(new File(outputPath + "//test.arff"), wekaText, "utf-8", true);
		}
		for (int i = 1; i <= size; i++) {
			try {
				forJava(new File(outputPath + "//test.arff"), new File(outputPath + "//test//" + i + "_test.arff"));
			} catch (Exception e) {
				System.err.println("copy error");
			}
		}
		return trainPair;
	}

	public static void forJava(File f1, File f2) throws Exception {
		int length = 2097152;
		FileInputStream in = new FileInputStream(f1);
		FileOutputStream out = new FileOutputStream(f2);
		byte[] buffer = new byte[length];
		while (true) {
			int ins = in.read(buffer);
			if (ins == -1) {
				in.close();
				out.flush();
				out.close();
				return;
			} else
				out.write(buffer, 0, ins);
		}
	}

	public static void main(String[] args) throws IOException {
		StopWordTokenierFactory stopWordFactory = new StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		StopNatureTokenizerFactory stopNatureTokenizerFactory = new StopNatureTokenizerFactory(stopWordFactory);
		// NGramTokenizerBasedOtherTokenizerFactory factory = new
		// NGramTokenizerBasedOtherTokenizerFactory(
		// stopNatureTokenizerFactory, 1, 2);
		// NGramTokenizerFactory factory = new NGramTokenizerFactory(2,2);
		List<Pair<String, String>> train = formTrainAndPrintTest("D://weka", stopNatureTokenizerFactory,6);
		ig(1, 6, 500, "D://weka", train, stopNatureTokenizerFactory);
		chi(1, 6, 500, "D://weka", train, stopNatureTokenizerFactory);
		cdh(1, 6, 500, "D://weka", train, stopNatureTokenizerFactory);
		df(1, 6, 500, "D://weka", train, stopNatureTokenizerFactory);
	}
}
