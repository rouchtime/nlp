package task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ObjectToSet;
import com.aliasi.util.ScoredObject;
import com.rouchtime.nlp.corpus.ClassificationCorpus;
import com.rouchtime.nlp.corpus.SougouCateCorpus;
import com.rouchtime.nlp.featureSelection.bean.FeatureSelectionBean;
import com.rouchtime.nlp.featureSelection.selector.DocumentFrequencyFeatureSelector;
import com.rouchtime.util.Contants;
import com.rouchtime.util.RegexUtils;
import com.rouchtime.util.WekaUtils;

import tokenizer.HanLPTokenizerFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

public class EmSougouCorpus extends Corpus<ObjectHandler<Classified<CharSequence>>> {

	private SougouCateCorpus sougouCorpus;

	Map<String, List<String>> labeledCatToTexts;
	List<String> unLabeledCatToTexts;
	Map<String, List<String>> mTestCatToTexts;
	int mMaxSupervisedInstancesPerCategory = 1;

	public void setmMaxUnSupervisedInstancesPerCategory(int mMaxUnSupervisedInstancesPerCategory) {
		this.mMaxUnSupervisedInstancesPerCategory = mMaxUnSupervisedInstancesPerCategory;
	}

	Set<String> dic = new HashSet<String>();
	static final TokenizerFactory TOKENIZER_FACTORY = tokenizerFactory();
	int mMaxUnSupervisedInstancesPerCategory = 1;

	static TokenizerFactory tokenizerFactory() {
		StopWordTokenierFactory stopWordFactory = new StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		StopNatureTokenizerFactory stopNatureFactory = new StopNatureTokenizerFactory(stopWordFactory);
		return stopNatureFactory;
	}

	public EmSougouCorpus(Long seed, int testSize, int trainSize, int unlabelSize) throws IOException {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring-mybatis.xml");
		sougouCorpus = (SougouCateCorpus) applicationContext.getBean(SougouCateCorpus.class);
		featureSelection();
		labeledCatToTexts = new HashMap<String, List<String>>();
		mTestCatToTexts = new HashMap<String, List<String>>();
		unLabeledCatToTexts = new ArrayList<String>();
		ditributeLabelAndUnLabel(seed, testSize, trainSize, unlabelSize);
	}

	private void featureSelection() {
		List<FeatureSelectionBean> trainSet = new ArrayList<FeatureSelectionBean>();
		for (String label : sougouCorpus.labels()) {
			for (String raw : sougouCorpus.rawFromLabel(label)) {
				FeatureSelectionBean fsb = new FeatureSelectionBean();
				fsb.setLabel(label);
				fsb.setRaw(raw);
				trainSet.add(fsb);
			}
		}
		DocumentFrequencyFeatureSelector dfSelector = new DocumentFrequencyFeatureSelector(trainSet, TOKENIZER_FACTORY);
		List<ScoredObject<String>> list = dfSelector.getDocumentFrequency(10000);
		for (ScoredObject<String> score : list) {
			dic.add(score.getObject());
		}
	}

	private void ditributeLabelAndUnLabel(Long seed, int testSize, int trainSize, int unlabelSize) {

		for (String label : sougouCorpus.labels()) {
			Random random = new Random(seed);
			List<String> fileids = sougouCorpus.fileidFromLabel(label);
			int size = fileids.size();
			int totalSize = size;

			for (int i = 0; i < testSize; i++) {
				int index = random.nextInt(size--);
				String raw = RegexUtils.cleanSpecialWord(sougouCorpus.rawFromfileids(fileids.get(index)));
				StringBuffer sb = new StringBuffer();
				for (String token : TOKENIZER_FACTORY.tokenizer(raw.toCharArray(), 0, raw.length())) {
					if (dic.contains(token.split("/")[0])) {
						sb.append(token).append(" ");
					}
				}
				if (mTestCatToTexts.get(label) == null) {
					List<String> list = new ArrayList<String>();
					list.add(sb.toString());
					mTestCatToTexts.put(label, list);
				} else {
					mTestCatToTexts.get(label).add(sb.toString());
				}
				fileids.remove(index);
			}

			for (int i = 0; i < unlabelSize; i++) {
				int index = random.nextInt(size--);
				String raw = RegexUtils.cleanSpecialWord(sougouCorpus.rawFromfileids(fileids.get(index)));
				StringBuffer sb = new StringBuffer();
				for (String token : TOKENIZER_FACTORY.tokenizer(raw.toCharArray(), 0, raw.length())) {
					if (dic.contains(token.split("/")[0])) {
						sb.append(token).append(" ");
					}
				}
				unLabeledCatToTexts.add(sb.toString());
				fileids.remove(index);
			}

			for (int i = 0; i < trainSize && i < size; i++) {
				int index = random.nextInt(size--);
				String raw = RegexUtils.cleanSpecialWord(sougouCorpus.rawFromfileids(fileids.get(index)));
				StringBuffer sb = new StringBuffer();
				for (String token : TOKENIZER_FACTORY.tokenizer(raw.toCharArray(), 0, raw.length())) {
					if (dic.contains(token.split("/")[0])) {
						sb.append(token).append(" ");
					}
				}
				if (labeledCatToTexts.get(label) == null) {
					List<String> list = new ArrayList<String>();
					list.add(sb.toString());

					labeledCatToTexts.put(label, list);
				} else {
					labeledCatToTexts.get(label).add(sb.toString());
				}
				fileids.remove(index);
			}
		}
	}

	public Set<String> categorySet() {
		return labeledCatToTexts.keySet();
	}

	public void setMaxSupervisedInstancesPerCategory(int max) {
		mMaxSupervisedInstancesPerCategory = max;
	}

	public void visitTrain(ObjectHandler<Classified<CharSequence>> handler) {
		visit(labeledCatToTexts, handler, mMaxSupervisedInstancesPerCategory);
	}

	public void visitTest(ObjectHandler<Classified<CharSequence>> handler) {
		visit(mTestCatToTexts, handler, Integer.MAX_VALUE);
	}

	public Corpus<ObjectHandler<CharSequence>> unlabeledCorpus() {
		return new Corpus<ObjectHandler<CharSequence>>() {
			public void visitTest(ObjectHandler<CharSequence> handler) {
				throw new UnsupportedOperationException();
			}

			public void visitTrain(ObjectHandler<CharSequence> handler) {
				for (int i = 0; i < mMaxUnSupervisedInstancesPerCategory * 9 && i < unLabeledCatToTexts.size(); i++) {
					handler.handle(unLabeledCatToTexts.get(i));
				}
			}
		};
	}

	private static void visit(Map<String, List<String>> catToItems, ObjectHandler<Classified<CharSequence>> handler,
			int maxItems) {
		for (Map.Entry<String, List<String>> entry : catToItems.entrySet()) {
			String cat = entry.getKey();
			Classification c = new Classification(cat);
			List<String> texts = entry.getValue();
			for (int i = 0; i < maxItems && i < texts.size(); ++i) {
				Classified<CharSequence> classifiedText = new Classified<CharSequence>(texts.get(i), c);
				handler.handle(classifiedText);
			}
		}
	}

}
