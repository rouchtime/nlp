package com.rouchtime.nlp.corpus;


import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
@ContextConfiguration(locations = { "classpath:spring-mybatis.xml" })
public class SougouCateCorpusTest extends AbstractJUnit4SpringContextTests{

	@Autowired
	@Qualifier("sougouCateCorpus")
	private SougouCateCorpus sougouCateCorpus;
	@Test
	public void test() {
		System.out.println(sougouCateCorpus.labels());
	}

}
