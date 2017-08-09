package com.rouchtime.ml.wekaExplore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.SerializationHelper;
import weka.filters.Filter;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.filters.unsupervised.instance.NonSparseToSparse;

public class WekaTextClassifyUtils implements Serializable {

	private static final long serialVersionUID = -5170845221576215830L;
	private Instances m_Data = null;
	private StringToWordVector m_filter;
	private Classifier m_Classifier = new NaiveBayesMultinomial();
	private boolean m_UpToDate;
	private List<String> labels;

	public WekaTextClassifyUtils(int size, List<String> labels) {
		String nameOfDataset = "MessageClassificationProblem";
		List<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add(new Attribute("Message", (List<String>) null));
		attributes.add(new Attribute("ClassWeka*", labels));
		m_Data = new Instances(nameOfDataset, (ArrayList<Attribute>) attributes, size);
		m_Data.setClassIndex(m_Data.numAttributes() - 1);
		m_Data.clear();

		m_filter = new StringToWordVector(1000);
		m_filter.setTFTransform(true);
		m_filter.setIDFTransform(true);
		m_filter.setOutputWordCounts(true);
		m_filter.setNormalizeDocLength(
				new SelectedTag(StringToWordVector.FILTER_NORMALIZE_ALL, StringToWordVector.TAGS_FILTER));
	}

	public WekaTextClassifyUtils(int vectorCount, Instances data) {
		m_Data = data;
		m_Data.setClassIndex(m_Data.numAttributes() - 1);
		labels = getLabels();
		m_filter = new StringToWordVector(vectorCount);
		m_filter.setTFTransform(true);
		m_filter.setIDFTransform(true);
		m_filter.setOutputWordCounts(true);
		m_filter.setNormalizeDocLength(
				new SelectedTag(StringToWordVector.FILTER_NORMALIZE_ALL, StringToWordVector.TAGS_FILTER));
		m_UpToDate = false;
	}

	public void updateData(String message, String classValue) {
		Instance instance = makeInstance(message, m_Data);
		instance.setClassValue(classValue);
		m_Data.add(instance);
		m_UpToDate = false;
	}

	private List<String> getLabels() {
		List<String> labels = new ArrayList<String>();
		for (int i = 0; i < m_Data.classAttribute().numValues(); i++) {
			labels.add(m_Data.classAttribute().value(i));
		}
		return labels;
	}

	private Instance makeInstance(String text, Instances data) {
		Instance instance = new DenseInstance(2);
		Attribute messageAtt = data.attribute("Text");
		instance.setValue(messageAtt, messageAtt.addStringValue(text));
		instance.setDataset(data);
		return instance;
	}

	public String classifyMessage(String message) throws Exception {
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
		return labels.get((int) predicted);
	}

	public Instances StringToVector() throws Exception {
		m_filter.setInputFormat(m_Data);
		Instances filteredData = Filter.useFilter(m_Data, m_filter);
		return filteredData;
	}

	public Instances SMOTEToEveryClass(Instances instances) throws Exception {
		m_filter.setInputFormat(m_Data);
		Instances filteredData = Filter.useFilter(m_Data, m_filter);
		Instances filterInstances = filteredData.stringFreeStructure();
		List<String> labels  = new ArrayList<String>();
		labels.add("占位");
		for (int i = 0; i < instances.attribute("class").numValues(); i++) {
			labels.add(instances.attribute("class").value(i));
		}

		Attribute attribute = new Attribute("classnew",labels);
		filterInstances.insertAttributeAt(attribute, 0);
		filterInstances.setClassIndex(0);
		filterInstances.deleteAttributeAt(1);
		for (Instance instance : instances) {
			m_filter.input(instance);
			Instance filteredInstance = m_filter.output();
			filterInstances.add(filteredInstance);
		}
		return m_Data;
	}

	public Instances SMOTESample(Instances filteredData, String classValue, double percentage, int neighbor)
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
		SmoteInstances.setClassIndex(0);
		NonSparseToSparse nonSparseToSparse = new NonSparseToSparse();
		nonSparseToSparse.setInputFormat(SmoteInstances.stringFreeStructure());
		Instances ins = Filter.useFilter(SmoteInstances, nonSparseToSparse);
		return ins;
	}

	public void writeModel(String outfilepath, Classifier classifer) throws Exception {
		if (m_Data == null) {
			throw new NullPointerException("数据为空！");
		}
		m_filter.setInputFormat(m_Data);
		Instances filteredData = Filter.useFilter(m_Data, m_filter);
		classifer.buildClassifier(filteredData);
		SerializationHelper.write(outfilepath, classifer);
	}
}
