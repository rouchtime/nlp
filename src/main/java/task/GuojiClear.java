package task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.aliasi.classify.ConfusionMatrix;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Pair;
import com.rouchtime.ml.wekaExplore.WekaTextClassifyUtils;
import com.rouchtime.nlp.common.Term;
import com.rouchtime.nlp.corpus.GuojiCorpus;
import com.rouchtime.nlp.duplicate.bean.DuplicateBean;
import com.rouchtime.nlp.duplicate.bean.Result;
import com.rouchtime.util.DuplicateUtils;
import com.rouchtime.util.RegexUtils;
import com.rouchtime.util.WekaUtils;

import tokenizer.HanLPTokenizerFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.converters.ConverterUtils.DataSink;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.filters.unsupervised.instance.NonSparseToSparse;

public class GuojiClear {

	static StopWordTokenierFactory stopTokenizerFactory = new StopWordTokenierFactory(
			HanLPTokenizerFactory.getIstance());
	static Map<String, Pair> mapURL = new HashMap<String, Pair>();
	static DuplicateUtils duplicateUtils = DuplicateUtils.getIstance(stopTokenizerFactory, 100000, true);

	public static void duplicate(String path) throws IOException {
		File[] files = new File(path).listFiles();
		for (File file : files) {
			List<String> lines = FileUtils.readLines(file);
			for (String line : lines) {
				String[] splits = line.split("\t+");
				if (line.split("\t+").length != 3) {
					System.err.println("ErrorLine:" + line);
					continue;
				}
				String id = RegexUtils.convertURLToNewsKey(splits[0]);
				mapURL.put(id, new Pair<String, String>(splits[0], file.getName()));
				if (id == null) {
					System.err.println("ErrorNewsKey:" + splits[0]);
					continue;
				}
				String raw = RegexUtils.cleanParaAndImgLabel(splits[2]);
				DuplicateBean duplicateBean = new DuplicateBean();
				duplicateBean.setId(id);
				duplicateBean.setRaw(raw);
				duplicateBean.setTimestamp(System.currentTimeMillis());
				List<Result> results = duplicateUtils.duplicateLong(duplicateBean, 0.8);

				if (results.size() > 1) {
					FileUtils.write(new File("D://corpus//category//guoji//duplicate"),
							file.getName() + "\t" + line + "\n", "utf-8", true);
					for (Result result : results) {
						FileUtils.write(new File("D://corpus//category//guoji//duplicate"),
								mapURL.get(result.getDuplicateBean().getId()) + "\n", "utf-8", true);
					}
				} else {
					FileUtils.write(new File("D://corpus//category//guoji//nonduplicate_zhang//" + file.getName()),
							line + "\n", "utf-8", true);
				}
			}
		}
	}

	public static void modelTrain() throws Exception {
		String path = "D:\\corpus\\category\\guoji\\AB";
		String modelPath = path + "\\model";

		String trainTotalPath = path + "\\traintotal.arff";
		Instances data1 = DataSource.read(trainTotalPath);
		Instances wtvData1 = writeModel(data1, modelPath + "\\traintotal.model");

		String path00 = path + "\\train\\00.arff";
		Instances data00 = DataSource.read(path00);
		Instances wtvData00 = writeModel(data00, modelPath + "\\00.model");

		String path01 = path + "\\train\\01.arff";
		Instances data01 = DataSource.read(path01);
		Instances wtvData01 = writeModel(data01, modelPath + "\\01.model");

		String path02 = path + "\\train\\02.arff";
		Instances data02 = DataSource.read(path02);
		Instances wtvData02 = writeModel(data02, modelPath + "\\02.model");

		String path03 = path + "\\train\\03.arff";
		Instances data03 = DataSource.read(path03);
		Instances wtvData03 = writeModel(data03, modelPath + "\\03.model");

		String path06 = path + "\\train\\06.arff";
		Instances data06 = DataSource.read(path06);
		Instances wtvData06 = writeModel(data06, modelPath + "\\06.model");

	}

	public static Instances writeModel(Instances data, String path) throws Exception {
		data.setClassIndex(data.numAttributes() - 1);
		WekaTextClassifyUtils wtcf = new WekaTextClassifyUtils(1000, data);
		wtcf.writeModel(path, new NaiveBayesMultinomial());
		return wtcf.StringToVector().stringFreeStructure();
	}



