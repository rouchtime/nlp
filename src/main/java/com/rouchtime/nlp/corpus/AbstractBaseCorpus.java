package com.rouchtime.nlp.corpus;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.rouchtime.nlp.dao.BaseRawMapper;

public abstract class AbstractBaseCorpus<T> implements ICorpus {
	
	
	
	public BaseRawMapper<T> getMapper(T model) {
		model.getClass().getName();
		return null;
	}
	
	@Override
	public List<String> fileids() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> fileidsFromLabel(String label) {
		return null;
	}

	@Override
	public Set<String> labels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String labelFromFileids(String fileid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> raw() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String rawFromFileid(String fileids) {
		return null;
	}

	@Override
	public List<String> rawFromLabel(String label) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> words() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> wordsFromFileid(String fileid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> wordsFromLabel(String label) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> sents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> sentsFromFileid(String fileid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> sentsFromLabel(String fileid) {
		// TODO Auto-generated method stub
		return null;
	}
}
