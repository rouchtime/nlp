package com.rouchtime.nlp.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rouchtime.nlp.model.NlpFinanceNewsNonRaw;

@Component("nlpFinanceNewsNonRawMapper")
public class NlpFinanceNewsNonRawMapperImpl extends SqlSessionDaoSupport implements NlpFinanceNewsNonRawMapper{
	
	
	@Override
	@Autowired
	public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
		super.setSqlSessionFactory(sqlSessionFactory);
	}
	
	@Override
	public int deleteByPrimaryKey(Long id) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int insert(NlpFinanceNewsNonRaw record) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public NlpFinanceNewsNonRaw selectByPrimaryKey(Long id) {
		return getSqlSession().selectOne("com.rouchtime.nlp.dao.NlpFinanceNewsNonRawMapper.selectByPrimaryKey",id);
	}

	@Override
	public List<NlpFinanceNewsNonRaw> selectAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int updateByPrimaryKey(NlpFinanceNewsNonRaw record) {
		// TODO Auto-generated method stub
		return 0;
	}

}
