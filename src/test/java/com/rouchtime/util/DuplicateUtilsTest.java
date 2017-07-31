package com.rouchtime.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.aliasi.tokenizer.StopTokenizerFactory;
import com.rouchtime.nlp.common.News;
import com.rouchtime.nlp.common.Result;
import com.rouchtime.persistence.dao.NlpDuplicateShortRawMapper;
import com.rouchtime.persistence.model.NlpDuplicateShortRaw;

import tokenizer.HanLPTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

@ContextConfiguration(locations = { "classpath:spring-mybatis.xml" })
public class DuplicateUtilsTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	NlpDuplicateShortRawMapper shortRawMapper;

	@Test
	public void testDuplicateRegex() throws IOException {
		List<NlpDuplicateShortRaw> listNlpDup = shortRawMapper.selectAll();
		List<News> listNews = new ArrayList<News>();
		for (NlpDuplicateShortRaw raw : listNlpDup) {
			if(raw.getUrl() == null ) {
				System.out.println(raw.getNewsKey());
				continue;
			}
			News news = new News();
			news.setArticle(raw.getContent());
			news.setUrl(raw.getUrl());
			news.setId(raw.getNewsKey());
			listNews.add(news);
		}
		StopWordTokenierFactory stopFactory = new StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		DuplicateUtils duplicateUtils = new DuplicateUtils(listNews, stopFactory, 90000);
		for (News news : listNews) {
			List<Result> results = duplicateUtils.duplicateShort(news, 0.8);
			if (results.size() < 1) {
				continue;
			}
			if(results.size() == 1 && results.get(0).getNews().getUrl().equals(news.getUrl())) {
				continue;
			}
			FileUtils.write(new File("D://corpus//duplicateTest"), "文章\t\t" + news.getArticle() + "\n", "utf-8", true);
			for (Result result : results) {

				FileUtils.write(new File("D://corpus//duplicateTest"), result.getNews().getArticle() + "\n", "utf-8",
						true);
			}
			FileUtils.write(new File("D://corpus//duplicateTest"), "=============================" + "\n", "utf-8", true);
		}
	}
}
