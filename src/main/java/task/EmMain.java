package task;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.io.FileUtils;

import com.aliasi.classify.Classified;
import com.aliasi.classify.JointClassifier;
import com.aliasi.classify.JointClassifierEvaluator;
import com.aliasi.classify.TradNaiveBayesClassifier;
import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;
import com.aliasi.stats.Statistics;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Factory;
import com.aliasi.util.Strings;

import tokenizer.HanLPTokenizerFactory;
import tokenizer.JiebaTokenizerFactory;
import tokenizer.SpecialWordSplitTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

public class EmMain {

	static final long RANDOM_SEED = 45L;

	static final int NUM_REPLICATIONS = 10;
	static final int MAX_EPOCHS = 20;

	static final double MIN_IMPROVEMENT = 0.0001;

	static final double CATEGORY_PRIOR = 0.005; // balanced, doesn't matter
	static final double TOKEN_IN_CATEGORY_PRIOR = 0.005; // very sensitive to this
	static final double INITIAL_TOKEN_IN_CATEGORY_PRIOR = 0.1; // only used first run; want more uniform
	static final double DOC_LENGTH_NORM = 9.0;
	static final double COUNT_MULTIPLIER = 1.0;
	static final double MIN_COUNT = 0.0001;

	static final TokenizerFactory TOKENIZER_FACTORY = tokenizerFactory();

	public static void main(String[] args) throws IOException, ClassNotFoundException {

		long startTime = System.currentTimeMillis();

		File corpusPath = new File("D:\\20news-bydate");

		System.out.println("CORPUS PATH=" + corpusPath);

		System.out.println("DOC LENGTH NORM=" + DOC_LENGTH_NORM);
		System.out.println("CATEGORY PRIOR=" + CATEGORY_PRIOR);
		System.out.println("TOKEN IN CATEGORY PRIOR=" + TOKEN_IN_CATEGORY_PRIOR);
		System.out.println("INITIAL TOKEN IN CATEGORY PRIOR=" + INITIAL_TOKEN_IN_CATEGORY_PRIOR);

		System.out.println("NUM REPS=" + NUM_REPLICATIONS);
		System.out.println("MAX EPOCHS=" + MAX_EPOCHS);
		System.out.println("RANDOM SEED=" + RANDOM_SEED);
		System.out.println();

		final EmSougouCorpus corpus = new EmSougouCorpus(RANDOM_SEED, 1000, 500, 500);
		Corpus<ObjectHandler<CharSequence>> unlabeledCorpus = corpus.unlabeledCorpus();
		System.out.println(corpus);
		System.out.println();

		Reporter reporter = Reporters.stream(System.out, "utf-8").setLevel(LogLevel.DEBUG);
		FileUtils.write(new File("D://emnbResult.txt"), String.format("%s\t%s\t%s\n", "有标签数据量", "无标签数据量", "准确率"),
				"utf-8", true);
		for (int numSupervisedItems : new Integer[] { 5, 20, 50, 100, 200, 300, 450 }) {

			for (int numUnsupervisedItem : new Integer[] { 50, 100, 200, 300, 400, 500 }) {
				System.out.println("SUPERVISED DOCS/CAT=" + numSupervisedItems);
				corpus.setMaxSupervisedInstancesPerCategory(numSupervisedItems);
				corpus.setmMaxUnSupervisedInstancesPerCategory(numUnsupervisedItem);
				TradNaiveBayesClassifier initialClassifier = new TradNaiveBayesClassifier(corpus.categorySet(),
						TOKENIZER_FACTORY, CATEGORY_PRIOR, INITIAL_TOKEN_IN_CATEGORY_PRIOR, DOC_LENGTH_NORM);

				Factory<TradNaiveBayesClassifier> classifierFactory = new Factory<TradNaiveBayesClassifier>() {
					public TradNaiveBayesClassifier create() {
						return new TradNaiveBayesClassifier(corpus.categorySet(), TOKENIZER_FACTORY, CATEGORY_PRIOR,
								TOKEN_IN_CATEGORY_PRIOR, DOC_LENGTH_NORM);
					}
				};

				TradNaiveBayesClassifier emClassifier = TradNaiveBayesClassifier.emTrain(initialClassifier,
						classifierFactory, corpus, unlabeledCorpus, MIN_COUNT, MAX_EPOCHS, MIN_IMPROVEMENT, reporter);
				FileUtils
						.write(new File("D://emnbResult.txt"),
								String.format("%s\t%s\t%f\n", String.valueOf(numSupervisedItems),
										String.valueOf(numUnsupervisedItem), eval(emClassifier, corpus)),
								"utf-8", true);
				// System.out.printf("ACC=%5.3f EM ACC=%5.3f\n\n", eval(initialClassifier,
				// corpus),
				// eval(emClassifier, corpus));
			}
		}

		reporter.close();

	}

	static TokenizerFactory tokenizerFactory() {
		SpecialWordSplitTokenizerFactory factory = SpecialWordSplitTokenizerFactory.getIstance();
		return factory;
	}

	static double eval(TradNaiveBayesClassifier classifier, Corpus<ObjectHandler<Classified<CharSequence>>> corpus)
			throws IOException, ClassNotFoundException {

		String[] categories = classifier.categorySet().toArray(new String[0]);
		Arrays.sort(categories);
		@SuppressWarnings("unchecked")
		JointClassifier<CharSequence> compiledClassifier = (JointClassifier<CharSequence>) AbstractExternalizable
				.compile(classifier);
		boolean storeInputs = false;
		JointClassifierEvaluator<CharSequence> evaluator = new JointClassifierEvaluator<CharSequence>(
				compiledClassifier, categories, storeInputs);
		corpus.visitTest(evaluator);
		return evaluator.confusionMatrix().totalAccuracy();
	}
}
