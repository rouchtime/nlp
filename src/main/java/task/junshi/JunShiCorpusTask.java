package task.junshi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ScoredObject;
import com.rouchtime.nlp.corpus.ClassificationCorpus;
import com.rouchtime.nlp.featureSelection.bean.FeatureSelectionBean;
import com.rouchtime.nlp.featureSelection.selector.CategoryDiscriminatingFeatureSelector;
import com.rouchtime.nlp.featureSelection.selector.ChiFeatureSelector;
import com.rouchtime.nlp.featureSelection.selector.DocumentFrequencyFeatureSelector;
import com.rouchtime.nlp.featureSelection.selector.IGFeatureSelector;
import com.rouchtime.util.CommonUtils;
import com.rouchtime.util.Contants;
import com.rouchtime.util.RegexUtils;
import com.rouchtime.util.WekaUtils;

import tokenizer.HanLPTokenizerFactory;
import tokenizer.JiebaTokenizerFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

public class JunShiCorpusTask {
	private static ClassificationCorpus corpus;
	private static String JunShiDir = "D:\\";
	private static TokenizerFactory tokenFactory;
	private static List<FeatureSelectionBean> trainlist = new ArrayList<FeatureSelectionBean>();
	private static List<FeatureSelectionBean> testlist = new ArrayList<FeatureSelectionBean>();
	private static String outpath = "D:\\corpus\\category\\weka\\junshi";
	static {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring-mybatis.xml");
		corpus = (ClassificationCorpus) applicationContext.getBean(ClassificationCorpus.class);
		tokenFactory = getTokenFactory();
	}

	public static void outputDir() throws IOException {
		for (String secondlabel : corpus.secondlabelsFromFirstlabel("junshi")) {
			for (String label : corpus.labelsFromSecondlabel(secondlabel)) {
				for (String fileid : corpus.fileidFromLabel(label)) {
					try {
						FileUtils.write(
								new File(JunShiDir, String.format("\\%s\\%s\\%s", "junshi", secondlabel, label)),
								String.format("%s/t\n", corpus.rawFromfileids(fileid)), "utf-8", true);
					} catch (Exception e) {
						System.out.println(fileid);
						continue;
					}
				}
			}
		}
	}

	private static TokenizerFactory getTokenFactory() {
		StopWordTokenierFactory stopWordFactory = new StopWordTokenierFactory(JiebaTokenizerFactory.getIstance());
		StopNatureTokenizerFactory stopNatureTokenizerFactory = new StopNatureTokenizerFactory(stopWordFactory);
		return stopNatureTokenizerFactory;
	}

	public static void seocndLabelDfExtract() throws IOException {
		List<FeatureSelectionBean> list = new ArrayList<FeatureSelectionBean>();
		for (String secondlabel : corpus.secondlabelsFromFirstlabel("junshi")) {
			for (String label : corpus.labelsFromSecondlabel(secondlabel)) {
				for (String fileid : corpus.fileidFromLabel(label)) {
					FeatureSelectionBean fsb = new FeatureSelectionBean();
					fsb.setLabel(secondlabel);
					StringBuffer sb = new StringBuffer();
					sb.append(corpus.titleFromfileid(fileid)).append(",").append(corpus.titleFromfileid(fileid))
							.append(",").append(corpus.titleFromfileid(fileid)).append(",")
							.append(corpus.rawFromfileids(fileid));
					fsb.setRaw(sb.toString());
					list.add(fsb);
				}
			}
		}
		StopWordTokenierFactory stopWordFactory = new StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		StopNatureTokenizerFactory stopNatureTokenizerFactory = new StopNatureTokenizerFactory(stopWordFactory);
		DocumentFrequencyFeatureSelector dfSelector = new DocumentFrequencyFeatureSelector(list,
				stopNatureTokenizerFactory);
		// Set<String> dic = new HashSet<String>();
		// List<ScoredObject<String>> list1 = dfSelector.getDocumentFrequency(1000);
		// for (ScoredObject<String> score : list1) {
		// dic.add(score.getObject());
		// }
		Set<String> dic = new HashSet<String>();
		for (String label : corpus.secondLabels()) {
			Map<String, Double> map = dfSelector.getDocumentFrequencyByLabel(label, 2000);
			for (String word : map.keySet()) {
				dic.add(word);
			}
		}
		System.out.println(dic.size());
		FileUtils.write(new File("D://junshi_second_cat.arff"), Contants.JUNSHISECONDLABEL(0), "utf-8", true);
		for (FeatureSelectionBean fsb : list) {
			String raw = fsb.getRaw();
			String wekaText = WekaUtils.formWekaArffTextFromRawAndDic(RegexUtils.cleanSpecialWord(raw),
					stopNatureTokenizerFactory, fsb.getLabel(), dic);
			FileUtils.write(new File("D://junshi_second_cat.arff"), wekaText, "utf-8", true);
		}

	}

