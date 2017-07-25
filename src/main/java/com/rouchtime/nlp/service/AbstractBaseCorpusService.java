package com.rouchtime.nlp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import tk.mybatis.mapper.common.Mapper;


public abstract class AbstractBaseCorpusService<T>{
	
    @Autowired
    protected Mapper<T> mapper;
	protected T getRawFromTitle(T t) {
		return mapper.selectOne(t);
	}
	
	protected List<T> getRawList() {
		return mapper.selectAll();
	}
}
