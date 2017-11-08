package com.rouchtime.nlp.keyword.extraction;

import java.util.List;
import java.util.Map;

import com.aliasi.util.ScoredObject;

public interface KeyWordExtraction {
	public List<String> keywordsExtract(String title,String article,int keywordNum);
	public List<ScoredObject<String>> keywordsScore(String title,String article,int keywordNum);
}
