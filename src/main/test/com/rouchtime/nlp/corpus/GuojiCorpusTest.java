package com.rouchtime.nlp.corpus;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.rouchtime.util.RegexUtils;
import com.rouchtime.util.WekaUtils;

import tokenizer.HanLPTokenizerFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

@ContextConfiguration(locations = { "classpath:spring-mybatis.xml" })
public class GuojiCorpusTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	@Qualifier("guojiCorpus")
	private GuojiCorpus guojiCorpus;

	@Test
	public void test() throws IOException {
		StopWordTokenierFactory stopWordFactory = new StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		StopNatureTokenizerFactory stopNatureTokenizerFactory = new StopNatureTokenizerFactory(stopWordFactory);
		for (String fileid : guojiCorpus.fileids()) {
			String line = WekaUtils.formWekaArffTextFromRaw(
					RegexUtils.cleanSpecialWord(guojiCorpus.rawFromfileids(fileid)), stopNatureTokenizerFactory,
					guojiCorpus.labelFromfileids(fileid));
			FileUtils.write(new File("D://corpus//category//guoji//weka_guoji.arff"), line + "\n", "utf-8", true);
		}
	}
}
