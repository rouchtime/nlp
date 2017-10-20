package task.yule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.aliasi.classify.Classified;
import com.aliasi.classify.JointClassifier;
import com.aliasi.classify.JointClassifierEvaluator;
import com.aliasi.classify.ScoredClassification;
import com.aliasi.classify.ScoredClassifier;
import com.aliasi.classify.TradNaiveBayesClassifier;
import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Factory;

import tokenizer.HanLPTokenizerFactory;

public class EmYuLeCorpus {

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
	static final String outputModel = "D://corpus//category//yule//model//em_nb_20171010.model";
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		System.out.println("DOC LENGTH NORM=" + DOC_LENGTH_NORM);
		System.out.println("CATEGORY PRIOR=" + CATEGORY_PRIOR);
		System.out.println("TOKEN IN CATEGORY PRIOR=" + TOKEN_IN_CATEGORY_PRIOR);
		System.out.println("INITIAL TOKEN IN CATEGORY PRIOR=" + INITIAL_TOKEN_IN_CATEGORY_PRIOR);

		System.out.println("NUM REPS=" + NUM_REPLICATIONS);
		System.out.println("MAX EPOCHS=" + MAX_EPOCHS);
		System.out.println("RANDOM SEED=" + RANDOM_SEED);
		System.out.println();

		final YuLeCorpus corpus = new YuLeCorpus(new File("D:\\corpus\\category\\yule"));
		Corpus<ObjectHandler<CharSequence>> unlabeledCorpus = corpus.unlabeledCorpus();

		Reporter reporter = Reporters.stream(System.out, "utf-8").setLevel(LogLevel.DEBUG);
		TradNaiveBayesClassifier initialClassifier = new TradNaiveBayesClassifier(corpus.categorySet(),
				TOKENIZER_FACTORY, CATEGORY_PRIOR, INITIAL_TOKEN_IN_CATEGORY_PRIOR, DOC_LENGTH_NORM);

		Factory<TradNaiveBayesClassifier> classifierFactory = new Factory<TradNaiveBayesClassifier>() {
			public TradNaiveBayesClassifier create() {
				return new TradNaiveBayesClassifier(corpus.categorySet(), TOKENIZER_FACTORY, CATEGORY_PRIOR,
						TOKEN_IN_CATEGORY_PRIOR, DOC_LENGTH_NORM);
			}
		};

		TradNaiveBayesClassifier emClassifier = TradNaiveBayesClassifier.emTrain(initialClassifier, classifierFactory,
				corpus, unlabeledCorpus, MIN_COUNT, MAX_EPOCHS, MIN_IMPROVEMENT, reporter);
		outputModel(emClassifier);
		category(new File("D:\\corpus\\category\\yule\\yule09\\yule09"));

		reporter.close();

	}

	static ScoredClassifier<CharSequence> compiledClassifier = null;
	@SuppressWarnings("unchecked")
	static String getCategory(String text) {
		String category = null;
		
		try {
			ObjectInputStream oi = new ObjectInputStream(new FileInputStream(outputModel));
			if(compiledClassifier == null) {
				compiledClassifier = (ScoredClassifier<CharSequence>) oi
						.readObject();
			}
			oi.close();
			ScoredClassification channels = compiledClassifier.classify(text
					.subSequence(0, text.length()));
			category = channels.bestCategory();

		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		return category;
	}
	
	static void outputModel(TradNaiveBayesClassifier classifier) {
		System.out.println("Save model to " + outputModel);
		ObjectOutputStream os;
		try {
			os = new ObjectOutputStream(new FileOutputStream(outputModel));
			classifier.compileTo(os);
			os.close();
		} catch (IOException e) {
			System.err.print(ExceptionUtils.getStackTrace(e));
		}
	}
	
	static TokenizerFactory tokenizerFactory() {
		return HanLPTokenizerFactory.getIstance();
	}

	
	@SuppressWarnings("null")
	static String category(File testDir) throws IOException {
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			fis = new FileInputStream(testDir);
			isr = new InputStreamReader(fis, "UTF-8");
			br = new BufferedReader(isr);
			String line = "";
			String[] arrs = null;
			while ((line = br.readLine()) != null) {
				try {

					arrs = line.split("\t");
					String raw = arrs[2];
					String cat = getCategory(raw);
					FileUtils.write(new File("D://yule_output//" + cat + ".txt"), line + "\n","utf-8",true);
				} catch (Exception e) {
					continue;
				}
			}
		} catch (UnsupportedEncodingException e) {
			br.close();
			isr.close();
			fis.close();
		} catch (FileNotFoundException e) {
			br.close();
			isr.close();
			fis.close();
		} catch (IOException e) {
			br.close();
			isr.close();
			fis.close();
		}

		return null;
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
