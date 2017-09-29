package task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ScoredObject;
import com.rouchtime.nlp.corpus.ClassificationCorpus;
import com.rouchtime.nlp.featureSelection.bean.FeatureSelectionBean;
import com.rouchtime.nlp.featureSelection.selector.CategoryDiscriminatingFeatureSelector;
import com.rouchtime.nlp.featureSelection.selector.ChiFeatureSelector;
import com.rouchtime.nlp.featureSelection.selector.DocumentFrequencyFeatureSelector;
import com.rouchtime.nlp.featureSelection.selector.IGFeatureSelector;
import com.rouchtime.util.Contants;
import com.rouchtime.util.RegexUtils;
import com.rouchtime.util.WekaUtils;

import tokenizer.HanLPTokenizerFactory;
import tokenizer.JiebaTokenizerFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

public class GuojiFeatureSelect {

	private static ClassificationCorpus corpus;
	static {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring-mybatis.xml");
		corpus = (ClassificationCorpus) applicationContext.getBean(ClassificationCorpus.class);
	}

	public static void cdh(int start, int end, int para, String outputPath, List<FeatureSelectionBean> trainSet,
			TokenizerFactory factory) throws IOException {
		CategoryDiscriminatingFeatureSelector cDFeatureSelector = new CategoryDiscriminatingFeatureSelector(trainSet,
				factory);
		for (int i = start; i <= end; i++) {
			Set<String> dic = new HashSet<String>();
			List<ScoredObject<String>> list = cDFeatureSelector.getCategoryDiscrimination(para * i);
			for (ScoredObject<String> score : list) {
				dic.add(score.getObject());
			}
			FileUtils.write(new File(outputPath + "//cdh//cdh_" + i + ".arff"), Contants.JUNSHICHINESELABEL(i), "utf-8",
					true);
			for (FeatureSelectionBean pair : trainSet) {
				String raw = pair.getRaw();
				String wekaText = WekaUtils.formWekaArffTextFromRawAndDic(RegexUtils.cleanSpecialWord(raw), factory,
						pair.getLabel(), dic);
				FileUtils.write(new File(outputPath + "//cdh//cdh_" + i + ".arff"), wekaText, "utf-8", true);
			}

		}
	}

	public static void chi(int start, int end, int para, String outputPath, List<FeatureSelectionBean> trainSet,
			TokenizerFactory factory) throws IOException {
		ChiFeatureSelector chiSelector = new ChiFeatureSelector(trainSet, factory);
		for (int i = start; i <= end; i++) {
			Set<String> dic = new HashSet<String>();
			List<ScoredObject<String>> list = chiSelector.getCHI(para * i);
			for (ScoredObject<String> score : list) {
				dic.add(score.getObject());
			}
			FileUtils.write(new File(outputPath + "//chi//chi_" + i + ".arff"), Contants.JUNSHICHINESELABEL(i), "utf-8",
					true);
			for (FeatureSelectionBean pair : trainSet) {
				String raw = pair.getRaw();
				String wekaText = WekaUtils.formWekaArffTextFromRawAndDic(RegexUtils.cleanSpecialWord(raw), factory,
						pair.getLabel(), dic);
				FileUtils.write(new File(outputPath + "//chi//chi_" + i + ".arff"), wekaText, "utf-8", true);
			}

		}
	}

	public static void ig(int start, int end, int para, String outputPath, List<FeatureSelectionBean> trainSet,
			TokenizerFactory factory) throws IOException {
		IGFeatureSelector igSelector = new IGFeatureSelector(trainSet, factory);
		for (int i = start; i <= end; i++) {
			Set<String> dic = new HashSet<String>();
			List<ScoredObject<String>> list = igSelector.getInformationGainByDF(i * para);
			for (ScoredObject<String> score : list) {
				dic.add(score.getObject());
			}
			FileUtils.write(new File(outputPath + "//ig//ig_" + i + ".arff"), Contants.JUNSHICHINESELABEL(i), "utf-8",
					true);
			for (FeatureSelectionBean pair : trainSet) {
				String raw = pair.getRaw();
				String wekaText = WekaUtils.formWekaArffTextFromRawAndDic(RegexUtils.cleanSpecialWord(raw), factory,
						pair.getLabel(), dic);
				FileUtils.write(new File(outputPath + "//ig//ig_" + i + ".arff"), wekaText, "utf-8", true);
			}

		}
	}

	public static void df(int start, int end, int para, String outputPath, List<FeatureSelectionBean> trainSet,
			TokenizerFactory factory) throws IOException {
		DocumentFrequencyFeatureSelector dfSelector = new DocumentFrequencyFeatureSelector(trainSet, factory);
		for (int i = start; i <= end; i++) {
			Set<String> dic = new HashSet<String>();
			List<ScoredObject<String>> list = dfSelector.getDocumentFrequency(i * para);
			for (ScoredObject<String> score : list) {
				dic.add(score.getObject());
			}
			FileUtils.write(new File(outputPath + "//df//df_" + i + ".arff"), Contants.JUNSHICHINESELABEL(i), "utf-8",
					true);
			for (FeatureSelectionBean pair : trainSet) {
				String raw = pair.getRaw();
				String wekaText = WekaUtils.formWekaArffTextFromRawAndDic(RegexUtils.cleanSpecialWord(raw), factory,
						pair.getLabel(), dic);
				FileUtils.write(new File(outputPath + "//df//df_" + i + ".arff"), wekaText, "utf-8", true);
			}
		}
	}