	public static List<String> getLabels(Instances data) {
		List<String> labels = new ArrayList<String>();
		for (int i = 0; i < data.classAttribute().numValues(); i++) {
			labels.add(data.classAttribute().value(i));
		}
		return labels;
	}

	public static void testModel() throws Exception {
		String path = "D:\\corpus\\category\\guoji\\AB";
		String testTotalPath = path + "\\testtotal.arff";
		Instances testdata = DataSource.read(testTotalPath);
		testdata.setClassIndex(testdata.numAttributes() - 1);
		
		String trainTotalPath = path + "\\traintotal.arff";
		Instances data1 = DataSource.read(trainTotalPath);
		WekaTextClassifyUtils wtcf = new WekaTextClassifyUtils(1000, data1);
		
		String path00 = path + "\\train\\00.arff";
		Instances data00 = DataSource.read(path00);
		WekaTextClassifyUtils wtcf00 = new WekaTextClassifyUtils(1000, data00);

		String path01 = path + "\\train\\01.arff";
		Instances data01 = DataSource.read(path01);
		WekaTextClassifyUtils wtcf01 = new WekaTextClassifyUtils(1000, data01);

		String path02 = path + "\\train\\02.arff";
		Instances data02 = DataSource.read(path02);
		WekaTextClassifyUtils wtcf02 = new WekaTextClassifyUtils(1000, data02);

		String path03 = path + "\\train\\03.arff";
		Instances data03 = DataSource.read(path03);
		WekaTextClassifyUtils wtcf03 = new WekaTextClassifyUtils(1000, data03);

		String path06 = path + "\\train\\06.arff";
		Instances data06 = DataSource.read(path06);
		WekaTextClassifyUtils wtcf06 = new WekaTextClassifyUtils(1000, data06);
		String[] labelArray = new String[getLabels(testdata).size()];
		ConfusionMatrix confusionMatrix = new ConfusionMatrix(getLabels(testdata).toArray(labelArray));
		for (Instance instance : testdata) {
			String text = instance.toString(0).replaceAll("'", "");
			String label = instance.toString(1);
			String combineClass = wtcf.classifyMessage(text);
			if (combineClass.equals("00")) {
				String sublabel = wtcf00.classifyMessage(text);
				confusionMatrix.increment(label, sublabel);
				continue;
			}
			if (combineClass.equals("01")) {
				String sublabel = wtcf01.classifyMessage(text);
				confusionMatrix.increment(label, sublabel);
				continue;
			}
			if (combineClass.equals("02")) {
				String sublabel = wtcf02.classifyMessage(text);
				confusionMatrix.increment(label, sublabel);
				continue;
			}
			if (combineClass.equals("03")) {
				String sublabel = wtcf03.classifyMessage(text);
				confusionMatrix.increment(label, sublabel);
				continue;
			}
			if (combineClass.equals("06")) {
				String sublabel = wtcf06.classifyMessage(text);
				confusionMatrix.increment(label, sublabel);
				continue;
			}
			confusionMatrix.increment(label, labelMap.get(combineClass));
		}
		StringBuffer sb = new StringBuffer();
		sb.append(confusionMatrix.macroAvgPrecision());
		sb.append(confusionMatrix.macroAvgRecall());
        for (int i = 0; i < confusionMatrix.categories().length; ++i) {
        	sb.append(confusionMatrix.categories()[i]).append("\n");
        	sb.append("accuracy:");sb.append(confusionMatrix.oneVsAll(i).accuracy());sb.append("\n");
        	sb.append("recall:");sb.append(confusionMatrix.oneVsAll(i).recall());sb.append("\n");
        	sb.append("fMeasure:");sb.append(confusionMatrix.oneVsAll(i).fMeasure());sb.append("\n");
        	sb.append("size:");sb.append(confusionMatrix.oneVsAll(i).positiveReference());sb.append("\n");
        }
		FileUtils.write(new File("D://result"), sb.toString(),"utf-8");
	}

