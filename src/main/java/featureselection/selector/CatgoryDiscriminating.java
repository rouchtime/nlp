package featureselection.selector;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Clock;

import org.apache.commons.io.FileUtils;

import com.aliasi.classify.TfIdfClassifierTrainer;
import com.aliasi.features.BoundedFeatureExtractor;
import com.aliasi.features.KnownFeatureExtractor;
import com.aliasi.features.ZScoreFeatureExtractor;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.ScoredObject;

import corpus.FinanceNewsOrNonCorpus;
import corpus.ICorpus;
import featureselection.dataoperation.DataSource;
import featureselection.dataoperation.DataSourceDTF;
import featureselection.dataoperation.SimpleDataSourcePool;
import tokenizer.HanLPTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

/**
 * 类区分度特征提取器 
 * @author 龚帅宾
 *
 */
public class CatgoryDiscriminating implements WeightingMethod {

	protected DataSource ds;
	protected Double threshold;
	ObjectToDoubleMap<String> result;
	private CatgoryDiscriminating(DataSource ds, double threshold) {
		this.ds = ds;
		this.threshold = threshold;
		this.result = new ObjectToDoubleMap<String>();
	}

	public static CatgoryDiscriminating build(ICorpus corpus) {
		DataSource dsdf = null;
		try {
			dsdf = SimpleDataSourcePool.create(corpus, DataSourceDTF.class);
		} catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException
				| IOException e) {
			System.err.println("Build ChiSquare instance fail: " + e.getMessage());
		}
		return new CatgoryDiscriminating(dsdf, 0.0);
	}

	/**
	 * 
	 * @param corpus
	 *            要导入的语料库
	 * @param threshold
	 *            类别区分词的最大后验概率与次大后延概率的差值，一般取0.3
	 * @return
	 */
	public static CatgoryDiscriminating build(ICorpus corpus, Double threshold) {
		DataSource dsdf = null;
		try {
			dsdf = SimpleDataSourcePool.create(corpus, DataSourceDTF.class);
		} catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException
				| IOException e) {
			System.err.println("Build ChiSquare instance fail: " + e.getMessage());
		}
		return new CatgoryDiscriminating(dsdf, threshold);
	}

	@Override
	public boolean computeAndPrint(String output_dic) throws IOException {
		if(compute()&&print(output_dic)) {
			return true;
		}
		return false;
	}

	@Override
	public ObjectToDoubleMap<String> result() {
		compute();
		return result;
	}

	@Override
	public boolean compute() {
		DataSourceDTF dsdf = (DataSourceDTF) ds;
		int programCount = 0;
		System.out.println("total word count:" + dsdf.getDicSize());
		long stime = Clock.systemDefaultZone().millis();
		double V = dsdf.getDictionary().size();
		for (String word : dsdf.getDictionary()) {
			programCount++;
			double probWord = 0.0;

			/* 计算p(t) */
			for (String label : dsdf.getLabels()) {
				// 获得词的总频数
				double totalwordlabelTF = dsdf.getLabelWordSize(label);
				// P(Ci)
				double probLabel = (dsdf.getLabelCn(label) * 1.0) / (dsdf.getLabelCn() * 1.0);
				double wordLabelTF = dsdf.getWordlabelCn(label, word);
				double probWordBylabel = (1 + wordLabelTF) / (V + totalwordlabelTF);
				probWord += probWordBylabel * probLabel;
			}
			double maxProbPosteriorWord = 0.0;
			double secondMaxProbPosteriorWord = 0.0;
			for (String label : dsdf.getLabels()) {
				double totalwordlabelTF = dsdf.getLabelWordSize(label);
				// P(Ci)
				double probLabel = (dsdf.getLabelCn(label) * 1.0) / (dsdf.getLabelCn() * 1.0);
				double wordLabelTF = dsdf.getWordlabelCn(label, word);
				double probWordBylabel = (1 + wordLabelTF) / (V + totalwordlabelTF);

				// 找到该词的第一大后验概率和第二大后验概率
				double probPosteriorWord = (probWordBylabel * probLabel) / probWord;
				if (probPosteriorWord > maxProbPosteriorWord) {
					secondMaxProbPosteriorWord = maxProbPosteriorWord;
					maxProbPosteriorWord = probPosteriorWord;
				} else if (probPosteriorWord < maxProbPosteriorWord && probPosteriorWord > secondMaxProbPosteriorWord) {
					secondMaxProbPosteriorWord = probPosteriorWord;
				}
			}
			result.put(word, maxProbPosteriorWord - secondMaxProbPosteriorWord);
			if ((programCount % 1000 == 0)) {
				System.out.println(programCount);
				System.out.println(" using " + (Clock.systemDefaultZone().millis() - stime) *1.0 / 1000);
			}
		}
		return true;
	
	}

	@Override
	public boolean print(String output_dic) {
		try {

			for (ScoredObject<String> scoredObject : result.scoredObjectsOrderedByValueList()) {
				if (scoredObject.score() > threshold) {
					FileUtils.write(new File(output_dic), scoredObject.getObject() + "\t" + scoredObject.score() + "\n",
							true);
				}
			}
		} catch(Exception e) {
			return false;
		}
		return true;
	}

	


	public static void main(String[] args) throws IOException {
		StopWordTokenierFactory stopTokenizerFactory = new StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		FinanceNewsOrNonCorpus corpus = new FinanceNewsOrNonCorpus(stopTokenizerFactory);
		WeightingMethod weightingMethod = CatgoryDiscriminating.build(corpus);
		weightingMethod.computeAndPrint("D://corpus//featureSelection//categoryDis_stopwords_hanLP");
	}
}