	public static void df_per_category(int start, int end, int para, String outputPath,
			List<FeatureSelectionBean> trainSet, TokenizerFactory factory) throws IOException {
		DocumentFrequencyFeatureSelector dfSelector = new DocumentFrequencyFeatureSelector(trainSet, factory);
		for (int i = start; i <= end; i++) {
			Set<String> dic = new HashSet<String>();
			for (String label : corpus.labels()) {
				Map<String, Double> map = dfSelector.getDocumentFrequencyByLabel(label, i * para);
				for (String word : map.keySet()) {
					dic.add(word);
				}
			}
			System.out.println(dic.size());
			FileUtils.write(new File(outputPath + "//df_per_category//df_per_category" + i + ".arff"),
					Contants.JUNSHICHINESELABEL(i), "utf-8", true);
			for (FeatureSelectionBean pair : trainSet) {
				String raw = pair.getRaw();
				String wekaText = WekaUtils.formWekaArffTextFromRawAndDic(RegexUtils.cleanSpecialWord(raw), factory,
						pair.getLabel(), dic);
				FileUtils.write(new File(outputPath + "//df_per_category//df_per_category" + i + ".arff"), wekaText,
						"utf-8", true);
			}

		}
	}

	public static void dfByConditionLabel(int start, int end, int para, String outputPath,
			Set<String> conditionLabelSet, TokenizerFactory factory, List<FeatureSelectionBean> trainSet)
			throws IOException {
		CategoryDiscriminatingFeatureSelector cDFeatureSelector = new CategoryDiscriminatingFeatureSelector(trainSet,
				factory);
		for (int i = start; i <= end; i++) {
			Set<String> dic = new HashSet<String>();
			List<ScoredObject<String>> list = cDFeatureSelector.getCategoryDiscrimination(para * i);
			for (ScoredObject<String> score : list) {
				dic.add(score.getObject());
			}
			FileUtils.write(new File(outputPath + "//cdh//cdh_" + i + ".arff"), Contants.GUONEI(i), "utf-8", true);
			for (FeatureSelectionBean pair : trainSet) {
				String raw = pair.getRaw();
				String wekaText = WekaUtils.formWekaArffTextFromRawAndDic(RegexUtils.cleanSpecialWord(raw), factory,
						pair.getLabel(), dic);
				FileUtils.write(new File(outputPath + "//cdh//cdh_" + i + ".arff"), wekaText, "utf-8", true);
			}

		}
	}

