package com.rouchtime.persistence.dao;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.rouchtime.persistence.model.NlpClassificationRaw;
import com.rouchtime.util.RegexUtils;

import tk.mybatis.mapper.entity.Example;

@ContextConfiguration(locations = { "classpath:spring-mybatis.xml" })
public class NlpClassificationRawMapperTest extends AbstractJUnit4SpringContextTests {
	private Logger log = Logger.getLogger(NlpClassificationRawMapperTest.class);
	@Autowired
	NlpClassificationRawMapper nlpClassificationRawMapper;
	private final static String JUNSHIDIR = "D:\\corpus\\category\\junshi\\17";

	@Test
	public void test() throws IOException {
		Map<String, String> tableLabel = new HashMap<String, String>();
		List<String> liness = FileUtils.readLines(new File("D:\\guonei.txt"), "utf-8");
		for (String line : liness) {
			String cname = line.split("\t")[3];
			tableLabel.put(cname, line);
		}
		File[] files = new File("D:\\corpus\\category\\guonei\\v1").listFiles();
		for (File file : files) {
			if (!file.getName().equals("生活休闲.txt")) {
				continue;
			}
			List<String> lines = FileUtils.readLines(file, "utf-8");
			for (String line : lines) {
				try {
					String[] splits = line.split("\t+");
					NlpClassificationRaw raw = new NlpClassificationRaw();
					if (splits.length == 2) {
						log.error("line.length==2:" + line);
						continue;
					}
					raw.setNewsKey(RegexUtils.convertURLToNewsKey(splits[0]));
					String label = file.getName().replaceAll(".txt", "");
					raw.setThirdLabel(tableLabel.get(label).split("\t")[0]);
					raw.setSecondLabel(tableLabel.get(label).split("\t")[1]);
					raw.setLabel(label);
					raw.setTitle(splits[1]);
					raw.setFirstLabel("guonei");
					StringBuffer sb = new StringBuffer();
					for (int i = 2; i < splits.length; i++) {
						if (splits[i].equals("guonei") || splits[i].equals("shehui")) {
							continue;
						}
						sb.append(splits[i]);
					}
					raw.setContent(RegexUtils.cleanSpecialWord(RegexUtils.cleanParaAndImgLabel(sb.toString())));
					raw.setUrl(splits[0]);

					NlpClassificationRaw raw1 = new NlpClassificationRaw();
					raw1.setNewsKey(raw.getNewsKey());
					if (nlpClassificationRawMapper.selectOne(raw1) != null) {
						log.info("duplicate" + file.getName() + "\t\t" + line);
						continue;
					}
					nlpClassificationRawMapper.insert(raw);
				} catch (DuplicateKeyException e) {
					log.error("duplicate_error" + file.getName() + "\t\t" + line);
				} catch (StringIndexOutOfBoundsException e) {
					log.error("outofbounds" + file.getName() + "\t\t" + line);
				} catch (Exception e) {
					log.error("other_error" + file.getName() + "\t\t" + line);
				}
			}
		}
	}

	@Test
	public void insert() throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		for (String line : FileUtils.readLines(new File("D:\\corpus\\category\\config.txt"))) {
			String[] splits = line.split("\t");
			map.put(splits[3], line);
		}

