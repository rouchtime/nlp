package task;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.TradNaiveBayesClassifier;
import com.aliasi.tokenizer.TokenizerFactory;
import com.rouchtime.nlp.corpus.SougouCateCorpus;
import com.rouchtime.util.CommonUtils;

import tokenizer.HanLPTokenizerFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

public class TradNBLearning {
	private static SougouCateCorpus SOUGOUCORPUS = getCorpus("sougouCateCorpus");
	private static TokenizerFactory TOKENIZERFACTORY = getFactory();

	public static void main(String[] args) {
		List<String> labels = SOUGOUCORPUS.labels();
		Set<String> categorySet = new HashSet<String>(labels);
		TradNaiveBayesClassifier tnbClassifier = new TradNaiveBayesClassifier(categorySet, TOKENIZERFACTORY);
		for (String label : SOUGOUCORPUS.labels()) {
			Classification classification = new Classification(label);
			for (String fileid : SOUGOUCORPUS.fileidFromLabel(label)) {
				StringBuffer content = CommonUtils.jointMultipleTitleAndRaw(3,SOUGOUCORPUS.rawFromfileids(fileid),
						SOUGOUCORPUS.titleFromfileids(fileid));
				Classified<CharSequence> classified = new Classified<CharSequence>(content, classification);
				tnbClassifier.handle(classified);
			}
		}
	}

	private static TokenizerFactory getFactory() {
		StopWordTokenierFactory stopTokenizerFactory = new StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		StopNatureTokenizerFactory stopNatureFactory = new StopNatureTokenizerFactory(stopTokenizerFactory);
		return stopNatureFactory;
	}

	private static SougouCateCorpus getCorpus(String string) {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-mybatis.xml");
		SougouCateCorpus corpus = (SougouCateCorpus) applicationContext.getBean("sougouCateCorpus");
		return corpus;
	}
}