	public static void combineAB(String trainPath, String testPath) throws IOException {
		StopWordTokenierFactory stopWordFactory = new StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		StopNatureTokenizerFactory stopNatureTokenizerFactory = new StopNatureTokenizerFactory(stopWordFactory);
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring-mybatis.xml");
		GuojiCorpus guojiCorpus = (GuojiCorpus) applicationContext.getBean(GuojiCorpus.class);
		Map<String, List<String>> trainAndTest = guojiCorpus.produceTrainAndTestByRate(0.66);
		for (String key : trainAndTest.keySet()) {
			if (key.equals("train")) {
				for (String fileid : trainAndTest.get(key)) {
					String label = guojiCorpus.labelFromfileids(fileid);
					String raw = guojiCorpus.rawFromfileids(fileid);
					combineRaw(raw, label, stopNatureTokenizerFactory, trainPath);
					writeToWekaFromRaw(raw, stopNatureTokenizerFactory, trainPath + "unCombine", label, label);
				}
			} else {
				for (String fileid : trainAndTest.get(key)) {
					String label = guojiCorpus.labelFromfileids(fileid);
					String raw = guojiCorpus.rawFromfileids(fileid);
					writeToWekaFromRaw(raw, stopNatureTokenizerFactory, testPath, label, label);
				}
			}
		}
	}

	public static void subCate(String trainPath, String testPath) throws IOException {
		StopWordTokenierFactory stopWordFactory = new StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		StopNatureTokenizerFactory stopNatureTokenizerFactory = new StopNatureTokenizerFactory(stopWordFactory);
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring-mybatis.xml");
		GuojiCorpus guojiCorpus = (GuojiCorpus) applicationContext.getBean(GuojiCorpus.class);
		Map<String, List<String>> trainAndTest = guojiCorpus.produceTrainAndTestByRate(0.66);
		for (String key : trainAndTest.keySet()) {
			if (key.equals("train")) {
				for (String fileid : trainAndTest.get(key)) {
					String label = guojiCorpus.labelFromfileids(fileid);
					String raw = guojiCorpus.rawFromfileids(fileid);
					combineRaw(raw, label, stopNatureTokenizerFactory, trainPath);

				}
			} else {
				for (String fileid : trainAndTest.get(key)) {
					String label = guojiCorpus.labelFromfileids(fileid);
					String raw = guojiCorpus.rawFromfileids(fileid);
					writeToWekaFromRaw(raw, stopNatureTokenizerFactory, testPath, label, label);
				}
			}
		}
	}

	public static void subCate(String path) throws IOException {
		StopWordTokenierFactory stopWordFactory = new StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		StopNatureTokenizerFactory stopNatureTokenizerFactory = new StopNatureTokenizerFactory(stopWordFactory);
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring-mybatis.xml");
		GuojiCorpus guojiCorpus = (GuojiCorpus) applicationContext.getBean(GuojiCorpus.class);
		for (String label : guojiCorpus.labels()) {
			if (label.equals("换届任免") || label.equals("政治丑闻") || label.equals("政治人物动态")) {
				writeToWekaFromCorpus(guojiCorpus, label, stopNatureTokenizerFactory, path, label);
			}
		}
	}

