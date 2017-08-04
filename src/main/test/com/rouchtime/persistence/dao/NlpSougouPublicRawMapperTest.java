package com.rouchtime.persistence.dao;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.rouchtime.persistence.model.NlpSougouPublicRaw;
@ContextConfiguration(locations = { "classpath:spring-mybatis.xml" })
public class NlpSougouPublicRawMapperTest extends AbstractJUnit4SpringContextTests{

	@Autowired
	NlpSougouPublicRawMapper nlpSougouPublicMapper;
	@Test
	public void test() {
//		nlpSougouPublicMapper.insert(record)
		File[] files = new File("D:\\corpus\\download_corpus\\SogouC.reduced.20061127\\SogouC.reduced\\Reduced").listFiles();
		for(File file : files) {
			String label = file.getName();
			File[] subfiles = file.listFiles();
			for(File subfile : subfiles) {
				try {
					String newsKey = UUID.randomUUID().toString().replace("-", ""); 
					String content = FileUtils.readFileToString(subfile, "GBK");
					NlpSougouPublicRaw nlpSougouPublicRaw = new NlpSougouPublicRaw();
					nlpSougouPublicRaw.setContent(content);
					nlpSougouPublicRaw.setNewsKey(newsKey);
					nlpSougouPublicRaw.setLabel(label);
					nlpSougouPublicMapper.insert(nlpSougouPublicRaw);
				} catch (Exception e) {
					continue;
				}
				
			}
		}
	}

}
