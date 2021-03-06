package com.rouchtime.persistence.dao;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.rouchtime.persistence.model.NlpGuojiRaw;
import com.rouchtime.util.RegexUtils;

@ContextConfiguration(locations = { "classpath:spring-mybatis.xml" })
public class NlpGuojiRawMapperTest extends AbstractJUnit4SpringContextTests {
	private Logger log = Logger.getLogger(NlpGuojiRawMapperTest.class);
	@Autowired
	private NlpGuojiRawMapper nlpGuojiRawMapper;

	@Test
	public void test() throws IOException {
		File[] files = new File("D:\\corpus\\category\\guoji\\nonduplicate_guoji").listFiles();
		for (File file : files) {
			File[] subFiles = file.listFiles();
			for (File subFile : subFiles) {
				List<String> lines = FileUtils.readLines(subFile, "utf-8");
				for (String line : lines) {
					try {
						String[] splits = line.split("\t+");
						NlpGuojiRaw raw = new NlpGuojiRaw();
						raw.setNewsKey(RegexUtils.convertURLToNewsKey(splits[0]));
						raw.setLabel(subFile.getName().replaceAll(".txt", ""));
						raw.setTitle(splits[1]);
						StringBuffer sb = new StringBuffer();
						for (int i = 2; i < splits.length; i++) {
							sb.append(splits[i]);
						}
						raw.setContent(RegexUtils.cleanParaAndImgLabel(sb.toString()));
						raw.setUrl(splits[0]);
						nlpGuojiRawMapper.insert(raw);
					} catch (DuplicateKeyException e) {
						log.error("duplicate_error" + file.getName() + "\t\t" + line);
					} catch (Exception e) {
						log.error("other_error" + file.getName() + "\t\t" + line);
					}
				}
			}
		}
	}

	@Test
	public void suppliment() throws IOException {
		File[] files = new File("D:\\\\corpus\\\\category\\\\guoji\\\\suppliment").listFiles();
		for (File file : files) {
			List<String> lines = FileUtils.readLines(file, "utf-8");
			for (String line : lines) {
				try {
					String[] splits = line.split("\t+");
					NlpGuojiRaw raw = new NlpGuojiRaw();
					if (splits.length == 2) {
						raw.setNewsKey(RegexUtils.convertURLToNewsKey(splits[0]));
						raw.setLabel(file.getName().substring(0, 4));
						raw.setContent(RegexUtils.cleanParaAndImgLabel(splits[1]));
						raw.setUrl(splits[0]);
						nlpGuojiRawMapper.insert(raw);
						continue;
					}
					raw.setNewsKey(RegexUtils.convertURLToNewsKey(splits[0]));
					raw.setLabel(file.getName().substring(0, 4));
					raw.setTitle(splits[1]);
					StringBuffer sb = new StringBuffer();
					for (int i = 2; i < splits.length; i++) {
						sb.append(splits[i]);
					}
					raw.setContent(RegexUtils.cleanParaAndImgLabel(sb.toString()));
					raw.setUrl(splits[0]);
					nlpGuojiRawMapper.insert(raw);
				} catch (DuplicateKeyException e) {
					log.error("duplicate_error" + file.getName() + "\t\t" + line);
				} catch(StringIndexOutOfBoundsException e) {
					log.error("outofbounds" + file.getName() + "\t\t" + line);
				}catch (Exception e) {
					log.error("other_error" + file.getName() + "\t\t" + line);
				}

			}
		}

	}

	@Test
	public void testFailed() throws IOException {
		List<String> lines = FileUtils.readLines(new File("D:\\corpus\\category\\guoji\\failed_guoji_corspus.txt"),
				"utf-8");
		for (String line : lines) {
			try {
				String[] splits = line.split("\t+");
				NlpGuojiRaw raw = new NlpGuojiRaw();
				raw.setNewsKey(RegexUtils.convertURLToNewsKey(splits[1]));
				NlpGuojiRaw findExists = nlpGuojiRawMapper.selectOne(raw);
				if (findExists != null) {
					System.out.println("dup");
					continue;
				}
				nlpGuojiRawMapper.insert(raw);
			} catch (Exception e) {
				System.out.println(ExceptionUtils.getRootCauseMessage(e));
			}
		}
	}

}