		int dupcount = 0;
		File[] files = new File(JUNSHIDIR).listFiles();
		for (File file : files) {
			File[] subfiles = file.listFiles();
			for (File subfile : subfiles) {
				List<String> lines = FileUtils.readLines(subfile, "utf-8");
				for (String line : lines) {
					NlpClassificationRaw raw = new NlpClassificationRaw();
					String label = subfile.getName().replaceAll(".txt", "");
					if (map.get(label) == null) {
						continue;
					}
					String labelLine = map.get(label);
					String[] labelSplits = labelLine.split("\t");
					String[] splits = line.split("\t+");
					try {
						if (splits.length <= 2) {
							log.error("line.length==2:" + line);
							continue;
						}
						raw.setNewsKey(RegexUtils.convertURLToNewsKey(splits[0]));
						raw.setLabel(label);
						raw.setTitle(splits[1]);
						raw.setSecondLabel(labelSplits[1]);
						raw.setThirdLabel(labelSplits[0]);
						raw.setFirstLabel("junshi");
						StringBuffer sb = new StringBuffer();
						for (int i = 2; i < splits.length; i++) {
							sb.append(splits[i]);
						}
						String article  = RegexUtils.cleanSpecialWord(RegexUtils.cleanParaAndImgLabel(sb.toString()));
						if (article.length() < 30) {
							FileUtils.write(new File("D://junshi_short.txt"), String.format("%s\t%s\n", line, label),
									"utf-8", true);
						}
						
						raw.setContent(article);
						raw.setUrl(splits[0]);

						NlpClassificationRaw raw1 = new NlpClassificationRaw();
						raw1.setNewsKey(raw.getNewsKey());
						if (nlpClassificationRawMapper.selectOne(raw1) != null) {
							dupcount++;
							continue;
						}
						nlpClassificationRawMapper.insert(raw);
					} catch (DuplicateKeyException e) {
						continue;
					} catch (StringIndexOutOfBoundsException e) {
//						log.error("outofbounds" + file.getName() + "\t\t" + line);
						FileUtils.write(new File("D://junshi_other.txt"), String.format("%s\t%s\n", line, label),
								"utf-8", true);
						continue;
					} catch (Exception e) {
//						log.error("other_error" + file.getName() + "\t\t" + line);
						FileUtils.write(new File("D://junshi_other.txt"), String.format("%s\t%s\n", line, label),
								"utf-8", true);
						continue;
					}
				}
			}
		}
		log.error("duplicate_error" + "\t\t" + dupcount++);
	}

	@Test
	public void insert2() throws IOException {
		int count = 0;
		Map<String, String> map = new HashMap<String, String>();
		for (String line : FileUtils.readLines(new File("D:\\corpus\\category\\config.txt"))) {
			String[] splits = line.split("\t");
			map.put(splits[3], line);
		}
		for (String line : FileUtils.readLines(new File("D://junshi_other.txt"), "utf-8")) {
			NlpClassificationRaw raw = new NlpClassificationRaw();
			String[] splits = line.split("\t+");
			try {

				if (splits.length <= 2) {
					log.error("line.length==2:" + line);
					continue;
				}
				raw.setNewsKey(UUID.randomUUID().toString().replaceAll("-", ""));
				raw.setLabel(splits[splits.length - 1]);
				if (map.get(raw.getLabel()) == null) {
					continue;
				}
				String labelLine = map.get(raw.getLabel());
				String[] labelSplits = labelLine.split("\t");
				raw.setTitle(splits[1]);
				raw.setFirstLabel("junshi");
				StringBuffer sb = new StringBuffer();
				for (int i = 2; i < splits.length; i++) {
					sb.append(splits[i]);
				}
				raw.setSecondLabel(labelSplits[1]);
				raw.setThirdLabel(labelSplits[0]);
				raw.setContent(RegexUtils.cleanSpecialWord(RegexUtils.cleanParaAndImgLabel(sb.toString())));
				raw.setUrl(splits[0]);
				nlpClassificationRawMapper.insert(raw);
				count++;
			} catch (DuplicateKeyException e) {
				log.error("other_error" + "\t\t" + line);
				continue;
			} catch (StringIndexOutOfBoundsException e) {
				log.error("other_error" + "\t\t" + line);
				continue;
			} catch (Exception e) {
				log.error("other_error" + "\t\t" + line);
				continue;
			}
		}
		System.out.println(count);
	}

	public void update() {
		Example example = new Example(NlpClassificationRaw.class);
		example.createCriteria().andCondition("first_label", "junshi");
		List<NlpClassificationRaw> list = nlpClassificationRawMapper.selectByExample(example);
		for (NlpClassificationRaw raw : list) {
			if (raw.getLabel().equals("")) {

			}
		}
	}

	@Test
	public void insertPart() throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		for (String line : FileUtils.readLines(new File("D:\\corpus\\category\\config.txt"))) {
			String[] splits = line.split("\t");
			map.put(splits[3], line);
		}
		File[] files = new File(JUNSHIDIR).listFiles();
		for (File file : files) {
			List<String> lines = FileUtils.readLines(file, "utf-8");
			for (String line : lines) {

				String label = file.getName().replaceAll(".txt", "");
				NlpClassificationRaw raw = new NlpClassificationRaw();
				
				String[] splits = line.split("\t+");
				try {
					if (splits.length <= 2) {
						log.error("line.length==2:" + line);
						continue;
					}
					raw.setLabel(label);
					if (map.get(raw.getLabel()) == null) {
						continue;
					}
					String labelLine = map.get(raw.getLabel());
					String[] labelSplits = labelLine.split("\t");
					raw.setSecondLabel(labelSplits[1]);
					raw.setThirdLabel(labelSplits[0]);
					raw.setNewsKey(RegexUtils.convertURLToNewsKey(splits[0]));
					raw.setTitle(splits[1]);
					raw.setFirstLabel("junshi");
					StringBuffer sb = new StringBuffer();
					for (int i = 2; i < splits.length; i++) {
						sb.append(splits[i]);
					}
					raw.setContent(RegexUtils.cleanSpecialWord(RegexUtils.cleanParaAndImgLabel(sb.toString())));
					raw.setUrl(splits[0]);

					NlpClassificationRaw raw1 = new NlpClassificationRaw();
					raw1.setNewsKey(raw.getNewsKey());
					if (nlpClassificationRawMapper.selectOne(raw1) != null) {
						log.info("duplicate" + file.getName() + "\t\t" + line);
						continue;
					}
					nlpClassificationRawMapper.insert(raw);
				} catch (DuplicateKeyException e) {
					log.error("duplicate_error" + file.getName() + "\t\t" + line);
					continue;
				} catch (StringIndexOutOfBoundsException e) {
					log.error("outofbounds" + file.getName() + "\t\t" + line);
					FileUtils.write(new File("D://junshi_other.txt"), String.format("%s\t%s\n", line, label), "utf-8",
							true);
					continue;
				} catch (Exception e) {
					log.error("other_error" + file.getName() + "\t\t" + line);
					FileUtils.write(new File("D://junshi_other.txt"), String.format("%s\t%s\n", line, label), "utf-8",
							true);
					continue;
				}

			}
		}
	}

	public void testOutput() {

	}

	@Test
	public void testSelect() {
		NlpClassificationRaw raw1 = new NlpClassificationRaw();
		raw1.setNewsKey("170808124825271");
		if (nlpClassificationRawMapper.selectOne(raw1) != null) {
			System.out.println("duplicate");
		}
	}

}
