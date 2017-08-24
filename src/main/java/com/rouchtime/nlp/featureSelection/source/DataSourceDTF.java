package com.rouchtime.nlp.featureSelection.source;

import java.io.IOException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ObjectToDoubleMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.rouchtime.nlp.common.News;
import com.rouchtime.nlp.common.Term;
import com.rouchtime.nlp.corpus.ICorpus;

public class DataSourceDTF extends DataSource {

	private Table<String, String, Double> label_word_tf;
	private Map<String, String> docToLabel;
	private ObjectToCounterMap<String> labelCounter;
	private ObjectToDoubleMap<String> labelWordCount;
	private ObjectToDoubleMap<String> wordDF;
	private TokenizerFactory mfactory;
	private List<Pair<String, String>> mCorpus;

	public DataSourceDTF() throws IOException {
		super();
	}

	@Override
	protected boolean resetImpl(List<Pair<String, String>> corpus, TokenizerFactory factory) throws IOException {
		mCorpus = corpus;
		mfactory = factory;
		docToLabel = new HashMap<String, String>();
		label_word_tf = HashBasedTable.create();
		labelCounter = new ObjectToCounterMap<String>();
		labelWordCount = new ObjectToDoubleMap<String>();
		wordDF = new ObjectToDoubleMap<String>();
		return false;
	}

	@Override
	public boolean load() throws IOException {
		for (Pair<String, String> pair : mCorpus) {
			String raw = pair.getLeft();
			String label = pair.getRight();
			labelCounter.increment(label, 1);
			Set<String> appearedWordIndoc = new HashSet<>();
			for (String term : mfactory.tokenizer(raw.toCharArray(), 0, raw.length())) {
				String word = term.split("/")[0];
				if (appearedWordIndoc.contains(word)) {
					wordDF.increment(word, 1);
					appearedWordIndoc.add(word);
				}
				addToMap(label_word_tf, label, word, 1.0);
				labelWordCount.increment(label, 1.0);
			}
		}
		return true;
	}

	@Override
	public Set<String> getDictionary() {
		return label_word_tf.columnKeySet();
	}

	@Override
	public Set<String> getLabels() {
		return labelCounter.keySet();
	}

	@Override
	public int getLabelCn() {
		int counter = 0;
		for (String key : labelCounter.keySet()) {
			counter += labelCounter.get(key).intValue();
		}
		return counter;
	}

	public int getLabelCn(String label) {
		return labelCounter.get(label).intValue();
	}

	@Override
	public int getDicSize() {
		return label_word_tf.columnKeySet().size();
	}

	@Override
	public double getDocCn() {
		double sum = 0.0;
		for (String label : labelCounter.keySet()) {
			sum += labelCounter.get(label).doubleValue();
		}
		return sum;
	}

	/**
	 * 获得类别下词的数量
	 * 
	 * @param label
	 * @param word
	 * @return
	 */
	public double getWordlabelCn(String label, String word) {
		if (String.valueOf(label_word_tf.get(label, word)).equals("null"))
			return 0.0;
		return label_word_tf.get(label, word);
	}

	/**
	 * 获得文章的类别标注
	 * 
	 * @param doc
	 * @return
	 */
	public String getlabelByDoc(String doc) {
		return docToLabel.get(doc);
	}

	/**
	 * 根据类别获得类别下的所有文章
	 * 
	 * @param label
	 * @return
	 */
	public List<String> getDocsBylabel(String label) {
		List<String> docs = new ArrayList<String>();
		for (String doc : docToLabel.keySet()) {
			if (docToLabel.get(doc).equals(label)) {
				docs.add(doc);
			}
		}
		return docs;
	}

	/**
	 * 获得类别下的总词频
	 * 
	 * @return
	 */
	public Double getLabelWordSize(String label) {
		return labelWordCount.get(label);
	}

	public double getWordDF(String word, boolean useSlow) {
		if (wordDF.containsKey(word))
			return wordDF.get(word);
		else
			return 0;
	}

	public static void main(String[] args) throws IOException {
		Table<String, String, Double> doc_word_tf = HashBasedTable.create();
		doc_word_tf.put("a", "b", (double) 1);
		doc_word_tf.put("a", "c", (double) 1);
		doc_word_tf.put("a", "d", (double) 1);
		doc_word_tf.put("b", "e", (double) 1);
		doc_word_tf.put("d", "e", (double) 1);
		System.out.println(doc_word_tf.get("a", "g"));
	}
}