	public static void combine(String path) throws IOException {
		StopWordTokenierFactory stopWordFactory = new StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		StopNatureTokenizerFactory stopNatureTokenizerFactory = new StopNatureTokenizerFactory(stopWordFactory);
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring-mybatis.xml");
		GuojiCorpus guojiCorpus = (GuojiCorpus) applicationContext.getBean(GuojiCorpus.class);
		for (String label : guojiCorpus.labels()) {
			if (label.equals("警匪犯罪") || label.equals("社会乱象") || label.equals("恐怖袭击") || label.equals("武装运动")) {
				writeToWekaFromCorpus(guojiCorpus, label, stopNatureTokenizerFactory, path, "00");
			}

			if (label.equals("公共基建") || label.equals("金融经济") || label.equals("科技") || label.equals("改革政策")
					|| label.equals("民生话题")) {
				writeToWekaFromCorpus(guojiCorpus, label, stopNatureTokenizerFactory, path, "01");
			}
			if (label.equals("其他生活新闻") || label.equals("异国风情") || label.equals("娱乐") || label.equals("情感生活")
					|| label.equals("正能量新闻")) {
				// writeToWekaFromCorpus(guojiCorpus, label, stopNatureTokenizerFactory, path,
				// "02");
				continue;
			}
			if (label.equals("军事相关") || label.equals("领土主权")) {
				writeToWekaFromCorpus(guojiCorpus, label, stopNatureTokenizerFactory, path, "03");
			}
			if (label.equals("国际外交")) {
				writeToWekaFromCorpus(guojiCorpus, label, stopNatureTokenizerFactory, path, "04");
			}
			if (label.equals("奇异动物")) {
				writeToWekaFromCorpus(guojiCorpus, label, stopNatureTokenizerFactory, path, "05");
			}
			if (label.equals("意外事故")) {
				writeToWekaFromCorpus(guojiCorpus, label, stopNatureTokenizerFactory, path, "06");
			}
			if (label.equals("换届任免") || label.equals("政治丑闻") || label.equals("政治人物动态")) {
				writeToWekaFromCorpus(guojiCorpus, label, stopNatureTokenizerFactory, path, "07");
			}
			if (label.equals("科研科考")) {
				writeToWekaFromCorpus(guojiCorpus, label, stopNatureTokenizerFactory, path, "08");
			}
			if (label.equals("自然灾害")) {
				writeToWekaFromCorpus(guojiCorpus, label, stopNatureTokenizerFactory, path, "09");
			}
			if (label.equals("集会游行")) {
				writeToWekaFromCorpus(guojiCorpus, label, stopNatureTokenizerFactory, path, "10");
			}
			if (label.equals("体育")) {
				writeToWekaFromCorpus(guojiCorpus, label, stopNatureTokenizerFactory, path, "11");
			}
			if (label.equals("文化")) {
				writeToWekaFromCorpus(guojiCorpus, label, stopNatureTokenizerFactory, path, "12");
			}
			if (label.equals("涉中事件")) {
				writeToWekaFromCorpus(guojiCorpus, label, stopNatureTokenizerFactory, path, "13");
			}
			if (label.equals("自然景观")) {
				writeToWekaFromCorpus(guojiCorpus, label, stopNatureTokenizerFactory, path, "14");
			}
			if (label.equals("趣闻轶事")) {
				writeToWekaFromCorpus(guojiCorpus, label, stopNatureTokenizerFactory, path, "15");
			}
		}
	}
	static Map<String,String> labelMap = new HashMap<String,String>();
	static {
		labelMap.put("04", "国际外交");
		labelMap.put("05", "意外事故");
		labelMap.put("07", "科研科考");
		labelMap.put("08", "自然灾害");
		labelMap.put("09", "集会游行");
		labelMap.put("10", "体育");
		labelMap.put("11", "文化");
		labelMap.put("12", "涉中事件");
	}
	public static void combineRaw(String raw, String label, TokenizerFactory factory, String path) throws IOException {
		if (label.equals("警匪犯罪") || label.equals("社会乱象") || label.equals("恐怖袭击") || label.equals("武装运动")) {
			writeToWekaFromRaw(raw, factory, path, "00", label);
		}

		if (label.equals("公共基建") || label.equals("金融经济") || label.equals("科技") || label.equals("改革政策")
				|| label.equals("民生话题")) {
			writeToWekaFromRaw(raw, factory, path, "01", label);
		}
		if (label.equals("其他生活新闻") || label.equals("异国风情") || label.equals("娱乐") || label.equals("情感生活")
				|| label.equals("正能量新闻") || label.equals("奇异动物") || label.equals("趣闻轶事") || label.equals("自然景观")) {
			writeToWekaFromRaw(raw, factory, path, "02", label);
		}
		if (label.equals("军事相关") || label.equals("领土主权")) {
			writeToWekaFromRaw(raw, factory, path, "03", label);
		}
		if (label.equals("国际外交")) {
			writeToWekaFromRaw(raw, factory, path, "04", label);
		}
		if (label.equals("意外事故")) {
			writeToWekaFromRaw(raw, factory, path, "05", label);
		}
		if (label.equals("换届任免") || label.equals("政治丑闻") || label.equals("政治人物动态")) {
			writeToWekaFromRaw(raw, factory, path, "06", label);
		}
		if (label.equals("科研科考")) {
			writeToWekaFromRaw(raw, factory, path, "07", label);
		}
		if (label.equals("自然灾害")) {
			writeToWekaFromRaw(raw, factory, path, "08", label);
		}
		if (label.equals("集会游行")) {
			writeToWekaFromRaw(raw, factory, path, "09", label);
		}
		if (label.equals("体育")) {
			writeToWekaFromRaw(raw, factory, path, "10", label);
		}
		if (label.equals("文化")) {
			writeToWekaFromRaw(raw, factory, path, "11", label);
		}
		if (label.equals("涉中事件")) {
			writeToWekaFromRaw(raw, factory, path, "12", label);
		}
	}

