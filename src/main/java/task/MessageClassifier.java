package task;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.SerializationHelper;
import weka.core.converters.CSVSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class MessageClassifier implements Serializable {

	private static final long serialVersionUID = -5170845221576215830L;
	private Instances m_Data_train = null;
	private Instances m_Data_test = null;
	private Instances m_Data = null;
	private StringToWordVector m_filter;
	private Classifier m_Classifier = new NaiveBayesMultinomial();
	private boolean m_UpToDate;

	public MessageClassifier(int trainSize, int testSize, Set<String> labels) {
		String nameOfDataset = "MessageClassificationProblem";
		List<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add(new Attribute("Message", (List<String>) null));
		List<String> classValues = new ArrayList<String>();
		for (String label : labels) {
			classValues.add(label);
		}
		attributes.add(new Attribute("ClassWeka*", classValues));
		m_Data = new Instances(nameOfDataset, (ArrayList<Attribute>) attributes, trainSize);
		m_Data.setClassIndex(m_Data.numAttributes() - 1);
		m_Data.clear();

		m_Data_train = new Instances(nameOfDataset, (ArrayList<Attribute>) attributes, trainSize);
		m_Data_train.setClassIndex(m_Data_train.numAttributes() - 1);
		m_Data_train.clear();

		m_Data_test = new Instances(nameOfDataset, (ArrayList<Attribute>) attributes, testSize);
		m_Data_test.setClassIndex(m_Data_test.numAttributes() - 1);
		m_Data_test.clear();

		/* 设置stringToWordVector */
		m_filter = new StringToWordVector(10000);
		m_filter.setTFTransform(true);
		m_filter.setIDFTransform(true);
		m_filter.setOutputWordCounts(true);
		m_filter.setNormalizeDocLength(
				new SelectedTag(StringToWordVector.FILTER_NORMALIZE_ALL, StringToWordVector.TAGS_FILTER));
	}

	public MessageClassifier(int size, Set<String> labels) {
		String nameOfDataset = "MessageClassificationProblem";
		List<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add(new Attribute("Message", (List<String>) null));
		List<String> classValues = new ArrayList<String>();
		for (String label : labels) {
			classValues.add(label);
		}
		attributes.add(new Attribute("ClassWeka*", classValues));
		m_Data = new Instances(nameOfDataset, (ArrayList<Attribute>) attributes, size);
		m_Data.setClassIndex(m_Data.numAttributes() - 1);
		m_Data.clear();

		/* 设置stringToWordVector */
		m_filter = new StringToWordVector(10000);
		m_filter.setTFTransform(true);
		m_filter.setIDFTransform(true);
		m_filter.setOutputWordCounts(true);
		m_filter.setNormalizeDocLength(
				new SelectedTag(StringToWordVector.FILTER_NORMALIZE_ALL, StringToWordVector.TAGS_FILTER));
	}

	public void writeModel(String outfilepath) throws Exception {
		m_filter.setInputFormat(m_Data);
		Instances filteredData = Filter.useFilter(m_Data, m_filter);
		NaiveBayesMultinomial nbm = new NaiveBayesMultinomial();
		nbm.buildClassifier(filteredData);
		SerializationHelper.write(outfilepath, nbm);
	}

	public void printCrossEvaluate(String outputPath, String title) throws Exception {
		m_filter.setInputFormat(m_Data);
		Instances filteredData = Filter.useFilter(m_Data, m_filter);
		Evaluation eval = new Evaluation(filteredData);
		NaiveBayesMultinomial nbm = new NaiveBayesMultinomial();
		eval.crossValidateModel(nbm, filteredData, 10, new Random(System.currentTimeMillis()));
		StringBuffer sb = new StringBuffer();
		sb.append(eval.weightedFMeasure()).append("\n").append(eval.toSummaryString()).append("\n")
				.append(eval.toMatrixString());
		FileUtils.write(new File(outputPath), sb.toString(), "utf-8", true);
	}

	public void printEvaluteRemain(String outputPath) throws Exception {
		m_filter.setInputFormat(m_Data_train);
		Instances train = Filter.useFilter(m_Data_train, m_filter);
		Instances testset = train.stringFreeStructure();
		for (Instance instance : m_Data_test) {
			m_filter.input(instance);
			Instance filteredInstance = m_filter.output();
			testset.add(filteredInstance);
		}
		NaiveBayesMultinomial nbm = new NaiveBayesMultinomial();
		nbm.buildClassifier(train);
		Evaluation eval = new Evaluation(train);
		eval.evaluateModel(nbm, testset);
		FileUtils.write(new File(outputPath), eval.toSummaryString() + "\n", "utf-8", true);
	}

	public void evaluateFeatureSelect(int featureSize, String outputPath) throws Exception {
		FilteredClassifier filterClassifier = new FilteredClassifier();
		AttributeSelectedClassifier attributeSelectClassifier = new AttributeSelectedClassifier();
		InfoGainAttributeEval eval = new InfoGainAttributeEval();
		Ranker ranker = new Ranker();
		ranker.setNumToSelect(30000);
		NaiveBayesMultinomial nbm = new NaiveBayesMultinomial();
		attributeSelectClassifier.setClassifier(nbm);
		attributeSelectClassifier.setEvaluator(eval);
		attributeSelectClassifier.setSearch(ranker);
		filterClassifier.setClassifier(attributeSelectClassifier);
		filterClassifier.setFilter(m_filter);
		Evaluation evaluation = new Evaluation(m_Data);
		evaluation.crossValidateModel(filterClassifier, m_Data, 10, new Random(System.currentTimeMillis()));
		FileUtils.write(new File(outputPath),
				evaluation.weightedFMeasure() + "\n" + evaluation.precision(0) + "\n" + evaluation.precision(1) + "\n",
				"utf-8", true);
	}

	public void printVector(String outfile) throws Exception {
		m_filter.setInputFormat(m_Data);
		Instances filteredData = Filter.useFilter(m_Data, m_filter);
		CSVSaver saver = new CSVSaver();
		saver.setInstances(filteredData);
		saver.setFile(new File(outfile));
		saver.writeBatch();
	}

	public double classifyMessage(String message) throws Exception {
		if (m_Data.numInstances() == 0) {
			throw new Exception("没有分类器可用。");
		}
		if (!m_UpToDate) {
			m_filter.setInputFormat(m_Data);
			Instances filteredData = Filter.useFilter(m_Data, m_filter);
			m_Classifier.buildClassifier(filteredData);
			m_UpToDate = true;
		}
		Instances testset = m_Data.stringFreeStructure();
		Instance instance = makeInstance(message, testset);
		m_filter.input(instance);
		Instance filteredInstance = m_filter.output();
		double predicted = m_Classifier.classifyInstance(filteredInstance);
		return predicted;
	}

	public void updateTrainData(String message, String classValue) {
		Instance instance = makeInstance(message, m_Data_train);
		instance.setClassValue(classValue);
		m_Data_train.add(instance);
		m_UpToDate = false;
	}

	public void updateTestData(String message, String classValue) {
		Instances testset = m_Data_train.stringFreeStructure();
		Instance instance = makeInstance(message, testset);
		instance.setClassValue(classValue);
		m_Data_test.add(instance);
		m_UpToDate = false;
	}

	public void updateData(String message, String classValue) {
		Instance instance = makeInstance(message, m_Data);
		instance.setClassValue(classValue);
		m_Data.add(instance);
		m_UpToDate = false;
	}

	private Instance makeInstance(String text, Instances data) {
		Instance instance = new DenseInstance(2);
		Attribute messageAtt = data.attribute("Message");
		instance.setValue(messageAtt, messageAtt.addStringValue(text));
		instance.setDataset(data);
		return instance;
	}

}
