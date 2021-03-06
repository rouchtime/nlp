package task;

import java.util.List;

import com.aliasi.classify.ConfusionMatrix;
import com.rouchtime.nlp.common.News;
import com.rouchtime.util.RegexUtils;

import corpus.FeNews2ClassCorpus;
import tokenizer.FudanNLPTokenzierFactory;
import tokenizer.HanLPTokenizerFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

public class FeorNonFeTestTask {
	public static void main(String[] args) throws Exception {

		StopWordTokenierFactory stopTokenizerFactory = new StopWordTokenierFactory(
				HanLPTokenizerFactory.getIstance());
//		StopNatureTokenizerFactory stopNatureFactory = new StopNatureTokenizerFactory(stopTokenizerFactory);
		FeNews2ClassCorpus corpus = new FeNews2ClassCorpus("D:\\corpus\\corpus\\classtest.json", stopTokenizerFactory,
				1.0);

		String[] categories =  new String[] {"0","1"};
		
		@SuppressWarnings("unused")
		
//		ConfusionMatrix confusionMatrix = new ConfusionMatrix(categories);
		ConfusionMatrix confusionMatrix = new ConfusionMatrix(categories);
		MessageClassifier mc = new MessageClassifier(corpus.train().size(), corpus.labels());
		for(News news : corpus.train()) {
			StringBuffer sb = new StringBuffer();
			List<String> words = corpus.words(news.getTitle());
			if(null == words) {
				continue;
			}
			for(String word : words) {
				sb.append(word).append(" ");
			}
			mc.updateData(sb.toString(),news.getLabel());
		}
		mc.evaluateFeatureSelect(1000, "D://abc");
//		for (News news : corpus.test()) {
//			int refereceLabel = news.getLabel().equals("0") ? 0 : 1;
//			String title = news.getTitle();
//			List<String> sents = corpus.sents(title);
//			if (sents == null) {
//				continue;
//			}
//			StringBuffer sb = new StringBuffer();
//			if (sents.size() > 2) {
//				sb.append(sents.get(0)).append("\t").append(sents.get(1));
//			} else if (sents.size() > 1) {
//				sb.append(sents.get(0));
//			} else {
//				continue;
//			}
//			if (title.indexOf("快讯") != -1) {
//				confusionMatrix.increment(refereceLabel, 1);
//				continue;
//			}
//			if (RegexUtils.isExistsDateWord(sb.toString())) {
//				confusionMatrix.increment(refereceLabel, 1);
//				continue;
//			}
//			if (RegexUtils.isExistsDateWord(title)) {
//				confusionMatrix.increment(refereceLabel, 1);
//				continue;
//			}
//			if (title.indexOf("今日") != -1) {
//				confusionMatrix.increment(refereceLabel, 1);
//				continue;
//			}
//			if (title.indexOf("昨日") != -1) {
//				confusionMatrix.increment(refereceLabel, 1);
//				continue;
//			}
//
//			if (RegexUtils.isExistsNewsReportWords(sb.toString())) {
//				confusionMatrix.increment(refereceLabel, 1);
//				continue;
//			}
//			if (RegexUtils.isExistsTimeWord(sb.toString())) {
//				confusionMatrix.increment(refereceLabel, 1);
//				continue;
//			}
//			StringBuffer Sbclassify = new StringBuffer();
//			List<String> words = corpus.words(news.getTitle());
//			if(null == words) {
//				continue;
//			}
//			for(String word : words) {
//				Sbclassify.append(word).append(" ");
//			}
//			int responseLabel = new Double(mc.classifyMessage(Sbclassify.toString())).intValue();
//			confusionMatrix.increment(refereceLabel, responseLabel);
//		}
//		System.out.println("totlaAccuray:" + confusionMatrix.totalAccuracy());
//		System.out.println("macroAvgPrecision:" + confusionMatrix.macroAvgPrecision());
//		System.out.println("macroAvgRecall:" + confusionMatrix.macroAvgRecall());
//		System.out.println("macroAvgFMeasure:" + confusionMatrix.macroAvgFMeasure());
//		System.out.println("非新闻准确率:" + confusionMatrix.oneVsAll(0).accuracy());
//		System.out.println("新闻准确率:" + confusionMatrix.oneVsAll(1).accuracy());
	}
}
