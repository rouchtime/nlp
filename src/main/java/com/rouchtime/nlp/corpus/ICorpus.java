package com.rouchtime.nlp.corpus;

import java.util.List;
import java.util.Set;

public interface ICorpus {
	/**
	 * 获得所有标题
	 * @return
	 */
	public List<String> fileids();
	
	/**
	 * 根据标签返回标题
	 * @param label
	 * @return
	 */
	public List<String> fileidsFromLabel(String label);
	
	/**
	 * 获得所有标签
	 * @return
	 */
	public Set<String> labels();
	
	/**
	 * 根据标题返回标签
	 * @param fileid
	 * @return
	 */
	public String labelFromFileids(String fileid);
	
	/**
	 * 获得所有原始文章
	 * @return
	 */
	public List<String> raw();
	
	/**
	 * 根据标题获得文章
	 * @param fileids
	 * @return
	 */
	public String rawFromFileid(String fileids);
	
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
	public List<String> words();
	
	/**
	 * 根据标题获得分词
	 * @param fileid
	 * @return
	 */
	public List<String> wordsFromFileid(String fileid);
	
	/**
	 * 根据标签获得分词集
	 * @param label
	 * @return
	 */
	public List<String> wordsFromLabel(String label);
	
	/**
	 * 获得所有句子
	 * @return
	 */
	public List<String> sents();
	
	/**
	 * 根据标题获得句子
	 * @param fileid
	 * @return
	 */
	public List<String> sentsFromFileid(String fileid);
	
	/**
	 * 根据标签获得句子
	 * @param fileid
	 * @return
	 */
	public List<String> sentsFromLabel(String fileid);
}
