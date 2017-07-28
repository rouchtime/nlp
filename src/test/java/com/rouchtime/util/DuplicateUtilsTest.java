package com.rouchtime.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.rouchtime.persistence.dao.NlpDuplicateShortRawMapper;
@ContextConfiguration(locations = { "classpath:spring-mybatis.xml" })
public class DuplicateUtilsTest extends AbstractJUnit4SpringContextTests{
	
	@Autowired
	NlpDuplicateShortRawMapper shortRawMapper;
}
