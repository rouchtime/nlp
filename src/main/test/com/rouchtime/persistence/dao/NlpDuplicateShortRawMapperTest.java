package com.rouchtime.persistence.dao;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.alibaba.fastjson.JSONObject;
import com.rouchtime.persistence.model.NlpDuplicateShortRaw;
import com.rouchtime.util.RegexUtils;

@ContextConfiguration(locations = { "classpath:spring-mybatis.xml" })
public class NlpDuplicateShortRawMapperTest extends AbstractJUnit4SpringContextTests {
	@Autowired
	NlpDuplicateShortRawMapper shortRawMapper;

	@Test
	public void testInsert() throws IOException {
		List<String> list = FileUtils.readLines(new File("D:\\corpus\\duplicate\\dup_video_short_corpus.json"));
		for (String line : list) {
			JSONObject jsonObject = JSONObject.parseObject(line);
			String article = jsonObject.getString("article");
			String url = jsonObject.getString("url");
			Date dateTime = null;
			String newsKey = null;
			try {
				newsKey = RegexUtils.convertURLToNewsKey(url);
				dateTime = RegexUtils.convertURLToDateTime(url);
			} catch (ParseException e) {
				continue;
			} catch (StringIndexOutOfBoundsException e) {
				continue;
			} catch (Exception e) {
				continue;
			}
			NlpDuplicateShortRaw raw = new NlpDuplicateShortRaw();
			raw.setContent(article);
			raw.setUrl(url);
			raw.setNewsKey(newsKey);
			raw.setDatetime(dateTime);
			raw.setTitle(article);
			try {
				int result = shortRawMapper.insertSelective(raw);
			} catch (Exception e) {
				continue;
			}
		}
	}

}
