package task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.aliasi.util.ScoredObject;
import com.rouchtime.nlp.corpus.GuojiCorpus;
import com.rouchtime.nlp.corpus.SougouCateCorpus;
import com.rouchtime.nlp.featureSelection.selector.CategoryDiscriminatingFeatureSelector;
import com.rouchtime.nlp.featureSelection.selector.ChiFeatureSelector;
import com.rouchtime.nlp.featureSelection.selector.DocumentFrequencyFeatureSelector;
import com.rouchtime.util.RegexUtils;

import tokenizer.HanLPTokenizerFactory;
import tokenizer.JiebaTokenizerFactory;
import tokenizer.NGramTokenizerBasedOtherTokenizerFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

public class FeatureCalculate {
	public static void main(String[] args) throws IOException {
	}
}