	public static void df_per() throws IOException {
		List<FeatureSelectionBean> list = new ArrayList<FeatureSelectionBean>();
		for (String secondlabel : corpus.secondlabelsFromFirstlabel("junshi")) {
			for (String label : corpus.labelsFromSecondlabel(secondlabel)) {
				for (String fileid : corpus.fileidFromLabel(label)) {
					FeatureSelectionBean fsb = new FeatureSelectionBean();
					fsb.setLabel(secondlabel);
					StringBuffer sb = new StringBuffer();
					sb.append(corpus.titleFromfileid(fileid)).append(",").append(corpus.titleFromfileid(fileid))
							.append(",").append(corpus.titleFromfileid(fileid)).append(",")
							.append(corpus.rawFromfileids(fileid));
					fsb.setRaw(sb.toString());
					list.add(fsb);
				}
			}
		}
		StopWordTokenierFactory stopWordFactory = new StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		StopNatureTokenizerFactory stopNatureTokenizerFactory = new StopNatureTokenizerFactory(stopWordFactory);
		DocumentFrequencyFeatureSelector dfSelector = new DocumentFrequencyFeatureSelector(list,
				stopNatureTokenizerFactory);

		for (String label : corpus.secondLabels()) {
			Map<String, Double> map = dfSelector.getDocumentFrequencyByLabel(label, 10000);
			for (String word : map.keySet()) {
				FileUtils.write(new File("D://junshi_per_cate//" + label), String.format("%s\n", word), "utf-8", true);
			}
		}
	}

	public static void xiuzheng() throws IOException {
		List<String> lines = FileUtils.readLines(new File("D://导弹"));
		for (String line : lines) {
			String[] splits = line.split("\t");
			if (splits[1].indexOf("核") != -1 || splits[1].indexOf("东风") != -1) {
				FileUtils.write(new File("D://he"), line + "\n", "utf-8", true);
			} else {
				FileUtils.write(new File("D://dao"), line + "\n", "utf-8", true);
			}
		}
	}

	public static void df(int start, int end, int para) throws IOException {
		DocumentFrequencyFeatureSelector dfSelector = new DocumentFrequencyFeatureSelector(trainlist, tokenFactory);

		for (int i = start; i <= end; i++) {
			Set<String> dic = new HashSet<String>();
			List<ScoredObject<String>> list = dfSelector.getDocumentFrequency(i * para);
			for (ScoredObject<String> score : list) {
				dic.add(score.getObject());
			}
			FileUtils.write(new File(outpath + "//df//df_" + i + ".arff"), Contants.JUNSHITHIRDLABEL(i), "utf-8", true);
			for (FeatureSelectionBean pair : trainlist) {
				String raw = pair.getRaw();
				String wekaText = WekaUtils.formWekaArffTextFromRawAndDic(RegexUtils.cleanSpecialWord(raw),
						tokenFactory, pair.getLabel(), dic);
				FileUtils.write(new File(outpath + "//df//df_" + i + ".arff"), wekaText, "utf-8", true);
			}

		}
	}

	public static void cdh(int start, int end, int para) throws IOException {
		CategoryDiscriminatingFeatureSelector cDFeatureSelector = new CategoryDiscriminatingFeatureSelector(trainlist,
				tokenFactory);
		for (int i = start; i <= end; i++) {
			Set<String> dic = new HashSet<String>();
			List<ScoredObject<String>> list = cDFeatureSelector.getCategoryDiscrimination(para * i);
			for (ScoredObject<String> score : list) {
				dic.add(score.getObject());
			}
			FileUtils.write(new File(outpath + "//cdh//cdh_" + i + ".arff"), Contants.JUNSHITHIRDLABEL(i), "utf-8",
					true);
			for (FeatureSelectionBean pair : trainlist) {
				String raw = pair.getRaw();
				String wekaText = WekaUtils.formWekaArffTextFromRawAndDic(RegexUtils.cleanSpecialWord(raw),
						tokenFactory, pair.getLabel(), dic);
				FileUtils.write(new File(outpath + "//cdh//cdh_" + i + ".arff"), wekaText, "utf-8", true);
			}

		}
	}

	public static void chi(int start, int end, int para) throws IOException {
		ChiFeatureSelector chiSelector = new ChiFeatureSelector(trainlist, tokenFactory);
		for (int i = start; i <= end; i++) {
			Set<String> dic = new HashSet<String>();
			List<ScoredObject<String>> list = chiSelector.getCHI(para * i);
			for (ScoredObject<String> score : list) {
				dic.add(score.getObject());
			}
			FileUtils.write(new File(outpath + "//chi//chi_" + i + ".arff"), Contants.JUNSHITHIRDLABEL(i), "utf-8",
					true);
			for (FeatureSelectionBean pair : trainlist) {
				String raw = pair.getRaw();
				String wekaText = WekaUtils.formWekaArffTextFromRawAndDic(RegexUtils.cleanSpecialWord(raw),
						tokenFactory, pair.getLabel(), dic);
				FileUtils.write(new File(outpath + "//chi//chi_" + i + ".arff"), wekaText, "utf-8", true);
			}

		}
	}

