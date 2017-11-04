package com.rouchtime.nlp.keyword;

import java.util.HashSet;

import com.google.common.base.Objects;

public class Token {
	private String word;
	private String nature;
	private int postion;
	private int area;
	public int getArea() {
		return area;
	}
	public void setArea(int area) {
		this.area = area;
	}

	private final int PRIME = 37;
	public Token(String word, String nature, int postion,int area) {
		super();
		this.word = word;
		this.nature = nature;
		this.postion = postion;
		this.area = area;
	}
	public Token() {
		super();
	}
	public int getPostion() {
		return postion;
	}
	public void setPostion(int postion) {
		this.postion = postion;
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
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(!(obj instanceof Token)) {
			return false;
		}
		Token other = (Token) obj;
		if(word == null || nature == null || other.getWord() == null || other.getNature() == null) {
			return false;
		}
		if(this.word.equals(other.word) && this.nature.equals(other.getNature()) ) {
			return true;
		}
		return false;
	}
	

    public int hashCode() {
    	return Objects.hashCode(word,nature);
   }
	@Override
	public String toString() {
		return "Token [word=" + word + ", nature=" + nature + "]";
	}  
	
    
}
