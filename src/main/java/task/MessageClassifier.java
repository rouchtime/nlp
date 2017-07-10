package task;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayesMultinomial;
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
	private Instances m_Data = null;
	private StringToWordVector m_filter;
	private Classifier m_Classifier = new J48();
	private boolean m_UpToDate;

	public MessageClassifier(int size, Set<String> labels) {
		String nameOfDataset = "MessageClassificationProblem";
		List<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add(new Attribute("Message", (List<String>) null));
		List<String> classValues = new ArrayList<String>();
		for (String label : labels) {
			classValues.add(label);
		}
		attributes.add(new Attribute("ClassWeka*", classValues));
		m_Data = new Instances(nameOfDataset, (ArrayList<Attribute>) attributes, 100);
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

	public void printEvaluate(String outputPath, String title) throws Exception {
		m_filter.setInputFormat(m_Data);
		Instances filteredData = Filter.useFilter(m_Data, m_filter);
		Evaluation eval = new Evaluation(filteredData);
		NaiveBayesMultinomial nbm = new NaiveBayesMultinomial();
		eval.crossValidateModel(nbm, filteredData, 10, new Random(System.currentTimeMillis()));
		StringBuffer sb = new StringBuffer();
		sb.append(eval.weightedFMeasure()).append("\n").append(eval.toSummaryString()).append("\n").append(eval.toMatrixString());
		FileUtils.write(new File(outputPath), sb.toString(), "utf-8", true);
	}

	public void printVector(String outfile) throws Exception {
		m_filter.setInputFormat(m_Data);
		Instances filteredData = Filter.useFilter(m_Data, m_filter);
		CSVSaver saver = new CSVSaver();
		saver.setInstances(filteredData);
		saver.setFile(new File(outfile));
		saver.writeBatch();
	}

	public void classifyMessage(String message) throws Exception {
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
		System.err.println("文本信息分类为：" + m_Data.classAttribute().value((int) predicted));
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