	public static void ig(int start, int end, int para) throws IOException {
		IGFeatureSelector igSelector = new IGFeatureSelector(trainlist, tokenFactory);
		for (int i = start; i <= end; i++) {
			Set<String> dic = new HashSet<String>();
			List<ScoredObject<String>> list = igSelector.getInformationGainByDF(i * para);
			for (ScoredObject<String> score : list) {
				dic.add(score.getObject());
			}
			FileUtils.write(new File(outpath + "//ig//ig_" + i + ".arff"), Contants.JUNSHITHIRDLABEL(i), "utf-8", true);
			for (FeatureSelectionBean pair : trainlist) {
				String raw = pair.getRaw();
				String wekaText = WekaUtils.formWekaArffTextFromRawAndDic(RegexUtils.cleanSpecialWord(raw),
						tokenFactory, pair.getLabel(), dic);
				FileUtils.write(new File(outpath + "//ig//ig_" + i + ".arff"), wekaText, "utf-8", true);
			}

		}
	}

	public static void df_per_category(int start, int end, int para) throws IOException {
		DocumentFrequencyFeatureSelector dfSelector = new DocumentFrequencyFeatureSelector(trainlist, tokenFactory);
		for (int i = start; i <= end; i++) {
			Set<String> dic = new HashSet<String>();
			for (String label : corpus.thridlabelsFromFirstlabel("junshi")) {
				Map<String, Double> map = dfSelector.getDocumentFrequencyByLabel(label, i * para);
				for (String word : map.keySet()) {
					dic.add(word);
				}
			}
			System.out.println(dic.size());
			FileUtils.write(new File(outpath + "//df_per_category//df_per_category" + i + ".arff"),
					Contants.JUNSHITHIRDLABEL(i), "utf-8", true);
			for (FeatureSelectionBean pair : trainlist) {
				String raw = pair.getRaw();
				String wekaText = WekaUtils.formWekaArffTextFromRawAndDic(RegexUtils.cleanSpecialWord(raw),
						tokenFactory, pair.getLabel(), dic);
				FileUtils.write(new File(outpath + "//df_per_category//df_per_category" + i + ".arff"), wekaText,
						"utf-8", true);
			}

		}
	}

	public static void formTestSet(int size) throws IOException {
		FileUtils.write(new File(outpath + "//test.arff"), Contants.JUNSHITHIRDLABEL("test"), "utf-8", true);
		for (FeatureSelectionBean fsb : testlist) {
			String wekaText = WekaUtils.formWekaArffTextFromRaw(RegexUtils.cleanSpecialWord(fsb.getRaw()), tokenFactory,
					fsb.getLabel());
			FileUtils.write(new File(outpath + "//test.arff"), wekaText, "utf-8", true);
		}
		for (int i = 1; i <= size; i++) {
			try {
				forJava(new File(outpath + "//test.arff"), new File(outpath + "//test//" + i + "_test.arff"));
			} catch (Exception e) {
				System.err.println("copy error");
			}
		}
	}

	private static void forJava(File f1, File f2) throws Exception {
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

	public static void extraCorpusPerCate(int num, double rate) throws IOException {
		Set<String> labels = corpus.thridlabelsFromFirstlabel("junshi");
		for (String label : labels) {
			Set<Integer> extraIndex = new HashSet<Integer>();
			List<String> fileids = corpus.fileidFromThirLabelAndFirstLabel(label, "junshi");
			Random random = new Random(100l);
			while (extraIndex.size() < num && extraIndex.size() < fileids.size()) {
				int index = random.nextInt(fileids.size());
				if (extraIndex.contains(index)) {
					continue;
				} else {
					extraIndex.add(index);
				}
			}
			for (int i = 0; i < (int) (extraIndex.size() * rate); i++) {
				String raw = corpus.rawFromfileids(fileids.get(i));
				String title = corpus.titleFromfileid(fileids.get(i));
				String secondLabel = corpus.secondLabelByFileid(fileids.get(i));
				FeatureSelectionBean fsb = new FeatureSelectionBean();
				fsb.setRaw(CommonUtils.jointMultipleTitleAndRaw(3, title, raw).toString());
				fsb.setLabel(label);
				trainlist.add(fsb);
			}
			for (int i = (int) (extraIndex.size() * rate) + 1; i < extraIndex.size(); i++) {
				String raw = corpus.rawFromfileids(fileids.get(i));
				String secondLabel = corpus.secondLabelByFileid(fileids.get(i));
				String title = corpus.titleFromfileid(fileids.get(i));
				FeatureSelectionBean fsb = new FeatureSelectionBean();
				fsb.setRaw(CommonUtils.jointMultipleTitleAndRaw(3, title, raw).toString());
				fsb.setLabel(label);
				testlist.add(fsb);
			}
		}

	}

	public static void main(String[] args) throws IOException {
		extraCorpusPerCate(500, 1);
		formTestSet(6);
		df(5, 5, 5000);
		// df_per_category(1,6,600);
		// chi(1, 6, 5000);
		// cdh(1,6,5000);
		// ig(1,6,5000);
		// xiuzheng();

		// df_per();
	}

}
