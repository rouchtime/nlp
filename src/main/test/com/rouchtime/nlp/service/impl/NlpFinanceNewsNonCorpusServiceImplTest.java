package com.rouchtime.nlp.service.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.rouchtime.nlp.corpus.FinanceNewsOrNonCorpus;
import com.rouchtime.persistence.dao.NlpFinanceNewsNonRawMapper;
import com.rouchtime.util.Contants;

import tokenizer.HanLPTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

@ContextConfiguration(locations = { "classpath:spring-mybatis.xml" })
public class NlpFinanceNewsNonCorpusServiceImplTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	@Qualifier("financeNewsOrNonCorpus")
	FinanceNewsOrNonCorpus financeNewsOrNonCorpus;
	@Test
	public void testNlpFinanceNewsNonCorpusService() throws IOException {
//		List<String> linesTotal = FileUtils.readLines(new File("D:\\corpus\\corpus\\new_fe_corpus_url.json"), "utf-8");
//		for (String line : linesTotal) {
//			JSONObject json = JSONObject.parseObject(line);
//			NlpFinanceNewsNonRaw nlpCorpus = new NlpFinanceNewsNonRaw();
//			String url = json.getString("url");
//			String title = json.getString("title");
//			String article = json.getString("article");
//			if(null == url || null == title || null == article) {
//				continue;
//			}
//			nlpCorpus.setContent(json.getString("article"));
//			nlpCorpus.setTitle(json.getString("title"));
//			nlpCorpus.setUrl(json.getString("url"));
//			if (json.getString("label").equals("0")) {
//				nlpCorpus.setLabel("非新闻");
//			} else {
//				nlpCorpus.setLabel("新闻");
//			}
//			nlpCorpus.setNewsKey(getTimeStamp(url));
//			try {
//				nlpFinanceNewsNonRawMapperImpl.insert(nlpCorpus);
//			}catch(Exception e) {
//				System.out.println(url);
//				continue;
//			}
//		}

	}

	private static String getTimeStamp(String url) {
		SimpleDateFormat sdf = new SimpleDateFormat(Contants.URL_TIME_REGEX);
		String s_time = url.substring(url.lastIndexOf(Contants.SLASH) + 1, url.lastIndexOf(Contants.DOT));
		if (s_time.length() == Contants.NEWS_URL_LENGTH) {
			return s_time;
		}
		if (s_time.length() == Contants.VIDEO_PIC_URL_LENGTH) {
			s_time = s_time.substring(0, s_time.length() - (Contants.VIDEO_PIC_URL_LENGTH - Contants.NEWS_URL_LENGTH));
			return s_time;
		}
		return null;
	}
}
