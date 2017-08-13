package corpus;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ObjectToDoubleMap;
import com.rouchtime.nlp.featureSelection.selector.CatgoryDiscriminating;
import com.rouchtime.nlp.featureSelection.selector.WeightingMethod;

import junit.framework.TestCase;
import task.MessageClassifier;
import tokenizer.FudanNLPTokenzierFactory;
import tokenizer.HanLPTokenizerFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.core.converters.LibSVMLoader;

public class WekaTest extends TestCase {
//	public static String convertCategory(String label) {
//		if (label.equals("0")) {
//			return "nonnews";
//		}
//		if (label.equals("1")) {
//			return "maynews";
//		}
//		if (label.equals("2")) {
//			return "news";
//		}
//		return null;
//	}
//
//	public void testStringToVector() throws Exception {
//		StopWordTokenierFactory stopTokenizerFactory = new StopWordTokenierFactory(
//				FudanNLPTokenzierFactory.getIstance());
//		StopNatureTokenizerFactory stopNatureFactory = new StopNatureTokenizerFactory(stopTokenizerFactory);
//		FinanceNewsOrNonCorpus corpus = new FinanceNewsOrNonCorpus(stopNatureFactory,"D://corpus//isnews_caijing.json");
////		FinanceNewsOrNonCorpus corpus = new FinanceNewsOrNonCorpus(stopNatureFactory);
//		WeightingMethod weightingMethod = CatgoryDiscriminating.build(corpus);
//		ObjectToDoubleMap<String> filter = weightingMethod.result();
//		for (int i = 1; i < 10; i++) {
//			MessageClassifier mc = new MessageClassifier(corpus.fileids().size(),corpus.labels());
//			for (String label : corpus.labels()) {
//				for (String title : corpus.fileidsFromLabel(label)) {
//					StringBuffer sb = new StringBuffer();
//					for (String word : corpus.words(title)) {
//
//						Double value = filter.get(word);
//						if (value != null && value > 0.1 * i) {
//							sb.append(word.replaceAll("'", "")).append(" ");
//						}
//					}
//						mc.updateData(sb.toString(), label);
//					
//				}
//			}
//			mc.printEvaluate("D://corpus//realtimeNewsResult.txt", "\nesult" + 0.01 * i);
//		}
//
//		// mc.evaluate();
//		// mc.writeModel();
//	}
}