	public static List<FeatureSelectionBean> formTrainAndPrintTest(String outputPath, TokenizerFactory factory,
			int size,String firstLabel,double rate,long seed) throws IOException {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring-mybatis.xml");
		ClassificationCorpus guojiCorpus = (ClassificationCorpus) applicationContext
				.getBean(ClassificationCorpus.class);
		Map<String, List<String>> trainAndTest = guojiCorpus.produceTrainAndTestByRateByLabel(rate, seed,firstLabel);
		List<FeatureSelectionBean> trainPair = new ArrayList<FeatureSelectionBean>();
		for (String fileid : trainAndTest.get("train")) {
			StringBuffer sb = new StringBuffer();
			String title = corpus.titleFromfileid(fileid);
			if (title != null) {
				sb.append(title).append(",").append(title).append(",").append(title).append(",")
						.append(corpus.rawFromfileids(fileid));
			} else {
				sb.append(corpus.rawFromfileids(fileid));
			}
			FeatureSelectionBean fsb = new FeatureSelectionBean();
			fsb.setRaw(sb.toString());
			fsb.setLabel(guojiCorpus.labelFromfileid(fileid));
			trainPair.add(fsb);
		}
		FileUtils.write(new File(outputPath + "//test.arff"), Contants.JUNSHICHINESELABEL("test"), "utf-8", true);
		for (String fileid : trainAndTest.get("test")) {
			StringBuffer sb = new StringBuffer();
			String title = corpus.titleFromfileid(fileid);
			if (title != null) {
				sb.append(title).append(",").append(title).append(",").append(title).append(",")
						.append(corpus.rawFromfileids(fileid));
			} else {
				sb.append(corpus.rawFromfileids(fileid));
			}
			String wekaText = WekaUtils.formWekaArffTextFromRaw(RegexUtils.cleanSpecialWord(sb.toString()), factory,
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

	/**
	 * 用DF提取各类别词汇
	 * 
	 * @throws IOException
	 */
	public static Set<String> DFWordEx(List<FeatureSelectionBean> trainSet, TokenizerFactory factory)
			throws IOException {
		DocumentFrequencyFeatureSelector df = new DocumentFrequencyFeatureSelector(trainSet, factory);
		Set<String> set = new HashSet<String>();
		for (String label : corpus.labels()) {
			Map<String, Double> map = df.getDocumentFrequencyByLabel(label, 1000);
			for (String word : map.keySet()) {
				set.add(word);
				// FileUtils.write(new File("D://corpus//category//guonei//dfWord//" + label +
				// ".txt"),
				// word + "\t\t" + map.get(word) + "\n", "utf-8", true);
			}
		}
		System.out.println(set.size());
		return set;
	}

	public static void dfByExtraDictionary(int start, int end, int para, String outputPath,
			List<FeatureSelectionBean> trainSet, TokenizerFactory factory, Set<String> dic) throws IOException {
		DocumentFrequencyFeatureSelector dfSelector = new DocumentFrequencyFeatureSelector(trainSet, factory);
		FileUtils.write(new File(outputPath + "//df//df_" + 1 + ".arff"), Contants.GUONEI(1), "utf-8", true);
		for (FeatureSelectionBean pair : trainSet) {
			String raw = pair.getRaw();
			String wekaText = WekaUtils.formWekaArffTextFromRawAndDic(RegexUtils.cleanSpecialWord(raw), factory,
					pair.getLabel(), dic);
			FileUtils.write(new File(outputPath + "//df//df_" + 1 + ".arff"), wekaText, "utf-8", true);
		}
	}

	public static List<Pair<String, String>> extract(String outpath, TokenizerFactory factory) throws IOException {
		List<Pair<String, String>> trainPair = new ArrayList<Pair<String, String>>();
		FileUtils.write(new File(outpath + "//test.arff"), Contants.GUONEISECONDLABEL("test"), "utf-8", true);
		for (String secondLabel : corpus.secondLabels()) {
			List<String> fileids = corpus.fileidFromSecondLabel(secondLabel);
			Random random = new Random(10l);
			int remainCount = (int) (fileids.size() * 0.7);
			for (int i = 0; i < remainCount; i++) {
				int selectIndex = random.nextInt(fileids.size());
				String fileid = fileids.get(selectIndex);
				fileids.remove(selectIndex);
				String raw = corpus.rawFromfileids(fileid);
				MutablePair<String, String> pair = new MutablePair<>(raw, secondLabel);
				trainPair.add(pair);
			}
			for (String fileid : fileids) {
				String raw = corpus.rawFromfileids(fileid);
				String wekaText = WekaUtils.formWekaArffTextFromRaw(RegexUtils.cleanSpecialWord(raw), factory,
						secondLabel);
				FileUtils.write(new File(outpath + "//test.arff"), wekaText, "utf-8", true);
			}
		}
		return trainPair;
	}

	public static void main(String[] args) throws IOException {
		// extract();
		StopWordTokenierFactory stopWordFactory = new StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		StopNatureTokenizerFactory stopNatureTokenizerFactory = new StopNatureTokenizerFactory(stopWordFactory);
		// String outpath = "D:\\corpus\\category\\weka\\guonei_top_level";
		// Set<String> setlabel = new HashSet<String>();
		// setlabel.add("地方发展");
		// setlabel.add("地方时政");
		// setlabel.add("基层工作");
		// setlabel.add("地方活动");
		// setlabel.add("公共基建");
		// dfByConditionLabel(1,6,5000,"D:\\corpus\\category\\weka\\guonei\\condition",setlabel,stopNatureTokenizerFactory);

		List<FeatureSelectionBean> train = formTrainAndPrintTest("D:\\corpus\\category\\weka\\junshi",
				stopNatureTokenizerFactory, 6,"junshi",0.7,100l);
//		 df(1, 6, 5000, "D:\\corpus\\category\\weka\\junshi", train,
//		 stopNatureTokenizerFactory);
//		 cdh(1, 6, 5000, "D:\\corpus\\category\\weka\\junshi", train,
//		 stopNatureTokenizerFactory);
//		 ig(1, 6, 5000, "D:\\corpus\\category\\weka\\junshi", train,
//		 stopNatureTokenizerFactory);
//		 chi(1, 6, 5000, "D:\\corpus\\category\\weka\\junshi", train,
//		 stopNatureTokenizerFactory);
//		df_per_category(1, 6, 600, "D:\\corpus\\category\\weka\\junshi", train, stopNatureTokenizerFactory);

		// List<Pair<String, String>> train = extract(outpath,
		// stopNatureTokenizerFactory);
		// df_per_category(1, 6, 2000, outpath, train, stopNatureTokenizerFactory);
		// df(1, 6, 5000, outpath, train, stopNatureTokenizerFactory);
		// chi(1, 6, 5000, outpath, train, stopNatureTokenizerFactory);
		// ig(1, 6, 5000, outpath, train, stopNatureTokenizerFactory);
		// cdh(1, 6, 5000, outpath, train, stopNatureTokenizerFactory);
	}
}
