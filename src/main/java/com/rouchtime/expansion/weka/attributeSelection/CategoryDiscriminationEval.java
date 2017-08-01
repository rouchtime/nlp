package com.rouchtime.expansion.weka.attributeSelection;

import java.util.Enumeration;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.AttributeEvaluator;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Capabilities.Capability;

public class CategoryDiscriminationEval extends ASEvaluation implements AttributeEvaluator, OptionHandler {
	private static final long serialVersionUID = 2134268634862380958L;
	private boolean m_missing_merge;
	private double[] m_CategoryDis;

	public CategoryDiscriminationEval() {
		resetOptions();
	}

	@Override
	public Enumeration<Option> listOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] getOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double evaluateAttribute(int attribute) throws Exception {
		return m_CategoryDis[attribute];
	}

	protected void resetOptions() {
		m_missing_merge = true;
	}

	@Override
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();
		result.disableAll();

		// attributes
		result.enable(Capability.NOMINAL_ATTRIBUTES);
		result.enable(Capability.NUMERIC_ATTRIBUTES);
		result.enable(Capability.DATE_ATTRIBUTES);
		result.enable(Capability.MISSING_VALUES);

		// class
		result.enable(Capability.NOMINAL_CLASS);
		result.enable(Capability.MISSING_CLASS_VALUES);

		return result;
	}

	@Override
	public void buildEvaluator(Instances data) throws Exception {
		getCapabilities().testWithFail(data);
		int classIndex = data.classIndex();
		int numInstances = data.numInstances();
		int numClasses = data.attribute(classIndex).numValues();
		int numAttributes_except_class = data.numAttributes() - 1;
		double[][] counts = new double[data.numAttributes()][];
	    for (int k = 0; k < data.numAttributes(); k++) {
	        if (k != classIndex) {
	          counts[k] = new double[numClasses];
	        }
	      }
		double[] class_counts = new double[numClasses];
		double[] attributes_counts = new double[numClasses];
		for (int k = 0; k < numInstances; k++) {
			Instance inst = data.instance(k);
			class_counts[(int) inst.classValue()] += inst.weight();
		}
		for (int k = 0; k < numInstances; k++) {
			Instance inst = data.instance(k);
			for (int i = 0; i < inst.numValues(); i++) {
				if (inst.index(i) != classIndex) {
					counts[inst.index(i)][(int) inst.classValue()] += inst.valueSparse(i);
					attributes_counts[(int) inst.classValue()] += inst.valueSparse(i);
				}
			}
		}
		m_CategoryDis = new double[data.numAttributes()];
		if (m_missing_merge) {
			for (int k = 0; k < data.numAttributes(); k++) {
				if (k != classIndex) {
					double prob_tk = 0.0;
					for (int i = 0; i < numClasses; i++) {
						double prob_Ci = (class_counts[i] / numInstances * 1.0);
						double prob_tk_Ci = (1 + counts[k][i])
								/ (numAttributes_except_class * 1.0 + attributes_counts[i]);
						prob_tk += (prob_Ci * prob_tk_Ci);
					}
					double max = 0.0;
					double second_max = 0.0;
					for (int i = 0; i < numClasses; i++) {
						double prob_Ci = (class_counts[i] / numInstances * 1.0);
						double prob_tk_Ci = (1 + counts[k][i])
								/ (numAttributes_except_class * 1.0 + attributes_counts[i]);
						double prob_Ci_tk = (prob_Ci * prob_tk_Ci) / prob_tk;
						if (prob_Ci_tk > max) {
							second_max = max;
							max = prob_Ci_tk;
						} else if (prob_Ci_tk < max && prob_Ci_tk > second_max) {
							second_max = prob_Ci_tk;
						}
					}
					m_CategoryDis[k] = max - second_max;
				}
			}
		}

	}

}