	public static void writeToWekaFromRaw(String raw, TokenizerFactory factory, String path, String Combinelabel,
			String label) throws IOException {
		String wekaContent = WekaUtils.formWekaArffTextFromRaw(RegexUtils.cleanParaAndImgLabel(raw), factory,
				Combinelabel);
		FileUtils.write(new File(path + "total.arff"), wekaContent + "\n", "utf-8", true);
		String wekaContent1 = WekaUtils.formWekaArffTextFromRaw(RegexUtils.cleanParaAndImgLabel(raw), factory, label);
		FileUtils.write(new File(path + "\\" + Combinelabel + ".arff"), wekaContent1 + "\n", "utf-8", true);
	}

	public static void writeToWekaFromCorpus(GuojiCorpus guojiCorpus, String label, TokenizerFactory factory,
			String path, String combineLabel) throws IOException {
		List<String> contents = guojiCorpus.rawFromLabel(label);
		for (String content : contents) {
			String wekaContent = WekaUtils.formWekaArffTextFromRaw(RegexUtils.cleanParaAndImgLabel(content), factory,
					combineLabel);
			FileUtils.write(new File(path), wekaContent + "\n", "utf-8", true);
		}
	}

	public static void SMOTETest() throws Exception {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring-mybatis.xml");
		GuojiCorpus guojiCorpus = (GuojiCorpus) applicationContext.getBean(GuojiCorpus.class);
		/* 初始化 */
		List<String> labels = new ArrayList<String>();
		labels.add("占位类");
		for (String label : guojiCorpus.labels()) {
			labels.add(label);
		}
		WekaTextClassifyUtils wekaTextClassifyUtils = new WekaTextClassifyUtils(labels.size(), labels);

		StopWordTokenierFactory stopWordFactory = new StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		StopNatureTokenizerFactory stopNatureTokenizerFactory = new StopNatureTokenizerFactory(stopWordFactory);
		Map<String, List<String>> map = guojiCorpus.produceTrainAndTestByRate(0.66);
		for (String fileid : map.get("train")) {
			StringBuilder sb = new StringBuilder();
			String label = guojiCorpus.labelFromfileids(fileid);
			List<Term> terms = guojiCorpus.wordFromfileids(fileid, stopNatureTokenizerFactory);
			for (Term term : terms) {
				sb.append(term.getWord().replaceAll("'", "")).append(" ");
			}
			wekaTextClassifyUtils.updateData(sb.toString(), label);
		}

		Instances stringToVector = wekaTextClassifyUtils.StringToVector();
		@SuppressWarnings("unused")
		Instances class2Smote = wekaTextClassifyUtils.SMOTESample(stringToVector, "2", 1000.0, 5);
		Instances class3Smote = wekaTextClassifyUtils.SMOTESample(class2Smote, "3", 4000.0, 5);
		Instances class4Smote = wekaTextClassifyUtils.SMOTESample(class3Smote, "4", 100, 5);
		Instances class7Smote = wekaTextClassifyUtils.SMOTESample(class4Smote, "7", 500.0, 5);
		Instances class8Smote = wekaTextClassifyUtils.SMOTESample(class7Smote, "8", 800.0, 5);
		Instances class10Smote = wekaTextClassifyUtils.SMOTESample(class7Smote, "10", 800.0, 5);
		DataSink.write("D://corpus//weka_data.arff", class3Smote);
		System.out.println("over");
	}

