package com.rouchtime.nlp.keyword;

import java.util.Set;

import com.google.common.base.Objects;
import com.rouchtime.util.Contants;

public class Token {
	private String word;
	private String nature;
	private Integer wordIndex;
	private Double tfIndoc;
	private int wordSpan;
	private int wordPosition;
	private int lastPosition;
	private int tokenExistsSentsNum;
	
	

	public int getTokenExistsSentsNum() {
		return tokenExistsSentsNum;
	}

	public void setTokenExistsSentsNum(int tokenExistsSentsNum) {
		this.tokenExistsSentsNum = tokenExistsSentsNum;
	}

	private Contants.WordArea area;
	
	public Double getTfIndoc() {
		return tfIndoc;
	}

	public void setTfIndoc(Double tfIndoc) {
		this.tfIndoc = tfIndoc;
	}

	public Integer getWordIndex() {
		return wordIndex;
	}

	public void setWordIndex(Integer wordIndex) {
		this.wordIndex = wordIndex;
	}

	public int getWordSpan() {
		return wordSpan;
	}

	public void setWordSpan(int wordSpan) {
		this.wordSpan = wordSpan;
	}

	public int getWordPosition() {
		return wordPosition;
	}

	public void setWordPosition(int wordPosition) {
		this.wordPosition = wordPosition;
	}
	public int getLastPosition() {
		return lastPosition;
	}

	public void setLastPosition(int lastPosition) {
		this.lastPosition = lastPosition;
	}
	

	public Contants.WordArea getArea() {
		return area;
	}

	public void setArea(Contants.WordArea area) {
		this.area = area;
	}

	/**
	 * 词的索引
	 * @param wordIndex
	 * 词
	 * @param word
	 * 词性
	 * @param nature
	 * 词的区域，如标题，正文
	 * @param area
	 * 第一次出现的词和最后一次出现的词的区间
	 * @param wordSpan
	 * 第一次出现词的位置
	 * @param wordPosition
	 */
	public Token(int wordIndex,String word, String nature, Contants.WordArea area, int wordSpan, int wordPosition,int lastPosition) {
		super();
		this.wordIndex = wordIndex;
		this.word = word;
		this.nature = nature;
		this.area = area;
		this.wordSpan = wordSpan;
		this.wordPosition = wordPosition;
		this.lastPosition = lastPosition;
	}

	public Token() {
		super();
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getNature() {
		return nature;
	}

	public void setNature(String nature) {
		this.nature = nature;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Token)) {
			return false;
		}
		Token other = (Token) obj;
		if (word == null || other.getWord() == null) {
			return false;
		}
		if (this.word.equals(other.word)) {
			return true;
		}
		return false;
	}

	public int hashCode() {
		return Objects.hashCode(word);
	}

}
