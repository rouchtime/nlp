package com.rouchtime.nlp.corpus;

import java.util.List;
import java.util.Set;

import com.aliasi.tokenizer.TokenizerFactory;
import com.rouchtime.nlp.common.Term;

public interface ICorpus {
	/**
	 * 获得所有标题
	 * @return
	 */
	public List<String> titles();
	
	/**
	 * 根据标签返回标题
	 * @param label
	 * @return
	 */
	public List<String> titlesFromLabel(String label);
	
	/**
	 * 获得所有标签
	 * @return
	 */
	public Set<String> labels();
	
	/**
	 * 根据标题返回标签
	 * @param Title
	 * @return
	 */
	public String labelFromTitles(String title);
	
	/**
	 * 获得所有原始文章
	 * @return
	 */
	public List<String> raw();
	
	/**
	 * 根据标题获得文章
	 * @param Titles
	 * @return
	 */
	public String rawFromTitle(String titles);
	
	/**
	 * 根据标签获得文章
	 * @param label
	 * @return
	 */
	public List<String> rawFromLabel(String label);
	
	/**
	 * 获得所有分词
	 * @return
	 */
	public List<Term> words(TokenizerFactory factory);
	
	/**
	 * 根据标题获得分词
	 * @param Title
	 * @return
	 */
	public List<Term> wordsFromTitle(String title,TokenizerFactory factory);
	
	/**
	 * 根据标签获得分词集
	 * @param label
	 * @return
	 */
	public List<Term> wordsFromLabel(String label,TokenizerFactory factory);
	
	/**
	 * 获得所有句子
	 * @return
	 */
	public List<String> sents();
	
	/**
	 * 根据标题获得句子
	 * @param Title
	 * @return
	 */
	public List<String> sentsFromTitle(String title);
	
	/**
	 * 根据标签获得句子
	 * @param Title
	 * @return
	 */
	public List<String> sentsFromLabel(String label);
}