	public static void combineSmote() throws Exception {
		String[] labels = new String[13];
		labels[0] = "00";
		labels[1] = "01";
		labels[2] = "02";
		labels[3] = "03";
		labels[4] = "04";
		labels[5] = "05";
		labels[6] = "06";
		labels[7] = "07";
		labels[8] = "08";
		labels[9] = "09";
		labels[10] = "10";
		labels[11] = "11";
		labels[12] = "12";
		Instances data1 = DataSource.read("C:\\Users\\Admin\\Desktop\\train_12c.arff");
		data1.setClassIndex(data1.numAttributes() - 1);
		StringToWordVector m_filter = new StringToWordVector();
		m_filter = new StringToWordVector(1000);
		m_filter.setTFTransform(true);
		m_filter.setIDFTransform(true);
		m_filter.setOutputWordCounts(true);
		m_filter.setNormalizeDocLength(
				new SelectedTag(StringToWordVector.FILTER_NORMALIZE_ALL, StringToWordVector.TAGS_FILTER));
		m_filter.setInputFormat(data1);
		Instances filteredData = Filter.useFilter(data1, m_filter);
		Instances class07 = SMOTESample(filteredData, "8", 500, 5);
		Instances class08 = SMOTESample(class07, "9", 500, 5);
		Instances class09 = SMOTESample(class08, "10", 1500, 5);
		Instances class10 = SMOTESample(class09, "11", 1500, 5);
		Instances class11 = SMOTESample(class10, "12", 700, 5);
		Instances class12 = SMOTESample(class11, "13", 200, 5);
		NaiveBayesMultinomial nbm = new NaiveBayesMultinomial();
		nbm.buildClassifier(class12);
		Instances testset = data1.stringFreeStructure();
		List<String> testData = FileUtils.readLines(new File("C:\\Users\\Admin\\Desktop\\test_12c.arff"));

		ConfusionMatrix confusionMatrix = new ConfusionMatrix(labels);
		for (String line : testData) {
			String text = line.split(",")[0].replaceAll("'", "");
			String label = line.split(",")[1];
			Instance instance = makeInstance(text, testset);
			m_filter.input(instance);
			Instance filteredInstance = m_filter.output();
			double predicted = nbm.classifyInstance(filteredInstance);
			String predictedLabel = labels[(int) predicted];
			confusionMatrix.increment(label, predictedLabel);
		}
		System.out.println("totlaAccuray:" + confusionMatrix.totalAccuracy());
		System.out.println("macroAvgPrecision:" + confusionMatrix.macroAvgPrecision());
		System.out.println("macroAvgRecall:" + confusionMatrix.macroAvgRecall());
		System.out.println("macroAvgFMeasure:" + confusionMatrix.macroAvgFMeasure());
		System.out.println("over");
	}

	private static Instance makeInstance(String text, Instances data) {
		Instance instance = new DenseInstance(2);
		Attribute messageAtt = data.attribute("Text");
		instance.setValue(messageAtt, messageAtt.addStringValue(text));
		instance.setDataset(data);
		return instance;
	}

	public static Instances SMOTESample(Instances filteredData, String classValue, double percentage, int neighbor)
			throws Exception {
		SMOTE convert = new SMOTE();
		convert.setClassValue(classValue);
		convert.setPercentage(percentage);
		convert.setNearestNeighbors(neighbor);
		convert.setRandomSeed((int) (Math.random() * 10));
		Instances SmoteInstances = null;
		try {
			convert.setInputFormat(filteredData);
			SmoteInstances = Filter.useFilter(filteredData, convert);
		} catch (Exception e) {
			e.printStackTrace();
		}
		NonSparseToSparse nonSparseToSparse = new NonSparseToSparse();
		nonSparseToSparse.setInputFormat(SmoteInstances.stringFreeStructure());
		Instances ins = Filter.useFilter(SmoteInstances, nonSparseToSparse);
		return ins;
	}

	public static void main(String[] args) throws Exception {
		// duplicate(
		// "D:\\corpus\\category\\guoji\\guoji_zhang_20170803\\guoji_zhang_20170803");
		// duplicate( "D:\\corpus\\category\\guoji\\guoji170803\\guoji170803");
		// combineAB("D://corpus//category//guoji//AB//train",
		// "D://corpus//category//guoji//AB//test");
		// SMOTETest();
		// combineSmote();
		// subCate("D://corpus//06");
//		 modelTrain();
		testModel();
	}
}
