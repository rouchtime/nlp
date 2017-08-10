package com.rouchtime.ml.wekaExplore;

import java.util.Enumeration;
import java.util.Vector;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.AttributeEvaluator;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;

public class MacroDocumentFrequencyEval extends ASEvaluation implements AttributeEvaluator, OptionHandler {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8963094789983608540L;
	private boolean m_missing_merge;
	private double[] m_docFreq;

	public MacroDocumentFrequencyEval() {
		resetOptions();
	}

	public String globalInfo() {
		return "MacroDocumentFrequencyEval";
	}

	@Override
	public Enumeration<Option> listOptions() {
		Vector<Option> newVector = new Vector<Option>(2);
		newVector.addElement(new Option("\ttreat missing values as a seperate " + "value.", "M", 0, "-M"));
		newVector.addElement(new Option(
				"\tjust binarize numeric attributes instead \n" + "\tof properly discretizing them.", "B", 0, "-B"));
		return newVector.elements();
	}

	protected void resetOptions() {
		m_missing_merge = true;
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		resetOptions();
		setMissingMerge(!(Utils.getFlag('M', options)));

	}

	public void setMissingMerge(boolean b) {
		m_missing_merge = b;
	}

	public boolean getMissingMerge() {
		return m_missing_merge;
	}

	@Override
	public String[] getOptions() {
		String[] options = new String[2];
		int current = 0;

		if (!getMissingMerge()) {
			options[current++] = "-M";
		}

		while (current < options.length) {
			options[current++] = "";
		}

		return options;
	}

	@Override
	public double evaluateAttribute(int attribute) throws Exception {
		return m_docFreq[attribute];
	}

	@Override
	public void buildEvaluator(Instances data) throws Exception {
		getCapabilities().testWithFail(data);
		int classIndex = data.classIndex();
		int numInstances = data.numInstances();
		int numClasses = data.attribute(classIndex).numValues();
		double[] counts = new double[data.numAttributes()];
		double[] class_counts = new double[numClasses];
		for (int k = 0; k < numInstances; k++) {
			Instance inst = data.instance(k);
			class_counts[(int) inst.classValue()] += inst.weight();
		}
		for (int k = 0; k < numInstances; k++) {
			Instance inst = data.instance(k);
			for (int i = 0; i < inst.numValues(); i++) {
				if (inst.index(i) != classIndex) {
					counts[inst.index(i)] += 1;
				}
			}
		}
		m_docFreq = new double[data.numAttributes()];
		if (m_missing_merge) {
			for (int k = 0; k < data.numAttributes(); k++) {
				if (k != classIndex) {
					m_docFreq[k] = counts[k];
				}
			}
		}
	}

}
