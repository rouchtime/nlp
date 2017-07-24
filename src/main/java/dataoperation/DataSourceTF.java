package dataoperation;

import com.aliasi.util.ObjectToDoubleMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import corpus.ICorpus;
import pojo.News;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.*;

/**
 * Created by py on 16-9-21.
 */
public class DataSourceTF extends DataSource {

	private DataSourceTF() throws IOException {

	}

	private ObjectToDoubleMap<String> wordTF;

	private ObjectToDoubleMap<String> labelTF;

	// 每种类别下，每个词的词频
	private Table<String, String, Double> label_word_tf;

	@Override
	protected boolean resetImpl(ICorpus corpus) {
		wordTF = new ObjectToDoubleMap<String>();
		labelTF = new ObjectToDoubleMap<String>();
		label_word_tf = HashBasedTable.create();

		return false;
	}

	@Override
	public boolean load(ICorpus corpus) throws IOException {
		long stime = Clock.systemDefaultZone().millis();
		System.out.print("load datasourceDF from " + corpus.path() + "...");

		// 得到各个类文件夹的path
		Set<String> labels = corpus.labels();
		for (String cp : labels) { // 遍历每个类别文件夹，读取文档

			String label = cp;// 类别名称
			int wordCount = 0;
			List<News> fileids = corpus.newsFromLabel(label);
			for (News fileid : fileids) {
				List<String> words = corpus.words(fileid.getTitle());
				wordCount += words.size();
				for (String word : words) {
					wordTF.increment(word, 1.0);
					// 更新词在某类下的词频
					double v = 0.0;
					if (label_word_tf.contains(label, word)) {
						v = label_word_tf.get(label, word);
					}
					label_word_tf.put(label, word, v + 1);
				}
			}
			labelTF.increment(label, wordCount * 1.0);
		}
		System.out.println(" using " + (Clock.systemDefaultZone().millis() - stime) * 0.5 / 1000);
		return true;
	}

	// 得到词典
	@Override
	public Set<String> getDictionary() {
		return wordTF.keySet();
	}

	// 得到所有类别标识
	@Override
	public Set<String> getLabels() {
		return labelTF.keySet();
	}

	// 类别数
	@Override
	public int getLabelCn() {
		return labelTF.size();
	}

	// 得到词典的大小
	@Override
	public int getDicSize() {
		return getDictionary().size();
	}

	// 文档数
	@Override
	public double getDocCn(boolean useSlow) {
		Double sum = 0.0;
		for (String label : labelTF.keySet()) {
			sum += labelTF.get(label);
		}
		return sum;
	}

	// 得到一个类下，一个词的词频
	public double getWordTF(String label, String word) {
		if (label_word_tf.contains(label, word))
			return label_word_tf.get(label, word);
		else
			return 0;
	}

	// 得到整个语料库中，一个词的词频
	public double getWordTF(String word) {
		if (wordTF.containsKey(word)) {
			return wordTF.get(word);
		} else
			return 0;
	}

	// 得到一个类下所有词出现的频率之和
	public double getLabelTF(String label) {
		if (labelTF.containsKey(label))
			return labelTF.get(label);
		else
			return 0;
	}
}
